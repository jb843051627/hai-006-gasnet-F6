package com.fc.v2.util;

import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;

import com.fc.v2.model.auto.TGasPipelineArch;
import com.fc.v2.model.auto.TGasRegulatorStationArch;

import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;

/**
 * 管道/调压站档案台账导出工具：两个 Sheet（管道档案 / 调压站档案）。
 * 是否包含停用/报废历史档案由服务层按 includeHistory 决定，本工具只负责写出。
 *
 * @author fuce
 * @date 2026-09-29
 */
public class GasArchiveLedgerExcelUtil {

    private static final ThreadLocal<SimpleDateFormat> TS =
            ThreadLocal.withInitial(() -> new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));

    private GasArchiveLedgerExcelUtil() {
    }

    /** 状态码 → 台账文本 */
    public static String statusText(Integer status) {
        if (status == null) {
            return "";
        }
        switch (status) {
            case 0:
                return "在用";
            case 1:
                return "停用";
            case 2:
                return "报废";
            default:
                return String.valueOf(status);
        }
    }

    private static String time(java.util.Date date) {
        return date == null ? "" : TS.get().format(date);
    }

    private static String num(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    /**
     * 写出台账（管道 Sheet + 调压站 Sheet）。
     *
     * @param pipes    管道档案数据（是否含停用/报废由调用方决定）
     * @param stations 调压站档案数据（是否含停用/报废由调用方决定）
     * @param out      输出流（写出后不关闭，由调用方关闭）
     */
    public static void write(List<TGasPipelineArch> pipes, List<TGasRegulatorStationArch> stations,
                             OutputStream out) {
        ExcelWriter writer = ExcelUtil.getWriter(true);
        try {
            writer.getWorkbook().setSheetName(0, "管道档案");
            writePipelines(writer, pipes);
            writer.setSheet("调压站档案");
            writeStations(writer, stations);
            writer.flush(out);
        } finally {
            writer.close();
        }
    }

    private static void writePipelines(ExcelWriter writer, List<TGasPipelineArch> list) {
        writer.writeHeadRow(Arrays.asList(
                "管道编号", "管道名称", "起点经度", "起点纬度", "终点经度", "终点纬度",
                "管径(mm)", "材质类型", "铺设年份", "所属区", "所属街道", "所属片区",
                "压力级制", "长度(m)", "当前状态", "备注", "建档时间", "最近变更时间"));
        if (list != null) {
            for (TGasPipelineArch p : list) {
                writer.writeRow(Arrays.asList(
                        p.getPipeNo(),
                        nz(p.getPipeName()),
                        num(p.getStartLng()),
                        num(p.getStartLat()),
                        num(p.getEndLng()),
                        num(p.getEndLat()),
                        num(p.getDiameter()),
                        nz(p.getMaterial()),
                        p.getLayYear() == null ? "" : String.valueOf(p.getLayYear()),
                        nz(p.getAreaName()),
                        nz(p.getStreetName()),
                        nz(p.getZoneName()),
                        nz(p.getPressureLevel()),
                        num(p.getLengthM()),
                        statusText(p.getStatus()),
                        nz(p.getRemark()),
                        time(p.getCreateTime()),
                        time(p.getUpdateTime())));
            }
        }
        int[] widths = {16, 28, 12, 12, 12, 12, 10, 12, 10, 12, 14, 16, 10, 10, 10, 24, 20, 20};
        for (int i = 0; i < widths.length; i++) {
            writer.setColumnWidth(i, widths[i] * 256);
        }
    }

    private static void writeStations(ExcelWriter writer, List<TGasRegulatorStationArch> list) {
        writer.writeHeadRow(Arrays.asList(
                "调压站编号", "调压站名称", "站址经度", "站址纬度", "管径描述", "主体材质",
                "建成年份", "所属区", "所属街道", "所属片区", "设备型号", "设计压力(MPa)",
                "出口压力(MPa)", "额定流量(m³/h)", "设备厂家", "当前状态", "备注", "建档时间", "最近变更时间"));
        if (list != null) {
            for (TGasRegulatorStationArch s : list) {
                writer.writeRow(Arrays.asList(
                        s.getStationNo(),
                        nz(s.getStationName()),
                        num(s.getLng()),
                        num(s.getLat()),
                        nz(s.getDiameter()),
                        nz(s.getMaterial()),
                        s.getLayYear() == null ? "" : String.valueOf(s.getLayYear()),
                        nz(s.getAreaName()),
                        nz(s.getStreetName()),
                        nz(s.getZoneName()),
                        nz(s.getDeviceModel()),
                        num(s.getDesignPressure()),
                        num(s.getOutletPressure()),
                        num(s.getRatedFlow()),
                        nz(s.getManufacturer()),
                        statusText(s.getStatus()),
                        nz(s.getRemark()),
                        time(s.getCreateTime()),
                        time(s.getUpdateTime())));
            }
        }
        int[] widths = {16, 24, 12, 12, 14, 12, 10, 12, 14, 16, 18, 12, 12, 14, 18, 10, 24, 20, 20};
        for (int i = 0; i < widths.length; i++) {
            writer.setColumnWidth(i, widths[i] * 256);
        }
    }

    private static String nz(String value) {
        return value == null ? "" : value;
    }
}
