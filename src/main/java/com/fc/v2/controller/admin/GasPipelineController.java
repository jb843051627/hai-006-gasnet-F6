package com.fc.v2.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fc.v2.common.base.BaseController;
import com.fc.v2.common.domain.AjaxResult;
import com.fc.v2.common.domain.ResultTable;
import com.fc.v2.common.log.Log;
import com.fc.v2.model.auto.TGasPipelineArch;
import com.fc.v2.model.auto.TGasRegulatorStationArch;
import com.fc.v2.service.ITGasPipelineService;
import com.fc.v2.service.ITGasRegulatorStationService;
import com.fc.v2.util.GasArchiveLedgerExcelUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
import java.util.List;

/**
 * 燃气管道基础档案 Controller
 * 档案铁律：有建档/编辑，没有任何删除接口；停用/报废仅改状态位，列表可过滤、台账可选导出。
 *
 * @author fuce
 * @date 2026-09-29
 */
@Api(value = "燃气管道基础档案")
@Controller
@RequestMapping("/gasPipeline")
public class GasPipelineController extends BaseController {

    private final String prefix = "admin/gasPipeline";

    @Autowired
    private ITGasPipelineService gasPipelineService;

    @Autowired
    private ITGasRegulatorStationService gasStationService;

    @ApiOperation(value = "档案台账跳转", notes = "档案台账跳转")
    @GetMapping("/view")
    @RequiresPermissions("gasPipeline:view")
    public String view(ModelMap model) {
        return prefix + "/list";
    }

    @Log(title = "燃气管道档案查询", action = "list")
    @ApiOperation(value = "分页查询", notes = "停用/报废档案默认一并展示，可用状态下拉过滤")
    @GetMapping("/list")
    @RequiresPermissions("gasPipeline:list")
    @ResponseBody
    public ResultTable list(TGasPipelineArch record) {
        QueryWrapper<TGasPipelineArch> wrapper = buildWrapper(record);
        wrapper.orderByDesc("create_time");
        startPage();
        com.github.pagehelper.PageInfo<TGasPipelineArch> page =
                new com.github.pagehelper.PageInfo<>(gasPipelineService.selectList(wrapper));
        return pageTable(page.getList(), page.getTotal());
    }

    @ApiOperation(value = "新增跳转", notes = "新增跳转")
    @GetMapping("/add")
    @RequiresPermissions("gasPipeline:add")
    public String add(ModelMap model) {
        return prefix + "/add";
    }

