package ac.at.tuwien.tdm.twitter.connector;

/**
 * Utility class
 * 
 * @author Irnes Okic (irnes.okic@student.tuwien.ac.at)
 * 
 */
public final class Utils {

  private Utils() {
    // hide constructor
  }

  public static boolean isNotBlank(final String str) {
    if (str == null || str.trim().isEmpty()) {
      return false;
    } else {
      return true;
    }
  }
}
