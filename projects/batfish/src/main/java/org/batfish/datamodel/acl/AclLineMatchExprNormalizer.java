package org.batfish.datamodel.acl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import net.sf.javabdd.BDD;
import org.batfish.common.BatfishException;
import org.batfish.datamodel.acl.explanation.ConjunctsBuilder;
import org.batfish.datamodel.acl.normalize.Negate;
import org.batfish.symbolic.bdd.AclLineMatchExprToBDD;

/**
 * Normalizes {@link AclLineMatchExpr AclLineMatchExprs} to their version of DNF (disjunctive normal
 * form). In general, normal forms have at one {@link OrMatchExpr}, which is at the root. {@link
 * AndMatchExpr AndMatchExprs} are allowed only at the root or as children of that single {@link
 * OrMatchExpr}.
 *
 * <p>To improve efficiency, it uses {@link BDD BDDs} to detect contradictions within the {@link
 * AndMatchExpr AndMatchExprs}. We also normalize top-down rather than bottom-up (which is a bit
 * simpler to grok) in order to detect those contradictions earlier.
 */
public final class AclLineMatchExprNormalizer implements GenericAclLineMatchExprVisitor<Void> {
  private final AclLineMatchExprToBDD _aclLineMatchExprToBDD;
  private Set<ConjunctsBuilder> _conjunctsBuilders;

  public AclLineMatchExprNormalizer(AclLineMatchExprToBDD aclLineMatchExprToBDD) {
    _aclLineMatchExprToBDD = aclLineMatchExprToBDD;
    _conjunctsBuilders = new HashSet<>();
    _conjunctsBuilders.add(new ConjunctsBuilder(_aclLineMatchExprToBDD));
  }

  public AclLineMatchExprNormalizer(AclLineMatchExprNormalizer other) {
    _aclLineMatchExprToBDD = other._aclLineMatchExprToBDD;
    _conjunctsBuilders =
        other._conjunctsBuilders.stream().map(ConjunctsBuilder::new).collect(Collectors.toSet());
  }

  public AclLineMatchExpr normalize(AclLineMatchExpr expr) {
    expr.accept(this);
    Set<AclLineMatchExpr> disjuncts =
        _conjunctsBuilders
            .stream()
            .filter(conjunctsBuilder -> !conjunctsBuilder.unsat())
            .map(ConjunctsBuilder::build)
            .collect(Collectors.toSet());
    return AclLineMatchExprs.or(disjuncts);
  }

  private void addConstraint(AclLineMatchExpr expr) {
    List<ConjunctsBuilder> unsat = new ArrayList<>();
    for (ConjunctsBuilder conjunctsBuilder : _conjunctsBuilders) {
      conjunctsBuilder.add(expr);
      if (conjunctsBuilder.unsat()) {
        unsat.add(conjunctsBuilder);
      }
    }
    _conjunctsBuilders.removeAll(unsat);
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
