package ac.at.tuwien.tdm.docstore;

public interface DocStoreConnector {
	public void connect();
	public void createTopicCollection();
	public void createUserTweetCollection();
	public void createAdsCollection();
	public void dropDatabase();
	public void getInterestsForUsers(int interestThreshold);
}
