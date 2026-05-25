CREATE DATABASE IF NOT EXISTS agro_drone_scheduler DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE agro_drone_scheduler;

DROP TABLE IF EXISTS schedule_plan;
DROP TABLE IF EXISTS operation_record;
DROP TABLE IF EXISTS operation_track;
DROP TABLE IF EXISTS operation_log;
DROP TABLE IF EXISTS operation_task;
DROP TABLE IF EXISTS field_boundary;
DROP TABLE IF EXISTS drone;
DROP TABLE IF EXISTS field_plot;
DROP TABLE IF EXISTS sys_user;

CREATE TABLE sys_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(64) NOT NULL,
    real_name VARCHAR(50) NOT NULL,
    role VARCHAR(30) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at DATETIME NOT NULL
);

CREATE TABLE drone (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    code VARCHAR(32) NOT NULL UNIQUE,
    model VARCHAR(64) NOT NULL,
    payload_kg DECIMAL(8, 2) NOT NULL,
    spray_width_meter DECIMAL(8, 2) NOT NULL,
    battery_percent INT NOT NULL,
    status VARCHAR(20) NOT NULL,
    longitude DECIMAL(10, 6),
    latitude DECIMAL(10, 6),
    updated_at DATETIME NOT NULL
);

CREATE TABLE field_plot (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(64) NOT NULL,
    crop_type VARCHAR(32) NOT NULL,
    area_mu DECIMAL(10, 2) NOT NULL,
    soil_moisture_level VARCHAR(20) NOT NULL,
    pest_risk_level VARCHAR(20) NOT NULL,
    longitude DECIMAL(10, 6),
    latitude DECIMAL(10, 6)
);

CREATE TABLE field_boundary (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    field_id BIGINT NOT NULL,
    point_order INT NOT NULL,
    longitude DECIMAL(10, 6) NOT NULL,
    latitude DECIMAL(10, 6) NOT NULL,
    INDEX idx_boundary_field (field_id, point_order),
    CONSTRAINT fk_boundary_field FOREIGN KEY (field_id) REFERENCES field_plot(id)
);

CREATE TABLE operation_task (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    task_no VARCHAR(40) NOT NULL UNIQUE,
    field_id BIGINT NOT NULL,
    operation_type VARCHAR(32) NOT NULL,
    required_area_mu DECIMAL(10, 2) NOT NULL,
    priority INT NOT NULL,
    earliest_start_time DATETIME NOT NULL,
    latest_end_time DATETIME NOT NULL,
    status VARCHAR(20) NOT NULL,
    remark VARCHAR(255),
    created_by VARCHAR(50),
    INDEX idx_task_status_priority (status, priority),
    CONSTRAINT fk_task_field FOREIGN KEY (field_id) REFERENCES field_plot(id)
);

CREATE TABLE schedule_plan (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    task_id BIGINT NOT NULL,
    drone_id BIGINT NOT NULL,
    planned_start_time DATETIME NOT NULL,
    planned_end_time DATETIME NOT NULL,
    estimated_area_mu DECIMAL(10, 2) NOT NULL,
    score DECIMAL(8, 2) NOT NULL,
    decision_reason VARCHAR(255) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at DATETIME NOT NULL,
    INDEX idx_plan_time (planned_start_time),
    CONSTRAINT fk_plan_task FOREIGN KEY (task_id) REFERENCES operation_task(id),
    CONSTRAINT fk_plan_drone FOREIGN KEY (drone_id) REFERENCES drone(id)
);

CREATE TABLE operation_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    schedule_plan_id BIGINT NOT NULL,
    task_id BIGINT NOT NULL,
    drone_id BIGINT NOT NULL,
    actual_start_time DATETIME NOT NULL,
    actual_end_time DATETIME,
    actual_area_mu DECIMAL(10, 2),
    duration_minutes INT,
    result VARCHAR(20) NOT NULL,
    exception_remark VARCHAR(255),
    created_at DATETIME NOT NULL,
    INDEX idx_record_plan (schedule_plan_id),
    CONSTRAINT fk_record_plan FOREIGN KEY (schedule_plan_id) REFERENCES schedule_plan(id),
    CONSTRAINT fk_record_task FOREIGN KEY (task_id) REFERENCES operation_task(id),
    CONSTRAINT fk_record_drone FOREIGN KEY (drone_id) REFERENCES drone(id)
);

CREATE TABLE operation_track (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    record_id BIGINT NOT NULL,
    drone_id BIGINT NOT NULL,
    longitude DECIMAL(10, 6) NOT NULL,
    latitude DECIMAL(10, 6) NOT NULL,
    altitude DECIMAL(8, 2),
    speed DECIMAL(8, 2),
    battery_percent INT,
    reported_at DATETIME NOT NULL,
    INDEX idx_track_record_time (record_id, reported_at),
    CONSTRAINT fk_track_record FOREIGN KEY (record_id) REFERENCES operation_record(id),
    CONSTRAINT fk_track_drone FOREIGN KEY (drone_id) REFERENCES drone(id)
);

