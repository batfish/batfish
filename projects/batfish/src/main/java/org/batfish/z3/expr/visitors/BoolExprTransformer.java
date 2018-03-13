package org.batfish.z3.expr.visitors;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Context;
import com.microsoft.z3.Expr;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import org.batfish.z3.NodContext;
import org.batfish.z3.SynthesizerInput;
import org.batfish.z3.TransformationHeaderField;
import org.batfish.z3.expr.AndExpr;
import org.batfish.z3.expr.BasicRuleStatement;
import org.batfish.z3.expr.BasicStateExpr;
import org.batfish.z3.expr.BooleanExpr;
import org.batfish.z3.expr.Comment;
import org.batfish.z3.expr.CurrentIsOriginalExpr;
import org.batfish.z3.expr.EqExpr;
import org.batfish.z3.expr.FalseExpr;
import org.batfish.z3.expr.GenericStatementVisitor;
import org.batfish.z3.expr.HeaderSpaceMatchExpr;
import org.batfish.z3.expr.IfExpr;
import org.batfish.z3.expr.NotExpr;
import org.batfish.z3.expr.OrExpr;
import org.batfish.z3.expr.PrefixMatchExpr;
import org.batfish.z3.expr.QueryStatement;
import org.batfish.z3.expr.RangeMatchExpr;
import org.batfish.z3.expr.SaneExpr;
import org.batfish.z3.expr.StateExpr;
import org.batfish.z3.expr.Statement;
import org.batfish.z3.expr.TransformationRuleStatement;
import org.batfish.z3.expr.TransformationStateExpr;
import org.batfish.z3.expr.TransformedBasicRuleStatement;
import org.batfish.z3.expr.TrueExpr;
import org.batfish.z3.state.StateParameter.Type;
import org.batfish.z3.state.visitors.Parameterizer;

/**
 * Visitor that transforms Batfish reachability AST {@link BooleanExpr} into Z3 AST {@link BoolExpr}
 */
