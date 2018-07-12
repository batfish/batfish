package org.batfish.z3;

import static org.batfish.datamodel.AclIpSpace.difference;
import static org.batfish.datamodel.AclIpSpace.union;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.SortedSet;
import java.util.stream.Collectors;
import org.batfish.datamodel.EmptyIpSpace;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpAccessListLine;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.UniverseIpSpace;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.AndMatchExpr;
import org.batfish.datamodel.acl.FalseExpr;
import org.batfish.datamodel.acl.GenericAclLineMatchExprVisitor;
import org.batfish.datamodel.acl.MatchHeaderSpace;
import org.batfish.datamodel.acl.MatchSrcInterface;
import org.batfish.datamodel.acl.NotMatchExpr;
import org.batfish.datamodel.acl.OrMatchExpr;
import org.batfish.datamodel.acl.OriginatingFromDevice;
import org.batfish.datamodel.acl.PermittedByAcl;
import org.batfish.datamodel.acl.TrueExpr;

/**
 * Specialize an {@link IpAccessList} to a given {@link HeaderSpace}. Lines that can never match the
 * {@link HeaderSpace} can be removed.
 */
public class IpAccessListSpecializer implements GenericAclLineMatchExprVisitor<AclLineMatchExpr> {
  private final boolean _canSpecialize;
  private final IpSpaceSpecializer _dstIpSpaceSpecializer;
  private final IpSpaceSpecializer _srcIpSpaceSpecializer;
  private final IpSpaceSpecializer _srcOrDstIpSpaceSpecializer;

  IpAccessListSpecializer(HeaderSpace headerSpace, Map<String, IpSpace> namedIpSpaces) {
    IpSpace dstIps = headerSpace.getDstIps();
    IpSpace srcIps = headerSpace.getSrcIps();
    IpSpace srcOrDstIps = headerSpace.getSrcOrDstIps();

    IpSpace notDstIps = headerSpace.getNotDstIps();
    IpSpace notSrcIps = headerSpace.getNotSrcIps();

    _dstIpSpaceSpecializer =
        (dstIps == null && srcOrDstIps == null && notDstIps == null)
            ? null
            : new IpSpaceIpSpaceSpecializer(
                difference(union(dstIps, srcOrDstIps), notDstIps), namedIpSpaces);
    _srcIpSpaceSpecializer =
        (srcIps == null && srcOrDstIps == null && notSrcIps == null)
            ? null
            : new IpSpaceIpSpaceSpecializer(
                difference(union(srcIps, srcOrDstIps), notSrcIps), namedIpSpaces);
    _srcOrDstIpSpaceSpecializer =
        (srcIps == null
                && dstIps == null
                && srcOrDstIps == null
                && notSrcIps == null
                && notDstIps == null)
            ? null
            : new IpSpaceIpSpaceSpecializer(
                difference(union(srcIps, dstIps, srcOrDstIps), union(notSrcIps, notDstIps)),
                namedIpSpaces);

    /*
     * Currently, specialization is based on srcIp and dstIp only. We can specialize only
     * if we have at least one IpSpace specializer.
     */
    _canSpecialize = _dstIpSpaceSpecializer != null || _srcIpSpaceSpecializer != null;
  }

  public IpAccessList specialize(IpAccessList ipAccessList) {
    if (!_canSpecialize) {
      return ipAccessList;
    } else {
      List<IpAccessListLine> specializedLines =
          ipAccessList
              .getLines()
              .stream()
              .map(this::specialize)
              .filter(Optional::isPresent)
              .map(Optional::get)
              .collect(Collectors.toList());

      return IpAccessList.builder()
          .setName(ipAccessList.getName())
          .setLines(specializedLines)
          .build();
    }
  }

  public Optional<IpAccessListLine> specialize(IpAccessListLine ipAccessListLine) {
    AclLineMatchExpr aclLineMatchExpr = ipAccessListLine.getMatchCondition().accept(this);

    if (aclLineMatchExpr == FalseExpr.INSTANCE) {
      return Optional.empty();
    }

    return Optional.of(ipAccessListLine.toBuilder().setMatchCondition(aclLineMatchExpr).build());
  }

  @Override
  public AclLineMatchExpr visitAndMatchExpr(AndMatchExpr andMatchExpr) {
    List<AclLineMatchExpr> conjuncts =
        andMatchExpr
            .getConjuncts()
            .stream()
            .map(expr -> expr.accept(this))
            .filter(expr -> expr != TrueExpr.INSTANCE)
            .collect(ImmutableList.toImmutableList());
    if (conjuncts.isEmpty()) {
      return TrueExpr.INSTANCE;
    }
    if (conjuncts.stream().anyMatch(expr -> expr == FalseExpr.INSTANCE)) {
      return FalseExpr.INSTANCE;
    }
    return new AndMatchExpr(conjuncts);
  }

  @Override
  public AclLineMatchExpr visitFalseExpr(FalseExpr falseExpr) {
    return falseExpr;
  }

