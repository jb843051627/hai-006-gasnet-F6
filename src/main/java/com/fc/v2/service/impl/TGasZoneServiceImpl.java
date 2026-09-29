package com.fc.v2.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fc.v2.mapper.auto.TGasPipelineArchMapper;
import com.fc.v2.mapper.auto.TGasRegulatorStationArchMapper;
import com.fc.v2.mapper.auto.TGasZoneMapper;
import com.fc.v2.mapper.auto.TSysAreaMapper;
import com.fc.v2.mapper.auto.TSysCityMapper;
import com.fc.v2.mapper.auto.TSysProvinceMapper;
import com.fc.v2.mapper.auto.TSysStreetMapper;
import com.fc.v2.model.auto.TGasPipelineArch;
import com.fc.v2.model.auto.TGasRegulatorStationArch;
import com.fc.v2.model.auto.TGasZone;
import com.fc.v2.model.auto.TSysArea;
import com.fc.v2.model.auto.TSysCity;
import com.fc.v2.model.auto.TSysProvince;
import com.fc.v2.model.auto.TSysStreet;
import com.fc.v2.model.custom.GasRegionStatVo;
import com.fc.v2.service.ITGasZoneService;
import com.fc.v2.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 燃气档案行政区划片区 Service实现
 * 维护 省—市—区—街道—片区 五级树，并按区域层级汇总管道/调压站档案数量与状态分布。
 *
 * @author fuce
 * @date 2026-09-29
 */
@Service
public class TGasZoneServiceImpl extends ServiceImpl<TGasZoneMapper, TGasZone> implements ITGasZoneService {

    @Autowired
    private TSysProvinceMapper sysProvinceMapper;

    @Autowired
    private TSysCityMapper sysCityMapper;

    @Autowired
    private TSysAreaMapper sysAreaMapper;

    @Autowired
    private TSysStreetMapper sysStreetMapper;

    @Autowired
    private TGasPipelineArchMapper pipelineMapper;

    @Autowired
    private TGasRegulatorStationArchMapper stationMapper;

    @Override
    public List<TGasZone> selectZoneList(Wrapper<TGasZone> queryWrapper) {
        List<TGasZone> list = this.baseMapper.selectList(queryWrapper);
        fillRegionNames(list);
        return list;
    }

    @Override
    public TGasZone selectZoneById(Long id) {
        TGasZone zone = this.baseMapper.selectById(id);
        if (zone != null) {
            fillRegionNames(java.util.Collections.singletonList(zone));
        }
        return zone;
    }

    @Override
    public int insertZone(TGasZone record) {
        if (record == null || StringUtils.isEmpty(record.getZoneCode()) || StringUtils.isEmpty(record.getZoneName())) {
            throw new IllegalArgumentException("片区代号与名称均为必填项");
        }
        record.setZoneCode(record.getZoneCode().trim());
        Integer dup = this.baseMapper.selectCount(new QueryWrapper<TGasZone>()
                .eq("zone_code", record.getZoneCode()));
        if (dup != null && dup > 0) {
            throw new IllegalArgumentException("片区代号 " + record.getZoneCode() + " 已存在");
        }
        bindStreet(record);
        record.setStatus(record.getStatus() == null ? 0 : record.getStatus());
        if (record.getStatus() < 0 || record.getStatus() > 1) {
            throw new IllegalArgumentException("片区状态取值非法（0正常 1停用）");
        }
        record.setDelFlag(0);
        return this.baseMapper.insert(record);
    }

