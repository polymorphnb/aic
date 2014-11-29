package ac.at.tuwien.tdm.file.dumper.pipeline;

import ac.at.tuwien.tdm.commons.pojo.Tweet;
import ac.at.tuwien.tdm.commons.pojo.User;

import java.util.List;
import java.util.Set;

public interface Task {

  void execute(final List<Tweet> tweets, final Set<User> users) throws Exception;

}
