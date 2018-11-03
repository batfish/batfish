package org.batfish.z3.expr.visitors;

import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.stream.Stream;
import org.batfish.z3.expr.AndExpr;
import org.batfish.z3.expr.BasicRuleStatement;
import org.batfish.z3.expr.BitVecExpr;
import org.batfish.z3.expr.BooleanExpr;
import org.batfish.z3.expr.Comment;
import org.batfish.z3.expr.EqExpr;
import org.batfish.z3.expr.Expr;
import org.batfish.z3.expr.ExtractExpr;
import org.batfish.z3.expr.FalseExpr;
import org.batfish.z3.expr.GenericStatementVisitor;
import org.batfish.z3.expr.HeaderSpaceMatchExpr;
import org.batfish.z3.expr.IdExpr;
import org.batfish.z3.expr.IfExpr;
import org.batfish.z3.expr.IfThenElse;
import org.batfish.z3.expr.IntExpr;
import org.batfish.z3.expr.IpSpaceMatchExpr;
import org.batfish.z3.expr.ListExpr;
import org.batfish.z3.expr.LitIntExpr;
import org.batfish.z3.expr.NotExpr;
import org.batfish.z3.expr.OrExpr;
import org.batfish.z3.expr.PrefixMatchExpr;
import org.batfish.z3.expr.QueryStatement;
import org.batfish.z3.expr.RangeMatchExpr;
import org.batfish.z3.expr.StateExpr;
import org.batfish.z3.expr.Statement;
import org.batfish.z3.expr.TransformedVarIntExpr;
import org.batfish.z3.expr.TrueExpr;
import org.batfish.z3.expr.VarIntExpr;

