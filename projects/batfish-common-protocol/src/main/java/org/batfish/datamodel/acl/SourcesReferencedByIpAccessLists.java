package org.batfish.datamodel.acl;

import static com.google.common.base.Preconditions.checkArgument;
import static org.batfish.common.util.CollectionUtil.toImmutableMap;

import com.google.common.collect.ImmutableSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import org.batfish.common.BatfishException;
import org.batfish.common.util.NonRecursiveSupplier;
import org.batfish.datamodel.AclAclLine;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ExprAclLine;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.IpAccessList;

/**
 * Find all the sources referenced by an IpAccessList or a collection of IpAccessLists, including
 * source interfaces and {@link SourcesReferencedByIpAccessLists#SOURCE_ORIGINATING_FROM_DEVICE}.
 */
public final class SourcesReferencedByIpAccessLists {
  public static final String SOURCE_ORIGINATING_FROM_DEVICE = "DEVICE IS THE SOURCE";

  /**
   * Returns the active sources that can be referred to by an {@link IpAccessList} or collection of
   * them, where source is either originating on the device ({@link
   * #SOURCE_ORIGINATING_FROM_DEVICE}) or entering an interface (aka, {@link
   * org.batfish.specifier.InterfaceLinkLocation}.
   */
  public static @Nonnull Set<String> activeAclSources(Configuration c) {
    ImmutableSet.Builder<String> ret = ImmutableSet.builder();
    ret.add(SOURCE_ORIGINATING_FROM_DEVICE);
    for (Interface i : c.getAllInterfaces().values()) {
      if (i.canReceiveIpTraffic()) {
        ret.add(i.getName());
      }
    }
    return ret.build();
  }

  private static final class ReferenceSourcesVisitor
      implements GenericAclLineMatchExprVisitor<Void>, GenericAclLineVisitor<Void> {
    private final ImmutableSet.Builder<String> _referencedSources;

    private final Map<String, Supplier<Void>> _namedAclThunks;

    ReferenceSourcesVisitor(Map<String, IpAccessList> namedAcls) {
      /*
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

    private Void processAclReference(String aclName) {
      Supplier<Void> thunk = _namedAclThunks.get(aclName);
      if (thunk == null) {
        throw new BatfishException("Unknown IpAccessList " + aclName);
      }
      return thunk.get();
    }

    /* AclLine visit methods */

    @Override
    public Void visitAclAclLine(AclAclLine aclAclLine) {
      return processAclReference(aclAclLine.getAclName());
    }

    @Override
    public Void visitExprAclLine(ExprAclLine exprAclLine) {
      visit(exprAclLine.getMatchCondition());
      return null;
    }

    /* AclLineMatchExpr visit methods */

    @Override
    public Void visitAndMatchExpr(AndMatchExpr andMatchExpr) {
      andMatchExpr.getConjuncts().forEach(this::visit);
      return null;
    }

    @Override
    public Void visitDeniedByAcl(DeniedByAcl deniedByAcl) {
      return processAclReference(deniedByAcl.getAclName());
    }

    @Override
    public Void visitFalseExpr(FalseExpr falseExpr) {
      return null;
    }

    @Override
    public Void visitMatchDestinationIp(MatchDestinationIp matchDestinationIp) {
      return null;
    }

    @Override
    public Void visitMatchDestinationPort(MatchDestinationPort matchDestinationPort) {
      return null;
    }

    @Override
    public Void visitMatchHeaderSpace(MatchHeaderSpace matchHeaderSpace) {
      return null;
    }

    @Override
    public Void visitMatchSourceIp(MatchSourceIp matchSourceIp) {
      return null;
    }

    @Override
    public Void visitMatchSourcePort(MatchSourcePort matchSourcePort) {
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
      return processAclReference(permittedByAcl.getAclName());
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

  public static Set<String> referencedSources(
      Map<String, IpAccessList> namedAcls, Set<String> acls) {
    checkArgument(namedAcls.keySet().containsAll(acls));
    ReferenceSourcesVisitor visitor = new ReferenceSourcesVisitor(namedAcls);
    acls.forEach(acl -> visitor.visit(namedAcls.get(acl)));
    return visitor.referencedInterfaces();
  }
}
