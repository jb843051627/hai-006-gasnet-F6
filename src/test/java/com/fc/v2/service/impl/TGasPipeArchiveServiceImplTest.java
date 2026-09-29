package com.fc.v2.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.fc.v2.mapper.auto.TGasPipeArchiveMapper;
import com.fc.v2.model.auto.TGasPipeArchive;
import com.fc.v2.model.auto.TSysArea;
import com.fc.v2.model.auto.TSysCity;
import com.fc.v2.model.auto.TSysProvince;
import com.fc.v2.model.auto.TSysStreet;
import com.fc.v2.model.custom.RegionStatNode;
import com.fc.v2.service.ITSysAreaService;
import com.fc.v2.service.ITSysCityService;
import com.fc.v2.service.ITSysProvinceService;
import com.fc.v2.service.ITSysStreetService;

/**
 * 管道/调压站档案建档校验、编辑冻结、状态流转与区域汇总的单元测试。
 */
@ExtendWith(MockitoExtension.class)
class TGasPipeArchiveServiceImplTest {

    @Mock
    private TGasPipeArchiveMapper archiveMapper;
    @Mock
    private ITSysProvinceService provinceService;
    @Mock
    private ITSysCityService cityService;
    @Mock
    private ITSysAreaService areaService;
    @Mock
    private ITSysStreetService streetService;

    @InjectMocks
    private TGasPipeArchiveServiceImpl service;

    private static final String P_CODE = "990000";
    private static final String C_CODE = "990100";
    private static final String A_CODE = "990101";
    private static final String S_CODE = "990101001";

    private TGasPipeArchive basePipe() {
        TGasPipeArchive r = new TGasPipeArchive();
        r.setArchiveNo("PA-GD-0001");
        r.setArchiveType(0);
        r.setArchiveName("东湖路主干管");
        r.setStartLng(new BigDecimal("112.99"));
        r.setStartLat(new BigDecimal("28.24"));
        r.setEndLng(new BigDecimal("113.00"));
        r.setEndLat(new BigDecimal("28.25"));
        r.setPipeSpec("DN300");
        r.setMaterialType("无缝钢管");
        r.setLayYear(2012);
        r.setAreaCode(A_CODE);
        return r;
    }

    @SuppressWarnings("unchecked")
    private void mockRegion(String province, String city, String area, String street) {
        TSysProvince p = new TSysProvince();
        p.setProvinceCode(P_CODE);
        p.setProvinceName("青坪省");
        TSysCity c = new TSysCity();
        c.setCityCode(C_CODE);
        c.setCityName("青坪市");
        c.setProvinceCode(P_CODE);
        TSysArea a = new TSysArea();
        a.setAreaCode(A_CODE);
        a.setAreaName("城东区");
        a.setCityCode(C_CODE);
        when(provinceService.selectTSysProvinceList(any(Wrapper.class)))
                .thenReturn(province == null ? Collections.emptyList() : new ArrayList<>(Arrays.asList(p)));
        when(cityService.selectTSysCityList(any(Wrapper.class)))
                .thenReturn(city == null ? Collections.emptyList() : new ArrayList<>(Arrays.asList(c)));
        when(areaService.selectTSysAreaList(any(Wrapper.class)))
                .thenReturn(area == null ? Collections.emptyList() : new ArrayList<>(Arrays.asList(a)));
        if (street != null) {
            TSysStreet s = new TSysStreet();
            s.setStreetCode(S_CODE);
            s.setStreetName("东湖街道");
            s.setAreaCode(A_CODE);
            when(streetService.selectTSysStreetList(any(Wrapper.class)))
                    .thenReturn(new ArrayList<>(Arrays.asList(s)));
        }
    }

    // ---------------- 建档：编号唯一 ----------------

