package org.batfish.z3.node;

import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Z3Exception;
import org.batfish.z3.NodProgram;

public class TrueExpr extends BooleanExpr {

  public static final TrueExpr INSTANCE = new TrueExpr();

  private TrueExpr() {}

  @Override
  public void print(StringBuilder sb, int indent) {
    sb.append("true");
  }

  @Override
  public BoolExpr toBoolExpr(NodProgram nodProgram) throws Z3Exception {
    return nodProgram.getContext().mkTrue();
  }
}
