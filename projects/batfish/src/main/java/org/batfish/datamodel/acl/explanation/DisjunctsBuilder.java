package org.batfish.datamodel.acl.explanation;

import static org.batfish.datamodel.acl.AclLineMatchExprs.TRUE;
import static org.batfish.datamodel.acl.AclLineMatchExprs.or;

import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;
import org.batfish.common.bdd.IpAccessListToBDD;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.NotMatchExpr;
import org.batfish.datamodel.acl.OrMatchExpr;
import org.batfish.datamodel.acl.normalize.Negate;

/**
 * A builder for the disjuncts of an {@link OrMatchExpr}. Uses {@link BDD BDDs} to remove redundant
 * disjuncts and to short-circuit when the set of disjuncts is valid.
 */
public final class DisjunctsBuilder extends AclLineMatchExprSetBuilder {
  private final IpAccessListToBDD _ipAccessListToBDD;
  private final BDD _zero;
  private final BDD _one;

  public DisjunctsBuilder(IpAccessListToBDD ipAccessListToBDD) {
    super(ipAccessListToBDD, ipAccessListToBDD.getBDDPacket().getFactory().zero());
    BDDFactory factory = ipAccessListToBDD.getBDDPacket().getFactory();
    _ipAccessListToBDD = ipAccessListToBDD;
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

  @Override
  public void add(AclLineMatchExpr expr) {
    /*
     * If expr is an OrMatchExpr (or can be simplified by one using deMorgan's law, then add each
     * disjunct separately (so we can detect and remove redundant disjuncts). This could create some
     * extra work though, so only do this if the conjunction won't be valid after adding.
     */
    if (_ipAccessListToBDD.visit(expr).or(getBdd()).isOne()) {
      /*
       * expr is valid with the other conjuncts. Just add it now.
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
    if (e instanceof OrMatchExpr) {
      ((OrMatchExpr) e).getDisjuncts().forEach(this::add);
    } else {
      addHelper(e);
    }
  }
}
