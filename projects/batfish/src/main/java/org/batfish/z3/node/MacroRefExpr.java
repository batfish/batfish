package org.batfish.z3.node;

import org.batfish.z3.NodProgram;

import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Z3Exception;

public class MacroRefExpr extends BooleanExpr {

   private String _macro;

   public MacroRefExpr(String macro) {
      _macro = macro;
   }

   @Override
   public void print(StringBuilder sb, int indent) {
      sb.append(_macro);
   }

   @Override
   public BoolExpr toBoolExpr(NodProgram nodProgram) throws Z3Exception {
      throw new UnsupportedOperationException(
            "no implementation for generated method"); // TODO Auto-generated
                                                       // method stub
   }

}
