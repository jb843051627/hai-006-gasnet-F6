package com.fc.v2.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fc.v2.common.base.BaseController;
import com.fc.v2.common.domain.AjaxResult;
import com.fc.v2.common.domain.ResultTable;
import com.fc.v2.common.log.Log;
import com.fc.v2.model.auto.TGasZone;
import com.fc.v2.model.auto.TSysProvince;
import com.fc.v2.service.ITGasZoneService;
import com.fc.v2.service.ITSysProvinceService;
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

import java.util.List;

/**
 * 燃气档案行政区划 Controller：片区建档、省—市—区—街道—片区行政区划树、按区域汇总统计。
 *
 * @author fuce
 * @date 2026-09-29
 */
@Api(value = "燃气档案行政区划")
@Controller
@RequestMapping("/gasZone")
public class GasZoneController extends BaseController {

    private final String prefix = "admin/gasZone";

    @Autowired
    private ITGasZoneService gasZoneService;

    @Autowired
    private ITSysProvinceService sysProvinceService;

    /** 全量启用省（档案各页面五级联动的第一级数据源） */
    @ApiOperation(value = "全量省选项", notes = "全量启用省选项")
    @GetMapping("/allProvinces")
    @ResponseBody
    public List<TSysProvince> allProvinces() {
        return sysProvinceService.selectTSysProvinceList(
                new QueryWrapper<TSysProvince>().eq("data_state", 0).orderByAsc("sort"));
    }

    @ApiOperation(value = "片区列表跳转", notes = "片区列表跳转")
    @GetMapping("/view")
    @RequiresPermissions("gasZone:view")
    public String view(ModelMap model) {
        return prefix + "/list";
    }

    @Log(title = "档案片区查询", action = "list")
    @ApiOperation(value = "分页查询片区", notes = "分页查询片区")
    @GetMapping("/list")
    @RequiresPermissions("gasZone:list")
    @ResponseBody
    public ResultTable list(TGasZone record) {
        QueryWrapper<TGasZone> wrapper = new QueryWrapper<>();
        wrapper.eq("del_flag", 0);
        if (record != null) {
            if (record.getZoneCode() != null && !record.getZoneCode().isEmpty()) {
                wrapper.like("zone_code", record.getZoneCode());
            }
            if (record.getZoneName() != null && !record.getZoneName().isEmpty()) {
                wrapper.like("zone_name", record.getZoneName());
            }
            if (record.getStreetCode() != null && !record.getStreetCode().isEmpty()) {
                wrapper.eq("street_code", record.getStreetCode());
            }
            if (record.getAreaCode() != null && !record.getAreaCode().isEmpty()) {
                wrapper.eq("area_code", record.getAreaCode());
            }
            if (record.getStatus() != null) {
                wrapper.eq("status", record.getStatus());
            }
        }
        wrapper.orderByAsc("area_code").orderByAsc("street_code").orderByAsc("sort");
        startPage();
        com.github.pagehelper.PageInfo<TGasZone> page =
                new com.github.pagehelper.PageInfo<>(gasZoneService.selectZoneList(wrapper));
        return pageTable(page.getList(), page.getTotal());
    }

    @ApiOperation(value = "新增片区跳转", notes = "新增片区跳转")
    @GetMapping("/add")
    @RequiresPermissions("gasZone:add")
    public String add(ModelMap model) {
        return prefix + "/add";
    }

    @Log(title = "档案片区新建", action = "add")
    @ApiOperation(value = "新建片区", notes = "片区代号全库唯一，按街道回填区/市/省")
    @PostMapping("/add")
    @RequiresPermissions("gasZone:add")
    @ResponseBody
    public AjaxResult add(TGasZone record) {
        try {
            return toAjax(gasZoneService.insertZone(record));
        } catch (IllegalArgumentException e) {
            return error(e.getMessage());
        }
    }

    @ApiOperation(value = "编辑片区跳转", notes = "编辑片区跳转")
    @GetMapping("/edit/{id}")
    @RequiresPermissions("gasZone:edit")
    public String edit(@org.springframework.web.bind.annotation.PathVariable("id") Long id, ModelMap model) {
        model.put("zone", gasZoneService.selectZoneById(id));
        return prefix + "/edit";
    }

    @Log(title = "档案片区编辑", action = "edit")
    @ApiOperation(value = "编辑片区", notes = "名称/排序/状态/街道划转可改，代号不可改，档案不可删")
    @PostMapping("/edit")
    @RequiresPermissions("gasZone:edit")
    @ResponseBody
    public AjaxResult edit(TGasZone record) {
        try {
            return toAjax(gasZoneService.updateZone(record));
        } catch (IllegalArgumentException e) {
            return error(e.getMessage());
        }
    }

    @ApiOperation(value = "片区下拉选项", notes = "按街道取正常状态片区，供档案建档/划转选择")
    @GetMapping("/options")
    @ResponseBody
    public List<TGasZone> options(@RequestParam(value = "streetCode", required = false) String streetCode) {
        QueryWrapper<TGasZone> wrapper = new QueryWrapper<TGasZone>()
                .eq("del_flag", 0).eq("status", 0).orderByAsc("sort");
        if (streetCode != null && !streetCode.isEmpty()) {
            wrapper.eq("street_code", streetCode);
        }
        return gasZoneService.selectZoneList(wrapper);
    }

    @ApiOperation(value = "行政区划树跳转", notes = "省—市—区—街道—片区树")
    @GetMapping("/treeView")
    @RequiresPermissions("gasZone:view")
    public String treeView(ModelMap model) {
        return prefix + "/tree";
    }

    @Log(title = "行政区划树查询", action = "list")
    @ApiOperation(value = "行政区划树数据", notes = "省—市—区—街道—片区五级树")
    @GetMapping("/regionTree")
    @RequiresPermissions("gasZone:list")
    @ResponseBody
    public AjaxResult regionTree() {
        return retobject(200, gasZoneService.regionTree());
    }

    @ApiOperation(value = "区域统计跳转", notes = "按区域汇总档案数量与状态分布")
    @GetMapping("/statView")
    @RequiresPermissions("gasZone:stat")
    public String statView(ModelMap model) {
        return prefix + "/stat";
    }

    @Log(title = "区域档案汇总统计", action = "list")
    @ApiOperation(value = "区域汇总统计", notes = "省/市/区/街道/片区五级档案数量与在用/停用/报废状态分布")
    @GetMapping("/regionStats")
    @RequiresPermissions("gasZone:stat")
    @ResponseBody
    public AjaxResult regionStats(@RequestParam(value = "areaCode", required = false) String areaCode,
                                  @RequestParam(value = "streetCode", required = false) String streetCode,
                                  @RequestParam(value = "zoneName", required = false) String zoneName) {
        return retobject(200, gasZoneService.regionStats(areaCode, streetCode, zoneName));
    }
}
