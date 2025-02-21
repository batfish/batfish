package org.batfish.representation.palo_alto;

import static org.batfish.datamodel.Names.generatedBgpCommonExportPolicyName;
import static org.batfish.datamodel.Names.generatedBgpPeerExportPolicyName;
import static org.batfish.datamodel.Names.generatedBgpPeerImportPolicyName;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.primitives.Ints;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.IntegerSpace;
import org.batfish.datamodel.IpProtocol;
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
import org.batfish.representation.palo_alto.application_definitions.ApplicationDefinition;
import org.batfish.representation.palo_alto.application_definitions.Default;
import org.batfish.representation.palo_alto.application_definitions.Port;

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
        w.redFlagf(
            "redist-profile %s referred in %s: BGP does not exist",
            redistRule.getKey().getRedistProfileName(), vr.getName());
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

  private static @Nullable If redistRuleToIfStatement(
      RedistRule redistRule, RedistProfile redistProfile) {
    // TODO: handle priority of redist profile
    RedistProfileFilter filter = redistProfile.getFilter();
    if (filter == null) {
      return null;
    }

    ImmutableList.Builder<BooleanExpr> redistConditions = ImmutableList.builder();

    ImmutableList.Builder<RoutingProtocol> protocolConditions = ImmutableList.builder();
    for (RoutingProtocol protocol : filter.getRoutingProtocols()) {
      protocolConditions.add(protocol);
      // BGP means both BGP and IBGP
      if (protocol == RoutingProtocol.BGP) {
        protocolConditions.add(RoutingProtocol.IBGP);
      }
    }
    redistConditions.add(new MatchProtocol(protocolConditions.build()));

    // TODO: I believe the set of prefixes can be empty and it means universe prefix space, not
    // empty set.
    PrefixSpace prefixSpace = new PrefixSpace();
    MatchPrefixSet matchPrefixSet =
        new MatchPrefixSet(DestinationNetwork.instance(), new ExplicitPrefixSet(prefixSpace));
    for (Prefix prefix : filter.getDestinationPrefixes()) {
      prefixSpace.addPrefix(prefix);
    }
    redistConditions.add(matchPrefixSet);

    ImmutableList.Builder<Statement> trueStatements = ImmutableList.builder();
    if (redistRule.getOrigin() != null) {
      trueStatements.add(new SetOrigin(new LiteralOrigin(redistRule.getOrigin(), null)));
    }
    trueStatements.add(
        redistProfile.getAction() == RedistProfile.Action.REDIST
            ? ROUTE_MAP_PERMIT_STATEMENT
            : ROUTE_MAP_DENY_STATEMENT);

    return new If(
        new Conjunction(redistConditions.build()),
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

  static @Nullable RoutingPolicy computeAndSetPerPeerImportPolicy(
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

  /** Convert an {@link ApplicationDefinition} into a datamodel {@link Application}. */
  @VisibleForTesting
  static @Nonnull Application definitionToApp(ApplicationDefinition appDef) {
    String appName = appDef.getName();
    return Application.builder(appName)
        .setDescription(String.format("built-in application %s", appName))
        .addServices(toServices(appDef))
        .build();
  }

  /**
   * Return a {@link List} of {@link Service}s corresponding to the specified {@link
   * ApplicationDefinition}.
   */
  private static @Nonnull List<Service> toServices(ApplicationDefinition appDef) {
    String appName = appDef.getName();
    Default defaultVal = appDef.getDefault();
    // Skip applications without any default port/protocol info - nothing to do in Batfish yet
    if (defaultVal == null) {
      return ImmutableList.of();
    }

    if (defaultVal.getPort() != null) {
      return portToServices(appName, defaultVal.getPort());
    } else if (defaultVal.getIdentByIpProtocol() != null) {
      return protocolToServices(appName, defaultVal.getIdentByIpProtocol());
    } else if (defaultVal.getIdentByIcmpType() != null) {
      return ImmutableList.of(icmpTypeToService(appName, defaultVal.getIdentByIcmpType()));
    }
    // Just ignore ping6/icmp6 type for now
    assert appName.equals("ping6");
    return ImmutableList.of();
  }

  private static @Nonnull List<Service> portToServices(String appName, Port port) {
    return port.getMember().stream()
        .map(m -> portMemberToService(appName, m))
        .filter(Optional::isPresent)
        .map(Optional::get)
        .collect(ImmutableList.toImmutableList());
  }

  /**
   * Convert a {@link Port} {@code member} string to its corresponding {@link Service}. If there is
   * no IPv4 service that corresponds to this member, then {@link Optional#empty()} is returned.
   */
  private static Optional<Service> portMemberToService(String appName, String member) {
    Service.Builder service = Service.builder(String.format("%s (%s)", appName, member));
    String[] parts = member.split("/", -1);
    assert parts.length == 2;
    String protocol = parts[0];
    String ports = parts[1];

    // Only TCP and UDP built-in applications have specific ports
    if (protocol.equals("tcp") || protocol.equals("udp")) {
      portsStringToIntegerSpace(ports).getSubRanges().forEach(service::addPorts);
    } else {
      assert ports.equals("dynamic");
    }

    switch (protocol) {
      case "icmp":
        return Optional.of(service.setIpProtocol(IpProtocol.ICMP).build());
      case "icmp6":
        return Optional.empty();
      case "tcp":
        return Optional.of(service.setIpProtocol(IpProtocol.TCP).build());
      case "udp":
      default:
        assert protocol.equals("udp");
        return Optional.of(service.setIpProtocol(IpProtocol.UDP).build());
    }
  }

  /**
   * Convert a string representing an application's ports into its {@link IntegerSpace}
   * representation.
   */
  @VisibleForTesting
  static IntegerSpace portsStringToIntegerSpace(String ports) {
    String[] parts = ports.replace(" ", "").split(",", -1);

    IntegerSpace.Builder space = IntegerSpace.builder();
    for (String part : parts) {
      // Keyword
      // TODO confirm behavior of dynamic
      if (part.equals("dynamic") || part.equals("any")) {
        return IntegerSpace.PORTS;
      }

      // Plain number
      if (!part.contains("-")) {
        Integer val = Ints.tryParse(part);
        assert val != null;
        space.including(val);
        continue;
      }

      // Range
      space.including(rangeStringToSubRange(part));
    }
    return space.build();
  }

  /**
   * Convert a simple range-string (e.g. "1234-1236") into a {@link SubRange}. The provided {@code
   * range} must be a {@link String} containing a start number, hyphen, and end number.
   */
  private static @Nonnull SubRange rangeStringToSubRange(String range) {
    String[] loHi = range.split("-", -1);
    assert loHi.length == 2;
    Integer lo = Ints.tryParse(loHi[0]);
    Integer hi = Ints.tryParse(loHi[1]);
    assert lo != null && hi != null;
    assert hi >= lo;
    return new SubRange(lo, hi);
  }

  /**
   * Return a {@link List} of {@link Service}s representing the specified {@code protocol} (number
   * or number range).
   */
  private static @Nonnull List<Service> protocolToServices(String appName, String protocol) {
    // Range of protocol numbers
    if (protocol.contains("-")) {
      return rangeStringToSubRange(protocol)
          .asStream()
          .mapToObj(
              i ->
                  Service.builder(String.format("%s (IP protocol %d)", appName, i))
                      .setIpProtocol(IpProtocol.fromNumber(i))
                      .build())
          .collect(ImmutableList.toImmutableList());
    }

    // Single number
    Integer protocolNumber = Ints.tryParse(protocol);
    assert protocolNumber != null;
    return ImmutableList.of(
        Service.builder(appName).setIpProtocol(IpProtocol.fromNumber(protocolNumber)).build());
  }

  /** Return a {@link Service} representing the specified {@code icmpType} (number). */
  private static @Nonnull Service icmpTypeToService(String appName, String icmpType) {
    Integer integer = Ints.tryParse(icmpType);
    assert integer != null;
    return Service.builder(appName).setIpProtocol(IpProtocol.ICMP).setIcmpType(integer).build();
  }

  private Conversions() {} // don't allow instantiation
}
