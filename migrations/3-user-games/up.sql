-- SQL ALTER statements for database migration
CREATE TABLE user_games (
       id SERIAL,
       user_id INT,
       game_id INT
);
