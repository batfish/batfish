package org.batfish.datamodel.acl.explanation;

import static org.batfish.datamodel.acl.AclLineMatchExprs.FALSE;
import static org.batfish.datamodel.acl.AclLineMatchExprs.and;

import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;
import org.batfish.common.bdd.IpAccessListToBDD;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.AndMatchExpr;

/**
 * A builder for the conjuncts of an {@link AndMatchExpr}. Uses {@link BDD BDDs} to remove redundant
 * conjuncts and to short-circuit when the set of disjuncts is unsatisfiable.
 */
public final class ConjunctsBuilder extends AclLineMatchExprSetBuilder {
  private final BDD _one;
  private final BDD _zero;

  public ConjunctsBuilder(IpAccessListToBDD ipAccessListToBDD) {
    super(ipAccessListToBDD, ipAccessListToBDD.getBDDPacket().getFactory().one());
    BDDFactory factory = ipAccessListToBDD.getBDDPacket().getFactory();
    _one = factory.one();
    _zero = factory.zero();
  }

  public ConjunctsBuilder(ConjunctsBuilder other) {
    super(other);
    _one = other._one;
    _zero = other._zero;
  }

  @Override
  protected BDD identity() {
    return _one;
  }

  @Override
  protected BDD combinator(BDD bdd1, BDD bdd2) {
    return bdd1.and(bdd2);
  }

  @Override
  protected BDD shortCircuitBDD() {
    return _zero;
  }

  @Override
  public AclLineMatchExpr build() {
    return getBdd().isZero() ? FALSE : and(getExprs());
  }

  public boolean unsat() {
    return getBdd().isZero();
  }
}
