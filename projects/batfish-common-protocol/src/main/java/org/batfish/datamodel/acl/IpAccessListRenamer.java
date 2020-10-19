package org.batfish.datamodel.acl;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import java.util.IdentityHashMap;
import java.util.function.Function;
import javax.annotation.Nullable;
import org.batfish.datamodel.AclAclLine;
import org.batfish.datamodel.AclLine;
import org.batfish.datamodel.ExprAclLine;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.visitors.IpSpaceRenamer;

/**
 * Renames references to named {@link org.batfish.datamodel.IpAccessList ACLs} and {@link IpSpace
 * IpSpaces} in an {@link org.batfish.datamodel.IpAccessList}.
 */
public class IpAccessListRenamer implements Function<IpAccessList, IpAccessList> {

  /**
   * Makes a copy of the given {@link AclLine} or {@link AclLineMatchExpr} with any referenced ACLs
   * and IP spaces renamed.
   */
  @VisibleForTesting
  class Visitor
      implements GenericAclLineMatchExprVisitor<AclLineMatchExpr>, GenericAclLineVisitor<AclLine> {

    private IpSpace rename(@Nullable IpSpace ipSpace) {
      return ipSpace == null ? null : _ipSpaceRenamer.apply(ipSpace);
    }

    /* AclLine visit methods */

    @Override
    public AclLine visitAclAclLine(AclAclLine aclAclLine) {
      String lineName = aclAclLine.getName() != null ? aclAclLine.getName() : aclAclLine.toString();
      return new AclAclLine(lineName, _aclRenamer.apply(aclAclLine.getAclName()));
    }

    @Override
    public AclLine visitExprAclLine(ExprAclLine exprAclLine) {
      return exprAclLine.toBuilder()
          .setMatchCondition(visit(exprAclLine.getMatchCondition()))
          .build();
    }

    /* AclLineMatchExpr visit methods */

    @Override
    public AclLineMatchExpr visitAndMatchExpr(AndMatchExpr andMatchExpr) {
      return new AndMatchExpr(
          andMatchExpr.getConjuncts().stream()
              .map(this::visit)
              .collect(ImmutableList.toImmutableList()));
    }

    @Override
    public AclLineMatchExpr visitDeniedByAcl(DeniedByAcl deniedByAcl) {
      DeniedByAcl newDeniedByAcl =
          new DeniedByAcl(
              _aclRenamer.apply(deniedByAcl.getAclName()), deniedByAcl.getTraceElement());
      _literalsMap.put(deniedByAcl, newDeniedByAcl);
      return newDeniedByAcl;
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
              headerSpace.toBuilder()
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
              .collect(ImmutableList.toImmutableList()));
    }

    @Override
    public AclLineMatchExpr visitPermittedByAcl(PermittedByAcl permittedByAcl) {
      PermittedByAcl newPermittedByAcl =
          new PermittedByAcl(
              _aclRenamer.apply(permittedByAcl.getAclName()), permittedByAcl.getTraceElement());
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
                .map(_visitor::visit)
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
