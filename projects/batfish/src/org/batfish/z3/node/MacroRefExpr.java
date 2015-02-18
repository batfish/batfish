package org.batfish.z3.node;

public class MacroRefExpr extends BooleanExpr {

   private String _macro;

   public MacroRefExpr(String macro) {
      _macro = macro;
   }

   @Override
   public void print(StringBuilder sb, int indent) {
      sb.append(_macro);
   }

}
