CREATE TABLE IF NOT EXISTS country (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    name_ru VARCHAR(100),
    code VARCHAR(10) NOT NULL UNIQUE
);

-- Если записей не существует, добавляем страны
INSERT INTO country (name, name_ru, code)
SELECT 'Russia', 'Россия', 'ru'
WHERE NOT EXISTS (SELECT 1 FROM country WHERE code = 'ru');

INSERT INTO country (name, name_ru, code)
SELECT 'USA', 'США', 'us'
WHERE NOT EXISTS (SELECT 1 FROM country WHERE code = 'us');

INSERT INTO country (name, name_ru, code)
SELECT 'Germany', 'Германия', 'de'
WHERE NOT EXISTS (SELECT 1 FROM country WHERE code = 'de');

INSERT INTO country (name, name_ru, code)
SELECT 'United Kingdom', 'Великобритания', 'gb'
WHERE NOT EXISTS (SELECT 1 FROM country WHERE code = 'gb');

INSERT INTO country (name, name_ru, code)
SELECT 'France', 'Франция', 'fr'
WHERE NOT EXISTS (SELECT 1 FROM country WHERE code = 'fr');

INSERT INTO country (name, name_ru, code)
SELECT 'China', 'Китай', 'cn'
WHERE NOT EXISTS (SELECT 1 FROM country WHERE code = 'cn');

INSERT INTO country (name, name_ru, code)
SELECT 'Japan', 'Япония', 'jp'
WHERE NOT EXISTS (SELECT 1 FROM country WHERE code = 'jp');

INSERT INTO country (name, name_ru, code)
SELECT 'Singapore', 'Сингапур', 'sg'
WHERE NOT EXISTS (SELECT 1 FROM country WHERE code = 'sg');

INSERT INTO country (name, name_ru, code)
SELECT 'Netherlands', 'Нидерланды', 'nl'
WHERE NOT EXISTS (SELECT 1 FROM country WHERE code = 'nl');

INSERT INTO country (name, name_ru, code)
SELECT 'Turkey', 'Турция', 'tr'
WHERE NOT EXISTS (SELECT 1 FROM country WHERE code = 'tr');

INSERT INTO country (name, name_ru, code)
SELECT 'Kazakhstan', 'Казахстан', 'kz'
WHERE NOT EXISTS (SELECT 1 FROM country WHERE code = 'kz');

INSERT INTO country (name, name_ru, code)
SELECT 'Switzerland', 'Швейцария', 'ch'
WHERE NOT EXISTS (SELECT 1 FROM country WHERE code = 'ch');

INSERT INTO country (name, name_ru, code)
SELECT 'United Arab Emirates', 'ОАЭ', 'ae'
WHERE NOT EXISTS (SELECT 1 FROM country WHERE code = 'ae');

INSERT INTO country (name, name_ru, code)
SELECT 'Canada', 'Канада', 'ca'
WHERE NOT EXISTS (SELECT 1 FROM country WHERE code = 'ca');

INSERT INTO country (name, name_ru, code)
SELECT 'Spain', 'Испания', 'es'
WHERE NOT EXISTS (SELECT 1 FROM country WHERE code = 'es');

INSERT INTO country (name, name_ru, code)
SELECT 'Finland', 'Финляндия', 'fi'
WHERE NOT EXISTS (SELECT 1 FROM country WHERE code = 'fi');

INSERT INTO country (name, name_ru, code)
SELECT 'Ukraine', 'Украина', 'ua'
WHERE NOT EXISTS (SELECT 1 FROM country WHERE code = 'ua');

INSERT INTO country (name, name_ru, code)
SELECT 'Austria', 'Австрия', 'at'
WHERE NOT EXISTS (SELECT 1 FROM country WHERE code = 'at');

INSERT INTO country (name, name_ru, code)
SELECT 'Italy', 'Италия', 'it'
WHERE NOT EXISTS (SELECT 1 FROM country WHERE code = 'it');

INSERT INTO country (name, name_ru, code)
SELECT 'Hong Kong', 'Гонконг', 'hk'
WHERE NOT EXISTS (SELECT 1 FROM country WHERE code = 'hk');

INSERT INTO country (name, name_ru, code)
SELECT 'Portugal', 'Португалия', 'pt'
WHERE NOT EXISTS (SELECT 1 FROM country WHERE code = 'pt');

INSERT INTO country (name, name_ru, code)
SELECT 'Sweden', 'Швеция', 'se'
WHERE NOT EXISTS (SELECT 1 FROM country WHERE code = 'se');

INSERT INTO country (name, name_ru, code)
SELECT 'Lithuania', 'Литва', 'lt'
WHERE NOT EXISTS (SELECT 1 FROM country WHERE code = 'lt');

INSERT INTO country (name, name_ru, code)
SELECT 'Estonia', 'Эстония', 'ee'
WHERE NOT EXISTS (SELECT 1 FROM country WHERE code = 'ee');

INSERT INTO country (name, name_ru, code)
SELECT 'Greece', 'Греция', 'gr'
WHERE NOT EXISTS (SELECT 1 FROM country WHERE code = 'gr');

INSERT INTO country (name, name_ru, code)
SELECT 'Latvia', 'Латвия', 'lv'
WHERE NOT EXISTS (SELECT 1 FROM country WHERE code = 'lv');

INSERT INTO country (name, name_ru, code)
SELECT 'Norway', 'Норвегия', 'no'
WHERE NOT EXISTS (SELECT 1 FROM country WHERE code = 'no');

INSERT INTO country (name, name_ru, code)
SELECT 'Ireland', 'Ирландия', 'ie'
WHERE NOT EXISTS (SELECT 1 FROM country WHERE code = 'ie');

INSERT INTO country (name, name_ru, code)
SELECT 'Denmark', 'Дания', 'dk'
WHERE NOT EXISTS (SELECT 1 FROM country WHERE code = 'dk');

INSERT INTO country (name, name_ru, code)
SELECT 'Belgium', 'Бельгия', 'be'
WHERE NOT EXISTS (SELECT 1 FROM country WHERE code = 'be');

INSERT INTO country (name, name_ru, code)
SELECT 'Poland', 'Польша', 'pl'
WHERE NOT EXISTS (SELECT 1 FROM country WHERE code = 'pl');

INSERT INTO country (name, name_ru, code)
SELECT 'Brazil', 'Бразилия', 'br'
WHERE NOT EXISTS (SELECT 1 FROM country WHERE code = 'br');

INSERT INTO country (name, name_ru, code)
SELECT 'North Macedonia', 'Македония', 'mk'
WHERE NOT EXISTS (SELECT 1 FROM country WHERE code = 'mk');

INSERT INTO country (name, name_ru, code)
SELECT 'Armenia', 'Армения', 'am'
WHERE NOT EXISTS (SELECT 1 FROM country WHERE code = 'am');

INSERT INTO country (name, name_ru, code)
SELECT 'Croatia', 'Хорватия', 'hr'
WHERE NOT EXISTS (SELECT 1 FROM country WHERE code = 'hr');

INSERT INTO country (name, name_ru, code)
SELECT 'Albania', 'Албания', 'al'
WHERE NOT EXISTS (SELECT 1 FROM country WHERE code = 'al');

-- Добавляем новую колонку country_id в таблицу server, если она не существует
ALTER TABLE servers
ADD COLUMN IF NOT EXISTS country_id INTEGER REFERENCES country(id);

-- Устанавливаем значения country_id для существующих серверов
UPDATE servers SET country_id = (SELECT id FROM country WHERE name = servers.country OR code = servers.country LIMIT 1)
WHERE country IS NOT NULL;
