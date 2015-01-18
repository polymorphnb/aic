package ac.at.tuwien.tdm.docstore;

import at.ac.tuwien.aic.Neo4JConnector;

import java.util.List;

public interface DocStoreConnector {
	public void connect();
	public void addTopicToUser(String user, String topic);
	public void createTopicCollection();
	public void createUserTweetCollection();
	public void createAdsCollection();
	public void dropDatabase();
	public void getInterestsForUsers(int interestThreshold, Neo4JConnector neo4jdb);
	public List<Long> retrieveTopics();
	public Long getTopicIDForKeyword(String keyword);
}