    @Override
    public int updateZone(TGasZone record) {
        if (record == null || record.getId() == null) {
            throw new IllegalArgumentException("缺少片区主键，无法编辑");
        }
        TGasZone db = this.baseMapper.selectById(record.getId());
        if (db == null || (db.getDelFlag() != null && db.getDelFlag() == 1)) {
            throw new IllegalArgumentException("片区不存在，无法编辑");
        }
        // 编号不可改
        record.setZoneCode(db.getZoneCode());
        if (StringUtils.isEmpty(record.getZoneName())) {
            throw new IllegalArgumentException("片区名称为必填项");
        }
        if (record.getStatus() != null && (record.getStatus() < 0 || record.getStatus() > 1)) {
            throw new IllegalArgumentException("片区状态取值非法（0正常 1停用）");
        }
        // 街道划转：重新回填区/市/省编码；不划转则保留原层级编码
        if (StringUtils.isNotEmpty(record.getStreetCode()) && !record.getStreetCode().equals(db.getStreetCode())) {
            bindStreet(record);
        } else {
            record.setStreetCode(db.getStreetCode());
            record.setAreaCode(db.getAreaCode());
            record.setCityCode(db.getCityCode());
            record.setProvinceCode(db.getProvinceCode());
        }
        if (record.getStatus() == null) {
            record.setStatus(db.getStatus());
        }
        record.setUpdateTime(new Date());
        return this.baseMapper.update(record, new QueryWrapper<TGasZone>()
                .eq("id", record.getId())
                .eq("del_flag", 0));
    }

    @Override
    public List<Map<String, Object>> regionTree() {
        List<Map<String, Object>> tree = new ArrayList<>();
        List<TSysProvince> provinces = sysProvinceMapper.selectList(new QueryWrapper<TSysProvince>()
                .eq("data_state", 0).orderByAsc("sort"));
        List<TSysCity> cities = sysCityMapper.selectList(new QueryWrapper<TSysCity>()
                .eq("data_state", 0));
        List<TSysArea> areas = sysAreaMapper.selectList(new QueryWrapper<TSysArea>()
                .eq("data_state", 0));
        List<TSysStreet> streets = sysStreetMapper.selectList(new QueryWrapper<TSysStreet>()
                .eq("data_state", 0).orderByAsc("sort"));
        List<TGasZone> zones = this.baseMapper.selectList(new QueryWrapper<TGasZone>()
                .eq("del_flag", 0).orderByAsc("sort"));

        for (TSysProvince p : provinces) {
            tree.add(node("P-" + p.getProvinceCode(), "0", p.getProvinceName(), 1,
                    p.getProvinceCode(), 0));
        }
        for (TSysCity c : cities) {
            tree.add(node("C-" + c.getCityCode(), "P-" + c.getProvinceCode(), c.getCityName(), 2,
                    c.getCityCode(), 0));
        }
        for (TSysArea a : areas) {
            tree.add(node("A-" + a.getAreaCode(), "C-" + a.getCityCode(), a.getAreaName(), 3,
                    a.getAreaCode(), 0));
        }
        for (TSysStreet s : streets) {
            tree.add(node("S-" + s.getStreetCode(), "A-" + s.getAreaCode(), s.getStreetName(), 4,
                    s.getStreetCode(), 0));
        }
        for (TGasZone z : zones) {
            tree.add(node("Z-" + z.getId(), "S-" + z.getStreetCode(), z.getZoneName(), 5,
                    z.getZoneCode(), z.getStatus() == null ? 0 : z.getStatus()));
        }
        return tree;
    }

