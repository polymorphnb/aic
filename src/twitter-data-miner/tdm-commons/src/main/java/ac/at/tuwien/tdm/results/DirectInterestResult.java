package ac.at.tuwien.tdm.results;

public class DirectInterestResult {
	int interest;
	private String topic;
	private Long topicID;
	
	public DirectInterestResult(int interest, String topic, long topicID) {
		this.interest = interest;
		this.topic = topic;
		this.topicID = topicID;
	}


	public String getTopic() {
		return this.topic;
	}

	public Long getTopicID() {
		return this.topicID;
	}
	
	
}
