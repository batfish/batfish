package org.batfish.question.filterlinereachability;

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import org.batfish.datamodel.IpAccessListLine;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.AndMatchExpr;
import org.batfish.datamodel.acl.FalseExpr;
import org.batfish.datamodel.acl.GenericAclLineMatchExprVisitor;
import org.batfish.datamodel.acl.GenericIpAccessListLineVisitor;
import org.batfish.datamodel.acl.MatchHeaderSpace;
import org.batfish.datamodel.acl.MatchSrcInterface;
import org.batfish.datamodel.acl.NotMatchExpr;
import org.batfish.datamodel.acl.OrMatchExpr;
import org.batfish.datamodel.acl.OriginatingFromDevice;
import org.batfish.datamodel.acl.PermittedByAcl;
import org.batfish.datamodel.acl.TrueExpr;

/** Utils for extracting referenced ACLs and interfaces from an {@link IpAccessListLine} */
public class FilterLineReachabilityUtils {
  private static final ReferencedAclsCollector ACLS_COLLECTOR = new ReferencedAclsCollector();
  private static final ReferencedInterfacesCollector INTERFACES_COLLECTOR =
      new ReferencedInterfacesCollector();

  private FilterLineReachabilityUtils() {}

  public static Set<String> getReferencedAcls(IpAccessListLine line) {
    return ACLS_COLLECTOR.visit(line);
  }

  public static Set<String> getReferencedInterfaces(IpAccessListLine line) {
    return INTERFACES_COLLECTOR.visit(line);
  }

  /**
   * Collects names of all ACLs directly referenced in an {@link AclLineMatchExpr} or {@link
   * IpAccessListLine}. Does not recurse into referenced ACLs.
   */
  private static class ReferencedAclsCollector
      implements GenericAclLineMatchExprVisitor<Set<String>>,
          GenericIpAccessListLineVisitor<Set<String>> {

    /* IpAccessListLine visit methods */

    @Override
    public Set<String> visitIpAccessListLine(IpAccessListLine ipAccessListLine) {
      return visit(ipAccessListLine.getMatchCondition());
    }

    /* AclLineMatchExpr visit methods */

    @Override
    public Set<String> visitAndMatchExpr(AndMatchExpr andMatchExpr) {
      return andMatchExpr.getConjuncts().stream()
          .flatMap(c -> visit(c).stream())
          .distinct()
          .collect(ImmutableSet.toImmutableSet());
    }

    @Override
    public Set<String> visitFalseExpr(FalseExpr falseExpr) {
      return ImmutableSet.of();
    }

    @Override
    public Set<String> visitMatchHeaderSpace(MatchHeaderSpace matchHeaderSpace) {
      return ImmutableSet.of();
    }

    @Override
    public Set<String> visitMatchSrcInterface(MatchSrcInterface matchSrcInterface) {
      return ImmutableSet.of();
    }

    @Override
    public Set<String> visitNotMatchExpr(NotMatchExpr notMatchExpr) {
      return visit(notMatchExpr.getOperand());
    }

    @Override
    public Set<String> visitOriginatingFromDevice(OriginatingFromDevice originatingFromDevice) {
      return ImmutableSet.of();
    }

    @Override
    public Set<String> visitOrMatchExpr(OrMatchExpr orMatchExpr) {
      return orMatchExpr.getDisjuncts().stream()
          .flatMap(c -> visit(c).stream())
          .distinct()
          .collect(ImmutableSet.toImmutableSet());
    }

    @Override
    public Set<String> visitPermittedByAcl(PermittedByAcl permittedByAcl) {
      return ImmutableSet.of(permittedByAcl.getAclName());
    }

    @Override
    public Set<String> visitTrueExpr(TrueExpr trueExpr) {
      return ImmutableSet.of();
    }
  }

  /**
   * Collects names of all interfaces directly referenced in an {@link AclLineMatchExpr} or {@link
   * IpAccessListLine}. Does not recurse into referenced ACLs.
   */
  private static class ReferencedInterfacesCollector
      implements GenericAclLineMatchExprVisitor<Set<String>>,
          GenericIpAccessListLineVisitor<Set<String>> {

    /* IpAccessListLine visit methods */

    @Override
    public Set<String> visitIpAccessListLine(IpAccessListLine ipAccessListLine) {
      return visit(ipAccessListLine.getMatchCondition());
    }

    /* AclLineMatchExpr visit methods */

    @Override
    public Set<String> visitAndMatchExpr(AndMatchExpr andMatchExpr) {
      return andMatchExpr.getConjuncts().stream()
          .flatMap(c -> visit(c).stream())
          .distinct()
          .collect(ImmutableSet.toImmutableSet());
    }

    @Override
    public Set<String> visitFalseExpr(FalseExpr falseExpr) {
      return ImmutableSet.of();
    }

    @Override
    public Set<String> visitMatchHeaderSpace(MatchHeaderSpace matchHeaderSpace) {
      return ImmutableSet.of();
    }

    @Override
    public Set<String> visitMatchSrcInterface(MatchSrcInterface matchSrcInterface) {
      return matchSrcInterface.getSrcInterfaces();
    }

    @Override
    public Set<String> visitNotMatchExpr(NotMatchExpr notMatchExpr) {
      return visit(notMatchExpr.getOperand());
    }

    @Override
    public Set<String> visitOriginatingFromDevice(OriginatingFromDevice originatingFromDevice) {
      return ImmutableSet.of();
    }

    @Override
    public Set<String> visitOrMatchExpr(OrMatchExpr orMatchExpr) {
      return orMatchExpr.getDisjuncts().stream()
          .flatMap(c -> visit(c).stream())
          .distinct()
          .collect(ImmutableSet.toImmutableSet());
    }

    @Override
    public Set<String> visitPermittedByAcl(PermittedByAcl permittedByAcl) {
      return ImmutableSet.of();
    }

    @Override
    public Set<String> visitTrueExpr(TrueExpr trueExpr) {
      return ImmutableSet.of();
    }
  }
}
