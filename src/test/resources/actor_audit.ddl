--<ScriptOptions statementTerminator=";"/>

CREATE TABLE actor_audit (
	audit_id INT NOT NULL,
	operation VARCHAR(45) NOT NULL,
	created_by VARCHAR(45) NOT NULL,
	created_on DATE NOT NULL,
	actor_id INT NOT NULL,
	source_id VARCHAR(45) NOT NULL,
	name VARCHAR(45),
	PRIMARY KEY (audit_id)
) ENGINE=InnoDB;

