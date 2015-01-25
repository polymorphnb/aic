package ac.at.tuwien.tdm.results;

public class InfluenceResult {
	  
	private String uid;
	private String screenName;
	private int followersCount;
	private int favouritesCount;
	private int retweetsCount;
	private int influenceScore;
	
	public InfluenceResult (
			 String uid,
			 String screenName,
			 int followersCount,
			 int favouritesCount,
			 int retweetsCount,
			 int influenceScore	
	) {
		this.setUid(uid);
		this.screenName = screenName;
		this.followersCount = followersCount;
		this.favouritesCount = favouritesCount;
		this.retweetsCount = retweetsCount;
		this.influenceScore = influenceScore;
	}

	public String getScreenName() {
		return screenName;
	}

	public int getFollowersCount() {
		return followersCount;
	}

	public int getFavouritesCount() {
		return favouritesCount;
	}

	public int getRetweetsCount() {
		return retweetsCount;
	}

	public int getInfluenceScore() {
		return influenceScore;
	}

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}
	
}
