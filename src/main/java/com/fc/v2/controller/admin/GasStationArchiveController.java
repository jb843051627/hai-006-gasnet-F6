package com.fc.v2.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fc.v2.common.base.BaseController;
import com.fc.v2.common.domain.AjaxResult;
import com.fc.v2.common.domain.ResuTree;
import com.fc.v2.common.domain.ResultTable;
import com.fc.v2.common.log.Log;
import com.fc.v2.model.auto.TGasPipeArchive;
import com.fc.v2.model.auto.TGasStationMaint;
import com.fc.v2.model.auto.TSysProvince;
import com.fc.v2.model.custom.RegionStatNode;
import com.fc.v2.service.ITGasPipeArchiveService;
import com.fc.v2.service.ITGasStationExtraService;
import com.fc.v2.service.ITSysProvinceService;
import com.fc.v2.util.GasArchiveExcelUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 燃气调压站基础档案 Controller。
 *
 * <p>除管道档案同口径的建档/编辑/状态/统计/导出外，另含：
 * 设备型号维护、管辖管段范围配置、维护记录只增登记、档案详情聚合。
 *
 * @author fuce
 * @date 2026-09-29
 */
@Api(value = "燃气调压站基础档案")
@Controller
@RequestMapping("/gasStationArchive")
public class GasStationArchiveController extends BaseController {

    private final String prefix = "admin/gasArchive";

    @Autowired
    private ITGasPipeArchiveService archiveService;
    @Autowired
    private ITGasStationExtraService stationExtraService;
    @Autowired
    private ITSysProvinceService provinceService;

    @ApiOperation(value = "调压站档案台账跳转", notes = "调压站档案台账跳转")
    @GetMapping("/view")
    @RequiresPermissions("gasArchive:station:view")
    public String view(ModelMap model) {
        model.put("archiveKind", "station");
        model.put("provinceList", provinceService.selectTSysProvinceList(new QueryWrapper<TSysProvince>()));
        return prefix + "/list";
    }

    @Log(title = "燃气调压站档案查询", action = "list")
    @ApiOperation(value = "调压站档案分页查询", notes = "支持编号/名称/状态/区划过滤")
    @GetMapping("/list")
    @RequiresPermissions("gasArchive:station:list")
    @ResponseBody
    public ResultTable list(TGasPipeArchive query, Integer includeHistory) {
        QueryWrapper<TGasPipeArchive> qw =
                GasPipeArchiveController.buildQuery(query, includeHistory, ITGasPipeArchiveService.TYPE_STATION);
        startPage();
        com.github.pagehelper.PageInfo<TGasPipeArchive> page =
                new com.github.pagehelper.PageInfo<TGasPipeArchive>(archiveService.selectList(qw));
        return pageTable(page.getList(), page.getTotal());
    }

    @ApiOperation(value = "新建调压站档案跳转", notes = "新建调压站档案跳转")
    @GetMapping("/add")
    @RequiresPermissions("gasArchive:station:add")
    public String add(ModelMap model) {
        model.put("formAction", "add");
        model.put("archiveKind", "station");
        model.put("provinceList", provinceService.selectTSysProvinceList(new QueryWrapper<TSysProvince>()));
        return prefix + "/add";
    }

    @Log(title = "燃气调压站档案建档", action = "add")
    @ApiOperation(value = "调压站档案建档", notes = "校验编号唯一与站点坐标有效")
    @PostMapping("/add")
    @RequiresPermissions("gasArchive:station:add")
    @ResponseBody
    public AjaxResult add(TGasPipeArchive record, String deviceModel) {
        record.setArchiveType(ITGasPipeArchiveService.TYPE_STATION);
        try {
            int rows = archiveService.createArchive(record);
            if (rows > 0 && deviceModel != null && !deviceModel.trim().isEmpty()) {
                stationExtraService.saveDeviceModel(record.getId(), deviceModel, null);
            }
            return toAjax(rows);
        } catch (IllegalArgumentException e) {
            return error(e.getMessage());
        }
    }

    @ApiOperation(value = "编辑调压站档案跳转", notes = "编辑调压站档案跳转（含设备型号）")
    @GetMapping("/edit")
    @RequiresPermissions("gasArchive:station:edit")
    public String edit(Long id, ModelMap model) {
        model.put("archive", archiveService.selectById(id));
        model.put("deviceModel", stationExtraService.getDeviceModel(id));
        model.put("formAction", "edit");
        model.put("archiveKind", "station");
        model.put("provinceList", provinceService.selectTSysProvinceList(new QueryWrapper<TSysProvince>()));
        return prefix + "/edit";
    }

    @Log(title = "燃气调压站档案编辑", action = "edit")
    @ApiOperation(value = "调压站档案属性变更", notes = "属性/片区划转/设备型号；编号类别不可变，状态走状态接口")
    @PostMapping("/edit")
    @RequiresPermissions("gasArchive:station:edit")
    @ResponseBody
    public AjaxResult edit(TGasPipeArchive record, String deviceModel) {
        record.setArchiveType(ITGasPipeArchiveService.TYPE_STATION);
        try {
            int rows = archiveService.updateArchive(record);
            if (rows > 0) {
                // 设备型号允许在编辑表单里一并修改（空串表示清空）
                stationExtraService.saveDeviceModel(record.getId(), deviceModel, null);
            }
            return toAjax(rows);
        } catch (IllegalArgumentException e) {
            return error(e.getMessage());
        }
    }

