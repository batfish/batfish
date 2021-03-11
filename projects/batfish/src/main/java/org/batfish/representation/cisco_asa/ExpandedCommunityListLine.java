package org.batfish.representation.cisco_asa;

import java.io.Serializable;
import org.batfish.datamodel.LineAction;

public class ExpandedCommunityListLine implements Serializable {

  private LineAction _action;
  private String _regex;

  public ExpandedCommunityListLine(LineAction action, String regex) {
    _action = action;
    _regex = regex;
  }

  public LineAction getAction() {
    return _action;
  }

  public String getRegex() {
    return _regex;
  }
}
