package com.fc.v2.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fc.v2.mapper.auto.TGasPipeArchiveMapper;
import com.fc.v2.mapper.auto.TGasRegulatorStationMapper;
import com.fc.v2.mapper.auto.TGasStationMaintMapper;
import com.fc.v2.mapper.auto.TGasStationPipeMapper;
import com.fc.v2.model.auto.TGasPipeArchive;
import com.fc.v2.model.auto.TGasRegulatorStation;
import com.fc.v2.model.auto.TGasStationMaint;
import com.fc.v2.model.auto.TGasStationPipe;
import com.fc.v2.service.ITGasPipeArchiveService;
import com.fc.v2.service.ITGasStationExtraService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 调压站附属信息 Service 实现。
 *
 * <p>管辖管段关联是纯关系表（无状态/无删除标记），随管辖范围调整做全量覆盖；
 * 维护记录只增不改；设备型号与调压站一对一 upsert。以上操作均不触碰档案主表。
 *
 * @author fuce
 * @date 2026-09-29
 */
@Service
public class TGasStationExtraServiceImpl implements ITGasStationExtraService {

    private static final int DEL_NORMAL = 0;

    @Resource
    private TGasRegulatorStationMapper stationMapper;
    @Resource
    private TGasStationPipeMapper stationPipeMapper;
    @Resource
    private TGasStationMaintMapper maintMapper;
    @Resource
    private TGasPipeArchiveMapper archiveMapper;

    @Override
    public String getDeviceModel(Long stationId) {
        TGasRegulatorStation row = findStationRow(stationId);
        return row == null ? null : row.getDeviceModel();
    }

    @Override
    public int saveDeviceModel(Long stationId, String deviceModel, String remark) {
        requireStation(stationId);
        TGasRegulatorStation row = findStationRow(stationId);
        if (row == null) {
            row = new TGasRegulatorStation();
            row.setStationId(stationId);
            row.setDeviceModel(trimToNull(deviceModel));
            row.setRemark(trimToNull(remark));
            row.setDelFlag(DEL_NORMAL);
            return stationMapper.insert(row);
        }
        row.setDeviceModel(trimToNull(deviceModel));
        if (remark != null) {
            row.setRemark(trimToNull(remark));
        }
        // 显式 set：MP 默认 updateById 忽略 null，显式清空设备型号时需要落 NULL；
        // entity 为 null 时自动填充不触发，update_by/update_time 手动写入
        row.setUpdateBy(com.fc.v2.shiro.util.ShiroUtils.getLoginName());
        row.setUpdateTime(new java.util.Date());
        return stationMapper.update(null, new com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper<TGasRegulatorStation>()
                .eq("id", row.getId()).eq("del_flag", DEL_NORMAL)
                .set("device_model", row.getDeviceModel())
                .set("remark", row.getRemark())
                .set("update_by", row.getUpdateBy())
                .set("update_time", row.getUpdateTime()));
    }

    @Override
    public List<TGasPipeArchive> listManagedPipes(Long stationId) {
        requireStation(stationId);
        List<TGasStationPipe> links = stationPipeMapper.selectList(new QueryWrapper<TGasStationPipe>()
                .eq("station_id", stationId)
                .orderByAsc("pipe_id"));
        List<TGasPipeArchive> pipes = new ArrayList<TGasPipeArchive>();
        for (TGasStationPipe link : links) {
            TGasPipeArchive pipe = archiveMapper.selectById(link.getPipeId());
            // 档案永不删除，理论上必命中；仍防御性跳过空值
            if (pipe != null && pipe.getDelFlag() != null && pipe.getDelFlag() == DEL_NORMAL) {
                pipes.add(pipe);
            }
        }
        return pipes;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int replaceManagedPipes(Long stationId, List<Long> pipeIds) {
        requireStation(stationId);
        Set<Long> want = new HashSet<Long>();
        if (pipeIds != null) {
            for (Long pid : pipeIds) {
                if (pid == null) {
                    continue;
                }
                if (pid.equals(stationId)) {
                    throw new IllegalArgumentException("调压站不能把自己关联为管辖管段");
                }
                TGasPipeArchive pipe = archiveMapper.selectById(pid);
                if (pipe == null || (pipe.getDelFlag() != null && pipe.getDelFlag() != DEL_NORMAL)) {
                    throw new IllegalArgumentException("管段档案不存在：" + pid);
                }
                if (pipe.getArchiveType() != null && pipe.getArchiveType() == ITGasPipeArchiveService.TYPE_STATION) {
                    throw new IllegalArgumentException("仅可关联管道档案，" + pid + " 是调压站");
                }
                want.add(pid);
            }
        }
        // 全量覆盖：删除旧关系、插入新关系（关系行无留痕价值，档案本体不受影响）
        stationPipeMapper.delete(new QueryWrapper<TGasStationPipe>().eq("station_id", stationId));
        for (Long pid : want) {
            TGasStationPipe link = new TGasStationPipe();
            link.setStationId(stationId);
            link.setPipeId(pid);
            stationPipeMapper.insert(link);
        }
        return want.size();
    }

    @Override
    public List<TGasStationMaint> listMaintRecords(Long stationId) {
        requireStation(stationId);
        return maintMapper.selectList(new QueryWrapper<TGasStationMaint>()
                .eq("station_id", stationId)
                .orderByDesc("maint_time")
                .orderByDesc("id"));
    }

    @Override
    public int addMaintRecord(TGasStationMaint record) {
        if (record == null) {
            throw new IllegalArgumentException("维护记录内容不能为空");
        }
        requireStation(record.getStationId());
        if (record.getMaintTime() == null) {
            record.setMaintTime(new Date());
        }
        if (trimToNull(record.getMaintType()) == null) {
            throw new IllegalArgumentException("维护类别不能为空");
        }
        if (trimToNull(record.getContent()) == null) {
            throw new IllegalArgumentException("维护内容不能为空");
        }
        record.setMaintType(record.getMaintType().trim());
        return maintMapper.insert(record);
    }

    private TGasRegulatorStation findStationRow(Long stationId) {
        if (stationId == null) {
            return null;
        }
        List<TGasRegulatorStation> list = stationMapper.selectList(new QueryWrapper<TGasRegulatorStation>()
                .eq("station_id", stationId)
                .eq("del_flag", DEL_NORMAL)
                .last("limit 1"));
        return list.isEmpty() ? null : list.get(0);
    }

    /** 确认主表存在且确为调压站，防止把管道 id 串到附属接口 */
    private TGasPipeArchive requireStation(Long stationId) {
        if (stationId == null) {
            throw new IllegalArgumentException("缺少调压站档案id");
        }
        TGasPipeArchive archive = archiveMapper.selectOne(new QueryWrapper<TGasPipeArchive>()
                .eq("id", stationId).eq("del_flag", DEL_NORMAL));
        if (archive == null) {
            throw new IllegalArgumentException("调压站档案不存在：" + stationId);
        }
        if (archive.getArchiveType() == null || archive.getArchiveType() != ITGasPipeArchiveService.TYPE_STATION) {
            throw new IllegalArgumentException("该档案不是调压站：" + stationId);
        }
        return archive;
    }

    private static String trimToNull(String s) {
        return s == null || s.trim().isEmpty() ? null : s.trim();
    }
}
