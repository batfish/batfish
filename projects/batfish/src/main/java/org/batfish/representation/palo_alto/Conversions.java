package org.batfish.representation.palo_alto;

import static org.batfish.datamodel.Names.generatedBgpCommonExportPolicyName;
import static org.batfish.datamodel.Names.generatedBgpPeerExportPolicyName;
import static org.batfish.datamodel.Names.generatedBgpPeerImportPolicyName;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.PrefixRange;
import org.batfish.datamodel.PrefixSpace;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.routing_policy.RoutingPolicy;
import org.batfish.datamodel.routing_policy.expr.BooleanExpr;
import org.batfish.datamodel.routing_policy.expr.BooleanExprs;
import org.batfish.datamodel.routing_policy.expr.CallExpr;
import org.batfish.datamodel.routing_policy.expr.Conjunction;
import org.batfish.datamodel.routing_policy.expr.DestinationNetwork;
import org.batfish.datamodel.routing_policy.expr.Disjunction;
import org.batfish.datamodel.routing_policy.expr.ExplicitPrefixSet;
import org.batfish.datamodel.routing_policy.expr.LiteralInt;
import org.batfish.datamodel.routing_policy.expr.LiteralLong;
import org.batfish.datamodel.routing_policy.expr.LiteralOrigin;
import org.batfish.datamodel.routing_policy.expr.MatchPrefixSet;
import org.batfish.datamodel.routing_policy.expr.MatchProtocol;
import org.batfish.datamodel.routing_policy.statement.If;
import org.batfish.datamodel.routing_policy.statement.SetMetric;
import org.batfish.datamodel.routing_policy.statement.SetOrigin;
import org.batfish.datamodel.routing_policy.statement.SetWeight;
import org.batfish.datamodel.routing_policy.statement.Statement;
import org.batfish.datamodel.routing_policy.statement.Statements;
import org.batfish.representation.palo_alto.PolicyRule.Action;
import org.batfish.representation.palo_alto.RedistRule.AddressFamilyIdentifier;

/** Utility conversions functions for {@link PaloAltoConfiguration} */
final class Conversions {

  private static final Statement ROUTE_MAP_PERMIT_STATEMENT =
      new If(
          BooleanExprs.CALL_EXPR_CONTEXT,
          ImmutableList.of(Statements.ReturnTrue.toStaticStatement()),
          ImmutableList.of(Statements.ExitAccept.toStaticStatement()));
  private static final Statement ROUTE_MAP_DENY_STATEMENT =
      new If(
          BooleanExprs.CALL_EXPR_CONTEXT,
          ImmutableList.of(Statements.ReturnFalse.toStaticStatement()),
          ImmutableList.of(Statements.ExitReject.toStaticStatement()));

  static @Nonnull Statement toStatement(PolicyRule policyRule) {
    ImmutableList.Builder<Statement> trueStatements = ImmutableList.builder();
    ImmutableList.Builder<BooleanExpr> conjuncts = ImmutableList.builder();

    // matches
    policyRule.getMatches().map(Conversions::toBooleanExpr).forEach(conjuncts::add);

    // updates
    policyRule.getUpdates().map(Conversions::toStatement).forEach(trueStatements::add);

    assert policyRule.getAction() != null;
    trueStatements.add(
        policyRule.getAction() == Action.ALLOW
            ? ROUTE_MAP_PERMIT_STATEMENT
            : ROUTE_MAP_DENY_STATEMENT);

    // if the condition of this policy rule does not match then fall through (no false statements)
    return new If(new Conjunction(conjuncts.build()), trueStatements.build(), ImmutableList.of());
  }

  /** Creates and stores a routing policy from the given statement in configuration object */
  static void statementToRoutingPolicy(
      Statement statement, Configuration c, String routingPolicyName) {
    RoutingPolicy.builder()
        .setOwner(c)
        .setName(routingPolicyName)
        .setStatements(ImmutableList.of(statement))
        .build();
  }

