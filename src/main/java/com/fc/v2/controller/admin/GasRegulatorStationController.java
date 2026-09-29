package com.fc.v2.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fc.v2.common.base.BaseController;
import com.fc.v2.common.domain.AjaxResult;
import com.fc.v2.common.domain.ResultTable;
import com.fc.v2.common.log.Log;
import com.fc.v2.model.auto.TGasRegulatorStationArch;
import com.fc.v2.model.auto.TGasStationMaintRecord;
import com.fc.v2.model.auto.TGasStationPipe;
import com.fc.v2.service.ITGasRegulatorStationService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * 燃气调压站基础档案 Controller
 * 除档案本体 CRUD（无删除）外，另含：管辖管段范围挂接、设备维护履历（只增不改）。
 *
 * @author fuce
 * @date 2026-09-29
 */
@Api(value = "燃气调压站基础档案")
@Controller
@RequestMapping("/gasStation")
public class GasRegulatorStationController extends BaseController {

    private final String prefix = "admin/gasStation";

    @Autowired
    private ITGasRegulatorStationService gasStationService;

    @ApiOperation(value = "档案台账跳转", notes = "档案台账跳转")
    @GetMapping("/view")
    @RequiresPermissions("gasStation:view")
    public String view(ModelMap model) {
        return prefix + "/list";
    }

    @Log(title = "燃气调压站档案查询", action = "list")
    @ApiOperation(value = "分页查询", notes = "停用/报废档案默认一并展示，可用状态下拉过滤")
    @GetMapping("/list")
    @RequiresPermissions("gasStation:list")
    @ResponseBody
    public ResultTable list(TGasRegulatorStationArch record) {
        QueryWrapper<TGasRegulatorStationArch> wrapper = new QueryWrapper<>();
        wrapper.eq("del_flag", 0);
        if (record != null) {
            if (record.getStationNo() != null && !record.getStationNo().isEmpty()) {
                wrapper.like("station_no", record.getStationNo());
            }
            if (record.getStationName() != null && !record.getStationName().isEmpty()) {
                wrapper.like("station_name", record.getStationName());
            }
            if (record.getDeviceModel() != null && !record.getDeviceModel().isEmpty()) {
                wrapper.like("device_model", record.getDeviceModel());
            }
            if (record.getStatus() != null) {
                wrapper.eq("status", record.getStatus());
            }
            if (record.getAreaCode() != null && !record.getAreaCode().isEmpty()) {
                wrapper.eq("area_code", record.getAreaCode());
            }
            if (record.getStreetCode() != null && !record.getStreetCode().isEmpty()) {
                wrapper.eq("street_code", record.getStreetCode());
            }
            if (record.getZoneId() != null) {
                wrapper.eq("zone_id", record.getZoneId());
            }
        }
        wrapper.orderByDesc("create_time");
        startPage();
        com.github.pagehelper.PageInfo<TGasRegulatorStationArch> page =
                new com.github.pagehelper.PageInfo<>(gasStationService.selectList(wrapper));
        return pageTable(page.getList(), page.getTotal());
    }

    @ApiOperation(value = "新增跳转", notes = "新增跳转")
    @GetMapping("/add")
    @RequiresPermissions("gasStation:add")
    public String add(ModelMap model) {
        return prefix + "/add";
    }

    @Log(title = "燃气调压站档案新建", action = "add")
    @ApiOperation(value = "新建档案", notes = "校验编号唯一性与站址坐标有效性")
    @PostMapping("/add")
    @RequiresPermissions("gasStation:add")
    @ResponseBody
    public AjaxResult add(TGasRegulatorStationArch record) {
        try {
            return toAjax(gasStationService.insert(record));
        } catch (IllegalArgumentException e) {
            return error(e.getMessage());
        }
    }

    @ApiOperation(value = "编辑跳转", notes = "编辑跳转")
    @GetMapping("/edit/{id}")
    @RequiresPermissions("gasStation:edit")
    public String edit(@PathVariable("id") Long id, ModelMap model) {
        model.put("station", gasStationService.selectById(id));
        return prefix + "/edit";
    }

    @Log(title = "燃气调压站档案编辑", action = "edit")
    @ApiOperation(value = "编辑保存", notes = "允许状态调整、片区划转、设备型号等属性变更；编号不可改、档案不可删")
    @PostMapping("/edit")
    @RequiresPermissions("gasStation:edit")
    @ResponseBody
    public AjaxResult edit(TGasRegulatorStationArch record) {
        try {
            return toAjax(gasStationService.update(record));
        } catch (IllegalArgumentException e) {
            return error(e.getMessage());
        }
    }

    @ApiOperation(value = "档案详情跳转", notes = "含管辖管段范围与设备维护履历")
    @GetMapping("/detail/{id}")
    @RequiresPermissions("gasStation:list")
    public String detail(@PathVariable("id") Long id, ModelMap model) {
        model.put("station", gasStationService.selectById(id));
        return prefix + "/detail";
    }

    @Log(title = "调压站管辖管段查询", action = "list")
    @ApiOperation(value = "管辖管段列表", notes = "管辖管段列表")
    @GetMapping("/pipes/{stationId}")
    @RequiresPermissions("gasStation:list")
    @ResponseBody
    public AjaxResult pipes(@PathVariable("stationId") Long stationId) {
        return retobject(200, gasStationService.listStationPipes(stationId));
    }

    @Log(title = "调压站管辖管段挂接", action = "edit")
    @ApiOperation(value = "保存管辖管段范围", notes = "覆盖式保存；仅解除/建立管辖关系，不删除管道档案")
    @PostMapping("/savePipes")
    @RequiresPermissions("gasStation:edit")
    @ResponseBody
    public AjaxResult savePipes(@RequestParam("stationId") Long stationId,
                                @RequestBody(required = false) List<TGasStationPipe> pipes) {
        try {
            gasStationService.saveStationPipes(stationId, pipes);
            return success();
        } catch (IllegalArgumentException e) {
            return error(e.getMessage());
        }
    }

    @Log(title = "调压站维护记录查询", action = "list")
    @ApiOperation(value = "维护履历列表", notes = "按维护时间倒序")
    @GetMapping("/maintRecords/{stationId}")
    @RequiresPermissions("gasStation:list")
    @ResponseBody
    public AjaxResult maintRecords(@PathVariable("stationId") Long stationId) {
        return retobject(200, gasStationService.listMaintRecords(stationId));
    }

    @Log(title = "调压站维护记录登记", action = "add")
    @ApiOperation(value = "登记维护记录", notes = "履历只增不改，自动快照站点编号与设备型号")
    @PostMapping("/addMaint")
    @RequiresPermissions("gasStation:maint")
    @ResponseBody
    public AjaxResult addMaint(TGasStationMaintRecord record) {
        try {
            return toAjax(gasStationService.addMaintRecord(record));
        } catch (IllegalArgumentException e) {
            return error(e.getMessage());
        }
    }
}