  private IpSpace specializeWith(IpSpace dstIpSpace, IpSpaceSpecializer specializer) {
    return dstIpSpace != null && specializer != null
        ? specializer.specialize(dstIpSpace)
        : dstIpSpace;
  }

  @Override
  public AclLineMatchExpr visitMatchHeaderSpace(MatchHeaderSpace matchHeaderSpace) {
    HeaderSpace headerSpace = matchHeaderSpace.getHeaderspace();
    IpSpace dstIps = headerSpace.getDstIps();
    IpSpace notDstIps = headerSpace.getNotDstIps();
    IpSpace notSrcIps = headerSpace.getNotSrcIps();
    IpSpace srcIps = headerSpace.getSrcIps();
    IpSpace srcOrDstIps = headerSpace.getSrcOrDstIps();

    dstIps = specializeWith(dstIps, _dstIpSpaceSpecializer);
    notDstIps = specializeWith(notDstIps, _dstIpSpaceSpecializer);
    notSrcIps = specializeWith(notSrcIps, _srcIpSpaceSpecializer);
    srcIps = specializeWith(srcIps, _srcIpSpaceSpecializer);
    srcOrDstIps = specializeWith(srcOrDstIps, _srcOrDstIpSpaceSpecializer);

    if (constraintUnionEmpty(dstIps, srcOrDstIps)) {
      return FalseExpr.INSTANCE;
    }

    if (constraintUnionEmpty(srcIps, srcOrDstIps)) {
      return FalseExpr.INSTANCE;
    }

    if (notDstIps == UniverseIpSpace.INSTANCE || notSrcIps == UniverseIpSpace.INSTANCE) {
      return FalseExpr.INSTANCE;
    }

    HeaderSpace specializedHeaderSpace =
        headerSpace
            .toBuilder()
            .setDstIps(simplifyPositiveIpConstraint(dstIps))
            .setNotDstIps(simplifyNegativeIpConstraint(notDstIps))
            .setNotSrcIps(simplifyNegativeIpConstraint(notSrcIps))
            .setSrcIps(simplifyPositiveIpConstraint(srcIps))
            .setSrcOrDstIps(simplifyPositiveIpConstraint(srcOrDstIps))
            .build();

    if (specializedHeaderSpace.equals(HeaderSpace.builder().build())) {
      return TrueExpr.INSTANCE;
    }

    return new MatchHeaderSpace(specializedHeaderSpace);
  }

  private static IpSpace simplifyNegativeIpConstraint(IpSpace ipSpace) {
    if (ipSpace == EmptyIpSpace.INSTANCE) {
      return null;
    }
    return ipSpace;
  }

  private static IpSpace simplifyPositiveIpConstraint(IpSpace ipSpace) {
    if (ipSpace == UniverseIpSpace.INSTANCE) {
      return null;
    }
    return ipSpace;
  }

  private static boolean constraintUnionEmpty(IpSpace ipSpace1, IpSpace ipSpace2) {
    return (ipSpace1 == EmptyIpSpace.INSTANCE && ipSpace2 == null)
        || (ipSpace1 == null && ipSpace2 == EmptyIpSpace.INSTANCE)
        || (ipSpace1 == EmptyIpSpace.INSTANCE && ipSpace2 == EmptyIpSpace.INSTANCE);
  }

  @Override
  public AclLineMatchExpr visitMatchSrcInterface(MatchSrcInterface matchSrcInterface) {
    return matchSrcInterface;
  }

  @Override
  public AclLineMatchExpr visitNotMatchExpr(NotMatchExpr notMatchExpr) {
    AclLineMatchExpr subExpr = notMatchExpr.getOperand().accept(this);

    if (subExpr == TrueExpr.INSTANCE) {
      return FalseExpr.INSTANCE;
    } else if (subExpr == FalseExpr.INSTANCE) {
      return TrueExpr.INSTANCE;
    } else {
      return new NotMatchExpr(subExpr);
    }
  }

  @Override
  public AclLineMatchExpr visitOriginatingFromDevice(OriginatingFromDevice originatingFromDevice) {
    return originatingFromDevice;
  }

  @Override
  public AclLineMatchExpr visitOrMatchExpr(OrMatchExpr orMatchExpr) {
    SortedSet<AclLineMatchExpr> disjuncts =
        orMatchExpr
            .getDisjuncts()
            .stream()
            .map(expr -> expr.accept(this))
            .filter(expr -> expr != FalseExpr.INSTANCE)
            .collect(
                ImmutableSortedSet.toImmutableSortedSet(
                    Objects.requireNonNull(orMatchExpr.getDisjuncts().comparator())));

    if (disjuncts.isEmpty()) {
      return FalseExpr.INSTANCE;
    } else {
      return new OrMatchExpr(disjuncts);
    }
  }

  @Override
  public AclLineMatchExpr visitPermittedByAcl(PermittedByAcl permittedByAcl) {
    return permittedByAcl;
  }

  @Override
  public AclLineMatchExpr visitTrueExpr(TrueExpr trueExpr) {
    return trueExpr;
  }
}
