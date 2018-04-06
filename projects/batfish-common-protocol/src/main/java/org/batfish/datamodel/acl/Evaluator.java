package org.batfish.datamodel.acl;

import java.util.Map;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.LineAction;

public class Evaluator implements GenericAclLineMatchExprVisitor<Boolean> {

  private final String _srcInterface;
  private final Flow _flow;
  private final Map<String, IpAccessList> _availableAcls;

  Evaluator(Flow flow, String srcInterface, Map<String, IpAccessList> availableAcls) {
    _srcInterface = srcInterface;
    _flow = flow;
    _availableAcls = availableAcls;
  }

  public static boolean matches(
      AclLineMatchExpr item,
      Flow flow,
      String srcInterface,
      Map<String, IpAccessList> availableAcls) {
    return item.accept(new Evaluator(flow, srcInterface, availableAcls));
  }

  @Override
  public Boolean visitAndMatchExpr(AndMatchExpr andMatchExpr) {
    return andMatchExpr
        .getConjuncts()
        .stream()
        .allMatch(c -> c.match(_flow, _srcInterface, _availableAcls));
  }

  @Override
  public Boolean visitFalseExpr(FalseExpr falseExpr) {
    return false;
  }

  @Override
  public Boolean visitMatchHeaderspace(MatchHeaderspace matchHeaderspace) {
    return matchHeaderspace.getHeaderspace().matches(_flow);
  }

  @Override
  public Boolean visitMatchSrcInterface(MatchSrcInterface matchSrcInterface) {
    return matchSrcInterface.getSrcInterfaces().contains(_srcInterface);
  }

  @Override
  public Boolean visitNotMatchExpr(NotMatchExpr notMatchExpr) {
    return !notMatchExpr.getOperand().match(_flow, _srcInterface, _availableAcls);
  }

  @Override
  public Boolean visitOrMatchExpr(OrMatchExpr orMatchExpr) {
    return orMatchExpr
        .getDisjuncts()
        .stream()
        .anyMatch(d -> d.match(_flow, _srcInterface, _availableAcls));
  }

  @Override
  public Boolean visitPermittedByAcl(PermittedByAcl permittedByAcl) {
    return _availableAcls.get(permittedByAcl.getAclName()).filter(_flow).getAction()
        == LineAction.ACCEPT;
  }

  @Override
  public Boolean visitTrueExpr(TrueExpr trueExpr) {
    return true;
  }
}
