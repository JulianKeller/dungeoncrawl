create table Player(
	pid int,				/*unique ID for player*/
    pname varchar(200),		/*arbitrary player name*/
    pclass varchar(200),	/*player class: Knight, Mage, Archer, or Tank*/
    plevel smallint,		/*player's experience/strength level*/
    
    primary key(pid)
);

create table Item(
	iid int,				/*unique ID for this item*/
    oid int,				/*ID of this item's owner*/
    ieffect varchar(200),	/*this item's effect, e.g. 'Healing'*/
    itype varchar(200),		/*this item's type, e.g. 'Sword' or 'Potion'*/
    imat varchar(200),		/*thie item's material, e.g. 'Leather'*/
    iworldCoordX int,		/*the world coordinates of this item*/
    iworldCoordY int,		/*  both will be -1 for items which don't exist on the world*/
    cursed boolean,
    isIdentified boolean,
    
    primary key(iid),
    foreign key(oid) references player(pid)
    on update cascade on delete cascade
);