package com.fc.v2.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fc.v2.mapper.auto.TGasPipeArchiveMapper;
import com.fc.v2.model.auto.TGasPipeArchive;
import com.fc.v2.model.auto.TSysArea;
import com.fc.v2.model.auto.TSysCity;
import com.fc.v2.model.auto.TSysProvince;
import com.fc.v2.model.auto.TSysStreet;
import com.fc.v2.model.custom.RegionStatNode;
import com.fc.v2.service.ITGasPipeArchiveService;
import com.fc.v2.service.ITSysAreaService;
import com.fc.v2.service.ITSysCityService;
import com.fc.v2.service.ITSysProvinceService;
import com.fc.v2.service.ITSysStreetService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 燃气管道/调压站基础档案 Service 实现。
 *
 * <p>建档三校验：编号全局唯一、空间坐标有效、行政区划编码存在且层级一致；
 * 编辑只改属性（编号/类别不可变），状态独立走流转；全流程无删除。
 *
 * @author fuce
 * @date 2026-09-29
 */
@Service
public class TGasPipeArchiveServiceImpl implements ITGasPipeArchiveService {

    private static final int DEL_NORMAL = 0;

    private static final BigDecimal LNG_MIN = new BigDecimal("-180");
    private static final BigDecimal LNG_MAX = new BigDecimal("180");
    private static final BigDecimal LAT_MIN = new BigDecimal("-90");
    private static final BigDecimal LAT_MAX = new BigDecimal("90");

    @Resource
    private TGasPipeArchiveMapper archiveMapper;

    @Resource
    private ITSysProvinceService provinceService;
    @Resource
    private ITSysCityService cityService;
    @Resource
    private ITSysAreaService areaService;
    @Resource
    private ITSysStreetService streetService;

    @Override
    public TGasPipeArchive selectById(Long id) {
        return archiveMapper.selectOne(new QueryWrapper<TGasPipeArchive>()
                .eq("id", id).eq("del_flag", DEL_NORMAL));
    }

    @Override
    public List<TGasPipeArchive> selectList(Wrapper<TGasPipeArchive> queryWrapper) {
        return archiveMapper.selectList(queryWrapper);
    }

    @Override
    public int createArchive(TGasPipeArchive record) {
        if (record == null) {
            throw new IllegalArgumentException("档案内容不能为空");
        }
        String archiveNo = trimRequired(record.getArchiveNo(), "档案编号不能为空");
        Integer archiveType = record.getArchiveType();
        if (archiveType == null
                || (archiveType != TYPE_PIPE && archiveType != TYPE_STATION)) {
            throw new IllegalArgumentException("档案类别必须为 管道 或 调压站");
        }
        if (isBlank(record.getArchiveName())) {
            throw new IllegalArgumentException("档案名称不能为空");
        }
        Integer dup = archiveMapper.selectCount(new QueryWrapper<TGasPipeArchive>()
                .eq("archive_no", archiveNo).eq("del_flag", DEL_NORMAL));
        if (dup != null && dup > 0) {
            throw new IllegalArgumentException("档案编号已存在：" + archiveNo + "，编号全局唯一不可重复建档");
        }

        validateCoordinates(archiveType, record);
        validateLayYear(record.getLayYear());
        RegionPath region = resolveRegion(record);

        record.setArchiveNo(archiveNo);
        record.setProvinceCode(region.provinceCode);
        record.setCityCode(region.cityCode);
        record.setAreaCode(region.areaCode);
        record.setStreetCode(region.streetCode);
        record.setRegionName(region.fullName);
        // 新建统一为在用；停用/报废建档后走状态流转
        record.setStatus(STATUS_IN_USE);
        record.setStatusTime(new java.util.Date());
        record.setDelFlag(DEL_NORMAL);
        return archiveMapper.insert(record);
    }

