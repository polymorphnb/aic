aic
===

AIC Twitter Data Analyzer



Installation:

We assume you are running a current version of ubuntu.

1. Check out the source code:
	mkdir src 
	cd src
    git clone https://github.com/polymorphnb/aic.git

2. Install dependencies
   #new repository for Java 8 and neo4j
   sudo su
   wget -O - http://debian.neo4j.org/neotechnology.gpg.key | apt-key add -
   echo 'deb http://debian.neo4j.org/repo stable/' > /etc/apt/sources.list.d/neo4j.list
   add-apt-repository ppa:webupd8team/java
   apt-get update
   apt-get install oracle-java8-installer neo4j mongodb maven tomcat7

3. compile via maven
   cd $HOME/src/aic/src/twitter-data-miner
   mvn install -DskipTests
   
   This will create two standalone executable jar-Files. One of them is used to collect data from Twitter while the other is used to process the collected data files.
   
4. The TwitterCollector
   The executable jar-File for the TwitterCollector is located in src/twitter-data-miner/tdm-file-dumper/target. 
   There are two files created via maven where TwitterTweetCollector-jar-with-dependencies.jar is the standalone file that is used to collect the data.
   Since the Twitter-API requires authentication to use various of their services. The authentication tokens are loaded automatically by the TwitterConnector and several of them 
   can be used at once. The authentication file is located in src/tdm-twitter-connector/src/main/resources and can be found under the name twitterAuthentication.txt. If new authentication 
   credentials are to be used this file needs to be modified. After modification of this file the source has to be rebuilt since it is read as an resource from within the jar file (mvn clean install -DskipTests).
   
   The TwitterCollector itself uses a file to configure the searchterms that are to be used during the data collection. These searchterms are specified in a file with the name searchTerms.txt where each searchterm is 
   represented in a separate line. The structure is as follows: searchterm;true/false. The searchterm is separated from an additional configuration value via a semicolon. The second value specifies whether it should only be 
   searched in hashtags (true) or in the entire text (false).
   There are two options to place the file searchTerms.txt. First, it can be placed in src/tdm-file-dumper/src/main/resources and then it is directly loaded from the classpath. Second, the file can be placed in the same 
   folder where the executable jar TwitterTweetCollector-jar-with-dependencies.jar is located. Since the first options is rather limited and requires a rebuilt after each change to the searchTerms.txt file (mvn clean install -DskipTests) 
   the second options is preferred. Please keep in mind that if the a searchTerms.txt file is located within the jar (first option), this file will always be loaded first.
   
   The searchTerms.txt file is automatically loaded and if none can be found the application will terminate immediatly. The TwitterCollector processes each searchTerm and up to 300000 Tweets are collected for each searchTerm. This value 
   can be adjusted via the constant DEFAULT_MAX_RESULTS_PER_TWEET_SEARCH in the ac.at.tuwien.tdm.twitter.connector.TwitterConnectorConstants class in src/tdm-twitter-connector. This value is fixated and if it is changed the source code 
   has to be rebuilt (mvn clean install -DskipTests).
   The TwitterCollector creates a folder dist in the same folder as it is located and thus write permission is necessary in the folder. In this dist folder folders for the tweets and users are created. For each searchTerm a single file 
   is created with the found tweets and the corresponding users. 
  
5. running the TwitterCollector
   The TwitterCollector can be run via the following command:
   
   java -jar TwitterTweetCollector-jar-with-dependencies.jar
  
