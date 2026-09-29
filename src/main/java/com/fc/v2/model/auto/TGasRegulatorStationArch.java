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
 * 燃气调压站基础档案对象 t_gas_regulator_station_arch
 *
 * @author fuce
 * @date 2026-09-29
 */
@TableName("t_gas_regulator_station_arch")
@ApiModel(value = "TGasRegulatorStationArch", description = "燃气调压站基础档案")
public class TGasRegulatorStationArch implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 主键 */
    @TableId(type = IdType.ASSIGN_ID)
    @JsonSerialize(using = ToStringSerializer.class)
    @ApiModelProperty(value = "主键")
    private Long id;

    /** 调压站编号（全库唯一，建档后不可改） */
    @TableField("station_no")
    @ApiModelProperty(value = "调压站编号")
    private String stationNo;

    /** 调压站名称 */
    @TableField("station_name")
    @ApiModelProperty(value = "调压站名称")
    private String stationName;

    /** 站址经度 */
    @TableField("lng")
    @ApiModelProperty(value = "站址经度")
    private BigDecimal lng;

    /** 站址纬度 */
    @TableField("lat")
    @ApiModelProperty(value = "站址纬度")
    private BigDecimal lat;

    /** 进出口管径尺寸描述 */
    @TableField("diameter")
    @ApiModelProperty(value = "进出口管径尺寸描述")
    private String diameter;

    /** 主体材质类型 */
    @TableField("material")
    @ApiModelProperty(value = "主体材质类型")
    private String material;

    /** 建成/铺设年份 */
    @TableField("lay_year")
    @ApiModelProperty(value = "建成/铺设年份")
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

    /** 调压器设备型号 */
    @TableField("device_model")
    @ApiModelProperty(value = "调压器设备型号")
    private String deviceModel;

    /** 设计压力(MPa) */
    @TableField("design_pressure")
    @ApiModelProperty(value = "设计压力(MPa)")
    private BigDecimal designPressure;

    /** 出口设定压力(MPa) */
    @TableField("outlet_pressure")
    @ApiModelProperty(value = "出口设定压力(MPa)")
    private BigDecimal outletPressure;

    /** 额定流量(m³/h) */
    @TableField("rated_flow")
    @ApiModelProperty(value = "额定流量(m³/h)")
    private BigDecimal ratedFlow;

    /** 设备厂家 */
    @TableField("manufacturer")
    @ApiModelProperty(value = "设备厂家")
    private String manufacturer;

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

    public String getStationNo() {
        return stationNo;
    }

    public void setStationNo(String stationNo) {
        this.stationNo = stationNo;
    }

    public String getStationName() {
        return stationName;
    }

    public void setStationName(String stationName) {
        this.stationName = stationName;
    }

    public BigDecimal getLng() {
        return lng;
    }

    public void setLng(BigDecimal lng) {
        this.lng = lng;
    }

    public BigDecimal getLat() {
        return lat;
    }

    public void setLat(BigDecimal lat) {
        this.lat = lat;
    }

    public String getDiameter() {
        return diameter;
    }

    public void setDiameter(String diameter) {
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

    public String getDeviceModel() {
        return deviceModel;
    }

    public void setDeviceModel(String deviceModel) {
        this.deviceModel = deviceModel;
    }

    public BigDecimal getDesignPressure() {
        return designPressure;
    }

    public void setDesignPressure(BigDecimal designPressure) {
        this.designPressure = designPressure;
    }

    public BigDecimal getOutletPressure() {
        return outletPressure;
    }

    public void setOutletPressure(BigDecimal outletPressure) {
        this.outletPressure = outletPressure;
    }

    public BigDecimal getRatedFlow() {
        return ratedFlow;
    }

    public void setRatedFlow(BigDecimal ratedFlow) {
        this.ratedFlow = ratedFlow;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
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
