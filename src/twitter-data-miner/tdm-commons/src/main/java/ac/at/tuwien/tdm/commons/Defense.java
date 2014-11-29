package ac.at.tuwien.tdm.commons;

import java.util.List;

/**
 * TODO move to commons module
 * 
 * Utility class for some helper methods regarding defensive programming.
 * 
 * @author Irnes Okic (irnes.okic@student.tuwien.ac.at)
 * 
 */
public final class Defense {

  private Defense() {
    // hide constructor
  }

  public static void notNull(final String objName, final Object obj) {
    if (obj == null) {
      throw new IllegalArgumentException(String.format("%s is null", objName));
    }
  }

  public static void notBlank(final String strName, final String str) {
    notNull(strName, str);

    if (str.trim().isEmpty()) {
      throw new IllegalArgumentException(String.format("%s is blank", strName));
    }
  }

  // avoid unnecessary autoboxing (Number object)
  public static void biggerThanZero(final String numberName, final int number) {
    if (number < 1) {
      throw new IllegalArgumentException(String.format("%s is less than 1. Actual value: %d", numberName, number));
    }
  }

  public static void notEmpty(final String listName, final List<?> list) {
    notNull(listName, list);

    if (list.isEmpty()) {
      throw new IllegalArgumentException(String.format("%s is empty", listName));
    }
  }
}
