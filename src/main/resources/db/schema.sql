CREATE EXTENSION IF NOT EXISTS postgis;

-- 테이블 삭제 (의존성 역순으로)
DROP TABLE IF EXISTS kafka_fail_log CASCADE;
DROP TABLE IF EXISTS user_report_alert CASCADE;
DROP TABLE IF EXISTS user_volunteer_alert CASCADE;
DROP TABLE IF EXISTS volunteer_alert CASCADE;
DROP TABLE IF EXISTS report_response CASCADE;
DROP TABLE IF EXISTS report CASCADE;
DROP TABLE IF EXISTS attendance_policy CASCADE;
DROP TABLE IF EXISTS volunteer_participant CASCADE;
DROP TABLE IF EXISTS volunteer_team CASCADE;
DROP TABLE IF EXISTS volunteer_location CASCADE;
DROP TABLE IF EXISTS post CASCADE;
DROP TABLE IF EXISTS user_device CASCADE;
DROP TABLE IF EXISTS report_alert CASCADE;
DROP TABLE IF EXISTS users CASCADE;

-- 사용자 테이블
CREATE TABLE users (
                       id BIGSERIAL PRIMARY KEY,
                       nickname VARCHAR(255) UNIQUE,
                       phone_number VARCHAR(20),
                       email VARCHAR(100) NOT NULL UNIQUE,
                       password VARCHAR(255),
                       login_type VARCHAR(50),
                       provider VARCHAR(255),
                       business_number VARCHAR(255),
                       organization_name VARCHAR(255),
                       province VARCHAR(255),
                       city VARCHAR(255),
                       user_role VARCHAR(50),
                       last_login_at TIMESTAMP,
                       created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 게시글 테이블
CREATE TABLE post (
                      id BIGSERIAL PRIMARY KEY,
                      user_id BIGINT NOT NULL,
                      category VARCHAR(50),
                      title VARCHAR(255),
                      content TEXT,
                      total_capacity INT,
                      team_size INT,
                      created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                      updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                      CONSTRAINT fk_post_user FOREIGN KEY (user_id) REFERENCES users(id)
);

-- 봉사 위치 (1:1 with post)
CREATE TABLE volunteer_location (
                                    id BIGSERIAL PRIMARY KEY,
                                    place_name VARCHAR(255) NOT NULL,
                                    location_lat DOUBLE PRECISION,
                                    location_lng DOUBLE PRECISION,
                                    post_id BIGINT NOT NULL UNIQUE,
                                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                    CONSTRAINT fk_location_post FOREIGN KEY (post_id) REFERENCES post(id)
);

-- 봉사 팀 (N:1 post)
CREATE TABLE volunteer_team (
                                id BIGSERIAL PRIMARY KEY,
                                post_id BIGINT NOT NULL,
                                team_number INT NOT NULL,
                                max_capacity INT NOT NULL,
                                CONSTRAINT fk_team_post FOREIGN KEY (post_id) REFERENCES post(id)
);

-- 봉사 참가자
CREATE TABLE volunteer_participant (
                                       id BIGSERIAL PRIMARY KEY,
                                       user_id BIGINT,
                                       team_id BIGINT,
                                       joined_at TIMESTAMP,
                                       checkin_status VARCHAR(50) NOT NULL,
                                       CONSTRAINT fk_participant_user FOREIGN KEY (user_id) REFERENCES users(id),
                                       CONSTRAINT fk_participant_team FOREIGN KEY (team_id) REFERENCES volunteer_team(id)
);

-- 출석 정책
CREATE TABLE attendance_policy (
                                   id BIGSERIAL PRIMARY KEY,
                                   checkin_start TIMESTAMP,
                                   checkin_end TIMESTAMP,
                                   attendance_radius_meters INT,
                                   min_checkin_minutes INT,
                                   post_id BIGINT NOT NULL UNIQUE,
                                   created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                   updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                   CONSTRAINT fk_attendance_post FOREIGN KEY (post_id) REFERENCES post(id)
);

-- 재난 알림
CREATE TABLE report_alert (
                              id BIGSERIAL PRIMARY KEY,
                              si VARCHAR(255) NOT NULL,
                              gu VARCHAR(255) NOT NULL,
                              disaster_type VARCHAR(50) NOT NULL,
                              count BIGINT NOT NULL,
                              created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                              updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 사용자 재난 알림
CREATE TABLE user_report_alert (
                                   id BIGSERIAL PRIMARY KEY,
                                   alert_id BIGINT NOT NULL,
                                   user_id BIGINT NOT NULL,
                                   created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                   updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                   CONSTRAINT uk_user_report_alert UNIQUE (user_id, alert_id),
                                   CONSTRAINT fk_user_report_alert_alert FOREIGN KEY (alert_id) REFERENCES report_alert(id),
                                   CONSTRAINT fk_user_report_alert_user FOREIGN KEY (user_id) REFERENCES users(id)
);

-- 재난 신고
CREATE TABLE report (
                        id BIGSERIAL PRIMARY KEY,
                        reporter_id BIGINT NOT NULL,
                        disaster_type VARCHAR(255) NOT NULL,
                        description TEXT NOT NULL,
                        image_url VARCHAR(255),
                        video_url VARCHAR(255),
                        status VARCHAR(255) NOT NULL,
                        province VARCHAR(255) NOT NULL,
                        city VARCHAR(255),
                        location GEOGRAPHY(Point, 4326),
                        responder_id BIGINT,
                        created_at TIMESTAMP,
                        updated_at TIMESTAMP,
                        CONSTRAINT fk_reporter FOREIGN KEY (reporter_id) REFERENCES users(id),
                        CONSTRAINT fk_responder FOREIGN KEY (responder_id) REFERENCES users(id)
);

-- 사용자 기기 정보
CREATE TABLE user_device (
                             id BIGSERIAL PRIMARY KEY,
                             user_id BIGINT NOT NULL,
                             type VARCHAR(255),
                             os VARCHAR(255),
                             os_version VARCHAR(255),
                             model VARCHAR(255),
                             fcm_token VARCHAR(255) NOT NULL,
                             created_at TIMESTAMP,
                             updated_at TIMESTAMP,
                             CONSTRAINT fk_user_device_user FOREIGN KEY (user_id) REFERENCES users(id)
);

-- 제보 응답
CREATE TABLE report_response (
                                 id BIGSERIAL PRIMARY KEY,
                                 is_notified BOOLEAN NOT NULL DEFAULT FALSE,
                                 response_status VARCHAR(50) NOT NULL,
                                 notified_at TIMESTAMP,
                                 report_id BIGINT NOT NULL,
                                 responder_id BIGINT NOT NULL,
                                 created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                 updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                 CONSTRAINT fk_response_report FOREIGN KEY (report_id) REFERENCES report(id),
                                 CONSTRAINT fk_response_user FOREIGN KEY (responder_id) REFERENCES users(id)
);

-- Kafka DLQ 로그
CREATE SEQUENCE IF NOT EXISTS kafka_fail_log_seq START WITH 1 INCREMENT BY 50;

CREATE TABLE kafka_fail_log (
                                id BIGSERIAL PRIMARY KEY,
                                topic VARCHAR(255),
                                consumer_group VARCHAR(255),
                                payload TEXT,
                                reason VARCHAR(1000),
                                listener VARCHAR(255),
                                exception VARCHAR(255),
                                failed_at TIMESTAMP
);

-- 자원봉사 알림
CREATE TABLE volunteer_alert (
                                 id BIGSERIAL PRIMARY KEY,
                                 title VARCHAR(255) NOT NULL,
                                 place_name VARCHAR(255) NOT NULL,
                                 checkin_start TIMESTAMP NOT NULL,
                                 created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                 updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 사용자 봉사 알림
CREATE TABLE user_volunteer_alert (
                                      id BIGSERIAL PRIMARY KEY,
                                      alert_id BIGINT NOT NULL,
                                      user_id BIGINT NOT NULL,
                                      CONSTRAINT uk_user_volunteer_alert UNIQUE (user_id, alert_id),
                                      CONSTRAINT fk_volunteer_alert FOREIGN KEY (alert_id) REFERENCES volunteer_alert(id),
                                      CONSTRAINT fk_user_volunteer FOREIGN KEY (user_id) REFERENCES users(id)
);