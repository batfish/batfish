package org.batfish.datamodel.acl;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Ordering;
import java.util.IdentityHashMap;
import java.util.function.Function;
import javax.annotation.Nullable;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.visitors.IpSpaceRenamer;

/**
 * Renames references to named {@link org.batfish.datamodel.IpAccessList ACLs} and {@link IpSpace
 * IpSpaces} in an {@link org.batfish.datamodel.IpAccessList}.
 */
public class IpAccessListRenamer implements Function<IpAccessList, IpAccessList> {
  @VisibleForTesting
  class Visitor implements GenericAclLineMatchExprVisitor<AclLineMatchExpr> {

    private IpSpace rename(@Nullable IpSpace ipSpace) {
      return ipSpace == null ? null : _ipSpaceRenamer.apply(ipSpace);
    }

    @Override
    public AclLineMatchExpr visitAndMatchExpr(AndMatchExpr andMatchExpr) {
      return new AndMatchExpr(
          andMatchExpr.getConjuncts().stream()
              .map(this::visit)
              .collect(ImmutableSortedSet.toImmutableSortedSet(Ordering.natural())));
    }

    @Override
    public AclLineMatchExpr visitFalseExpr(FalseExpr falseExpr) {
      return falseExpr;
    }

    @Override
    public AclLineMatchExpr visitMatchHeaderSpace(MatchHeaderSpace matchHeaderSpace) {
      HeaderSpace headerSpace = matchHeaderSpace.getHeaderspace();
      IpSpace dstIps = rename(headerSpace.getDstIps());
      IpSpace notDstIps = rename(headerSpace.getNotDstIps());
      IpSpace srcIps = rename(headerSpace.getSrcIps());
      IpSpace notSrcIps = rename(headerSpace.getNotSrcIps());
      IpSpace srcOrDstIps = rename(headerSpace.getSrcOrDstIps());
      MatchHeaderSpace newMatchHeaderSpace =
          new MatchHeaderSpace(
              headerSpace
                  .toBuilder()
                  .setDstIps(dstIps)
                  .setNotDstIps(notDstIps)
                  .setSrcIps(srcIps)
                  .setNotSrcIps(notSrcIps)
                  .setSrcOrDstIps(srcOrDstIps)
                  .build());
      _literalsMap.put(matchHeaderSpace, newMatchHeaderSpace);
      return newMatchHeaderSpace;
    }

    @Override
    public AclLineMatchExpr visitMatchSrcInterface(MatchSrcInterface matchSrcInterface) {
      return matchSrcInterface;
    }

    @Override
    public AclLineMatchExpr visitNotMatchExpr(NotMatchExpr notMatchExpr) {
      return new NotMatchExpr(notMatchExpr.getOperand().accept(this));
    }

    @Override
    public AclLineMatchExpr visitOriginatingFromDevice(
        OriginatingFromDevice originatingFromDevice) {
      return originatingFromDevice;
    }

    @Override
    public AclLineMatchExpr visitOrMatchExpr(OrMatchExpr orMatchExpr) {
      return new OrMatchExpr(
          orMatchExpr.getDisjuncts().stream()
              .map(this::visit)
              .collect(ImmutableSortedSet.toImmutableSortedSet(Ordering.natural())));
    }

    @Override
    public AclLineMatchExpr visitPermittedByAcl(PermittedByAcl permittedByAcl) {
      PermittedByAcl newPermittedByAcl =
          new PermittedByAcl(
              _aclRenamer.apply(permittedByAcl.getAclName()),
              permittedByAcl.getDefaultAccept(),
              permittedByAcl.getDescription());
      _literalsMap.put(permittedByAcl, newPermittedByAcl);
      return newPermittedByAcl;
    }

    @Override
    public AclLineMatchExpr visitTrueExpr(TrueExpr trueExpr) {
      return trueExpr;
    }
  }

  private final Function<String, String> _aclRenamer;

  private IpSpaceRenamer _ipSpaceRenamer;

  // a map from each literal in the original ACLs to its replacement in the renamed ACLs,
  // if a new one was created
  private IdentityHashMap<AclLineMatchExpr, AclLineMatchExpr> _literalsMap;

  private final Visitor _visitor;

  public IpAccessListRenamer(Function<String, String> aclRenamer, IpSpaceRenamer ipSpaceRenamer) {
    _aclRenamer = aclRenamer;
    _ipSpaceRenamer = ipSpaceRenamer;
    _literalsMap = new IdentityHashMap<>();
    _visitor = new Visitor();
  }

  @Override
  public IpAccessList apply(IpAccessList ipAccessList) {
    return IpAccessList.builder()
        .setName(_aclRenamer.apply(ipAccessList.getName()))
        .setLines(
            ipAccessList.getLines().stream()
                .map(
                    ln ->
                        ln.toBuilder()
                            .setMatchCondition(_visitor.visit(ln.getMatchCondition()))
                            .build())
                .collect(ImmutableList.toImmutableList()))
        .build();
  }

  @VisibleForTesting
  Visitor getAclLineMatchExprVisitor() {
    return _visitor;
  }

  IdentityHashMap<AclLineMatchExpr, AclLineMatchExpr> getLiteralsMap() {
    return _literalsMap;
  }
}
