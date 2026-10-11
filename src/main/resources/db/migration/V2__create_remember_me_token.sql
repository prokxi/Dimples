-- Table and column names are fixed by Spring Security's JdbcTokenRepositoryImpl.
-- username holds the golfer's email, so it allows up to 254 characters.
create table persistent_logins (
    series    varchar(64)  primary key,
    username  varchar(254) not null,
    token     varchar(64)  not null,
    last_used timestamptz  not null
);

create index persistent_logins_username_idx on persistent_logins (username);
