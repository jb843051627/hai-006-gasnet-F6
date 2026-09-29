package com.fc.v2.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fc.v2.mapper.auto.TGasPipelineArchMapper;
import com.fc.v2.mapper.auto.TGasRegulatorStationArchMapper;
import com.fc.v2.mapper.auto.TGasStationMaintRecordMapper;
import com.fc.v2.mapper.auto.TGasStationPipeMapper;
import com.fc.v2.mapper.auto.TGasZoneMapper;
import com.fc.v2.mapper.auto.TSysAreaMapper;
import com.fc.v2.mapper.auto.TSysStreetMapper;
import com.fc.v2.model.auto.TGasPipelineArch;
import com.fc.v2.model.auto.TGasRegulatorStationArch;
import com.fc.v2.model.auto.TGasStationMaintRecord;
import com.fc.v2.model.auto.TGasStationPipe;
import com.fc.v2.model.auto.TGasZone;
import com.fc.v2.model.auto.TSysArea;
import com.fc.v2.model.auto.TSysStreet;
import com.fc.v2.service.ITGasRegulatorStationService;
import com.fc.v2.util.GasArchiveValidator;
import com.fc.v2.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 燃气调压站基础档案 Service实现
 * 档案铁律：编号唯一不可改、坐标强校验、无物理删除；管辖管段覆盖式挂接不影响管道档案本体；
 * 设备维护履历只增不改。
 *
 * @author fuce
 * @date 2026-09-29
 */