    @Override
    public int updateArchive(TGasPipeArchive record) {
        if (record == null || record.getId() == null) {
            throw new IllegalArgumentException("缺少档案主键");
        }
        TGasPipeArchive db = selectById(record.getId());
        if (db == null) {
            throw new IllegalArgumentException("档案不存在，无法编辑");
        }
        // 报废为终态：历史档案冻结，任何属性（含片区划转）均不再允许变更
        if (db.getStatus() != null && db.getStatus() == STATUS_SCRAPPED) {
            throw new IllegalArgumentException("档案已报废（终态），属性已冻结不可编辑；如需恢复管线请另行建档");
        }
        // 编号、类别为档案身份，建档后冻结；前端若传了不一致的值直接拒绝，防止误改
        if (!isBlank(record.getArchiveNo()) && !db.getArchiveNo().equals(record.getArchiveNo().trim())) {
            throw new IllegalArgumentException("档案编号不可变更");
        }
        if (record.getArchiveType() != null && !record.getArchiveType().equals(db.getArchiveType())) {
            throw new IllegalArgumentException("档案类别不可变更");
        }
        if (!isBlank(record.getArchiveName())) {
            db.setArchiveName(record.getArchiveName().trim());
        }
        validateCoordinates(db.getArchiveType(), record);
        db.setStartLng(record.getStartLng());
        db.setStartLat(record.getStartLat());
        db.setEndLng(record.getEndLng());
        db.setEndLat(record.getEndLat());
        db.setPipeSpec(blankToNull(record.getPipeSpec()));
        db.setMaterialType(blankToNull(record.getMaterialType()));
        validateLayYear(record.getLayYear());
        db.setLayYear(record.getLayYear());

        // 片区划转：编码有传即重新校验层级并刷新名称快照
        if (!isBlank(record.getAreaCode())) {
            RegionPath region = resolveRegion(record);
            db.setProvinceCode(region.provinceCode);
            db.setCityCode(region.cityCode);
            db.setAreaCode(region.areaCode);
            db.setStreetCode(region.streetCode);
            db.setRegionName(region.fullName);
        }
        db.setSectorName(blankToNull(record.getSectorName()));
        db.setRemark(record.getRemark());
        // 状态不允许借编辑接口改动（del_flag 更不允许），保证“状态调整/片区划转”各自留痕。
        // 用显式 set 的 UpdateWrapper：MP 默认 updateById 忽略 null 字段，显式置空（如清空管径/片区）才会落库；
        // 代价是 entity 为 null 时自动填充不触发，update_by/update_time 在此手动写入。
        java.util.Date now = new java.util.Date();
        db.setUpdateTime(now);
        db.setUpdateBy(com.fc.v2.shiro.util.ShiroUtils.getLoginName());
        com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper<TGasPipeArchive> uw =
                new com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper<TGasPipeArchive>()
                        .eq("id", db.getId()).eq("del_flag", DEL_NORMAL)
                        .set("archive_name", db.getArchiveName())
                        .set("start_lng", db.getStartLng())
                        .set("start_lat", db.getStartLat())
                        .set("end_lng", db.getEndLng())
                        .set("end_lat", db.getEndLat())
                        .set("pipe_spec", db.getPipeSpec())
                        .set("material_type", db.getMaterialType())
                        .set("lay_year", db.getLayYear())
                        .set("province_code", db.getProvinceCode())
                        .set("city_code", db.getCityCode())
                        .set("area_code", db.getAreaCode())
                        .set("street_code", db.getStreetCode())
                        .set("region_name", db.getRegionName())
                        .set("sector_name", db.getSectorName())
                        .set("remark", db.getRemark())
                        .set("update_by", db.getUpdateBy())
                        .set("update_time", db.getUpdateTime());
        return archiveMapper.update(null, uw);
    }

