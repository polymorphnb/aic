package ac.at.tuwien.tdm.file.dumper;

final class TweetSearchTopic {

  private final String searchTerm;

  private final boolean searchOnlyInHashTags;

  public TweetSearchTopic(final String searchTerm, final boolean searchOnlyInHashTags) {
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
