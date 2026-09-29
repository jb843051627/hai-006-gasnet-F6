package com.fc.v2.model.auto;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import java.util.Date;

/**
 * 调压站管辖管段范围关联对象 t_gas_station_pipe
 *
 * @author fuce
 * @date 2026-09-29
 */
@TableName("t_gas_station_pipe")
@ApiModel(value = "TGasStationPipe", description = "调压站管辖管段范围关联")
public class TGasStationPipe implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 主键 */
    @TableId(type = IdType.ASSIGN_ID)
    @JsonSerialize(using = ToStringSerializer.class)
    @ApiModelProperty(value = "主键")
    private Long id;

    /** 调压站档案 */
    @TableField("station_id")
    @JsonSerialize(using = ToStringSerializer.class)
    @ApiModelProperty(value = "调压站档案")
    private Long stationId;

    /** 管辖管道档案 */
    @TableField("pipeline_id")
    @JsonSerialize(using = ToStringSerializer.class)
    @ApiModelProperty(value = "管辖管道档案")
    private Long pipelineId;

    /** 管辖范围说明（阀井区间/桩号等） */
    @TableField("scope_desc")
    @ApiModelProperty(value = "管辖范围说明")
    private String scopeDesc;

    /** 创建者 */
    @TableField(value = "create_by", fill = FieldFill.INSERT)
    @ApiModelProperty(value = "创建者")
    private String createBy;

    /** 创建时间 */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    /** 管道编号（联表展示用，不落库） */
    @TableField(exist = false)
    @ApiModelProperty(value = "管道编号")
    private String pipeNo;

    /** 管道名称（联表展示用，不落库） */
    @TableField(exist = false)
    @ApiModelProperty(value = "管道名称")
    private String pipeName;

    /** 管道状态（联表展示用，不落库） 0在用 1停用 2报废 */
    @TableField(exist = false)
    @ApiModelProperty(value = "管道状态")
    private Integer pipeStatus;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getStationId() {
        return stationId;
    }

    public void setStationId(Long stationId) {
        this.stationId = stationId;
    }

    public Long getPipelineId() {
        return pipelineId;
    }

    public void setPipelineId(Long pipelineId) {
        this.pipelineId = pipelineId;
    }

    public String getScopeDesc() {
        return scopeDesc;
    }

    public void setScopeDesc(String scopeDesc) {
        this.scopeDesc = scopeDesc;
    }

    public String getCreateBy() {
        return createBy;
    }

    public void setCreateBy(String createBy) {
        this.createBy = createBy;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getPipeNo() {
        return pipeNo;
    }

    public void setPipeNo(String pipeNo) {
        this.pipeNo = pipeNo;
    }

    public String getPipeName() {
        return pipeName;
    }

    public void setPipeName(String pipeName) {
        this.pipeName = pipeName;
    }

    public Integer getPipeStatus() {
        return pipeStatus;
    }

    public void setPipeStatus(Integer pipeStatus) {
        this.pipeStatus = pipeStatus;
    }
}
