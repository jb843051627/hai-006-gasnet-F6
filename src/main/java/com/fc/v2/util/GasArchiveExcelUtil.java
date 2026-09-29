package com.fc.v2.util;

import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import com.fc.v2.model.auto.TGasPipeArchive;

import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * 燃气管道/调压站档案台账 Excel 导出。
 *
 * <p>两个 sheet：管道档案、调压站档案；是否包含停用/报废历史档案由调用方按状态过滤后决定，
 * 本工具只负责如实写出。档案台账不做删除行处理。
 *
 * @author fuce
 * @date 2026-09-29
 */
public class GasArchiveExcelUtil {

    private static final ThreadLocal<SimpleDateFormat> TS =
            ThreadLocal.withInitial(() -> new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));

    private GasArchiveExcelUtil() {
    }

    /**
     * 写出档案台账（管道与调压站列口径一致：编号/名称/状态/坐标/规格材质/年份/区划/时间）。
     * 传入 null 的类别不生成对应 sheet；台账过滤口径（是否含停用/报废历史）由调用方决定。
     *
     * @param pipes    管道档案（null 表示不导出该 sheet）
     * @param stations 调压站档案（null 表示不导出该 sheet）
     * @param out      输出流（调用方负责关闭）
     */
    public static void writeLedger(List<TGasPipeArchive> pipes, List<TGasPipeArchive> stations, OutputStream out) {
        ExcelWriter writer = ExcelUtil.getWriter();
        try {
            List<String> head = Arrays.asList(
                    "档案编号", "档案名称", "当前状态", "起点经度", "起点纬度",
                    "终点经度", "终点纬度", "管径尺寸", "材质类型", "铺设年份",
                    "所属行政区(省—市—区—街道)", "所属片区", "状态变更时间", "建档时间", "备注");
            boolean first = true;
            if (pipes != null) {
                writer.renameSheet("管道档案(" + pipes.size() + ")");
                writeSheet(writer, head, pipes);
                first = false;
            }
            if (stations != null) {
                if (!first) {
                    writer.setSheet("调压站档案(" + stations.size() + ")");
                } else {
                    writer.renameSheet("调压站档案(" + stations.size() + ")");
                }
                writeSheet(writer, head, stations);
            }
            writer.flush(out);
        } finally {
            writer.close();
        }
    }

    private static void writeSheet(ExcelWriter writer, List<String> head, List<TGasPipeArchive> rows) {
        writer.writeHeadRow(head);
        for (TGasPipeArchive r : rows) {
            writer.writeRow(toRow(r));
        }
        autoSize(writer, head.size());
    }

    private static void autoSize(ExcelWriter writer, int columns) {
        for (int i = 0; i < columns; i++) {
            writer.autoSizeColumn(i);
        }
    }

    private static List<Object> toRow(TGasPipeArchive r) {
        return Arrays.asList(
                nullToEmpty(r.getArchiveNo()),
                nullToEmpty(r.getArchiveName()),
                statusText(r.getStatus()),
                r.getStartLng(),
                r.getStartLat(),
                r.getEndLng(),
                r.getEndLat(),
                nullToEmpty(r.getPipeSpec()),
                nullToEmpty(r.getMaterialType()),
                r.getLayYear() == null ? "" : r.getLayYear(),
                nullToEmpty(r.getRegionName()),
                nullToEmpty(r.getSectorName()),
                formatTime(r.getStatusTime()),
                formatTime(r.getCreateTime()),
                nullToEmpty(r.getRemark()));
    }

    private static String statusText(Integer status) {
        if (status == null) {
            return "在用";
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

    private static String formatTime(Date d) {
        return d == null ? "" : TS.get().format(d);
    }

    private static String nullToEmpty(String s) {
        return s == null ? "" : s;
    }
}
