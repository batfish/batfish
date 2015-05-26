package org.batfish.z3.node;

import org.batfish.z3.NodProgram;

import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Z3Exception;

public abstract class BooleanExpr extends Expr {

   @Override
   public BooleanExpr simplify() {
      return this;
   }

   public abstract BoolExpr toBoolExpr(NodProgram nodProgram)
         throws Z3Exception;

}
