package org.batfish.z3.expr.visitors;

import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.stream.Stream;
import org.batfish.common.BatfishException;
import org.batfish.z3.expr.AndExpr;
import org.batfish.z3.expr.BasicRuleStatement;
import org.batfish.z3.expr.BasicStateExpr;
import org.batfish.z3.expr.BitVecExpr;
import org.batfish.z3.expr.BooleanExpr;
import org.batfish.z3.expr.Comment;
import org.batfish.z3.expr.CurrentIsOriginalExpr;
import org.batfish.z3.expr.DeclareRelStatement;
import org.batfish.z3.expr.DeclareVarStatement;
import org.batfish.z3.expr.EqExpr;
import org.batfish.z3.expr.Expr;
import org.batfish.z3.expr.ExtractExpr;
import org.batfish.z3.expr.FalseExpr;
import org.batfish.z3.expr.GenericStatementVisitor;
import org.batfish.z3.expr.HeaderSpaceMatchExpr;
import org.batfish.z3.expr.IdExpr;
import org.batfish.z3.expr.IfExpr;
import org.batfish.z3.expr.IntExpr;
import org.batfish.z3.expr.ListExpr;
import org.batfish.z3.expr.LitIntExpr;
import org.batfish.z3.expr.NotExpr;
import org.batfish.z3.expr.OrExpr;
import org.batfish.z3.expr.PrefixMatchExpr;
import org.batfish.z3.expr.QueryStatement;
import org.batfish.z3.expr.RangeMatchExpr;
import org.batfish.z3.expr.RuleStatement;
import org.batfish.z3.expr.SaneExpr;
import org.batfish.z3.expr.Statement;
import org.batfish.z3.expr.TransformationRuleStatement;
import org.batfish.z3.expr.TransformationStateExpr;
import org.batfish.z3.expr.TransformedExpr;
import org.batfish.z3.expr.TrueExpr;
import org.batfish.z3.expr.VarIntExpr;

public class Simplifier
    implements GenericBooleanExprVisitor<BooleanExpr>,
        GenericExprVisitor<Expr>,
        GenericIntExprVisitor<IntExpr>,
        GenericStatementVisitor<Statement> {

  private static final Comment UNUSABLE_RULE = new Comment("(unsatisfiable rule)");

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
    if (newConjuncts.size() == 0) {
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
    return visitRuleStatement(basicRuleStatement);
  }

  @Override
  public BooleanExpr visitBasicStateExpr(BasicStateExpr basicStateExpr) {
    return basicStateExpr;
  }

  @Override
  public IntExpr visitBitVecExpr(BitVecExpr bitVecExpr) {
    throw new UnsupportedOperationException(
        "no implementation for generated method"); // TODO Auto-generated method stub
  }

  @Override
  public Statement visitComment(Comment comment) {
    return comment;
  }

  @Override
  public BooleanExpr visitCurrentIsOriginalExpr(CurrentIsOriginalExpr currentIsOriginalExpr) {
    return simplifyBooleanExpr(currentIsOriginalExpr.getExpr());
  }

  @Override
  public Statement visitDeclareRelStatement(DeclareRelStatement declareRelStatement) {
    return declareRelStatement;
  }

  @Override
  public Statement visitDeclareVarStatement(DeclareVarStatement declareVarStatement) {
    return declareVarStatement;
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
    throw new UnsupportedOperationException(
        "no implementation for generated method"); // TODO Auto-generated method stub
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
    throw new UnsupportedOperationException(
        "no implementation for generated method"); // TODO Auto-generated method stub
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
  public Expr visitListExpr(ListExpr listExpr) {
    throw new UnsupportedOperationException(
        "no implementation for generated method"); // TODO Auto-generated method stub
  }

  @Override
  public IntExpr visitLitIntExpr(LitIntExpr litIntExpr) {
    throw new UnsupportedOperationException(
        "no implementation for generated method"); // TODO Auto-generated method stub
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
    if (newDisjuncts.size() == 0) {
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

  public Statement visitRuleStatement(RuleStatement ruleStatement) {
    BooleanExpr oldExpr = ruleStatement.getSubExpression();
    BooleanExpr newExpr = simplifyBooleanExpr(oldExpr);
    if (newExpr != oldExpr) {
      if (newExpr == TrueExpr.INSTANCE) {
        return VACUOUS_RULE;
      } else if (newExpr == FalseExpr.INSTANCE) {
        throw new BatfishException("Unsatifiable!");
      } else if (newExpr instanceof IfExpr
          && ((IfExpr) newExpr).getAntecedent() == FalseExpr.INSTANCE) {
        return UNUSABLE_RULE;
      } else if (newExpr instanceof BasicStateExpr) {
        return new BasicRuleStatement((BasicStateExpr) newExpr);
      } else if (newExpr instanceof TransformationStateExpr) {
        return new TransformationRuleStatement((TransformationStateExpr) newExpr);
      } else if (newExpr instanceof IfExpr) {
        IfExpr newInterior = (IfExpr) newExpr;
        BooleanExpr newConsequent = newInterior.getConsequent();
        if (newConsequent instanceof BasicStateExpr) {
          return new BasicRuleStatement(
              newInterior.getAntecedent(), (BasicStateExpr) newConsequent);
        } else if (newConsequent instanceof TransformationStateExpr) {
          return new TransformationRuleStatement(
              newInterior.getAntecedent(), (TransformationStateExpr) newConsequent);
        } else {
          throw new BatfishException(
              String.format(
                  "Unexpected consequent type after simplification: %s",
                  newConsequent.getClass().getCanonicalName()));
        }
      } else {
        throw new BatfishException(
            String.format(
                "Unexpected type after simplification: %s", newExpr.getClass().getCanonicalName()));
      }
    } else {
      return ruleStatement;
    }
  }

  @Override
  public BooleanExpr visitSaneExpr(SaneExpr saneExpr) {
    return simplifyBooleanExpr(saneExpr.getExpr());
  }

  @Override
  public Statement visitTransformationRuleStatement(
      TransformationRuleStatement transformationRuleStatement) {
    return visitRuleStatement(transformationRuleStatement);
  }

  @Override
  public BooleanExpr visitTransformationStateExpr(TransformationStateExpr transformationStateExpr) {
    /** TODO: something smarter */
    return transformationStateExpr;
  }

  @Override
  public BooleanExpr visitTransformedExpr(TransformedExpr transformedExpr) {
    /* TODO: push transformation down to children? */
    /* TODO: eliminate non-adjacent TransformedExpr children */
    BooleanExpr originalSubExpression = transformedExpr.getSubExpression();
    BooleanExpr simplifiedSubExpression = simplifyBooleanExpr(originalSubExpression);
    if (simplifiedSubExpression instanceof TransformedExpr) {
      // Transformation is idempotent
      return simplifiedSubExpression;
    } else if (simplifiedSubExpression != originalSubExpression) {
      return simplifyBooleanExpr(new TransformedExpr(simplifiedSubExpression));
    } else {
      return transformedExpr;
    }
  }

  @Override
  public BooleanExpr visitTrueExpr(TrueExpr trueExpr) {
    return trueExpr;
  }

  @Override
  public IntExpr visitVarIntExpr(VarIntExpr varIntExpr) {
    throw new UnsupportedOperationException(
        "no implementation for generated method"); // TODO Auto-generated method stub
  }
}
