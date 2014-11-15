package at.ac.tuwien.aic.tda.json.twitter;

import org.codehaus.jackson.annotate.JsonProperty;


public class TwitterUser {

  @JsonProperty("default_profile_image") 
  private Boolean default_profile_image;
  
  @JsonProperty("profile_use_background_image") 
  private Boolean profile_use_background_image;
  
  @JsonProperty("show_all_inline_media") 
  private Boolean show_all_inline_media;
  
  @JsonProperty("geo_enabled") 
  private Boolean geo_enabled;
  
  @JsonProperty("profile_background_color") 
  private String profile_background_color;
  
  @JsonProperty("id_str") 
  private String id_str;
  
  @JsonProperty("profile_background_image_url") 
  private String profile_background_image_url;
  
  @JsonProperty("favourites_count") 
  private Integer favourites_count;
  
  @JsonProperty("follow_request_sent") 
  private String follow_request_sent;
  
  @JsonProperty("url") 
  private String url;
  
  @JsonProperty("display_url") 
  private String display_url;
  
  @JsonProperty("expanded_url") 
  private String expanded_url;
  
  @JsonProperty("profile_image_url") 
  private String profile_image_url;
  
  @JsonProperty("description") 
  private String description;
  
  @JsonProperty("statuses_count") 
  private Integer statuses_count;
  
  @JsonProperty("notifications") 
  private String notifications;
  
  @JsonProperty("profile_text_color") 
  private String profile_text_color;
  
  @JsonProperty("followers_count") 
  private Integer followers_count;
  
  @JsonProperty("default_profile") 
  private Boolean default_profile;
  
  @JsonProperty("contributors_enabled") 
  private Boolean contributors_enabled;
  
  @JsonProperty("lang") 
  private String lang;
  
  @JsonProperty("profile_sidebar_fill_color") 
  private String profile_sidebar_fill_color;
  
  @JsonProperty("screen_name") 
  private String screen_name;
  
  @JsonProperty("is_translator") 
  private Boolean is_translator;
  
  @JsonProperty("profile_background_tile") 
  private Boolean profile_background_tile;
  
  @JsonProperty("profile_background_image_url_https") 
  private String profile_background_image_url_https;
  
  @JsonProperty("location") 
  private String location;
  
  @JsonProperty("listed_count") 
  private Integer listed_count;
  
  @JsonProperty("friends_count") 
  private Integer friends_count;
  
  @JsonProperty("protected") 
  private Boolean protectedVal;
  
  @JsonProperty("following") 
  private String following;
  
  @JsonProperty("profile_link_color") 
  private String profile_link_color;

  @JsonProperty("name") 
  private String name;
  
  @JsonProperty("verified") 
  private Boolean verified;
  
  @JsonProperty("created_at") 
  private String created_at;
  
  @JsonProperty("profile_sidebar_border_color") 
  private String profile_sidebar_border_color;
  
  @JsonProperty("profile_image_url_https") 
  private String profile_image_url_https;  
  
  @JsonProperty("id") 
  private Integer id;
  
  @JsonProperty("time_zone") 
  private String time_zone;  
  
  @JsonProperty("utc_offset") 
  private Integer utc_offset;  
  
  @JsonProperty("entities") 
  private TweetEntities entities;
  
}
