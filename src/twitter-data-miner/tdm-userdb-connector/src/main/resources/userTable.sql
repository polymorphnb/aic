CREATE TABLE IF NOT EXISTS twitterUsers (
	id BIGINT NOT NULL AUTO_INCREMENT,
	screenName VARCHAR(255) NOT NULL,
	name VARCHAR(255) NOT NULL,
	location VARCHAR(255),
	statusesCount INTEGER,
	followersCount INTEGER,
	language VARCHAR(255),
	favoritesCount INTEGER,
	friendsCount INTEGER,
	followerUserIds VARCHAR,
	friendsUserIds VARCHAR,
	PRIMARY KEY(id)
)