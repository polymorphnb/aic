package ac.at.tuwien.tdm.results;

public class IndirectInterestResult {
	private int interest;
	private String topic;
	private Long topicID;
	private int depth;
	
	public IndirectInterestResult(int interest, String topic, long topicID, int depth) {
		this.interest = interest;
		this.topic = topic;
		this.topicID = topicID;
		this.depth = depth;
	}

	public int getDepth() {
		return this.depth;
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