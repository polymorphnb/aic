package at.tuwien;

import ac.at.tuwien.tdm.commons.pojo.Ad;
import ac.at.tuwien.tdm.commons.pojo.User;
import ac.at.tuwien.tdm.commons.pojo.UserFrequency;
import ac.at.tuwien.tdm.docstore.DocStoreConnectorImpl;
import ac.at.tuwien.tdm.results.DirectInterestResult;
import ac.at.tuwien.tdm.results.IndirectInterestResult;
import ac.at.tuwien.tdm.results.InfluenceResult;
import ac.at.tuwien.tdm.userdb.UserDBConnector;

import at.ac.tuwien.aic.Neo4JConnector;
import at.ac.tuwien.aic.Neo4JConnectorImpl;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

@ManagedBean(name = "user")
@SessionScoped
public class UserBean {

  static final Logger LOGGER = Logger.getLogger(UserBean.class);

  private String name;
  private String user;

  private Integer countInfluentalUser;
  private List<InfluenceResult> influentalUsers;
  private String influenceFollowersWeight;
  private String influenceFavouritesWeight;
  private String influenceRetweetsWeight;
  
  private Neo4JConnector neo4j = null;
  private UserDBConnector userDB = null;

  //Parameter countInfluentalUser
  public void searchMostInfluentalUser() throws IOException {
    //Initialize Logger
    BasicConfigurator.configure();

    LOGGER.info("search most influental user");
    
    int favo;
    if(null!=influenceFavouritesWeight && !influenceFavouritesWeight.isEmpty()){
    	favo = Integer.parseInt(influenceFavouritesWeight);
    }
    else {
    	favo = 1;
    }
    
    int foll;
    if(null!=influenceFollowersWeight && !influenceFollowersWeight.isEmpty()){
    	foll = Integer.parseInt(influenceFollowersWeight);
    }
    else {
    	foll = 1;
    }
    
    int retweet;
    if(null!=influenceRetweetsWeight && !influenceRetweetsWeight.isEmpty()){
    	retweet = Integer.parseInt(influenceRetweetsWeight);
    }
    else {
    	retweet = 1;
    }

    this.initialize();
    List<InfluenceResult> users = this.userDB.calcInfluenceAll(foll, retweet, favo, countInfluentalUser);

    if (null != users) {
      LOGGER.info(users.size());
      influentalUsers = users;
    }

    //show only given amount
    if (influentalUsers.size() > countInfluentalUser) {
      influentalUsers = influentalUsers.subList(0, countInfluentalUser);
    }

    //For Logger
    BasicConfigurator.resetConfiguration();

  }

  public void deleteMostInfluentalUsers() {
    influentalUsers = new ArrayList<InfluenceResult>();
    countInfluentalUser = null;
  }

  private Integer countFrequencedUser;
  private List<UserFrequency> frequencedUsers;
  private String topic;

  //Parameter countInfluentalUser
  public void searchMostFrequencedUser() throws FileNotFoundException, IOException {
    //For Logger
    BasicConfigurator.configure();

    this.initialize();
    List<User> users = this.userDB.getUsers();
    this.userDB.disconnect();

    DocStoreConnectorImpl docstore = new DocStoreConnectorImpl();
    frequencedUsers = new ArrayList<UserFrequency>();
    for (int i = 0; i < countFrequencedUser; i++) {
      User user = users.get(i);
      double calc_tf_idf_UserTopic = docstore.calc_tf_idf_UserTopic(user.getId(), Long.parseLong(topic));
      UserFrequency uf = new UserFrequency();
      uf.setUsername(user.getScreenName());
      uf.setId(user.getId());
      uf.setFrequence(calc_tf_idf_UserTopic);
      frequencedUsers.add(uf);
    }

    //show only given amount
    if (frequencedUsers.size() > countFrequencedUser) {
      frequencedUsers = frequencedUsers.subList(0, countFrequencedUser);
    }

    BasicConfigurator.resetConfiguration();
  }

