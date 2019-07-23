drop table Node if exists;
CREATE TABLE Node (
	name VARCHAR(20),
	active CHAR(1),
	since DATE,
	size decimal,
	id INT
) ;
