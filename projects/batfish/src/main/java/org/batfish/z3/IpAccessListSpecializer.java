package org.batfish.z3;

import static org.batfish.datamodel.acl.AclLineMatchExprs.FALSE;
import static org.batfish.datamodel.acl.AclLineMatchExprs.TRUE;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedSet;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.SortedSet;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
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
import org.batfish.datamodel.acl.NotMatchExpr;
import org.batfish.datamodel.acl.OrMatchExpr;
import org.batfish.datamodel.acl.PermittedByAcl;
import org.batfish.datamodel.acl.TrueExpr;

/**
 * Specialize an {@link IpAccessList} to a given {@link HeaderSpace}. Lines that can never match the
 * {@link HeaderSpace} can be removed.
 */
public abstract class IpAccessListSpecializer
    implements GenericAclLineMatchExprVisitor<AclLineMatchExpr> {

  private static final IpAccessListLine FALSE_LINE =
      IpAccessListLine.accepting().setMatchCondition(FALSE).build();

  private static <T> boolean emptyDisjuction(Collection<T> orig, Collection<T> specialized) {
    return orig != null && !orig.isEmpty() && specialized.isEmpty();
  }

  private static IpSpace simplifyNegativeIpConstraint(IpSpace ipSpace) {
    return ipSpace == EmptyIpSpace.INSTANCE ? null : ipSpace;
  }

  private static IpSpace simplifyPositiveIpConstraint(IpSpace ipSpace) {
    return ipSpace == UniverseIpSpace.INSTANCE ? null : ipSpace;
  }

  abstract boolean canSpecialize();

  abstract HeaderSpace specialize(HeaderSpace headerSpace);

  /**
   * Returns a specialized version of the given {@link IpAccessList}.
   *
   * @param ipAccessList ACL to specialize
   * @return Specialized version of the given ACL
   */
  public final IpAccessList specialize(IpAccessList ipAccessList) {
    return specializeUpToLine(ipAccessList, ipAccessList.getLines().size());
  }

  /**
   * Returns a version of the given {@link IpAccessList} with lines up to {@code lineNum}
   * specialized, line {@code lineNum} unchanged, and all later lines removed.
   *
   * @param ipAccessList ACL to specialize
   * @param lineNum Final line to include in specialized version; this line will not be specialized
   * @return Version of the given ACL specialized up to line {@code lineNum}
   */
  public final IpAccessList specializeUpToLine(IpAccessList ipAccessList, int lineNum) {
    if (!canSpecialize()) {
      return ipAccessList;
    }

    List<IpAccessListLine> originalLines = ipAccessList.getLines();
    int lastSpecializedLine = Math.min(lineNum, originalLines.size());

    List<IpAccessListLine> specializedLines =
        IntStream.range(0, lastSpecializedLine)
            .mapToObj(i -> specialize(originalLines.get(i)).orElse(FALSE_LINE))
            .collect(Collectors.toList());
    if (lineNum < originalLines.size()) {
      specializedLines.add(originalLines.get(lineNum));
    }

    return IpAccessList.builder()
        .setName(ipAccessList.getName())
        .setLines(specializedLines)
        .build();
  }

  public final Optional<IpAccessListLine> specialize(IpAccessListLine ipAccessListLine) {
    AclLineMatchExpr aclLineMatchExpr = ipAccessListLine.getMatchCondition().accept(this);
    return aclLineMatchExpr == FALSE
        ? Optional.empty()
        : Optional.of(ipAccessListLine.toBuilder().setMatchCondition(aclLineMatchExpr).build());
  }

  @Override
  public final AclLineMatchExpr visitAndMatchExpr(AndMatchExpr andMatchExpr) {
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
    if (conjuncts.contains(FalseExpr.INSTANCE)) {
      return FalseExpr.INSTANCE;
    }
    return new AndMatchExpr(conjuncts);
  }

  @Override
  public final AclLineMatchExpr visitFalseExpr(FalseExpr falseExpr) {
    return falseExpr;
  }

  @Override
  public final AclLineMatchExpr visitMatchHeaderSpace(MatchHeaderSpace matchHeaderSpace) {
    HeaderSpace originalHeaderSpace = matchHeaderSpace.getHeaderspace();
    HeaderSpace headerSpace = specialize(originalHeaderSpace);
    IpSpace dstIps = headerSpace.getDstIps();
    IpSpace notDstIps = headerSpace.getNotDstIps();
    IpSpace notSrcIps = headerSpace.getNotSrcIps();
    IpSpace srcIps = headerSpace.getSrcIps();
    IpSpace srcOrDstIps = headerSpace.getSrcOrDstIps();

    boolean emptyIpSpace =
        dstIps == EmptyIpSpace.INSTANCE
            || srcIps == EmptyIpSpace.INSTANCE
            || srcOrDstIps == EmptyIpSpace.INSTANCE
            || notDstIps == UniverseIpSpace.INSTANCE
            || notSrcIps == UniverseIpSpace.INSTANCE;

    if (emptyIpSpace) {
      return FALSE;
    }

    /*
     * if any field has an empty list of required values (i.e. it must be one of zero choices), then
     * false. Exclude empty lists of forbidden values (that part of the constraint has become true).
     */
    boolean emptyDisjunction =
        emptyDisjuction(originalHeaderSpace.getDscps(), headerSpace.getDscps())
            || emptyDisjuction(originalHeaderSpace.getEcns(), headerSpace.getEcns())
            || emptyDisjuction(originalHeaderSpace.getDstPorts(), headerSpace.getDstPorts())
            || emptyDisjuction(
                originalHeaderSpace.getFragmentOffsets(), headerSpace.getFragmentOffsets())
            || emptyDisjuction(originalHeaderSpace.getIcmpCodes(), headerSpace.getIcmpCodes())
            || emptyDisjuction(originalHeaderSpace.getIcmpTypes(), headerSpace.getIcmpTypes())
            || emptyDisjuction(originalHeaderSpace.getIpProtocols(), headerSpace.getIpProtocols())
            || emptyDisjuction(originalHeaderSpace.getSrcPorts(), headerSpace.getSrcPorts())
            || emptyDisjuction(
                originalHeaderSpace.getSrcOrDstPorts(), headerSpace.getSrcOrDstPorts())
            || emptyDisjuction(
                originalHeaderSpace.getSrcOrDstProtocols(), headerSpace.getSrcOrDstProtocols())
            || emptyDisjuction(originalHeaderSpace.getStates(), headerSpace.getStates())
            || emptyDisjuction(originalHeaderSpace.getTcpFlags(), headerSpace.getTcpFlags());

    if (emptyDisjunction) {
      return FALSE;
    }

    HeaderSpace simplifiedHeaderSpace =
        headerSpace
            .toBuilder()
            .setDstIps(simplifyPositiveIpConstraint(dstIps))
            .setNotDstIps(simplifyNegativeIpConstraint(notDstIps))
            .setNotSrcIps(simplifyNegativeIpConstraint(notSrcIps))
            .setSrcIps(simplifyPositiveIpConstraint(srcIps))
            .setSrcOrDstIps(simplifyPositiveIpConstraint(srcOrDstIps))
            .build();

    if (simplifiedHeaderSpace.equals(HeaderSpace.builder().build())) {
      // unconstrained: the input headerspace contains the space we're specializing to
      return TRUE;
    }

    return new MatchHeaderSpace(simplifiedHeaderSpace);
  }

  @Override
  public final AclLineMatchExpr visitNotMatchExpr(NotMatchExpr notMatchExpr) {
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
  public final AclLineMatchExpr visitOrMatchExpr(OrMatchExpr orMatchExpr) {
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
    }
    if (disjuncts.contains(TrueExpr.INSTANCE)) {
      return TrueExpr.INSTANCE;
    }
    return new OrMatchExpr(disjuncts);
  }

  @Override
  public final AclLineMatchExpr visitPermittedByAcl(PermittedByAcl permittedByAcl) {
    return permittedByAcl;
  }

  @Override
  public final AclLineMatchExpr visitTrueExpr(TrueExpr trueExpr) {
    return trueExpr;
  }
}
