package org.batfish.datamodel.assertion;

import java.util.List;

public class And implements BooleanExpr {

  private List<BooleanExpr> _conjuncts;

  public And(List<BooleanExpr> conjuncts) {
    _conjuncts = conjuncts;
  }

  @Override
  public Boolean evaluate(Environment env) {
    for (BooleanExpr conjunct : _conjuncts) {
      if (!conjunct.evaluate(env)) {
        return false;
      }
    }
    return true;
  }
}
