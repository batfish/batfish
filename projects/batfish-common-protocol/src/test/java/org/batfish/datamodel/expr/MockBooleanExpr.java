package org.batfish.datamodel.expr;

import java.util.Set;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.IpAccessList;

public class MockBooleanExpr extends BooleanExpr {
  private Boolean _matchResult;

  public MockBooleanExpr(boolean matchResult) {
    _matchResult = matchResult;
  }

  @Override
  public boolean match(Flow flow, String srcInterface, Set<IpAccessList> availableAcls) {
    return _matchResult;
  }

  @Override
  public int hashCode() {
    return _matchResult.hashCode();
  }

  @Override
  public String toString() {
    return _matchResult.toString();
  }
}
