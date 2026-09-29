package com.fc.v2.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.fc.v2.model.auto.TGasZone;
import com.fc.v2.model.custom.GasRegionStatVo;

import java.util.List;
import java.util.Map;

/**
 * 燃气档案行政区划片区 Service接口
 *
 * @author fuce
 * @date 2026-09-29
 */
public interface ITGasZoneService {

    /** 按条件查询片区列表（含街道/区名称） */
    List<TGasZone> selectZoneList(Wrapper<TGasZone> queryWrapper);

    /** 按主键查询 */
    TGasZone selectZoneById(Long id);

    /** 新建片区：编号唯一，按街道回填区/市/省编码 */
    int insertZone(TGasZone record);

    /** 编辑片区：名称/排序/状态/所属街道划转可改，编号不可改 */
    int updateZone(TGasZone record);

    /**
     * 行政区划树（省—市—区—街道—片区）。
     * 节点字段：id/pId/name/level(1省 2市 3区 4街道 5片区)/code/status。
     */
    List<Map<String, Object>> regionTree();

    /**
     * 按区域汇总管道与调压站档案数量及状态分布（区→街道→片区，市/省在区基础上上卷）。
     *
     * @param areaCode   仅统计指定区（可空）
     * @param streetCode 仅统计指定街道（可空）
     * @param zoneName   片区名模糊（可空）
     */
    List<GasRegionStatVo> regionStats(String areaCode, String streetCode, String zoneName);
}
