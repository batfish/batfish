package org.batfish.z3;

import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.batfish.common.BatfishException;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpAccessListLine;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.AndMatchExpr;
import org.batfish.datamodel.acl.FalseExpr;
import org.batfish.datamodel.acl.GenericAclLineMatchExprVisitor;
import org.batfish.datamodel.acl.MatchHeaderSpace;
import org.batfish.datamodel.acl.MatchSrcInterface;
import org.batfish.datamodel.acl.NotMatchExpr;
import org.batfish.datamodel.acl.OrMatchExpr;
import org.batfish.datamodel.acl.PermittedByAcl;
import org.batfish.datamodel.acl.TrueExpr;
import org.batfish.z3.expr.AndExpr;
import org.batfish.z3.expr.BooleanExpr;
import org.batfish.z3.expr.HeaderSpaceMatchExpr;
import org.batfish.z3.expr.IfThenElse;
import org.batfish.z3.expr.NotExpr;
import org.batfish.z3.expr.OrExpr;

public class AclLineMatchExprToBooleanExpr implements GenericAclLineMatchExprVisitor<BooleanExpr> {
  private final Map<String, IpAccessList> _nodeAcls;

  public AclLineMatchExprToBooleanExpr(Map<String, IpAccessList> nodeAcls) {
    _nodeAcls = ImmutableMap.copyOf(nodeAcls);
  }

  public BooleanExpr toBooleanExpr(AclLineMatchExpr aclLineMatchExpr) {
    return aclLineMatchExpr.accept(this);
  }

  @Override
  public BooleanExpr visitAndMatchExpr(AndMatchExpr andMatchExpr) {
    return new AndExpr(
        andMatchExpr.getConjuncts().stream().map(this::toBooleanExpr).collect(Collectors.toList()));
  }

  @Override
  public BooleanExpr visitFalseExpr(FalseExpr falseExpr) {
    return org.batfish.z3.expr.FalseExpr.INSTANCE;
  }

  @Override
  public BooleanExpr visitMatchHeaderSpace(MatchHeaderSpace matchHeaderSpace) {
    return new HeaderSpaceMatchExpr(matchHeaderSpace.getHeaderspace());
  }

  @Override
  public BooleanExpr visitMatchSrcInterface(MatchSrcInterface matchSrcInterface) {
    throw new BatfishException("TODO");
  }

  @Override
  public BooleanExpr visitNotMatchExpr(NotMatchExpr notMatchExpr) {
    return new NotExpr(notMatchExpr.getOperand().accept(this));
  }

  @Override
  public BooleanExpr visitOrMatchExpr(OrMatchExpr orMatchExpr) {
    return new OrExpr(
        orMatchExpr.getDisjuncts().stream().map(this::toBooleanExpr).collect(Collectors.toList()));
  }

  @Override
  public BooleanExpr visitPermittedByAcl(PermittedByAcl permittedByAcl) {
    IpAccessList acl = _nodeAcls.get(permittedByAcl.getAclName());
    List<BooleanExpr> lineExprs =
        acl.getLines()
            .stream()
            .map(IpAccessListLine::getMatchCondition)
            .map(this::toBooleanExpr)
            .collect(Collectors.toList());

    // Right fold. Base case (when no line matches) is not permitted.
    BooleanExpr expr = org.batfish.z3.expr.FalseExpr.INSTANCE;
    for (int i = lineExprs.size() - 1; i >= 0; i--) {
      BooleanExpr matched = lineExprs.get(i);
      BooleanExpr permitted =
          acl.getLines().get(i).getAction() == LineAction.ACCEPT
              ? org.batfish.z3.expr.TrueExpr.INSTANCE
              : org.batfish.z3.expr.FalseExpr.INSTANCE;
      expr = new IfThenElse(matched, permitted, expr);
    }
    return expr;
  }

  @Override
  public BooleanExpr visitTrueExpr(TrueExpr trueExpr) {
    return org.batfish.z3.expr.TrueExpr.INSTANCE;
  }
}
