package org.batfish.z3.expr.visitors;

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import org.batfish.z3.HeaderField;
import org.batfish.z3.expr.AndExpr;
import org.batfish.z3.expr.BitVecExpr;
import org.batfish.z3.expr.CollapsedListExpr;
import org.batfish.z3.expr.Comment;
import org.batfish.z3.expr.DeclareRelExpr;
import org.batfish.z3.expr.DeclareVarExpr;
import org.batfish.z3.expr.EqExpr;
import org.batfish.z3.expr.ExpandedListExpr;
import org.batfish.z3.expr.Expr;
import org.batfish.z3.expr.ExprVisitor;
import org.batfish.z3.expr.ExtractExpr;
import org.batfish.z3.expr.FalseExpr;
import org.batfish.z3.expr.HeaderSpaceMatchExpr;
import org.batfish.z3.expr.IdExpr;
import org.batfish.z3.expr.IfExpr;
import org.batfish.z3.expr.LitIntExpr;
import org.batfish.z3.expr.NotExpr;
import org.batfish.z3.expr.OrExpr;
import org.batfish.z3.expr.PrefixMatchExpr;
import org.batfish.z3.expr.QueryExpr;
import org.batfish.z3.expr.RangeMatchExpr;
import org.batfish.z3.expr.RuleExpr;
import org.batfish.z3.expr.SaneExpr;
import org.batfish.z3.expr.TrueExpr;
import org.batfish.z3.expr.VarIntExpr;
import org.batfish.z3.state.State;
import org.batfish.z3.state.State.StateExpr;
import org.batfish.z3.state.StateParameterization;

public class VariableCollector implements ExprVisitor {

  public static final Set<HeaderField> collectVariables(Expr expr) {
    VariableCollector variableCollector = new VariableCollector();
    expr.accept(variableCollector);
    return variableCollector._vars.build();
  }

  private final ImmutableSet.Builder<HeaderField> _vars;

  private VariableCollector() {
    _vars = ImmutableSet.builder();
  }

  @Override
  public void visitAndExpr(AndExpr andExpr) {
    andExpr.getConjuncts().forEach(expr -> expr.accept(this));
  }

  @Override
  public void visitBitVecExpr(BitVecExpr bitVecExpr) {}

  @Override
  public void visitCollapsedListExpr(CollapsedListExpr collapsedListExpr) {
    collapsedListExpr.getSubExpressions().forEach(expr -> expr.accept(this));
  }

  @Override
  public void visitComment(Comment comment) {}

  @Override
  public void visitDeclareRelExpr(DeclareRelExpr declareRelExpr) {}

  @Override
  public void visitDeclareVarExpr(DeclareVarExpr declareVarExpr) {}

  @Override
  public void visitEqExpr(EqExpr eqExpr) {
    eqExpr.getLhs().accept(this);
    eqExpr.getRhs().accept(this);
  }

  @Override
  public void visitExpandedListExpr(ExpandedListExpr expandedListExpr) {
    expandedListExpr.getSubExpressions().forEach(expr -> expr.accept(this));
  }

  @Override
  public void visitExpr(Expr expr) {
    expr.accept(this);
  }

  @Override
  public void visitExtractExpr(ExtractExpr extractExpr) {
    _vars.add(extractExpr.getVar().getHeaderField());
  }

  @Override
  public void visitFalseExpr(FalseExpr falseExpr) {}

  @Override
  public void visitHeaderSpaceMatchExpr(HeaderSpaceMatchExpr headerSpaceMatchExpr) {
    headerSpaceMatchExpr.getExpr().accept(this);
  }

  @Override
  public void visitIdExpr(IdExpr idExpr) {}

  @Override
  public void visitIfExpr(IfExpr ifExpr) {
    ifExpr.getAntecedent().accept(this);
    ifExpr.getConsequent().accept(this);
  }

  @Override
  public void visitLitIntExpr(LitIntExpr litIntExpr) {}

  @Override
  public void visitNotExpr(NotExpr notExpr) {
    notExpr.getArg().accept(this);
  }

  @Override
  public void visitOrExpr(OrExpr orExpr) {
    orExpr.getDisjuncts().forEach(expr -> expr.accept(this));
  }

  @Override
  public void visitPrefixMatchExpr(PrefixMatchExpr prefixMatchExpr) {
    prefixMatchExpr.getExpr().accept(this);
  }

  @Override
  public void visitQueryExpr(QueryExpr queryExpr) {
    queryExpr.getSubExpression().accept(this);
  }

  @Override
  public void visitRangeMatchExpr(RangeMatchExpr rangeMatchExpr) {
    rangeMatchExpr.getExpr().accept(this);
  }

  @Override
  public void visitRuleExpr(RuleExpr ruleExpr) {
    ruleExpr.getSubExpression().accept(this);
  }

  @Override
  public void visitSaneExpr(SaneExpr saneExpr) {
    saneExpr.getExpr().accept(this);
  }

  @Override
  public <T extends State<T, ?>, P extends StateParameterization<T>> void visitStateExpr(
      StateExpr<T, P> stateExpr) {
    StateExpr.VARIABLES.forEach(expr -> expr.accept(this));
  }

  @Override
  public void visitTrueExpr(TrueExpr trueExpr) {}

  @Override
  public void visitVarIntExpr(VarIntExpr varIntExpr) {
    _vars.add(varIntExpr.getHeaderField());
  }
}
