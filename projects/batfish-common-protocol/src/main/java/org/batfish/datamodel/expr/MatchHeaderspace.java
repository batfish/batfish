package org.batfish.datamodel.expr;

import java.util.Set;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.IpAccessList;

public class MatchHeaderspace extends AclLineExpr {
  private HeaderSpace _headerspace;

  public MatchHeaderspace(HeaderSpace headerspace) {
    _headerspace = headerspace;
  }

  @Override
  public boolean match(Flow flow, String srcInterface, Set<IpAccessList> availableAcls) {
    return _headerspace.matches(flow);
  }

  @Override
  public boolean exprEquals(Object o) {
    return _headerspace == ((MatchHeaderspace) o).getHeaderspace();
  }

  @Override
  public int hashCode() {
    return _headerspace.hashCode();
  }

  @Override
  public String toString() {
    return "MatchHeaderspace " + _headerspace.toString();
  }

  public HeaderSpace getHeaderspace() {
    return _headerspace;
  }
}
