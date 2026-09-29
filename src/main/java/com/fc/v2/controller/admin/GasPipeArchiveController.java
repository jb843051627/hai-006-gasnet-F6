package com.fc.v2.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fc.v2.common.base.BaseController;
import com.fc.v2.common.domain.AjaxResult;
import com.fc.v2.common.domain.ResultTable;
import com.fc.v2.common.domain.ResuTree;
import com.fc.v2.common.log.Log;
import com.fc.v2.model.auto.TGasPipeArchive;
import com.fc.v2.model.auto.TSysProvince;
import com.fc.v2.model.custom.RegionStatNode;
import com.fc.v2.service.ITGasPipeArchiveService;
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
import java.net.URLEncoder;
import java.util.List;

/**
 * 燃气管道基础档案 Controller。
 *
 * <p>建档校验编号唯一/坐标有效；编辑只改属性；状态走 changeStatus；全模块无删除接口。
 *
 * @author fuce
 * @date 2026-09-29
 */
@Api(value = "燃气管道基础档案")
@Controller
@RequestMapping("/gasPipeArchive")
public class GasPipeArchiveController extends BaseController {

    private final String prefix = "admin/gasArchive";

    @Autowired
    private ITGasPipeArchiveService archiveService;

    @Autowired
    private ITSysProvinceService provinceService;

    @ApiOperation(value = "管道档案台账跳转", notes = "管道档案台账跳转")
    @GetMapping("/view")
    @RequiresPermissions("gasArchive:pipe:view")
    public String view(ModelMap model) {
        model.put("archiveKind", "pipe");
        model.put("provinceList", provinceService.selectTSysProvinceList(new QueryWrapper<TSysProvince>()));
        return prefix + "/list";
    }

    @Log(title = "燃气管道档案查询", action = "list")
    @ApiOperation(value = "管道档案分页查询", notes = "支持编号/名称/状态/区划过滤")
    @GetMapping("/list")
    @RequiresPermissions("gasArchive:pipe:list")
    @ResponseBody
    public ResultTable list(TGasPipeArchive query, Integer includeHistory) {
        QueryWrapper<TGasPipeArchive> qw = buildQuery(query, includeHistory, ITGasPipeArchiveService.TYPE_PIPE);
        startPage();
        com.github.pagehelper.PageInfo<TGasPipeArchive> page =
                new com.github.pagehelper.PageInfo<TGasPipeArchive>(archiveService.selectList(qw));
        return pageTable(page.getList(), page.getTotal());
    }

    @ApiOperation(value = "新建管道档案跳转", notes = "新建管道档案跳转")
    @GetMapping("/add")
    @RequiresPermissions("gasArchive:pipe:add")
    public String add(ModelMap model) {
        model.put("formAction", "add");
        model.put("archiveKind", "pipe");
        model.put("provinceList", provinceService.selectTSysProvinceList(new QueryWrapper<TSysProvince>()));
        return prefix + "/add";
    }

    @Log(title = "燃气管道档案建档", action = "add")
    @ApiOperation(value = "管道档案建档", notes = "校验编号唯一与坐标有效性")
    @PostMapping("/add")
    @RequiresPermissions("gasArchive:pipe:add")
    @ResponseBody
    public AjaxResult add(TGasPipeArchive record) {
        record.setArchiveType(ITGasPipeArchiveService.TYPE_PIPE);
        try {
            return toAjax(archiveService.createArchive(record));
        } catch (IllegalArgumentException e) {
            return error(e.getMessage());
        }
    }

    @ApiOperation(value = "编辑管道档案跳转", notes = "编辑管道档案跳转")
    @GetMapping("/edit")
    @RequiresPermissions("gasArchive:pipe:edit")
    public String edit(Long id, ModelMap model) {
        model.put("archive", archiveService.selectById(id));
        model.put("formAction", "edit");
        model.put("archiveKind", "pipe");
        model.put("provinceList", provinceService.selectTSysProvinceList(new QueryWrapper<TSysProvince>()));
        return prefix + "/edit";
    }

    @Log(title = "燃气管道档案编辑", action = "edit")
    @ApiOperation(value = "管道档案属性变更", notes = "允许规格材质/坐标/片区划转等；编号类别不可变，状态走状态接口")
    @PostMapping("/edit")
    @RequiresPermissions("gasArchive:pipe:edit")
    @ResponseBody
    public AjaxResult edit(TGasPipeArchive record) {
        record.setArchiveType(ITGasPipeArchiveService.TYPE_PIPE);
        try {
            return toAjax(archiveService.updateArchive(record));
        } catch (IllegalArgumentException e) {
            return error(e.getMessage());
        }
    }

