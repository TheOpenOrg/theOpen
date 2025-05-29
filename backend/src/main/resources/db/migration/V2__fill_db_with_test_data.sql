-- V2__fill_tables.sql

insert into users (tg_id, name)
select 1000 + g, concat('User_', g)
from generate_series(1, 50) as g;

insert into servers (name, country, api_url, api_token)
select concat('Server_', g),
       case when g % 5 = 0 then 'USA'
            when g % 5 = 1 then 'Germany'
            when g % 5 = 2 then 'France'
            when g % 5 = 3 then 'Netherlands'
            else 'Singapore' end,
       concat('http://192.168.1.', g, ':9898'),
       concat('token_', g)
from generate_series(1, 50) as g;

insert into providers (name)
select concat('Provider_', g)
from generate_series(1, 50) as g;

insert into config (server_id, uid, month_amount, buy_time, is_active, payment_link, payment_status, is_trial, extra_config)
select
    (select id from servers order by id limit 1 offset (g % 50)),
    (select id from users order by id limit 1 offset ((g * 3) % 50)),
    (g % 12) + 1,
    now() - (g || ' days')::interval,
    (g % 2 = 0),
    concat('https://pay.example.com/', g),
    case when g % 3 = 0 then 'paid' when g % 3 = 1 then 'pending' else 'trial' end,
    (g % 10 = 0),
    concat('extra_config_', g)
from generate_series(1, 50) as g;