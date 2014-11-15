package at.ac.tuwien.aic.tda.json;

import at.ac.tuwien.aic.tda.json.twitter.Tweet;

import java.io.IOException;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;
import org.codehaus.jackson.map.ObjectMapper;

public class JsonConnector {

  public static void  main(String[] args) { 
    JsonFactory f = new JsonFactory();
    JsonParser jp;
    try {
      jp = f.createJsonParser(JsonConnector.class.getClassLoader().getResourceAsStream("tweets.txt"));

      // advance stream to START_ARRAY first:
      jp.nextToken();
      // and then each time, advance to opening START_OBJECT
      ObjectMapper mapper = new ObjectMapper();
      while (jp.nextToken() == JsonToken.START_OBJECT) {
        Tweet foobar = mapper.readValue(jp, Tweet.class);
        
        System.out.println(foobar);
        
        System.out.println(foobar.getId_str());
        System.out.println(jp.nextToken());
      }
    } catch (JsonParseException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
