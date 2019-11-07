package org.batfish.representation.cisco_xr;

import java.io.Serializable;
import java.util.List;

public class RoutePolicyElseBlock implements Serializable {

  private List<RoutePolicyStatement> _stmtList;

  public RoutePolicyElseBlock(List<RoutePolicyStatement> stmtList) {
    _stmtList = stmtList;
  }

  public void addStatement(RoutePolicyStatement stmt) {
    _stmtList.add(stmt);
  }

  public List<RoutePolicyStatement> getStatements() {
    return _stmtList;
  }
}
