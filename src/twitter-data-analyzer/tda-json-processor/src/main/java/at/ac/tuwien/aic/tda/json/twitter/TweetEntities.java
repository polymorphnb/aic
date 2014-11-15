package at.ac.tuwien.aic.tda.json.twitter;

import java.util.List;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown=true)
public class TweetEntities {
  
  @JsonProperty("hashtags") 
  private List<Object> hashtags;
  
  @JsonProperty("urls") 
  private List<Object> urls;
  
  @JsonProperty("url") 
  private String url;
  
  @JsonProperty("expanded_url") 
  private String expanded_url;
  
  @JsonProperty("user_mentions") 
  private List<TweetUserMentions> user_mentions;


}
