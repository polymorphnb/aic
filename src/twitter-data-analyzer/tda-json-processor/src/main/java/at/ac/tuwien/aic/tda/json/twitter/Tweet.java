package at.ac.tuwien.aic.tda.json.twitter;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown=true)
public class Tweet {
  
  @JsonProperty("text") 
  private String text;
  
  @JsonProperty("id_str") 
  private String id_str;
  
  @JsonProperty("coordinates") 
  private Object coordinates;
  
  @JsonProperty("in_reply_to_user_id") 
  private Long in_reply_to_user_id;
  
  @JsonProperty("favorited") 
  private Boolean favorited;
  
  @JsonProperty("in_reply_to_status_id_str") 
  private String in_reply_to_status_id_str;
  
  @JsonProperty("source") 
  private String source;
  
  @JsonProperty("entities") 
  private TweetEntities entities;
  
  @JsonProperty("in_reply_to_screen_name") 
  private String in_reply_to_screen_name;
  
  @JsonProperty("truncated") 
  private Boolean truncated;
  
  @JsonProperty("in_reply_to_user_id_str") 
  private String in_reply_to_user_id_str;
  
  @JsonProperty("place") 
  private TweetPlace place;
  
  @JsonProperty("contributors") 
  private String contributors;
  
  @JsonProperty("retweeted") 
  private Boolean retweeted;
  
  @JsonProperty("in_reply_to_status_id") 
  private Long in_reply_to_status_id;
  
  @JsonProperty("user") 
  private TwitterUser user;
  
  @JsonProperty("id") 
  private Long id;
  
  @JsonProperty("created_at") 
  private String created_at;
  
  @JsonProperty("geo") 
  private Object geo;
  
  @JsonProperty("retweet_count") 
  private Integer retweet_count;
  
  public String getId_str() {
    return id_str;
  }

  
  public void setId_str(String id_str) {
    this.id_str = id_str;
  }

}
