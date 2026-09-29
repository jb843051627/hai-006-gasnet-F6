-- gasnet 燃气管道与调压站基础档案建档（编号唯一/坐标校验/只增不删/区域树汇总）增量迁移 2026-09-29
-- 适用于已按旧版 gasnet.sql 建库的部署：新增基础档案主表与三张调压站附属表。
-- 新库直接执行 gasnet.sql 即可，无需本脚本。

CREATE TABLE IF NOT EXISTS t_gas_pipe_archive (
  id bigint NOT NULL COMMENT '主键',
  archive_no varchar(64) NOT NULL COMMENT '档案编号(管道/调压站全局唯一)',
  archive_type int NOT NULL COMMENT '档案类别 0管道 1调压站',
  archive_name varchar(128) DEFAULT NULL COMMENT '档案名称',
  start_lng decimal(10,6) DEFAULT NULL COMMENT '起点经度(调压站为站点经度)',
  start_lat decimal(10,6) DEFAULT NULL COMMENT '起点纬度(调压站为站点纬度)',
  end_lng decimal(10,6) DEFAULT NULL COMMENT '终点经度(调压站无终点,留空)',
  end_lat decimal(10,6) DEFAULT NULL COMMENT '终点纬度(调压站无终点,留空)',
  pipe_spec varchar(32) DEFAULT NULL COMMENT '管径尺寸(如 DN300/De110)',
  material_type varchar(64) DEFAULT NULL COMMENT '材质类型(如 无缝钢管/PE管/铸铁管)',
  lay_year int DEFAULT NULL COMMENT '铺设年份',
  province_code varchar(12) DEFAULT NULL COMMENT '所属省编码 t_sys_province.province_code',
  city_code varchar(12) DEFAULT NULL COMMENT '所属市编码 t_sys_city.city_code',
  area_code varchar(12) DEFAULT NULL COMMENT '所属区编码 t_sys_area.area_code',
  street_code varchar(12) DEFAULT NULL COMMENT '所属街道编码 t_sys_street.street_code',
  region_name varchar(255) DEFAULT NULL COMMENT '行政区划全名快照(省—市—区—街道)',
  sector_name varchar(128) DEFAULT NULL COMMENT '所属片区(街道以下片区,文本)',
  status int DEFAULT '0' COMMENT '当前状态 0在用 1停用 2报废(终态,只可由停用转入)',
  status_time datetime DEFAULT NULL COMMENT '状态最近变更时间',
  del_flag int DEFAULT '0' COMMENT '删除标记 恒为0:档案严禁删除',
  create_by varchar(64) DEFAULT NULL COMMENT '创建者',
  create_time datetime DEFAULT NULL COMMENT '创建时间',
  update_by varchar(64) DEFAULT NULL COMMENT '更新者',
  update_time datetime DEFAULT NULL COMMENT '更新时间',
  remark varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (id),
  UNIQUE KEY uk_gas_archive_no (archive_no),
  KEY idx_gas_archive_type_status (archive_type, status, del_flag),
  KEY idx_gas_archive_area (area_code),
  KEY idx_gas_archive_street (street_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='燃气管道/调压站基础档案';

CREATE TABLE IF NOT EXISTS t_gas_regulator_station (
  id bigint NOT NULL COMMENT '主键',
  station_id bigint NOT NULL COMMENT '调压站档案 t_gas_pipe_archive.id',
  device_model varchar(128) DEFAULT NULL COMMENT '设备型号(调压器/柜型号)',
  del_flag int DEFAULT '0' COMMENT '删除标记 0正常 1删除',
  create_by varchar(64) DEFAULT NULL COMMENT '创建者',
  create_time datetime DEFAULT NULL COMMENT '创建时间',
  update_by varchar(64) DEFAULT NULL COMMENT '更新者',
  update_time datetime DEFAULT NULL COMMENT '更新时间',
  remark varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (id),
  UNIQUE KEY uk_gas_station_id (station_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='调压站附属设备信息';

CREATE TABLE IF NOT EXISTS t_gas_station_pipe (
  id bigint NOT NULL COMMENT '主键',
  station_id bigint NOT NULL COMMENT '调压站档案 t_gas_pipe_archive.id',
  pipe_id bigint NOT NULL COMMENT '管辖管段档案 t_gas_pipe_archive.id(管道)',
  create_by varchar(64) DEFAULT NULL COMMENT '创建者',
  create_time datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_gas_station_pipe (station_id, pipe_id),
  KEY idx_gas_station_pipe_pipe (pipe_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='调压站管辖管段关联';

CREATE TABLE IF NOT EXISTS t_gas_station_maint (
  id bigint NOT NULL COMMENT '主键',
  station_id bigint NOT NULL COMMENT '调压站档案 t_gas_pipe_archive.id',
  maint_time datetime DEFAULT NULL COMMENT '维护时间',
  maint_type varchar(32) DEFAULT NULL COMMENT '维护类别(日常维护/定期检修/年度校验/抢修)',
  content varchar(500) DEFAULT NULL COMMENT '维护内容',
  operator varchar(64) DEFAULT NULL COMMENT '维护人',
  result varchar(255) DEFAULT NULL COMMENT '维护结果',
  create_by varchar(64) DEFAULT NULL COMMENT '创建者',
  create_time datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (id),
  KEY idx_gas_station_maint_station (station_id, maint_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='调压站维护记录(只增不改)';
