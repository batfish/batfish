package org.batfish.z3.node;

import com.microsoft.z3.Z3Exception;
import org.batfish.z3.NodProgram;

public abstract class IntExpr extends Expr {

  public abstract com.microsoft.z3.BitVecExpr toBitVecExpr(NodProgram nodProgram)
      throws Z3Exception;
}
