package org.batfish.z3.node;

import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Z3Exception;
import org.batfish.z3.NodProgram;

public class FalseExpr extends BooleanExpr {

  public static final FalseExpr INSTANCE = new FalseExpr();

  private FalseExpr() {}

  @Override
  public void print(StringBuilder sb, int indent) {
    sb.append("false");
  }

  @Override
  public BoolExpr toBoolExpr(NodProgram nodProgram) throws Z3Exception {
    return nodProgram.getContext().mkFalse();
  }
}
