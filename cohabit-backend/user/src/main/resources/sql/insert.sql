-- Predefined accounts (survive restarts because drop-and-create reruns this script)
-- Vendor: vendor@cohabit.at / vendor123  (UUID matches owner_id in catalog insert.sql)
-- User:   user@cohabit.at   / user123

INSERT INTO users (id, email, password, created_at, updated_at)
VALUES
    ('fff0e4d7-aebe-4978-a212-89404a92c117', 'vendor@cohabit.at', '$2a$10$VpsjeLqe/JZMH6j5e1XD4O3QW5Qitx1qHWePHpJta1Tt0jZ5eHJDq', NOW(), NOW()),
    ('b1c2d3e4-f5a6-7890-abcd-ef1234567890', 'user@cohabit.at',   '$2a$10$mlEvI2lJNpc6n0GGTa3qZu1Nbjnd0QImvBg7Bk82QtdVk1ZPjWVPa', NOW(), NOW());

INSERT INTO user_roles (user_id, role)
VALUES
    ('fff0e4d7-aebe-4978-a212-89404a92c117', 'VENDOR'),
    ('b1c2d3e4-f5a6-7890-abcd-ef1234567890', 'USER');
