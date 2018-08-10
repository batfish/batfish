package org.batfish.datamodel.acl;

import static org.batfish.common.util.CommonUtil.toImmutableMap;

import com.google.common.collect.ImmutableSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Supplier;
import org.batfish.common.BatfishException;
import org.batfish.common.util.NonRecursiveSupplier;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpAccessListLine;

/** Find all the ACLs referenced by an IpAccessList or a collection of IpAccessLists. */
public final class SourcesReferencedByIpAccessLists {
  public static final String SOURCE_ORIGINATING_FROM_DEVICE = "DEVICE IS THE SOURCE";

  private static final class ReferenceSourcesVisitor
      implements GenericAclLineMatchExprVisitor<Void> {
    private final ImmutableSet.Builder<String> _referencedSources;

    private final Map<String, Supplier<Void>> _namedAclThunks;

    ReferenceSourcesVisitor(Map<String, IpAccessList> namedAcls) {
      /**
       * Thunks used to include sources (on demand) referenced by ACLs referenced by PermittedByAcl
       * match exprs. NonRecursiveSupplier detects cyclic references and throws an exception rather
       * than going into an infinite loop.
       */
      _namedAclThunks =
          toImmutableMap(
              namedAcls,
              Entry::getKey,
              entry ->
                  new NonRecursiveSupplier<>(
                      () -> {
                        visit(entry.getValue());
                        return null;
                      }));
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
      _referencedSources.add(SOURCE_ORIGINATING_FROM_DEVICE);
      return null;
    }

    @Override
    public Void visitOrMatchExpr(OrMatchExpr orMatchExpr) {
      orMatchExpr.getDisjuncts().forEach(this::visit);
      return null;
    }

    @Override
    public Void visitPermittedByAcl(PermittedByAcl permittedByAcl) {
      String aclName = permittedByAcl.getAclName();
      Supplier<Void> thunk = _namedAclThunks.get(aclName);
      if (thunk == null) {
        throw new BatfishException("Unknown IpAccessList " + aclName);
      }
      return thunk.get();
    }

    @Override
    public Void visitTrueExpr(TrueExpr trueExpr) {
      return null;
    }
  }

  public static Set<String> referencedSources(
      Map<String, IpAccessList> namedAcls, AclLineMatchExpr expr) {
    ReferenceSourcesVisitor visitor = new ReferenceSourcesVisitor(namedAcls);
    visitor.visit(expr);
    return visitor.referencedInterfaces();
  }

  public static Set<String> referencedSources(
      Map<String, IpAccessList> namedAcls, IpAccessList acl) {
    ReferenceSourcesVisitor visitor = new ReferenceSourcesVisitor(namedAcls);
    visitor.visit(acl);
    return visitor.referencedInterfaces();
  }
}
