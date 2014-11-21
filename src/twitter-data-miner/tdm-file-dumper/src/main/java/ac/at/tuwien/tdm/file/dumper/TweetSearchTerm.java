package ac.at.tuwien.tdm.file.dumper;

/**
 * Represents a topic that should be searched for (in tweets)
 * 
 * @author Irnes Okic (irnes.okic@student.tuwien.ac.at)
 * 
 */
public final class TweetSearchTerm {

  private final String searchTerm;

  private final boolean searchOnlyInHashTags;

  public TweetSearchTerm(final String searchTerm, final boolean searchOnlyInHashTags) {
    this.searchTerm = searchTerm;
    this.searchOnlyInHashTags = searchOnlyInHashTags;
  }

  public String getSearchTerm() {
    return searchTerm;
  }

  public boolean isSearchOnlyInHashTags() {
    return searchOnlyInHashTags;
  }

  @Override
  public String toString() {
    return "TweetSearchTopic [searchTerm=" + searchTerm + ", searchOnlyInHashTags=" + searchOnlyInHashTags + "]";
  }
}
