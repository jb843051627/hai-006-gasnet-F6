-- 初始档案数据：id=0 在用、id=1 已退役
-- 管线长度上限 30/80/200 km：等于上限算高一档。
INSERT IGNORE INTO t_gas_pipe (id, site_no, site_name, site_type, road_name, th1_max, th2_max, th3_max, status, del_flag, create_by, create_time)
VALUES (0, 'GP00', '青坪市城区中压主干管段（在用）', '管道', '青坪省—青坪市—城东区—东湖街道', 30.00, 80.00, 200.00, 0, 0, 'seed', NOW()),
       (1, 'GP01', '青坪市城东调压站（已退役）', '调压站', '青坪省—青坪市—城东区—东湖街道', 30.00, 80.00, 200.00, 1, 0, 'seed', NOW());

-- 巡检班组（挂在总部节点 id=1 之下，status=1 启用）
INSERT IGNORE INTO t_sys_department (id, parent_id, dept_name, leader, phone, email, status, order_num, create_by, create_time)
VALUES (101, 1, '巡检一班', '班组长一', '13000000001', 'team1@gas.net', 1, 11, 'seed', NOW()),
       (102, 1, '巡检二班', '班组长二', '13000000002', 'team2@gas.net', 1, 12, 'seed', NOW()),
       (103, 1, '巡检三班', '班组长三', '13000000003', 'team3@gas.net', 1, 13, 'seed', NOW());

-- 到期任务派单演示数据：覆盖 超期5天/3天/1天、今日到期、窗口内未到期、窗口外、已派 七种情形
-- amount=提前派单自然日数；status 0待派 1已派 2派不出去 3已取消(管段停用)
-- site_id/site_no 关联管段底档：已停用管段(status=1)的任务不再被扫描派单；未关联(NULL)的按原样参与
INSERT IGNORE INTO t_gas_plan_task (id, item_no, site_id, site_no, due_at, amount, content, status, del_flag, create_by, create_time)
VALUES (1, 'PT-YEAR-0001', 0, 'GP00', DATE_SUB(NOW(), INTERVAL 5 DAY), 3, '年度检测：青坪市城区中压主干管段泄漏浓度普测', 0, 0, 'seed', NOW()),
       (2, 'PT-MON-0002', NULL, NULL, DATE_SUB(NOW(), INTERVAL 3 DAY), 3, '月度巡检：城东区东湖街道调压柜运行状态检查', 0, 0, 'seed', NOW()),
       (3, 'PT-YEAR-0003', NULL, NULL, DATE_SUB(NOW(), INTERVAL 1 DAY), 3, '年度检测：城西片区中压支线管段防腐层检测', 0, 0, 'seed', NOW()),
       (4, 'PT-MON-0004', NULL, NULL, DATE_FORMAT(NOW(), '%Y-%m-%d 23:59:59'), 3, '月度巡检：城南片区庭院管网入户安全检查', 0, 0, 'seed', NOW()),
       (5, 'PT-SEAS-0005', NULL, NULL, DATE_ADD(NOW(), INTERVAL 2 DAY), 3, '季度检测：城北调压站压力容器定期校验', 0, 0, 'seed', NOW()),
       (6, 'PT-YEAR-0006', NULL, NULL, DATE_ADD(NOW(), INTERVAL 10 DAY), 3, '年度检测：开发区工业用户专线管段巡检', 0, 0, 'seed', NOW()),
       (7, 'PT-MON-0007', NULL, NULL, DATE_SUB(NOW(), INTERVAL 2 DAY), 3, '月度巡检：老城区铸铁管段重点排查（已派）', 1, 0, 'seed', NOW());

-- 周期扫描定时任务：每日 01:00 扫描到期任务并自动派单（status 0正常 1暂停）
INSERT IGNORE INTO t_sys_quartz_job (id, job_name, job_group, invoke_target, cron_expression, misfire_policy, concurrent, status, create_by, create_time, remark)
VALUES (9001, '到期任务扫描派单', 'GASNET', 'gasPlanTaskScanTask.scanDueTasks()', '0 0 1 * * ?', '2', '1', 0, 'seed', NOW(), '每日扫描应派未派的巡检任务，按班组负载均衡自动派单，超期3天以上优先');

