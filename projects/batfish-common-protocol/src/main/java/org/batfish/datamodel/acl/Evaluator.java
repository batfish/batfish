package org.batfish.datamodel.acl;

import java.util.Map;
import javax.annotation.Nullable;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.LineAction;

public class Evaluator implements GenericAclLineMatchExprVisitor<Boolean> {

  public static boolean matches(
      AclLineMatchExpr item,
      Flow flow,
      String srcInterface,
      Map<String, IpAccessList> availableAcls,
      Map<String, IpSpace> namedIpSpaces) {
    return item.accept(new Evaluator(flow, srcInterface, availableAcls, namedIpSpaces));
  }

  protected final Map<String, IpAccessList> _availableAcls;

  protected final Flow _flow;

  protected final Map<String, IpSpace> _namedIpSpaces;

  protected final @Nullable String _srcInterface;

  public Evaluator(
      Flow flow,
      @Nullable String srcInterface,
      Map<String, IpAccessList> availableAcls,
      Map<String, IpSpace> namedIpSpaces) {
    _srcInterface = srcInterface;
    _flow = flow;
    _availableAcls = availableAcls;
    _namedIpSpaces = namedIpSpaces;
  }

  private LineAction filter(String filterName) {
    return _availableAcls
        .get(filterName)
        .filter(_flow, _srcInterface, _availableAcls, _namedIpSpaces)
        .getAction();
  }

  @Override
  public Boolean visitAndMatchExpr(AndMatchExpr andMatchExpr) {
    return andMatchExpr.getConjuncts().stream().allMatch(c -> c.accept(this));
  }

  @Override
  public Boolean visitDeniedByAcl(DeniedByAcl deniedByAcl) {
    return filter(deniedByAcl.getAclName()) == LineAction.DENY;
  }

  @Override
  public Boolean visitFalseExpr(FalseExpr falseExpr) {
    return false;
  }

  @Override
  public Boolean visitMatchDestinationIp(MatchDestinationIp matchDestinationIp) {
    return matchDestinationIp.getIps().containsIp(_flow.getDstIp(), _namedIpSpaces);
  }

  @Override
  public Boolean visitMatchHeaderSpace(MatchHeaderSpace matchHeaderSpace) {
    return matchHeaderSpace.getHeaderspace().matches(_flow, _namedIpSpaces);
  }

  @Override
  public Boolean visitMatchSourceIp(MatchSourceIp matchSourceIp) {
    return matchSourceIp.getIps().containsIp(_flow.getSrcIp(), _namedIpSpaces);
  }

  @Override
  public Boolean visitMatchSrcInterface(MatchSrcInterface matchSrcInterface) {
    return matchSrcInterface.getSrcInterfaces().contains(_srcInterface);
  }

  @Override
  public Boolean visitNotMatchExpr(NotMatchExpr notMatchExpr) {
    return !notMatchExpr.getOperand().accept(this);
  }

  @Override
  public Boolean visitOriginatingFromDevice(OriginatingFromDevice originatingFromDevice) {
    return _srcInterface == null;
  }

  @Override
  public Boolean visitOrMatchExpr(OrMatchExpr orMatchExpr) {
    return orMatchExpr.getDisjuncts().stream().anyMatch(d -> d.accept(this));
  }

  @Override
  public Boolean visitPermittedByAcl(PermittedByAcl permittedByAcl) {
    return filter(permittedByAcl.getAclName()) == LineAction.PERMIT;
  }

  @Override
  public Boolean visitTrueExpr(TrueExpr trueExpr) {
    return true;
  }
}
