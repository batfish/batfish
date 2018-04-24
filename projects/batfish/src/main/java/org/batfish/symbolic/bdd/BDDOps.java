package org.batfish.symbolic.bdd;

import java.util.Arrays;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;

public class BDDOps {
  private final BDDFactory _factory;

  public BDDOps(BDDFactory factory) {
    _factory = factory;
  }

  public BDD and(BDD... conjuncts) {
    return and(Arrays.asList(conjuncts));
  }

  public BDD and(Iterable<BDD> conjuncts) {
    BDD result = _factory.one();
    for (BDD conjunct : conjuncts) {
      if (conjunct != null) {
        result = result.and(conjunct);
      }
    }
    return result;
  }

  public BDD or(BDD... disjuncts) {
    return or(Arrays.asList(disjuncts));
  }

  public BDD or(Iterable<BDD> disjuncts) {
    BDD result = _factory.zero();
    for (BDD disjunct : disjuncts) {
      if (disjunct != null) {
        result = result.or(disjunct);
      }
    }
    return result;
  }
}
