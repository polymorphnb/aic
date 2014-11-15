package at.ac.tuwien.aic.tda.json.twitter;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown=true)
public class TweetPlace {
  
  @JsonProperty("country") 
  private String country;
  
  @JsonProperty("place_type") 
  private String place_type;
  
  @JsonProperty("url") 
  private String url;
  
  @JsonProperty("country_code") 
  private String country_code;
  
  @JsonProperty("attributes") 
  private Object attributes;
  
  @JsonProperty("full_name") 
  private String full_name;
  
  @JsonProperty("bounding_box") 
  private Object bounding_box;
  
  @JsonProperty("name") 
  private String name;
  
  @JsonProperty("id") 
  private String id;
  
}
