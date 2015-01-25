package ac.at.tuwien.tdm.commons.pojo;

public class UserFrequency {

  private String username;
  private Long id;
  private Long topicID;
  private String topic;
  private double frequence;

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }
  
  public Long getTopicID() {
    return topicID;
  }

  public void setTopicID(Long topicID) {
    this.topicID = topicID;
  }

  public double getFrequence() {
    return frequence;
  }

  public void setFrequence(double frequence) {
    this.frequence = frequence;
  }

  public String getTopic() {
    return topic;
  }

  public void setTopic(String topic) {
    this.topic = topic;
  }
}
