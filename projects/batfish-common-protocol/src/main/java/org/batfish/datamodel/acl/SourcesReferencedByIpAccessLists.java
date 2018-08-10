package org.batfish.datamodel.acl;

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpAccessListLine;

/** Find all the ACLs referenced by an IpAccessList or a collection of IpAccessLists. */
public final class SourcesReferencedByIpAccessLists {
  public static final String DEVICE_IS_THE_SOURCE = "DEVICE IS THE SOURCE";

  private static final class ReferenceSourcesVisitor
      implements GenericAclLineMatchExprVisitor<Void> {
    private final ImmutableSet.Builder<String> _referencedSources;

    ReferenceSourcesVisitor() {
      _referencedSources = ImmutableSet.builder();
    }

    Set<String> referencedInterfaces() {
      return _referencedSources.build();
    }

    void visit(IpAccessList acl) {
      acl.getLines().forEach(this::visit);
    }

    void visit(IpAccessListLine line) {
      visit(line.getMatchCondition());
    }

    @Override
    public Void visitAndMatchExpr(AndMatchExpr andMatchExpr) {
      andMatchExpr.getConjuncts().forEach(this::visit);
      return null;
    }

    @Override
    public Void visitFalseExpr(FalseExpr falseExpr) {
      return null;
    }

    @Override
    public Void visitMatchHeaderSpace(MatchHeaderSpace matchHeaderSpace) {
      return null;
    }

    @Override
    public Void visitMatchSrcInterface(MatchSrcInterface matchSrcInterface) {
      _referencedSources.addAll(matchSrcInterface.getSrcInterfaces());
      return null;
    }

    @Override
    public Void visitNotMatchExpr(NotMatchExpr notMatchExpr) {
      visit(notMatchExpr.getOperand());
      return null;
    }

    @Override
    public Void visitOriginatingFromDevice(OriginatingFromDevice originatingFromDevice) {
      _referencedSources.add(DEVICE_IS_THE_SOURCE);
      return null;
    }

    @Override
    public Void visitOrMatchExpr(OrMatchExpr orMatchExpr) {
      orMatchExpr.getDisjuncts().forEach(this::visit);
      return null;
    }

    @Override
    public Void visitPermittedByAcl(PermittedByAcl permittedByAcl) {
      return null;
    }

    @Override
    public Void visitTrueExpr(TrueExpr trueExpr) {
      return null;
    }
  }

  public static Set<String> referencedSources(AclLineMatchExpr expr) {
    ReferenceSourcesVisitor visitor = new ReferenceSourcesVisitor();
    visitor.visit(expr);
    return visitor.referencedInterfaces();
  }

  public static Set<String> referencedSources(IpAccessList acl) {
    ReferenceSourcesVisitor visitor = new ReferenceSourcesVisitor();
    visitor.visit(acl);
    return visitor.referencedInterfaces();
  }
}
