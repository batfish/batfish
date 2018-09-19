package org.batfish.datamodel.acl;

import static org.batfish.datamodel.acl.AclLineMatchExprs.TRUE;
import static org.batfish.datamodel.acl.AclLineMatchExprs.or;

import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Ordering;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import net.sf.javabdd.BDD;
import org.batfish.common.BatfishException;
import org.batfish.common.bdd.IpAccessListToBDD;
import org.batfish.datamodel.acl.explanation.ConjunctsBuilder;
import org.batfish.datamodel.acl.normalize.Negate;

/**
 * Normalizes {@link AclLineMatchExpr AclLineMatchExprs} to their version of DNF (disjunctive normal
 * form). In general, normal forms have at most one {@link OrMatchExpr}, which is at the root.
 * {@link AndMatchExpr AndMatchExprs} are allowed only at the root or as children of that single
 * {@link OrMatchExpr}.
 *
 * <p>To improve efficiency, it uses {@link BDD BDDs} to detect contradictions within the {@link
 * AndMatchExpr AndMatchExprs}. We also normalize top-down rather than bottom-up (which is a bit
 * simpler to grok) in order to detect those contradictions earlier.
 */
public final class AclLineMatchExprNormalizer implements GenericAclLineMatchExprVisitor<Void> {
  private final IpAccessListToBDD _ipAccessListToBDD;
  private Set<ConjunctsBuilder> _conjunctsBuilders;

  private AclLineMatchExprNormalizer(IpAccessListToBDD ipAccessListToBDD) {
    _ipAccessListToBDD = ipAccessListToBDD;
    _conjunctsBuilders = new HashSet<>();
    _conjunctsBuilders.add(new ConjunctsBuilder(_ipAccessListToBDD));
  }

  private AclLineMatchExprNormalizer(AclLineMatchExprNormalizer other) {
    _ipAccessListToBDD = other._ipAccessListToBDD;
    _conjunctsBuilders =
        other._conjunctsBuilders.stream().map(ConjunctsBuilder::new).collect(Collectors.toSet());
  }

  /**
   * This method is the public API of the class. It normalizes the input {@link AclLineMatchExpr}.
   */
  public static AclLineMatchExpr normalize(IpAccessListToBDD toBDD, AclLineMatchExpr expr) {
    AclLineMatchExprNormalizer normalizer = new AclLineMatchExprNormalizer(toBDD);
    expr.accept(normalizer);
    Set<AclLineMatchExpr> disjuncts =
        normalizer
            ._conjunctsBuilders
            .stream()
            .filter(conjunctsBuilder -> !conjunctsBuilder.unsat())
            .map(ConjunctsBuilder::build)
            .collect(ImmutableSortedSet.toImmutableSortedSet(Ordering.natural()));
    return disjuncts.contains(TRUE) ? TRUE : or(disjuncts);
  }

  private void addConstraint(AclLineMatchExpr expr) {
    /*
     * Add expr to each disjunct. Then remove any that are now unsat (FALSE), since A || FALSE == A.
     */
    _conjunctsBuilders.removeAll(
        _conjunctsBuilders
            .stream()
            .peek(cb -> cb.add(expr))
            .filter(ConjunctsBuilder::unsat)
            .collect(Collectors.toList()));
  }

  @Override
  public Void visitAndMatchExpr(AndMatchExpr andMatchExpr) {
    andMatchExpr.getConjuncts().forEach(this::visit);
    return null;
  }

  @Override
  public Void visitFalseExpr(FalseExpr falseExpr) {
    _conjunctsBuilders.clear();
    return null;
  }

  @Override
  public Void visitMatchHeaderSpace(MatchHeaderSpace matchHeaderSpace) {
    addConstraint(matchHeaderSpace);
    return null;
  }

  @Override
  public Void visitMatchSrcInterface(MatchSrcInterface matchSrcInterface) {
    addConstraint(matchSrcInterface);
    return null;
  }

  @Override
  public Void visitNotMatchExpr(NotMatchExpr notMatchExpr) {
    AclLineMatchExpr negatedOperand = Negate.negate(notMatchExpr.getOperand());
    if (negatedOperand instanceof NotMatchExpr) {
      // negated leaf node. rather than recurse, just add to the conjuctsBuilders.
      addConstraint(notMatchExpr);
    } else {
      negatedOperand.accept(this);
    }
    return null;
  }

  @Override
  public Void visitOriginatingFromDevice(OriginatingFromDevice originatingFromDevice) {
    addConstraint(originatingFromDevice);
    return null;
  }

  @Override
  public Void visitOrMatchExpr(OrMatchExpr orMatchExpr) {
    _conjunctsBuilders =
        orMatchExpr
            .getDisjuncts()
            .stream()
            .flatMap(
                disjunct -> {
                  AclLineMatchExprNormalizer normalizer = new AclLineMatchExprNormalizer(this);
                  disjunct.accept(normalizer);
                  return normalizer
                      ._conjunctsBuilders
                      .stream()
                      .filter(conjunctsBuilder -> !conjunctsBuilder.unsat());
                })
            .collect(Collectors.toSet());
    return null;
  }

  @Override
  public Void visitPermittedByAcl(PermittedByAcl permittedByAcl) {
    throw new BatfishException("PermittedByAcl expressions must be inlined");
  }

  @Override
  public Void visitTrueExpr(TrueExpr trueExpr) {
    return null;
  }
}
