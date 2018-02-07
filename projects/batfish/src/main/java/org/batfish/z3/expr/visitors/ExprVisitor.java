package org.batfish.z3.expr.visitors;

import org.batfish.z3.expr.AndExpr;
import org.batfish.z3.expr.BitVecExpr;
import org.batfish.z3.expr.CollapsedListExpr;
import org.batfish.z3.expr.Comment;
import org.batfish.z3.expr.DeclareRelExpr;
import org.batfish.z3.expr.DeclareVarExpr;
import org.batfish.z3.expr.EqExpr;
import org.batfish.z3.expr.ExpandedListExpr;
import org.batfish.z3.expr.Expr;
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
