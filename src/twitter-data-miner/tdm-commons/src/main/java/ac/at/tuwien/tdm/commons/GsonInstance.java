package ac.at.tuwien.tdm.commons;

import com.google.gson.Gson;


public class GsonInstance {

  private static final Gson gson = new Gson();
  
  private GsonInstance(){
  }
  
  public static Gson get(){
    return gson;
  }
}