    @Override
    public int changeStatus(Long id, Integer targetStatus, String reason) {
        TGasPipeArchive db = selectById(id);
        if (db == null) {
            throw new IllegalArgumentException("档案不存在，无法调整状态");
        }
        if (targetStatus == null
                || (targetStatus != STATUS_IN_USE
                && targetStatus != STATUS_STOPPED
                && targetStatus != STATUS_SCRAPPED)) {
            throw new IllegalArgumentException("目标状态非法（0在用 1停用 2报废）");
        }
        int current = db.getStatus() == null ? STATUS_IN_USE : db.getStatus();
        if (targetStatus.equals(current)) {
            throw new IllegalArgumentException("档案当前已是该状态，无需调整");
        }
        if (current == STATUS_SCRAPPED) {
            throw new IllegalArgumentException("档案已报废，报废为终态不可恢复");
        }
        if (targetStatus == STATUS_SCRAPPED && current != STATUS_STOPPED) {
            throw new IllegalArgumentException("须先停用档案，确认无在役供气后才可报废");
        }
        db.setStatus(targetStatus);
        db.setStatusTime(new java.util.Date());
        // db 是带旧值取出的对象，MetaObjectHandler 的 fillStrategy 仅在字段为 null 时填充，
        // 故更新人/时间在此显式刷新
        db.setUpdateBy(com.fc.v2.shiro.util.ShiroUtils.getLoginName());
        db.setUpdateTime(db.getStatusTime());
        String note = statusName(targetStatus);
        if (!isBlank(reason)) {
            note += "：" + reason.trim();
        }
        String prev = isBlank(db.getRemark()) ? "" : db.getRemark() + "；";
        // 报废原因并入备注留痕；不新增删除标记。超长截断保护 remark varchar(500)
        String merged = prev + "[" + new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm").format(db.getStatusTime())
                + "] 状态调整为「" + note + "」(操作人见更新记录)";
        db.setRemark(merged.length() > 500 ? merged.substring(0, 500) : merged);
        return archiveMapper.updateById(db);
    }

    @Override
    public List<RegionStatNode> regionTree(Integer archiveType) {
        QueryWrapper<TGasPipeArchive> qw = new QueryWrapper<TGasPipeArchive>()
                .eq("del_flag", DEL_NORMAL)
                .isNotNull("area_code");
        if (archiveType != null) {
            qw.eq("archive_type", archiveType);
        }
        List<TGasPipeArchive> rows = archiveMapper.selectList(qw);

        Map<String, RegionStatNode> nodes = new LinkedHashMap<String, RegionStatNode>();
        for (TGasPipeArchive r : rows) {
            String pc = r.getProvinceCode();
            String cc = r.getCityCode();
            String ac = r.getAreaCode();
            String sc = r.getStreetCode();

            // 解析不到名称的编码也建节点（用编码占位），保证脏数据下统计不丢
            TSysProvince province = lookupProvince(pc);
            TSysCity city = lookupCity(cc);
            TSysArea area = lookupArea(ac);
            TSysStreet street = isBlank(sc) ? null : lookupStreet(sc);
            final String fpc = province == null ? pc : province.getProvinceCode();
            final String fcc = city == null ? cc : city.getCityCode();
            final String fac = area == null ? ac : area.getAreaCode();
            final String fsc = street == null ? null : street.getStreetCode();
            final String provinceName = province == null ? fpc : province.getProvinceName();
            final String cityName = city == null ? fcc : city.getCityName();
            final String areaName = area == null ? fac : area.getAreaName();
            final String streetName = street == null ? null : street.getStreetName();

            RegionStatNode provinceNode = nodes.computeIfAbsent("P-" + fpc,
                    k -> new RegionStatNode("P-" + fpc, "0", 0, fpc, "provinceCode", provinceName));
            RegionStatNode cityNode = nodes.computeIfAbsent("C-" + fcc,
                    k -> new RegionStatNode("C-" + fcc, "P-" + fpc, 1, fcc, "cityCode", cityName));
            RegionStatNode areaNode = nodes.computeIfAbsent("A-" + fac,
                    k -> new RegionStatNode("A-" + fac, "C-" + fcc, 2, fac, "areaCode", areaName));
            RegionStatNode leaf;
            if (fsc != null) {
                leaf = nodes.computeIfAbsent("S-" + fsc,
                        k -> new RegionStatNode("S-" + fsc, "A-" + fac, 3, fsc, "streetCode", streetName));
            } else {
                // 未细化到街道的片区档案直接挂在区下（区节点自身计数）
                leaf = areaNode;
            }
            accumulate(leaf, r.getStatus());
            // 向上汇总到区/市/省（leaf 即区节点时跳过自身重复计数）
            if (leaf != areaNode) {
                accumulate(areaNode, r.getStatus());
            }
            accumulate(cityNode, r.getStatus());
            accumulate(provinceNode, r.getStatus());
        }
        for (RegionStatNode n : nodes.values()) {
            n.setTitle(n.getName() + "（共" + n.getTotal() + "，在用" + n.getInUse()
                    + "，停用" + n.getStopped() + "，报废" + n.getScrapped() + "）");
        }
        return new ArrayList<RegionStatNode>(nodes.values());
    }

