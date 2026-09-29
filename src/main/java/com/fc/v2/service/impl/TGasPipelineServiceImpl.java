package com.fc.v2.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fc.v2.mapper.auto.TGasPipelineArchMapper;
import com.fc.v2.mapper.auto.TGasZoneMapper;
import com.fc.v2.mapper.auto.TSysAreaMapper;
import com.fc.v2.mapper.auto.TSysStreetMapper;
import com.fc.v2.model.auto.TGasPipelineArch;
import com.fc.v2.model.auto.TGasZone;
import com.fc.v2.model.auto.TSysArea;
import com.fc.v2.model.auto.TSysCity;
import com.fc.v2.model.auto.TSysStreet;
import com.fc.v2.service.ITGasPipelineService;
import com.fc.v2.util.GasArchiveValidator;
import com.fc.v2.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 燃气管道基础档案 Service实现
 * 档案铁律：编号唯一不可改、坐标强校验、只新增/编辑（无删除入口）、状态位停用报废不丢历史。
 *
 * @author fuce
 * @date 2026-09-29
 */
@Service
public class TGasPipelineServiceImpl extends ServiceImpl<TGasPipelineArchMapper, TGasPipelineArch>
        implements ITGasPipelineService {

    @Autowired
    private TGasZoneMapper gasZoneMapper;

    @Autowired
    private TSysStreetMapper sysStreetMapper;

    @Autowired
    private TSysAreaMapper sysAreaMapper;

    @Autowired
    private com.fc.v2.mapper.auto.TSysCityMapper sysCityMapper;

    @Override
    public TGasPipelineArch selectById(Long id) {
        TGasPipelineArch record = this.baseMapper.selectById(id);
        if (record != null) {
            fillRegionNames(java.util.Collections.singletonList(record));
        }
        return record;
    }

    @Override
    public List<TGasPipelineArch> selectList(Wrapper<TGasPipelineArch> queryWrapper) {
        List<TGasPipelineArch> list = this.baseMapper.selectList(queryWrapper);
        fillRegionNames(list);
        return list;
    }

    @Override
    public int insert(TGasPipelineArch record) {
        validateBase(record);
        if (StringUtils.isEmpty(record.getPipeNo())) {
            throw new IllegalArgumentException("管道编号为必填项");
        }
        Integer dup = this.baseMapper.selectCount(new QueryWrapper<TGasPipelineArch>()
                .eq("pipe_no", record.getPipeNo().trim()));
        if (dup != null && dup > 0) {
            throw new IllegalArgumentException("管道编号 " + record.getPipeNo() + " 已存在，编号全库唯一");
        }
        record.setPipeNo(record.getPipeNo().trim());
        bindZone(record, record.getZoneId());
        record.setStatus(record.getStatus() == null ? 0 : record.getStatus());
        checkStatus(record.getStatus());
        record.setDelFlag(0);
        return this.baseMapper.insert(record);
    }

    @Override
    public int update(TGasPipelineArch record) {
        if (record == null || record.getId() == null) {
            throw new IllegalArgumentException("缺少档案主键，无法编辑");
        }
        TGasPipelineArch db = this.baseMapper.selectById(record.getId());
        if (db == null || (db.getDelFlag() != null && db.getDelFlag() == 1)) {
            throw new IllegalArgumentException("档案不存在，无法编辑");
        }
        validateBase(record);
        // 编号建档后不可改：表单若带编号必须与库内一致；不带则保持原编号
        if (StringUtils.isNotEmpty(record.getPipeNo()) && !db.getPipeNo().equals(record.getPipeNo().trim())) {
            throw new IllegalArgumentException("管道编号建档后不允许修改");
        }
        record.setPipeNo(db.getPipeNo());
        if (record.getStatus() == null) {
            record.setStatus(db.getStatus());
        }
        checkStatus(record.getStatus());
        // 片区划转：换片区时同步街道/区冗余编码
        Long targetZoneId = record.getZoneId() == null ? db.getZoneId() : record.getZoneId();
        if (record.getZoneId() != null && !record.getZoneId().equals(db.getZoneId())) {
            bindZone(record, targetZoneId);
        } else {
            record.setStreetCode(db.getStreetCode());
            record.setAreaCode(db.getAreaCode());
        }
        record.setUpdateTime(new Date());
        return this.baseMapper.update(record, new QueryWrapper<TGasPipelineArch>()
                .eq("id", record.getId())
                .eq("del_flag", 0));
    }

    @Override
    public List<TGasPipelineArch> selectForExport(Wrapper<TGasPipelineArch> queryWrapper, boolean includeHistory) {
        QueryWrapper<TGasPipelineArch> wrapper;
        if (queryWrapper instanceof QueryWrapper) {
            wrapper = (QueryWrapper<TGasPipelineArch>) queryWrapper;
        } else {
            wrapper = new QueryWrapper<>();
        }
        wrapper.eq("del_flag", 0);
        if (!includeHistory) {
            wrapper.eq("status", 0);
        }
        wrapper.orderByAsc("area_code").orderByAsc("street_code").orderByAsc("pipe_no");
        List<TGasPipelineArch> list = this.baseMapper.selectList(wrapper);
        fillRegionNames(list);
        return list;
    }

    /** 公共字段校验：坐标、年份、尺寸、状态取值 */
    private void validateBase(TGasPipelineArch record) {
        if (record == null) {
            throw new IllegalArgumentException("档案内容为空");
        }
        GasArchiveValidator.checkCoord(record.getStartLng(), record.getStartLat(), "起点");
        GasArchiveValidator.checkCoord(record.getEndLng(), record.getEndLat(), "终点");
        GasArchiveValidator.checkEndpointsDistinct(record.getStartLng(), record.getStartLat(),
                record.getEndLng(), record.getEndLat());
        GasArchiveValidator.checkLayYear(record.getLayYear());
        GasArchiveValidator.checkPositive(record.getDiameter(), "管径尺寸");
        GasArchiveValidator.checkPositive(record.getLengthM(), "管线长度");
    }

    private void checkStatus(Integer status) {
        if (status == null || status < 0 || status > 2) {
            throw new IllegalArgumentException("状态取值非法（0在用 1停用 2报废）");
        }
    }

    /** 挂接片区：片区须存在且未删除/未停用，并回填街道、区编码 */
    private void bindZone(TGasPipelineArch record, Long zoneId) {
        if (zoneId == null) {
            throw new IllegalArgumentException("所属片区为必填项");
        }
        TGasZone zone = gasZoneMapper.selectById(zoneId);
        if (zone == null || (zone.getDelFlag() != null && zone.getDelFlag() == 1)) {
            throw new IllegalArgumentException("所属片区不存在，请先在行政区划树中建档");
        }
        if (zone.getStatus() != null && zone.getStatus() == 1) {
            throw new IllegalArgumentException("片区【" + zone.getZoneName() + "】已停用，不能新建或划转档案");
        }
        record.setZoneId(zone.getId());
        record.setStreetCode(zone.getStreetCode());
        record.setAreaCode(zone.getAreaCode());
    }

    /** 批量回填 片区/街道/区 名称，避免列表 N+1 查询 */
    private void fillRegionNames(List<TGasPipelineArch> list) {
        if (list == null || list.isEmpty()) {
            return;
        }
        Map<Long, TGasZone> zoneMap = new HashMap<>();
        Set<String> streetCodes = new HashSet<>();
        Set<String> areaCodes = new HashSet<>();
        for (TGasPipelineArch p : list) {
            if (p.getStreetCode() != null) {
                streetCodes.add(p.getStreetCode());
            }
            if (p.getAreaCode() != null) {
                areaCodes.add(p.getAreaCode());
            }
        }
        List<Long> zoneIds = new ArrayList<>();
        Set<Long> zoneIdSet = new HashSet<>();
        for (TGasPipelineArch p : list) {
            if (p.getZoneId() != null && zoneIdSet.add(p.getZoneId())) {
                zoneIds.add(p.getZoneId());
            }
        }
        if (!zoneIds.isEmpty()) {
            for (TGasZone z : gasZoneMapper.selectBatchIds(zoneIds)) {
                zoneMap.put(z.getId(), z);
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
        Map<String, TSysArea> areaMap = new HashMap<>();
        if (!areaCodes.isEmpty()) {
            for (TSysArea a : sysAreaMapper.selectList(new QueryWrapper<TSysArea>()
                    .in("area_code", areaCodes))) {
                areaNameMap.put(a.getAreaCode(), a.getAreaName());
                areaMap.put(a.getAreaCode(), a);
            }
        }
        // 按区推导市/省编码（编辑回显五级联动用）
        Set<String> cityCodes = new HashSet<>();
        for (TSysArea a : areaMap.values()) {
            cityCodes.add(a.getCityCode());
        }
        Map<String, String> cityProvMap = new HashMap<>();
        if (!cityCodes.isEmpty()) {
            for (com.fc.v2.model.auto.TSysCity c : sysCityMapper.selectList(new QueryWrapper<com.fc.v2.model.auto.TSysCity>()
                    .in("city_code", cityCodes))) {
                cityProvMap.put(c.getCityCode(), c.getProvinceCode());
            }
        }
        for (TGasPipelineArch p : list) {
            if (p.getZoneId() != null) {
                TGasZone z = zoneMap.get(p.getZoneId());
                if (z != null) {
                    p.setZoneName(z.getZoneName());
                }
            }
            p.setStreetName(streetNameMap.get(p.getStreetCode()));
            p.setAreaName(areaNameMap.get(p.getAreaCode()));
            TSysArea a = areaMap.get(p.getAreaCode());
            if (a != null) {
                p.setCityCode(a.getCityCode());
                p.setProvinceCode(cityProvMap.get(a.getCityCode()));
            }
        }
    }
}
