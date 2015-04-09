package org.batfish.z3.node;

import org.batfish.z3.NodProgram;

import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Z3Exception;

public class TrueExpr extends BooleanExpr {

   public static final TrueExpr INSTANCE = new TrueExpr();

   private TrueExpr() {
      _printer = new SimpleExprPrinter("true");
   }

   @Override
   public BoolExpr toBoolExpr(NodProgram nodProgram) throws Z3Exception {
      return nodProgram.getContext().mkTrue();
   }

}