6. The TwitterProcessor
   The TwitterProcessor is used to process the collected data from the TwitterCollector. It can be found (after a mvn clean install -DskipTests) under src/tdm-processor/target and is called TwitterTweetProcessor-jar-with-dependencies.jar It uses the same dist folder where the resulting files are located. Since the TwitterProcessor uses different kind of technologies 
   several configuration values can be set. During the processing of the files a graph based database (neo4j) is created where all users and their interactions as well as interests in topics are stored. Furthermore, 
   topics that classify the searchTerms are organized via a document store (mongoDB). In addition the user meta-data is stored in a traditional relational database (H2).
   The neo4j database as well as the user database (H2) are in memory databases and as such no connection to external services are necessary. However, the document store is based 
   on the mongoDB service and thus configuration values are necessary.
   
   The configuration file for the graph database is located in the folder src/tdm-neo4j-connector/src/main/resources and is called neo4j.properties. It allows the specification of 
   several values such as mapped_memory or if the auto_index should be used. An example for such a configuration file is already placed in the src/main/resources folder. Changes to this 
   file should be made very carefully since changing values such as the node_keys_indexable may require severe changes to the source code.
   
   The user database (H2) is automatically created (or connected to) on startup. The configuration values are located in the class UserDBConnector of the src/tdm-userdb-connector project. Values for
   DB_NAME, PATH_TO_DB_DEFAULT and others can be changed here. In addition a file containing the database structure can be found in src/main/resources of the tdm-userdb-connector project (userTable.sql). Changes to 
   this file should be made very carefully as it requires changes to the source code as well.
   
   Last but not least the configuration for the document store (mongoDB) is located in the tdm-docstore-connector project. The class DocStoreConnectorImpl contains the 
   configuration values for the document store. It allows changes to the database name and location (since it connects to an external service). Furthermore, there are two files located in src/main/resources where topics 
   and ads are stored. These file hold a json structure which is loaded by the DocStoreConnector and then processed (it builds the document store based on these values). The file topics.json holds the topics and the relationship 
   to its keywords and thus classifies keywords to topics. Ads.json holds the corresponding ads to the topics.
   
   There are already correct configuration values in place for each of the three database systems and the TwitteProcessor can be run using them. However, there is also the option to specify a separate configuration file config.properties 
   that holds additional configuration values. This file should be placed in the same folder as the executable jar-File and is then loaded automatically. 
   
   An example for the configuration values are shown here:
   
   neo4j.path=./graphDBPerConfig
   neo4j.properties=./neo4jPerConfig.properties

   userdb.path=./userDBPerConfig
   userdb.table=./userTablePerConfig.sql
   
   These values overwrite the configuration values mentioned above. neo4j.path specifies where the resulting graph database should be placed and neo4j.properties specifies where the configuration file for neo4j is located (compare src/tdm-neo4j-connector/src/main/resources/neo4j.properties).
   Please keep in mind that these values have to point to a correct location (with write permission) because otherwise the TwitterProcessor will terminate immediatly.
   
7. running the TwitterProcessor
	First the mongoDB service has to be started. If default values are used it can be started via:
	mongod
	java -jar TwitterTweetProcessor-jar-with-dependencies.jar [rebuild]
	
	Depending on the presence of a config.properties the configuration is loaded from the configuration files within the jar file or 
	from the locations specified in the configuration file. The argument [rebuild] specifies whether all databases should be reset before processing the collected data.
	
	The TwitterProcessor looks for a folder called dist in the same folder and processes these files. After a files is successfully processed it is moved to a folder called processed in the corresponding tweets or users folder. 
	This allows to run the TwitterProcessor multiple times without already processed files being processed again.
	
	After completion of the TwitterProcessr the document store is populated and the populated user database and graph database can be found under the location of its configuration.
	
8. Setting up Tomcat
	The Web-Frontend is built as a JSF application and can be run in Tomcat. However, since the databases are (and should not) be included in the resulting war-archive, their location has to specified separately. For this purpose a 
	file with the configuration values (config.properties) has to be placed in the classpath of Tomcat. The config.properties file has the same format as described in section 6 and should contain absolute file paths to the location of the 
	graph databse and the user database. An example for such a file is shown here:
	
	neo4j.path=C:\\Users\\user\\TwitterBigDataAnalyzer\\databases\\graphDB
	neo4j.properties=C:\\Users\\user\\TwitterBigDataAnalyzer\\databases\\neo4jPerConfig.properties

	userdb.path=C:\\Users\\user\\TwitterBigDataAnalyzer\\databases\\userDB
	
	To make Tomcat aware of this config file a minor change to the catalina.properties file from Tomcat is necessary. 
	Edit $TOMCAT_HOME/conf/catalina.properties and find the line with shared.loader. Add the following value:
	
	shared.loader=${catalina.base}/lib/addClasspathFolder
	
	Now the config.properties file can be placed in $TOMCAT_HOME/lib/addClasspathFolder so that it can be loaded from the web application. 
	Afterwards the tomcat server has to be restarted.
	
9. Creating the Web-Frontend
	The web application can be built via maven in the src/twitter-data-analyzer folder:
	
	mvn clean install
	
	This creates a war-archive that is ready for deployment. 
	The war file can be found under:
	
	src/twitter-data-analyzer/tda-parent/tda-webapp/target
	
	and is called:
	
	tda-webapp-1.0.0-SNAPSHOT.war
	
10. Deploying the Web-Frontent

	Copy the file tda-webapp-1.0.0-SNAPSHOT.war to $TOMCAT_HOME/webapps
	
	cp tda-webapp-1.0.0-SNAPSHOT.war $TOMCAT_HOME/webapps
	
11. Using the Web-Frontend	
	After deploying the web application it can be used. Assuming that there are default values for Tomcat in place it can be called via:
	
	http://localhost:8080/tda-webapp-1.0.0-SNAPSHOT/
	
	The Web-Frontend specifies several formulars that allow to parametrize queries that operate on the twitter data. The available queries 
	allow to find the most influental users, the frequency of posts from users for a specific topic, find direct interests for users (e.g. tweets) 
	and find potential interests via other users for a user.
	
	
