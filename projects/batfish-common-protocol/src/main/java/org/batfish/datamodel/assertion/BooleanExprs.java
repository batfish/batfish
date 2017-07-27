package org.batfish.datamodel.assertion;

import org.batfish.common.BatfishException;

public enum BooleanExprs implements BooleanExpr {
  FALSE,
  TRUE;

  @Override
  public Boolean evaluate(Environment env) {
    switch (this) {
      case FALSE:
        return false;

      case TRUE:
        return true;

      default:
        throw new BatfishException("Invalid " + BooleanExprs.class.getSimpleName());
    }
  }
}
