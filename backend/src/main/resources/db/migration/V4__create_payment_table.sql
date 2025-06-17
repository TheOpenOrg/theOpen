-- Создание таблицы payment для хранения данных о платежах
CREATE TABLE IF NOT EXISTS payment (
    id BIGSERIAL PRIMARY KEY,
    paymentId BIGINT,
    terminal_key VARCHAR(64),
    amount BIGINT,
    order_id VARCHAR(128),
    description VARCHAR(512),
    token VARCHAR(128),
    url VARCHAR(512),
    status VARCHAR(32)
);