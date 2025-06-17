-- Удаляем поле payment_link, extra_config
-- Добавляем поле paymentId (bigint), поле name (varchar) для хранения имени файла
ALTER TABLE payment
    DROP COLUMN IF EXISTS paymentid,
    ADD COLUMN payment_id BIGINT;
