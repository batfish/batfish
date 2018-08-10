package org.batfish.datamodel.acl;

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpAccessListLine;

/** Find all the ACLs referenced by an IpAccessList or a collection of IpAccessLists. */
public final class InterfacesReferencedByIpAccessLists {
  private static final class ReferencedInterfacesVisitor
      implements GenericAclLineMatchExprVisitor<Void> {
    private final ImmutableSet.Builder<String> _referencedInterfaces;

    ReferencedInterfacesVisitor() {
      _referencedInterfaces = ImmutableSet.builder();
    }

    Set<String> referencedInterfaces() {
      return _referencedInterfaces.build();
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
      _referencedInterfaces.addAll(matchSrcInterface.getSrcInterfaces());
      return null;
    }

    @Override
    public Void visitNotMatchExpr(NotMatchExpr notMatchExpr) {
      visit(notMatchExpr.getOperand());
      return null;
    }

    @Override
    public Void visitOriginatingFromDevice(OriginatingFromDevice originatingFromDevice) {
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

  public static Set<String> referencedInterfaces(AclLineMatchExpr expr) {
    ReferencedInterfacesVisitor visitor = new ReferencedInterfacesVisitor();
    visitor.visit(expr);
    return visitor.referencedInterfaces();
  }

  public static Set<String> referencedInterfaces(IpAccessList acl) {
    ReferencedInterfacesVisitor visitor = new ReferencedInterfacesVisitor();
    visitor.visit(acl);
    return visitor.referencedInterfaces();
  }
}
