CREATE TABLE IF NOT EXISTS twitterUsers (
	id BIGINT NOT NULL,
	screenName VARCHAR(255) NOT NULL,
	name VARCHAR(255) NOT NULL,
	location VARCHAR(255),
	statusesCount INTEGER,
	followers INTEGER,
	PRIMARY KEY(id)
)