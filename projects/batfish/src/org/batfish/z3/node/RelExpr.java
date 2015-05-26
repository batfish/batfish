package org.batfish.z3.node;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.batfish.z3.NodProgram;

import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Context;
import com.microsoft.z3.FuncDecl;
import com.microsoft.z3.Z3Exception;

public class RelExpr extends BooleanExpr implements ComplexExpr {

   private List<IntExpr> _args;
   private String _name;
   private List<Expr> _subExpressions;

   public RelExpr(String name) {
      _name = name;
      _subExpressions = new ArrayList<Expr>();
      _args = new ArrayList<IntExpr>();
      _subExpressions.add(new IdExpr(name));
      _printer = new CollapsedComplexExprPrinter(this);
   }

   public void addArgument(IntExpr arg) {
      _subExpressions.add(arg);
      _args.add(arg);
   }

   @Override
   public Set<String> getRelations() {
      return Collections.singleton(_name);
   }

   @Override
   public List<Expr> getSubExpressions() {
      return _subExpressions;
   }

   @Override
   public Set<String> getVariables() {
      Set<String> variables = new HashSet<String>();
      for (Expr subExpression : _subExpressions) {
         variables.addAll(subExpression.getVariables());
      }
      return variables;
   }

   @Override
   public BoolExpr toBoolExpr(NodProgram nodProgram) throws Z3Exception {
      Context ctx = nodProgram.getContext();
      FuncDecl funcDecl = nodProgram.getRelationDeclarations().get(_name);
      List<com.microsoft.z3.Expr> args = new ArrayList<com.microsoft.z3.Expr>();
      for (IntExpr arg : _args) {
         args.add(arg.toBitVecExpr(nodProgram));
      }
      com.microsoft.z3.Expr result = ctx.mkApp(funcDecl,
            args.toArray(new com.microsoft.z3.Expr[] {}));
      return (BoolExpr) result;
   }

}
