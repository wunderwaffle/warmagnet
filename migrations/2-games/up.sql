-- SQL ALTER statements for database migration
CREATE TABLE games (
       id SERIAL,
       name VARCHAR(255),
       data TEXT
);

CREATE TABLE gamelogs (
       id SERIAL,
       game_id INT,
       type VARCHAR(64),
       data TEXT
);
