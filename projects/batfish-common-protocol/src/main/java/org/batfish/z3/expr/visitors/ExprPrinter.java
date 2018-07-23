package org.batfish.z3.expr.visitors;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.List;
import org.batfish.z3.expr.AndExpr;
import org.batfish.z3.expr.BasicRuleStatement;
import org.batfish.z3.expr.BitVecExpr;
import org.batfish.z3.expr.Comment;
import org.batfish.z3.expr.EqExpr;
import org.batfish.z3.expr.Expr;
import org.batfish.z3.expr.ExtractExpr;
import org.batfish.z3.expr.FalseExpr;
import org.batfish.z3.expr.HeaderSpaceMatchExpr;
import org.batfish.z3.expr.IdExpr;
import org.batfish.z3.expr.IfExpr;
import org.batfish.z3.expr.IfThenElse;
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
import org.batfish.z3.expr.VoidStatementVisitor;

public class ExprPrinter implements ExprVisitor, VoidStatementVisitor {

  public static String print(Expr expr) {
    ExprPrinter printer = new ExprPrinter();
    expr.accept(printer);
    return printer._sb.toString();
  }

  public static String print(Statement statement) {
    ExprPrinter printer = new ExprPrinter();
    statement.accept(printer);
    return printer._sb.toString();
  }

  private final int _indent;

  private final StringBuilder _sb;

  private ExprPrinter() {
    this(new StringBuilder(), 0);
  }

  private ExprPrinter(StringBuilder sb, int indent) {
    _sb = sb;
    _indent = indent;
  }

  private void printCollapsedComplexExpr(List<Expr> subExpressions) {
    _sb.append("(");
    int size = subExpressions.size();
    if (size > 0) {
      printExpr(subExpressions.get(0));
      for (int i = 1; i < size; i++) {
        _sb.append(" ");
        printExpr(subExpressions.get(i));
      }
      Expr lastSubExpression = subExpressions.get(size - 1);
      if (IsComplexVisitor.isComplexExpr(lastSubExpression)) {
        _sb.append(" ");
      }
    }
    _sb.append(")");
  }

  private void printExpandedComplexExpr(List<Expr> subExpressions) {
    _sb.append("(");
    int size = subExpressions.size();
    if (size > 0) {
      printExpr(subExpressions.get(0));
      for (int i = 1; i < size; i++) {
        _sb.append("\n");
        for (int j = 0; j <= _indent; j++) {
          _sb.append(" ");
        }
        printExpr(subExpressions.get(i), _indent + 1);
      }
      Expr lastSubExpression = subExpressions.get(size - 1);
      if (IsComplexVisitor.isComplexExpr(lastSubExpression)) {
        _sb.append(" ");
      }
    }
    _sb.append(")");
  }

  private void printExpr(Expr expr) {
    expr.accept(this);
  }

  private void printExpr(Expr expr, int indent) {
    expr.accept(new ExprPrinter(_sb, indent));
  }

  @Override
  public void visitAndExpr(AndExpr andExpr) {
    List<Expr> subExpressions =
        ImmutableList.<Expr>builder().add(new IdExpr("and")).addAll(andExpr.getConjuncts()).build();
    printExpandedComplexExpr(subExpressions);
  }

  @Override
  public void visitBasicRuleStatement(BasicRuleStatement basicRuleStatement) {
    printCollapsedComplexExpr(
        ImmutableList.of(
            new IdExpr("basic-rule"),
            basicRuleStatement.getPreconditionStateIndependentConstraints(),
            new ListExpr(ImmutableList.copyOf(basicRuleStatement.getPreconditionStates())),
            basicRuleStatement.getPostconditionState()));
  }

  @Override
  public void visitBitVecExpr(BitVecExpr bitVecExpr) {
    List<Expr> subExpressions =
        ImmutableList.of(
            new IdExpr("_"),
            new IdExpr("BitVec"),
            new IdExpr(Integer.toString(bitVecExpr.getSize())));
    printCollapsedComplexExpr(subExpressions);
  }

  @Override
  public void visitComment(Comment comment) {
    _sb.append("\n");
    for (String line : comment.getLines()) {
      _sb.append(String.format(";;; %s\n", line));
    }
  }

  @Override
  public void visitEqExpr(EqExpr eqExpr) {
    printCollapsedComplexExpr(ImmutableList.of(new IdExpr("="), eqExpr.getLhs(), eqExpr.getRhs()));
  }

