package com.fc.v2.service;

import com.fc.v2.model.auto.TGasPipeArchive;
import com.fc.v2.model.auto.TGasStationMaint;

import java.util.List;

/**
 * 调压站附属信息 Service：设备型号（一对一）、管辖管段范围（多对多）、维护记录（只增不改）。
 *
 * @author fuce
 * @date 2026-09-29
 */
public interface ITGasStationExtraService {

    /** 读取调压站设备型号附属行（无则 null） */
    String getDeviceModel(Long stationId);

    /** 保存（不存在则建，存在则改）设备型号；archive 必须为调压站 */
    int saveDeviceModel(Long stationId, String deviceModel, String remark);

    /** 调压站管辖的管道档案列表 */
    List<TGasPipeArchive> listManagedPipes(Long stationId);

    /**
     * 全量覆盖调压站管辖管段范围（传入的 pipeIds 必须为在档管道，且不能指向调压站自身）。
     *
     * @return 最终关联条数
     */
    int replaceManagedPipes(Long stationId, List<Long> pipeIds);

    /** 维护记录按时间倒序 */
    List<TGasStationMaint> listMaintRecords(Long stationId);

    /** 追加一条维护记录（只增）；maintTime 缺省为当前时间 */
    int addMaintRecord(TGasStationMaint record);
}