-- 浓度分档规则版本化演示：同一判线 CITY-MID 两个版本，2026-01-01 00:00:00 交棒
-- V1 阈值 20/50/100 ppm（已让位）；V2 收紧为 10/30/80 ppm（现行、长期有效）
-- 区间左闭右开：2025-12-31 23:59:59 的检测仍按 V1，2026-01-01 00:00:00 起按 V2
INSERT IGNORE INTO t_gas_conc_rule (id, rule_code, rule_name, version, th1_max, th2_max, th3_max, eff_start, eff_end, priority, status, del_flag, create_by, create_time, remark)
VALUES (8001, 'CITY-MID', '城区中压管网浓度判线', 1, 20.00, 50.00, 100.00, '2025-01-01 00:00:00', '2026-01-01 00:00:00', 10, 1, 0, 'seed', NOW(), '初版判线'),
       (8002, 'CITY-MID', '城区中压管网浓度判线', 2, 10.00, 30.00, 80.00, '2026-01-01 00:00:00', NULL, 10, 0, 0, 'seed', NOW(), '依据新标准收紧阈值（等于上限取高一档）');

-- 检测评定记录演示（按各自检测时刻的在用版本判档，版本快照留痕）：
-- 8101 检测于交棒前，浓度15ppm：V1 下落轻微(1)（若误用 V2 会成中度(2)，历史评定不随后续改标漂移）
-- 8102 检测于交棒后，浓度15ppm：V2 下中度(2)
INSERT IGNORE INTO t_gas_leak_check (id, check_no, detect_time, site_no, site_name, conc_value, grade_level, rule_id, rule_code, rule_version, recheck_status, status, del_flag, create_by, create_time)
VALUES (8101, 'LC20251215100000-8101', '2025-12-15 10:00:00', 'GP00-V01', '青坪市城区中压主干管段东湖路口检测点', 15.00, 1, 8001, 'CITY-MID', 1, 0, 0, 0, 'seed', NOW()),
       (8102, 'LC20260920103000-8102', '2026-09-20 10:30:00', 'GP00-V01', '青坪市城区中压主干管段东湖路口检测点', 15.00, 2, 8002, 'CITY-MID', 2, 0, 0, 0, 'seed', NOW());

-- 规则交棒排期翻转定时任务：每分钟扫描 eff_end 已到但状态仍为现行的版本并翻转（status 0正常 1暂停）
INSERT IGNORE INTO t_sys_quartz_job (id, job_name, job_group, invoke_target, cron_expression, misfire_policy, concurrent, status, create_by, create_time, remark)
VALUES (9002, '浓度规则交棒翻转', 'GASNET', 'gasConcRuleRolloverTask.rollover()', '0 * * * * ?', '2', '1', 0, 'seed', NOW(), '每分钟扫描到点交棒的规则版本，翻转台账状态位（评定在否在用以时间窗为准）');

-- 管段停用申请演示：GP00 主干管段已达设计寿命且有重大隐患，运维已提交，现停在 1安全核 待审
-- node_no 0运维提/1安全核/2工程评/3管理层批；status 0在核 1已核讫 2已打回 3已否决
INSERT IGNORE INTO t_gas_stop_bill (id, bill_no, site_id, site_no, apply_reason, node_no, sign_mode, need_count, sign_count, status, del_flag, create_by, create_time)
VALUES (7001, 'SB20260926000001', 0, 'GP00', '管段服役已满设计寿命，近期泄漏普测浓度持续偏高，存在重大安全隐患，申请停用。', 1, 0, 1, 0, 0, 0, 'seed', NOW());

-- 停用申请审批履历演示：7001 的运维提交留痕（action 0提交 1通过 2退回补充 3否决）
INSERT IGNORE INTO t_gas_stop_bill_record (id, bill_id, bill_no, node_no, node_name, action, approver, opinion, create_time)
VALUES (7001, 7001, 'SB20260926000001', 0, '运维申请', 0, 'seed', '管段服役已满设计寿命，近期泄漏普测浓度持续偏高，存在重大安全隐患，申请停用。', NOW());

