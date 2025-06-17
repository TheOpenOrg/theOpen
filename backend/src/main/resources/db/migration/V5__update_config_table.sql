-- Удаляем поле payment_link, extra_config
-- Добавляем поле paymentId (bigint), поле name (varchar) для хранения имени файла
ALTER TABLE config
    DROP COLUMN IF EXISTS payment_link,
    DROP COLUMN IF EXISTS payment_status,
    DROP COLUMN IF EXISTS extra_config,
    DROP COLUMN IF EXISTS paymtent_id,
    ADD COLUMN payment_id BIGINT REFERENCES payment(id),
    ADD COLUMN name VARCHAR(255);
