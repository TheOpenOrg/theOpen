create table users
(
    id      bigserial
        primary key,
    tg_id bigint,
    name    varchar(255)
);

create table servers(
    id bigserial primary key,
    name varchar(512),
    country varchar(512),
    api_url varchar(1024), -- URL API ovpn-server.py, например http://1.2.3.4:9898
    api_token varchar(255) -- токен для авторизации к ovpn-server.py
);

create table config(
    id bigserial primary key,
    server_id bigint references public.servers,
    uid bigint references public.users,
    month_amount int,
    buy_time timestamp(6),
    is_active bool,
    payment_link text,
    payment_status text,
    is_trial bool default false,
    extra_config text
);

create table providers(
    id bigserial primary key,
    name varchar(255)
);