public class BoolExprTransformer
    implements GenericBooleanExprVisitor<BoolExpr>,
        GenericStatementVisitor<BoolExpr>,
        GenericGeneralStateExprVisitor<BoolExpr> {

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
    return stateExpr.accept(new BoolExprTransformer(input, nodContext));
  }

  public static BoolExpr toBoolExpr(
      Statement statement, SynthesizerInput input, NodContext nodContext) {
    return statement.accept(new BoolExprTransformer(input, nodContext));
  }

  private final Expr[] _basicStateArguments;

  private final Expr[] _from;

  private final SynthesizerInput _input;

  private final NodContext _nodContext;

  private final Map<Expr, Expr> _substitutions;

  private final Expr[] _to;

  private BoolExprTransformer(SynthesizerInput input, NodContext nodContext) {
    _input = input;
    _nodContext = nodContext;
    _basicStateArguments =
        Arrays.stream(nodContext.getBasicStateVarIntExprs())
            .map(varIntExpr -> BitVecExprTransformer.toBitVecExpr(varIntExpr, _nodContext))
            .toArray(Expr[]::new);
    Set<String> variables = nodContext.getVariables().keySet();
    _substitutions =
        Arrays.stream(TransformationHeaderField.values())
            .filter(field -> variables.contains(field.getName()))
            .collect(
                ImmutableMap.toImmutableMap(
                    thf -> _nodContext.getVariables().get(thf.getCurrent().getName()),
                    thf -> _nodContext.getVariables().get(thf.getName())));
    _from = _substitutions.keySet().toArray(new Expr[] {});
    _to = _substitutions.values().toArray(new Expr[] {});
  }

  @Override
  public BoolExpr castToGenericBooleanExprVisitorReturnType(Object o) {
    return (BoolExpr) o;
  }

  @Override
  public BoolExpr castToGenericGeneralStateExprVisitorReturnType(Object o) {
    return (BoolExpr) o;
  }

  private Expr[] getBasicRelationArgs(SynthesizerInput input, BasicStateExpr stateExpr) {
    return _basicStateArguments;
  }

  private Expr[] getTransformationRelationArgs(
      SynthesizerInput input, TransformationStateExpr stateExpr) {
    /* TODO: support vectorized state parameters */
    return _basicStateArguments;
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
        .map(preconditionState -> toBoolExpr(preconditionState, _input, _nodContext))
        .forEach(preconditions::add);
    return ctx.mkImplies(
        ctx.mkAnd(preconditions.build().stream().toArray(BoolExpr[]::new)),
        toBoolExpr(basicRuleStatement.getPostconditionState(), _input, _nodContext));
  }

  @Override
  public BoolExpr visitBasicStateExpr(BasicStateExpr basicStateExpr) {
    /* TODO: allow vectorized variables */
    return (BoolExpr)
        _nodContext
            .getContext()
            .mkApp(
                _nodContext.getRelationDeclarations().get(getNodName(_input, basicStateExpr)),
                getBasicRelationArgs(_input, basicStateExpr));
  }

  @Override
  public BoolExpr visitComment(Comment comment) {
    throw new UnsupportedOperationException(
        "no implementation for generated method"); // TODO Auto-generated method stub
  }

  @Override
  public BoolExpr visitCurrentIsOriginalExpr(CurrentIsOriginalExpr currentIsOriginalExpr) {
    return currentIsOriginalExpr.getExpr().accept(this);
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
    return queryStatement.getSubExpression().accept(this);
  }

  @Override
  public BoolExpr visitRangeMatchExpr(RangeMatchExpr rangeMatchExpr) {
    return rangeMatchExpr.getExpr().accept(this);
  }

  @Override
  public BoolExpr visitSaneExpr(SaneExpr saneExpr) {
    return saneExpr.getExpr().accept(this);
  }

  @Override
  public BoolExpr visitTransformationRuleStatement(
      TransformationRuleStatement transformationRuleStatement) {
    Context ctx = _nodContext.getContext();
    ImmutableList.Builder<BoolExpr> preconditions =
        ImmutableList.<BoolExpr>builder()
            .add(
                toBoolExpr(
                    transformationRuleStatement.getPreconditionStateIndependentConstraints(),
                    _input,
                    _nodContext));
    transformationRuleStatement
        .getPreconditionPreTransformationStates()
        .stream()
        .map(
            preconditionPreTransformationState ->
                toBoolExpr(preconditionPreTransformationState, _input, _nodContext))
        .forEach(preconditions::add);
    transformationRuleStatement
        .getPreconditionPostTransformationStates()
        .stream()
        .map(
            preconditionPostTransformationState ->
                (BoolExpr)
                    toBoolExpr(preconditionPostTransformationState, _input, _nodContext)
                        .substitute(_from, _to))
        .forEach(preconditions::add);
    transformationRuleStatement
        .getPreconditionTransformationStates()
        .stream()
        .map(
            preconditionTransformationState ->
                toBoolExpr(preconditionTransformationState, _input, _nodContext))
        .forEach(preconditions::add);
    return ctx.mkImplies(
        ctx.mkAnd(preconditions.build().stream().toArray(BoolExpr[]::new)),
        (BoolExpr)
            toBoolExpr(
                    transformationRuleStatement.getPostconditionTransformationState(),
                    _input,
                    _nodContext)
                .substitute(_from, _to));
  }

  @Override
  public BoolExpr visitTransformationStateExpr(TransformationStateExpr transformationStateExpr) {
    /* TODO: allow vectorized variables */
    return (BoolExpr)
        _nodContext
            .getContext()
            .mkApp(
                _nodContext
                    .getRelationDeclarations()
                    .get(getNodName(_input, transformationStateExpr)),
                getTransformationRelationArgs(_input, transformationStateExpr));
  }

  @Override
  public BoolExpr visitTransformedBasicRuleStatement(
      TransformedBasicRuleStatement transformedBasicRuleStatement) {
    Context ctx = _nodContext.getContext();
    ImmutableList.Builder<BoolExpr> preconditions =
        ImmutableList.<BoolExpr>builder()
            .add(
                toBoolExpr(
                    transformedBasicRuleStatement.getPreconditionStateIndependentConstraints(),
                    _input,
                    _nodContext));
    transformedBasicRuleStatement
        .getPreconditionPreTransformationStates()
        .stream()
        .map(
            preconditionPreTransformationState ->
                toBoolExpr(preconditionPreTransformationState, _input, _nodContext))
        .forEach(preconditions::add);
    transformedBasicRuleStatement
        .getPreconditionPostTransformationStates()
        .stream()
        .map(
            preconditionPostTransformationState ->
                toBoolExpr(preconditionPostTransformationState, _input, _nodContext))
        .forEach(preconditions::add);
    transformedBasicRuleStatement
        .getPreconditionTransformationStates()
        .stream()
        .map(
            preconditionTransformationState ->
                toBoolExpr(preconditionTransformationState, _input, _nodContext))
        .forEach(preconditions::add);
    return ctx.mkImplies(
        ctx.mkAnd(preconditions.build().stream().toArray(BoolExpr[]::new)),
        toBoolExpr(
            transformedBasicRuleStatement.getPostconditionPostTransformationState(),
            _input,
            _nodContext));
  }

  @Override
  public BoolExpr visitTrueExpr(TrueExpr trueExpr) {
    return _nodContext.getContext().mkTrue();
  }
}
