create table user
(
    id          int auto_increment
        primary key,
    nick_name   varchar(64)  not null comment '昵称',
    ext_info    json         null comment '扩展信息',
    create_time timestamp    not null default CURRENT_TIMESTAMP,
    update_time timestamp    not null default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP,
    constraint user_nick_name_uindex
        unique (nick_name)
)
    comment '用户';

create table contest
(
    id              int auto_increment
        primary key,
    type            varchar(32) not null comment '类型',
    status          varchar(16) not null comment '状态',
    create_group_id bigint      null,
    create_time     timestamp   not null default CURRENT_TIMESTAMP,
    update_time     timestamp   not null default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP
)
    comment '比赛';

create table contest_end
(
    id          int auto_increment
        primary key,
    contest_id  int            not null,
    user_id     int            not null,
    end_point   decimal(10, 2) not null comment '结算点数',
    elo_change  decimal(10, 2) null comment 'elo变化',
    create_time timestamp      not null default CURRENT_TIMESTAMP,
    update_time timestamp      not null default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP,
    constraint contest_end_contest_id_user_id_uindex
        unique (contest_id, user_id)
)
    comment '比赛结算';

create table contest_record
(
    id             int auto_increment
        primary key,
    contest_id     int         not null,
    direction      varchar(16) not null comment 'direction',
    record_user_id int         not null comment '用户id',
    point          int         not null comment '点数',
    create_time    timestamp   not null default CURRENT_TIMESTAMP,
    update_time    timestamp   not null default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP,
    constraint contest_record_contest_id_direction_uindex
        unique (contest_id, direction)
)
    comment '比赛记录';

create table elo
(
    id          int auto_increment
        primary key,
    user_id     int                            not null,
    type        varchar(16)                    null,
    elo         decimal(10, 2) default 2000.00 not null,
    create_time timestamp                      not null default CURRENT_TIMESTAMP,
    update_time timestamp                      not null default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP,
    constraint elo_type_user_id_uindex
        unique (type, user_id)
)
    comment 'elo表';