    private static void accumulate(RegionStatNode node, Integer status) {
        node.setTotal(node.getTotal() + 1);
        int st = status == null ? STATUS_IN_USE : status;
        if (st == STATUS_STOPPED) {
            node.setStopped(node.getStopped() + 1);
        } else if (st == STATUS_SCRAPPED) {
            node.setScrapped(node.getScrapped() + 1);
        } else {
            node.setInUse(node.getInUse() + 1);
        }
    }

    // ---------------------------------------------------------------- 校验

    /**
     * 坐标有效性：
     * 管道必须给出起点与终点，经纬度各自落在 [-180,180]/[-90,90]，且起终点不得完全重合；
     * 调压站只需站点坐标（起经/起纬），终点坐标一律忽略。
     */
    private void validateCoordinates(int archiveType, TGasPipeArchive r) {
        if (r.getStartLng() == null || r.getStartLat() == null) {
            throw new IllegalArgumentException(archiveType == TYPE_STATION
                    ? "调压站站点经纬度不能为空" : "管道起点经纬度不能为空");
        }
        checkRange(r.getStartLng(), LNG_MIN, LNG_MAX, "起点经度");
        checkRange(r.getStartLat(), LAT_MIN, LAT_MAX, "起点纬度");
        if (archiveType == TYPE_PIPE) {
            if (r.getEndLng() == null || r.getEndLat() == null) {
                throw new IllegalArgumentException("管道终点经纬度不能为空");
            }
            checkRange(r.getEndLng(), LNG_MIN, LNG_MAX, "终点经度");
            checkRange(r.getEndLat(), LAT_MIN, LAT_MAX, "终点纬度");
            if (r.getStartLng().compareTo(r.getEndLng()) == 0
                    && r.getStartLat().compareTo(r.getEndLat()) == 0) {
                throw new IllegalArgumentException("管道起点坐标与终点坐标不得完全重合");
            }
        } else {
            r.setEndLng(null);
            r.setEndLat(null);
        }
    }

    private static void checkRange(BigDecimal v, BigDecimal min, BigDecimal max, String label) {
        if (v.compareTo(min) < 0 || v.compareTo(max) > 0) {
            throw new IllegalArgumentException(label + "超出有效范围（经度-180~180，纬度-90~90）：" + v);
        }
    }

    private void validateLayYear(Integer layYear) {
        if (layYear == null) {
            return;
        }
        int year = Calendar.getInstance().get(Calendar.YEAR);
        if (layYear < 1800 || layYear > year) {
            throw new IllegalArgumentException("铺设年份须在 1800~" + year + " 之间");
        }
    }

