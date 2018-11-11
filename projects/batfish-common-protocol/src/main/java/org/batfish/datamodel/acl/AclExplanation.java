package org.batfish.datamodel.acl;

import static com.google.common.base.Preconditions.checkState;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;
import java.util.Collections;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.IntersectHeaderSpaces;

/**
 * Generates reduced explanations of satisfiable sets of {@link AclLineMatchExpr} literals: positive
 * {@link MatchHeaderSpace}, negative {@link MatchHeaderSpace} (negated by {@link NotMatchExpr}),
 * {@link OriginatingFromDevice}, and {@link MatchSrcInterface}). Merges multiple positive {@link
 * MatchHeaderSpace} constraints using {@link IntersectHeaderSpaces}.
 *
 * <p>Generated explanations have the format: at most 1 positive {@link MatchHeaderSpace} constraint
 * (default meaning unconstrained), at most 1 location constraint ({@link OriginatingFromDevice} or
 * {@link MatchSrcInterface}), and some number of negative {@link MatchHeaderSpace} constraints.
 *
 * <p>Along with the explanation we produce a provenance, which is a map from each literal in the
 * explanation to the set of original literals on which it depends.
 */
public final class AclExplanation {

  /**
   * Visitor for building {@link AclExplanation} from normalized {@link AclLineMatchExpr
   * AclLineMatchExprs}.
   */
  private final class AclLineMatchExprVisitor implements GenericAclLineMatchExprVisitor<Void> {
    @Override
    public Void visitAndMatchExpr(AndMatchExpr andMatchExpr) {
      throw new IllegalArgumentException("Can only explain AclLineMatchExpr literals.");
    }

    @Override
    public Void visitFalseExpr(FalseExpr falseExpr) {
      throw new IllegalArgumentException("Can only explain AclLineMatchExpr literals.");
    }

    @Override
    public Void visitMatchHeaderSpace(MatchHeaderSpace matchHeaderSpace) {
      requireHeaderSpace(matchHeaderSpace);
      return null;
    }

    @Override
    public Void visitMatchSrcInterface(MatchSrcInterface matchSrcInterface) {
      requireSourceInterfaces(matchSrcInterface);
      return null;
    }

    @Override
    public Void visitNotMatchExpr(NotMatchExpr notMatchExpr) {
      AclLineMatchExpr operand = notMatchExpr.getOperand();
      if (operand instanceof MatchHeaderSpace) {
        forbidHeaderSpace((MatchHeaderSpace) operand);
        return null;
      }
      throw new IllegalArgumentException("Can only explain AclLineMatchExpr literals.");
    }

    @Override
    public Void visitOriginatingFromDevice(OriginatingFromDevice originatingFromDevice) {
      requireOriginatingFromDevice(originatingFromDevice);
      return null;
    }

    @Override
    public Void visitOrMatchExpr(OrMatchExpr orMatchExpr) {
      throw new IllegalArgumentException("Can only explain AclLineMatchExpr literals.");
    }

    @Override
    public Void visitPermittedByAcl(PermittedByAcl permittedByAcl) {
      throw new IllegalArgumentException("Can only explain AclLineMatchExpr literals.");
    }

    @Override
    public Void visitTrueExpr(TrueExpr trueExpr) {
      // noop
      return null;
    }
  }

  private enum Sources {
    INTERFACES,
    DEVICE,
    ANY
  }

  private @Nullable HeaderSpace _headerSpace = null;

  private @Nullable SortedSet<HeaderSpace> _notHeaderSpaces = new TreeSet<>();

  private @Nonnull Sources _sources = Sources.ANY;

  private @Nullable Set<String> _sourceInterfaces = null;

  // provenance for the above data -- the set of literals that each depends upon
  private @Nonnull Set<AclLineMatchExpr> _headerSpaceProvenance =
      Collections.newSetFromMap(new IdentityHashMap<>());
  private @Nonnull IdentityHashMap<HeaderSpace, AclLineMatchExpr> _notHeaderSpacesProvenance =
      new IdentityHashMap<>();
  private @Nonnull Set<AclLineMatchExpr> _sourcesProvenance =
      Collections.newSetFromMap(new IdentityHashMap<>());
  private @Nonnull Set<AclLineMatchExpr> _sourceInterfacesProvenance =
      Collections.newSetFromMap(new IdentityHashMap<>());

  private final @Nonnull AclLineMatchExprVisitor _visitor = new AclLineMatchExprVisitor();

  @VisibleForTesting
  void requireSourceInterfaces(MatchSrcInterface matchSrcInterface) {
    Set<String> sourceInterfaces = matchSrcInterface.getSrcInterfaces();
    checkState(_sources != Sources.DEVICE, "AclExplanation is unsatisfiable");
    _sources = Sources.INTERFACES;
    _sourceInterfaces =
        _sourceInterfaces == null
            ? sourceInterfaces
            : Sets.intersection(_sourceInterfaces, sourceInterfaces);
    checkState(!_sourceInterfaces.isEmpty(), "AclExplanation is unsatisfiable");
    _sourceInterfacesProvenance.add(matchSrcInterface);
  }

  @VisibleForTesting
  void requireOriginatingFromDevice(OriginatingFromDevice originatingFromDevice) {
    checkState(_sources != Sources.INTERFACES, "AclExplanation is unsatisfiable");
    _sources = Sources.DEVICE;
    _sourcesProvenance.add(originatingFromDevice);
  }

