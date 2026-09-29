package com.fc.v2.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Date;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fc.v2.mapper.auto.TGasPipeArchiveMapper;
import com.fc.v2.mapper.auto.TGasRegulatorStationMapper;
import com.fc.v2.mapper.auto.TGasStationMaintMapper;
import com.fc.v2.mapper.auto.TGasStationPipeMapper;
import com.fc.v2.model.auto.TGasPipeArchive;
import com.fc.v2.model.auto.TGasStationMaint;

/**
 * 调压站附属信息：管辖管段校验、维护记录只增的单元测试。
 */
@ExtendWith(MockitoExtension.class)
class TGasStationExtraServiceImplTest {

    @Mock
    private TGasRegulatorStationMapper stationMapper;
    @Mock
    private TGasStationPipeMapper stationPipeMapper;
    @Mock
    private TGasStationMaintMapper maintMapper;
    @Mock
    private TGasPipeArchiveMapper archiveMapper;

    @InjectMocks
    private TGasStationExtraServiceImpl service;

    private static TGasPipeArchive archive(long id, int type, int delFlag) {
        TGasPipeArchive a = new TGasPipeArchive();
        a.setId(id);
        a.setArchiveType(type);
        a.setDelFlag(delFlag);
        return a;
    }

    @Test
    void replacePipes_stationMissingRejected() {
        when(archiveMapper.selectOne(any())).thenReturn(null);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.replaceManagedPipes(9L, Arrays.asList(1L)));
        assertTrue(ex.getMessage().contains("调压站档案不存在"));
    }

    @Test
    void replacePipes_selfReferenceRejected() {
        when(archiveMapper.selectOne(any())).thenReturn(archive(9L, 1, 0));
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.replaceManagedPipes(9L, Arrays.asList(9L)));
        assertTrue(ex.getMessage().contains("不能把自己关联"));
        verify(stationPipeMapper, never()).delete(any());
    }

    @Test
    void replacePipes_stationTypePipeRejected() {
        when(archiveMapper.selectOne(any())).thenReturn(archive(9L, 1, 0));
        when(archiveMapper.selectById(8L)).thenReturn(archive(8L, 1, 0));
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.replaceManagedPipes(9L, Arrays.asList(8L)));
        assertTrue(ex.getMessage().contains("仅可关联管道"));
    }

    @Test
    void replacePipes_pipeMissingRejected() {
        when(archiveMapper.selectOne(any())).thenReturn(archive(9L, 1, 0));
        when(archiveMapper.selectById(8L)).thenReturn(null);
        assertThrows(IllegalArgumentException.class,
                () -> service.replaceManagedPipes(9L, Arrays.asList(8L)));
    }

    @Test
    void replacePipes_validThenFullReplace() {
        when(archiveMapper.selectOne(any())).thenReturn(archive(9L, 1, 0));
        when(archiveMapper.selectById(8L)).thenReturn(archive(8L, 0, 0));
        int count = service.replaceManagedPipes(9L, Arrays.asList(8L, 8L));
        // 重复 id 去重
        assertEquals(1, count);
        // 先全量删除再插入
        verify(stationPipeMapper).delete(any());
        verify(stationPipeMapper).insert(any());
    }

    @Test
    void addMaint_requiresTypeAndContent() {
        when(archiveMapper.selectOne(any())).thenReturn(archive(9L, 1, 0));
        TGasStationMaint m = new TGasStationMaint();
        m.setStationId(9L);
        m.setMaintType("  ");
        m.setContent("内容");
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.addMaintRecord(m));
        assertTrue(ex.getMessage().contains("维护类别不能为空"));
    }

    @Test
    void addMaint_okDefaultsTime() {
        when(archiveMapper.selectOne(any())).thenReturn(archive(9L, 1, 0));
        TGasStationMaint m = new TGasStationMaint();
        m.setStationId(9L);
        m.setMaintType("日常维护");
        m.setContent("滤芯清污");
        service.addMaintRecord(m);
        org.junit.jupiter.api.Assertions.assertNotNull(m.getMaintTime());
        verify(maintMapper).insert(m);
        // 只增：没有任何更新/删除调用
        verify(maintMapper, never()).updateById(any());
    }

    @Test
    void addMaint_rejectsWhenTargetNotStation() {
        when(archiveMapper.selectOne(any())).thenReturn(archive(9L, 0, 0));
        TGasStationMaint m = new TGasStationMaint();
        m.setStationId(9L);
        m.setMaintTime(new Date());
        m.setMaintType("抢修");
        m.setContent("x");
        assertThrows(IllegalArgumentException.class, () -> service.addMaintRecord(m));
    }
}
