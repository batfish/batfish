package org.batfish.datamodel.assertion;

import org.batfish.common.BatfishException;

public class Gt implements BooleanExpr {

  private ComparableExpr _lhs;

  private ComparableExpr _rhs;

  public Gt(ComparableExpr lhs, ComparableExpr rhs) {
    _lhs = lhs;
    _rhs = rhs;
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  @Override
  public Boolean evaluate(Environment env) {
    Comparable lhs = _lhs.evaluate(env);
    Comparable rhs = _rhs.evaluate(env);
    if (!lhs.getClass().equals(rhs.getClass())) {
      throw new BatfishException("Class mismatch: lhs: '" + lhs + "', rhs: '" + rhs + "'");
    }
    return lhs.compareTo(rhs) > 0;
  }
}
