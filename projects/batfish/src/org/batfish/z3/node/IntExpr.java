package org.batfish.z3.node;

import org.batfish.z3.NodProgram;

import com.microsoft.z3.Z3Exception;

public abstract class IntExpr extends Expr {

   public abstract com.microsoft.z3.BitVecExpr toBitVecExpr(
         NodProgram nodProgram) throws Z3Exception;
}
