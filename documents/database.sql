create table tables(
  id SERIAL,
  name varchar(100),
  primary key(id)
);

create table seats(
  id SERIAL,
  role varchar(20),
  primary key(id)
);

create table table_seat(
  table_id int,
  seat_id int,
  nro int,
  primary key(table_id,nro),
  foreign key(table_id) references tables(id),
  foreign key(seat_id) references seats(id)
);

create table gamemodes(
  id SERIAL,
  gamemode varchar(100),
  currency varchar(10),
  minstake DOUBLE PRECISION,
  maxstake DOUBLE PRECISION,
  primary key(id)
);

create table actions(
  id SERIAL,
  action varchar(50),
  primary key(id)
);

create table players(
  id SERIAL,
  name varchar(100),
  primary key(id)
);

create table hands(
  id SERIAL,
  table_id int,
  gamemode_id int,
  siteid DOUBLE PRECISION,
  name varchar(100),
  date timestamp,
  primary key(id),
  foreign key(table_id) references tables(id),
  foreign key(gamemode_id) references gamemodes(id)
);

create table turns(
  id SERIAL,
  hand_id int,
  phase varchar(50),
  communitycards text[],
  primary key(id),
  foreign key(hand_id) references hands(id)
);

create table hand_player(
  id SERIAL,
  seat_nro int,
  hand_id int,
  player_id int,
  cards text[],
  primary key(id),
  foreign key(hand_id) references hands(id),
  foreign key(player_id) references players(id)
);

create table turn_player_action(
  id SERIAL,
  player_id int,
  action_id int,
  turn_id int,
  amount DOUBLE PRECISION,
  primary key(id),
  foreign key(turn_id) references turns(id),
  foreign key(action_id) references actions(id),
  foreign key(player_id) references players(id)
);

INSERT into tables(name) VALUES('McNaught');
INSERT into tables(name) VALUES('McDolan');

INSERT into seats(role) VALUES ('btn');
INSERT into seats(role) VALUES ('sb');
INSERT into seats(role) VALUES ('bb');
INSERT into seats(role) VALUES ('normal');

INSERT into table_seat VALUES (1,1,1);
INSERT into table_seat VALUES (1,2,2);
INSERT into table_seat VALUES (1,3,3);
INSERT into table_seat VALUES (1,4,4);
INSERT into table_seat VALUES (1,4,5);
INSERT into table_seat VALUES (1,4,6);
INSERT into table_seat VALUES (2,1,1);
INSERT into table_seat VALUES (2,2,2);
INSERT into table_seat VALUES (2,3,3);
INSERT into table_seat VALUES (2,4,4);
INSERT into table_seat VALUES (2,4,5);
INSERT into table_seat VALUES (2,4,6);
INSERT into table_seat VALUES (2,4,7);
INSERT into table_seat VALUES (2,4,8);
INSERT into table_seat VALUES (2,4,9);

INSERT INTO gamemodes(gamemode, currency, minstake, maxstake) VALUES ('Hold''em No Limit','USD',0.01,0.02);

--Could be integers instead of varchar--
INSERT INTO actions(action) VALUES ('fold');
INSERT INTO actions(action) VALUES ('call');
INSERT INTO actions(action) VALUES ('raise');
INSERT INTO actions(action) VALUES ('check');

INSERT into players(name) VALUES ('yakka34');
INSERT into players(name) VALUES ('Redvin33');

--Check if java can return timestamp that will work directly with psql--
INSERT into hands(table_id, gamemode_id, siteid, name, date) VALUES (1,1,171235453897,'PokerStars Zoom Hand','2004-10-19 10:23:54');

INSERT into turns(hand_id, phase) VALUES (1,'preflop');
INSERT into turns(hand_id, phase, communitycards) VALUES (1,'flop','{"7c","Ac","2c"}');
INSERT into turns(hand_id, phase, communitycards) VALUES (1,'turn','{"7c","Ac","2c","Ah"}');

INSERT into hand_player(seat_nro, hand_id, player_id, cards) VALUES (1,1,1,'{"Ad","Kd"}');
INSERT into hand_player(seat_nro, hand_id, player_id, cards) VALUES (2,1,2,'{"Qd","Jd"}');

INSERT INTO turn_player_action(player_id, action_id, turn_id, amount) VALUES (1,1,1,0.0);
INSERT INTO turn_player_action(player_id, action_id, turn_id, amount) VALUES (2,2,1,5.0);
INSERT INTO turn_player_action(player_id, action_id, turn_id, amount) VALUES (2,4,2,0.0);
