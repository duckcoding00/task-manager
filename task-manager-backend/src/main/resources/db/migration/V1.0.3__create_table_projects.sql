create type status_project as enum('active', 'inactive', 'archive', 'cancel');

create table if not exists projects(
    id serial primary key,
    name varchar(255) not null,
    description text,
    status status_project not null default 'active',
    start_date date,
    end_date date,
    created_by varchar(255) not null,
    created_at timestamp default current_timestamp,
    updated_at timestamp default current_timestamp,
    constraint fk_projects_creator foreign key (created_by) references users(username),
    is_deleted boolean default false,
    deleted_at timestamp
);