package org.batfish.bddreachability.transition;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Table;
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
    return bdd.applyEx(_forwardRelation, BDDFactory.and, _pairingFactory.getDomainVarsBdd())
        .replaceWith(_pairingFactory.getSwapPairing());
  }

  @Override
  public BDD transitBackward(BDD bdd) {
    if (_reverseRelation == null) {
      init();
    }
    return bdd.applyEx(_reverseRelation, BDDFactory.and, _pairingFactory.getDomainVarsBdd())
        .replaceWith(_pairingFactory.getSwapPairing());
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
  public Optional<Transform> tryOr(Transform other) {
    if (!_pairingFactory.equals(other._pairingFactory)) {
      return Optional.empty();
    }
    return Optional.of(new Transform(_forwardRelation.or(other._forwardRelation), _pairingFactory));
  }

  /** Reduce the input list of transforms by combining those with equal domain variables. */
  public static List<Transform> reduceWithOr(List<Transform> transforms) {
    checkArgument(!transforms.isEmpty(), "orTransforms: transforms list must be non-empty");
    if (transforms.size() == 1) {
      return transforms;
    }
    Map<BDDPairingFactory, List<Transform>> transformsByPairingFactory =
        transforms.stream()
            .collect(Collectors.groupingBy(t -> t._pairingFactory, Collectors.toList()));
    if (transformsByPairingFactory.size() == transforms.size()) {
      // no two Transforms had the same BDDPairingFactory
      return transforms;
    }
    return transformsByPairingFactory.values().stream()
        .map(
            transforms1 -> {
              Transform transform = transforms1.get(0);
              if (transforms1.size() == 1) {
                return transform;
              }
              BDDFactory bddFactory = transform._forwardRelation.getFactory();
              BDD forwardRel =
                  bddFactory.orAll(
                      transforms1.stream()
                          .map(t -> t._forwardRelation)
                          .collect(ImmutableList.toImmutableList()));
              return new Transform(forwardRel, transform._pairingFactory);
            })
        .collect(Collectors.toList());
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
    // determined by
    // _forwardRelation and _pairingFactory.
    return _forwardRelation.equals(transform._forwardRelation)
        && _pairingFactory.equals(transform._pairingFactory);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_forwardRelation, _pairingFactory);
  }
}
