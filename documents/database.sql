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

create table hand_player_seat(
  id SERIAL,
  seat_id int,
  hand_id int,
  player_id int,
  cards text[],
  primary key(id),
  foreign key(hand_id) references hands(id),
  foreign key(seat_id) references seats(id),
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