-- 抢修工单超期预警定时任务：每小时扫描处置时限已过仍未封卡的工单（status 0正常 1暂停）
INSERT IGNORE INTO t_sys_quartz_job (id, job_name, job_group, invoke_target, cron_expression, misfire_policy, concurrent, status, create_by, create_time, remark)
VALUES (9003, '抢修工单超期预警', 'GASNET', 'gasRepairOrderWarnTask.scanOverdue()', '0 0 * * * ?', '2', '1', 0, 'seed', NOW(), '每小时扫描处置时限已过仍未完工封卡的抢修工单，站内信提醒负责人/接单人督促处置，同一工单24小时内不重复提醒');

-- 抢修工单处置流程演示：
-- 6001 城东调压站燃气泄漏，超期未接单（待接单，时限已过，预警目标）
-- 6002 庭院管被施工挖破，已接单并开始抢修（抢修中）
-- 6003 阀门井微漏，处置完成已完工封卡（终态，永久封存）
-- stage 0待接单 1已接单 2抢修中 3待复核 4待封卡 5已封存；status 0待接单 1在办 2已封存
INSERT IGNORE INTO t_gas_repair_order (id, biz_no, content, stage, status, owner_id, owner, owner_name, assignee_id, assignee, assignee_name, due_at, repair_result, claim_time, start_time, submit_time, review_time, seal_time, warn_count, last_warn_time, last_action, del_flag, create_by, create_time, remark)
VALUES (6001, 'RO20260928090001', '城东区东湖街调压站调压器法兰处燃气泄漏，现场有明显臭剂味，已警戒疏散', 0, 0, 1, 'admin', '管理员', NULL, NULL, NULL, DATE_SUB(NOW(), INTERVAL 2 HOUR), NULL, NULL, NULL, NULL, NULL, NULL, 0, NULL, NULL, 0, 'seed', NOW(), '超期未接单，定时任务将预警负责人'),
       (6002, 'RO20260928100002', '城南片区庭院管被第三方施工挖破，中压泄漏，已关阀降压', 2, 1, 1, 'admin', '管理员', 1, 'admin', '管理员', DATE_ADD(NOW(), INTERVAL 6 HOUR), NULL, DATE_SUB(NOW(), INTERVAL 90 MINUTE), DATE_SUB(NOW(), INTERVAL 30 MINUTE), NULL, NULL, NULL, 0, NULL, '开始抢修', 0, 'seed', DATE_SUB(NOW(), INTERVAL 100 MINUTE), '抢修进行中'),
       (6003, 'RO20260928110003', '城北小区阀门井接头微漏', 5, 2, 1, 'admin', '管理员', 1, 'admin', '管理员', DATE_SUB(NOW(), INTERVAL 1 DAY), '更换密封圈并复紧接头，气密性试验合格，恢复正常供气', DATE_SUB(NOW(), INTERVAL 30 HOUR), DATE_SUB(NOW(), INTERVAL 29 HOUR), DATE_SUB(NOW(), INTERVAL 26 HOUR), DATE_SUB(NOW(), INTERVAL 25 HOUR), DATE_SUB(NOW(), INTERVAL 24 HOUR), 0, NULL, '完工封卡', 0, 'seed', DATE_SUB(NOW(), INTERVAL 32 HOUR), '已完工封卡，记录永久封存');

-- 抢修工单处置履历演示：6002 已走到开始抢修；6003 五段履历齐全（action 0接单 1开始抢修 2提交抢修结果 3现场复核 4完工封卡）
INSERT IGNORE INTO t_gas_repair_order_record (id, order_id, biz_no, stage, stage_name, action, operator, operator_name, opinion, create_time)
VALUES (600201, 6002, 'RO20260928100002', 0, '接单', 0, 'admin', '管理员', '已电话核实险情，即刻赶赴现场', DATE_SUB(NOW(), INTERVAL 90 MINUTE)),
       (600202, 6002, 'RO20260928100002', 1, '开始抢修', 1, 'admin', '管理员', '关闭上游阀门降压，开挖工作面', DATE_SUB(NOW(), INTERVAL 30 MINUTE)),
       (600301, 6003, 'RO20260928110003', 0, '接单', 0, 'admin', '管理员', '接单前往', DATE_SUB(NOW(), INTERVAL 30 HOUR)),
       (600302, 6003, 'RO20260928110003', 1, '开始抢修', 1, 'admin', '管理员', '拆除保温层查漏点', DATE_SUB(NOW(), INTERVAL 29 HOUR)),
       (600303, 6003, 'RO20260928110003', 2, '提交抢修结果', 2, 'admin', '管理员', '更换密封圈并复紧接头，气密试验合格', DATE_SUB(NOW(), INTERVAL 26 HOUR)),
       (600304, 6003, 'RO20260928110003', 3, '现场复核', 3, 'admin', '管理员', '现场复核浓度检测正常，复核通过', DATE_SUB(NOW(), INTERVAL 25 HOUR)),
       (600305, 6003, 'RO20260928110003', 4, '完工封卡', 4, 'admin', '管理员', '资料齐全，准予封卡', DATE_SUB(NOW(), INTERVAL 24 HOUR));

