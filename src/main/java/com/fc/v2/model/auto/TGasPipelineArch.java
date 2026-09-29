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
import java.math.BigDecimal;
import java.util.Date;

/**
 * 燃气管道基础档案对象 t_gas_pipeline_arch
 *
 * @author fuce
 * @date 2026-09-29
 */
@TableName("t_gas_pipeline_arch")
@ApiModel(value = "TGasPipelineArch", description = "燃气管道基础档案")
public class TGasPipelineArch implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 主键 */
    @TableId(type = IdType.ASSIGN_ID)
    @JsonSerialize(using = ToStringSerializer.class)
    @ApiModelProperty(value = "主键")
    private Long id;

    /** 管道编号（全库唯一，建档后不可改） */
    @TableField("pipe_no")
    @ApiModelProperty(value = "管道编号")
    private String pipeNo;

    /** 管道名称/起止描述 */
    @TableField("pipe_name")
    @ApiModelProperty(value = "管道名称")
    private String pipeName;

    /** 起点经度 */
    @TableField("start_lng")
    @ApiModelProperty(value = "起点经度")
    private BigDecimal startLng;

    /** 起点纬度 */
    @TableField("start_lat")
    @ApiModelProperty(value = "起点纬度")
    private BigDecimal startLat;

    /** 终点经度 */
    @TableField("end_lng")
    @ApiModelProperty(value = "终点经度")
    private BigDecimal endLng;

    /** 终点纬度 */
    @TableField("end_lat")
    @ApiModelProperty(value = "终点纬度")
    private BigDecimal endLat;

    /** 管径尺寸(mm) */
    @TableField("diameter")
    @ApiModelProperty(value = "管径尺寸(mm)")
    private BigDecimal diameter;

    /** 材质类型 */
    @TableField("material")
    @ApiModelProperty(value = "材质类型")
    private String material;

    /** 铺设年份 */
    @TableField("lay_year")
    @ApiModelProperty(value = "铺设年份")
    private Integer layYear;

    /** 所属片区 */
    @TableField("zone_id")
    @JsonSerialize(using = ToStringSerializer.class)
    @ApiModelProperty(value = "所属片区")
    private Long zoneId;

    /** 所属街道代号（冗余） */
    @TableField("street_code")
    @ApiModelProperty(value = "所属街道代号")
    private String streetCode;

    /** 所属区代号（冗余） */
    @TableField("area_code")
    @ApiModelProperty(value = "所属区代号")
    private String areaCode;

    /** 压力级制 */
    @TableField("pressure_level")
    @ApiModelProperty(value = "压力级制")
    private String pressureLevel;

    /** 管线长度(m) */
    @TableField("length_m")
    @ApiModelProperty(value = "管线长度(m)")
    private BigDecimal lengthM;

    /** 当前状态 0在用 1停用 2报废 */
    @TableField("status")
    @ApiModelProperty(value = "当前状态 0在用 1停用 2报废")
    private Integer status;

    /** 删除标记（档案铁律：恒为0，不允许删除） */
    @TableField("del_flag")
    @ApiModelProperty(value = "删除标记")
    private Integer delFlag;

    /** 创建者 */
    @TableField(value = "create_by", fill = FieldFill.INSERT)
    @ApiModelProperty(value = "创建者")
    private String createBy;

    /** 创建时间 */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    /** 更新者 */
    @TableField(value = "update_by", fill = FieldFill.UPDATE)
    @ApiModelProperty(value = "更新者")
    private String updateBy;

    /** 更新时间 */
    @TableField(value = "update_time", fill = FieldFill.UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @ApiModelProperty(value = "更新时间")
    private Date updateTime;

    /** 备注 */
    @TableField("remark")
    @ApiModelProperty(value = "备注")
    private String remark;

    /** 片区名称（联表展示用，不落库） */
    @TableField(exist = false)
    @ApiModelProperty(value = "片区名称")
    private String zoneName;

    /** 所属街道名称（联表展示用，不落库） */
    @TableField(exist = false)
    @ApiModelProperty(value = "所属街道名称")
    private String streetName;

    /** 所属区名称（联表展示用，不落库） */
    @TableField(exist = false)
    @ApiModelProperty(value = "所属区名称")
    private String areaName;

    /** 所属市代号（按区推导，不落库，编辑回显用） */
    @TableField(exist = false)
    @ApiModelProperty(value = "所属市代号")
    private String cityCode;

    /** 所属省代号（按区推导，不落库，编辑回显用） */
    @TableField(exist = false)
    @ApiModelProperty(value = "所属省代号")
    private String provinceCode;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public BigDecimal getStartLng() {
        return startLng;
    }

    public void setStartLng(BigDecimal startLng) {
        this.startLng = startLng;
    }

    public BigDecimal getStartLat() {
        return startLat;
    }

    public void setStartLat(BigDecimal startLat) {
        this.startLat = startLat;
    }

    public BigDecimal getEndLng() {
        return endLng;
    }

    public void setEndLng(BigDecimal endLng) {
        this.endLng = endLng;
    }

    public BigDecimal getEndLat() {
        return endLat;
    }

    public void setEndLat(BigDecimal endLat) {
        this.endLat = endLat;
    }

    public BigDecimal getDiameter() {
        return diameter;
    }

    public void setDiameter(BigDecimal diameter) {
        this.diameter = diameter;
    }

    public String getMaterial() {
        return material;
    }

    public void setMaterial(String material) {
        this.material = material;
    }

    public Integer getLayYear() {
        return layYear;
    }

    public void setLayYear(Integer layYear) {
        this.layYear = layYear;
    }

    public Long getZoneId() {
        return zoneId;
    }

    public void setZoneId(Long zoneId) {
        this.zoneId = zoneId;
    }

    public String getStreetCode() {
        return streetCode;
    }

    public void setStreetCode(String streetCode) {
        this.streetCode = streetCode;
    }

    public String getAreaCode() {
        return areaCode;
    }

    public void setAreaCode(String areaCode) {
        this.areaCode = areaCode;
    }

    public String getPressureLevel() {
        return pressureLevel;
    }

    public void setPressureLevel(String pressureLevel) {
        this.pressureLevel = pressureLevel;
    }

    public BigDecimal getLengthM() {
        return lengthM;
    }

    public void setLengthM(BigDecimal lengthM) {
        this.lengthM = lengthM;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getDelFlag() {
        return delFlag;
    }

    public void setDelFlag(Integer delFlag) {
        this.delFlag = delFlag;
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

    public String getUpdateBy() {
        return updateBy;
    }

    public void setUpdateBy(String updateBy) {
        this.updateBy = updateBy;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getZoneName() {
        return zoneName;
    }

    public void setZoneName(String zoneName) {
        this.zoneName = zoneName;
    }

    public String getStreetName() {
        return streetName;
    }

    public void setStreetName(String streetName) {
        this.streetName = streetName;
    }

    public String getAreaName() {
        return areaName;
    }

    public void setAreaName(String areaName) {
        this.areaName = areaName;
    }

    public String getCityCode() {
        return cityCode;
    }

    public void setCityCode(String cityCode) {
        this.cityCode = cityCode;
    }

    public String getProvinceCode() {
        return provinceCode;
    }

    public void setProvinceCode(String provinceCode) {
        this.provinceCode = provinceCode;
    }
}
