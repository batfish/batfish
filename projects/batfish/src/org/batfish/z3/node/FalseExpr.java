package org.batfish.z3.node;

import org.batfish.z3.NodProgram;

import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Context;
import com.microsoft.z3.Z3Exception;

public class FalseExpr extends BooleanExpr {

   public static final FalseExpr INSTANCE = new FalseExpr();

   private FalseExpr() {
      _printer = new SimpleExprPrinter("false");
   }

   @Override
   public BoolExpr toBoolExpr(NodProgram nodProgram) throws Z3Exception {
      throw new UnsupportedOperationException("no implementation for generated method"); // TODO Auto-generated method stub
   }

}
