package org.batfish.representation.cisco_asa;

import java.io.Serializable;
import java.util.List;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.bgp.community.StandardCommunity;

public class StandardCommunityListLine implements Serializable {

  private LineAction _action;
  private List<StandardCommunity> _communities;

  public StandardCommunityListLine(LineAction action, List<StandardCommunity> communities) {
    _action = action;
    _communities = communities;
  }

  public LineAction getAction() {
    return _action;
  }

  public List<StandardCommunity> getCommunities() {
    return _communities;
  }
}