  private static @Nonnull BooleanExpr toBooleanExpr(PolicyRuleMatch match) {
    return match.accept(
        new PolicyRuleMatchVisitor<BooleanExpr>() {
          @Override
          public BooleanExpr visitPolicyRuleMatchAddressPrefixSet(
              PolicyRuleMatchAddressPrefixSet policyRuleMatchAddressPrefixSet) {
            Disjunction disjunction = new Disjunction();
            for (AddressPrefix addressPrefix :
                policyRuleMatchAddressPrefixSet.getAddressPrefixes()) {
              if (addressPrefix.getExact()) {
                disjunction
                    .getDisjuncts()
                    .add(
                        new MatchPrefixSet(
                            DestinationNetwork.instance(),
                            new ExplicitPrefixSet(
                                new PrefixSpace(
                                    PrefixRange.fromPrefix(addressPrefix.getPrefix())))));
              } else {
                disjunction
                    .getDisjuncts()
                    .add(
                        new MatchPrefixSet(
                            DestinationNetwork.instance(),
                            new ExplicitPrefixSet(
                                new PrefixSpace(
                                    new PrefixRange(
                                        addressPrefix.getPrefix(),
                                        new SubRange(
                                            addressPrefix.getPrefix().getPrefixLength(),
                                            Prefix.MAX_PREFIX_LENGTH))))));
              }
            }
            return disjunction;
          }

          @Override
          public BooleanExpr visitPolicyRuleMatchFromPeerSet(
              PolicyRuleMatchFromPeerSet policyRuleMatchFromPeerSet) {
            // Cannot be currently represented by any match boolean expressions
            return BooleanExprs.TRUE;
          }
        });
  }

  private static @Nonnull Statement toStatement(PolicyRuleUpdate update) {
    return update.accept(
        new PolicyRuleUpdateVisitior<Statement>() {
          @Override
          public Statement visitPolicyRuleUpdateMetric(
              PolicyRuleUpdateMetric policyRuleUpdateMetric) {
            return new SetMetric(new LiteralLong(policyRuleUpdateMetric.getMetric()));
          }

          @Override
          public Statement visitPolicyRuleUpdateOrigin(
              PolicyRuleUpdateOrigin policyRuleUpdateOrigin) {
            return new SetOrigin(new LiteralOrigin(policyRuleUpdateOrigin.getOrigin(), null));
          }

          @Override
          public Statement visitPolicyRuleUpdateWeight(
              PolicyRuleUpdateWeight policyRuleUpdateWeight) {
            return new SetWeight(new LiteralInt(policyRuleUpdateWeight.getWeight()));
          }
        });
  }

  /** Returns a routing policy representing all the redist-rules at the BgpVr level */
  static RoutingPolicy getBgpCommonExportPolicy(
      BgpVr bgpVr, VirtualRouter vr, Warnings w, Configuration c) {
    Map<RedistRuleRefNameOrPrefix, RedistRule> redistRules = bgpVr.getRedistRules();
    ImmutableList.Builder<Statement> statementsForCommonExportPolicy = ImmutableList.builder();

    for (Entry<RedistRuleRefNameOrPrefix, RedistRule> redistRule : redistRules.entrySet()) {
      if (redistRule.getKey().getRedistProfileName() == null) {
        // TODO handle redist-rules with prefix and IP addresses
        continue;
      }
      if (redistRule.getValue().getAddressFamilyIdentifier() != AddressFamilyIdentifier.IPV4) {
        // no export policies will be created for IPv6
        continue;
      }
      RedistProfile redistProfile =
          vr.getRedistProfiles().get(redistRule.getKey().getRedistProfileName());
      if (redistProfile == null) {
        w.redFlag(
            String.format(
                "redist-profile %s referred in %s: BGP does not exist",
                redistRule.getKey().getRedistProfileName(), vr.getName()));
        continue;
      }
      If ifStatement = redistRuleToIfStatement(redistRule.getValue(), redistProfile);
      if (ifStatement == null) {
        continue;
      }
      statementsForCommonExportPolicy.add(ifStatement);
    }

    // all BGP/IBGP type routes should be eligible for export
    statementsForCommonExportPolicy.add(
        new If(
            new MatchProtocol(RoutingProtocol.BGP, RoutingProtocol.IBGP),
            ImmutableList.of(Statements.ReturnTrue.toStaticStatement()),
            ImmutableList.of()));

    // Finally, the export policy ends with returning false: if the route falls through till this
    // point then it should not be exported
    statementsForCommonExportPolicy.add(Statements.ReturnFalse.toStaticStatement());

    String policyName = generatedBgpCommonExportPolicyName(vr.getName());
    RoutingPolicy commonExportPolicy = new RoutingPolicy(policyName, c);
    commonExportPolicy.getStatements().addAll(statementsForCommonExportPolicy.build());
    return commonExportPolicy;
  }