    @Override
    public List<GasRegionStatVo> regionStats(String areaCode, String streetCode, String zoneName) {
        // 档案按冗余层级编码直接分组，五级中 片区→街道→区→市→省 自下而上累加
        QueryWrapper<TGasPipelineArch> pWrap = new QueryWrapper<TGasPipelineArch>().eq("del_flag", 0);
        QueryWrapper<TGasRegulatorStationArch> sWrap = new QueryWrapper<TGasRegulatorStationArch>().eq("del_flag", 0);
        if (StringUtils.isNotEmpty(areaCode)) {
            pWrap.eq("area_code", areaCode);
            sWrap.eq("area_code", areaCode);
        }
        if (StringUtils.isNotEmpty(streetCode)) {
            pWrap.eq("street_code", streetCode);
            sWrap.eq("street_code", streetCode);
        }
        List<TGasPipelineArch> pipes = pipelineMapper.selectList(pWrap);
        List<TGasRegulatorStationArch> stations = stationMapper.selectList(sWrap);

        // 过滤片区名（在内存里按 zone 名称筛）
        Set<Long> nameFilterZoneIds = null;
        if (StringUtils.isNotEmpty(zoneName)) {
            nameFilterZoneIds = new HashSet<>();
            for (TGasZone z : this.baseMapper.selectList(new QueryWrapper<TGasZone>()
                    .like("zone_name", zoneName).eq("del_flag", 0))) {
                nameFilterZoneIds.add(z.getId());
            }
        }
        // 行键: level|code → 统计行
        Map<String, GasRegionStatVo> rows = new LinkedHashMap<>();
        for (TGasPipelineArch p : pipes) {
            if (nameFilterZoneIds != null && (p.getZoneId() == null || !nameFilterZoneIds.contains(p.getZoneId()))) {
                continue;
            }
            accumulate(rows, p.getZoneId(), p.getStreetCode(), p.getAreaCode(), p.getStatus(), true);
        }
        for (TGasRegulatorStationArch s : stations) {
            if (nameFilterZoneIds != null && (s.getZoneId() == null || !nameFilterZoneIds.contains(s.getZoneId()))) {
                continue;
            }
            accumulate(rows, s.getZoneId(), s.getStreetCode(), s.getAreaCode(), s.getStatus(), false);
        }

        // 回填名称与父子关系
        fillStatMeta(rows);
        return new ArrayList<>(rows.values());
    }

    /** 把一条档案累加到 片区/街道/区 三层节点（市、省由区上卷，见 fillStatMeta） */
    private void accumulate(Map<String, GasRegionStatVo> rows, Long zoneId, String streetCode,
                            String areaCode, Integer status, boolean pipeline) {
        int st = status == null ? 0 : status;
        if (zoneId != null) {
            bump(rows, "5|" + zoneId, 5, String.valueOf(zoneId),
                    StringUtils.isNotEmpty(streetCode) ? "S-" + streetCode : null, st, pipeline);
        }
        if (StringUtils.isNotEmpty(streetCode)) {
            bump(rows, "4|" + streetCode, 4, streetCode,
                    StringUtils.isNotEmpty(areaCode) ? "A-" + areaCode : null, st, pipeline);
        }
        if (StringUtils.isNotEmpty(areaCode)) {
            // 父级市在 fillStatMeta 中按区表回填
            bump(rows, "3|" + areaCode, 3, areaCode, null, st, pipeline);
        }
    }

    private void bump(Map<String, GasRegionStatVo> rows, String key, int level, String code,
                      String parentCode, int status, boolean pipeline) {
        GasRegionStatVo vo = rows.get(key);
        if (vo == null) {
            vo = new GasRegionStatVo();
            vo.setLevel(level);
            vo.setRegionCode(code);
            vo.setParentCode(parentCode);
            rows.put(key, vo);
        }
        if (pipeline) {
            vo.setPipeCount(vo.getPipeCount() + 1);
        } else {
            vo.setStationCount(vo.getStationCount() + 1);
        }
        if (status == 1) {
            vo.setStopped(vo.getStopped() + 1);
        } else if (status == 2) {
            vo.setScrapped(vo.getScrapped() + 1);
        } else {
            vo.setInUse(vo.getInUse() + 1);
        }
    }

