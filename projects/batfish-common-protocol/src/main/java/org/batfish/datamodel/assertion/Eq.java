package org.batfish.datamodel.assertion;

import org.batfish.common.BatfishException;

public class Eq implements BooleanExpr {

  private Expr _lhs;

  private Expr _rhs;

  public Eq(Expr lhs, Expr rhs) {
    _lhs = lhs;
    _rhs = rhs;
  }

  @Override
  public Boolean evaluate(Environment env) {
    Object lhs = _lhs.evaluate(env);
    Object rhs = _rhs.evaluate(env);
    if (!lhs.getClass().equals(rhs.getClass())) {
      throw new BatfishException("Class mismatch: lhs: '" + lhs + "', rhs: '" + rhs + "'");
    }
    return lhs.equals(rhs);
  }
}