    @Log(title = "燃气调压站档案状态调整", action = "changeStatus")
    @ApiOperation(value = "调压站档案状态调整", notes = "在用→停用→报废；停用可恢复，报废终态；无删除")
    @PostMapping("/changeStatus")
    @RequiresPermissions("gasArchive:station:changeStatus")
    @ResponseBody
    public AjaxResult changeStatus(Long id, Integer status, String reason) {
        try {
            return toAjax(archiveService.changeStatus(id, status, reason));
        } catch (IllegalArgumentException e) {
            return error(e.getMessage());
        }
    }

    @ApiOperation(value = "调压站档案详情跳转", notes = "基础档案+设备型号+管辖管段+维护记录")
    @GetMapping("/detail")
    @RequiresPermissions("gasArchive:station:detail")
    public String detail(Long id, ModelMap model) {
        List<TGasPipeArchive> managedPipes = stationExtraService.listManagedPipes(id);
        model.put("archive", archiveService.selectById(id));
        model.put("deviceModel", stationExtraService.getDeviceModel(id));
        model.put("managedPipes", managedPipes);
        // 内联到页面 JS 时按字符串给出，避免雪花 id 超出 JS 安全整数范围丢精度
        model.put("managedPipeIds", managedPipes.stream()
                .map(p -> String.valueOf(p.getId())).collect(java.util.stream.Collectors.toList()));
        model.put("maintRecords", stationExtraService.listMaintRecords(id));
        return prefix + "/detail";
    }

    @Log(title = "燃气调压站管辖管段配置", action = "editPipes")
    @ApiOperation(value = "保存管辖管段范围", notes = "全量覆盖调压站管辖的管道档案id集合")
    @PostMapping("/savePipes")
    @RequiresPermissions("gasArchive:station:editPipes")
    @ResponseBody
    public AjaxResult savePipes(Long stationId, @RequestParam(value = "pipeIds", required = false) List<Long> pipeIds) {
        try {
            int count = stationExtraService.replaceManagedPipes(stationId, pipeIds);
            return success(200, "管辖管段已保存，共 " + count + " 段", count);
        } catch (IllegalArgumentException e) {
            return error(e.getMessage());
        }
    }

    @ApiOperation(value = "全部管道档案选项", notes = "管辖管段配置用：全部在档管道（含停用），返回编号/名称/状态")
    @GetMapping("/pipePicker")
    @RequiresPermissions("gasArchive:station:editPipes")
    @ResponseBody
    public List<TGasPipeArchive> pipePicker() {
        return archiveService.selectList(new QueryWrapper<TGasPipeArchive>()
                .eq("del_flag", 0)
                .eq("archive_type", ITGasPipeArchiveService.TYPE_PIPE)
                .orderByAsc("archive_no"));
    }

    @Log(title = "燃气调压站维护记录登记", action = "addMaint")
    @ApiOperation(value = "追加维护记录", notes = "维护记录只增不改")
    @PostMapping("/addMaint")
    @RequiresPermissions("gasArchive:station:maint")
    @ResponseBody
    public AjaxResult addMaint(TGasStationMaint record) {
        try {
            return toAjax(stationExtraService.addMaintRecord(record));
        } catch (IllegalArgumentException e) {
            return error(e.getMessage());
        }
    }

    @Log(title = "燃气调压站档案区域统计", action = "regionStats")
    @ApiOperation(value = "行政区划统计树", notes = "按省市区街道汇总档案数量与状态分布")
    @GetMapping("/regionStats")
    @RequiresPermissions("gasArchive:station:stats")
    @ResponseBody
    public ResuTree regionStats() {
        List<RegionStatNode> tree = archiveService.regionTree(ITGasPipeArchiveService.TYPE_STATION);
        return dataTree(tree);
    }

    @Log(title = "燃气调压站台账导出", action = "export")
    @ApiOperation(value = "调压站台账导出", notes = "includeHistory=1 时含停用/报废历史档案")
    @GetMapping("/export")
    @RequiresPermissions("gasArchive:station:export")
    public void export(TGasPipeArchive query,
                       @RequestParam(value = "includeHistory", defaultValue = "1") Integer includeHistory,
                       HttpServletResponse response) throws Exception {
        QueryWrapper<TGasPipeArchive> qw =
                GasPipeArchiveController.buildQuery(query, includeHistory, ITGasPipeArchiveService.TYPE_STATION);
        List<TGasPipeArchive> stations = archiveService.selectList(qw);
        GasPipeArchiveController.setExcelResponse(response, "燃气调压站档案台账.xlsx");
        GasArchiveExcelUtil.writeLedger(null, stations, response.getOutputStream());
    }
}