    /** 回填各统计节点名称，并由区上卷生成市、省两层汇总 */
    private void fillStatMeta(Map<String, GasRegionStatVo> rows) {
        Set<String> areaCodes = new HashSet<>();
        Set<String> streetCodes = new HashSet<>();
        Set<Long> zoneIds = new HashSet<>();
        for (GasRegionStatVo vo : rows.values()) {
            if (vo.getLevel() == 3) {
                areaCodes.add(vo.getRegionCode());
            } else if (vo.getLevel() == 4) {
                streetCodes.add(vo.getRegionCode());
                if (vo.getParentCode() != null && vo.getParentCode().startsWith("A-")) {
                    areaCodes.add(vo.getParentCode().substring(2));
                }
            } else if (vo.getLevel() == 5) {
                zoneIds.add(Long.valueOf(vo.getRegionCode()));
            }
        }
        Map<String, TSysArea> areaMap = new HashMap<>();
        if (!areaCodes.isEmpty()) {
            for (TSysArea a : sysAreaMapper.selectList(new QueryWrapper<TSysArea>().in("area_code", areaCodes))) {
                areaMap.put(a.getAreaCode(), a);
            }
        }
        Map<String, TSysStreet> streetMap = new HashMap<>();
        if (!streetCodes.isEmpty()) {
            for (TSysStreet s : sysStreetMapper.selectList(new QueryWrapper<TSysStreet>().in("street_code", streetCodes))) {
                streetMap.put(s.getStreetCode(), s);
            }
        }
        Map<Long, TGasZone> zoneMap = new HashMap<>();
        if (!zoneIds.isEmpty()) {
            for (TGasZone z : this.baseMapper.selectBatchIds(zoneIds)) {
                zoneMap.put(z.getId(), z);
            }
        }

        // 先定 区/街道/片区 名称与区→市父级
        for (GasRegionStatVo vo : rows.values()) {
            if (vo.getLevel() == 3) {
                TSysArea a = areaMap.get(vo.getRegionCode());
                if (a != null) {
                    vo.setRegionName(a.getAreaName());
                    vo.setParentCode("C-" + a.getCityCode());
                }
            } else if (vo.getLevel() == 4) {
                TSysStreet s = streetMap.get(vo.getRegionCode());
                if (s != null) {
                    vo.setRegionName(s.getStreetName());
                }
            } else if (vo.getLevel() == 5) {
                TGasZone z = zoneMap.get(Long.valueOf(vo.getRegionCode()));
                if (z != null) {
                    vo.setRegionName(z.getZoneName());
                }
            }
        }

        // 区 → 市 → 省 上卷
        Map<String, GasRegionStatVo> cityRows = new LinkedHashMap<>();
        Map<String, GasRegionStatVo> provinceRows = new LinkedHashMap<>();
        Set<String> cityCodes = new HashSet<>();
        for (TSysArea a : areaMap.values()) {
            cityCodes.add(a.getCityCode());
        }
        Map<String, TSysCity> cityMap = new HashMap<>();
        if (!cityCodes.isEmpty()) {
            for (TSysCity c : sysCityMapper.selectList(new QueryWrapper<TSysCity>().in("city_code", cityCodes))) {
                cityMap.put(c.getCityCode(), c);
            }
        }
        Set<String> provinceCodes = new HashSet<>();
        for (TSysCity c : cityMap.values()) {
            provinceCodes.add(c.getProvinceCode());
        }
        Map<String, TSysProvince> provinceMap = new HashMap<>();
        if (!provinceCodes.isEmpty()) {
            for (TSysProvince p : sysProvinceMapper.selectList(new QueryWrapper<TSysProvince>().in("province_code", provinceCodes))) {
                provinceMap.put(p.getProvinceCode(), p);
            }
        }

        for (Map.Entry<String, GasRegionStatVo> e : rows.entrySet()) {
            if (e.getValue().getLevel() != 3) {
                continue;
            }
            TSysArea a = areaMap.get(e.getValue().getRegionCode());
            if (a == null) {
                continue;
            }
            GasRegionStatVo cityVo = cityRows.computeIfAbsent("2|" + a.getCityCode(), k -> {
                GasRegionStatVo v = new GasRegionStatVo();
                v.setLevel(2);
                v.setRegionCode(a.getCityCode());
                TSysCity c = cityMap.get(a.getCityCode());
                v.setParentCode(c == null ? null : "P-" + c.getProvinceCode());
                if (c != null) {
                    v.setRegionName(c.getCityName());
                }
                return v;
            });
            merge(cityVo, e.getValue());

            TSysCity c = cityMap.get(a.getCityCode());
            if (c != null) {
                GasRegionStatVo provVo = provinceRows.computeIfAbsent("1|" + c.getProvinceCode(), k -> {
                    GasRegionStatVo v = new GasRegionStatVo();
                    v.setLevel(1);
                    v.setRegionCode(c.getProvinceCode());
                    v.setParentCode("0");
                    TSysProvince p = provinceMap.get(c.getProvinceCode());
                    if (p != null) {
                        v.setRegionName(p.getProvinceName());
                    }
                    return v;
                });
                merge(provVo, e.getValue());
            }
        }
        rows.putAll(cityRows);
        rows.putAll(provinceRows);
    }

