package at.ac.tuwien.aic;

import org.neo4j.graphdb.RelationshipType;


public enum TwitterRelationshipType implements RelationshipType {
  FOLLOWS(RelationshipTypeConstants.FOLLOWS),
  FRIEND(RelationshipTypeConstants.FRIEND),
  RETWEETS(RelationshipTypeConstants.RETWEETS),
  REPLIES(RelationshipTypeConstants.REPLIES),
  INTERESTEDIN(RelationshipTypeConstants.INTERESTED_IN),
  INTERACTS_WITH(RelationshipTypeConstants.INTERACTS_WITH);
  
  
  private String value;

  private TwitterRelationshipType(String value) {
      this.value = value;
  }

  public String getValue() {
      return this.value;
  }
}
