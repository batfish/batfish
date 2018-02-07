package org.batfish.z3.expr;

import org.batfish.z3.state.State;
import org.batfish.z3.state.State.StateExpr;
import org.batfish.z3.state.StateParameterization;

public interface ExprVisitor {
  void visitAndExpr(AndExpr andExpr);

  void visitBitVecExpr(BitVecExpr bitVecExpr);

  void visitCollapsedListExpr(CollapsedListExpr collapsedListExpr);

  void visitComment(Comment comment);

  void visitDeclareRelExpr(DeclareRelExpr declareRelExpr);

  void visitDeclareVarExpr(DeclareVarExpr declareVarExpr);

  void visitEqExpr(EqExpr eqExpr);

  void visitExpandedListExpr(ExpandedListExpr expandedListExpr);

  void visitExpr(Expr expr);

  void visitExtractExpr(ExtractExpr extractExpr);

  void visitFalseExpr(FalseExpr falseExpr);

  void visitHeaderSpaceMatchExpr(HeaderSpaceMatchExpr headerSpaceMatchExpr);

  void visitIdExpr(IdExpr idExpr);

  void visitIfExpr(IfExpr ifExpr);

  void visitLitIntExpr(LitIntExpr litIntExpr);

  void visitNotExpr(NotExpr notExpr);

  void visitOrExpr(OrExpr orExpr);

  void visitPrefixMatchExpr(PrefixMatchExpr prefixMatchExpr);

  void visitQueryExpr(QueryExpr queryExpr);

  void visitRangeMatchExpr(RangeMatchExpr rangeMatchExpr);

  void visitRuleExpr(RuleExpr ruleExpr);

  void visitSaneExpr(SaneExpr saneExpr);

  <T extends State<T, ?>, P extends StateParameterization<T>> void visitStateExpr(
      StateExpr<T, P> stateExpr);

  void visitTrueExpr(TrueExpr trueExpr);

  void visitVarIntExpr(VarIntExpr varIntExpr);
}
