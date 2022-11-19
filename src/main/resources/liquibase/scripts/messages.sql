--liquibase formatted sql


-- changeSet brakovchen:1
CREATE TABLE notification_task(
    id INTEGER,
    notifications varchar,
    date timestamp
);




