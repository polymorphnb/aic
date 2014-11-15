package at.ac.tuwien.aic.tda.json.twitter;

import java.util.List;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown=true)
public class TweetUserMentions {

  @JsonProperty("indices") 
  private List<Integer> indices;
  
  @JsonProperty("id_str") 
  private String id_str;
  
  @JsonProperty("screen_name") 
  private String screen_name;
  
  @JsonProperty("name") 
  private String name;
  
  @JsonProperty("id") 
  private Long id;

}