    @Log(title = "燃气管道档案状态调整", action = "changeStatus")
    @ApiOperation(value = "管道档案状态调整", notes = "在用→停用→报废；停用可恢复，报废终态；无删除")
    @PostMapping("/changeStatus")
    @RequiresPermissions("gasArchive:pipe:changeStatus")
    @ResponseBody
    public AjaxResult changeStatus(Long id, Integer status, String reason) {
        try {
            return toAjax(archiveService.changeStatus(id, status, reason));
        } catch (IllegalArgumentException e) {
            return error(e.getMessage());
        }
    }

    @Log(title = "燃气管道档案区域统计", action = "regionStats")
    @ApiOperation(value = "行政区划统计树", notes = "按省市区街道汇总档案数量与状态分布")
    @GetMapping("/regionStats")
    @RequiresPermissions("gasArchive:pipe:stats")
    @ResponseBody
    public ResuTree regionStats() {
        List<RegionStatNode> tree = archiveService.regionTree(ITGasPipeArchiveService.TYPE_PIPE);
        return dataTree(tree);
    }

    @Log(title = "燃气管道台账导出", action = "export")
    @ApiOperation(value = "管道台账导出", notes = "includeHistory=1 时含停用/报废历史档案")
    @GetMapping("/export")
    @RequiresPermissions("gasArchive:pipe:export")
    public void export(TGasPipeArchive query,
                       @RequestParam(value = "includeHistory", defaultValue = "1") Integer includeHistory,
                       HttpServletResponse response) throws Exception {
        QueryWrapper<TGasPipeArchive> qw = buildQuery(query, includeHistory, ITGasPipeArchiveService.TYPE_PIPE);
        List<TGasPipeArchive> pipes = archiveService.selectList(qw);
        setExcelResponse(response, "燃气管道档案台账.xlsx");
        GasArchiveExcelUtil.writeLedger(pipes, null, response.getOutputStream());
    }

    /**
     * 列表/导出共用查询：恒带管道类别与在档标记；includeHistory 缺省/为 1 时返回全部状态，
     * 为 0 时仅返回在用。显式指定 status 时按单状态精确过滤（过滤下拉用）。
     */
    static QueryWrapper<TGasPipeArchive> buildQuery(TGasPipeArchive query, Integer includeHistory, int archiveType) {
        QueryWrapper<TGasPipeArchive> qw = new QueryWrapper<TGasPipeArchive>()
                .eq("del_flag", 0)
                .eq("archive_type", archiveType);
        if (query != null) {
            if (query.getArchiveNo() != null && !query.getArchiveNo().trim().isEmpty()) {
                qw.like("archive_no", query.getArchiveNo().trim());
            }
            if (query.getArchiveName() != null && !query.getArchiveName().trim().isEmpty()) {
                qw.like("archive_name", query.getArchiveName().trim());
            }
            if (query.getStatus() != null) {
                qw.eq("status", query.getStatus());
            } else if (includeHistory == null || includeHistory == 0) {
                qw.eq("status", ITGasPipeArchiveService.STATUS_IN_USE);
            }
            if (query.getProvinceCode() != null && !query.getProvinceCode().trim().isEmpty()) {
                qw.eq("province_code", query.getProvinceCode().trim());
            }
            if (query.getCityCode() != null && !query.getCityCode().trim().isEmpty()) {
                qw.eq("city_code", query.getCityCode().trim());
            }
            if (query.getAreaCode() != null && !query.getAreaCode().trim().isEmpty()) {
                qw.eq("area_code", query.getAreaCode().trim());
            }
            if (query.getStreetCode() != null && !query.getStreetCode().trim().isEmpty()) {
                qw.eq("street_code", query.getStreetCode().trim());
            }
            if (query.getSectorName() != null && !query.getSectorName().trim().isEmpty()) {
                qw.like("sector_name", query.getSectorName().trim());
            }
        }
        qw.orderByAsc("status").orderByAsc("archive_no");
        return qw;
    }

    static void setExcelResponse(HttpServletResponse response, String filename) throws Exception {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        response.setHeader("Content-Disposition", "attachment;filename*=utf-8''" + URLEncoder.encode(filename, "UTF-8"));
    }
}
