-- Изменяем первичный ключ таблицы users на tg_id

-- Сначала удаляем существующее ограничение внешнего ключа
ALTER TABLE config DROP CONSTRAINT IF EXISTS config_uid_fkey;
ALTER TABLE config DROP CONSTRAINT IF EXISTS fk_config_user;

-- Сохраняем связь между id и tg_id для последующего обновления
CREATE TEMPORARY TABLE user_mapping AS
SELECT id, tg_id FROM users;

-- Убедимся, что tg_id имеет ограничение NOT NULL и обновляем пропущенные значения, если они есть
UPDATE users SET tg_id = id WHERE tg_id IS NULL;
ALTER TABLE users ALTER COLUMN tg_id SET NOT NULL;

-- Сначала добавляем ограничение уникальности для tg_id
ALTER TABLE users ADD CONSTRAINT users_tg_id_key UNIQUE (tg_id);

-- Обновляем таблицу config, чтобы в поле uid содержались значения tg_id вместо id
UPDATE config c SET uid = um.tg_id
FROM user_mapping um
WHERE c.uid = um.id;

-- Теперь обновляем внешний ключ в таблице config
ALTER TABLE config
    ADD CONSTRAINT config_uid_fkey FOREIGN KEY (uid) REFERENCES users(tg_id);

-- Удаляем старый первичный ключ
ALTER TABLE users DROP CONSTRAINT IF EXISTS users_pkey;

-- Делаем tg_id первичным ключом
ALTER TABLE users ADD PRIMARY KEY (tg_id);

-- Удаляем столбец id, так как мы будем использовать tg_id в качестве первичного ключа
ALTER TABLE users DROP COLUMN IF EXISTS id;

-- Удаляем временную таблицу
DROP TABLE user_mapping;
