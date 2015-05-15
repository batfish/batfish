package org.batfish.z3.node;

import java.util.ArrayList;
import java.util.List;

import org.batfish.z3.NodProgram;

import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Z3Exception;

public class LetExpr extends BooleanExpr implements ComplexExpr {

   private BooleanExpr _expression;
   private List<MacroDefExpr> _macroDefs;
   private List<Expr> _subExpressions;

   public LetExpr(List<MacroDefExpr> macroDefs, BooleanExpr b) {
      _macroDefs = macroDefs;
      _expression = b;
      _subExpressions = new ArrayList<Expr>();
      _printer = new ExpandedComplexExprPrinter(this);
      refreshSubexpressions();
   }

   public BooleanExpr getExpression() {
      return _expression;
   }

   public List<MacroDefExpr> getMacroDefs() {
      return _macroDefs;
   }

   @Override
   public List<Expr> getSubExpressions() {
      return _subExpressions;
   }

   private void refreshSubexpressions() {
      _subExpressions.clear();
      _subExpressions.add(new IdExpr("let"));
      ListExpr list = new ExpandedListExpr();
      _subExpressions.add(list);
      _subExpressions.add(_expression);
      for (MacroDefExpr macroDefExpr : _macroDefs) {
         list.addSubExpression(macroDefExpr);
      }
   }

   @Override
   public BoolExpr toBoolExpr(NodProgram nodProgram) throws Z3Exception {
      throw new UnsupportedOperationException(
            "no implementation for generated method"); // TODO Auto-generated
                                                       // method stub
   }

}
