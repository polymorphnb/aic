package ac.at.tuwien.tdm.results;

public class DirectInterestResult {
	private int interest;
	private String topic;
	private Long topicID;
	
	public DirectInterestResult(int interest, String topic, long topicID) {
		this.interest = interest;
		this.topic = topic;
		this.topicID = topicID;
	}

	public int getInterest() {
		return this.interest;
	}
	
	public String getTopic() {
		return this.topic;
	}

	public Long getTopicID() {
		return this.topicID;
	}
	
	
}
