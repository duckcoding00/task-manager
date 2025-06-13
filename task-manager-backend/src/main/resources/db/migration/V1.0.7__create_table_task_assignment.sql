create table if not exists task_assignments(
    id serial primary key,
    task_id integer not null,
    user_id integer not null,
    assigned_by integer not null,
    assigned_at timestamp default current_timestamp,
    constraint fk_assignments_task foreign key (task_id) references team_tasks(id),
    constraint fk_assignments_user foreign key (user_id) references users(id),
    constraint fk_assignments_assigner foreign key (assigned_by) references users (id),
    constraint uk_task_user_assignment unique (task_id, user_id)
);