  public void deleteMostFrequencedUsers() {
    frequencedUsers = new ArrayList<UserFrequency>();
    countFrequencedUser = null;
  }

  private String userExistingInterests;
  private Integer maximalExistingInterests;
  private List<Ad> existingInterests;

  //Parameter countInfluentalUser
  public void searchExistingInterests() throws IOException {
    //		Initialize Logger
    BasicConfigurator.configure();
    
    this.initialize();
    this.neo4j.startTransaction();
    DocStoreConnectorImpl docstore = new DocStoreConnectorImpl();
    List<DirectInterestResult> directInterestsForUser =  this.neo4j.getDirectInterestsForUser(
        Long.parseLong(userExistingInterests), maximalExistingInterests, docstore);
    this.neo4j.closeTransaction();
    setExistingInterests(new ArrayList<Ad>());

    List<ac.at.tuwien.tdm.commons.pojo.Ad> retrieveAds = docstore.retrieveAds();

    if (null != directInterestsForUser) {
      for (DirectInterestResult di : directInterestsForUser) {
        for (Ad ad : retrieveAds) {
          if (ad.getTopicID() == di.getTopicID().intValue()) {
            existingInterests.add(ad);
          }
        }
      }
    }

    //show only given amount
    if (existingInterests.size() > maximalExistingInterests) {
      existingInterests = existingInterests.subList(0, maximalExistingInterests);
    }

    BasicConfigurator.resetConfiguration();
  }

  public void deleteExistingInterests() {
    existingInterests = new ArrayList<Ad>();
    maximalExistingInterests = null;
    userExistingInterests = "";
  }

  private String userPotentialInterests;
  private Integer maximalPotentialInterests;
  private List<Ad> potentialInterests;

  //Parameter countInfluentalUser
  public void searchPotentialInterests() throws IOException {
    BasicConfigurator.configure();
    //		if(null==influentalUsers){
    
    this.initialize();
    this.neo4j.startTransaction();
    DocStoreConnectorImpl docstore = new DocStoreConnectorImpl();
    List<IndirectInterestResult> directInterestsForUser =  this.neo4j.getIndirectInterestsForUser(
        Long.parseLong(userPotentialInterests), 5, maximalPotentialInterests, docstore);
    this.neo4j.closeTransaction();
    setExistingInterests(new ArrayList<Ad>());

    List<ac.at.tuwien.tdm.commons.pojo.Ad> retrieveAds = docstore.retrieveAds();

    if (null != directInterestsForUser) {
      for (IndirectInterestResult di : directInterestsForUser) {
        for (Ad ad : retrieveAds) {
          if (ad.getTopicID() == di.getTopicID().intValue()) {
            potentialInterests.add(ad);
          }
        }
      }
    }
    //show only given amount
    if (potentialInterests.size() > maximalPotentialInterests) {
      potentialInterests = potentialInterests.subList(0, maximalPotentialInterests);
    }

    BasicConfigurator.resetConfiguration();
  }

  public void deletePotentialInterests() {
    potentialInterests = new ArrayList<Ad>();
    maximalPotentialInterests = null;
    userPotentialInterests = "";
  }

  public String getUser() {
    return user;
  }

