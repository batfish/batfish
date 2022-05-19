package org.batfish.bddreachability.transition;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Table;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;
import org.batfish.common.bdd.BDDPairingFactory;

/** Apply a transformation encoded as a relation over unprimed and primed variables. */
@ParametersAreNonnullByDefault
public class Transform implements Transition {
  private final BDD _forwardRelation;
  private final BDDPairingFactory _pairingFactory;

  // lazy init
  private @Nullable BDD _reverseRelation;

  public Transform(BDD relation, BDDPairingFactory pairingFactory) {
    _forwardRelation = relation;
    _pairingFactory = pairingFactory;
    assert initIfAssertionsEnabled();
  }

  /**
   * Initialize eagerly when assertions are enabled, so that the transit methods do not allocate any
   * BDDs. See {@link org.batfish.bddreachability.BDDReachabilityUtils#fixpoint(Map, Table,
   * BiFunction)}
   */
  private boolean initIfAssertionsEnabled() {
    init();
    return true;
  }

  private void init() {
    _pairingFactory.getDomainVarsBdd(); // pairing factory computes this lazily
    _pairingFactory.getPrimeToUnprimePairing();
    _reverseRelation = _forwardRelation.replace(_pairingFactory.getSwapPairing());
  }

  BDD getForwardRelation() {
    return _forwardRelation;
  }

  BDDPairingFactory getPairingFactory() {
    return _pairingFactory;
  }

  @Override
  public BDD transitForward(BDD bdd) {
    return bdd.transform(_forwardRelation, _pairingFactory.getPrimeToUnprimePairing());
  }

  @Override
  public BDD transitBackward(BDD bdd) {
    if (_reverseRelation == null) {
      init();
    }
    return bdd.transform(_reverseRelation, _pairingFactory.getPrimeToUnprimePairing());
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
    if (_pairingFactory.overlapsWith(other._pairingFactory)) {
      // vars are not disjoint so we can't compose
      return Optional.empty();
    }

    /* vars are disjoint so we can combine them other's relation may constrain (but not transform) variables transformed
     * by this. those constraints should be applied after this's transformation. We do that by moving any constraints on
     * this's domain vars to this's codomain vars.
     */
    BDD otherForwardRelation = other._forwardRelation.replace(_pairingFactory.getSwapPairing());
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
  public Transform or(Transform other) {
    if (!_pairingFactory.equals(other._pairingFactory)) {
      BDDPairingFactory unionPairingFactory = _pairingFactory.union(other._pairingFactory);
      return this.expandTo(unionPairingFactory).or(other.expandTo(unionPairingFactory));
    }
    return new Transform(_forwardRelation.or(other._forwardRelation), _pairingFactory);
  }

  private static Transform orAllEqualPairingFactory(List<Transform> transforms) {
    assert transforms.stream().map(Transform::getPairingFactory).distinct().count() == 1
        : "All transforms must have the same pairing factory";
    Transform transform = transforms.get(0);
    if (transforms.size() == 1) {
      return transform;
    }
    BDDFactory bddFactory = transform._forwardRelation.getFactory();
    BDD forwardRel =
        bddFactory.orAll(
            transforms.stream()
                .map(t -> t._forwardRelation)
                .collect(ImmutableList.toImmutableList()));
    return new Transform(forwardRel, transform._pairingFactory);
  }

  /** Reduce the input list of transforms by combining those with equal domain variables. */
  public static List<Transform> reduceWithOr(List<Transform> transforms) {
    checkArgument(!transforms.isEmpty(), "orTransforms: transforms list must be non-empty");
    if (transforms.size() == 1) {
      return transforms;
    }
    Collection<Transform> reducedTransforms =
        transforms.stream()
            .collect(
                Collectors.groupingBy(
                    t -> t._pairingFactory,
                    Collectors.collectingAndThen(
                        Collectors.toList(), Transform::orAllEqualPairingFactory)))
            .values();
    if (reducedTransforms.size() == 1) {
      return ImmutableList.of(Iterables.getOnlyElement(reducedTransforms));
    }
    BDDPairingFactory unionPairingFactory =
        BDDPairingFactory.union(
            reducedTransforms.stream()
                .map(Transform::getPairingFactory)
                .collect(Collectors.toList()));
    return ImmutableList.of(
        orAllEqualPairingFactory(
            reducedTransforms.stream()
                .map(transform -> transform.expandTo(unionPairingFactory))
                .collect(Collectors.toList())));
  }

  public Transform expandTo(BDDPairingFactory factory) {
    assert factory.includes(_pairingFactory)
        : "Cannot expand to a factory that does not include all transformed vars";
    if (factory.equals(_pairingFactory)) {
      return this;
    }

    // compute the identity relation on variables that only exist in the expanded factory
    BDD idRel = factory.identityRelation(var -> !_pairingFactory.domainIncludes(var));
    // TODO: ensure we can use andWith (i.e. free bdds from this)
    return new Transform(idRel.andEq(_forwardRelation), factory);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Transform)) {
      return false;
    }
    Transform transform = (Transform) o;
    // Exclude _reverseRelations, _swapPairing and _vars, which are lazy initialized and/or
    // determined by _forwardRelation and _pairingFactory.
    return _forwardRelation.equals(transform._forwardRelation)
        && _pairingFactory.equals(transform._pairingFactory);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_forwardRelation, _pairingFactory);
  }
}
