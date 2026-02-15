package org.batfish.question.filterlinereachability;

import java.util.Map;
import java.util.stream.Collectors;
import org.batfish.datamodel.AclAclLine;
import org.batfish.datamodel.AclLine;
import org.batfish.datamodel.ExprAclLine;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.AclLineMatchExprs;
import org.batfish.datamodel.acl.AndMatchExpr;
import org.batfish.datamodel.acl.CircularReferenceException;
import org.batfish.datamodel.acl.DeniedByAcl;
import org.batfish.datamodel.acl.FalseExpr;
import org.batfish.datamodel.acl.GenericAclLineMatchExprVisitor;
import org.batfish.datamodel.acl.GenericAclLineVisitor;
import org.batfish.datamodel.acl.MatchDestinationIp;
import org.batfish.datamodel.acl.MatchDestinationPort;
import org.batfish.datamodel.acl.MatchHeaderSpace;
import org.batfish.datamodel.acl.MatchIpProtocol;
import org.batfish.datamodel.acl.MatchSourceIp;
import org.batfish.datamodel.acl.MatchSourcePort;
import org.batfish.datamodel.acl.MatchSrcInterface;
import org.batfish.datamodel.acl.NotMatchExpr;
import org.batfish.datamodel.acl.OrMatchExpr;
import org.batfish.datamodel.acl.OriginatingFromDevice;
import org.batfish.datamodel.acl.PermittedByAcl;
import org.batfish.datamodel.acl.TrueExpr;
import org.batfish.datamodel.acl.UndefinedReferenceException;
import org.batfish.datamodel.visitors.IpSpaceDereferencer;

/**
 * Makes a version of the given {@link AclLine} or {@link AclLineMatchExpr} with any named IP space
 * references replaced with the dereferenced {@link IpSpace}. Throws {@link
 * CircularReferenceException} if any circular IP space reference is referenced, or {@link
 * UndefinedReferenceException} if any undefined IP space is referenced.
 */
public class HeaderSpaceSanitizer
    implements GenericAclLineMatchExprVisitor<AclLineMatchExpr>, GenericAclLineVisitor<AclLine> {

  private final Map<String, IpSpace> _namedIpSpaces;

  public HeaderSpaceSanitizer(Map<String, IpSpace> namedIpSpaces) {
    _namedIpSpaces = namedIpSpaces;
  }

  /* AclLine visit methods */

  @Override
  public AclLine visitAclAclLine(AclAclLine aclAclLine) {
    return aclAclLine;
  }

  @Override
  public AclLine visitExprAclLine(ExprAclLine exprAclLine) {
    return exprAclLine.toBuilder()
        .setMatchCondition(visit(exprAclLine.getMatchCondition()))
        .setName(exprAclLine.getName())
        .build();
  }

  /* AclLineMatchExpr visit methods */

  @Override
  public AclLineMatchExpr visitAndMatchExpr(AndMatchExpr andMatchExpr)
      throws CircularReferenceException, UndefinedReferenceException {
    return new AndMatchExpr(
        andMatchExpr.getConjuncts().stream().map(this::visit).collect(Collectors.toList()));
  }

  @Override
  public AclLineMatchExpr visitDeniedByAcl(DeniedByAcl deniedByAcl) {
    return deniedByAcl;
  }

  @Override
  public AclLineMatchExpr visitFalseExpr(FalseExpr falseExpr) {
    return falseExpr;
  }

  @Override
  public AclLineMatchExpr visitMatchDestinationIp(MatchDestinationIp matchDestinationIp) {
    IpSpaceDereferencer def = new IpSpaceDereferencer(_namedIpSpaces);
    IpSpace derefed = matchDestinationIp.getIps().accept(def);
    if (derefed == matchDestinationIp.getIps()) {
      return matchDestinationIp;
    }
    return AclLineMatchExprs.matchDst(derefed, matchDestinationIp.getTraceElement());
  }

  @Override
  public AclLineMatchExpr visitMatchDestinationPort(MatchDestinationPort matchDestinationPort) {
    return matchDestinationPort;
  }

  @Override
  public AclLineMatchExpr visitMatchHeaderSpace(MatchHeaderSpace matchHeaderSpace)
      throws CircularReferenceException, UndefinedReferenceException {
    return new MatchHeaderSpace(
        IpSpaceDereferencer.dereferenceHeaderSpace(
            matchHeaderSpace.getHeaderspace(), _namedIpSpaces));
  }

  @Override
  public AclLineMatchExpr visitMatchIpProtocol(MatchIpProtocol matchIpProtocol) {
    return matchIpProtocol;
  }

  @Override
  public AclLineMatchExpr visitMatchSourceIp(MatchSourceIp matchSourceIp) {
    IpSpaceDereferencer def = new IpSpaceDereferencer(_namedIpSpaces);
    IpSpace derefed = matchSourceIp.getIps().accept(def);
    if (derefed == matchSourceIp.getIps()) {
      return matchSourceIp;
    }
    return AclLineMatchExprs.matchSrc(derefed, matchSourceIp.getTraceElement());
  }

  @Override
  public AclLineMatchExpr visitMatchSourcePort(MatchSourcePort matchSourcePort) {
    return matchSourcePort;
  }

  @Override
  public AclLineMatchExpr visitMatchSrcInterface(MatchSrcInterface matchSrcInterface) {
    return matchSrcInterface;
  }

  @Override
  public AclLineMatchExpr visitNotMatchExpr(NotMatchExpr notMatchExpr)
      throws CircularReferenceException, UndefinedReferenceException {
    return new NotMatchExpr(visit(notMatchExpr.getOperand()));
  }

  @Override
  public AclLineMatchExpr visitOriginatingFromDevice(OriginatingFromDevice originatingFromDevice) {
    return originatingFromDevice;
  }

  @Override
  public AclLineMatchExpr visitOrMatchExpr(OrMatchExpr orMatchExpr)
      throws CircularReferenceException, UndefinedReferenceException {
    return new OrMatchExpr(
        orMatchExpr.getDisjuncts().stream().map(this::visit).collect(Collectors.toList()));
  }

  @Override
  public AclLineMatchExpr visitPermittedByAcl(PermittedByAcl permittedByAcl) {
    return permittedByAcl;
  }

  @Override
  public AclLineMatchExpr visitTrueExpr(TrueExpr trueExpr) {
    return trueExpr;
  }
}
