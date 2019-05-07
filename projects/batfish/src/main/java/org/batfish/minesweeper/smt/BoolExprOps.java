package org.batfish.minesweeper.smt;

import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Context;
import java.util.Arrays;
import java.util.Objects;

public class BoolExprOps {
  private final Context _context;

  public BoolExprOps(Context context) {
    _context = context;
  }

  public BoolExpr and(BoolExpr... conjuncts) {
    return andNonNull(Arrays.stream(conjuncts).filter(Objects::nonNull).toArray(BoolExpr[]::new));
  }

  private BoolExpr andNonNull(BoolExpr... conjuncts) {
    return conjuncts.length == 0 ? _context.mkTrue() : _context.mkAnd(conjuncts);
  }

  public BoolExpr or(BoolExpr... disjuncts) {
    return orNonNull(Arrays.stream(disjuncts).filter(Objects::nonNull).toArray(BoolExpr[]::new));
  }

  private BoolExpr orNonNull(BoolExpr... disjuncts) {
    return disjuncts.length == 0 ? _context.mkFalse() : _context.mkOr(disjuncts);
  }
}