    /**
     * 行政区划编码校验：区必选；省/市/街道随层级联动解析，任一编码不存在或父子关系不一致即拒绝。
     */
    private RegionPath resolveRegion(TGasPipeArchive r) {
        RegionPath path = new RegionPath();
        if (isBlank(r.getAreaCode())) {
            throw new IllegalArgumentException("所属行政区至少要选择到区/片区");
        }
        TSysArea area = lookupArea(r.getAreaCode().trim());
        if (area == null) {
            throw new IllegalArgumentException("所属区编码不存在：" + r.getAreaCode());
        }
        path.areaCode = area.getAreaCode();
        TSysCity city = lookupCity(area.getCityCode());
        if (city == null) {
            throw new IllegalArgumentException("区所属市编码缺失：" + area.getCityCode());
        }
        path.cityCode = city.getCityCode();
        TSysProvince province = lookupProvince(city.getProvinceCode());
        if (province == null) {
            throw new IllegalArgumentException("市所属省编码缺失：" + city.getProvinceCode());
        }
        path.provinceCode = province.getProvinceCode();

        if (!isBlank(r.getProvinceCode()) && !r.getProvinceCode().trim().equals(path.provinceCode)) {
            throw new IllegalArgumentException("所选省与区/市层级不一致");
        }
        if (!isBlank(r.getCityCode()) && !r.getCityCode().trim().equals(path.cityCode)) {
            throw new IllegalArgumentException("所选市与区/省层级不一致");
        }

        path.streetCode = null;
        String streetName = "";
        if (!isBlank(r.getStreetCode())) {
            TSysStreet street = lookupStreet(r.getStreetCode().trim());
            if (street == null) {
                throw new IllegalArgumentException("所属街道编码不存在：" + r.getStreetCode());
            }
            if (!area.getAreaCode().equals(street.getAreaCode())) {
                throw new IllegalArgumentException("街道不属于所选区，请重新选择");
            }
            path.streetCode = street.getStreetCode();
            streetName = street.getStreetName();
        }
        StringBuilder full = new StringBuilder(province.getProvinceName())
                .append("—").append(city.getCityName())
                .append("—").append(area.getAreaName());
        if (!streetName.isEmpty()) {
            full.append("—").append(streetName);
        }
        if (!isBlank(r.getSectorName())) {
            full.append("—").append(r.getSectorName().trim());
        }
        path.fullName = full.toString();
        return path;
    }

    private TSysProvince lookupProvince(String code) {
        if (isBlank(code)) {
            return null;
        }
        List<TSysProvince> list = provinceService.selectTSysProvinceList(
                new QueryWrapper<TSysProvince>().eq("province_code", code.trim()));
        return list.isEmpty() ? null : list.get(0);
    }

    private TSysCity lookupCity(String code) {
        if (isBlank(code)) {
            return null;
        }
        List<TSysCity> list = cityService.selectTSysCityList(
                new QueryWrapper<TSysCity>().eq("city_code", code.trim()));
        return list.isEmpty() ? null : list.get(0);
    }

    private TSysArea lookupArea(String code) {
        if (isBlank(code)) {
            return null;
        }
        List<TSysArea> list = areaService.selectTSysAreaList(
                new QueryWrapper<TSysArea>().eq("area_code", code.trim()));
        return list.isEmpty() ? null : list.get(0);
    }

    private TSysStreet lookupStreet(String code) {
        if (isBlank(code)) {
            return null;
        }
        List<TSysStreet> list = streetService.selectTSysStreetList(
                new QueryWrapper<TSysStreet>().eq("street_code", code.trim()));
        return list.isEmpty() ? null : list.get(0);
    }

    private static String trimRequired(String v, String msg) {
        if (isBlank(v)) {
            throw new IllegalArgumentException(msg);
        }
        return v.trim();
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private static String blankToNull(String s) {
        return isBlank(s) ? null : s.trim();
    }

    private static String statusName(int status) {
        if (status == STATUS_IN_USE) {
            return "在用";
        }
        if (status == STATUS_STOPPED) {
            return "停用";
        }
        return "报废";
    }

    /** 行政区划解析结果（四级编码 + 全名快照） */
    private static class RegionPath {
        private String provinceCode;
        private String cityCode;
        private String areaCode;
        private String streetCode;
        private String fullName;
    }
}
