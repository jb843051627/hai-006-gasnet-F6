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
 * 调压站设备维护记录对象 t_gas_station_maint_record（只增不改）
 *
 * @author fuce
 * @date 2026-09-29
 */
@TableName("t_gas_station_maint_record")
@ApiModel(value = "TGasStationMaintRecord", description = "调压站设备维护记录")
public class TGasStationMaintRecord implements Serializable {
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

    /** 调压站编号（冗余快照） */
    @TableField("station_no")
    @ApiModelProperty(value = "调压站编号")
    private String stationNo;

    /** 维护时间 */
    @TableField("maint_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @ApiModelProperty(value = "维护时间")
    private Date maintTime;

    /** 维护类型(日常巡检/定期保养/故障维修/大修改造) */
    @TableField("maint_type")
    @ApiModelProperty(value = "维护类型")
    private String maintType;

    /** 维护对象设备型号（快照） */
    @TableField("device_model")
    @ApiModelProperty(value = "维护对象设备型号")
    private String deviceModel;

    /** 维护内容 */
    @TableField("content")
    @ApiModelProperty(value = "维护内容")
    private String content;

    /** 维护人 */
    @TableField("maintainer")
    @ApiModelProperty(value = "维护人")
    private String maintainer;

    /** 下次维护计划时间 */
    @TableField("next_maint_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @ApiModelProperty(value = "下次维护计划时间")
    private Date nextMaintTime;

    /** 登记人 */
    @TableField(value = "create_by", fill = FieldFill.INSERT)
    @ApiModelProperty(value = "登记人")
    private String createBy;

    /** 登记时间 */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @ApiModelProperty(value = "登记时间")
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

    public String getStationNo() {
        return stationNo;
    }

    public void setStationNo(String stationNo) {
        this.stationNo = stationNo;
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

    public String getDeviceModel() {
        return deviceModel;
    }

    public void setDeviceModel(String deviceModel) {
        this.deviceModel = deviceModel;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getMaintainer() {
        return maintainer;
    }

    public void setMaintainer(String maintainer) {
        this.maintainer = maintainer;
    }

    public Date getNextMaintTime() {
        return nextMaintTime;
    }

    public void setNextMaintTime(Date nextMaintTime) {
        this.nextMaintTime = nextMaintTime;
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
