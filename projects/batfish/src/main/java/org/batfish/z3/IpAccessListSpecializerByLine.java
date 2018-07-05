package org.batfish.z3;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSortedSet;
import java.util.List;
import java.util.Objects;
import java.util.SortedSet;
import java.util.stream.Collectors;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpAccessListLine;
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
public class IpAccessListSpecializerByLine
    implements GenericAclLineMatchExprVisitor<AclLineMatchExpr> {
  private final IpAccessListLine _line;

  public IpAccessListSpecializerByLine(IpAccessListLine line) {
    _line = line;
  }

  public IpAccessList specialize(IpAccessList ipAccessList) {
    List<IpAccessListLine> specializedLines =
        ipAccessList.getLines().stream().map(this::specialize).collect(Collectors.toList());

    return createAcl(ipAccessList.getName(), specializedLines);
  }

  public IpAccessList specializeSublist(IpAccessList ipAccessList, int lastLineNum) {
    List<IpAccessListLine> specializedLines =
        ipAccessList
            .getLines()
            .subList(0, lastLineNum + 1)
            .stream()
            .map(this::specialize)
            .collect(Collectors.toList());

    return createAcl(ipAccessList.getName(), specializedLines);
  }

  private IpAccessList createAcl(String name, List<IpAccessListLine> lines) {
    return IpAccessList.builder().setName(name).setLines(lines).build();
  }

  public IpAccessListLine specialize(IpAccessListLine ipAccessListLine) {
    AclLineMatchExpr aclLineMatchExpr = ipAccessListLine.getMatchCondition().accept(this);
    return ipAccessListLine.toBuilder().setMatchCondition(aclLineMatchExpr).build();
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
    for (AclLineMatchExpr conjunct : andMatchExpr.getConjuncts()) {
      if (conjunct.accept(this) == FalseExpr.INSTANCE) {
        return FalseExpr.INSTANCE;
      }
    }
    return new AndMatchExpr(conjuncts);
  }

  @Override
  public AclLineMatchExpr visitFalseExpr(FalseExpr falseExpr) {
    return falseExpr;
  }

  @Override
  public AclLineMatchExpr visitMatchHeaderSpace(MatchHeaderSpace matchHeaderSpace) {
    IpAccessListSpecializer specializer =
        new IpAccessListSpecializer(matchHeaderSpace.getHeaderspace(), ImmutableMap.of());
    return specializer.specialize(_line).isPresent() ? matchHeaderSpace : FalseExpr.INSTANCE;
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
