create table golfer (
    id                   uuid        primary key,
    email                text,
    password_hash        text,
    display_name         text,
    distance_unit        text        not null check (distance_unit in ('METERS', 'YARDS')),
    status               text        not null check (status in ('ACTIVE', 'DELETED')),
    role                 text        not null check (role in ('GOLFER', 'OPERATOR')),
    must_change_password boolean     not null,
    created_at           timestamptz not null,
    deleted_at           timestamptz,
    version              int         not null,
    -- Personal data exists only while the account is active (SPEC-1, section 4.7).
    constraint golfer_personal_data_by_status check (
        (status = 'ACTIVE' and email is not null and password_hash is not null and display_name is not null
            and deleted_at is null)
        or (status = 'DELETED' and email is null and password_hash is null and display_name is null
            and deleted_at is not null)
    )
);

-- A deleted golfer frees the email for a new registration.
create unique index golfer_active_email_key on golfer (email) where status = 'ACTIVE';

create table handicap_index_entry (
    id           uuid         primary key,
    golfer_id    uuid         not null references golfer (id),
    value        numeric(3,1) not null check (value between -10.0 and 54.0),
    effective_on date         not null,
    recorded_at  timestamptz  not null,
    -- Also serves as the index on the foreign key golfer_id.
    constraint handicap_index_entry_golfer_effective_on_key unique (golfer_id, effective_on)
);
