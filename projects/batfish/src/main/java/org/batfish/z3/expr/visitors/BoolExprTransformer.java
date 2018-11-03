package org.batfish.z3.expr.visitors;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Context;
import com.microsoft.z3.Expr;
import java.util.List;
import java.util.Set;
import org.batfish.z3.Field;
import org.batfish.z3.NodContext;
import org.batfish.z3.SynthesizerInput;
import org.batfish.z3.expr.AndExpr;
import org.batfish.z3.expr.BasicRuleStatement;
import org.batfish.z3.expr.BooleanExpr;
import org.batfish.z3.expr.Comment;
import org.batfish.z3.expr.EqExpr;
import org.batfish.z3.expr.FalseExpr;
import org.batfish.z3.expr.GenericStatementVisitor;
import org.batfish.z3.expr.HeaderSpaceMatchExpr;
import org.batfish.z3.expr.IfExpr;
import org.batfish.z3.expr.IfThenElse;
import org.batfish.z3.expr.IpSpaceMatchExpr;
import org.batfish.z3.expr.NotExpr;
import org.batfish.z3.expr.OrExpr;
import org.batfish.z3.expr.PrefixMatchExpr;
import org.batfish.z3.expr.QueryStatement;
import org.batfish.z3.expr.RangeMatchExpr;
import org.batfish.z3.expr.StateExpr;
import org.batfish.z3.expr.Statement;
import org.batfish.z3.expr.TrueExpr;
import org.batfish.z3.state.StateParameter.Type;
import org.batfish.z3.state.visitors.Parameterizer;

/**
 * Visitor that transforms Batfish reachability AST {@link BooleanExpr} into Z3 AST {@link BoolExpr}
 */
