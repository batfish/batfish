package org.batfish.datamodel.acl;

import static com.google.common.base.Preconditions.checkState;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Sets;
import java.util.Comparator;
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
      requireHeaderSpace(matchHeaderSpace.getHeaderspace());
      return null;
    }

    @Override
    public Void visitMatchSrcInterface(MatchSrcInterface matchSrcInterface) {
      requireSourceInterfaces(matchSrcInterface.getSrcInterfaces());
      return null;
    }

    @Override
    public Void visitNotMatchExpr(NotMatchExpr notMatchExpr) {
      if (notMatchExpr.getOperand() instanceof MatchHeaderSpace) {
        HeaderSpace headerSpace = ((MatchHeaderSpace) notMatchExpr.getOperand()).getHeaderspace();
        forbidHeaderSpace(headerSpace);
        return null;
      }
      throw new IllegalArgumentException("Can only explain AclLineMatchExpr literals.");
    }

    @Override
    public Void visitOriginatingFromDevice(OriginatingFromDevice originatingFromDevice) {
      requireOriginatingFromDevice();
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

  private final @Nonnull AclLineMatchExprVisitor _visitor = new AclLineMatchExprVisitor();

  @VisibleForTesting
  void requireSourceInterfaces(Set<String> sourceInterfaces) {
    checkState(_sources != Sources.DEVICE, "AclExplanation is unsatisfiable");
    _sources = Sources.INTERFACES;
    _sourceInterfaces =
        _sourceInterfaces == null
            ? sourceInterfaces
            : Sets.intersection(_sourceInterfaces, sourceInterfaces);
    checkState(!_sourceInterfaces.isEmpty(), "AclExplanation is unsatisfiable");
  }

  @VisibleForTesting
  void requireOriginatingFromDevice() {
    checkState(_sources != Sources.INTERFACES, "AclExplanation is unsatisfiable");
    _sources = Sources.DEVICE;
  }

  @VisibleForTesting
  void requireHeaderSpace(HeaderSpace headerSpace) {
    if (_headerSpace == null) {
      _headerSpace = headerSpace;
    } else {
      Optional<HeaderSpace> intersection =
          IntersectHeaderSpaces.intersect(_headerSpace, headerSpace);
      checkState(intersection.isPresent(), "AclExplanation is unsatisfiable");
      _headerSpace = intersection.get();
    }
  }

  @VisibleForTesting
  void forbidHeaderSpace(HeaderSpace headerSpace) {
    _notHeaderSpaces.add(headerSpace);
  }

  @VisibleForTesting
  AclLineMatchExpr build() {
    ImmutableSortedSet.Builder<AclLineMatchExpr> conjunctsBuilder =
        new ImmutableSortedSet.Builder<>(Comparator.naturalOrder());
    if (_headerSpace != null) {
      conjunctsBuilder.add(new MatchHeaderSpace(_headerSpace));
    }
    _notHeaderSpaces.forEach(
        notHeaderSpace ->
            conjunctsBuilder.add(new NotMatchExpr(new MatchHeaderSpace(notHeaderSpace))));
    switch (_sources) {
      case DEVICE:
        conjunctsBuilder.add(OriginatingFromDevice.INSTANCE);
        break;
      case INTERFACES:
        conjunctsBuilder.add(new MatchSrcInterface(_sourceInterfaces));
        break;
      case ANY:
        break;
      default:
        throw new IllegalArgumentException("unexpected Sources value: " + _sources);
    }
    SortedSet<AclLineMatchExpr> conjuncts = conjunctsBuilder.build();
    if (conjuncts.isEmpty()) {
      return TrueExpr.INSTANCE;
    }
    if (conjuncts.size() == 1) {
      return conjuncts.first();
    }
    return new AndMatchExpr(conjuncts);
  }

  public static AclLineMatchExpr explainLiterals(Iterable<AclLineMatchExpr> conjuncts) {
    AclExplanation explanation = new AclExplanation();
    conjuncts.forEach(explanation._visitor::visit);
    return explanation.build();
  }
}
