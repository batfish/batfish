package org.batfish.representation.cisco_xr;

import java.io.Serializable;
import java.util.List;

public class RoutePolicyElseIfBlock implements Serializable {

  private RoutePolicyBoolean _guard;
  private List<RoutePolicyStatement> _stmtList;

  public RoutePolicyElseIfBlock(RoutePolicyBoolean guard, List<RoutePolicyStatement> stmtList) {
    _guard = guard;
    _stmtList = stmtList;
  }

  public void addStatement(RoutePolicyStatement stmt) {
    _stmtList.add(stmt);
  }

  public RoutePolicyBoolean getGuard() {
    return _guard;
  }

  public List<RoutePolicyStatement> getStatements() {
    return _stmtList;
  }
}
