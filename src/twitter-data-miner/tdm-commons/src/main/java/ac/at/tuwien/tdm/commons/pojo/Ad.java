package ac.at.tuwien.tdm.commons.pojo;

public class Ad {
	private int ID;
	private int topicID;
	private String name;
	private String content;
	private String topicName;
	private String indirectDepth;
	
	public Ad(int iD, int topicID, String name, String content) {
		this.ID = iD;
		this.topicID = topicID;
		this.name = name;
		this.content = content;
	}
	
	public Ad(int iD, int topicID, String name, String content, String topicName) {
		this.ID = iD;
		this.topicID = topicID;
		this.name = name;
		this.content = content;
		this.topicName = topicName;
	}
	
	public int getID() {
		return ID;
	}
	public int getTopicID() {
		return topicID;
	}
	public String getName() {
		return name;
	}
	public String getContent() {
		return content;
	}

	public String getTopicName() {
		return topicName;
	}

	public void setTopicName(String topicName) {
		this.topicName = topicName;
	}

	public String getIndirectDepth() {
		return indirectDepth;
	}

	public void setIndirectDepth(String indirectDepth) {
		this.indirectDepth = indirectDepth;
	}
}
