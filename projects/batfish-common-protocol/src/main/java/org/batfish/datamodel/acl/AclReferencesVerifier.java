package org.batfish.datamodel.acl;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.function.Consumer;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.VendorConversionException;
import org.batfish.datamodel.AclAclLine;
import org.batfish.datamodel.AclLine;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ExprAclLine;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.packet_policy.PacketPolicy;
import org.batfish.datamodel.transformation.Transformation;

/**
 * Provides functionality to verify absence of undefined references to ACLs in {@link
 * PermittedByAcl}, {@link DeniedByAcl}, {@link AclAclLine}, and {@link
 * org.batfish.datamodel.packet_policy.ApplyFilter}.
 *
 * <p>It is the responsibility of conversion code to handle undefined references to vendor-specific
 * structures. Conversion code must not propagate undefined references to vendor-independent
 * structures. Thus any such references to vendor-independent structures signify errors in
 * conversion code, rather than the input configuration text.
 */
public final class AclReferencesVerifier {

  /**
   * Verifies absence of undefined references to ACLs within the input {@code configuration}.
   *
   * @throws VendorConversionException if an undefined reference to an ACL is found in {@code
   *     configuration}.
   */
  public static void verify(Configuration configuration) {
    Set<String> undefinedAclReferences =
        Sets.difference(
            collectAclReferences(configuration), configuration.getIpAccessLists().keySet());
    if (!undefinedAclReferences.isEmpty()) {
      throw new VendorConversionException(
          String.format(
              "Configuration %s has undefined ACL references: %s",
              configuration.getHostname(), undefinedAclReferences));
    }
  }

  @VisibleForTesting
  @ParametersAreNonnullByDefault
  static final class CollectAclReferences
      implements GenericAclLineVisitor<Void>, GenericAclLineMatchExprVisitor<Void> {
    /**
     * Returns ACL names referenced by {@link PermittedByAcl}, {@link DeniedByAcl}, {@link
     * AclAclLine}, and {@link org.batfish.datamodel.packet_policy.ApplyFilter} in {@code c}.
     */
    public static Set<String> collect(Configuration c) {
      ImmutableSet.Builder<String> set = ImmutableSet.builder();
      CollectAclReferences collector = new CollectAclReferences(set);
      for (IpAccessList acl : c.getIpAccessLists().values()) {
        collector.visit(acl);
      }
      for (Interface i : c.getAllInterfaces().values()) {
        collector.visit(i.getIncomingTransformation());
        collector.visit(i.getOutgoingTransformation());
      }
      for (PacketPolicy pp : c.getPacketPolicies().values()) {
        collector.visit(pp);
      }
      return set.build();
    }

    private final @Nonnull ImmutableSet.Builder<String> _set;
    private final @Nonnull Set<Object> _visited;

    private CollectAclReferences(ImmutableSet.Builder<String> set) {
      _set = set;
      _visited = Collections.newSetFromMap(new IdentityHashMap<>());
    }

    // Impl below here.

    private void visit(@Nullable Transformation t) {
      // to avoid stack overflow on large transformations, use a work queue instead of recursion
      Queue<Transformation> queue = new LinkedList<>();
      Consumer<Transformation> enqueue =
          tx -> {
            if (tx != null && _visited.add(tx)) {
              queue.add(tx);
            }
          };
      enqueue.accept(t);
      while (!queue.isEmpty()) {
        Transformation tx = queue.remove();
        visit(tx.getGuard());
        enqueue.accept(tx.getAndThen());
        enqueue.accept(tx.getOrElse());
      }
    }

    private void visit(IpAccessList acl) {
      if (!_visited.add(acl)) {
        return;
      }
      for (AclLine line : acl.getLines()) {
        visit(line);
      }
    }

    private void visit(PacketPolicy pp) {
      if (!_visited.add(pp)) {
        return;
      }
      for (org.batfish.datamodel.packet_policy.Statement stmt : pp.getStatements()) {
        visit(stmt);
      }
    }

    private void visit(org.batfish.datamodel.packet_policy.Statement stmt) {
      if (!_visited.add(stmt)) {
        return;
      }
      if (stmt instanceof org.batfish.datamodel.packet_policy.ApplyFilter) {
        _set.add(((org.batfish.datamodel.packet_policy.ApplyFilter) stmt).getFilter());
      } else if (stmt instanceof org.batfish.datamodel.packet_policy.If) {
        org.batfish.datamodel.packet_policy.If ifStmt =
            (org.batfish.datamodel.packet_policy.If) stmt;
        visit(ifStmt.getMatchCondition());
        ifStmt.getTrueStatements().forEach(this::visit);
      }
      // Return and ApplyTransformation don't reference ACLs
    }

    private void visit(org.batfish.datamodel.packet_policy.BoolExpr expr) {
      if (!_visited.add(expr)) {
        return;
      }
      if (expr instanceof org.batfish.datamodel.packet_policy.FalseExpr
          || expr instanceof org.batfish.datamodel.packet_policy.TrueExpr) {
        // No references
        return;
      }
      if (expr instanceof org.batfish.datamodel.packet_policy.Conjunction) {
        ((org.batfish.datamodel.packet_policy.Conjunction) expr)
            .getConjuncts()
            .forEach(this::visit);
      } else if (expr instanceof org.batfish.datamodel.packet_policy.PacketMatchExpr) {
        visit(((org.batfish.datamodel.packet_policy.PacketMatchExpr) expr).getExpr());
      }
    }

    @Override
    public Void visit(AclLineMatchExpr expr) {
      if (!_visited.add(expr)) {
        return null;
      }
      return GenericAclLineMatchExprVisitor.super.visit(expr);
    }

    @Override
    public Void visit(AclLine line) {
      if (!_visited.add(line)) {
        return null;
      }
      return GenericAclLineVisitor.super.visit(line);
    }

    @Override
    public Void visitAndMatchExpr(AndMatchExpr andMatchExpr) {
      andMatchExpr.getConjuncts().forEach(this::visit);
      return null;
    }

    @Override
    public Void visitDeniedByAcl(DeniedByAcl deniedByAcl) {
      _set.add(deniedByAcl.getAclName());
      return null;
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
    public Void visitMatchIpProtocol(MatchIpProtocol matchIpProtocol) {
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
      _set.add(permittedByAcl.getAclName());
      return null;
    }

    @Override
    public Void visitTrueExpr(TrueExpr trueExpr) {
      return null;
    }

    @Override
    public Void visitAclAclLine(AclAclLine aclAclLine) {
      _set.add(aclAclLine.getAclName());
      return null;
    }

    @Override
    public Void visitExprAclLine(ExprAclLine exprAclLine) {
      visit(exprAclLine.getMatchCondition());
      return null;
    }
  }

  @VisibleForTesting
  static Set<String> collectAclReferences(Configuration c) {
    return CollectAclReferences.collect(c);
  }

  private AclReferencesVerifier() {}
}