@Service
public class TGasRegulatorStationServiceImpl
        extends ServiceImpl<TGasRegulatorStationArchMapper, TGasRegulatorStationArch>
        implements ITGasRegulatorStationService {

    @Autowired
    private TGasZoneMapper gasZoneMapper;

    @Autowired
    private TSysStreetMapper sysStreetMapper;

    @Autowired
    private TSysAreaMapper sysAreaMapper;

    @Autowired
    private com.fc.v2.mapper.auto.TSysCityMapper sysCityMapper;

    @Autowired
    private TGasStationPipeMapper stationPipeMapper;

    @Autowired
    private TGasStationMaintRecordMapper maintRecordMapper;

    @Autowired
    private TGasPipelineArchMapper pipelineMapper;

    @Override
    public TGasRegulatorStationArch selectById(Long id) {
        TGasRegulatorStationArch record = this.baseMapper.selectById(id);
        if (record != null) {
            fillRegionNames(java.util.Collections.singletonList(record));
        }
        return record;
    }

    @Override
    public List<TGasRegulatorStationArch> selectList(Wrapper<TGasRegulatorStationArch> queryWrapper) {
        List<TGasRegulatorStationArch> list = this.baseMapper.selectList(queryWrapper);
        fillRegionNames(list);
        return list;
    }

    @Override
    public int insert(TGasRegulatorStationArch record) {
        validateBase(record);
        if (StringUtils.isEmpty(record.getStationNo())) {
            throw new IllegalArgumentException("调压站编号为必填项");
        }
        Integer dup = this.baseMapper.selectCount(new QueryWrapper<TGasRegulatorStationArch>()
                .eq("station_no", record.getStationNo().trim()));
        if (dup != null && dup > 0) {
            throw new IllegalArgumentException("调压站编号 " + record.getStationNo() + " 已存在，编号全库唯一");
        }
        record.setStationNo(record.getStationNo().trim());
        bindZone(record, record.getZoneId());
        record.setStatus(record.getStatus() == null ? 0 : record.getStatus());
        checkStatus(record.getStatus());
        record.setDelFlag(0);
        return this.baseMapper.insert(record);
    }

    @Override
    public int update(TGasRegulatorStationArch record) {
        if (record == null || record.getId() == null) {
            throw new IllegalArgumentException("缺少档案主键，无法编辑");
        }
        TGasRegulatorStationArch db = this.baseMapper.selectById(record.getId());
        if (db == null || (db.getDelFlag() != null && db.getDelFlag() == 1)) {
            throw new IllegalArgumentException("档案不存在，无法编辑");
        }
        validateBase(record);
        if (StringUtils.isNotEmpty(record.getStationNo()) && !db.getStationNo().equals(record.getStationNo().trim())) {
            throw new IllegalArgumentException("调压站编号建档后不允许修改");
        }
        record.setStationNo(db.getStationNo());
        if (record.getStatus() == null) {
            record.setStatus(db.getStatus());
        }
        checkStatus(record.getStatus());
        Long targetZoneId = record.getZoneId() == null ? db.getZoneId() : record.getZoneId();
        if (record.getZoneId() != null && !record.getZoneId().equals(db.getZoneId())) {
            bindZone(record, targetZoneId);
        } else {
            record.setStreetCode(db.getStreetCode());
            record.setAreaCode(db.getAreaCode());
        }
        record.setUpdateTime(new Date());
        return this.baseMapper.update(record, new QueryWrapper<TGasRegulatorStationArch>()
                .eq("id", record.getId())
                .eq("del_flag", 0));
    }

    @Override
    public List<TGasRegulatorStationArch> selectForExport(Wrapper<TGasRegulatorStationArch> queryWrapper,
                                                          boolean includeHistory) {
        QueryWrapper<TGasRegulatorStationArch> wrapper;
        if (queryWrapper instanceof QueryWrapper) {
            wrapper = (QueryWrapper<TGasRegulatorStationArch>) queryWrapper;
        } else {
            wrapper = new QueryWrapper<>();
        }
        wrapper.eq("del_flag", 0);
        if (!includeHistory) {
            wrapper.eq("status", 0);
        }
        wrapper.orderByAsc("area_code").orderByAsc("station_no");
        List<TGasRegulatorStationArch> list = this.baseMapper.selectList(wrapper);
        fillRegionNames(list);
        return list;
    }

    @Override
    public List<TGasStationPipe> listStationPipes(Long stationId) {
        List<TGasStationPipe> rels = stationPipeMapper.selectList(new QueryWrapper<TGasStationPipe>()
                .eq("station_id", stationId).orderByAsc("id"));
        if (rels.isEmpty()) {
            return rels;
        }
        List<Long> pipeIds = new ArrayList<>();
        for (TGasStationPipe r : rels) {
            pipeIds.add(r.getPipelineId());
        }
        Map<Long, TGasPipelineArch> pipeMap = new HashMap<>();
        for (TGasPipelineArch p : pipelineMapper.selectBatchIds(pipeIds)) {
            pipeMap.put(p.getId(), p);
        }
        for (TGasStationPipe r : rels) {
            TGasPipelineArch p = pipeMap.get(r.getPipelineId());
            if (p != null) {
                r.setPipeNo(p.getPipeNo());
                r.setPipeName(p.getPipeName());
                r.setPipeStatus(p.getStatus());
            }
        }
        return rels;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveStationPipes(Long stationId, List<TGasStationPipe> pipes) {
        TGasRegulatorStationArch station = requireStation(stationId);
        // 覆盖式：先解除全部旧管辖关系，再写入新挂接（不动管道档案本体）
        stationPipeMapper.delete(new QueryWrapper<TGasStationPipe>().eq("station_id", stationId));
        if (pipes == null || pipes.isEmpty()) {
            return;
        }
        Set<Long> seen = new HashSet<>();
        for (TGasStationPipe item : pipes) {
            if (item.getPipelineId() == null) {
                throw new IllegalArgumentException("管辖管段缺少管道档案主键");
            }
            if (!seen.add(item.getPipelineId())) {
                throw new IllegalArgumentException("管辖管段存在重复挂接：" + item.getPipelineId());
            }
            TGasPipelineArch pipe = pipelineMapper.selectById(item.getPipelineId());
            if (pipe == null || (pipe.getDelFlag() != null && pipe.getDelFlag() == 1)) {
                throw new IllegalArgumentException("管辖管段档案不存在：" + item.getPipelineId());
            }
            item.setId(null);
            item.setStationId(station.getId());
            stationPipeMapper.insert(item);
        }
    }

    @Override
    public int addMaintRecord(TGasStationMaintRecord record) {
        if (record == null || record.getStationId() == null) {
            throw new IllegalArgumentException("缺少调压站主键，无法登记维护记录");
        }
        TGasRegulatorStationArch station = requireStation(record.getStationId());
        if (record.getMaintTime() == null) {
            throw new IllegalArgumentException("维护时间为必填项");
        }
        if (StringUtils.isEmpty(record.getMaintType())) {
            throw new IllegalArgumentException("维护类型为必填项");
        }
        if (StringUtils.isEmpty(record.getContent())) {
            throw new IllegalArgumentException("维护内容为必填项");
        }
        // 履历只增不改：强制新主键、快照站点编号与当前设备型号
        record.setId(null);
        record.setStationNo(station.getStationNo());
        if (StringUtils.isEmpty(record.getDeviceModel())) {
            record.setDeviceModel(station.getDeviceModel());
        }
        return maintRecordMapper.insert(record);
    }

    @Override
    public List<TGasStationMaintRecord> listMaintRecords(Long stationId) {
        return maintRecordMapper.selectList(new QueryWrapper<TGasStationMaintRecord>()
                .eq("station_id", stationId).orderByDesc("maint_time").orderByDesc("id"));
    }

    private TGasRegulatorStationArch requireStation(Long stationId) {
        if (stationId == null) {
            throw new IllegalArgumentException("缺少调压站主键");
        }
        TGasRegulatorStationArch station = this.baseMapper.selectById(stationId);
        if (station == null || (station.getDelFlag() != null && station.getDelFlag() == 1)) {
            throw new IllegalArgumentException("调压站档案不存在");
        }
        return station;
    }

    private void validateBase(TGasRegulatorStationArch record) {
        if (record == null) {
            throw new IllegalArgumentException("档案内容为空");
        }
        GasArchiveValidator.checkCoord(record.getLng(), record.getLat(), "站址");
        if (record.getLayYear() != null) {
            GasArchiveValidator.checkLayYear(record.getLayYear());
        }
    }

    private void checkStatus(Integer status) {
        if (status == null || status < 0 || status > 2) {
            throw new IllegalArgumentException("状态取值非法（0在用 1停用 2报废）");
        }
    }

    private void bindZone(TGasRegulatorStationArch record, Long zoneId) {
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

    /** 批量回填 片区/街道/区 名称 */
    private void fillRegionNames(List<TGasRegulatorStationArch> list) {
        if (list == null || list.isEmpty()) {
            return;
        }
        Set<String> streetCodes = new HashSet<>();
        Set<String> areaCodes = new HashSet<>();
        List<Long> zoneIds = new ArrayList<>();
        for (TGasRegulatorStationArch s : list) {
            if (s.getStreetCode() != null) {
                streetCodes.add(s.getStreetCode());
            }
            if (s.getAreaCode() != null) {
                areaCodes.add(s.getAreaCode());
            }
            if (s.getZoneId() != null && !zoneIds.contains(s.getZoneId())) {
                zoneIds.add(s.getZoneId());
            }
        }
        Map<Long, TGasZone> zoneMap = new HashMap<>();
        if (!zoneIds.isEmpty()) {
            for (TGasZone z : gasZoneMapper.selectBatchIds(zoneIds)) {
                zoneMap.put(z.getId(), z);
            }
        }
        Map<String, String> streetNameMap = new HashMap<>();
        if (!streetCodes.isEmpty()) {
            for (TSysStreet st : sysStreetMapper.selectList(new QueryWrapper<TSysStreet>()
                    .in("street_code", streetCodes))) {
                streetNameMap.put(st.getStreetCode(), st.getStreetName());
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
        Set<String> cityCodes = new HashSet<>();
        for (TSysArea a : areaMap.values()) {
            cityCodes.add(a.getCityCode());
        }
        Map<String, String> cityProvMap = new HashMap<>();
        if (!cityCodes.isEmpty()) {
            for (com.fc.v2.model.auto.TSysCity c : sysCityMapper.selectList(
                    new QueryWrapper<com.fc.v2.model.auto.TSysCity>().in("city_code", cityCodes))) {
                cityProvMap.put(c.getCityCode(), c.getProvinceCode());
            }
        }
        for (TGasRegulatorStationArch s : list) {
            if (s.getZoneId() != null) {
                TGasZone z = zoneMap.get(s.getZoneId());
                if (z != null) {
                    s.setZoneName(z.getZoneName());
                }
            }
            s.setStreetName(streetNameMap.get(s.getStreetCode()));
            s.setAreaName(areaNameMap.get(s.getAreaCode()));
            TSysArea a = areaMap.get(s.getAreaCode());
            if (a != null) {
                s.setCityCode(a.getCityCode());
                s.setProvinceCode(cityProvMap.get(a.getCityCode()));
            }
        }
    }
}
