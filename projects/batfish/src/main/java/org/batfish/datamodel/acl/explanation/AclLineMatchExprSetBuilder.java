package org.batfish.datamodel.acl.explanation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.sf.javabdd.BDD;
import org.batfish.common.bdd.IpAccessListToBDD;
import org.batfish.datamodel.acl.AclLineMatchExpr;

/**
 * Represents a set of {@link AclLineMatchExpr AclLineMatchExprs} -- either a set of conjuncts or a
 * set of disjuncts. Uses {@link BDD BDDs} to detect and remove redundant members.
 */
public abstract class AclLineMatchExprSetBuilder {

  private final IpAccessListToBDD _ipAccessListToBDD;
  private final Map<AclLineMatchExpr, BDD> _exprs;
  private BDD _bdd;
  private BDD _orExprBDDs;

  protected AclLineMatchExprSetBuilder(IpAccessListToBDD ipAccessListToBDD, BDD identity) {
    _ipAccessListToBDD = ipAccessListToBDD;
    _exprs = new HashMap<>();
    _bdd = identity;
    _orExprBDDs = identity.getFactory().zero();
  }

  public AclLineMatchExprSetBuilder(AclLineMatchExprSetBuilder other) {
    _ipAccessListToBDD = other._ipAccessListToBDD;
    _exprs = new HashMap<>(other._exprs);
    _bdd = other._bdd;
    _orExprBDDs = other._orExprBDDs;
  }

  protected abstract BDD identity();

  protected abstract BDD combinator(BDD bdd1, BDD bdd2);

  protected abstract BDD shortCircuitBDD();

  public abstract AclLineMatchExpr build();

  /** This is the public method to add elements. Abstract so that subclasses can preprocess. */
  public abstract void add(AclLineMatchExpr expr);

  /** Helper method for subclasses to use to implement {@link AclLineMatchExprSetBuilder#add}. */
  protected void addHelper(AclLineMatchExpr expr) {
    if (_bdd.equals(shortCircuitBDD()) || _exprs.containsKey(expr)) {
      return;
    }

    BDD exprBdd = _ipAccessListToBDD.visit(expr);
    BDD newBdd = combinator(_bdd, exprBdd);
    if (newBdd.equals(_bdd)) {
      // expr contributes nothing to the set; discard
      return;
    }

    /*
     * Check if expr makes some other element in the set redundant, and if so remove it. This is
     * a linear-time check (linear number of BDD operations), so we want to avoid it if possible.
     * We do this by maintaining _orExprBDDs (the disjunction of the BDDs in _exprs). If exprBdd
     * is disjoint from that intersection, we know nothing is redundant, and can skip the check.
     */
    if (!_exprs.isEmpty() && !exprBdd.and(_orExprBDDs).isZero()) {
      // try to remove something
      List<AclLineMatchExpr> toRemove = new ArrayList<>();
      _exprs.forEach(
          (expr1, bdd1) -> {
            if (combinator(bdd1, exprBdd).equals(exprBdd)) {
              // bdd1 is now redundant; remove
              toRemove.add(expr1);
            }
          });
      if (!toRemove.isEmpty()) {
        toRemove.forEach(_exprs::remove);
        _orExprBDDs = _exprs.values().stream().reduce(_bdd.getFactory().zero(), BDD::or);
      }
    }

    _exprs.put(expr, exprBdd);
    _bdd = newBdd;
    _orExprBDDs = _orExprBDDs.or(exprBdd);
  }

  protected BDD getBdd() {
    return _bdd;
  }

  protected Set<AclLineMatchExpr> getExprs() {
    return _exprs.keySet();
  }
}
