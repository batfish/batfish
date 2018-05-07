package org.batfish.z3;

import static java.lang.Math.max;

import com.google.common.collect.ImmutableList;
import com.google.common.math.LongMath;
import java.math.RoundingMode;
import java.util.List;
import org.batfish.z3.expr.AndExpr;
import org.batfish.z3.expr.BasicRuleStatement;
import org.batfish.z3.expr.Comment;
import org.batfish.z3.expr.EqExpr;
import org.batfish.z3.expr.GenericStatementVisitor;
import org.batfish.z3.expr.LitIntExpr;
import org.batfish.z3.expr.QueryStatement;
import org.batfish.z3.expr.StateExpr;
import org.batfish.z3.expr.Statement;
import org.batfish.z3.expr.VarIntExpr;
import org.batfish.z3.state.OriginateInterface;
import org.batfish.z3.state.OriginateVrf;

/**
 * A NOD instrumentation that tracks which of several possible IngressPoints was used to derive each
 * solution.
 */
public class IngressPointInstrumentation implements GenericStatementVisitor<Statement> {

  private final int _fieldBits;

  private final ImmutableList<IngressPoint> _ingressPoints;

  private final Field _ingressPointField;

  public static final String INGRESS_POINT_FIELD_NAME = "INGRESS_POINT";

  public IngressPointInstrumentation(ImmutableList<IngressPoint> ingressPoints) {
    _ingressPoints = ingressPoints;
    _fieldBits = max(LongMath.log2(_ingressPoints.size(), RoundingMode.CEILING), 1);
    _ingressPointField = new Field(INGRESS_POINT_FIELD_NAME, _fieldBits);
  }

  public int getFieldBits() {
    return _fieldBits;
  }

  public List<IngressPoint> getIngressPoints() {
    return _ingressPoints;
  }

  public Statement instrumentStatement(Statement statement) {
    return statement.accept(this);
  }

  @Override
  public Statement visitBasicRuleStatement(BasicRuleStatement basicRuleStatement) {
    StateExpr postState = basicRuleStatement.getPostconditionState();
    if (postState instanceof OriginateVrf) {
      OriginateVrf originateVrf = (OriginateVrf) postState;
      int index =
          _ingressPoints.indexOf(
              IngressPoint.ingressVrf(originateVrf.getHostname(), originateVrf.getVrf()));
      return new BasicRuleStatement(
          new AndExpr(
              ImmutableList.of(
                  basicRuleStatement.getPreconditionStateIndependentConstraints(),
                  new EqExpr(
                      new VarIntExpr(_ingressPointField), new LitIntExpr(index, _fieldBits)))),
          basicRuleStatement.getPreconditionStates(),
          postState);
    } else if (postState instanceof OriginateInterface) {
      OriginateInterface originateInterface = (OriginateInterface) postState;
      int index =
          _ingressPoints.indexOf(
              IngressPoint.ingressInterface(
                  originateInterface.getHostname(), originateInterface.getIface()));
      return new BasicRuleStatement(
          new AndExpr(
              ImmutableList.of(
                  basicRuleStatement.getPreconditionStateIndependentConstraints(),
                  new EqExpr(
                      new VarIntExpr(_ingressPointField), new LitIntExpr(index, _fieldBits)))),
          basicRuleStatement.getPreconditionStates(),
          postState);
    } else {
      return basicRuleStatement;
    }
  }

  @Override
  public Statement visitComment(Comment comment) {
    return comment;
  }

  @Override
  public Statement visitQueryStatement(QueryStatement queryStatement) {
    return queryStatement;
  }
}
