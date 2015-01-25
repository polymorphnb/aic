package ac.at.tuwien.tdm.queries;

import ac.at.tuwien.tdm.results.DirectInterestResult;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public class QueryHelper {
  
  public static double calc_tf_idf_UserTopic(int numTotalTweets, int numTweetsInTopic) {
    if(numTweetsInTopic == 0) {
      return 0;
    }
    double calc_tf_idf_UserTopic = Math.log(numTotalTweets/numTweetsInTopic);
    
    return calc_tf_idf_UserTopic;
  }
  
  public static List<DirectInterestResult> sortDirectInterestResults(List<DirectInterestResult> resultList) {
    Collections.sort(resultList, new Comparator<DirectInterestResult>() {

      @Override
      public int compare(DirectInterestResult dir1, DirectInterestResult dir2) {
        return Integer.valueOf(dir1.getInterest()).compareTo(dir2.getInterest());
      }
    });
    
    return resultList;
  }

}
