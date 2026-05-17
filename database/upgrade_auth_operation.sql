USE agro_drone_scheduler;

CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(64) NOT NULL,
    real_name VARCHAR(50) NOT NULL,
    role VARCHAR(30) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at DATETIME NOT NULL
);

INSERT INTO sys_user (username, password_hash, real_name, role, status, created_at)
SELECT 'admin', '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9', '系统管理员', 'ADMIN', 'ACTIVE', NOW()
WHERE NOT EXISTS (SELECT 1 FROM sys_user WHERE username = 'admin');

CREATE TABLE IF NOT EXISTS operation_record (
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

CREATE TABLE IF NOT EXISTS field_boundary (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    field_id BIGINT NOT NULL,
    point_order INT NOT NULL,
    longitude DECIMAL(10, 6) NOT NULL,
    latitude DECIMAL(10, 6) NOT NULL,
    INDEX idx_boundary_field (field_id, point_order),
    CONSTRAINT fk_boundary_field FOREIGN KEY (field_id) REFERENCES field_plot(id)
);

INSERT INTO field_boundary (field_id, point_order, longitude, latitude)
SELECT f.id, 1, f.longitude - 0.002000, f.latitude - 0.002000 FROM field_plot f
WHERE NOT EXISTS (SELECT 1 FROM field_boundary b WHERE b.field_id = f.id)
UNION ALL
SELECT f.id, 2, f.longitude + 0.002000, f.latitude - 0.002000 FROM field_plot f
WHERE NOT EXISTS (SELECT 1 FROM field_boundary b WHERE b.field_id = f.id)
UNION ALL
SELECT f.id, 3, f.longitude + 0.002000, f.latitude + 0.002000 FROM field_plot f
WHERE NOT EXISTS (SELECT 1 FROM field_boundary b WHERE b.field_id = f.id)
UNION ALL
SELECT f.id, 4, f.longitude - 0.002000, f.latitude + 0.002000 FROM field_plot f
WHERE NOT EXISTS (SELECT 1 FROM field_boundary b WHERE b.field_id = f.id);

CREATE TABLE IF NOT EXISTS operation_track (
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

CREATE TABLE IF NOT EXISTS operation_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    module_name VARCHAR(50) NOT NULL,
    action_name VARCHAR(50) NOT NULL,
    target_type VARCHAR(50),
    target_id BIGINT,
    content VARCHAR(255) NOT NULL,
    created_at DATETIME NOT NULL,
    INDEX idx_log_time (created_at)
);