-- ============================================================================
-- 燃气管道与调压站基础档案演示数据（2026-09-29）
-- 演示行政区划：青坪省(990000)—青坪市(990100)—城东区/城西片区(990101/990102)—东湖街道等
-- ============================================================================

INSERT IGNORE INTO t_sys_province (id, province_code, province_name, short_name, lng, lat, sort, data_state, create_by, create_time)
VALUES (9901, '990000', '青坪省', '青坪', '112.900000', '28.200000', 99, 0, 'seed', NOW());

INSERT IGNORE INTO t_sys_city (id, city_code, city_name, short_name, province_code, lng, lat, sort, data_state, create_by, create_time)
VALUES (9901, '990100', '青坪市', '青坪', '990000', '112.950000', '28.230000', 99, 0, 'seed', NOW());

INSERT IGNORE INTO t_sys_area (id, area_code, city_code, area_name, short_name, lng, lat, sort, data_state, create_by, create_time)
VALUES (9901, '990101', '990100', '城东区', '城东', '112.980000', '28.240000', 1, 0, 'seed', NOW()),
       (9902, '990102', '990100', '城西片区', '城西', '112.900000', '28.230000', 2, 0, 'seed', NOW());

INSERT IGNORE INTO t_sys_street (id, street_code, area_code, street_name, short_name, lng, lat, sort, data_state, create_by, create_time)
VALUES (9901, '990101001', '990101', '东湖街道', '东湖', '112.990000', '28.245000', 1, 0, 'seed', NOW()),
       (9902, '990101002', '990101', '西河街道', '西河', '112.970000', '28.235000', 2, 0, 'seed', NOW()),
       (9903, '990102001', '990102', '南湖街道', '南湖', '112.890000', '28.220000', 1, 0, 'seed', NOW());

-- 管道档案：0在用 1停用 2报废；调压站档案无终点坐标
INSERT IGNORE INTO t_gas_pipe_archive (id, archive_no, archive_type, archive_name, start_lng, start_lat, end_lng, end_lat, pipe_spec, material_type, lay_year, province_code, city_code, area_code, street_code, region_name, sector_name, status, status_time, del_flag, create_by, create_time, remark)
VALUES (9301, 'PA-GD-0001', 0, '城东区东湖路中压主干管', 112.991200, 28.246300, 112.998800, 28.249100, 'DN300', '无缝钢管', 2012, '990000', '990100', '990101', '990101001', '青坪省—青坪市—城东区—东湖街道', '东湖片区', 0, NOW(), 0, 'seed', NOW(), '城区中压主干'),
       (9302, 'PA-GD-0002', 0, '城东区东湖街庭院支管', 112.990100, 28.244200, 112.993500, 28.245800, 'De110', 'PE管', 2003, '990000', '990100', '990101', '990101001', '青坪省—青坪市—城东区—东湖街道', '东湖片区', 1, NOW(), 0, 'seed', NOW(), '老化停用，待报废评估'),
       (9303, 'PA-GD-0003', 0, '城西片区南湖路次干管', 112.888100, 28.219600, 112.895400, 28.224300, 'DN200', '球墨铸铁管', 2018, '990000', '990100', '990102', '990102001', '青坪省—青坪市—城西片区—南湖街道', '南湖片区', 0, NOW(), 0, 'seed', NOW(), NULL),
       (9304, 'PA-GD-0004', 0, '城东区西河路老低压管', 112.968200, 28.233100, 112.972800, 28.236900, 'DN150', '灰口铸铁管', 1996, '990000', '990100', '990101', '990101002', '青坪省—青坪市—城东区—西河街道', '西河片区', 2, NOW(), 0, 'seed', NOW(), '已报废,档案永久保留'),
       (9311, 'PA-ZJ-0001', 1, '城东东湖调压站', 112.992600, 28.247100, NULL, NULL, 'DN200/DN100', '钢制', 2012, '990000', '990100', '990101', '990101001', '青坪省—青坪市—城东区—东湖街道', '东湖片区', 0, NOW(), 0, 'seed', NOW(), '区域中压调压枢纽'),
       (9312, 'PA-ZJ-0002', 1, '西河南湖调压柜', 112.891300, 28.221500, NULL, NULL, 'DN100/DN50', '钢制', 2008, '990000', '990100', '990102', '990102001', '青坪省—青坪市—城西片区—南湖街道', '南湖片区', 1, NOW(), 0, 'seed', NOW(), '设备老化停用检修');

