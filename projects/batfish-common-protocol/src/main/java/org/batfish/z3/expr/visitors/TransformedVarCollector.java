package org.batfish.z3.expr.visitors;

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import java.util.stream.Stream;
import org.batfish.common.BatfishException;
import org.batfish.z3.Field;
import org.batfish.z3.expr.AndExpr;
import org.batfish.z3.expr.BooleanExpr;
import org.batfish.z3.expr.EqExpr;
import org.batfish.z3.expr.ExtractExpr;
import org.batfish.z3.expr.FalseExpr;
import org.batfish.z3.expr.HeaderSpaceMatchExpr;
import org.batfish.z3.expr.IfExpr;
import org.batfish.z3.expr.IfThenElse;
import org.batfish.z3.expr.IpSpaceMatchExpr;
import org.batfish.z3.expr.LitIntExpr;
import org.batfish.z3.expr.NotExpr;
import org.batfish.z3.expr.OrExpr;
import org.batfish.z3.expr.PrefixMatchExpr;
import org.batfish.z3.expr.RangeMatchExpr;
import org.batfish.z3.expr.TransformedVarIntExpr;
import org.batfish.z3.expr.TrueExpr;
import org.batfish.z3.expr.VarIntExpr;

public class TransformedVarCollector
    implements GenericIntExprVisitor<Set<Field>>, GenericBooleanExprVisitor<Set<Field>> {
  private static final GenericBooleanExprVisitor<Set<Field>> INSTANCE =
      new TransformedVarCollector();

  @Override
  public Set<Field> castToGenericBooleanExprVisitorReturnType(Object o) {
    throw new BatfishException("not implemented");
  }

  @Override
  public Set<Field> castToGenericIntExprVisitorReturnType(Object o) {
    throw new BatfishException("not implemented");
  }

  public static Set<Field> collectTransformedVars(BooleanExpr expr) {
    return expr.accept(INSTANCE);
  }

  @Override
  public Set<Field> visitAndExpr(AndExpr andExpr) {
    return andExpr
        .getConjuncts()
        .stream()
        .map(TransformedVarCollector::collectTransformedVars)
        .flatMap(Set::stream)
        .collect(ImmutableSet.toImmutableSet());
  }

  @Override
  public Set<Field> visitEqExpr(EqExpr eqExpr) {
    return Stream.of(eqExpr.getLhs().accept(this), eqExpr.getRhs().accept(this))
        .flatMap(Set::stream)
        .collect(ImmutableSet.toImmutableSet());
  }

  @Override
  public Set<Field> visitFalseExpr(FalseExpr falseExpr) {
    return ImmutableSet.of();
  }

  @Override
  public Set<Field> visitHeaderSpaceMatchExpr(HeaderSpaceMatchExpr headerSpaceMatchExpr) {
    return headerSpaceMatchExpr.getExpr().accept(this);
  }

  @Override
  public Set<Field> visitIfExpr(IfExpr ifExpr) {
    return Stream.of(ifExpr.getAntecedent().accept(this), ifExpr.getConsequent().accept(this))
        .flatMap(Set::stream)
        .collect(ImmutableSet.toImmutableSet());
  }

  @Override
  public Set<Field> visitIfThenElse(IfThenElse ifThenElse) {
    return Stream.of(
            ifThenElse.getCondition().accept(this),
            ifThenElse.getThen().accept(this),
            ifThenElse.getElse().accept(this))
        .flatMap(Set::stream)
        .collect(ImmutableSet.toImmutableSet());
  }

  @Override
  public Set<Field> visitMatchIpSpaceExpr(IpSpaceMatchExpr matchIpSpaceExpr) {
    return matchIpSpaceExpr.getExpr().accept(this);
  }

  @Override
  public Set<Field> visitNotExpr(NotExpr notExpr) {
    return notExpr.getArg().accept(this);
  }

  @Override
  public Set<Field> visitOrExpr(OrExpr orExpr) {
    return orExpr
        .getDisjuncts()
        .stream()
        .map(TransformedVarCollector::collectTransformedVars)
        .flatMap(Set::stream)
        .collect(ImmutableSet.toImmutableSet());
  }

  @Override
  public Set<Field> visitPrefixMatchExpr(PrefixMatchExpr prefixMatchExpr) {
    return prefixMatchExpr.getExpr().accept(this);
  }

  @Override
  public Set<Field> visitRangeMatchExpr(RangeMatchExpr rangeMatchExpr) {
    return rangeMatchExpr.getExpr().accept(this);
  }

  @Override
  public Set<Field> visitTrueExpr(TrueExpr trueExpr) {
    return ImmutableSet.of();
  }

  @Override
  public Set<Field> visitExtractExpr(ExtractExpr extractExpr) {
    return extractExpr.getVar().accept(this);
  }

  @Override
  public Set<Field> visitLitIntExpr(LitIntExpr litIntExpr) {
    return ImmutableSet.of();
  }

  @Override
  public Set<Field> visitTransformedVarIntExpr(TransformedVarIntExpr transformedVarIntExpr) {
    return ImmutableSet.of(transformedVarIntExpr.getField());
  }

  @Override
  public Set<Field> visitVarIntExpr(VarIntExpr varIntExpr) {
    return ImmutableSet.of();
  }
}
