create table if not exists sub_tasks (
    id serial primary key,
    task_id integer not null,
    task varchar(255) not null,
    isDone boolean default false,
    created_at timestamp default current_timestamp,
    updated_at timestamp default current_timestamp,
    constraint fk_sub_task_task_id foreign key (task_id) references tasks(id) on delete cascade
);

create table if not exists team_sub_tasks (
    id serial primary key,
    team_task_id integer not null,
    task varchar(255) not null,
    isDone boolean default false,
    created_at timestamp default current_timestamp,
    updated_at timestamp default current_timestamp,
    constraint fk_team_sub_task_task_id foreign key (team_task_id) references team_tasks(id) on delete cascade
);