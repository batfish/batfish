package org.batfish.question.aclreachability2;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.batfish.common.BatfishException;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.acl.AclLineMatchExpr;
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
import org.batfish.datamodel.visitors.IpSpaceDereferencer;

/**
 * Visits an {@link AclLineMatchExpr} and replaces any named IP space references with the
 * dereferenced {@link IpSpace}. An {@link AclLineMatchExpr} may contain multiple IP space
 * references if it is of type {@link AndMatchExpr}, {@link OrMatchExpr}, or {@link NotMatchExpr}.
 * Returns a version of the original {@link AclLineMatchExpr} with all IP spaces dereferenced, or
 * {@code null} if any undefined IP space is referenced.
 */
public class HeaderSpaceSanitizer implements GenericAclLineMatchExprVisitor<AclLineMatchExpr> {

  private final Map<String, IpSpace> _namedIpSpaces;

  public HeaderSpaceSanitizer(Map<String, IpSpace> namedIpSpaces) {
    _namedIpSpaces = namedIpSpaces;
  }

  @Override
  public AclLineMatchExpr visitAndMatchExpr(AndMatchExpr andMatchExpr) {
    List<AclLineMatchExpr> newConjuncts = new ArrayList<>();
    for (AclLineMatchExpr conjunct : andMatchExpr.getConjuncts()) {
      AclLineMatchExpr sanitizedConjunct = conjunct.accept(this);
      if (sanitizedConjunct == null) {
        return null;
      }
      newConjuncts.add(sanitizedConjunct);
    }
    return new AndMatchExpr(newConjuncts);
  }

  @Override
  public AclLineMatchExpr visitFalseExpr(FalseExpr falseExpr) {
    return falseExpr;
  }

  @Override
  public AclLineMatchExpr visitMatchHeaderSpace(MatchHeaderSpace matchHeaderSpace) {
    try {
      // Try dereferencing all IpSpace fields in header space. If that results in a header space
      // different from the original, sanitize the line.
      HeaderSpace headerSpace = matchHeaderSpace.getHeaderspace();
      return new MatchHeaderSpace(
          IpSpaceDereferencer.dereferenceHeaderSpace(headerSpace, _namedIpSpaces));
    } catch (BatfishException e) {
      // If dereferencing causes an error, one of the IpSpaces was a cycle or undefined ref.
      return null;
    }
  }

  @Override
  public AclLineMatchExpr visitMatchSrcInterface(MatchSrcInterface matchSrcInterface) {
    return matchSrcInterface;
  }

  @Override
  public AclLineMatchExpr visitNotMatchExpr(NotMatchExpr notMatchExpr) {
    AclLineMatchExpr sanitizedOperand = notMatchExpr.getOperand().accept(this);
    if (sanitizedOperand == null) {
      return null;
    }
    return new NotMatchExpr(sanitizedOperand);
  }

  @Override
  public AclLineMatchExpr visitOriginatingFromDevice(OriginatingFromDevice originatingFromDevice) {
    return originatingFromDevice;
  }

  @Override
  public AclLineMatchExpr visitOrMatchExpr(OrMatchExpr orMatchExpr) {
    List<AclLineMatchExpr> newDisjuncts = new ArrayList<>();
    for (AclLineMatchExpr disjunct : orMatchExpr.getDisjuncts()) {
      AclLineMatchExpr sanitizedDisjunct = disjunct.accept(this);
      if (sanitizedDisjunct == null) {
        return null;
      }
      newDisjuncts.add(sanitizedDisjunct);
    }
    return new OrMatchExpr(newDisjuncts);
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
