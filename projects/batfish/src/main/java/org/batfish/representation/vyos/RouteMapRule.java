package org.batfish.representation.vyos;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import org.batfish.datamodel.LineAction;

public class RouteMapRule implements Serializable {

  private LineAction _action;

  private String _description;

  private final Set<RouteMapMatch> _matches = new HashSet<>();

  public LineAction getAction() {
    return _action;
  }

  public String getDescription() {
    return _description;
  }

  public Set<RouteMapMatch> getMatches() {
    return _matches;
  }

  public void setAction(LineAction action) {
    _action = action;
  }

  public void setDescription(String description) {
    _description = description;
  }
}
