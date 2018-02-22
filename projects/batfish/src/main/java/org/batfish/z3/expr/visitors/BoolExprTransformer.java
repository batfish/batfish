package org.batfish.z3.expr.visitors;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Streams;
import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Expr;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import org.batfish.z3.BasicHeaderField;
import org.batfish.z3.NodContext;
import org.batfish.z3.SynthesizerInput;
import org.batfish.z3.TransformationHeaderField;
import org.batfish.z3.expr.AndExpr;
import org.batfish.z3.expr.BasicStateExpr;
import org.batfish.z3.expr.BooleanExpr;
import org.batfish.z3.expr.CurrentIsOriginalExpr;
import org.batfish.z3.expr.EqExpr;
import org.batfish.z3.expr.FalseExpr;
import org.batfish.z3.expr.HeaderSpaceMatchExpr;
import org.batfish.z3.expr.IfExpr;
import org.batfish.z3.expr.NotExpr;
import org.batfish.z3.expr.OrExpr;
import org.batfish.z3.expr.PrefixMatchExpr;
import org.batfish.z3.expr.RangeMatchExpr;
import org.batfish.z3.expr.SaneExpr;
import org.batfish.z3.expr.StateExpr;
import org.batfish.z3.expr.TransformationStateExpr;
import org.batfish.z3.expr.TransformedExpr;
import org.batfish.z3.expr.TrueExpr;
import org.batfish.z3.expr.VarIntExpr;
import org.batfish.z3.state.StateParameter.Type;
import org.batfish.z3.state.visitors.DefaultTransitionGenerator;
import org.batfish.z3.state.visitors.Parameterizer;

/**
 * Visitor that transforms Batfish reachability AST {@link BooleanExpr} into Z3 AST {@link BoolExpr}
 */
public class BoolExprTransformer implements GenericBooleanExprVisitor<BoolExpr> {

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

  private final Supplier<Expr[]> _basicArguments;

  private final Supplier<Expr[]> _from;

  private final SynthesizerInput _input;

  private final NodContext _nodContext;

  private final Supplier<Map<Expr, Expr>> _substitutions;

  private final Supplier<Expr[]> _to;

  private final Supplier<Expr[]> _transformationArguments;

  private BoolExprTransformer(SynthesizerInput input, NodContext nodContext) {
    _input = input;
    _nodContext = nodContext;
    _basicArguments =
        Suppliers.memoize(
            () ->
                Arrays.stream(BasicHeaderField.values())
                    .map(VarIntExpr::new)
                    .map(e -> BitVecExprTransformer.toBitVecExpr(e, _nodContext))
                    .toArray(Expr[]::new));
    _transformationArguments =
        Suppliers.memoize(
            () ->
                Streams.concat(
                        Arrays.stream(_basicArguments.get()),
                        Arrays.stream(TransformationHeaderField.values())
                            .map(VarIntExpr::new)
                            .map(e -> BitVecExprTransformer.toBitVecExpr(e, _nodContext)))
                    .toArray(Expr[]::new));
    _substitutions =
        () ->
            Arrays.stream(TransformationHeaderField.values())
                .collect(
                    ImmutableMap.toImmutableMap(
                        thf -> _nodContext.getVariables().get(thf.getCurrent().getName()),
                        thf -> _nodContext.getVariables().get(thf.getName())));
    _from = () -> _substitutions.get().keySet().toArray(new Expr[] {});
    _to = () -> _substitutions.get().values().toArray(new Expr[] {});
  }

  @Override
  public BoolExpr castToGenericBooleanExprVisitorReturnType(Object o) {
    return (BoolExpr) o;
  }

  private Expr[] getBasicRelationArgs(SynthesizerInput input, BasicStateExpr stateExpr) {
    /* TODO: support vectorized state parameters */
    return _basicArguments.get();
  }

  private Expr[] getTransformationRelationArgs(
      SynthesizerInput input, TransformationStateExpr stateExpr) {
    /* TODO: support vectorized state parameters */
    return _transformationArguments.get();
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
  public BoolExpr visitRangeMatchExpr(RangeMatchExpr rangeMatchExpr) {
    return rangeMatchExpr.getExpr().accept(this);
  }

  @Override
  public BoolExpr visitSaneExpr(SaneExpr saneExpr) {
    return saneExpr.getExpr().accept(this);
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

  /**
   * Transforms e.g. R1(h) and Transformed(R2(h)) -> R3(h,h') to R1(h) and R2(h') -> R3(h,h'), where
   * h'=h with a particular fixed set of variable substitutions.<br>
   * See {@link DefaultTransitionGenerator#visitPostOutInterface} for example of use.
   */
  @Override
  public BoolExpr visitTransformedExpr(TransformedExpr transformedExpr) {
    /* TODO: allow vectorized variables */
    BoolExpr subExpression = toBoolExpr(transformedExpr.getSubExpression(), _input, _nodContext);
    return (BoolExpr) subExpression.substitute(_from.get(), _to.get());
  }

  @Override
  public BoolExpr visitTrueExpr(TrueExpr trueExpr) {
    return _nodContext.getContext().mkTrue();
  }
}
