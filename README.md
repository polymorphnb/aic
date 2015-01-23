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
   apt-get install oracle-java8-installer neo4j mongodb maven

3. compile via maven
   cd $HOME/src/aic/src/twitter-data-miner
   mvn install
