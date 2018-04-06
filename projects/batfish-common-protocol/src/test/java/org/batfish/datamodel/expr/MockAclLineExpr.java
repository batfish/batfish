package org.batfish.datamodel.expr;

import java.util.Set;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.IpAccessList;

public class MockAclLineExpr extends AclLineExpr {
  private Boolean _matchResult;

  public MockAclLineExpr(boolean matchResult) {
    _matchResult = matchResult;
  }

  @Override
  public boolean match(Flow flow, String srcInterface, Set<IpAccessList> availableAcls) {
    return _matchResult;
  }

  @Override
  public boolean exprEquals(Object o) {
    return _matchResult == ((MockAclLineExpr) o).getMatchResult();
  }

  @Override
  public int hashCode() {
    return _matchResult.hashCode();
  }

  @Override
  public String toString() {
    return _matchResult.toString();
  }

  public Boolean getMatchResult() {
    return _matchResult;
  }
}
