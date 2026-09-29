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
 * 调压站管辖管段关联对象 t_gas_station_pipe（多对多）
 *
 * @author fuce
 * @date 2026-09-29
 */
@TableName("t_gas_station_pipe")
@ApiModel(value = "TGasStationPipe", description = "调压站管辖管段关联")
public class TGasStationPipe implements Serializable {
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

    /** 管辖管段档案 t_gas_pipe_archive.id(管道) */
    @TableField("pipe_id")
    @JsonSerialize(using = ToStringSerializer.class)
    @ApiModelProperty(value = "管辖管段档案id")
    private Long pipeId;

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

    public Long getPipeId() {
        return pipeId;
    }

    public void setPipeId(Long pipeId) {
        this.pipeId = pipeId;
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
