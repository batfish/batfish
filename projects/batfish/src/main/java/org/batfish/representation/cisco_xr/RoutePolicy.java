package org.batfish.representation.cisco_xr;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class RoutePolicy implements Serializable {

  private final String _name;

  private List<RoutePolicyStatement> _stmtList;

  public RoutePolicy(String name) {
    _name = name;
    _stmtList = new ArrayList<>();
  }

  public String getName() {
    return _name;
  }

  public List<RoutePolicyStatement> getStatements() {
    return _stmtList;
  }
}
