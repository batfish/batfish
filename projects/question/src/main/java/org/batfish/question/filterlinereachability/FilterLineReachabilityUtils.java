package org.batfish.question.filterlinereachability;

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import java.util.stream.Stream;
import org.batfish.datamodel.AclAclLine;
import org.batfish.datamodel.AclLine;
import org.batfish.datamodel.ExprAclLine;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.AndMatchExpr;
import org.batfish.datamodel.acl.DeniedByAcl;
import org.batfish.datamodel.acl.FalseExpr;
import org.batfish.datamodel.acl.GenericAclLineMatchExprVisitor;
import org.batfish.datamodel.acl.GenericAclLineVisitor;
import org.batfish.datamodel.acl.MatchHeaderSpace;
import org.batfish.datamodel.acl.MatchSrcInterface;
import org.batfish.datamodel.acl.NotMatchExpr;
import org.batfish.datamodel.acl.OrMatchExpr;
import org.batfish.datamodel.acl.OriginatingFromDevice;
import org.batfish.datamodel.acl.PermittedByAcl;
import org.batfish.datamodel.acl.TrueExpr;

/** Utils for extracting referenced ACLs and interfaces from an {@link AclLine} */
public class FilterLineReachabilityUtils {
  private static final ReferencedAclsCollector ACLS_COLLECTOR = new ReferencedAclsCollector();
  private static final ReferencedInterfacesCollector INTERFACES_COLLECTOR =
      new ReferencedInterfacesCollector();

  private FilterLineReachabilityUtils() {}

  public static Set<String> getReferencedAcls(AclLine line) {
    return ACLS_COLLECTOR.visit(line).collect(ImmutableSet.toImmutableSet());
  }

  public static Set<String> getReferencedInterfaces(AclLine line) {
    return INTERFACES_COLLECTOR.visit(line).collect(ImmutableSet.toImmutableSet());
  }

  /**
   * Collects names of all ACLs directly referenced in an {@link AclLineMatchExpr} or {@link
   * AclLine}. Does not recurse into referenced ACLs.
   */
  private static class ReferencedAclsCollector
      implements GenericAclLineMatchExprVisitor<Stream<String>>,
          GenericAclLineVisitor<Stream<String>> {

    /* AclLine visit methods */

    @Override
    public Stream<String> visitAclAclLine(AclAclLine aclAclLine) {
      return Stream.of(aclAclLine.getAclName());
    }

    @Override
    public Stream<String> visitExprAclLine(ExprAclLine exprAclLine) {
      return visit(exprAclLine.getMatchCondition());
    }

    /* AclLineMatchExpr visit methods */

    @Override
    public Stream<String> visitAndMatchExpr(AndMatchExpr andMatchExpr) {
      return andMatchExpr.getConjuncts().stream().flatMap(this::visit);
    }

    @Override
    public Stream<String> visitDeniedByAcl(DeniedByAcl deniedByAcl) {
      return Stream.of(deniedByAcl.getAclName());
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
   * AclLine}. Does not recurse into referenced ACLs.
   */
  private static class ReferencedInterfacesCollector
      implements GenericAclLineMatchExprVisitor<Stream<String>>,
          GenericAclLineVisitor<Stream<String>> {

    /* AclLine visit methods */

    @Override
    public Stream<String> visitAclAclLine(AclAclLine aclAclLine) {
      return Stream.of();
    }

    @Override
    public Stream<String> visitExprAclLine(ExprAclLine exprAclLine) {
      return visit(exprAclLine.getMatchCondition());
    }

    /* AclLineMatchExpr visit methods */

    @Override
    public Stream<String> visitAndMatchExpr(AndMatchExpr andMatchExpr) {
      return andMatchExpr.getConjuncts().stream().flatMap(this::visit);
    }

    @Override
    public Stream<String> visitDeniedByAcl(DeniedByAcl deniedByAcl) {
      return Stream.of();
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
