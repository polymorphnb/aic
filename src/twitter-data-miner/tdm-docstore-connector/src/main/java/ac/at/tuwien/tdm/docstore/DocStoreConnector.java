package ac.at.tuwien.tdm.docstore;

import java.util.List;

import ac.at.tuwien.tdm.commons.pojo.Ad;

public interface DocStoreConnector {
	public void connect();
	public void addTopicToUser(Long user, Long topic);
	public void createTopicCollection();
	public void createUserTweetCollection();
	public void createAdsCollection();
	public void dropDatabase();
	public List<Ad> retrieveAds();
	//public void getInterestsForUsers(int interestThreshold, Neo4JConnector neo4jdb);
	public List<Long> retrieveTopics();
	public Long getTopicIDForKeyword(String keyword);
	public double calc_tf_idf_UserTopic(Long userID, Long Topic);
	public String getTopicForID(Long id);
}