    private void merge(GasRegionStatVo target, GasRegionStatVo source) {
        target.setInUse(target.getInUse() + source.getInUse());
        target.setStopped(target.getStopped() + source.getStopped());
        target.setScrapped(target.getScrapped() + source.getScrapped());
        target.setPipeCount(target.getPipeCount() + source.getPipeCount());
        target.setStationCount(target.getStationCount() + source.getStationCount());
    }

    /** 按街道回填 区/市/省 编码 */
    private void bindStreet(TGasZone record) {
        if (StringUtils.isEmpty(record.getStreetCode())) {
            throw new IllegalArgumentException("所属街道为必填项");
        }
        TSysStreet street = sysStreetMapper.selectOne(new QueryWrapper<TSysStreet>()
                .eq("street_code", record.getStreetCode()));
        if (street == null || (street.getDataState() != null && street.getDataState() != 0)) {
            throw new IllegalArgumentException("所属街道不存在或已停用");
        }
        TSysArea area = sysAreaMapper.selectOne(new QueryWrapper<TSysArea>()
                .eq("area_code", street.getAreaCode()));
        if (area == null) {
            throw new IllegalArgumentException("街道所属区档案缺失，无法挂接片区");
        }
        TSysCity city = sysCityMapper.selectOne(new QueryWrapper<TSysCity>()
                .eq("city_code", area.getCityCode()));
        if (city == null) {
            throw new IllegalArgumentException("区所属市档案缺失，无法挂接片区");
        }
        record.setStreetCode(street.getStreetCode());
        record.setAreaCode(area.getAreaCode());
        record.setCityCode(city.getCityCode());
        record.setProvinceCode(city.getProvinceCode());
    }

    private void fillRegionNames(List<TGasZone> list) {
        if (list == null || list.isEmpty()) {
            return;
        }
        Set<String> streetCodes = new HashSet<>();
        Set<String> areaCodes = new HashSet<>();
        for (TGasZone z : list) {
            if (z.getStreetCode() != null) {
                streetCodes.add(z.getStreetCode());
            }
            if (z.getAreaCode() != null) {
                areaCodes.add(z.getAreaCode());
            }
        }
        Map<String, String> streetNameMap = new HashMap<>();
        if (!streetCodes.isEmpty()) {
            for (TSysStreet s : sysStreetMapper.selectList(new QueryWrapper<TSysStreet>()
                    .in("street_code", streetCodes))) {
                streetNameMap.put(s.getStreetCode(), s.getStreetName());
            }
        }
        Map<String, String> areaNameMap = new HashMap<>();
        if (!areaCodes.isEmpty()) {
            for (TSysArea a : sysAreaMapper.selectList(new QueryWrapper<TSysArea>()
                    .in("area_code", areaCodes))) {
                areaNameMap.put(a.getAreaCode(), a.getAreaName());
            }
        }
        for (TGasZone z : list) {
            z.setStreetName(streetNameMap.get(z.getStreetCode()));
            z.setAreaName(areaNameMap.get(z.getAreaCode()));
        }
    }

    private Map<String, Object> node(String id, String pId, String name, int level, String code, int status) {
        Map<String, Object> n = new HashMap<>();
        n.put("id", id);
        n.put("pId", pId);
        n.put("name", name);
        n.put("level", level);
        n.put("code", code);
        n.put("status", status);
        return n;
    }
}
