package org.batfish.z3;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.ListIterator;
import java.util.Map;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpAccessListLine;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.LineAction;
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
import org.batfish.z3.expr.AndExpr;
import org.batfish.z3.expr.BooleanExpr;
import org.batfish.z3.expr.EqExpr;
import org.batfish.z3.expr.HeaderSpaceMatchExpr;
import org.batfish.z3.expr.IfThenElse;
import org.batfish.z3.expr.IntExpr;
import org.batfish.z3.expr.LitIntExpr;
import org.batfish.z3.expr.NotExpr;
import org.batfish.z3.expr.OrExpr;
import org.batfish.z3.expr.VarIntExpr;
import org.batfish.z3.state.visitors.DefaultTransitionGenerator;

public class AclLineMatchExprToBooleanExpr implements GenericAclLineMatchExprVisitor<BooleanExpr> {
  private final Map<String, IpSpace> _namedIpSpaces;

  private final Map<String, IpAccessList> _nodeAcls;

  private final Field _sourceInterfaceField;

  private final ImmutableMap<String, IntExpr> _sourceInterfaceFieldValues;

  public AclLineMatchExprToBooleanExpr(
      Map<String, IpAccessList> nodeAcls,
      Map<String, IpSpace> namedIpSpaces,
      Field sourceInterfaceField,
      Map<String, IntExpr> sourceInterfaceFieldValues) {
    _namedIpSpaces = ImmutableMap.copyOf(namedIpSpaces);
    _nodeAcls = ImmutableMap.copyOf(nodeAcls);
    _sourceInterfaceField = sourceInterfaceField;
    _sourceInterfaceFieldValues = ImmutableMap.copyOf(sourceInterfaceFieldValues);
  }

  @VisibleForTesting
  BooleanExpr matchSrcInterfaceExpr(String srcInterfaceName) {
    return new EqExpr(
        new VarIntExpr(_sourceInterfaceField), _sourceInterfaceFieldValues.get(srcInterfaceName));
  }

  public BooleanExpr toBooleanExpr(AclLineMatchExpr aclLineMatchExpr) {
    return aclLineMatchExpr.accept(this);
  }

  @Override
  public BooleanExpr visitAndMatchExpr(AndMatchExpr andMatchExpr) {
    return new AndExpr(
        andMatchExpr
            .getConjuncts()
            .stream()
            .map(this::toBooleanExpr)
            .collect(ImmutableList.toImmutableList()));
  }

  @Override
  public BooleanExpr visitFalseExpr(FalseExpr falseExpr) {
    return org.batfish.z3.expr.FalseExpr.INSTANCE;
  }

  @Override
  public BooleanExpr visitMatchHeaderSpace(MatchHeaderSpace matchHeaderSpace) {
    return new HeaderSpaceMatchExpr(matchHeaderSpace.getHeaderspace(), _namedIpSpaces).getExpr();
  }

  @Override
  public BooleanExpr visitMatchSrcInterface(MatchSrcInterface matchSrcInterface) {
    return new OrExpr(
        matchSrcInterface
            .getSrcInterfaces()
            .stream()
            .map(this::matchSrcInterfaceExpr)
            .collect(ImmutableList.toImmutableList()));
  }

  @Override
  public BooleanExpr visitNotMatchExpr(NotMatchExpr notMatchExpr) {
    return new NotExpr(notMatchExpr.getOperand().accept(this));
  }

  @Override
  public BooleanExpr visitOriginatingFromDevice(OriginatingFromDevice originatingFromDevice) {
    return new EqExpr(
        new VarIntExpr(_sourceInterfaceField),
        new LitIntExpr(
            DefaultTransitionGenerator.NO_SOURCE_INTERFACE, _sourceInterfaceField.getSize()));
  }

  @Override
  public BooleanExpr visitOrMatchExpr(OrMatchExpr orMatchExpr) {
    return new OrExpr(
        orMatchExpr
            .getDisjuncts()
            .stream()
            .map(this::toBooleanExpr)
            .collect(ImmutableList.toImmutableList()));
  }

  @Override
  public BooleanExpr visitPermittedByAcl(PermittedByAcl permittedByAcl) {
    IpAccessList acl = _nodeAcls.get(permittedByAcl.getAclName());

    // Right fold. Base case (when no line matches) is not permitted.
    BooleanExpr expr = org.batfish.z3.expr.FalseExpr.INSTANCE;
    ListIterator<IpAccessListLine> iter = acl.getLines().listIterator(acl.getLines().size());
    while (iter.hasPrevious()) {
      IpAccessListLine line = iter.previous();
      BooleanExpr matched = toBooleanExpr(line.getMatchCondition());
      BooleanExpr permitted =
          line.getAction() == LineAction.PERMIT
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
