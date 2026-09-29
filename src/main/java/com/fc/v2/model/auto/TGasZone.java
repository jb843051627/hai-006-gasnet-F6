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
 * 燃气档案行政区划片区（街道下一级）对象 t_gas_zone
 *
 * @author fuce
 * @date 2026-09-29
 */
@TableName("t_gas_zone")
@ApiModel(value = "TGasZone", description = "燃气档案行政区划片区")
public class TGasZone implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 主键 */
    @TableId(type = IdType.ASSIGN_ID)
    @JsonSerialize(using = ToStringSerializer.class)
    @ApiModelProperty(value = "主键")
    private Long id;

    /** 片区代号（全库唯一） */
    @TableField("zone_code")
    @ApiModelProperty(value = "片区代号（全库唯一）")
    private String zoneCode;

    /** 片区名称 */
    @TableField("zone_name")
    @ApiModelProperty(value = "片区名称")
    private String zoneName;

    /** 所属街道代号 */
    @TableField("street_code")
    @ApiModelProperty(value = "所属街道代号")
    private String streetCode;

    /** 所属区代号（冗余） */
    @TableField("area_code")
    @ApiModelProperty(value = "所属区代号（冗余）")
    private String areaCode;

    /** 所属市代号（冗余） */
    @TableField("city_code")
    @ApiModelProperty(value = "所属市代号（冗余）")
    private String cityCode;

    /** 所属省代号（冗余） */
    @TableField("province_code")
    @ApiModelProperty(value = "所属省代号（冗余）")
    private String provinceCode;

    /** 排序号 */
    @TableField("sort")
    @ApiModelProperty(value = "排序号")
    private Integer sort;

    /** 片区情形 0正常 1停用 */
    @TableField("status")
    @ApiModelProperty(value = "片区情形 0正常 1停用")
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

    /** 所属街道名称（联表展示用，不落库） */
    @TableField(exist = false)
    @ApiModelProperty(value = "所属街道名称")
    private String streetName;

    /** 所属区名称（联表展示用，不落库） */
    @TableField(exist = false)
    @ApiModelProperty(value = "所属区名称")
    private String areaName;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getZoneCode() {
        return zoneCode;
    }

    public void setZoneCode(String zoneCode) {
        this.zoneCode = zoneCode;
    }

    public String getZoneName() {
        return zoneName;
    }

    public void setZoneName(String zoneName) {
        this.zoneName = zoneName;
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

    public Integer getSort() {
        return sort;
    }

    public void setSort(Integer sort) {
        this.sort = sort;
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
}