    @Test
    void create_duplicateNoRejected() {
        TGasPipeArchive r = basePipe();
        when(archiveMapper.selectCount(any())).thenReturn(1);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.createArchive(r));
        assertTrue(ex.getMessage().contains("档案编号已存在"));
        verify(archiveMapper, never()).insert(any());
    }

    @Test
    void create_blankNoRejected() {
        TGasPipeArchive r = basePipe();
        r.setArchiveNo("  ");
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.createArchive(r));
        assertTrue(ex.getMessage().contains("档案编号不能为空"));
    }

    @Test
    void create_invalidTypeRejected() {
        TGasPipeArchive r = basePipe();
        r.setArchiveType(9);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.createArchive(r));
        assertTrue(ex.getMessage().contains("档案类别"));
    }

    // ---------------- 建档：坐标有效性 ----------------

    @Test
    void create_pipeMissingEndCoordRejected() {
        TGasPipeArchive r = basePipe();
        r.setEndLng(null);
        r.setEndLat(null);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.createArchive(r));
        assertTrue(ex.getMessage().contains("终点经纬度不能为空"));
    }

    @Test
    void create_coordOutOfRangeRejected() {
        TGasPipeArchive r = basePipe();
        r.setStartLng(new BigDecimal("181.2"));
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.createArchive(r));
        assertTrue(ex.getMessage().contains("超出有效范围"));
    }

    @Test
    void create_pipeSameStartEndRejected() {
        TGasPipeArchive r = basePipe();
        r.setEndLng(r.getStartLng());
        r.setEndLat(r.getStartLat());
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.createArchive(r));
        assertTrue(ex.getMessage().contains("不得完全重合"));
    }

    @Test
    void create_stationNeedsNoEndCoord() {
        mockRegion("p", "c", "a", null);
        when(archiveMapper.selectCount(any())).thenReturn(0);
        TGasPipeArchive s = new TGasPipeArchive();
        s.setArchiveNo("PA-ZJ-0001");
        s.setArchiveType(1);
        s.setArchiveName("东湖调压站");
        s.setStartLng(new BigDecimal("112.99"));
        s.setStartLat(new BigDecimal("28.24"));
        s.setAreaCode(A_CODE);
        service.createArchive(s);
        ArgumentCaptor<TGasPipeArchive> cap = ArgumentCaptor.forClass(TGasPipeArchive.class);
        verify(archiveMapper).insert(cap.capture());
        assertEquals(null, cap.getValue().getEndLng());
        assertEquals(Integer.valueOf(0), cap.getValue().getStatus());
    }

    @Test
    void create_invalidYearRejected() {
        TGasPipeArchive r = basePipe();
        r.setLayYear(3000);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.createArchive(r));
        assertTrue(ex.getMessage().contains("铺设年份"));
    }

    // ---------------- 建档：行政区划层级 ----------------

    @Test
    void create_areaCodeNotFoundRejected() {
        TGasPipeArchive r = basePipe();
        when(areaService.selectTSysAreaList(any(Wrapper.class))).thenReturn(Collections.emptyList());
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.createArchive(r));
        assertTrue(ex.getMessage().contains("所属区编码不存在"));
    }

    @Test
    void create_streetNotBelongsToAreaRejected() {
        mockRegion("p", "c", "a", null);
        TSysStreet foreign = new TSysStreet();
        foreign.setStreetCode(S_CODE);
        foreign.setAreaCode("990999");
        foreign.setStreetName("外来街道");
        when(streetService.selectTSysStreetList(any(Wrapper.class)))
                .thenReturn(new ArrayList<>(Arrays.asList(foreign)));
        TGasPipeArchive r = basePipe();
        r.setStreetCode(S_CODE);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.createArchive(r));
        assertTrue(ex.getMessage().contains("街道不属于所选区"));
    }

    @Test
    void create_successFillsRegionSnapshotAndDefaults() {
        mockRegion("p", "c", "a", "s");
        when(archiveMapper.selectCount(any())).thenReturn(0);
        TGasPipeArchive r = basePipe();
        r.setStreetCode(S_CODE);
        r.setSectorName("东湖滨水片区");
        service.createArchive(r);
        ArgumentCaptor<TGasPipeArchive> cap = ArgumentCaptor.forClass(TGasPipeArchive.class);
        verify(archiveMapper).insert(cap.capture());
        TGasPipeArchive saved = cap.getValue();
        assertEquals("青坪省—青坪市—城东区—东湖街道—东湖滨水片区", saved.getRegionName());
        assertEquals(P_CODE, saved.getProvinceCode());
        assertEquals(C_CODE, saved.getCityCode());
        assertEquals(A_CODE, saved.getAreaCode());
        assertEquals(S_CODE, saved.getStreetCode());
        assertEquals(Integer.valueOf(0), saved.getStatus());
        assertEquals(Integer.valueOf(0), saved.getDelFlag());
    }

    // ---------------- 编辑：身份冻结 / 终态冻结 ----------------

    @Test
    void update_notFoundRejected() {
        when(archiveMapper.selectOne(any())).thenReturn(null);
        TGasPipeArchive r = new TGasPipeArchive();
        r.setId(1L);
        assertThrows(IllegalArgumentException.class, () -> service.updateArchive(r));
    }

    @Test
    void update_archiveNoImmutable() {
        TGasPipeArchive db = basePipe();
        db.setId(1L);
        db.setStatus(0);
        when(archiveMapper.selectOne(any())).thenReturn(db);
        TGasPipeArchive r = new TGasPipeArchive();
        r.setId(1L);
        r.setArchiveNo("PA-GD-9999");
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.updateArchive(r));
        assertTrue(ex.getMessage().contains("编号不可变更"));
    }

    @Test
    void update_typeImmutable() {
        TGasPipeArchive db = basePipe();
        db.setId(1L);
        db.setStatus(0);
        when(archiveMapper.selectOne(any())).thenReturn(db);
        TGasPipeArchive r = new TGasPipeArchive();
        r.setId(1L);
        r.setArchiveType(1);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.updateArchive(r));
        assertTrue(ex.getMessage().contains("类别不可变更"));
    }

    @Test
    void update_scrappedFrozen() {
        TGasPipeArchive db = basePipe();
        db.setId(1L);
        db.setStatus(2);
        when(archiveMapper.selectOne(any())).thenReturn(db);
        TGasPipeArchive r = new TGasPipeArchive();
        r.setId(1L);
        r.setSectorName("新片区");
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.updateArchive(r));
        assertTrue(ex.getMessage().contains("已报废"));
    }

    // ---------------- 状态流转 ----------------

    @Test
    void status_inUseToScrapRejected() {
        TGasPipeArchive db = basePipe();
        db.setId(1L);
        db.setStatus(0);
        when(archiveMapper.selectOne(any())).thenReturn(db);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.changeStatus(1L, 2, "直接报废"));
        assertTrue(ex.getMessage().contains("须先停用"));
    }

    @Test
    void status_scrapTerminal() {
        TGasPipeArchive db = basePipe();
        db.setId(1L);
        db.setStatus(2);
        when(archiveMapper.selectOne(any())).thenReturn(db);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.changeStatus(1L, 0, "恢复"));
        assertTrue(ex.getMessage().contains("报废为终态"));
    }

    @Test
    void status_sameStatusRejected() {
        TGasPipeArchive db = basePipe();
        db.setId(1L);
        db.setStatus(0);
        when(archiveMapper.selectOne(any())).thenReturn(db);
        assertThrows(IllegalArgumentException.class, () -> service.changeStatus(1L, 0, null));
    }

    @Test
    void status_stopThenScrapFlowOk() {
        TGasPipeArchive db = basePipe();
        db.setId(1L);
        db.setStatus(0);
        when(archiveMapper.selectOne(any())).thenReturn(db);
        service.changeStatus(1L, 1, "老化检修");
        assertEquals(Integer.valueOf(1), db.getStatus());
        service.changeStatus(1L, 2, "评估后报废");
        assertEquals(Integer.valueOf(2), db.getStatus());
    }

    @Test
    void status_stopCanResume() {
        TGasPipeArchive db = basePipe();
        db.setId(1L);
        db.setStatus(1);
        when(archiveMapper.selectOne(any())).thenReturn(db);
        service.changeStatus(1L, 0, "检修完成恢复");
        assertEquals(Integer.valueOf(0), db.getStatus());
    }

    // ---------------- 区域汇总 ----------------

    @Test
    @SuppressWarnings("unchecked")
    void regionTree_groupsByHierarchy() {
        mockRegion("p", "c", "a", "s");
        TGasPipeArchive r1 = basePipe();
        r1.setStatus(0);
        r1.setProvinceCode(P_CODE);
        r1.setCityCode(C_CODE);
        r1.setAreaCode(A_CODE);
        r1.setStreetCode(S_CODE);
        TGasPipeArchive r2 = basePipe();
        r2.setArchiveNo("PA-GD-0002");
        r2.setStatus(1);
        r2.setProvinceCode(P_CODE);
        r2.setCityCode(C_CODE);
        r2.setAreaCode(A_CODE);
        r2.setStreetCode(S_CODE);
        TGasPipeArchive r3 = basePipe();
        r3.setArchiveNo("PA-GD-0003");
        r3.setStatus(2);
        r3.setProvinceCode(P_CODE);
        r3.setCityCode(C_CODE);
        r3.setAreaCode(A_CODE);
        r3.setStreetCode(null);
        when(archiveMapper.selectList(any(Wrapper.class))).thenReturn(Arrays.asList(r1, r2, r3));

        List<RegionStatNode> nodes = service.regionTree(0);
        RegionStatNode province = find(nodes, "P-" + P_CODE);
        RegionStatNode street = find(nodes, "S-" + S_CODE);
        RegionStatNode area = find(nodes, "A-" + A_CODE);
        assertEquals(3, province.getTotal());
        assertEquals(1, province.getInUse());
        assertEquals(1, province.getStopped());
        assertEquals(1, province.getScrapped());
        // 街道节点只含细化到街道的 2 条；无街道的那条挂区节点
        assertEquals(2, street.getTotal());
        assertEquals(1, street.getStopped());
        assertEquals(3, area.getTotal());
        assertEquals("青坪省", province.getName());
    }

    private static RegionStatNode find(List<RegionStatNode> nodes, String id) {
        return nodes.stream().filter(n -> n.getId().equals(id)).findFirst().orElse(null);
    }
}
