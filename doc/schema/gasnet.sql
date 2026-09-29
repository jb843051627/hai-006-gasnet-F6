-- gasnet 城市燃气泄漏检测与管道安全巡检管理 -- schema (hai-005)
-- 列名与基线实体契约（@TableName/@TableField）逐列对齐，改列必须同步实体。
-- 库：hai_006

CREATE TABLE IF NOT EXISTS t_gas_conc_rule (
  id bigint NOT NULL COMMENT '主键',
  rule_code varchar(64) DEFAULT NULL COMMENT '泄漏浓度判定线代号',
  rule_name varchar(128) DEFAULT NULL COMMENT '判线名',
  version int DEFAULT '1' COMMENT '规则版本号(同一rule_code下自增)',
  th1_max decimal(12,2) DEFAULT NULL COMMENT '轻度档浓度上限(ppm)',
  th2_max decimal(12,2) DEFAULT NULL COMMENT '中度档浓度上限(ppm)',
  th3_max decimal(12,2) DEFAULT NULL COMMENT '重度档浓度上限(ppm)',
  eff_start datetime DEFAULT NULL COMMENT '启用之日(含)',
  eff_end datetime DEFAULT NULL COMMENT '交棒之日(不含,NULL为长期有效)',
  priority int DEFAULT NULL COMMENT '让线顺位(数值大的先说话)',
  status int DEFAULT NULL COMMENT '判线情形 0现行 1已让位',
  del_flag int DEFAULT '0' COMMENT '删除标记 0正常 1删除',
  create_by varchar(64) DEFAULT NULL COMMENT '创建者',
  create_time datetime DEFAULT NULL COMMENT '创建时间',
  update_by varchar(64) DEFAULT NULL COMMENT '更新者',
  update_time datetime DEFAULT NULL COMMENT '更新时间',
  remark varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='泄漏浓度分档判定线';

CREATE TABLE IF NOT EXISTS t_gas_imp_row (
  id bigint NOT NULL COMMENT '主键',
  batch_no varchar(64) DEFAULT NULL COMMENT '检漏数据上传批次号',
  row_no int DEFAULT NULL COMMENT '原表行次(Excel行号，表头为第1行)',
  item_code varchar(64) DEFAULT NULL COMMENT '检测设备/项代号(选填)',
  site_no varchar(64) DEFAULT NULL COMMENT '管段编号(原始值)',
  detect_time datetime DEFAULT NULL COMMENT '检测时间(解析成功值)',
  detect_time_raw varchar(64) DEFAULT NULL COMMENT '检测时间原始文本',
  qty decimal(12,2) DEFAULT NULL COMMENT '本行浓度读数ppm(解析成功值)',
  conc_raw varchar(64) DEFAULT NULL COMMENT '浓度原始文本',
  lng decimal(10,6) DEFAULT NULL COMMENT '经度(解析成功值)',
  lng_raw varchar(64) DEFAULT NULL COMMENT '经度原始文本',
  lat decimal(10,6) DEFAULT NULL COMMENT '纬度(解析成功值)',
  lat_raw varchar(64) DEFAULT NULL COMMENT '纬度原始文本',
  err_msg varchar(500) DEFAULT NULL COMMENT '校验失败原因(多项以；分隔)',
  check_id bigint DEFAULT NULL COMMENT '入库后关联正式记录 t_gas_leak_check.id',
  status int DEFAULT NULL COMMENT '行落地情形 0待核 1已入库 2退回(校验失败) 3重复跳过',
  del_flag int DEFAULT '0' COMMENT '删除标记 0正常 1删除',
  create_by varchar(64) DEFAULT NULL COMMENT '创建者',
  create_time datetime DEFAULT NULL COMMENT '创建时间',
  update_by varchar(64) DEFAULT NULL COMMENT '更新者',
  update_time datetime DEFAULT NULL COMMENT '更新时间',
  remark varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (id),
  KEY idx_gas_imp_row_batch (batch_no, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='检漏数据导入核对行';

CREATE TABLE IF NOT EXISTS t_gas_leak_check (
  id bigint NOT NULL COMMENT '主键',
  check_no varchar(64) DEFAULT NULL COMMENT '检测评定记录号',
  detect_time datetime DEFAULT NULL COMMENT '现场检测时间(以此定位在用规则版本)',
  site_no varchar(64) DEFAULT NULL COMMENT '检测点代号',
  site_name varchar(128) DEFAULT NULL COMMENT '检测点名称',
  conc_value decimal(12,2) DEFAULT NULL COMMENT '原始浓度读数(ppm)',
  grade_level int DEFAULT NULL COMMENT '评定等级 1轻微 2中度 3严重 4极严重 0无法判定',
  rule_id bigint DEFAULT NULL COMMENT '评定时适用规则版本主键(快照)',
  rule_code varchar(64) DEFAULT NULL COMMENT '适用规则代号(快照)',
  rule_version int DEFAULT NULL COMMENT '适用规则版本号(快照)',
  recheck_status int DEFAULT '0' COMMENT '复核情形 0未复核 1已重算 2重算后等级变更',
  recheck_time datetime DEFAULT NULL COMMENT '最近复核时间',
  recheck_by varchar(64) DEFAULT NULL COMMENT '复核人',
  recheck_grade_level int DEFAULT NULL COMMENT '复核后等级',
  recheck_rule_id bigint DEFAULT NULL COMMENT '复核时适用规则版本主键',
  recheck_rule_version int DEFAULT NULL COMMENT '复核时适用规则版本号',
  recheck_remark varchar(500) DEFAULT NULL COMMENT '复核说明',
  batch_no varchar(64) DEFAULT NULL COMMENT '检漏数据导入批次号(手工录入为NULL)',
  lng decimal(10,6) DEFAULT NULL COMMENT '检测点经度',
  lat decimal(10,6) DEFAULT NULL COMMENT '检测点纬度',
  status int DEFAULT '0' COMMENT '记录情形 0有效 1作废',
  del_flag int DEFAULT '0' COMMENT '删除标记 0正常 1删除',
  create_by varchar(64) DEFAULT NULL COMMENT '创建者',
  create_time datetime DEFAULT NULL COMMENT '创建时间',
  update_by varchar(64) DEFAULT NULL COMMENT '更新者',
  update_time datetime DEFAULT NULL COMMENT '更新时间',
  remark varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (id),
  UNIQUE KEY uk_gas_leak_check_no (check_no),
  KEY idx_gas_leak_detect_time (detect_time),
  KEY idx_gas_leak_rule (rule_id),
  KEY idx_gas_leak_batch (batch_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='泄漏浓度检测评定记录';

CREATE TABLE IF NOT EXISTS t_gas_pipe (
  id bigint NOT NULL COMMENT '主键',
  site_no varchar(64) DEFAULT NULL COMMENT '管段/调压站档案代号',
  site_name varchar(128) DEFAULT NULL COMMENT '档案名称',
  site_type varchar(32) DEFAULT NULL COMMENT '档案类别(管道/调压站)',
  road_name varchar(128) DEFAULT NULL COMMENT '所属行政区划(省—市—区—街道)',
  th1_max decimal(12,2) DEFAULT NULL COMMENT '单档管线长度一档上限(km)',
  th2_max decimal(12,2) DEFAULT NULL COMMENT '二档上限(km)',
  th3_max decimal(12,2) DEFAULT NULL COMMENT '三档上限(km)',
  status int DEFAULT NULL COMMENT '底档情形 0在用 1已退役',
  del_flag int DEFAULT '0' COMMENT '删除标记 0正常 1删除',
  create_by varchar(64) DEFAULT NULL COMMENT '创建者',
  create_time datetime DEFAULT NULL COMMENT '创建时间',
  update_by varchar(64) DEFAULT NULL COMMENT '更新者',
  update_time datetime DEFAULT NULL COMMENT '更新时间',
  remark varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='管段/调压站底档';

CREATE TABLE IF NOT EXISTS t_gas_pipe_arch (
  id bigint NOT NULL COMMENT '主键',
  bill_no varchar(64) DEFAULT NULL COMMENT '管道/调压站档案卡号',
  node_no int DEFAULT NULL COMMENT '本卡版本次序',
  site_id int DEFAULT NULL COMMENT '所属底档',
  site_no varchar(64) DEFAULT NULL COMMENT '所属底档代号',
  qty decimal(12,2) DEFAULT NULL COMMENT '本卡管线长度(km)',
  fine_amt decimal(12,2) DEFAULT NULL COMMENT '本年度新增管线长度(km)',
  content varchar(255) DEFAULT NULL COMMENT '校验串（随版本次序走）',
  grade_level int DEFAULT NULL COMMENT '管线长度折到的巡检强度层级',
  status int DEFAULT NULL COMMENT '进展 0待核对 1已核对 2已压死',
  del_flag int DEFAULT '0' COMMENT '删除标记 0正常 1删除',
  create_by varchar(64) DEFAULT NULL COMMENT '创建者',
  create_time datetime DEFAULT NULL COMMENT '创建时间',
  update_by varchar(64) DEFAULT NULL COMMENT '更新者',
  update_time datetime DEFAULT NULL COMMENT '更新时间',
  remark varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='管道/调压站档案卡';

CREATE TABLE IF NOT EXISTS t_gas_plan_task (
  id bigint NOT NULL COMMENT '主键',
  item_no varchar(64) DEFAULT NULL COMMENT '巡检派单事项号',
  site_id bigint DEFAULT NULL COMMENT '关联管段底档 t_gas_pipe.id(NULL为未关联)',
  site_no varchar(64) DEFAULT NULL COMMENT '关联管段底档代号(冗余)',
  due_at datetime DEFAULT NULL COMMENT '该动手那一天的最后一刻',
  amount decimal(12,2) DEFAULT NULL COMMENT '单条提前派单的自然日数',
  content varchar(255) DEFAULT NULL COMMENT '派单事由与受派班组记要',
  status int DEFAULT NULL COMMENT '条目情形 0待派 1已派 2派不出去 3已取消(管段停用)',
  del_flag int DEFAULT '0' COMMENT '删除标记 0正常 1删除',
  create_by varchar(64) DEFAULT NULL COMMENT '创建者',
  create_time datetime DEFAULT NULL COMMENT '创建时间',
  update_by varchar(64) DEFAULT NULL COMMENT '更新者',
  update_time datetime DEFAULT NULL COMMENT '更新时间',
  remark varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (id),
  KEY idx_gas_plan_task_site (site_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='巡检计划到期任务派单';

CREATE TABLE IF NOT EXISTS t_gas_repair_order (
  id bigint NOT NULL COMMENT '主键',
  biz_no varchar(64) DEFAULT NULL COMMENT '抢修工单号',
  content varchar(500) DEFAULT NULL COMMENT '险情记事(泄漏位置/险情描述等)',
  stage int DEFAULT 0 COMMENT '当前处置段 0待接单 1已接单 2抢修中 3待复核 4待封卡 5已封存',
  status int DEFAULT 0 COMMENT '工单落定 0待接单 1在办 2已封存',
  owner_id bigint DEFAULT NULL COMMENT '负责人 t_sys_user.id(超期预警接收人)',
  owner varchar(64) DEFAULT NULL COMMENT '负责人登录名(快照)',
  owner_name varchar(64) DEFAULT NULL COMMENT '负责人姓名(快照)',
  assignee_id bigint DEFAULT NULL COMMENT '接单巡检员 t_sys_user.id',
  assignee varchar(64) DEFAULT NULL COMMENT '接单人登录名(快照)',
  assignee_name varchar(64) DEFAULT NULL COMMENT '接单人姓名(快照)',
  due_at datetime DEFAULT NULL COMMENT '处置时限(超期未封卡即预警)',
  repair_result varchar(1000) DEFAULT NULL COMMENT '抢修结果(处置措施/换件/恢复供气等)',
  claim_time datetime DEFAULT NULL COMMENT '接单时间',
  start_time datetime DEFAULT NULL COMMENT '开始抢修时间',
  submit_time datetime DEFAULT NULL COMMENT '提交抢修结果时间',
  review_time datetime DEFAULT NULL COMMENT '现场复核时间',
  seal_time datetime DEFAULT NULL COMMENT '完工封卡时间',
  warn_count int DEFAULT 0 COMMENT '超期预警已发次数',
  last_warn_time datetime DEFAULT NULL COMMENT '最近一次超期预警时间(24小时内不重复提醒)',
  last_action varchar(64) DEFAULT NULL COMMENT '最近一次过口动作',
  del_flag int DEFAULT '0' COMMENT '删除标记 0正常 1删除',
  create_by varchar(64) DEFAULT NULL COMMENT '创建者',
  create_time datetime DEFAULT NULL COMMENT '创建时间',
  update_by varchar(64) DEFAULT NULL COMMENT '更新者',
  update_time datetime DEFAULT NULL COMMENT '更新时间',
  remark varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (id),
  KEY idx_gas_repair_stage (status, stage),
  KEY idx_gas_repair_due (due_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='抢修工单';

CREATE TABLE IF NOT EXISTS t_gas_repair_order_record (
  id bigint NOT NULL COMMENT '主键',
  order_id bigint DEFAULT NULL COMMENT '抢修工单 t_gas_repair_order.id',
  biz_no varchar(64) DEFAULT NULL COMMENT '抢修工单号(冗余)',
  stage int DEFAULT NULL COMMENT '动作发生时所处段 0接单 1开始抢修 2提交抢修结果 3现场复核 4完工封卡',
  stage_name varchar(32) DEFAULT NULL COMMENT '环节名快照(接单/开始抢修/提交抢修结果/现场复核/完工封卡)',
  action int DEFAULT NULL COMMENT '过口动作 0接单 1开始抢修 2提交抢修结果 3现场复核 4完工封卡',
  operator varchar(64) DEFAULT NULL COMMENT '操作人登录名',
  operator_name varchar(64) DEFAULT NULL COMMENT '操作人姓名(快照)',
  opinion varchar(500) DEFAULT NULL COMMENT '操作备注',
  create_time datetime DEFAULT NULL COMMENT '操作时间',
  PRIMARY KEY (id),
  KEY idx_gas_repair_record_order (order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='抢修工单处置履历(只增不改)';

CREATE TABLE IF NOT EXISTS t_gas_stop_bill (
  id bigint NOT NULL COMMENT '主键',
  bill_no varchar(64) DEFAULT NULL COMMENT '停用申请签核单号',
  site_id bigint DEFAULT NULL COMMENT '所属管段底档 t_gas_pipe.id',
  site_no varchar(64) DEFAULT NULL COMMENT '所属管段底档代号(快照)',
  apply_reason varchar(500) DEFAULT NULL COMMENT '停用申请理由(重大隐患/达到设计寿命等)',
  node_no int DEFAULT NULL COMMENT '当前所在道 0..3（0运维提/1安全核/2工程评/3管理层批）',
  sign_mode int DEFAULT NULL COMMENT '同口并印办法 0任一人 1两名点齐',
  need_count int DEFAULT NULL COMMENT '本口应落印数',
  sign_count int DEFAULT NULL COMMENT '本口已落印数',
  status int DEFAULT NULL COMMENT '报批情形 0在核 1已核讫 2已打回 3已否决',
  del_flag int DEFAULT '0' COMMENT '删除标记 0正常 1删除',
  create_by varchar(64) DEFAULT NULL COMMENT '创建者',
  create_time datetime DEFAULT NULL COMMENT '创建时间',
  update_by varchar(64) DEFAULT NULL COMMENT '更新者',
  update_time datetime DEFAULT NULL COMMENT '更新时间',
  remark varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (id),
  KEY idx_gas_stop_bill_site (site_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='燃气管段停用申请签核单';

CREATE TABLE IF NOT EXISTS t_gas_stop_bill_record (
  id bigint NOT NULL COMMENT '主键',
  bill_id bigint DEFAULT NULL COMMENT '签核单 t_gas_stop_bill.id',
  bill_no varchar(64) DEFAULT NULL COMMENT '签核单号(冗余)',
  node_no int DEFAULT NULL COMMENT '动作发生时所在道 0..3（0运维提/1安全核/2工程评/3管理层批）',
  node_name varchar(32) DEFAULT NULL COMMENT '环节名快照(运维申请/安全审核/工程评估/管理层批准)',
  action int DEFAULT NULL COMMENT '过口动作 0提交 1通过 2退回补充 3否决',
  approver varchar(64) DEFAULT NULL COMMENT '审批人(操作人登录名)',
  opinion varchar(500) DEFAULT NULL COMMENT '审批意见',
  create_time datetime DEFAULT NULL COMMENT '审批时间',
  PRIMARY KEY (id),
  KEY idx_gas_stop_record_bill (bill_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='燃气管段停用申请审批履历';

CREATE TABLE IF NOT EXISTS t_gas_task_dispatch (
  id bigint NOT NULL COMMENT '主键',
  dispatch_no varchar(64) DEFAULT NULL COMMENT '派发单号',
  task_id bigint DEFAULT NULL COMMENT '关联到期任务 t_gas_plan_task.id',
  item_no varchar(64) DEFAULT NULL COMMENT '巡检派单事项号(冗余)',
  target_type int DEFAULT NULL COMMENT '接收对象 0班组 1巡检员',
  target_id varchar(64) DEFAULT NULL COMMENT '接收对象主键(部门/用户)',
  target_name varchar(128) DEFAULT NULL COMMENT '接收对象名称(冗余)',
  dispatch_mode int DEFAULT NULL COMMENT '派发方式 0自动扫描 1手动指派',
  urgent int DEFAULT NULL COMMENT '紧急程度 0普通 1紧急(超期3天及以上)',
  overdue_days int DEFAULT NULL COMMENT '派发时已超期自然日数(未超期为0)',
  dispatch_time datetime DEFAULT NULL COMMENT '派发时间',
  dispatch_by varchar(64) DEFAULT NULL COMMENT '派发人(自动派发为system)',
  exec_status int DEFAULT NULL COMMENT '执行情况 0待执行 1执行中 2已完成',
  finish_time datetime DEFAULT NULL COMMENT '执行完成时间',
  status int DEFAULT NULL COMMENT '记录情形 0有效 1已撤销',
  del_flag int DEFAULT '0' COMMENT '删除标记 0正常 1删除',
  create_by varchar(64) DEFAULT NULL COMMENT '创建者',
  create_time datetime DEFAULT NULL COMMENT '创建时间',
  update_by varchar(64) DEFAULT NULL COMMENT '更新者',
  update_time datetime DEFAULT NULL COMMENT '更新时间',
  remark varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (id),
  KEY idx_gas_dispatch_task (task_id),
  KEY idx_gas_dispatch_target (target_type, target_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='巡检到期任务派发记录';

-- 档案铁律：
--   1) 编号唯一(uk)，建档后编号不可改；
--   2) 坐标经度[73,136)、纬度[3,54)（覆盖中国境内常见读数），服务层强校验；
--   3) 档案只允许新增/编辑，严禁物理删除（无删除接口，del_flag 恒为 0）；
--   4) status 0在用 1停用 2报废；停用/报废仅为状态位，历史永久可查、可选择导出；
--   5) 地理层级 省—市—区—街道—片区，片区挂在 t_sys_street 之下，
--      管道/调压站档案直挂片区，汇总沿片区→街道→区上卷。

CREATE TABLE IF NOT EXISTS t_gas_zone (
  id bigint NOT NULL COMMENT '主键',
  zone_code varchar(64) NOT NULL COMMENT '片区代号（全库唯一）',
  zone_name varchar(128) NOT NULL COMMENT '片区名称',
  street_code varchar(64) DEFAULT NULL COMMENT '所属街道 t_sys_street.street_code',
  area_code varchar(64) DEFAULT NULL COMMENT '所属区 t_sys_area.area_code（冗余，便于上卷统计）',
  city_code varchar(64) DEFAULT NULL COMMENT '所属市 t_sys_city.city_code（冗余）',
  province_code varchar(64) DEFAULT NULL COMMENT '所属省 t_sys_province.province_code（冗余）',
  sort int DEFAULT 0 COMMENT '排序号',
  status int DEFAULT 0 COMMENT '片区情形 0正常 1停用（停用不影响在档档案，仅禁止新建挂接）',
  del_flag int DEFAULT 0 COMMENT '删除标记（档案铁律：恒为0，不允许删除）',
  create_by varchar(64) DEFAULT NULL COMMENT '创建者',
  create_time datetime DEFAULT NULL COMMENT '创建时间',
  update_by varchar(64) DEFAULT NULL COMMENT '更新者',
  update_time datetime DEFAULT NULL COMMENT '更新时间',
  remark varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (id),
  UNIQUE KEY uk_gas_zone_code (zone_code),
  KEY idx_gas_zone_street (street_code),
  KEY idx_gas_zone_area (area_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='燃气档案行政区划片区（街道下一级）';

CREATE TABLE IF NOT EXISTS t_gas_pipeline_arch (
  id bigint NOT NULL COMMENT '主键',
  pipe_no varchar(64) NOT NULL COMMENT '管道编号（全库唯一，建档后不可改）',
  pipe_name varchar(128) DEFAULT NULL COMMENT '管道名称/起止描述',
  start_lng decimal(10,6) NOT NULL COMMENT '起点经度',
  start_lat decimal(10,6) NOT NULL COMMENT '起点纬度',
  end_lng decimal(10,6) NOT NULL COMMENT '终点经度',
  end_lat decimal(10,6) NOT NULL COMMENT '终点纬度',
  diameter decimal(10,2) DEFAULT NULL COMMENT '管径尺寸(mm)',
  material varchar(64) DEFAULT NULL COMMENT '材质类型(钢管/PE管/铸铁管/球墨铸铁管等)',
  lay_year int DEFAULT NULL COMMENT '铺设年份',
  zone_id bigint DEFAULT NULL COMMENT '所属片区 t_gas_zone.id',
  street_code varchar(64) DEFAULT NULL COMMENT '所属街道代号（冗余，片区划转时随片区同步）',
  area_code varchar(64) DEFAULT NULL COMMENT '所属区代号（冗余）',
  pressure_level varchar(32) DEFAULT NULL COMMENT '压力级制(高压/次高压/中压/低压)',
  length_m decimal(12,2) DEFAULT NULL COMMENT '管线长度(m)',
  status int NOT NULL DEFAULT 0 COMMENT '当前状态 0在用 1停用 2报废（仅状态位，历史永久保留）',
  del_flag int DEFAULT 0 COMMENT '删除标记（档案铁律：恒为0，不允许删除）',
  create_by varchar(64) DEFAULT NULL COMMENT '创建者',
  create_time datetime DEFAULT NULL COMMENT '创建时间',
  update_by varchar(64) DEFAULT NULL COMMENT '更新者',
  update_time datetime DEFAULT NULL COMMENT '更新时间',
  remark varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (id),
  UNIQUE KEY uk_gas_pipeline_no (pipe_no),
  KEY idx_gas_pipeline_status (status),
  KEY idx_gas_pipeline_zone (zone_id),
  KEY idx_gas_pipeline_street (street_code),
  KEY idx_gas_pipeline_area (area_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='燃气管道基础档案';

CREATE TABLE IF NOT EXISTS t_gas_regulator_station_arch (
  id bigint NOT NULL COMMENT '主键',
  station_no varchar(64) NOT NULL COMMENT '调压站编号（全库唯一，建档后不可改）',
  station_name varchar(128) DEFAULT NULL COMMENT '调压站名称',
  lng decimal(10,6) NOT NULL COMMENT '站址经度',
  lat decimal(10,6) NOT NULL COMMENT '站址纬度',
  diameter varchar(64) DEFAULT NULL COMMENT '进出口管径尺寸描述',
  material varchar(64) DEFAULT NULL COMMENT '主体材质类型',
  lay_year int DEFAULT NULL COMMENT '建成/铺设年份',
  zone_id bigint DEFAULT NULL COMMENT '所属片区 t_gas_zone.id',
  street_code varchar(64) DEFAULT NULL COMMENT '所属街道代号（冗余）',
  area_code varchar(64) DEFAULT NULL COMMENT '所属区代号（冗余）',
  device_model varchar(128) DEFAULT NULL COMMENT '调压器设备型号',
  design_pressure decimal(10,2) DEFAULT NULL COMMENT '设计压力(MPa)',
  outlet_pressure decimal(10,4) DEFAULT NULL COMMENT '出口设定压力(MPa)',
  rated_flow decimal(12,2) DEFAULT NULL COMMENT '额定流量(m³/h)',
  manufacturer varchar(128) DEFAULT NULL COMMENT '设备厂家',
  status int NOT NULL DEFAULT 0 COMMENT '当前状态 0在用 1停用 2报废（仅状态位，历史永久保留）',
  del_flag int DEFAULT 0 COMMENT '删除标记（档案铁律：恒为0，不允许删除）',
  create_by varchar(64) DEFAULT NULL COMMENT '创建者',
  create_time datetime DEFAULT NULL COMMENT '创建时间',
  update_by varchar(64) DEFAULT NULL COMMENT '更新者',
  update_time datetime DEFAULT NULL COMMENT '更新时间',
  remark varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (id),
  UNIQUE KEY uk_gas_station_no (station_no),
  KEY idx_gas_station_status (status),
  KEY idx_gas_station_zone (zone_id),
  KEY idx_gas_station_street (street_code),
  KEY idx_gas_station_area (area_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='燃气调压站基础档案';

CREATE TABLE IF NOT EXISTS t_gas_station_pipe (
  id bigint NOT NULL COMMENT '主键',
  station_id bigint NOT NULL COMMENT '调压站档案 t_gas_regulator_station_arch.id',
  pipeline_id bigint NOT NULL COMMENT '管辖管道档案 t_gas_pipeline_arch.id',
  scope_desc varchar(255) DEFAULT NULL COMMENT '管辖范围说明（阀井区间/桩号等）',
  create_by varchar(64) DEFAULT NULL COMMENT '创建者',
  create_time datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_gas_station_pipe (station_id, pipeline_id),
  KEY idx_gas_stp_pipeline (pipeline_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='调压站管辖管段范围关联';

CREATE TABLE IF NOT EXISTS t_gas_station_maint_record (
  id bigint NOT NULL COMMENT '主键',
  station_id bigint NOT NULL COMMENT '调压站档案 t_gas_regulator_station_arch.id',
  station_no varchar(64) DEFAULT NULL COMMENT '调压站编号（冗余快照）',
  maint_time datetime NOT NULL COMMENT '维护时间',
  maint_type varchar(32) DEFAULT NULL COMMENT '维护类型(日常巡检/定期保养/故障维修/大修改造)',
  device_model varchar(128) DEFAULT NULL COMMENT '维护对象设备型号（快照）',
  content varchar(1000) DEFAULT NULL COMMENT '维护内容（更换部件/调试情况等）',
  maintainer varchar(64) DEFAULT NULL COMMENT '维护人',
  next_maint_time datetime DEFAULT NULL COMMENT '下次维护计划时间',
  create_by varchar(64) DEFAULT NULL COMMENT '登记人',
  create_time datetime DEFAULT NULL COMMENT '登记时间',
  PRIMARY KEY (id),
  KEY idx_gas_maint_station (station_id),
  KEY idx_gas_maint_time (maint_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='调压站设备维护记录（只增不改）';

-- 初始档案数据：id=0 在用、id=1 已退役
-- 管线长度上限 30/80/200 km：等于上限算高一档。
INSERT IGNORE INTO t_gas_pipe (id, site_no, site_name, site_type, road_name, th1_max, th2_max, th3_max, status, del_flag, create_by, create_time)
VALUES (0, 'GP00', '青坪市城区中压主干管段（在用）', '管道', '青坪省—青坪市—城东区—东湖街道', 30.00, 80.00, 200.00, 0, 0, 'seed', NOW()),
       (1, 'GP01', '青坪市城东调压站（已退役）', '调压站', '青坪省—青坪市—城东区—东湖街道', 30.00, 80.00, 200.00, 1, 0, 'seed', NOW());

-- ===== 管道/调压站基础档案库演示数据（2026-09-29） =====
-- 行政区划沿用基线库重庆市编码；基线库 t_sys_street 无种子，补两条（渝中区 500103）
INSERT IGNORE INTO t_sys_street (id, street_code, area_code, street_name, short_name, sort, data_state, create_by, create_time)
VALUES (510103001, '500103001', '500103', '解放碑街道', '解放碑', 1, 0, 'seed', NOW()),
       (510103002, '500103002', '500103', '南纪门街道', '南纪门', 2, 0, 'seed', NOW());

-- 片区（街道下一级）
INSERT IGNORE INTO t_gas_zone (id, zone_code, zone_name, street_code, area_code, city_code, province_code, sort, status, del_flag, create_by, create_time)
VALUES (5001, 'ZONE-JFB-01', '解放碑东片区', '500103001', '500103', '500100', '500000', 1, 0, 0, 'seed', NOW()),
       (5002, 'ZONE-JFB-02', '解放碑西片区', '500103001', '500103', '500100', '500000', 2, 0, 0, 'seed', NOW()),
       (5003, 'ZONE-NJM-01', '南纪门沿江片区', '500103002', '500103', '500100', '500000', 1, 0, 0, 'seed', NOW());

-- 管道档案：覆盖 在用/停用/报废（status 0在用 1停用 2报废）
INSERT IGNORE INTO t_gas_pipeline_arch (id, pipe_no, pipe_name, start_lng, start_lat, end_lng, end_lat, diameter, material, lay_year, zone_id, street_code, area_code, pressure_level, length_m, status, del_flag, create_by, create_time)
VALUES (5101, 'GD-JFB-0001', '解放碑东片区中压主干管', 106.572000, 29.557000, 106.581000, 29.558500, 300.00, 'PE管', 2015, 5001, '500103001', '500103', '中压', 1200.00, 0, 0, 'seed', NOW()),
       (5102, 'GD-JFB-0002', '解放碑西片区庭院支管', 106.560000, 29.556000, 106.564000, 29.554500, 150.00, '钢管', 2003, 5002, '500103001', '500103', '低压', 620.00, 1, 0, 'seed', NOW()),
       (5103, 'GD-NJM-0001', '南纪门沿江老铸铁管段', 106.563000, 29.550000, 106.569000, 29.548500, 200.00, '铸铁管', 1996, 5003, '500103002', '500103', '中压', 880.00, 2, 0, 'seed', NOW());

-- 调压站档案：1 在用 + 1 停用
INSERT IGNORE INTO t_gas_regulator_station_arch (id, station_no, station_name, lng, lat, diameter, material, lay_year, zone_id, street_code, area_code, device_model, design_pressure, outlet_pressure, rated_flow, manufacturer, status, del_flag, create_by, create_time)
VALUES (5201, 'RTZ-JFB-01', '解放碑东调压站', 106.575000, 29.557600, 'DN200/DN150', '钢管', 2015, 5001, '500103001', '500103', 'RTZ-200/0.4AQ', 0.40, 0.0030, 2000.00, '重庆燃气设备厂', 0, 0, 'seed', NOW()),
       (5202, 'RTZ-JFB-02', '解放碑西调压柜', 106.562500, 29.555200, 'DN100/DN80', '钢管', 2003, 5002, '500103001', '500103', 'RTZ-100/0.4FQ', 0.40, 0.0028, 800.00, '川仪股份', 1, 0, 'seed', NOW());

-- 管辖关系：5201 管 5101/5102；5202 管 5102
INSERT IGNORE INTO t_gas_station_pipe (id, station_id, pipeline_id, scope_desc, create_by, create_time)
VALUES (5301, 5201, 5101, '解放碑东路口1#阀井至民生路2#阀井', 'seed', NOW()),
       (5302, 5201, 5102, '民权路3#阀井至邹容路4#阀井', 'seed', NOW()),
       (5303, 5202, 5102, '西区庭院网入口阀组（与5201共管）', 'seed', NOW());

-- 维护履历（只增不改）
INSERT IGNORE INTO t_gas_station_maint_record (id, station_id, station_no, maint_time, maint_type, device_model, content, maintainer, next_maint_time, create_by, create_time)
VALUES (5401, 5201, 'RTZ-JFB-01', '2026-06-15 09:30:00', '定期保养', 'RTZ-200/0.4AQ', '清洗调压器导压管、校验出口压力设定值0.003MPa，更换过滤器滤芯', '王维保', '2026-12-15 00:00:00', 'seed', NOW()),
       (5402, 5201, 'RTZ-JFB-01', '2026-09-01 14:00:00', '故障维修', 'RTZ-200/0.4AQ', '出口压力波动偏大，更换主调压器膜片后恢复正常', '李抢修', NULL, 'seed', NOW());

