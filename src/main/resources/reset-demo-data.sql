-- Reset local demo data. This script deletes business data in campus_activity.
-- Use only for local demo databases, not for production.

USE campus_activity;

SET FOREIGN_KEY_CHECKS = 0;

TRUNCATE TABLE notification;
TRUNCATE TABLE verification_record;
TRUNCATE TABLE activity_reservation;
TRUNCATE TABLE activity;
TRUNCATE TABLE activity_category;
TRUNCATE TABLE app_user;

SET FOREIGN_KEY_CHECKS = 1;

-- Default admin: 13900001111 / test123
INSERT INTO app_user (phone, password, nickname, role, status)
VALUES
('13900001111', '$2a$10$fFc4EXzJnqAbrK0R7JZB1.hH3Vg6tnMF3YFLBpbIOvpKVX1cu1hHC', 'Admin', 'ADMIN', 1),
('13800001111', '$2a$10$fFc4EXzJnqAbrK0R7JZB1.hH3Vg6tnMF3YFLBpbIOvpKVX1cu1hHC', 'DemoUser', 'USER', 1);

INSERT INTO activity_category (name, sort, status)
VALUES
('Lecture', 1, 1),
('Competition', 2, 1),
('Club', 3, 1);

INSERT INTO activity (
    category_id,
    title,
    description,
    location,
    total_quota,
    available_quota,
    start_time,
    end_time,
    reservation_start_time,
    reservation_end_time,
    status
)
VALUES
(
    1,
    'Java Backend Internship Talk',
    'A campus talk about Java backend learning paths and internship project preparation.',
    'Teaching Building A101',
    50,
    50,
    '2026-08-10 19:00:00',
    '2026-08-10 21:00:00',
    '2026-07-22 00:00:00',
    '2026-08-09 23:59:59',
    1
),
(
    2,
    'Campus Programming Challenge',
    'A campus-wide programming and project competition.',
    'Lab Building B203',
    30,
    30,
    '2026-08-15 14:00:00',
    '2026-08-15 17:00:00',
    '2026-07-22 00:00:00',
    '2026-08-14 23:59:59',
    1
);