CREATE TABLE operation_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    module_name VARCHAR(50) NOT NULL,
    action_name VARCHAR(50) NOT NULL,
    target_type VARCHAR(50),
    target_id BIGINT,
    content VARCHAR(255) NOT NULL,
    created_at DATETIME NOT NULL,
    INDEX idx_log_time (created_at)
);

DROP TABLE IF EXISTS pest_detection_record;

CREATE TABLE pest_detection_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    field_plot_id BIGINT,
    image_url VARCHAR(255) NOT NULL,
    original_filename VARCHAR(255) NOT NULL,
    detected_disease VARCHAR(64),
    severity VARCHAR(20),
    confidence DECIMAL(5,4),
    description VARCHAR(500),
    green_ratio DECIMAL(5,4),
    yellow_ratio DECIMAL(5,4),
    brown_ratio DECIMAL(5,4),
    gray_ratio DECIMAL(5,4),
    orange_ratio DECIMAL(5,4),
    applied_to_field TINYINT NOT NULL DEFAULT 0,
    created_by BIGINT,
    created_at DATETIME NOT NULL,
    updated_at DATETIME,
    INDEX idx_detection_field (field_plot_id),
    INDEX idx_detection_time (created_at)
);

INSERT INTO sys_user (username, password_hash, real_name, role, status, created_at) VALUES
('admin', '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9', '系统管理员', 'ADMIN', 'ACTIVE', NOW());

INSERT INTO drone (code, model, payload_kg, spray_width_meter, battery_percent, status, longitude, latitude, updated_at) VALUES
('UAV-A01', 'DJI Agras T50', 40.00, 11.00, 92, 'IDLE', 116.397128, 39.916527, NOW()),
('UAV-A02', 'DJI Agras T25', 20.00, 7.00, 76, 'IDLE', 116.401221, 39.918332, NOW()),
('UAV-B01', 'XAG P100 Pro', 50.00, 12.00, 38, 'CHARGING', 116.390311, 39.912145, NOW()),
('UAV-C03', 'EAVision J100', 30.00, 9.00, 63, 'MAINTENANCE', 116.405632, 39.921482, NOW());

INSERT INTO field_plot (name, crop_type, area_mu, soil_moisture_level, pest_risk_level, longitude, latitude) VALUES
('一号水稻田', '水稻', 128.50, 'MEDIUM', 'HIGH', 116.412432, 39.924876),
('西侧玉米地', '玉米', 86.00, 'LOW', 'MEDIUM', 116.386522, 39.917212),
('北区小麦田', '小麦', 156.00, 'HIGH', 'LOW', 116.398711, 39.936721),
('试验蔬菜棚', '蔬菜', 42.50, 'MEDIUM', 'HIGH', 116.374234, 39.910342);

INSERT INTO field_boundary (field_id, point_order, longitude, latitude) VALUES
(1, 1, 116.410432, 39.922876), (1, 2, 116.414432, 39.922876), (1, 3, 116.414432, 39.926876), (1, 4, 116.410432, 39.926876),
(2, 1, 116.384522, 39.915212), (2, 2, 116.388522, 39.915212), (2, 3, 116.388522, 39.919212), (2, 4, 116.384522, 39.919212),
(3, 1, 116.396711, 39.934721), (3, 2, 116.400711, 39.934721), (3, 3, 116.400711, 39.938721), (3, 4, 116.396711, 39.938721),
(4, 1, 116.372234, 39.908342), (4, 2, 116.376234, 39.908342), (4, 3, 116.376234, 39.912342), (4, 4, 116.372234, 39.912342);

INSERT INTO operation_task (task_no, field_id, operation_type, required_area_mu, priority, earliest_start_time, latest_end_time, status, remark, created_by) VALUES
('TASK-2026050901', 1, 'PESTICIDE', 128.50, 5, NOW(), DATE_ADD(NOW(), INTERVAL 12 HOUR), 'PENDING', '稻飞虱预警，优先处理', 'admin'),
('TASK-2026050902', 2, 'FERTILIZER', 86.00, 3, DATE_ADD(NOW(), INTERVAL 1 HOUR), DATE_ADD(NOW(), INTERVAL 1 DAY), 'PENDING', '追肥作业', 'admin'),
('TASK-2026050903', 3, 'SEEDING', 120.00, 2, DATE_ADD(NOW(), INTERVAL 2 HOUR), DATE_ADD(NOW(), INTERVAL 2 DAY), 'PENDING', '补播区域', 'admin'),
('TASK-2026050904', 4, 'PESTICIDE', 42.50, 4, NOW(), DATE_ADD(NOW(), INTERVAL 8 HOUR), 'PENDING', '温室周边飞防', 'admin');