public class BoolExprTransformer
    implements GenericBooleanExprVisitor<BoolExpr>, GenericStatementVisitor<BoolExpr> {

  public static String getNodName(Set<Type> vectorizedParameters, StateExpr stateExpr) {
    StringBuilder name = new StringBuilder();
    name.append(String.format("S_%s", stateExpr.getClass().getSimpleName()));
    Parameterizer.getParameters(stateExpr)
        .stream()
        .filter(parameter -> !vectorizedParameters.contains(parameter.getType()))
        .forEach(parameter -> name.append(String.format("_%s", parameter.getId())));
    return name.toString();
  }

  public static String getNodName(SynthesizerInput input, StateExpr stateExpr) {
    return getNodName(input.getVectorizedParameters(), stateExpr);
  }

  public static BoolExpr toBoolExpr(
      BooleanExpr booleanExpr, SynthesizerInput input, NodContext nodContext) {
    return booleanExpr.accept(new BoolExprTransformer(input, nodContext));
  }

  public static BoolExpr toBoolExpr(
      StateExpr stateExpr, SynthesizerInput input, NodContext nodContext) {
    return new BoolExprTransformer(input, nodContext)
        .transformStateExpr(stateExpr, ImmutableSet.of());
  }

  public static BoolExpr toBoolExpr(
      Statement statement, SynthesizerInput input, NodContext nodContext) {
    return statement.accept(new BoolExprTransformer(input, nodContext));
  }

  private final List<Field> _fields;

  private final SynthesizerInput _input;

  private final NodContext _nodContext;

  private BoolExprTransformer(SynthesizerInput input, NodContext nodContext) {
    _input = input;
    _nodContext = nodContext;
    _fields =
        _nodContext
            .getVariableNames()
            .stream()
            .map(
                varName ->
                    new Field(varName, _nodContext.getVariables().get(varName).getSortSize()))
            .collect(ImmutableList.toImmutableList());
  }

  @Override
  public BoolExpr castToGenericBooleanExprVisitorReturnType(Object o) {
    return (BoolExpr) o;
  }

  public BoolExpr transformStateExpr(StateExpr stateExpr, Set<Field> transformedFields) {
    return (BoolExpr)
        _nodContext
            .getContext()
            .mkApp(
                _nodContext.getRelationDeclarations().get(getNodName(_input, stateExpr)),
                _fields
                    .stream()
                    .map(
                        field ->
                            transformedFields.contains(field)
                                ? _nodContext.getTransformedVariables().get(field.getName())
                                : _nodContext.getVariables().get(field.getName()))
                    .toArray(Expr[]::new));
  }

  @Override
  public BoolExpr visitAndExpr(AndExpr andExpr) {
    return _nodContext
        .getContext()
        .mkAnd(
            andExpr
                .getConjuncts()
                .stream()
                .map(conjunct -> toBoolExpr(conjunct, _input, _nodContext))
                .toArray(BoolExpr[]::new));
  }

  @Override
  public BoolExpr visitBasicRuleStatement(BasicRuleStatement basicRuleStatement) {
    Context ctx = _nodContext.getContext();

    Set<Field> transformedVars =
        TransformedVarCollector.collectTransformedVars(
            basicRuleStatement.getPreconditionStateIndependentConstraints());

    ImmutableList.Builder<BoolExpr> preconditions =
        ImmutableList.<BoolExpr>builder()
            .add(
                toBoolExpr(
                    basicRuleStatement.getPreconditionStateIndependentConstraints(),
                    _input,
                    _nodContext));
    basicRuleStatement
        .getPreconditionStates()
        .stream()
        .map(preconditionState -> transformStateExpr(preconditionState, ImmutableSet.of()))
        .forEach(preconditions::add);
    return ctx.mkImplies(
        ctx.mkAnd(preconditions.build().toArray(new BoolExpr[0])),
        transformStateExpr(basicRuleStatement.getPostconditionState(), transformedVars));
  }

  @Override
  public BoolExpr visitComment(Comment comment) {
    throw new UnsupportedOperationException("no implementation for generated method");
  }

  @Override
  public BoolExpr visitEqExpr(EqExpr eqExpr) {
    return _nodContext
        .getContext()
        .mkEq(
            BitVecExprTransformer.toBitVecExpr(eqExpr.getLhs(), _nodContext),
            BitVecExprTransformer.toBitVecExpr(eqExpr.getRhs(), _nodContext));
  }

  @Override
  public BoolExpr visitFalseExpr(FalseExpr falseExpr) {
    return _nodContext.getContext().mkFalse();
  }

  @Override
  public BoolExpr visitHeaderSpaceMatchExpr(HeaderSpaceMatchExpr headerSpaceMatchExpr) {
    return headerSpaceMatchExpr.getExpr().accept(this);
  }

  @Override
  public BoolExpr visitIfExpr(IfExpr ifExpr) {
    return _nodContext
        .getContext()
        .mkImplies(
            BoolExprTransformer.toBoolExpr(ifExpr.getAntecedent(), _input, _nodContext),
            BoolExprTransformer.toBoolExpr(ifExpr.getConsequent(), _input, _nodContext));
  }

  @Override
  public BoolExpr visitIfThenElse(IfThenElse ifThenElse) {
    Context ctx = _nodContext.getContext();
    return (BoolExpr)
        ctx.mkITE(
            ifThenElse.getCondition().accept(this),
            ifThenElse.getThen().accept(this),
            ifThenElse.getElse().accept(this));
  }

  @Override
  public BoolExpr visitMatchIpSpaceExpr(IpSpaceMatchExpr matchIpSpaceExpr) {
    return matchIpSpaceExpr.getExpr().accept(this);
  }

  @Override
  public BoolExpr visitNotExpr(NotExpr notExpr) {
    return _nodContext.getContext().mkNot(toBoolExpr(notExpr.getArg(), _input, _nodContext));
  }

  @Override
  public BoolExpr visitOrExpr(OrExpr orExpr) {
    return _nodContext
        .getContext()
        .mkOr(
            orExpr
                .getDisjuncts()
                .stream()
                .map(disjunct -> toBoolExpr(disjunct, _input, _nodContext))
                .toArray(BoolExpr[]::new));
  }

  @Override
  public BoolExpr visitPrefixMatchExpr(PrefixMatchExpr prefixMatchExpr) {
    return prefixMatchExpr.getExpr().accept(this);
  }

  @Override
  public BoolExpr visitQueryStatement(QueryStatement queryStatement) {
    return transformStateExpr(queryStatement.getStateExpr(), ImmutableSet.of());
  }

  @Override
  public BoolExpr visitRangeMatchExpr(RangeMatchExpr rangeMatchExpr) {
    return rangeMatchExpr.getExpr().accept(this);
  }

  @Override
  public BoolExpr visitTrueExpr(TrueExpr trueExpr) {
    return _nodContext.getContext().mkTrue();
  }
}
