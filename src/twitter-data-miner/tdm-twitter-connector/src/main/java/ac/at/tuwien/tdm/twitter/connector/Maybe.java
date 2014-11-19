package ac.at.tuwien.tdm.twitter.connector;

/**
 * Used for explicit handling of maybe not existing values (instead of null)
 * 
 * @author Irnes Okic (irnes.okic@student.tuwien.ac.at)
 * 
 */
public final class Maybe<T> {

  private final T value;

  private Maybe(final T value) {
    this.value = value;
  }

  public static <T> Maybe<T> definitely(final T value) {
    return new Maybe<T>(value);
  }

  public static <T> Maybe<T> unknown(final Class<T> clazz) {
    return new Maybe<T>(null);
  }

  public boolean isKnown() {
    return value != null;
  }

  public boolean isUnknown() {
    return !isKnown();
  }

  public T value() {
    return value;
  }
}
