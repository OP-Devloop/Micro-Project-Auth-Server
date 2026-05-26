create table app_user
(
    id            bigserial primary key,
    username      varchar(255) UNIQUE NOT NULL,
    password      varchar(255)        NOT NULL,
    role          varchar(255)        NOT NULL,
    date_of_birth date,
    created_by    varchar(255)
);