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
import org.batfish.z3.expr.Statement;
import org.batfish.z3.expr.TransformationRuleStatement;
import org.batfish.z3.expr.VarIntExpr;
import org.batfish.z3.state.OriginateVrf;

/**
 * A NOD instrumentation that tracks which of several possible OriginateVrf states was used to
 * derive each solution.
 */
public class OriginateVrfInstrumentation implements GenericStatementVisitor<Statement> {

  private final int _fieldBits;

  private final ImmutableList<OriginateVrf> _originateVrfs;

  private final Field _originateVrfField;

  public static final String ORIGINATE_VRF_FIELD_NAME = "ORIGINATE_VRF";

  public OriginateVrfInstrumentation(ImmutableList<OriginateVrf> originateVrfs) {
    _originateVrfs = originateVrfs;
    _fieldBits = max(LongMath.log2(_originateVrfs.size(), RoundingMode.CEILING), 1);
    _originateVrfField = new Field(ORIGINATE_VRF_FIELD_NAME, _fieldBits);
  }

  public int getFieldBits() {
    return _fieldBits;
  }

  public List<OriginateVrf> getOriginateVrfs() {
    return _originateVrfs;
  }

  public Statement instrumentStatement(Statement statement) {
    return statement.accept(this);
  }

  @Override
  public Statement visitBasicRuleStatement(BasicRuleStatement basicRuleStatement) {
    if (basicRuleStatement.getPostconditionState() instanceof OriginateVrf) {
      int index = _originateVrfs.indexOf(basicRuleStatement.getPostconditionState());
      return new BasicRuleStatement(
          new AndExpr(
              ImmutableList.of(
                  basicRuleStatement.getPreconditionStateIndependentConstraints(),
                  new EqExpr(
                      new VarIntExpr(_originateVrfField), new LitIntExpr(index, _fieldBits)))),
          basicRuleStatement.getPreconditionStates(),
          basicRuleStatement.getPostconditionState());
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

  @Override
  public Statement visitTransformationRuleStatement(
      TransformationRuleStatement transformationRuleStatement) {
    return transformationRuleStatement;
  }
}
