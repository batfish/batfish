package org.batfish.z3;

import com.microsoft.z3.BitVecExpr;
import com.microsoft.z3.BoolExpr;
import java.util.Map;

public class SmtInput {
  public final BoolExpr _expr;
  public final Map<String, BitVecExpr> _variablesAsConsts;

  public SmtInput(BoolExpr expr, Map<String, BitVecExpr> variablesAsConsts) {
    _expr = expr;
    _variablesAsConsts = variablesAsConsts;
  }
}
