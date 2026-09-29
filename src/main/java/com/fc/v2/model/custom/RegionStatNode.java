package com.fc.v2.model.custom;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;

/**
 * 行政区划档案统计树节点（dtree list 风格：id/parentId/title）。
 *
 * <p>层级：0省 1市 2区 3街道；每个节点汇总挂在本层级（含下级）档案的状态分布。
 *
 * @author fuce
 * @date 2026-09-29
 */
@ApiModel(value = "RegionStatNode", description = "行政区划档案统计节点")
public class RegionStatNode implements Serializable {
    private static final long serialVersionUID = 1L;

    /** dtree 节点id：用层级编码 */
    private String id;

    /** dtree 父节点id：上级编码；省为 "0" */
    private String parentId;

    /** dtree 节点标题：名称(总数) */
    private String title;

    /** 层级 0省 1市 2区 3街道 */
    private Integer level;

    /** 本层级编码（列表过滤回传用） */
    private String code;

    /** 层级字段名：provinceCode/cityCode/areaCode/streetCode */
    private String levelField;

    /** 区划名称 */
    private String name;

    /** 档案总数 */
    private Integer total;

    /** 在用数 */
    private Integer inUse;

    /** 停用数 */
    private Integer stopped;

    /** 报废数 */
    private Integer scrapped;

    public RegionStatNode() {
    }

    public RegionStatNode(String id, String parentId, Integer level, String code, String levelField, String name) {
        this.id = id;
        this.parentId = parentId;
        this.level = level;
        this.code = code;
        this.levelField = levelField;
        this.name = name;
        this.total = 0;
        this.inUse = 0;
        this.stopped = 0;
        this.scrapped = 0;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getLevelField() {
        return levelField;
    }

    public void setLevelField(String levelField) {
        this.levelField = levelField;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }

    public Integer getInUse() {
        return inUse;
    }

    public void setInUse(Integer inUse) {
        this.inUse = inUse;
    }

    public Integer getStopped() {
        return stopped;
    }

    public void setStopped(Integer stopped) {
        this.stopped = stopped;
    }

    public Integer getScrapped() {
        return scrapped;
    }

    @ApiModelProperty(value = "报废数")
    public void setScrapped(Integer scrapped) {
        this.scrapped = scrapped;
    }
}
