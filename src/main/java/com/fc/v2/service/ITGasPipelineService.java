package com.fc.v2.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.fc.v2.model.auto.TGasPipelineArch;

import java.util.List;

/**
 * 燃气管道基础档案 Service接口
 *
 * @author fuce
 * @date 2026-09-29
 */
public interface ITGasPipelineService {

    /** 按主键查询（含片区/街道/区名称） */
    TGasPipelineArch selectById(Long id);

    /** 按条件查询列表（分页由调用方统一处理） */
    List<TGasPipelineArch> selectList(Wrapper<TGasPipelineArch> queryWrapper);

    /**
     * 新建档案：校验编号唯一、坐标有效、片区存在且启用。
     *
     * @throws IllegalArgumentException 任一建档校验不通过
     */
    int insert(TGasPipelineArch record);

    /**
     * 编辑档案：允许状态调整、片区划转、属性变更；编号建档后不可改。
     *
     * @throws IllegalArgumentException 记录不存在、编号被篡改或校验不通过
     */
    int update(TGasPipelineArch record);

    /**
     * 台账导出数据集。
     *
     * @param queryWrapper   编号/名称/区域等过滤条件
     * @param includeHistory true=含停用/报废历史档案；false=仅在用
     */
    List<TGasPipelineArch> selectForExport(Wrapper<TGasPipelineArch> queryWrapper, boolean includeHistory);
}
