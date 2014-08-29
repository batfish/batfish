package batfish.z3;

import java.util.ArrayList;
import java.util.List;

import batfish.z3.node.AssertExpr;
import batfish.z3.node.BooleanExpr;
import batfish.z3.node.CheckSatExpr;
import batfish.z3.node.GetModelExpr;
import batfish.z3.node.Statement;

public class ConcretizerQuery {

   private List<Statement> _statements;

   public ConcretizerQuery(BooleanExpr booleanExpr) {
      _statements = new ArrayList<Statement>();
      for (Statement varDecl : Synthesizer.getVarDeclExprs()) {
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
