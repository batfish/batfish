package org.batfish.z3;

import java.util.Map;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpAccessListLine;
import org.batfish.datamodel.acl.AndMatchExpr;
import org.batfish.datamodel.acl.FalseExpr;
import org.batfish.datamodel.acl.GenericAclLineMatchExprVisitor;
import org.batfish.datamodel.acl.MatchHeaderSpace;
import org.batfish.datamodel.acl.MatchSrcInterface;
import org.batfish.datamodel.acl.NotMatchExpr;
import org.batfish.datamodel.acl.OrMatchExpr;
import org.batfish.datamodel.acl.OriginatingFromDevice;
import org.batfish.datamodel.acl.PermittedByAcl;
import org.batfish.datamodel.acl.TrueExpr;

public class DependsOnSourceInterface implements GenericAclLineMatchExprVisitor<Boolean> {
  private final Map<String, IpAccessList> _ipAccessLists;

  public DependsOnSourceInterface(Map<String, IpAccessList> ipAccessLists) {
    _ipAccessLists = ipAccessLists;
  }

  @Override
  public Boolean visitAndMatchExpr(AndMatchExpr andMatchExpr) {
    return andMatchExpr.getConjuncts().stream().anyMatch(expr -> expr.accept(this));
  }

  @Override
  public Boolean visitFalseExpr(FalseExpr falseExpr) {
    return false;
  }

  @Override
  public Boolean visitMatchHeaderSpace(MatchHeaderSpace matchHeaderSpace) {
    return false;
  }

  @Override
  public Boolean visitMatchSrcInterface(MatchSrcInterface matchSrcInterface) {
    return true;
  }

  @Override
  public Boolean visitNotMatchExpr(NotMatchExpr notMatchExpr) {
    return notMatchExpr.getOperand().accept(this);
  }

  @Override
  public Boolean visitOriginatingFromDevice(OriginatingFromDevice originatingFromDevice) {
    return true;
  }

  @Override
  public Boolean visitOrMatchExpr(OrMatchExpr orMatchExpr) {
    return orMatchExpr.getDisjuncts().stream().anyMatch(expr -> expr.accept(this));
  }

  @Override
  public Boolean visitPermittedByAcl(PermittedByAcl permittedByAcl) {
    return dependsOnSourceInterface(_ipAccessLists.get(permittedByAcl.getAclName()));
  }

  public Boolean dependsOnSourceInterface(IpAccessList ipAccessList) {
    return ipAccessList
        .getLines()
        .stream()
        .map(IpAccessListLine::getMatchCondition)
        .anyMatch(expr -> expr.accept(this));
  }

  @Override
  public Boolean visitTrueExpr(TrueExpr trueExpr) {
    return false;
  }
}
