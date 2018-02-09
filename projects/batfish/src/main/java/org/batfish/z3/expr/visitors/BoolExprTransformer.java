package org.batfish.z3.expr.visitors;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Expr;
import java.util.Arrays;
import org.batfish.z3.HeaderField;
import org.batfish.z3.NodProgram;
import org.batfish.z3.SynthesizerInput;
import org.batfish.z3.expr.AndExpr;
import org.batfish.z3.expr.BooleanExpr;
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
import org.batfish.z3.expr.TrueExpr;
import org.batfish.z3.expr.VarIntExpr;
import org.batfish.z3.state.visitors.Parameterizer;

public class BoolExprTransformer implements BooleanExprVisitor {

  public static String getNodName(SynthesizerInput input, StateExpr stateExpr) {
    StringBuilder name = new StringBuilder();
    name.append(String.format("S_%s", stateExpr.getClass().getSimpleName()));
    Parameterizer.getParameters(stateExpr)
        .stream()
        .filter(parameter -> !input.getVectorizedParameters().contains(parameter.getType()))
        .forEach(parameter -> name.append(String.format("_%s", parameter.getId())));
    return name.toString();
  }

  public static BoolExpr toBoolExpr(
      BooleanExpr booleanExpr, SynthesizerInput input, NodProgram nodProgram) {
    BoolExprTransformer boolExprTransformer = new BoolExprTransformer(input, nodProgram);
    booleanExpr.accept(boolExprTransformer);
    return boolExprTransformer._boolExpr;
  }

  private BoolExpr _boolExpr;

  private final Supplier<Expr[]> _headerFieldArgs;

  private final SynthesizerInput _input;

  private final NodProgram _nodProgram;

  private BoolExprTransformer(SynthesizerInput input, NodProgram nodProgram) {
    _input = input;
    _nodProgram = nodProgram;
    _headerFieldArgs =
        Suppliers.memoize(
            () ->
                Arrays.stream(HeaderField.values())
                    .map(VarIntExpr::new)
                    .map(e -> BitVecExprTransformer.toBitVecExpr(e, _nodProgram))
                    .toArray(Expr[]::new));
  }

  private Expr[] getNodRelationArgs(SynthesizerInput input, StateExpr stateExpr) {
    /* TODO: support vectorized state parameters */
    return _headerFieldArgs.get();
  }

  @Override
  public void visitAndExpr(AndExpr andExpr) {
    _boolExpr =
        _nodProgram
            .getContext()
            .mkAnd(
                andExpr
                    .getConjuncts()
                    .stream()
                    .map(conjunct -> toBoolExpr(conjunct, _input, _nodProgram))
                    .toArray(BoolExpr[]::new));
  }

  @Override
  public void visitEqExpr(EqExpr eqExpr) {
    _boolExpr =
        _nodProgram
            .getContext()
            .mkEq(
                BitVecExprTransformer.toBitVecExpr(eqExpr.getLhs(), _nodProgram),
                BitVecExprTransformer.toBitVecExpr(eqExpr.getRhs(), _nodProgram));
  }

  @Override
  public void visitFalseExpr(FalseExpr falseExpr) {
    _boolExpr = _nodProgram.getContext().mkFalse();
  }

  @Override
  public void visitHeaderSpaceMatchExpr(HeaderSpaceMatchExpr headerSpaceMatchExpr) {
    headerSpaceMatchExpr.getExpr().accept(this);
  }

  @Override
  public void visitIfExpr(IfExpr ifExpr) {
    _boolExpr =
        _nodProgram
            .getContext()
            .mkImplies(
                BoolExprTransformer.toBoolExpr(ifExpr.getAntecedent(), _input, _nodProgram),
                BoolExprTransformer.toBoolExpr(ifExpr.getConsequent(), _input, _nodProgram));
  }

  @Override
  public void visitNotExpr(NotExpr notExpr) {
    _boolExpr = _nodProgram.getContext().mkNot(toBoolExpr(notExpr.getArg(), _input, _nodProgram));
  }

  @Override
  public void visitOrExpr(OrExpr orExpr) {
    _boolExpr =
        _nodProgram
            .getContext()
            .mkOr(
                orExpr
                    .getDisjuncts()
                    .stream()
                    .map(disjunct -> toBoolExpr(disjunct, _input, _nodProgram))
                    .toArray(BoolExpr[]::new));
  }

  @Override
  public void visitPrefixMatchExpr(PrefixMatchExpr prefixMatchExpr) {
    prefixMatchExpr.getExpr().accept(this);
  }

  @Override
  public void visitRangeMatchExpr(RangeMatchExpr rangeMatchExpr) {
    rangeMatchExpr.getExpr().accept(this);
  }

  @Override
  public void visitSaneExpr(SaneExpr saneExpr) {
    saneExpr.getExpr().accept(this);
  }

  @Override
  public void visitStateExpr(StateExpr stateExpr) {
    /* TODO: allow vectorized variables */
    _boolExpr =
        (BoolExpr)
            _nodProgram
                .getContext()
                .mkApp(
                    _nodProgram.getRelationDeclarations().get(getNodName(_input, stateExpr)),
                    getNodRelationArgs(_input, stateExpr));
  }

  @Override
  public void visitTrueExpr(TrueExpr trueExpr) {
    _boolExpr = _nodProgram.getContext().mkTrue();
  }
}
