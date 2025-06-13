create type member_status as enum ('active', 'inactive', 'left');

create table if not exists project_members(
    id serial primary key,
    project_id integer not null,
    user_id integer not null,
    role_id integer not null,
    joined_at timestamp default current_timestamp,
    left_at timestamp,
    constraint fk_project_members_project foreign key (project_id) references projects(id),
    constraint fk_project_members_user foreign key (user_id) references users(id),
    constraint fk_project_members_role foreign key (role_id) references roles(id),
    constraint uk_project_user unique (project_id, user_id),
    status member_status default 'active',
    is_deleted boolean default false,
    deleted_at timestamp
);