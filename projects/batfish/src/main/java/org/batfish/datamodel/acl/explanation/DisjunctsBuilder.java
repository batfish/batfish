package org.batfish.datamodel.acl.explanation;

import static org.batfish.datamodel.acl.AclLineMatchExprs.TRUE;
import static org.batfish.datamodel.acl.AclLineMatchExprs.or;

import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;
import org.batfish.common.bdd.AclLineMatchExprToBDD;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.OrMatchExpr;

/**
 * A builder for the disjuncts of an {@link OrMatchExpr}. Uses {@link BDD BDDs} to remove redundant
 * disjuncts and to short-circuit when the set of disjuncts is valid.
 */
public final class DisjunctsBuilder extends AclLineMatchExprSetBuilder {
  BDD _zero;
  BDD _one;

  public DisjunctsBuilder(AclLineMatchExprToBDD aclLineMatchExprToBDD) {
    super(aclLineMatchExprToBDD, aclLineMatchExprToBDD.getBDDPacket().getFactory().zero());
    BDDFactory factory = aclLineMatchExprToBDD.getBDDPacket().getFactory();
    _one = factory.one();
    _zero = factory.zero();
  }

  @Override
  protected BDD identity() {
    return _zero;
  }

  @Override
  protected BDD combinator(BDD bdd1, BDD bdd2) {
    return bdd1.or(bdd2);
  }

  @Override
  protected BDD shortCircuitBDD() {
    return _one;
  }

  @Override
  public AclLineMatchExpr build() {
    return getBdd().isOne() ? TRUE : or(getExprs());
  }
}
