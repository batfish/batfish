package org.batfish.question.filterlinereachability;

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import java.util.stream.Stream;
import org.batfish.datamodel.AbstractAclLine;
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

/** Utils for extracting referenced ACLs and interfaces from an {@link AbstractAclLine} */
public class FilterLineReachabilityUtils {
  private static final ReferencedAclsCollector ACLS_COLLECTOR = new ReferencedAclsCollector();
  private static final ReferencedInterfacesCollector INTERFACES_COLLECTOR =
      new ReferencedInterfacesCollector();

  private FilterLineReachabilityUtils() {}

  public static Set<String> getReferencedAcls(AbstractAclLine line) {
    return ACLS_COLLECTOR.visit(line).collect(ImmutableSet.toImmutableSet());
  }

  public static Set<String> getReferencedInterfaces(AbstractAclLine line) {
    return INTERFACES_COLLECTOR.visit(line).collect(ImmutableSet.toImmutableSet());
  }

  /**
   * Collects names of all ACLs directly referenced in an {@link AclLineMatchExpr} or {@link
   * AbstractAclLine}. Does not recurse into referenced ACLs.
   */
  private static class ReferencedAclsCollector
      implements GenericAclLineMatchExprVisitor<Stream<String>>,
          GenericIpAccessListLineVisitor<Stream<String>> {

    /* AbstractAclLine visit methods */

    @Override
    public Stream<String> visitIpAccessListLine(IpAccessListLine ipAccessListLine) {
      return visit(ipAccessListLine.getMatchCondition());
    }

    /* AclLineMatchExpr visit methods */

    @Override
    public Stream<String> visitAndMatchExpr(AndMatchExpr andMatchExpr) {
      return andMatchExpr.getConjuncts().stream().flatMap(this::visit);
    }

    @Override
    public Stream<String> visitFalseExpr(FalseExpr falseExpr) {
      return Stream.of();
    }

    @Override
    public Stream<String> visitMatchHeaderSpace(MatchHeaderSpace matchHeaderSpace) {
      return Stream.of();
    }

    @Override
    public Stream<String> visitMatchSrcInterface(MatchSrcInterface matchSrcInterface) {
      return Stream.of();
    }

    @Override
    public Stream<String> visitNotMatchExpr(NotMatchExpr notMatchExpr) {
      return visit(notMatchExpr.getOperand());
    }

    @Override
    public Stream<String> visitOriginatingFromDevice(OriginatingFromDevice originatingFromDevice) {
      return Stream.of();
    }

    @Override
    public Stream<String> visitOrMatchExpr(OrMatchExpr orMatchExpr) {
      return orMatchExpr.getDisjuncts().stream().flatMap(this::visit);
    }

    @Override
    public Stream<String> visitPermittedByAcl(PermittedByAcl permittedByAcl) {
      return Stream.of(permittedByAcl.getAclName());
    }

    @Override
    public Stream<String> visitTrueExpr(TrueExpr trueExpr) {
      return Stream.of();
    }
  }

  /**
   * Collects names of all interfaces directly referenced in an {@link AclLineMatchExpr} or {@link
   * AbstractAclLine}. Does not recurse into referenced ACLs.
   */
  private static class ReferencedInterfacesCollector
      implements GenericAclLineMatchExprVisitor<Stream<String>>,
          GenericIpAccessListLineVisitor<Stream<String>> {

    /* AbstractAclLine visit methods */

    @Override
    public Stream<String> visitIpAccessListLine(IpAccessListLine ipAccessListLine) {
      return visit(ipAccessListLine.getMatchCondition());
    }

    /* AclLineMatchExpr visit methods */

    @Override
    public Stream<String> visitAndMatchExpr(AndMatchExpr andMatchExpr) {
      return andMatchExpr.getConjuncts().stream().flatMap(this::visit);
    }

    @Override
    public Stream<String> visitFalseExpr(FalseExpr falseExpr) {
      return Stream.of();
    }

    @Override
    public Stream<String> visitMatchHeaderSpace(MatchHeaderSpace matchHeaderSpace) {
      return Stream.of();
    }

    @Override
    public Stream<String> visitMatchSrcInterface(MatchSrcInterface matchSrcInterface) {
      return matchSrcInterface.getSrcInterfaces().stream();
    }

    @Override
    public Stream<String> visitNotMatchExpr(NotMatchExpr notMatchExpr) {
      return visit(notMatchExpr.getOperand());
    }

    @Override
    public Stream<String> visitOriginatingFromDevice(OriginatingFromDevice originatingFromDevice) {
      return Stream.of();
    }

    @Override
    public Stream<String> visitOrMatchExpr(OrMatchExpr orMatchExpr) {
      return orMatchExpr.getDisjuncts().stream().flatMap(this::visit);
    }

    @Override
    public Stream<String> visitPermittedByAcl(PermittedByAcl permittedByAcl) {
      return Stream.of();
    }

    @Override
    public Stream<String> visitTrueExpr(TrueExpr trueExpr) {
      return Stream.of();
    }
  }
}
