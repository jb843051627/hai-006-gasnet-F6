-- gasnet 城市燃气管道/调压站建档登记（基础档案库）增量迁移 2026-09-29
-- 适用于已按旧版 gasnet.sql 建库的部署：新增管道档案、调压站档案、
-- 调压站管辖管段关联、调压站设备维护记录、行政区划片区五张表。
-- 新库直接执行 gasnet.sql 即可，无需本脚本。
-- 库：hai_006
--
-- 档案铁律：
--   1) 编号唯一（uk），新建时强校验；
--   2) 起终点坐标经度 [73,136)、纬度 [3,54)（覆盖中国境内 GCJ-02/WGS-84/BD-09 常见读数）；
--   3) 档案只允许新增/编辑，严禁物理删除（无任何删除接口，del_flag 恒为 0）；
--   4) status 0在用 1停用 2报废；停用/报废仅为状态位，台账历史永久可查、可选择导出；
--   5) 地理层级 省—市—区—街道—片区：片区表挂在 t_sys_street 之下，
--      管道/调压站档案直挂片区，汇总统计沿片区→街道→区→市→省上卷。

-- 1) 片区（行政区划树末梢：街道下一级；树由 省/市/区/街道 既有四张表 + 本表层接而成）
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

-- 2) 管道档案
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

-- 3) 调压站档案（在管道档案公共字段之外，另含设备型号、设计压力等附属信息）
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

-- 4) 调压站—管辖管段关联（一站可管多段，一段可被多站管辖；只增/可编辑挂接，不随站停用而物理删除）
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

-- 5) 调压站设备维护记录（附属履历，只增不改）
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

-- 6) 种子数据：行政区划沿用基线库重庆市编码；街道基线库无种子，补两条（渝中区 500103）
INSERT IGNORE INTO t_sys_street (id, street_code, area_code, street_name, short_name, sort, data_state, create_by, create_time)
VALUES (510103001, '500103001', '500103', '解放碑街道', '解放碑', 1, 0, 'seed', NOW()),
       (510103002, '500103002', '500103', '南纪门街道', '南纪门', 2, 0, 'seed', NOW());

-- 片区：每个街道下两个片区
INSERT IGNORE INTO t_gas_zone (id, zone_code, zone_name, street_code, area_code, city_code, province_code, sort, status, del_flag, create_by, create_time)
VALUES (5001, 'ZONE-JFB-01', '解放碑东片区', '500103001', '500103', '500100', '500000', 1, 0, 0, 'seed', NOW()),
       (5002, 'ZONE-JFB-02', '解放碑西片区', '500103001', '500103', '500100', '500000', 2, 0, 0, 'seed', NOW()),
       (5003, 'ZONE-NJM-01', '南纪门沿江片区', '500103002', '500103', '500100', '500000', 1, 0, 0, 'seed', NOW());

-- 管道档案演示：覆盖 在用/停用/报废 三种状态
-- status 0在用 1停用 2报废；坐标为渝中区城区近似经纬度
INSERT IGNORE INTO t_gas_pipeline_arch (id, pipe_no, pipe_name, start_lng, start_lat, end_lng, end_lat, diameter, material, lay_year, zone_id, street_code, area_code, pressure_level, length_m, status, del_flag, create_by, create_time)
VALUES (5101, 'GD-JFB-0001', '解放碑东片区中压主干管', 106.572000, 29.557000, 106.581000, 29.558500, 300.00, 'PE管', 2015, 5001, '500103001', '500103', '中压', 1200.00, 0, 0, 'seed', NOW()),
       (5102, 'GD-JFB-0002', '解放碑西片区庭院支管', 106.560000, 29.556000, 106.564000, 29.554500, 150.00, '钢管', 2003, 5002, '500103001', '500103', '低压', 620.00, 1, 0, 'seed', NOW()),
       (5103, 'GD-NJM-0001', '南纪门沿江老铸铁管段', 106.563000, 29.550000, 106.569000, 29.548500, 200.00, '铸铁管', 1996, 5003, '500103002', '500103', '中压', 880.00, 2, 0, 'seed', NOW());

-- 调压站档案演示：1 在用 + 1 停用
INSERT IGNORE INTO t_gas_regulator_station_arch (id, station_no, station_name, lng, lat, diameter, material, lay_year, zone_id, street_code, area_code, device_model, design_pressure, outlet_pressure, rated_flow, manufacturer, status, del_flag, create_by, create_time)
VALUES (5201, 'RTZ-JFB-01', '解放碑东调压站', 106.575000, 29.557600, 'DN200/DN150', '钢管', 2015, 5001, '500103001', '500103', 'RTZ-200/0.4AQ', 0.40, 0.0030, 2000.00, '重庆燃气设备厂', 0, 0, 'seed', NOW()),
       (5202, 'RTZ-JFB-02', '解放碑西调压柜', 106.562500, 29.555200, 'DN100/DN80', '钢管', 2003, 5002, '500103001', '500103', 'RTZ-100/0.4FQ', 0.40, 0.0028, 800.00, '川仪股份', 1, 0, 'seed', NOW());

-- 管辖关系：5201 管 5101/5102；5202 管 5102
INSERT IGNORE INTO t_gas_station_pipe (id, station_id, pipeline_id, scope_desc, create_by, create_time)
VALUES (5301, 5201, 5101, '解放碑东路口1#阀井至民生路2#阀井', 'seed', NOW()),
       (5302, 5201, 5102, '民权路3#阀井至邹容路4#阀井', 'seed', NOW()),
       (5303, 5202, 5102, '西区庭院网入口阀组（与5201共管）', 'seed', NOW());

-- 维护履历演示（只增不改）
INSERT IGNORE INTO t_gas_station_maint_record (id, station_id, station_no, maint_time, maint_type, device_model, content, maintainer, next_maint_time, create_by, create_time)
VALUES (5401, 5201, 'RTZ-JFB-01', '2026-06-15 09:30:00', '定期保养', 'RTZ-200/0.4AQ', '清洗调压器导压管、校验出口压力设定值0.003MPa，更换过滤器滤芯', '王维保', '2026-12-15 00:00:00', 'seed', NOW()),
       (5402, 5201, 'RTZ-JFB-01', '2026-09-01 14:00:00', '故障维修', 'RTZ-200/0.4AQ', '出口压力波动偏大，更换主调压器膜片后恢复正常', '李抢修', NULL, 'seed', NOW());

-- 7) 权限菜单（与既有燃气模块一致：不写入 t_sys_permission，admin 角色全量可访问）
