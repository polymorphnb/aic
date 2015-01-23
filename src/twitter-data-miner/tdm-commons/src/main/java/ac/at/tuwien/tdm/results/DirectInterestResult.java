package ac.at.tuwien.tdm.results;

public class DirectInterestResult {
	private String user;
	private Long userID;
	private String topic;
	private long topicID;
	
	public DirectInterestResult(String user, Long userID, String topic, long topicID) {
		this.user = user;
		this.userID = userID;
		this.topic = topic;
		this.topicID = topicID;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public void setUserID(Long userID) {
		this.userID = userID;
	}

	public void setTopic(String topic) {
		this.topic = topic;
	}

	public void setTopicID(long topicID) {
		this.topicID = topicID;
	}
	
	
}
