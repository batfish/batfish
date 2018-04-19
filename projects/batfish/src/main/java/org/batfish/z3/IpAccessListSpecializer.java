package org.batfish.z3;

import static org.batfish.datamodel.AclIpSpace.*;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedSet;
import java.util.List;
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
import org.batfish.datamodel.acl.PermittedByAcl;
import org.batfish.datamodel.acl.TrueExpr;
import sun.jvm.hotspot.memory.HeapBlock.Header;

/**
 * Specialize an {@link IpAccessList} to a given {@link HeaderSpace}. Lines that can never match the
 * {@link HeaderSpace} can be removed.
 */
public class IpAccessListSpecializer implements GenericAclLineMatchExprVisitor<AclLineMatchExpr> {
  private final boolean _canSpecialize;
  private final HeaderSpace _headerSpace;
  private final IpSpaceSpecializer _dstIpSpaceSpecializer;
  private final IpSpaceSpecializer _srcIpSpaceSpecializer;
  private final IpSpaceSpecializer _srcOrDstIpSpaceSpecializer;

  public IpAccessListSpecializer(HeaderSpace headerSpace) {
    _headerSpace = headerSpace;

    IpSpace dstIps = _headerSpace.getDstIps();
    IpSpace srcIps = _headerSpace.getSrcIps();
    IpSpace srcOrDstIps = _headerSpace.getSrcOrDstIps();

    IpSpace notDstIps = _headerSpace.getNotDstIps();
    IpSpace notSrcIps = _headerSpace.getNotSrcIps();

    /*
     * Currently, specialization is based on srcIp and dstIp only. We can specialize only
     * if we have a meaningful constraint on srcIp or on dstIp.
     */
    _canSpecialize =
        dstIps != null
            || srcIps != null
            || srcOrDstIps != null
            || notDstIps != null
            || notSrcIps != null;

    _dstIpSpaceSpecializer =
        new IpSpaceSpecializer(difference(union(dstIps, srcOrDstIps), notDstIps));
    _srcIpSpaceSpecializer =
        new IpSpaceSpecializer(difference(union(srcIps, srcOrDstIps), notSrcIps));
    _srcOrDstIpSpaceSpecializer =
        new IpSpaceSpecializer(
            difference(union(srcIps, dstIps, srcOrDstIps), union(notSrcIps, notDstIps)));
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

  /*

  if (aclLineMatchExpr instanceof TrueExpr) {
    return UniverseIpSpace.INSTANCE;
  } else if (aclLineMatchExpr instanceof FalseExpr) {
    return EmptyIpSpace.INSTANCE;
  }

  HeaderSpace oldHeaderSpace = HeaderSpaceConverter.convert(ipAccessListLine.getMatchCondition());
  IpWildcardSetIpSpace.Builder srcIpSpaceBuilder =
      IpWildcardSetIpSpace.builder().excluding(oldHeaderSpace.getNotSrcIps());
  if (oldHeaderSpace.getSrcIps().isEmpty() && oldHeaderSpace.getSrcOrDstIps().isEmpty()) {
    srcIpSpaceBuilder.including(IpWildcard.ANY);
  } else {
    srcIpSpaceBuilder.including(oldHeaderSpace.getSrcIps());
    srcIpSpaceBuilder.including(oldHeaderSpace.getSrcOrDstIps());
  }
  IpSpace specializedSrcIpSpace = _srcIpSpaceSpecializer.specialize(srcIpSpaceBuilder.build());

  IpWildcardSetIpSpace.Builder dstIpSpaceBuilder =
      IpWildcardSetIpSpace.builder().excluding(oldHeaderSpace.getNotDstIps());
  if (oldHeaderSpace.getDstIps().isEmpty() && oldHeaderSpace.getSrcOrDstIps().isEmpty()) {
    dstIpSpaceBuilder.including(IpWildcard.ANY);
  } else {
    dstIpSpaceBuilder.including(oldHeaderSpace.getDstIps());
    dstIpSpaceBuilder.including(oldHeaderSpace.getSrcOrDstIps());
  }
  IpSpace specializedDstIpSpace = _dstIpSpaceSpecializer.specialize(dstIpSpaceBuilder.build());

  if (specializedDstIpSpace instanceof EmptyIpSpace
      || specializedSrcIpSpace instanceof EmptyIpSpace) {
    return Optional.empty();
  }

  Set<IpWildcard> specializedDstIps;
  Set<IpWildcard> specializedNotDstIps;
  if (specializedDstIpSpace instanceof UniverseIpSpace) {
    // for a HeaderSpace, empty dstIps means Universe
    specializedDstIps = ImmutableSet.of();
    specializedNotDstIps = ImmutableSet.of();
  } else if (specializedDstIpSpace instanceof IpWildcardSetIpSpace) {
    IpWildcardSetIpSpace dstIpWildcardSetIpSpace = (IpWildcardSetIpSpace) specializedDstIpSpace;
    specializedDstIps = dstIpWildcardSetIpSpace.getWhitelist();
    specializedNotDstIps = dstIpWildcardSetIpSpace.getBlacklist();
  } else if (specializedDstIpSpace instanceof IpWildcard) {
    specializedDstIps = ImmutableSet.of((IpWildcard) specializedDstIpSpace);
    specializedNotDstIps = ImmutableSet.of();
  } else {
    throw new BatfishException("unexpected specializedDstIpSpace type");
  }

  Set<IpWildcard> specializedSrcIps;
  Set<IpWildcard> specializedNotSrcIps;
  if (specializedSrcIpSpace instanceof UniverseIpSpace) {
    specializedSrcIps = ImmutableSet.of();
    specializedNotSrcIps = ImmutableSet.of();
  } else if (specializedSrcIpSpace instanceof IpWildcardSetIpSpace) {
    IpWildcardSetIpSpace srcIpWildcardSetIpSpace = (IpWildcardSetIpSpace) specializedSrcIpSpace;
    specializedSrcIps = srcIpWildcardSetIpSpace.getWhitelist();
    specializedNotSrcIps = srcIpWildcardSetIpSpace.getBlacklist();
  } else if (specializedSrcIpSpace instanceof IpWildcard) {
    specializedSrcIps = ImmutableSet.of((IpWildcard) specializedSrcIpSpace);
    specializedNotSrcIps = ImmutableSet.of();
  } else {
    throw new BatfishException("unexpected specializedSrcIpSpace type");
  }

  HeaderSpace newHeaderSpace =
      oldHeaderSpace
          .toBuilder()
          .setDstIps(specializedDstIps)
          .setNotDstIps(specializedNotDstIps)
          .setSrcIps(specializedSrcIps)
          .setNotSrcIps(specializedNotSrcIps)
          .build();
  AclLineMatchExpr matchCondition =
      newHeaderSpace.unrestricted() ? TrueExpr.INSTANCE : new MatchHeaderSpace(newHeaderSpace);

  return Optional.of(
      IpAccessListLine.builder()
          .setAction(ipAccessListLine.getAction())
          .setMatchCondition(matchCondition)
          .setName(ipAccessListLine.getName())
          .build()
  */

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

  @Override
  public AclLineMatchExpr visitMatchHeaderSpace(MatchHeaderSpace matchHeaderSpace) {
    HeaderSpace headerSpace = matchHeaderSpace.getHeaderspace();
    IpSpace dstIps = headerSpace.getDstIps();
    IpSpace notDstIps = headerSpace.getNotDstIps();
    IpSpace notSrcIps = headerSpace.getNotSrcIps();
    IpSpace srcIps = headerSpace.getSrcIps();
    IpSpace srcOrDstIps = headerSpace.getSrcOrDstIps();

    if (dstIps != null) {
      dstIps = _dstIpSpaceSpecializer.specialize(dstIps);
    }
    if (notDstIps != null) {
      notDstIps = _dstIpSpaceSpecializer.specialize(notDstIps);
    }
    if (notSrcIps != null) {
      notSrcIps = _srcIpSpaceSpecializer.specialize(notSrcIps);
    }
    if (srcIps != null) {
      srcIps = _srcIpSpaceSpecializer.specialize(srcIps);
    }
    if (srcOrDstIps != null) {
      srcOrDstIps = _srcOrDstIpSpaceSpecializer.specialize(srcOrDstIps);
    }

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
            .rebuild()
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