  public void setUser(String user) {
    this.user = user;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Integer getCountInfluentalUser() {
    return countInfluentalUser;
  }

  public void setCountInfluentalUser(Integer countInfluentalUser) {
    this.countInfluentalUser = countInfluentalUser;
  }

  public List<InfluenceResult> getInfluentalUsers() {
    return influentalUsers;
  }

  public void setInfluentalUsers(List<InfluenceResult> influentalUsers) {
    if (null == influentalUsers) {
      influentalUsers = new ArrayList<InfluenceResult>();
    }
    this.influentalUsers = influentalUsers;
  }

  public Integer getCountFrequencedUser() {
    return countFrequencedUser;
  }

  public void setCountFrequencedUser(Integer countFrequencedUser) {
    this.countFrequencedUser = countFrequencedUser;
  }

  public List<UserFrequency> getFrequencedUsers() {
    return frequencedUsers;
  }

  public void setFrequencedUsers(List<UserFrequency> frequencedUsers) {
    if (null == frequencedUsers) {
      frequencedUsers = new ArrayList<UserFrequency>();
    }
    this.frequencedUsers = frequencedUsers;
  }

  public String getUserExistingInterests() {
    return userExistingInterests;
  }

  public void setUserExistingInterests(String userExistingInterests) {
    this.userExistingInterests = userExistingInterests;
  }

  public List<Ad> getExistingInterests() {
    return existingInterests;
  }

  public void setExistingInterests(List<Ad> existingInterests) {
    if (null == existingInterests) {
      existingInterests = new ArrayList<Ad>();
    }
    this.existingInterests = existingInterests;
  }

  public Integer getMaximalExistingInterests() {
    return maximalExistingInterests;
  }

  public void setMaximalExistingInterests(Integer maximalExistingInterests) {
    this.maximalExistingInterests = maximalExistingInterests;
  }

  public void setPotentialInterests(List<Ad> potentialInterests) {
    if (null == potentialInterests) {
      potentialInterests = new ArrayList<Ad>();
    }
    this.potentialInterests = potentialInterests;
  }

  public List<Ad> getPotentialInterests() {
    return potentialInterests;
  }

  public Integer getMaximalPotentialInterests() {
    return maximalPotentialInterests;
  }

  public void setMaximalPotentialInterests(Integer maximalPotentialInterests) {
    this.maximalPotentialInterests = maximalPotentialInterests;
  }

  public String getUserPotentialInterests() {
    return userPotentialInterests;
  }

  public void setUserPotentialInterests(String userPotentialInterests) {
    this.userPotentialInterests = userPotentialInterests;
  }

  public String getInfluenceFollowersWeight() {
	return influenceFollowersWeight;
}

public void setInfluenceFollowersWeight(String influenceFollowersWeight) {
	this.influenceFollowersWeight = influenceFollowersWeight;
}

public String getInfluenceFavouritesWeight() {
	return influenceFavouritesWeight;
}

public void setInfluenceFavouritesWeight(String influenceFavouritesWeight) {
	this.influenceFavouritesWeight = influenceFavouritesWeight;
}

public String getInfluenceRetweetsWeight() {
	return influenceRetweetsWeight;
}

public void setInfluenceRetweetsWeight(String influenceRetweetsWeight) {
	this.influenceRetweetsWeight = influenceRetweetsWeight;
}

public Properties loadProperties() throws IOException {
    Properties prop = new Properties();

    prop.load(this.getClass().getResourceAsStream("/" + ac.at.tuwien.tdm.commons.Constants.CONFIG_FILE_NAME));

    return prop;
  }
  
  public void initializeUserDB() throws IOException {
    if(this.userDB == null) {
      this.userDB = new UserDBConnector(loadProperties().getProperty(UserDBConnector.PATH_USERDB_KEY), false);
    }
  }

  public void initializeNeo4J() throws IOException {
    if(this.neo4j == null) {
      Properties prop = loadProperties();
      String neoPath = prop.getProperty(Neo4JConnector.NEO4J_PATH_KEY);
      String neoProp = prop.getProperty(Neo4JConnector.NEO4J_PATH_PROPERTIES_KEY);
  
      this.neo4j = new Neo4JConnectorImpl(neoPath, neoProp);
      this.neo4j.connect(false);
    }
  }
  
  @PostConstruct
  public void initialize() throws IOException {
    this.initializeNeo4J();
    this.initializeUserDB();
  }
  
  @PreDestroy
  public void cleanup() {
    System.out.println("CLEANUP CALLED");
    this.neo4j.disconnect();
    this.userDB.disconnect();
  }

public String getTopic() {
	return topic;
}

public void setTopic(String topic) {
	this.topic = topic;
}
}