  @VisibleForTesting
  void requireHeaderSpace(MatchHeaderSpace matchHeaderSpace) {
    HeaderSpace headerSpace = matchHeaderSpace.getHeaderspace();
    if (_headerSpace == null) {
      _headerSpace = headerSpace;
    } else {
      Optional<HeaderSpace> intersection =
          IntersectHeaderSpaces.intersect(_headerSpace, headerSpace);
      checkState(intersection.isPresent(), "AclExplanation is unsatisfiable");
      _headerSpace = intersection.get();
    }
    _headerSpaceProvenance.add(matchHeaderSpace);
  }

  @VisibleForTesting
  void forbidHeaderSpace(MatchHeaderSpace matchHeaderSpace) {
    HeaderSpace headerSpace = matchHeaderSpace.getHeaderspace();
    _notHeaderSpaces.add(headerSpace);
    _notHeaderSpacesProvenance.put(headerSpace, matchHeaderSpace);
  }

  @VisibleForTesting
  AclLineMatchExprWithProvenance<AclLineMatchExpr> build() {
    ImmutableSortedSet.Builder<AclLineMatchExpr> conjunctsBuilder =
        new ImmutableSortedSet.Builder<>(Comparator.naturalOrder());
    IdentityHashMap<AclLineMatchExpr, Set<AclLineMatchExpr>> conjunctsProvenance =
        new IdentityHashMap<>();
    if (_headerSpace != null) {
      MatchHeaderSpace matchHeaderSpace = new MatchHeaderSpace(_headerSpace);
      conjunctsBuilder.add(matchHeaderSpace);
      conjunctsProvenance.put(matchHeaderSpace, _headerSpaceProvenance);
    }
    _notHeaderSpaces.forEach(
        notHeaderSpace -> {
          MatchHeaderSpace matchHeaderSpace = new MatchHeaderSpace(notHeaderSpace);
          NotMatchExpr notMatchExpr = new NotMatchExpr(matchHeaderSpace);
          conjunctsBuilder.add(notMatchExpr);
          conjunctsProvenance.put(
              matchHeaderSpace,
              Collections.singleton(_notHeaderSpacesProvenance.get(notHeaderSpace)));
        });
    switch (_sources) {
      case DEVICE:
        OriginatingFromDevice originatingFromDevice = OriginatingFromDevice.INSTANCE;
        conjunctsBuilder.add(originatingFromDevice);
        conjunctsProvenance.put(originatingFromDevice, _sourcesProvenance);
        break;
      case INTERFACES:
        MatchSrcInterface matchSrcInterface = new MatchSrcInterface(_sourceInterfaces);
        conjunctsBuilder.add(matchSrcInterface);
        conjunctsProvenance.put(matchSrcInterface, _sourceInterfacesProvenance);
        break;
      case ANY:
        break;
      default:
        throw new IllegalArgumentException("unexpected Sources value: " + _sources);
    }
    SortedSet<AclLineMatchExpr> conjuncts = conjunctsBuilder.build();
    if (conjuncts.isEmpty()) {
      return new AclLineMatchExprWithProvenance<>(TrueExpr.INSTANCE, new IdentityHashMap<>());
    }
    if (conjuncts.size() == 1) {
      return new AclLineMatchExprWithProvenance<>(conjuncts.first(), conjunctsProvenance);
    }
    return new AclLineMatchExprWithProvenance<>(new AndMatchExpr(conjuncts), conjunctsProvenance);
  }

  public static AclLineMatchExprWithProvenance<AclLineMatchExpr> explainLiterals(
      Iterable<AclLineMatchExpr> conjuncts) {
    AclExplanation explanation = new AclExplanation();
    conjuncts.forEach(explanation._visitor::visit);
    return explanation.build();
  }

  /** A factor is either a literal or a conjunction of literals. */
  static AclLineMatchExprWithProvenance<AclLineMatchExpr> explainFactor(AclLineMatchExpr factor) {
    if (factor instanceof AndMatchExpr) {
      return explainLiterals(((AndMatchExpr) factor).getConjuncts());
    }
    return explainLiterals(ImmutableList.of(factor));
  }

  public static AclLineMatchExprWithProvenance<AclLineMatchExpr> explainNormalForm(
      AclLineMatchExpr nf) {
    /*
     * A normal form is either a factor or a disjunction of factors.
     */
    if (nf instanceof OrMatchExpr) {
      /*
       * Each disjunct is a factor.
       */
      SortedSet<AclLineMatchExprWithProvenance<AclLineMatchExpr>> disjunctsWithProvenance =
          ((OrMatchExpr) nf)
              .getDisjuncts()
              .stream()
              .map(AclExplanation::explainFactor)
              .collect(ImmutableSortedSet.toImmutableSortedSet(Ordering.natural()));
      List<AclLineMatchExpr> disjuncts = new LinkedList<>();
      IdentityHashMap<AclLineMatchExpr, Set<AclLineMatchExpr>> provenance = new IdentityHashMap<>();
      for (AclLineMatchExprWithProvenance<AclLineMatchExpr> matchExprWithProvenance :
          disjunctsWithProvenance) {
        disjuncts.add(matchExprWithProvenance.getMatchExpr());
        provenance.putAll(matchExprWithProvenance.getProvenance());
      }
      return new AclLineMatchExprWithProvenance<>(new OrMatchExpr(disjuncts), provenance);
    }
    return explainFactor(nf);
  }
}
