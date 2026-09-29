package com.fc.v2.util;

import java.math.BigDecimal;
import java.time.Year;

/**
 * 燃气档案建档校验工具：空间坐标有效性、铺设年份、正数尺寸等建档强校验。
 * 坐标取中国境内常见读数范围：经度 [73,136)、纬度 [3,54)，
 * 覆盖 WGS-84 / GCJ-02 / BD-09 三种坐标系下的大陆区域读数。
 *
 * @author fuce
 * @date 2026-09-29
 */
public class GasArchiveValidator {

    /** 中国大陆经度区间（含下界，不含上界） */
    public static final BigDecimal LNG_MIN = new BigDecimal("73.000000");
    public static final BigDecimal LNG_MAX = new BigDecimal("136.000000");

    /** 中国大陆纬度区间（含下界，不含上界） */
    public static final BigDecimal LAT_MIN = new BigDecimal("3.000000");
    public static final BigDecimal LAT_MAX = new BigDecimal("54.000000");

    private GasArchiveValidator() {
    }

    /**
     * 校验经纬度：非空且落在中国境内读数区间。
     *
     * @param lng     经度
     * @param lat     纬度
     * @param fieldLabel 拼接错误信息用的点位名（起点/终点/站址）
     * @throws IllegalArgumentException 校验失败时带可读原因
     */
    public static void checkCoord(BigDecimal lng, BigDecimal lat, String fieldLabel) {
        if (lng == null || lat == null) {
            throw new IllegalArgumentException(fieldLabel + "坐标不完整，经度纬度均为必填项");
        }
        if (lng.compareTo(LNG_MIN) < 0 || lng.compareTo(LNG_MAX) >= 0) {
            throw new IllegalArgumentException(fieldLabel + "经度 " + lng.toPlainString()
                    + " 超出有效范围（应在 73 ~ 136 之间）");
        }
        if (lat.compareTo(LAT_MIN) < 0 || lat.compareTo(LAT_MAX) >= 0) {
            throw new IllegalArgumentException(fieldLabel + "纬度 " + lat.toPlainString()
                    + " 超出有效范围（应在 3 ~ 54 之间）");
        }
    }

    /** 起终点不得重合（同一点位不构成管段） */
    public static void checkEndpointsDistinct(BigDecimal startLng, BigDecimal startLat,
                                              BigDecimal endLng, BigDecimal endLat) {
        if (startLng.compareTo(endLng) == 0 && startLat.compareTo(endLat) == 0) {
            throw new IllegalArgumentException("起点坐标与终点坐标重合，不构成有效管段");
        }
    }

    /** 铺设年份：有公历纪年起、不晚于当前年份 */
    public static void checkLayYear(Integer layYear) {
        if (layYear == null) {
            throw new IllegalArgumentException("铺设年份为必填项");
        }
        int currentYear = Year.now().getValue();
        if (layYear < 1900 || layYear > currentYear) {
            throw new IllegalArgumentException("铺设年份 " + layYear + " 无效，应在 1900 ~ " + currentYear + " 之间");
        }
    }

    /** 正数尺寸字段（管径/长度等），允许为空（非必填时不校验） */
    public static void checkPositive(BigDecimal value, String fieldLabel) {
        if (value != null && value.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(fieldLabel + "必须为大于 0 的数值");
        }
    }
}
