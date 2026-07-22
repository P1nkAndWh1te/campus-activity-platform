-- Development seed data. Run manually after schema.sql when you need a clean local demo database.
-- Default admin: 13900001111 / test123

USE campus_activity;

INSERT INTO app_user (phone, password, nickname, role, status)
VALUES
('13900001111', '$2a$10$fFc4EXzJnqAbrK0R7JZB1.hH3Vg6tnMF3YFLBpbIOvpKVX1cu1hHC', 'Admin', 'ADMIN', 1)
ON DUPLICATE KEY UPDATE
    nickname = VALUES(nickname),
    role = VALUES(role),
    status = VALUES(status);

INSERT INTO activity_category (name, sort, status)
VALUES
('Lecture', 1, 1),
('Competition', 2, 1),
('Club', 3, 1)
ON DUPLICATE KEY UPDATE
    sort = VALUES(sort),
    status = VALUES(status);
