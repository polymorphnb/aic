package ac.at.tuwien.tdm.twitter.connector.job;

import ac.at.tuwien.tdm.twitter.connector.result.CursorListTaskResult;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import twitter4j.IDs;
import twitter4j.PagableResponseList;
import twitter4j.User;

public abstract class AbstractFriendsFollowersTask implements Task<CursorListTaskResult<Long>> {

  protected CursorListTaskResult<Long> buildResult(final IDs ids) {
    return new CursorListTaskResult<>(ids.getRateLimitStatus(), retrieveUserIds(ids), (ids.hasNext()
        ? ids.getNextCursor() : null));
  }

  protected CursorListTaskResult<Long> buildResult(final PagableResponseList<User> users) {
    return new CursorListTaskResult<>(users.getRateLimitStatus(), retrieveUserIds(users), (users.hasNext()
        ? users.getNextCursor() : null));
  }

  protected List<Long> retrieveUserIds(final IDs ids) {
    final List<Long> userIds = new ArrayList<>(32);

    for (final long id : ids.getIDs()) {
      userIds.add(id);
    }

    return userIds;
  }

  protected List<Long> retrieveUserIds(final PagableResponseList<User> users) {
    final List<Long> userIds = new ArrayList<>(32);

    final Iterator<User> userIterator = users.iterator();

    while (userIterator.hasNext()) {
      userIds.add(userIterator.next().getId());
    }

    return userIds;
  }
}
