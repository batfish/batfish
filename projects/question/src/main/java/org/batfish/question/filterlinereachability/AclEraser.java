package org.batfish.question.filterlinereachability;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import org.batfish.datamodel.AclAclLine;
import org.batfish.datamodel.AclLine;
import org.batfish.datamodel.ExprAclLine;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.TraceElement;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.AclLineMatchExprs;
import org.batfish.datamodel.acl.AndMatchExpr;
import org.batfish.datamodel.acl.DeniedByAcl;
import org.batfish.datamodel.acl.FalseExpr;
import org.batfish.datamodel.acl.GenericAclLineMatchExprVisitor;
import org.batfish.datamodel.acl.GenericAclLineVisitor;
import org.batfish.datamodel.acl.MatchDestinationIp;
import org.batfish.datamodel.acl.MatchHeaderSpace;
import org.batfish.datamodel.acl.MatchSourceIp;
import org.batfish.datamodel.acl.MatchSrcInterface;
import org.batfish.datamodel.acl.NotMatchExpr;
import org.batfish.datamodel.acl.OrMatchExpr;
import org.batfish.datamodel.acl.OriginatingFromDevice;
import org.batfish.datamodel.acl.PermittedByAcl;
import org.batfish.datamodel.acl.TrueExpr;

/**
 * Erases {@link TraceElement TraceElements} and {@link org.batfish.vendor.VendorStructureId
 * VendorStructureIds} from {@link IpAccessList ACLs}.
 */
public final class AclEraser
    implements GenericAclLineVisitor<AclLine>, GenericAclLineMatchExprVisitor<AclLineMatchExpr> {
  @VisibleForTesting static final AclEraser INSTANCE = new AclEraser();

  private AclEraser() {}

  /**
   * Return a functionally-equivalent version of the input {@link
   * org.batfish.datamodel.IpAccessList} with all {@link TraceElement TraceElements} and {@link
   * org.batfish.vendor.VendorStructureId VendorStructureIds} removed.
   */
  public static IpAccessList erase(IpAccessList acl) {
    ImmutableList<AclLine> lines =
        acl.getLines().stream().map(INSTANCE::visit).collect(ImmutableList.toImmutableList());
    return IpAccessList.builder()
        .setName(acl.getName())
        .setLines(lines)
        .setSourceName(acl.getSourceName())
        .setSourceType(acl.getSourceType())
        .build();
  }

  @Override
  public AclLine visitAclAclLine(AclAclLine aclAclLine) {
    return new AclAclLine(aclAclLine.getName(), aclAclLine.getAclName());
  }

  @Override
  public AclLine visitExprAclLine(ExprAclLine exprAclLine) {
    return new ExprAclLine(
        exprAclLine.getAction(),
        exprAclLine.getMatchCondition().accept(this),
        exprAclLine.getName());
  }

  @Override
  public AclLineMatchExpr visitAndMatchExpr(AndMatchExpr andMatchExpr) {
    return new AndMatchExpr(
        andMatchExpr.getConjuncts().stream()
            .map(this::visit)
            .collect(ImmutableList.toImmutableList()));
  }

  @Override
  public AclLineMatchExpr visitDeniedByAcl(DeniedByAcl deniedByAcl) {
    return new DeniedByAcl(deniedByAcl.getAclName());
  }

  @Override
  public AclLineMatchExpr visitFalseExpr(FalseExpr falseExpr) {
    return FalseExpr.INSTANCE;
  }

  @Override
  public AclLineMatchExpr visitMatchDestinationIp(MatchDestinationIp matchDestinationIp) {
    return matchDestinationIp.getTraceElement() == null
        ? matchDestinationIp
        : AclLineMatchExprs.matchDst(matchDestinationIp.getIps());
  }

  @Override
  public AclLineMatchExpr visitMatchHeaderSpace(MatchHeaderSpace matchHeaderSpace) {
    // TODO erase within IpSpaces if/when we add TraceElements to them
    return new MatchHeaderSpace(matchHeaderSpace.getHeaderspace());
  }

  @Override
  public AclLineMatchExpr visitMatchSourceIp(MatchSourceIp matchSourceIp) {
    return matchSourceIp.getTraceElement() == null
        ? matchSourceIp
        : AclLineMatchExprs.matchSrc(matchSourceIp.getIps());
  }

  @Override
  public AclLineMatchExpr visitMatchSrcInterface(MatchSrcInterface matchSrcInterface) {
    return new MatchSrcInterface(matchSrcInterface.getSrcInterfaces());
  }

  @Override
  public AclLineMatchExpr visitNotMatchExpr(NotMatchExpr notMatchExpr) {
    return new NotMatchExpr(visit(notMatchExpr.getOperand()));
  }

  @Override
  public AclLineMatchExpr visitOriginatingFromDevice(OriginatingFromDevice originatingFromDevice) {
    return OriginatingFromDevice.INSTANCE;
  }

  @Override
  public AclLineMatchExpr visitOrMatchExpr(OrMatchExpr orMatchExpr) {
    return new OrMatchExpr(
        orMatchExpr.getDisjuncts().stream()
            .map(this::visit)
            .collect(ImmutableList.toImmutableList()));
  }

  @Override
  public AclLineMatchExpr visitPermittedByAcl(PermittedByAcl permittedByAcl) {
    return new PermittedByAcl(permittedByAcl.getAclName());
  }

  @Override
  public AclLineMatchExpr visitTrueExpr(TrueExpr trueExpr) {
    return TrueExpr.INSTANCE;
  }
}
