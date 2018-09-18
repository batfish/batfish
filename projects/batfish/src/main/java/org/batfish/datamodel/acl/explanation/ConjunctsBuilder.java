package org.batfish.datamodel.acl.explanation;

import static org.batfish.datamodel.acl.AclLineMatchExprs.FALSE;
import static org.batfish.datamodel.acl.AclLineMatchExprs.and;

import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;
import org.batfish.common.bdd.AclLineMatchExprToBDD;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.AndMatchExpr;
import org.batfish.datamodel.acl.NotMatchExpr;
import org.batfish.datamodel.acl.normalize.Negate;

/**
 * A builder for the conjuncts of an {@link AndMatchExpr}. Uses {@link BDD BDDs} to remove redundant
 * conjuncts and to short-circuit when the set of disjuncts is unsatisfiable.
 */
public final class ConjunctsBuilder extends AclLineMatchExprSetBuilder {
  private final AclLineMatchExprToBDD _aclLineMatchExprToBDD;
  private final BDD _one;
  private final BDD _zero;

  public ConjunctsBuilder(AclLineMatchExprToBDD aclLineMatchExprToBDD) {
    super(aclLineMatchExprToBDD, aclLineMatchExprToBDD.getBDDPacket().getFactory().one());
    BDDFactory factory = aclLineMatchExprToBDD.getBDDPacket().getFactory();
    _aclLineMatchExprToBDD = aclLineMatchExprToBDD;
    _one = factory.one();
    _zero = factory.zero();
  }

  public ConjunctsBuilder(ConjunctsBuilder other) {
    super(other);
    _aclLineMatchExprToBDD = other._aclLineMatchExprToBDD;
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

  @Override
  public void add(AclLineMatchExpr expr) {
    /*
     * If expr is an AndMatchExpr (or can be simplified by one using deMorgan's law, then add each
     * conjunct separately (so we can detect and remove redundant conjuncts). This could create some
     * extra work though, so only do this if the conjunction won't be unsat after adding.
     */
    if (_aclLineMatchExprToBDD.visit(expr).and(getBdd()).isZero()) {
      /*
       * expr is inconsistent with the other conjuncts. Just add it now.
       */
      addHelper(expr);
      return;
    }

    AclLineMatchExpr e = expr;
    if (e instanceof NotMatchExpr) {
      /*
       * Try to apply deMorgan.
       */
      e = Negate.negate(((NotMatchExpr) e).getOperand());
    }
    if (e instanceof AndMatchExpr) {
      ((AndMatchExpr) e).getConjuncts().forEach(this::add);
    } else {
      addHelper(e);
    }
  }

  public boolean unsat() {
    return getBdd().isZero();
  }
}