  @Override
  public void visitExtractExpr(ExtractExpr extractExpr) {
    printCollapsedComplexExpr(
        ImmutableList.of(
            new ListExpr(
                ImmutableList.of(
                    new IdExpr("_"),
                    new IdExpr("extract"),
                    new IdExpr(Integer.toString(extractExpr.getHigh())),
                    new IdExpr(Integer.toString(extractExpr.getLow())))),
            extractExpr.getVar()));
  }

  @Override
  public void visitFalseExpr(FalseExpr falseExpr) {
    _sb.append("false");
  }

  @Override
  public void visitHeaderSpaceMatchExpr(HeaderSpaceMatchExpr headerSpaceMatchExpr) {
    printCollapsedComplexExpr(
        ImmutableList.of(new IdExpr("headerSpaceMatch"), headerSpaceMatchExpr.getExpr()));
  }

  @Override
  public void visitIdExpr(IdExpr idExpr) {
    _sb.append(idExpr.getId());
  }

  @Override
  public void visitIfExpr(IfExpr ifExpr) {
    printExpandedComplexExpr(
        ImmutableList.of(new IdExpr("=>"), ifExpr.getAntecedent(), ifExpr.getConsequent()));
  }

  @Override
  public void visitIfThenElse(IfThenElse ifThenElse) {
    printCollapsedComplexExpr(
        ImmutableList.of(
            new IdExpr("ite"),
            ifThenElse.getCondition(),
            ifThenElse.getThen(),
            ifThenElse.getElse()));
  }

  @Override
  public void visitIpSpaceMatchExpr(IpSpaceMatchExpr ipSpaceMatchExpr) {
    printCollapsedComplexExpr(
        ImmutableList.of(new IdExpr("ipSpaceMatch"), ipSpaceMatchExpr.getExpr()));
  }

  @Override
  public void visitListExpr(ListExpr listExpr) {
    printCollapsedComplexExpr(listExpr.getSubExpressions());
  }

  @Override
  public void visitLitIntExpr(LitIntExpr litIntExpr) {
    int bits = litIntExpr.getBits();
    long num = litIntExpr.getNum();
    String numString;
    if (bits % 4 == 0) {
      // hex
      int numNibbles = bits / 4;
      numString = "#x" + String.format("%0" + numNibbles + "x", num);
    } else {
      // bin
      StringBuilder numStringBuilder = new StringBuilder();
      numStringBuilder.append("#b");
      for (int pos = bits - 1; pos >= 0; pos--) {
        long mask = 1L << pos;
        long bit = num & mask;
        numStringBuilder.append(Integer.toString((bit != 0) ? 1 : 0));
      }
      numString = numStringBuilder.toString();
    }
    _sb.append(numString);
  }

  @Override
  public void visitNotExpr(NotExpr notExpr) {
    printCollapsedComplexExpr(ImmutableList.of(new IdExpr("not"), notExpr.getArg()));
  }

  @Override
  public void visitOrExpr(OrExpr orExpr) {
    printExpandedComplexExpr(
        ImmutableList.<Expr>builder().add(new IdExpr("or")).addAll(orExpr.getDisjuncts()).build());
  }

  @Override
  public void visitPrefixMatchExpr(PrefixMatchExpr prefixMatchExpr) {
    printCollapsedComplexExpr(
        ImmutableList.of(new IdExpr("prefixMatch"), prefixMatchExpr.getExpr()));
  }

  @Override
  public void visitQueryStatement(QueryStatement queryStatement) {
    printCollapsedComplexExpr(ImmutableList.of(new IdExpr("query"), queryStatement.getStateExpr()));
  }

  @Override
  public void visitRangeMatchExpr(RangeMatchExpr rangeMatchExpr) {
    printCollapsedComplexExpr(ImmutableList.of(new IdExpr("rangeMatch"), rangeMatchExpr.getExpr()));
  }

  @Override
  public void visitStateExpr(StateExpr stateExpr) {
    /* TODO: handle vectorized state parameters as variables */
    /* TODO: handle arguments */
    printCollapsedComplexExpr(
        ImmutableList.<Expr>builder()
            .add(new IdExpr(BoolExprTransformer.getNodName(ImmutableSet.of(), stateExpr)))
            .build());
  }

  @Override
  public void visitTrueExpr(TrueExpr trueExpr) {
    _sb.append("true");
  }

  @Override
  public void visitVarIntExpr(VarIntExpr varIntExpr) {
    _sb.append(varIntExpr.getField().getName());
  }

  @Override
  public void visitTransformedVarIntExpr(TransformedVarIntExpr transformedVarIntExpr) {
    _sb.append(String.format("Transformed(%s)", transformedVarIntExpr.getField().getName()));
  }
}
