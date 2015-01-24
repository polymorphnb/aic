package at.tuwien;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

import ac.at.tuwien.tdm.commons.pojo.Ad;
import ac.at.tuwien.tdm.commons.pojo.User;
import ac.at.tuwien.tdm.docstore.DocStoreConnectorImpl;
import ac.at.tuwien.tdm.results.DirectInterestResult;
import ac.at.tuwien.tdm.results.IndirectInterestResult;
import ac.at.tuwien.tdm.results.InfluenceResult;
import ac.at.tuwien.tdm.userdb.UserDBConnector;
import at.ac.tuwien.aic.Neo4JConnector;
import at.ac.tuwien.aic.Neo4JConnectorImpl;


@ManagedBean(name="user")
@SessionScoped
public class UserBean{
	
	
	static final Logger logger = Logger.getLogger(UserBean.class);
	
	private String name;
	private String user;
	
	ac.at.tuwien.tdm.commons.pojo.User user1;
	
	private Integer countInfluentalUser;
	private List<InfluenceResult> influentalUsers;
	
	//Parameter countInfluentalUser
	public void searchMostInfluentalUser() throws IOException{
		//Initialize Logger
		BasicConfigurator.configure();
		
		logger.info("search most influental user");
		
		UserDBConnector userdb = new UserDBConnector(loadProperties().getProperty("userdb.path"), loadProperties().getProperty("userdb.table"));
		List<InfluenceResult> users = userdb.calcInfluenceAll(1, 1, 1, countInfluentalUser);
		
		if(null!=users){
			logger.info(users.size());
			influentalUsers = users;
		}
		
		//show only given amount
		if(influentalUsers.size()>countInfluentalUser){
			influentalUsers = influentalUsers.subList(0, countInfluentalUser);
		}
		
	}
	
	public void deleteMostInfluentalUsers(){
		influentalUsers = new ArrayList<InfluenceResult>();
		countInfluentalUser = null;
	}
	
	
	private Integer countFrequencedUser;
	private List<User> frequencedUsers;
	
	//Parameter countInfluentalUser
	public void searchMostFrequencedUser(){
		BasicConfigurator.configure();
//		if(null==influentalUsers){
		frequencedUsers = new ArrayList<User>();
		frequencedUsers.add(new User(1, "user1", "user1", "Vienna", "German", 100, 100, 100, 100));
		frequencedUsers.add(new User(1, "user2", "user2", "Vienna", "German", 100, 100, 100, 100));
		frequencedUsers.add(new User(1, "user3", "user3", "Vienna", "German", 100, 100, 100, 100));
		frequencedUsers.add(new User(1, "user4", "user4", "Vienna", "German", 100, 100, 100, 100));
		frequencedUsers.add(new User(1, "user5", "user5", "Vienna", "German", 100, 100, 100, 100));

//		}
		
		//show only given amount
		if(frequencedUsers.size()>countFrequencedUser){
			frequencedUsers = frequencedUsers.subList(0, countFrequencedUser);
		}
	}
	
	public void deleteMostFrequencedUsers(){
		frequencedUsers = new ArrayList<User>();
		countFrequencedUser = null;
	}
	
	private String userExistingInterests;
	private Integer maximalExistingInterests;
	private List<Ad> existingInterests;
	
	//Parameter countInfluentalUser
	public void searchExistingInterests() throws IOException{
//		Initialize Logger
		BasicConfigurator.configure();
		
		Neo4JConnectorImpl neo = createNeo4J();
		neo.connect(false);
		neo.startTransaction();
		DocStoreConnectorImpl docstore = new DocStoreConnectorImpl();
		List<DirectInterestResult> directInterestsForUser = neo.getDirectInterestsForUser(Long.parseLong(userExistingInterests), maximalExistingInterests, docstore);
		neo.closeTransaction();
		neo.disconnect();
		setExistingInterests(new ArrayList<Ad>());
		
		List<ac.at.tuwien.tdm.commons.pojo.Ad> retrieveAds = docstore.retrieveAds();
		
		if(null!=directInterestsForUser){
			for (DirectInterestResult di : directInterestsForUser) {
				for (Ad ad : retrieveAds) {
					if(ad.getTopicID() == di.getTopicID().intValue()){
						existingInterests.add(ad);
					}
				}
			}
		}
		
		//show only given amount
		if(existingInterests.size()>maximalExistingInterests){
			existingInterests = existingInterests.subList(0, maximalExistingInterests);
		}
	}
	
	public void deleteExistingInterests(){
		existingInterests = new ArrayList<Ad>();
		maximalExistingInterests = null;
		userExistingInterests = "";
	}
	
	private String userPotentialInterests;
	private Integer maximalPotentialInterests;
	private List<Ad> potentialInterests;
	
	//Parameter countInfluentalUser
	public void searchPotentialInterests() throws IOException{
		BasicConfigurator.configure();
//		if(null==influentalUsers){
		Neo4JConnectorImpl neo = createNeo4J();
		neo.connect(false);
		neo.startTransaction();
		DocStoreConnectorImpl docstore = new DocStoreConnectorImpl();
		List<IndirectInterestResult> directInterestsForUser = neo.getIndirectInterestsForUser(Long.parseLong(userPotentialInterests), 5, maximalPotentialInterests, docstore);
		neo.closeTransaction();
		neo.disconnect();
		setExistingInterests(new ArrayList<Ad>());
		
		List<ac.at.tuwien.tdm.commons.pojo.Ad> retrieveAds = docstore.retrieveAds();
		
		if(null!=directInterestsForUser){
			for (IndirectInterestResult di : directInterestsForUser) {
				for (Ad ad : retrieveAds) {
					if(ad.getTopicID() == di.getTopicID().intValue()){
						potentialInterests.add(ad);
					}
				}
			}
		}
		//show only given amount
		if(potentialInterests.size()>maximalPotentialInterests){
			potentialInterests = potentialInterests.subList(0, maximalPotentialInterests);
		}
	}
	
	public void deletePotentialInterests(){
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
		if(null==influentalUsers){
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

	public List<User> getFrequencedUsers() {
		return frequencedUsers;
	}

	public void setFrequencedUsers(List<User> frequencedUsers) {
		if(null==frequencedUsers){
			frequencedUsers = new ArrayList<User>();
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
		if(null==existingInterests){
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
		if(null==potentialInterests){
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

	public Properties loadProperties() throws IOException{
		Properties prop = new Properties();
		String propFileName = System.getProperty("config.filepath");
 
		InputStream inputStream = new FileInputStream(new File(propFileName));
 
		prop.load(inputStream);
		
		return prop;
	}
	
	public Neo4JConnectorImpl createNeo4J() throws IOException{
		
		Properties prop = loadProperties();
		String neoPath = prop.getProperty("neo4j.path");
		String neoProp = prop.getProperty("neo4j.properties");
		
//		Neo4JConnectorImpl.getInstance().getUserViaCypher(12323940L);
//		Neo4JConnectorImpl neo = new Neo4JConnectorImpl("C:\\Users\\Georg\\Carola\\aic\\test_dbs\\graphdb", "C:\\Users\\Georg\\Carola\\aic\\test_dbs\\neo4j.properties");
		return new Neo4JConnectorImpl(neoPath, neoProp);
	}
}