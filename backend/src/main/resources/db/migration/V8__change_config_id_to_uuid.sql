-- Создаем временную таблицу с новой структурой
CREATE TABLE config_new (
    id UUID NOT NULL,
    server_id BIGINT,
    uid BIGINT,
    month_amount INT,
    buy_time timestamp(6),
    is_active BOOLEAN,
    payment_id BIGINT,
    is_trial BOOLEAN,
    name VARCHAR(255),
    PRIMARY KEY (id),
    FOREIGN KEY (server_id) REFERENCES servers(id),
    FOREIGN KEY (uid) REFERENCES users(tg_id),
    FOREIGN KEY (payment_id) REFERENCES payment(id)
);

-- Переносим данные, генерируя UUID для каждой строки
INSERT INTO config_new (id, server_id, uid, month_amount, buy_time, is_active, payment_id, is_trial, name)
SELECT gen_random_uuid(), server_id, uid, month_amount, buy_time, is_active, payment_id, is_trial, name
FROM config;

-- Удаляем старую таблицу
DROP TABLE config;

-- Переименовываем новую таблицу
Alter TABLE config_new Rename TO config;

-- Создаем индекс для ускорения поиска
CREATE INDEX idx_config_uid ON config(uid);
CREATE INDEX idx_config_name ON config(name);
