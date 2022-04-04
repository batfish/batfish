package org.batfish.bddreachability.transition;

import java.util.Optional;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;
import net.sf.javabdd.BDDPairing;
import org.batfish.common.bdd.BDDPairingFactory;

/** Apply a transformation encoded as a relation over unprimed and primed variables. */
public class Transform implements Transition {
  private final BDD _forwardRelation;
  private final BDDPairingFactory _pairingFactory;
  private final BDD _vars;

  // lazy init
  private BDD _reverseRelation;
  private BDDPairing _swapPairing;

  public Transform(BDD relation, BDDPairingFactory pairingFactory) {
    _forwardRelation = relation;
    _pairingFactory = pairingFactory;
    _vars = _pairingFactory.getDomainVarsBdd();
  }

  private void init() {
    _swapPairing = _pairingFactory.makeSwapPairing();
    _reverseRelation = _forwardRelation.replace(_swapPairing);
  }

  @Override
  public BDD transitForward(BDD bdd) {
    if (_swapPairing == null) {
      init();
    }
    return bdd.applyEx(_forwardRelation, BDDFactory.and, _vars).replaceWith(_swapPairing);
  }

  @Override
  public BDD transitBackward(BDD bdd) {
    if (_swapPairing == null) {
      init();
    }
    return bdd.applyEx(_reverseRelation, BDDFactory.and, _vars).replaceWith(_swapPairing);
  }

  @Override
  public <T> T accept(TransitionVisitor<T> visitor) {
    return visitor.visitTransform(this);
  }

  /**
   * Compose this {@link Transform} with another, if possible. The semantics is that they are
   * applied in series -- first this, then other.
   */
  public Optional<Transform> tryCompose(Transform other) {
    BDD vars = _pairingFactory.getDomainVarsBdd();
    BDD otherVars = other._pairingFactory.getDomainVarsBdd();
    if (!vars.diffSat(otherVars)) {
      // vars are not disjoint so we can't compose
      return Optional.empty();
    }
    /* vars are disjoint so we can combine them other's relation may constrain (but not transform) variables transformed
     * by this. those constraints should be applied after this's transformation. We do that by moving any constraints on
     * this's domain vars to this's codomain vars.
     */
    BDD otherForwardRelation = other._forwardRelation.replace(_swapPairing);
    return Optional.of(
        new Transform(
            _forwardRelation.and(otherForwardRelation),
            _pairingFactory.composeWith(other._pairingFactory)));
  }

  /**
   * Apply this {@link Transform} in parallel with another, if possible. The semantics is that a
   * nondeterministic choice is made which to apply. If an input flow is in the domain of both
   * transforms, the effect of merging is to non-deterministically choose between them. Requires the
   * two transforms to have equal domains.
   */
  public Optional<Transform> tryOr(Transform other) {
    BDD vars = _pairingFactory.getDomainVarsBdd();
    BDD otherVars = other._pairingFactory.getDomainVarsBdd();
    if (!vars.equals(otherVars)) {
      return Optional.empty();
    }
    return Optional.of(new Transform(_forwardRelation.or(other._forwardRelation), _pairingFactory));
  }
}