public class Simplifier
    implements GenericBooleanExprVisitor<BooleanExpr>,
        GenericExprVisitor<Expr>,
        GenericIntExprVisitor<IntExpr>,
        GenericStatementVisitor<Statement> {

  private static final Comment VACUOUS_RULE = new Comment("(vacuous rule)");

  public static BooleanExpr simplifyBooleanExpr(BooleanExpr expr) {
    return expr.accept(new Simplifier());
  }

  public static Statement simplifyStatement(Statement statement) {
    return statement.accept(new Simplifier());
  }

  private Simplifier() {}

  @Override
  public BooleanExpr castToGenericBooleanExprVisitorReturnType(Object o) {
    return (BooleanExpr) o;
  }

  @Override
  public IntExpr castToGenericIntExprVisitorReturnType(Object o) {
    return (IntExpr) o;
  }

  @Override
  public BooleanExpr visitAndExpr(AndExpr andExpr) {
    boolean changed = false;
    List<BooleanExpr> oldConjuncts = andExpr.getConjuncts();
    ImmutableList.Builder<BooleanExpr> newConjunctsBuilder = ImmutableList.builder();

    // first check for nested ANDs
    if (oldConjuncts.stream().anyMatch(Predicates.instanceOf(AndExpr.class))) {
      return simplifyBooleanExpr(
          new AndExpr(
              oldConjuncts
                  .stream()
                  .flatMap(
                      conjunct ->
                          conjunct instanceof AndExpr
                              ? ((AndExpr) conjunct).getConjuncts().stream()
                              : Stream.of(conjunct))
                  .collect(ImmutableList.toImmutableList())));
    }

    // no nested ANDs, so just simplify all conjuncts
    for (BooleanExpr conjunct : oldConjuncts) {
      BooleanExpr simplifiedConjunct = simplifyBooleanExpr(conjunct);
      if (conjunct != simplifiedConjunct) {
        changed = true;
      }
      if (simplifiedConjunct == FalseExpr.INSTANCE) {
        return FalseExpr.INSTANCE;
      } else if (simplifiedConjunct != TrueExpr.INSTANCE) {
        newConjunctsBuilder.add(simplifiedConjunct);
      } else {
        changed = true;
      }
    }
    List<BooleanExpr> newConjuncts = newConjunctsBuilder.build();
    if (newConjuncts.isEmpty()) {
      return TrueExpr.INSTANCE;
    } else if (newConjuncts.size() == 1) {
      return newConjuncts.get(0);
    } else if (!changed) {
      return andExpr;
    } else {
      return new AndExpr(newConjuncts);
    }
  }

  @Override
  public Statement visitBasicRuleStatement(BasicRuleStatement basicRuleStatement) {
    /* TODO: something smarter */
    BooleanExpr originalPreconditionStateIndependentConstraints =
        basicRuleStatement.getPreconditionStateIndependentConstraints();
    BooleanExpr simplifiedPreconditionStateIndependentConstraints =
        simplifyBooleanExpr(originalPreconditionStateIndependentConstraints);
    if (simplifiedPreconditionStateIndependentConstraints == FalseExpr.INSTANCE) {
      return VACUOUS_RULE;
    } else if (originalPreconditionStateIndependentConstraints
        != simplifiedPreconditionStateIndependentConstraints) {
      return simplifyStatement(
          new BasicRuleStatement(
              simplifiedPreconditionStateIndependentConstraints,
              basicRuleStatement.getPreconditionStates(),
              basicRuleStatement.getPostconditionState()));
    } else {
      return basicRuleStatement;
    }
  }

  @Override
  public IntExpr visitBitVecExpr(BitVecExpr bitVecExpr) {
    throw new UnsupportedOperationException("no implementation for generated method");
  }

  @Override
  public Statement visitComment(Comment comment) {
    return comment;
  }

  @Override
  public BooleanExpr visitEqExpr(EqExpr eqExpr) {
    IntExpr lhs = eqExpr.getLhs();
    IntExpr rhs = eqExpr.getRhs();
    if (lhs.equals(rhs)) {
      return TrueExpr.INSTANCE;
    } else if (lhs instanceof LitIntExpr && rhs instanceof LitIntExpr) {
      return FalseExpr.INSTANCE;
    } else {
      return eqExpr;
    }
  }

  @Override
  public IntExpr visitExtractExpr(ExtractExpr extractExpr) {
    throw new UnsupportedOperationException("no implementation for generated method");
  }

  @Override
  public BooleanExpr visitFalseExpr(FalseExpr falseExpr) {
    return falseExpr;
  }

  @Override
  public BooleanExpr visitHeaderSpaceMatchExpr(HeaderSpaceMatchExpr headerSpaceMatchExpr) {
    return simplifyBooleanExpr(headerSpaceMatchExpr.getExpr());
  }

  @Override
  public Expr visitIdExpr(IdExpr idExpr) {
    throw new UnsupportedOperationException("no implementation for generated method");
  }

  @Override
  public BooleanExpr visitIfExpr(IfExpr ifExpr) {
    BooleanExpr oldAntecedent = ifExpr.getAntecedent();
    BooleanExpr oldConsequent = ifExpr.getConsequent();
    BooleanExpr newAntecedent = simplifyBooleanExpr(oldAntecedent);
    BooleanExpr newConsequent = simplifyBooleanExpr(oldConsequent);
    if (newAntecedent == FalseExpr.INSTANCE
        || newConsequent == TrueExpr.INSTANCE
        || newAntecedent.equals(newConsequent)) {
      return TrueExpr.INSTANCE;
    } else if (newAntecedent == TrueExpr.INSTANCE) {
      return newConsequent;
    } else if (newAntecedent != oldAntecedent || newConsequent != oldConsequent) {
      return new IfExpr(newAntecedent, newConsequent);
    } else {
      return ifExpr;
    }
  }

  @Override
  public BooleanExpr visitIfThenElse(IfThenElse ifThenElse) {
    BooleanExpr condition = ifThenElse.getCondition().accept(this);

    if (condition == TrueExpr.INSTANCE) {
      return ifThenElse.getThen().accept(this);
    } else if (condition == FalseExpr.INSTANCE) {
      return ifThenElse.getElse().accept(this);
    } else {
      BooleanExpr then = ifThenElse.getThen().accept(this);
      BooleanExpr els = ifThenElse.getElse().accept(this);
      if (then == els) {
        return then;
      } else if (then == TrueExpr.INSTANCE && els == FalseExpr.INSTANCE) {
        return condition;
      } else if (then == FalseExpr.INSTANCE && els == TrueExpr.INSTANCE) {
        return new NotExpr(condition);
      } else if (then == TrueExpr.INSTANCE) {
        return new OrExpr(ImmutableList.of(condition, els));
      } else if (then == FalseExpr.INSTANCE) {
        return new AndExpr(ImmutableList.of(new NotExpr(condition), els));
      } else if (els == TrueExpr.INSTANCE) {
        return new OrExpr(ImmutableList.of(new NotExpr(condition), then));
      } else if (els == FalseExpr.INSTANCE) {
        return new AndExpr(ImmutableList.of(condition, then));
      }
      // No nice simplifications, return a new ITE if any of the three components is simpler.
      if (condition != ifThenElse.getCondition()
          || then != ifThenElse.getThen()
          || els != ifThenElse.getElse()) {
        return new IfThenElse(condition, then, els);
      } else {
        return ifThenElse;
      }
    }
  }

  @Override
  public Expr visitListExpr(ListExpr listExpr) {
    throw new UnsupportedOperationException("no implementation for generated method");
  }

  @Override
  public IntExpr visitLitIntExpr(LitIntExpr litIntExpr) {
    throw new UnsupportedOperationException("no implementation for generated method");
  }

  @Override
  public BooleanExpr visitMatchIpSpaceExpr(IpSpaceMatchExpr matchIpSpaceExpr) {
    return matchIpSpaceExpr.getExpr().accept(this);
  }

  @Override
  public BooleanExpr visitNotExpr(NotExpr notExpr) {
    BooleanExpr oldArg = notExpr.getArg();
    BooleanExpr newArg = simplifyBooleanExpr(oldArg);
    if (newArg == FalseExpr.INSTANCE) {
      return TrueExpr.INSTANCE;
    } else if (newArg == TrueExpr.INSTANCE) {
      return FalseExpr.INSTANCE;
    } else if (newArg instanceof NotExpr) {
      return ((NotExpr) newArg).getArg();
    } else if (newArg != oldArg) {
      return new NotExpr(newArg);
    } else {
      return notExpr;
    }
  }

  @Override
  public BooleanExpr visitOrExpr(OrExpr orExpr) {
    boolean changed = false;
    List<BooleanExpr> oldDisjuncts = orExpr.getDisjuncts();
    ImmutableList.Builder<BooleanExpr> newDisjunctsBuilder = ImmutableList.builder();

    // first check for nested ORs
    if (oldDisjuncts.stream().anyMatch(Predicates.instanceOf(OrExpr.class))) {
      return simplifyBooleanExpr(
          new OrExpr(
              oldDisjuncts
                  .stream()
                  .flatMap(
                      disjunct ->
                          disjunct instanceof OrExpr
                              ? ((OrExpr) disjunct).getDisjuncts().stream()
                              : Stream.of(disjunct))
                  .collect(ImmutableList.toImmutableList())));
    }

    // no nested ORs, so just simplify all disjuncts
    for (BooleanExpr disjunct : oldDisjuncts) {
      BooleanExpr simplifiedDisjunct = simplifyBooleanExpr(disjunct);
      if (disjunct != simplifiedDisjunct) {
        changed = true;
      }
      if (simplifiedDisjunct == TrueExpr.INSTANCE) {
        return TrueExpr.INSTANCE;
      } else if (simplifiedDisjunct != FalseExpr.INSTANCE) {
        newDisjunctsBuilder.add(simplifiedDisjunct);
      } else {
        changed = true;
      }
    }
    List<BooleanExpr> newDisjuncts = newDisjunctsBuilder.build();
    if (newDisjuncts.isEmpty()) {
      return FalseExpr.INSTANCE;
    } else if (newDisjuncts.size() == 1) {
      return newDisjuncts.get(0);
    } else if (!changed) {
      return orExpr;
    } else {
      return new OrExpr(newDisjuncts);
    }
  }

  @Override
  public BooleanExpr visitPrefixMatchExpr(PrefixMatchExpr prefixMatchExpr) {
    return simplifyBooleanExpr(prefixMatchExpr.getExpr());
  }

  @Override
  public Statement visitQueryStatement(QueryStatement queryStatement) {
    return queryStatement;
  }

  @Override
  public BooleanExpr visitRangeMatchExpr(RangeMatchExpr rangeMatchExpr) {
    return simplifyBooleanExpr(rangeMatchExpr.getExpr());
  }

  @Override
  public StateExpr visitStateExpr(StateExpr stateExpr) {
    return stateExpr;
  }

  @Override
  public IntExpr visitTransformedVarIntExpr(TransformedVarIntExpr transformedVarIntExpr) {
    return transformedVarIntExpr;
  }

  @Override
  public BooleanExpr visitTrueExpr(TrueExpr trueExpr) {
    return trueExpr;
  }

  @Override
  public IntExpr visitVarIntExpr(VarIntExpr varIntExpr) {
    return varIntExpr;
  }
}