    @ApiOperation(value = "全部管道选项", notes = "供调压站管辖管段挂接选择（含停用/报废，管辖关系不因管段停用而丢失）")
    @GetMapping("/allOptions")
    @RequiresPermissions("gasStation:edit")
    @ResponseBody
    public List<TGasPipelineArch> allOptions(@RequestParam(value = "keyword", required = false) String keyword) {
        QueryWrapper<TGasPipelineArch> wrapper = new QueryWrapper<TGasPipelineArch>()
                .eq("del_flag", 0).orderByAsc("pipe_no");
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like("pipe_no", keyword).or().like("pipe_name", keyword));
        }
        return gasPipelineService.selectList(wrapper);
    }

    @Log(title = "燃气管道档案新建", action = "add")
    @ApiOperation(value = "新建档案", notes = "校验编号唯一性与空间坐标有效性")
    @PostMapping("/add")
    @RequiresPermissions("gasPipeline:add")
    @ResponseBody
    public AjaxResult add(TGasPipelineArch record) {
        try {
            return toAjax(gasPipelineService.insert(record));
        } catch (IllegalArgumentException e) {
            return error(e.getMessage());
        }
    }

    @ApiOperation(value = "编辑跳转", notes = "编辑跳转")
    @GetMapping("/edit/{id}")
    @RequiresPermissions("gasPipeline:edit")
    public String edit(@PathVariable("id") Long id, ModelMap model) {
        model.put("pipeline", gasPipelineService.selectById(id));
        return prefix + "/edit";
    }

    @Log(title = "燃气管道档案编辑", action = "edit")
    @ApiOperation(value = "编辑保存", notes = "允许状态调整、片区划转等属性变更；编号不可改、档案不可删")
    @PostMapping("/edit")
    @RequiresPermissions("gasPipeline:edit")
    @ResponseBody
    public AjaxResult edit(TGasPipelineArch record) {
        try {
            return toAjax(gasPipelineService.update(record));
        } catch (IllegalArgumentException e) {
            return error(e.getMessage());
        }
    }

    @Log(title = "燃气管道/调压站档案台账导出", action = "export")
    @ApiOperation(value = "台账导出", notes = "导出管道+调压站两个Sheet；includeHistory=1 含停用/报废历史档案")
    @GetMapping("/exportLedger")
    @RequiresPermissions("gasPipeline:export")
    public void exportLedger(@RequestParam(value = "includeHistory", defaultValue = "1") Integer includeHistory,
                             @RequestParam(value = "areaCode", required = false) String areaCode,
                             @RequestParam(value = "streetCode", required = false) String streetCode,
                             @RequestParam(value = "zoneId", required = false) Long zoneId,
                             HttpServletResponse response) throws Exception {
        boolean withHistory = includeHistory == null || includeHistory == 1;
        QueryWrapper<TGasPipelineArch> pWrapper = new QueryWrapper<>();
        applyRegionFilter(pWrapper, areaCode, streetCode, zoneId);
        QueryWrapper<TGasRegulatorStationArch> sWrapper = new QueryWrapper<>();
        applyStationRegionFilter(sWrapper, areaCode, streetCode, zoneId);

        List<TGasPipelineArch> pipes = gasPipelineService.selectForExport(pWrapper, withHistory);
        List<TGasRegulatorStationArch> stations = gasStationService.selectForExport(sWrapper, withHistory);

        String filename = "燃气管道调压站档案台账_" + (withHistory ? "含历史档案" : "仅在用") + ".xlsx";
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        response.setHeader("Content-Disposition", "attachment;filename*=utf-8''" + URLEncoder.encode(filename, "UTF-8"));
        GasArchiveLedgerExcelUtil.write(pipes, stations, response.getOutputStream());
    }

    /** 列表过滤条件（编号/名称/材质模糊，状态/区域精确；不过滤状态即含停用/报废全部） */
    static QueryWrapper<TGasPipelineArch> buildWrapper(TGasPipelineArch record) {
        QueryWrapper<TGasPipelineArch> wrapper = new QueryWrapper<>();
        wrapper.eq("del_flag", 0);
        if (record != null) {
            if (record.getPipeNo() != null && !record.getPipeNo().isEmpty()) {
                wrapper.like("pipe_no", record.getPipeNo());
            }
            if (record.getPipeName() != null && !record.getPipeName().isEmpty()) {
                wrapper.like("pipe_name", record.getPipeName());
            }
            if (record.getMaterial() != null && !record.getMaterial().isEmpty()) {
                wrapper.like("material", record.getMaterial());
            }
            if (record.getStatus() != null) {
                wrapper.eq("status", record.getStatus());
            }
            applyRegionFilter(wrapper, record.getAreaCode(), record.getStreetCode(), record.getZoneId());
        }
        return wrapper;
    }

    static void applyRegionFilter(QueryWrapper<TGasPipelineArch> wrapper, String areaCode,
                                  String streetCode, Long zoneId) {
        if (areaCode != null && !areaCode.isEmpty()) {
            wrapper.eq("area_code", areaCode);
        }
        if (streetCode != null && !streetCode.isEmpty()) {
            wrapper.eq("street_code", streetCode);
        }
        if (zoneId != null) {
            wrapper.eq("zone_id", zoneId);
        }
    }

    static void applyStationRegionFilter(QueryWrapper<TGasRegulatorStationArch> wrapper, String areaCode,
                                         String streetCode, Long zoneId) {
        if (areaCode != null && !areaCode.isEmpty()) {
            wrapper.eq("area_code", areaCode);
        }
        if (streetCode != null && !streetCode.isEmpty()) {
            wrapper.eq("street_code", streetCode);
        }
        if (zoneId != null) {
            wrapper.eq("zone_id", zoneId);
        }
    }
}
