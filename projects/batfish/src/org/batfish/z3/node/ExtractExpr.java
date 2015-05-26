package org.batfish.z3.node;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.batfish.z3.NodProgram;

import com.microsoft.z3.BitVecExpr;
import com.microsoft.z3.Context;
import com.microsoft.z3.Z3Exception;

public class ExtractExpr extends IntExpr implements ComplexExpr {

   private int _high;
   private int _low;
   private List<Expr> _subExpressions;
   private VarIntExpr _var;

   public ExtractExpr(String var, int low, int high) {
      _low = low;
      _high = high;
      _subExpressions = new ArrayList<Expr>();
      ListExpr listExpr = new CollapsedListExpr();
      listExpr.addSubExpression(new IdExpr("_"));
      listExpr.addSubExpression(new IdExpr("extract"));
      listExpr.addSubExpression(new IdExpr(Integer.toString(high)));
      listExpr.addSubExpression(new IdExpr(Integer.toString(low)));
      _subExpressions.add(listExpr);
      _var = new VarIntExpr(var);
      _subExpressions.add(_var);
      _printer = new CollapsedComplexExprPrinter(this);
   }

   @Override
   public List<Expr> getSubExpressions() {
      return _subExpressions;
   }

   @Override
   public Set<String> getVariables() {
      return _var.getVariables();
   }

   @Override
   public BitVecExpr toBitVecExpr(NodProgram nodProgram) throws Z3Exception {
      Context ctx = nodProgram.getContext();
      BitVecExpr bvArg = _var.toBitVecExpr(nodProgram);
      BitVecExpr result = ctx.mkExtract(_high, _low, bvArg);
      return result;
   }

}