-- 调压站设备型号
INSERT IGNORE INTO t_gas_regulator_station (id, station_id, device_model, del_flag, create_by, create_time, remark)
VALUES (9401, 9311, 'RX-300/0.4-M 区域调压柜(双路)', 0, 'seed', NOW(), '一用一备双调压器'),
       (9402, 9312, 'RX-100/0.4-S 楼栋调压箱(单路)', 0, 'seed', NOW(), NULL);

-- 城东东湖调压站管辖管段：9301、9302
INSERT IGNORE INTO t_gas_station_pipe (id, station_id, pipe_id, create_by, create_time)
VALUES (9501, 9311, 9301, 'seed', NOW()),
       (9502, 9311, 9302, 'seed', NOW());

-- 维护记录（只增不改）
INSERT IGNORE INTO t_gas_station_maint (id, station_id, maint_time, maint_type, content, operator, result, create_by, create_time)
VALUES (9601, 9311, DATE_SUB(NOW(), INTERVAL 90 DAY), '定期检修', '调压器拆解清洗、更换阀口垫片、校验切断压力', '张维护', '出口压力稳定,校验合格', 'seed', NOW()),
       (9602, 9311, DATE_SUB(NOW(), INTERVAL 20 DAY), '日常维护', '滤芯清污、安全放散阀动作试验、井室积水抽排', '李巡检', '动作正常', 'seed', NOW()),
       (9603, 9312, DATE_SUB(NOW(), INTERVAL 12 DAY), '抢修', '出口压力波动,临时停用并更换老化膜片', '王抢修', '停用待复测,暂未恢复供气', 'seed', NOW());

