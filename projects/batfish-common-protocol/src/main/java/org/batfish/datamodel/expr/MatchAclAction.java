package org.batfish.datamodel.expr;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.LineAction;

public class MatchAclAction extends BooleanExpr {
  private String _aclName;
  private LineAction _action;

  public MatchAclAction(String aclName, LineAction action) {
    _aclName = aclName;
    _action = action;
  }

  @Override
  public boolean match(Flow flow, String srcInterface, Set<IpAccessList> availableAcls) {
    List<IpAccessList> list =
        availableAcls
            .stream()
            .filter(a -> a.getName().equals(_aclName))
            .collect(Collectors.toList());
    return list.get(0).filter(flow).getAction() == _action;
  }

  @Override
  public int hashCode() {
    return _aclName.hashCode();
  }

  @Override
  public String toString() {
    return "PassAcl " + _aclName;
  }
}
