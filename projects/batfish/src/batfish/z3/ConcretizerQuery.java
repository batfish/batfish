package batfish.z3;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import batfish.collections.VarSizeMap;
import batfish.z3.node.AssertExpr;
import batfish.z3.node.BooleanExpr;
import batfish.z3.node.CheckSatExpr;
import batfish.z3.node.DeclareVarExpr;
import batfish.z3.node.GetModelExpr;
import batfish.z3.node.Statement;

public class ConcretizerQuery {

   private List<Statement> _statements;

   public ConcretizerQuery(BooleanExpr booleanExpr, VarSizeMap varSizeMap) {
      _statements = new ArrayList<Statement>();
      Set<String> vars = booleanExpr.getVariables();
      for (String var : vars) {
         int varSize = varSizeMap.get(var);
         DeclareVarExpr varDecl = new DeclareVarExpr(var, varSize);
         _statements.add(varDecl);
      }
      _statements.add(new AssertExpr(booleanExpr));
      _statements.add(CheckSatExpr.INSTANCE);
      _statements.add(GetModelExpr.INSTANCE);
   }

   public ConcretizerQuery(List<Statement> statements) {
      _statements = statements;
   }

   public String getText() {
      StringBuilder sb = new StringBuilder();
      for (Statement statement : _statements) {
         statement.print(sb, 0);
         sb.append("\n");
      }
      return sb.toString();
   }

}
