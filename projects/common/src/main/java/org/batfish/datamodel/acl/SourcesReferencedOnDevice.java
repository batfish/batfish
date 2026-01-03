package org.batfish.datamodel.acl;

import static org.batfish.datamodel.acl.SourcesReferencedByIpAccessLists.activeAclSources;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.packet_policy.ApplyFilter;
import org.batfish.datamodel.packet_policy.ApplyTransformation;
import org.batfish.datamodel.packet_policy.BoolExpr;
import org.batfish.datamodel.packet_policy.BoolExprVisitor;
import org.batfish.datamodel.packet_policy.Conjunction;
import org.batfish.datamodel.packet_policy.FalseExpr;
import org.batfish.datamodel.packet_policy.FibLookupOutgoingInterfaceIsOneOf;
import org.batfish.datamodel.packet_policy.If;
import org.batfish.datamodel.packet_policy.PacketMatchExpr;
import org.batfish.datamodel.packet_policy.PacketPolicy;
import org.batfish.datamodel.packet_policy.Return;
import org.batfish.datamodel.packet_policy.Statement;
import org.batfish.datamodel.packet_policy.StatementVisitor;
import org.batfish.datamodel.packet_policy.TrueExpr;
import org.batfish.datamodel.transformation.Transformation;

/**
 * Find all the sources referenced by {@link AclLineMatchExpr} configuration on a device.
 *
 * <p>Here, sources means any interface referenced with a {@link MatchSrcInterface} or the device
 * itself ({@link OriginatingFromDevice}).
 */
public final class SourcesReferencedOnDevice {

  /**
   * Like {@link #allReferencedSources(Configuration)}, but limited to only active source
   * interfaces.
   */
  public static Set<String> activeReferencedSources(Configuration c) {
    return Sets.intersection(allReferencedSources(c), activeAclSources(c));
  }

  /**
   * Looks everywhere in a {@link Configuration} that {@link MatchSrcInterface} can occur, and
   * collects all such references.
   *
   * <p>Considered objects include:
   *
   * <ul>
   *   <li>ACLs (including arbitrary nesting)
   *   <li>Guards in {@link Transformation}
   *   <li>Guards in {@link PacketPolicy} {@link BoolExpr}
   * </ul>
   */
  public static Set<String> allReferencedSources(Configuration c) {
    Map<String, IpAccessList> ipAccessLists = c.getIpAccessLists();
    Map<String, PacketPolicy> packetPolicies = c.getPacketPolicies();
    Set<Object> visited = Collections.newSetFromMap(new IdentityHashMap<>());
    Set<String> referenced = new HashSet<>();
    referenced.addAll(
        SourcesReferencedByIpAccessLists.referencedSources(ipAccessLists, ipAccessLists.keySet()));
    collectAllTransformationReferences(c, visited, referenced);
    collectAllPacketPolicyReferences(packetPolicies, ipAccessLists, visited, referenced);
    return ImmutableSet.copyOf(referenced);
  }

  private static void collectAllPacketPolicyReferences(
      Map<String, PacketPolicy> packetPolicies,
      Map<String, IpAccessList> ipAccessLists,
      Set<Object> visited,
      Set<String> referenced) {
    for (PacketPolicy p : packetPolicies.values()) {
      collectPacketPolicyReferences(p, ipAccessLists, visited, referenced);
    }
  }

  @VisibleForTesting
  static void collectPacketPolicyReferences(
      PacketPolicy p,
      Map<String, IpAccessList> ipAccessLists,
      Set<Object> visited,
      Set<String> referenced) {
    BoolExprVisitor<Void> boolCollector =
        new BoolExprVisitor<Void>() {
          @Override
          public Void visit(@Nonnull BoolExpr expr) {
            if (!visited.add(expr)) {
              // already visited
              return null;
            }
            return BoolExprVisitor.super.visit(expr);
          }

          @Override
          public Void visitPacketMatchExpr(PacketMatchExpr expr) {
            referenced.addAll(
                SourcesReferencedByIpAccessLists.referencedSources(ipAccessLists, expr.getExpr()));
            return null;
          }

          @Override
          public Void visitTrueExpr(@Nonnull TrueExpr expr) {
            // Nothing
            return null;
          }

          @Override
          public Void visitFalseExpr(@Nonnull FalseExpr expr) {
            // Nothing
            return null;
          }

          @Override
          public Void visitFibLookupOutgoingInterfaceIsOneOf(
              @Nonnull FibLookupOutgoingInterfaceIsOneOf expr) {
            // Nothing
            return null;
          }

          @Override
          public Void visitConjunction(Conjunction expr) {
            expr.getConjuncts().forEach(this::visit);
            return null;
          }
        };

    StatementVisitor<Void> collector =
        new StatementVisitor<Void>() {
          @Override
          public Void visit(@Nonnull Statement statement) {
            if (!visited.add(statement)) {
              // already visited
              return null;
            }
            return StatementVisitor.super.visit(statement);
          }

          @Override
          public Void visitApplyFilter(@Nonnull ApplyFilter applyFilter) {
            // nothing to do here, we have already converted all ACLs.
            return null;
          }

          @Override
          public Void visitApplyTransformation(ApplyTransformation transformation) {
            collectTransformationReferences(
                transformation.getTransformation(), ipAccessLists, visited, referenced);
            return null;
          }

          @Override
          public Void visitIf(If ifStmt) {
            boolCollector.visit(ifStmt.getMatchCondition());
            ifStmt.getTrueStatements().forEach(this::visit);
            return null;
          }

          @Override
          public Void visitReturn(@Nonnull Return returnStmt) {
            // Nothing referenced.
            return null;
          }
        };

    // Collect all references in all statements.
    p.getStatements().forEach(collector::visit);
  }

  private static void collectAllTransformationReferences(
      Configuration c, Set<Object> visited, Set<String> referenced) {
    for (Interface i : c.getAllInterfaces().values()) {
      if (!i.getActive()) {
        continue;
      }
      if (!i.canSendIpTraffic() && !i.canReceiveIpTraffic()) {
        // Transformations can only be reached for sent/received traffic.
        continue;
      }
      if (i.getIncomingTransformation() != null) {
        collectTransformationReferences(
            i.getIncomingTransformation(), c.getIpAccessLists(), visited, referenced);
      }
      if (i.getOutgoingTransformation() != null) {
        collectTransformationReferences(
            i.getOutgoingTransformation(), c.getIpAccessLists(), visited, referenced);
      }
    }
  }

  private static void collectTransformationReferences(
      Transformation t,
      Map<String, IpAccessList> namedAcls,
      Set<Object> visitedAlready,
      Set<String> referenced) {
    if (!visitedAlready.add(t)) {
      // already been here.
      return;
    }
    referenced.addAll(SourcesReferencedByIpAccessLists.referencedSources(namedAcls, t.getGuard()));
    if (t.getAndThen() != null) {
      collectTransformationReferences(t.getAndThen(), namedAcls, visitedAlready, referenced);
    }
    if (t.getOrElse() != null) {
      collectTransformationReferences(t.getOrElse(), namedAcls, visitedAlready, referenced);
    }
  }

  private SourcesReferencedOnDevice() {} // prevent construction of utility class
}
