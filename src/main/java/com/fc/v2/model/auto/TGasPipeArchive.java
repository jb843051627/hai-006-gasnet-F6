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
 * 燃气管道/调压站基础档案对象 t_gas_pipe_archive
 *
 * <p>建档规则：编号全局唯一；坐标在建档时校验；档案只增不删（无删除接口，del_flag 恒为 0），
 * 停用/报废只走状态流转。所属行政区划存四级编码，region_name 为服务端按编码解析的名称快照。
 *
 * @author fuce
 * @date 2026-09-29
 */
@TableName("t_gas_pipe_archive")
@ApiModel(value = "TGasPipeArchive", description = "燃气管道/调压站基础档案")
public class TGasPipeArchive implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 主键 */
    @TableId(type = IdType.ASSIGN_ID)
    @JsonSerialize(using = ToStringSerializer.class)
    @ApiModelProperty(value = "主键")
    private Long id;

    /** 档案编号(管道/调压站全局唯一) */
    @TableField("archive_no")
    @ApiModelProperty(value = "档案编号")
    private String archiveNo;

    /** 档案类别 0管道 1调压站 */
    @TableField("archive_type")
    @ApiModelProperty(value = "档案类别 0管道 1调压站")
    private Integer archiveType;

    /** 档案名称 */
    @TableField("archive_name")
    @ApiModelProperty(value = "档案名称")
    private String archiveName;

    /** 起点经度(调压站为站点经度) */
    @TableField("start_lng")
    @ApiModelProperty(value = "起点经度")
    private BigDecimal startLng;

    /** 起点纬度(调压站为站点纬度) */
    @TableField("start_lat")
    @ApiModelProperty(value = "起点纬度")
    private BigDecimal startLat;

    /** 终点经度(调压站无终点,留空) */
    @TableField("end_lng")
    @ApiModelProperty(value = "终点经度")
    private BigDecimal endLng;

    /** 终点纬度(调压站无终点,留空) */
    @TableField("end_lat")
    @ApiModelProperty(value = "终点纬度")
    private BigDecimal endLat;

    /** 管径尺寸(如 DN300/De110) */
    @TableField("pipe_spec")
    @ApiModelProperty(value = "管径尺寸")
    private String pipeSpec;

    /** 材质类型(如 无缝钢管/PE管/铸铁管) */
    @TableField("material_type")
    @ApiModelProperty(value = "材质类型")
    private String materialType;

    /** 铺设年份 */
    @TableField("lay_year")
    @ApiModelProperty(value = "铺设年份")
    private Integer layYear;

    /** 所属省编码 */
    @TableField("province_code")
    @ApiModelProperty(value = "所属省编码")
    private String provinceCode;

    /** 所属市编码 */
    @TableField("city_code")
    @ApiModelProperty(value = "所属市编码")
    private String cityCode;

    /** 所属区编码 */
    @TableField("area_code")
    @ApiModelProperty(value = "所属区编码")
    private String areaCode;

    /** 所属街道编码 */
    @TableField("street_code")
    @ApiModelProperty(value = "所属街道编码")
    private String streetCode;

    /** 行政区划全名快照(省—市—区—街道) */
    @TableField("region_name")
    @ApiModelProperty(value = "行政区划全名快照")
    private String regionName;

    /** 所属片区(街道以下片区,文本) */
    @TableField("sector_name")
    @ApiModelProperty(value = "所属片区")
    private String sectorName;

    /** 当前状态 0在用 1停用 2报废 */
    @TableField("status")
    @ApiModelProperty(value = "当前状态 0在用 1停用 2报废")
    private Integer status;

    /** 状态最近变更时间 */
    @TableField("status_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @ApiModelProperty(value = "状态最近变更时间")
    private Date statusTime;

    /** 删除标记 恒为0:档案严禁删除 */
    @TableField("del_flag")
    @ApiModelProperty(value = "删除标记,恒为0")
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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getArchiveNo() {
        return archiveNo;
    }

    public void setArchiveNo(String archiveNo) {
        this.archiveNo = archiveNo;
    }

    public Integer getArchiveType() {
        return archiveType;
    }

    public void setArchiveType(Integer archiveType) {
        this.archiveType = archiveType;
    }

    public String getArchiveName() {
        return archiveName;
    }

    public void setArchiveName(String archiveName) {
        this.archiveName = archiveName;
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

    public String getPipeSpec() {
        return pipeSpec;
    }

    public void setPipeSpec(String pipeSpec) {
        this.pipeSpec = pipeSpec;
    }

    public String getMaterialType() {
        return materialType;
    }

    public void setMaterialType(String materialType) {
        this.materialType = materialType;
    }

    public Integer getLayYear() {
        return layYear;
    }

    public void setLayYear(Integer layYear) {
        this.layYear = layYear;
    }

    public String getProvinceCode() {
        return provinceCode;
    }

    public void setProvinceCode(String provinceCode) {
        this.provinceCode = provinceCode;
    }

    public String getCityCode() {
        return cityCode;
    }

    public void setCityCode(String cityCode) {
        this.cityCode = cityCode;
    }

    public String getAreaCode() {
        return areaCode;
    }

    public void setAreaCode(String areaCode) {
        this.areaCode = areaCode;
    }

    public String getStreetCode() {
        return streetCode;
    }

    public void setStreetCode(String streetCode) {
        this.streetCode = streetCode;
    }

    public String getRegionName() {
        return regionName;
    }

    public void setRegionName(String regionName) {
        this.regionName = regionName;
    }

    public String getSectorName() {
        return sectorName;
    }

    public void setSectorName(String sectorName) {
        this.sectorName = sectorName;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Date getStatusTime() {
        return statusTime;
    }

    public void setStatusTime(Date statusTime) {
        this.statusTime = statusTime;
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
}