-- ============================================================================
-- 菜单与权限（授给管理员角色 488243256161730560；档案管理独立目录 9200）
-- ============================================================================
INSERT IGNORE INTO t_sys_permission (id, name, descripion, url, is_blank, pid, perms, type, icon, order_num, visible, create_by, create_time)
VALUES (9200, '燃气档案管理', '管道与调压站基础档案', '', 0, 0, '', 0, 'layui-icon layui-icon-template-1', 20, 0, 'seed', NOW()),
       (9210, '管道档案', '管道档案台账', '/gasPipeArchive/view', 0, 9200, 'gasArchive:pipe:view', 1, 'layui-icon layui-icon-share', 1, 0, 'seed', NOW()),
       (9211, '管道档案集合', '管道档案集合查询', '/gasPipeArchive/list', 0, 9210, 'gasArchive:pipe:list', 2, '', NULL, 0, 'seed', NOW()),
       (9212, '管道档案新增', '管道档案建档', '/gasPipeArchive/add', 0, 9210, 'gasArchive:pipe:add', 2, '', NULL, 0, 'seed', NOW()),
       (9213, '管道档案编辑', '管道档案属性变更', '/gasPipeArchive/edit', 0, 9210, 'gasArchive:pipe:edit', 2, '', NULL, 0, 'seed', NOW()),
       (9214, '管道状态调整', '在用/停用/报废状态流转', '/gasPipeArchive/changeStatus', 0, 9210, 'gasArchive:pipe:changeStatus', 2, '', NULL, 0, 'seed', NOW()),
       (9215, '管道台账导出', '管道档案台账导出', '/gasPipeArchive/export', 0, 9210, 'gasArchive:pipe:export', 2, '', NULL, 0, 'seed', NOW()),
       (9216, '区域档案统计', '按行政区划统计数量状态', '/gasPipeArchive/regionStats', 0, 9210, 'gasArchive:pipe:stats', 2, '', NULL, 0, 'seed', NOW()),
       (9220, '调压站档案', '调压站档案台账', '/gasStationArchive/view', 0, 9200, 'gasArchive:station:view', 1, 'layui-icon layui-icon-app', 2, 0, 'seed', NOW()),
       (9221, '调压站档案集合', '调压站档案集合查询', '/gasStationArchive/list', 0, 9220, 'gasArchive:station:list', 2, '', NULL, 0, 'seed', NOW()),
       (9222, '调压站档案新增', '调压站档案建档', '/gasStationArchive/add', 0, 9220, 'gasArchive:station:add', 2, '', NULL, 0, 'seed', NOW()),
       (9223, '调压站档案编辑', '调压站档案属性变更/片区划转', '/gasStationArchive/edit', 0, 9220, 'gasArchive:station:edit', 2, '', NULL, 0, 'seed', NOW()),
       (9224, '调压站状态调整', '在用/停用/报废状态流转', '/gasStationArchive/changeStatus', 0, 9220, 'gasArchive:station:changeStatus', 2, '', NULL, 0, 'seed', NOW()),
       (9225, '调压站台账导出', '调压站档案台账导出', '/gasStationArchive/export', 0, 9220, 'gasArchive:station:export', 2, '', NULL, 0, 'seed', NOW()),
       (9226, '调压站详情', '管辖管段/设备型号/维护记录', '/gasStationArchive/detail', 0, 9220, 'gasArchive:station:detail', 2, '', NULL, 0, 'seed', NOW()),
       (9227, '维护记录登记', '调压站维护记录只增登记', '/gasStationArchive/addMaint', 0, 9220, 'gasArchive:station:maint', 2, '', NULL, 0, 'seed', NOW()),
       (9228, '管辖管段配置', '维护调压站管辖管段范围', '/gasStationArchive/savePipes', 0, 9220, 'gasArchive:station:editPipes', 2, '', NULL, 0, 'seed', NOW());

INSERT IGNORE INTO t_sys_permission_role (id, role_id, permission_id, create_by, create_time)
VALUES (9200, 488243256161730560, 9200, 'seed', NOW()),
       (9210, 488243256161730560, 9210, 'seed', NOW()),
       (9211, 488243256161730560, 9211, 'seed', NOW()),
       (9212, 488243256161730560, 9212, 'seed', NOW()),
       (9213, 488243256161730560, 9213, 'seed', NOW()),
       (9214, 488243256161730560, 9214, 'seed', NOW()),
       (9215, 488243256161730560, 9215, 'seed', NOW()),
       (9216, 488243256161730560, 9216, 'seed', NOW()),
       (9220, 488243256161730560, 9220, 'seed', NOW()),
       (9221, 488243256161730560, 9221, 'seed', NOW()),
       (9222, 488243256161730560, 9222, 'seed', NOW()),
       (9223, 488243256161730560, 9223, 'seed', NOW()),
       (9224, 488243256161730560, 9224, 'seed', NOW()),
       (9225, 488243256161730560, 9225, 'seed', NOW()),
       (9226, 488243256161730560, 9226, 'seed', NOW()),
       (9227, 488243256161730560, 9227, 'seed', NOW()),
       (9228, 488243256161730560, 9228, 'seed', NOW());

-- 调压站区域统计按钮权限（追加，授给管理员角色）
INSERT IGNORE INTO t_sys_permission (id, name, descripion, url, is_blank, pid, perms, type, icon, order_num, visible, create_by, create_time)
VALUES (9229, '调压站区域统计', '按行政区划统计数量状态', '/gasStationArchive/regionStats', 0, 9220, 'gasArchive:station:stats', 2, '', NULL, 0, 'seed', NOW());
INSERT IGNORE INTO t_sys_permission_role (id, role_id, permission_id, create_by, create_time)
VALUES (9229, 488243256161730560, 9229, 'seed', NOW());
