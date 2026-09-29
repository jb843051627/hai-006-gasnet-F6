package com.fc.v2.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.fc.v2.model.auto.TGasPipeArchive;
import com.fc.v2.model.custom.RegionStatNode;

import java.util.List;

/**
 * 燃气管道/调压站基础档案 Service 接口。
 *
 * <p>档案台账铁律：只有建档与变更，没有删除；停用/报废仅走状态流转。
 *
 * @author fuce
 * @date 2026-09-29
 */
public interface ITGasPipeArchiveService {

    /** 档案类别：管道 */
    int TYPE_PIPE = 0;
    /** 档案类别：调压站 */
    int TYPE_STATION = 1;

    /** 状态：在用 */
    int STATUS_IN_USE = 0;
    /** 状态：停用 */
    int STATUS_STOPPED = 1;
    /** 状态：报废（终态） */
    int STATUS_SCRAPPED = 2;

    /** 按主键查询 */
    TGasPipeArchive selectById(Long id);

    /** 按条件查询列表（分页由调用方统一处理） */
    List<TGasPipeArchive> selectList(Wrapper<TGasPipeArchive> queryWrapper);

    /**
     * 建档：编号唯一性、空间坐标有效性、行政区划编码层级一致性校验。
     *
     * @throws IllegalArgumentException 任一校验不通过
     */
    int createArchive(TGasPipeArchive record);

    /**
     * 编辑档案：编号不可变，严禁删除；允许属性变更（管径材质/坐标/片区划转等）。
     * 状态调整请走 {@link #changeStatus(Long, Integer, String)}。
     *
     * @throws IllegalArgumentException 档案不存在或校验不通过
     */
    int updateArchive(TGasPipeArchive record);

    /**
     * 状态流转：在用→停用、停用→在用（恢复）、停用→报废；报废为终态不可恢复。
     *
     * @throws IllegalArgumentException 非法流转
     */
    int changeStatus(Long id, Integer targetStatus, String reason);

    /**
     * 行政区划树（仅含有档案的分支），各节点附带档案总数与在用/停用/报废分布。
     * 节点按 省—市—区—街道 编码层级挂载，点节点即按该层级编码过滤台账。
     *
     * @param archiveType 0管道 1调压站；null 全部
     */
    List<RegionStatNode> regionTree(Integer archiveType);
}