  @Nullable
  private static If redistRuleToIfStatement(RedistRule redistRule, RedistProfile redistProfile) {
    // TODO: handle priority of redist profile
    RedistProfileFilter filter = redistProfile.getFilter();
    if (filter == null) {
      return null;
    }
    // using conjunction since all conditions of the filter have to be met for a route to be
    // redistributed
    ImmutableList.Builder<BooleanExpr> conditionForConjunction = ImmutableList.builder();

    // TODO: add support for protocols other than static
    if (filter.getRoutingProtocols().contains(RoutingProtocol.STATIC)) {
      conditionForConjunction.add(new MatchProtocol(RoutingProtocol.STATIC));
    }
    PrefixSpace prefixSpace = new PrefixSpace();
    MatchPrefixSet matchPrefixSet =
        new MatchPrefixSet(DestinationNetwork.instance(), new ExplicitPrefixSet(prefixSpace));
    for (Prefix prefix : filter.getDestinationPrefixes()) {
      prefixSpace.addPrefix(prefix);
    }
    conditionForConjunction.add(matchPrefixSet);

    ImmutableList.Builder<Statement> trueStatements = ImmutableList.builder();
    if (redistRule.getOrigin() != null) {
      trueStatements.add(new SetOrigin(new LiteralOrigin(redistRule.getOrigin(), null)));
    }
    trueStatements.add(
        redistProfile.getAction() == RedistProfile.Action.REDIST
            ? ROUTE_MAP_PERMIT_STATEMENT
            : ROUTE_MAP_DENY_STATEMENT);

    return new If(
        new Conjunction(conditionForConjunction.build()),
        trueStatements.build(),
        // just fall through if the conditions are not matched
        ImmutableList.of());
  }

  static RoutingPolicy computeAndSetPerPeerExportPolicy(
      BgpPeer peer, Configuration c, VirtualRouter vr, BgpVr bgpVr, String peerGroupName) {
    List<PolicyRule> exportPolicyRulesUsedByThisPeer =
        bgpVr.getExportPolicyRules().values().stream()
            .filter(ep -> ep.getUsedBy().contains(peerGroupName))
            .collect(ImmutableList.toImmutableList());

    List<Statement> statementsForExportPolicyRules =
        exportPolicyRulesUsedByThisPeer.stream()
            .map(Conversions::toStatement)
            .collect(ImmutableList.toImmutableList());

    // making a routing policy for the above statements so we can use it later in a CallExpr (we
    // need CallExpr to use in a Conjunction as list of If statements cannot be used in a
    // Conjunction)
    String policyRulesRpNameForPeer =
        getRoutingPolicyNameForExportPolicyRulesForPeer(vr.getName(), peer.getName());
    RoutingPolicy.builder()
        .setOwner(c)
        .setName(policyRulesRpNameForPeer)
        .setStatements(
            !statementsForExportPolicyRules.isEmpty()
                ? statementsForExportPolicyRules
                : ImmutableList.of(Statements.ExitAccept.toStaticStatement()))
        .build();

    // for a route to be exported by this peer, it has to match the common BGP export policy AND
    // "any" of the policy rules used by this peer
    Conjunction canMatchRedistributeAndAllExportRules =
        new Conjunction(
            ImmutableList.of(
                new CallExpr(generatedBgpCommonExportPolicyName(vr.getName())),
                new CallExpr(policyRulesRpNameForPeer)));

    return RoutingPolicy.builder()
        .setOwner(c)
        .setName(generatedBgpPeerExportPolicyName(vr.getName(), peer.getName()))
        .setStatements(
            ImmutableList.of(
                new If(
                    canMatchRedistributeAndAllExportRules,
                    ImmutableList.of(Statements.ExitAccept.toStaticStatement()),
                    ImmutableList.of(Statements.ExitReject.toStaticStatement()))))
        .build();
  }

  @Nullable
  static RoutingPolicy computeAndSetPerPeerImportPolicy(
      BgpPeer peer, Configuration c, VirtualRouter vr, BgpVr bgpVr, String peerGroupName) {
    List<PolicyRule> importPolicyRulesUsedByThisPeer =
        bgpVr.getImportPolicyRules().values().stream()
            .filter(ep -> ep.getUsedBy().contains(peerGroupName))
            .collect(ImmutableList.toImmutableList());

    if (importPolicyRulesUsedByThisPeer.isEmpty()) {
      return null;
    }

    List<Statement> statementsForImportPolicyRules =
        Stream.concat(
                importPolicyRulesUsedByThisPeer.stream().map(Conversions::toStatement),
                // adding a final false statement to reject anything which didn't match any of the
                // above policy rules
                Stream.of(Statements.ReturnFalse.toStaticStatement()))
            .collect(ImmutableList.toImmutableList());

    return RoutingPolicy.builder()
        .setOwner(c)
        .setName(generatedBgpPeerImportPolicyName(vr.getName(), peer.getName()))
        .setStatements(statementsForImportPolicyRules)
        .build();
  }

  static String getRoutingPolicyNameForExportPolicyRulesForPeer(String vrName, String peerName) {
    return String.format("~BGP_POLICY_RULE_EXPORT_POLICY:%s:%s~", vrName, peerName);
  }

  private Conversions() {} // don't allow instantiation
}
