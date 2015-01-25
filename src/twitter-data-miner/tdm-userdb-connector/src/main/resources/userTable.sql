CREATE TABLE IF NOT EXISTS twitterUsers (
	userId BIGINT NOT NULL,
	screenName VARCHAR(255) NOT NULL,
	name VARCHAR(255) NOT NULL,
	location VARCHAR(255),
	statusesCount INTEGER,
	followersCount INTEGER,
	language VARCHAR(255),
	favoritesCount INTEGER,
	retweetsCount INTEGER,
	friendsCount INTEGER,
	collectedTweetsCount INTEGER,
	PRIMARY KEY(userId)
)