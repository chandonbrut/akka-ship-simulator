CREATE DATABASE simulator;
\c simulator
CREATE USER simulator WITH PASSWORD 'simulator';
SET ROLE simulator;
create table report(imo text, ts timestamp, latitude real, longitude real);