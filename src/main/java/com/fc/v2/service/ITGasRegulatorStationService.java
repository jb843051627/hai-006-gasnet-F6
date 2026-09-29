package com.fc.v2.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.fc.v2.model.auto.TGasRegulatorStationArch;
import com.fc.v2.model.auto.TGasStationMaintRecord;
import com.fc.v2.model.auto.TGasStationPipe;

import java.util.List;

/**
 * 燃气调压站基础档案 Service接口
 *
 * @author fuce
 * @date 2026-09-29
 */
public interface ITGasRegulatorStationService {

    /** 按主键查询（含片区/街道/区名称） */
    TGasRegulatorStationArch selectById(Long id);

    /** 按条件查询列表 */
    List<TGasRegulatorStationArch> selectList(Wrapper<TGasRegulatorStationArch> queryWrapper);

    /**
     * 新建档案：校验编号唯一、站址坐标有效、片区存在且启用。
     *
     * @throws IllegalArgumentException 建档校验不通过
     */
    int insert(TGasRegulatorStationArch record);

    /** 编辑档案：状态调整、片区划转、设备型号等属性可改，编号不可改 */
    int update(TGasRegulatorStationArch record);

    /** 台账导出数据集（includeHistory=false 仅在用，true 含停用/报废） */
    List<TGasRegulatorStationArch> selectForExport(Wrapper<TGasRegulatorStationArch> queryWrapper, boolean includeHistory);

    /** 查询站点管辖管段（联表带管道编号/名称/状态） */
    List<TGasStationPipe> listStationPipes(Long stationId);

    /**
     * 覆盖式保存站点管辖管段范围（先清空旧挂接再写入；关联仅解除管辖关系，
     * 不删除任何管道档案）。重复管段/不存在管段直接拒绝。
     */
    void saveStationPipes(Long stationId, List<TGasStationPipe> pipes);

    /** 新增一条维护记录（履历只增不改），自动快照站点编号与设备型号 */
    int addMaintRecord(TGasStationMaintRecord record);

    /** 查询站点维护履历（按维护时间倒序） */
    List<TGasStationMaintRecord> listMaintRecords(Long stationId);
}
