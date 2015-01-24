package ac.at.tuwien.tdm.commons.pojo;

public class Ad {
	private int ID;
	private int topicID;
	private String name;
	private String content;
	
	public Ad(int iD, int topicID, String name, String content) {
		this.ID = iD;
		this.topicID = topicID;
		this.name = name;
		this.content = content;
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
}
