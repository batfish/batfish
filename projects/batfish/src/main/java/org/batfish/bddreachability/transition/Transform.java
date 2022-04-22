package org.batfish.bddreachability.transition;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;
import net.sf.javabdd.BDDPairing;
import org.batfish.common.bdd.BDDPairingFactory;

/** Apply a transformation encoded as a relation over unprimed and primed variables. */
@ParametersAreNonnullByDefault
public class Transform implements Transition {
  private final BDD _forwardRelation;
  private final BDDPairingFactory _pairingFactory;
  private final BDD _vars;

  // lazy init
  private @Nullable BDD _reverseRelation;
  private @Nullable BDDPairing _swapPairing;

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
    if (_vars.testsVars(other._vars)) {
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
    if (!_vars.equals(other._vars)) {
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
    Map<BDD, List<Transform>> transformsByVars =
        transforms.stream().collect(Collectors.groupingBy(t -> t._vars, Collectors.toList()));
    if (transformsByVars.size() == transforms.size()) {
      // no two Transforms had the same vars
      return transforms;
    }
    return transformsByVars.values().stream()
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
    return _forwardRelation.equals(transform._forwardRelation)
        && _pairingFactory.equals(transform._pairingFactory)
        && _vars.equals(transform._vars)
        && Objects.equal(_reverseRelation, transform._reverseRelation)
        && Objects.equal(_swapPairing, transform._swapPairing);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(
        _forwardRelation, _pairingFactory, _vars, _reverseRelation, _swapPairing);
  }
}
