package com.fc.v2.model.custom;

/**
 * 区域档案汇总统计行：某一行政层级节点下的管道/调压站档案数量与状态分布。
 * level 1省 2市 3区 4街道 5片区。
 *
 * @author fuce
 * @date 2026-09-29
 */
public class GasRegionStatVo {

    /** 层级 1省 2市 3区 4街道 5片区 */
    private Integer level;

    /** 节点编码（省/市/区/街道代号或片区ID字符串） */
    private String regionCode;

    /** 节点名称 */
    private String regionName;

    /** 父节点编码 */
    private String parentCode;

    /** 在用数 */
    private long inUse;

    /** 停用数 */
    private long stopped;

    /** 报废数 */
    private long scrapped;

    /** 管道档案数 */
    private long pipeCount;

    /** 调压站档案数 */
    private long stationCount;

    public long getTotal() {
        return inUse + stopped + scrapped;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public String getRegionCode() {
        return regionCode;
    }

    public void setRegionCode(String regionCode) {
        this.regionCode = regionCode;
    }

    public String getRegionName() {
        return regionName;
    }

    public void setRegionName(String regionName) {
        this.regionName = regionName;
    }

    public String getParentCode() {
        return parentCode;
    }

    public void setParentCode(String parentCode) {
        this.parentCode = parentCode;
    }

    public long getInUse() {
        return inUse;
    }

    public void setInUse(long inUse) {
        this.inUse = inUse;
    }

    public long getStopped() {
        return stopped;
    }

    public void setStopped(long stopped) {
        this.stopped = stopped;
    }

    public long getScrapped() {
        return scrapped;
    }

    public void setScrapped(long scrapped) {
        this.scrapped = scrapped;
    }

    public long getPipeCount() {
        return pipeCount;
    }

    public void setPipeCount(long pipeCount) {
        this.pipeCount = pipeCount;
    }

    public long getStationCount() {
        return stationCount;
    }

    public void setStationCount(long stationCount) {
        this.stationCount = stationCount;
    }
}
