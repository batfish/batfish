package org.batfish.datamodel.assertion;

import java.util.List;

public class Or implements BooleanExpr {

  private List<BooleanExpr> _disjuncts;

  public Or(List<BooleanExpr> disjuncts) {
    _disjuncts = disjuncts;
  }

  @Override
  public Boolean evaluate(Environment env) {
    for (BooleanExpr conjunct : _disjuncts) {
      if (conjunct.evaluate(env)) {
        return true;
      }
    }
    return false;
  }
}
