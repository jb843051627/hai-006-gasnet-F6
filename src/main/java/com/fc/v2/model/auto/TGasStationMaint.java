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
 * 调压站维护记录对象 t_gas_station_maint（只增不改，无更新/删除接口）
 *
 * @author fuce
 * @date 2026-09-29
 */
@TableName("t_gas_station_maint")
@ApiModel(value = "TGasStationMaint", description = "调压站维护记录")
public class TGasStationMaint implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 主键 */
    @TableId(type = IdType.ASSIGN_ID)
    @JsonSerialize(using = ToStringSerializer.class)
    @ApiModelProperty(value = "主键")
    private Long id;

    /** 调压站档案 t_gas_pipe_archive.id */
    @TableField("station_id")
    @JsonSerialize(using = ToStringSerializer.class)
    @ApiModelProperty(value = "调压站档案id")
    private Long stationId;

    /** 维护时间 */
    @TableField("maint_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @ApiModelProperty(value = "维护时间")
    private Date maintTime;

    /** 维护类别(日常维护/定期检修/年度校验/抢修) */
    @TableField("maint_type")
    @ApiModelProperty(value = "维护类别")
    private String maintType;

    /** 维护内容 */
    @TableField("content")
    @ApiModelProperty(value = "维护内容")
    private String content;

    /** 维护人 */
    @TableField("operator")
    @ApiModelProperty(value = "维护人")
    private String operator;

    /** 维护结果 */
    @TableField("result")
    @ApiModelProperty(value = "维护结果")
    private String result;

    /** 创建者 */
    @TableField(value = "create_by", fill = FieldFill.INSERT)
    @ApiModelProperty(value = "创建者")
    private String createBy;

    /** 创建时间 */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @ApiModelProperty(value = "创建时间")
    private Date createTime;

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

    public Date getMaintTime() {
        return maintTime;
    }

    public void setMaintTime(Date maintTime) {
        this.maintTime = maintTime;
    }

    public String getMaintType() {
        return maintType;
    }

    public void setMaintType(String maintType) {
        this.maintType = maintType;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
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
}
