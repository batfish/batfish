package org.batfish.representation.juniper;

import static com.google.common.base.MoreObjects.firstNonNull;
import static java.util.stream.Collectors.groupingBy;
import static org.batfish.datamodel.BgpPeerConfig.ALL_AS_NUMBERS;
import static org.batfish.datamodel.BumTransportMethod.UNICAST_FLOOD_GROUP;
import static org.batfish.datamodel.Names.escapeNameIfNeeded;
import static org.batfish.datamodel.Names.zoneToZoneFilter;
import static org.batfish.datamodel.acl.AclLineMatchExprs.and;
import static org.batfish.datamodel.acl.AclLineMatchExprs.matchSrcInterface;
import static org.batfish.datamodel.acl.AclLineMatchExprs.or;
import static org.batfish.datamodel.bgp.AllowRemoteAsOutMode.ALWAYS;
import static org.batfish.datamodel.bgp.AllowRemoteAsOutMode.EXCEPT_FIRST;
import static org.batfish.datamodel.bgp.LocalOriginationTypeTieBreaker.NO_PREFERENCE;
import static org.batfish.datamodel.bgp.NextHopIpTieBreaker.HIGHEST_NEXT_HOP_IP;
import static org.batfish.datamodel.routing_policy.statement.Statements.ReturnFalse;
import static org.batfish.representation.juniper.EthernetSwitching.DEFAULT_VLAN_MEMBER;
import static org.batfish.representation.juniper.JuniperStructureType.ADDRESS_BOOK;
import static org.batfish.representation.juniper.JuniperStructureType.POLICY_STATEMENT_TERM;
import static org.batfish.representation.juniper.JuniperStructureType.ROUTING_INSTANCE;
import static org.batfish.representation.juniper.NatPacketLocation.interfaceLocation;
import static org.batfish.representation.juniper.NatPacketLocation.routingInstanceLocation;
import static org.batfish.representation.juniper.NatPacketLocation.zoneLocation;
import static org.batfish.representation.juniper.PsFromNeighbor.toBooleanExprMany;
import static org.batfish.representation.juniper.RoutingInformationBase.RIB_IPV4_UNICAST;
import static org.batfish.representation.juniper.RoutingInstance.OSPF_INTERNAL_SUMMARY_DISCARD_METRIC;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import com.google.common.collect.Range;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.VendorConversionException;
import org.batfish.common.Warnings;
import org.batfish.common.util.CollectionUtil;
import org.batfish.datamodel.AclAclLine;
import org.batfish.datamodel.AclIpSpace;
import org.batfish.datamodel.AclLine;
import org.batfish.datamodel.AuthenticationKey;
import org.batfish.datamodel.AuthenticationKeyChain;
import org.batfish.datamodel.BgpActivePeerConfig;
import org.batfish.datamodel.BgpAuthenticationAlgorithm;
import org.batfish.datamodel.BgpAuthenticationSettings;
import org.batfish.datamodel.BgpPassivePeerConfig;
import org.batfish.datamodel.BgpPeerConfig.Builder;
import org.batfish.datamodel.BgpProcess;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.ConnectedRouteMetadata;
import org.batfish.datamodel.DeviceModel;
import org.batfish.datamodel.ExprAclLine;
import org.batfish.datamodel.FirewallSessionInterfaceInfo;
import org.batfish.datamodel.FirewallSessionInterfaceInfo.Action;
import org.batfish.datamodel.IkeKeyType;
import org.batfish.datamodel.IkePhase1Key;
import org.batfish.datamodel.IkePhase1Policy;
import org.batfish.datamodel.IkePhase1Proposal;
import org.batfish.datamodel.InactiveReason;
import org.batfish.datamodel.IntegerSpace;
import org.batfish.datamodel.Interface.Dependency;
import org.batfish.datamodel.Interface.DependencyType;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.IpSpaceMetadata;
import org.batfish.datamodel.IpSpaceReference;
import org.batfish.datamodel.IpWildcard;
import org.batfish.datamodel.IpWildcardSetIpSpace;
import org.batfish.datamodel.IpsecPeerConfig;
import org.batfish.datamodel.IpsecPhase2Policy;
import org.batfish.datamodel.IpsecPhase2Proposal;
import org.batfish.datamodel.IpsecStaticPeerConfig;
import org.batfish.datamodel.IsoAddress;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.LongSpace;
import org.batfish.datamodel.MainRibVrfLeakConfig;
import org.batfish.datamodel.MultipathEquivalentAsPathMatchMode;
import org.batfish.datamodel.OriginType;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.PrefixRange;
import org.batfish.datamodel.PrefixSpace;
import org.batfish.datamodel.Route;
import org.batfish.datamodel.RouteFilterList;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.SnmpCommunity;
import org.batfish.datamodel.SnmpServer;
import org.batfish.datamodel.SubRange;
import org.batfish.datamodel.SwitchportEncapsulationType;
import org.batfish.datamodel.SwitchportMode;
import org.batfish.datamodel.TraceElement;
import org.batfish.datamodel.UseConstantIp;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.VrfLeakConfig;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.MatchSrcInterface;
import org.batfish.datamodel.acl.OriginatingFromDevice;
import org.batfish.datamodel.acl.PermittedByAcl;
import org.batfish.datamodel.acl.TrueExpr;
import org.batfish.datamodel.bgp.AddressFamilyCapabilities;
import org.batfish.datamodel.bgp.BgpConfederation;
import org.batfish.datamodel.bgp.Ipv4UnicastAddressFamily;
import org.batfish.datamodel.bgp.community.Community;
import org.batfish.datamodel.dataplane.rib.RibId;
import org.batfish.datamodel.isis.IsisInterfaceMode;
import org.batfish.datamodel.isis.IsisLevel;
import org.batfish.datamodel.isis.IsisProcess;
import org.batfish.datamodel.ospf.OspfAreaSummary;
import org.batfish.datamodel.ospf.OspfMetricType;
import org.batfish.datamodel.ospf.OspfNetworkType;
import org.batfish.datamodel.ospf.OspfProcess;
import org.batfish.datamodel.packet_policy.Drop;
import org.batfish.datamodel.packet_policy.PacketMatchExpr;
import org.batfish.datamodel.packet_policy.PacketPolicy;
import org.batfish.datamodel.packet_policy.Return;
import org.batfish.datamodel.route.nh.NextHop;
import org.batfish.datamodel.route.nh.NextHopDiscard;
import org.batfish.datamodel.route.nh.NextHopInterface;
import org.batfish.datamodel.route.nh.NextHopIp;
import org.batfish.datamodel.route.nh.NextHopVrf;
import org.batfish.datamodel.routing_policy.RoutingPolicy;
import org.batfish.datamodel.routing_policy.communities.ColonSeparatedRendering;
import org.batfish.datamodel.routing_policy.communities.CommunityIs;
import org.batfish.datamodel.routing_policy.communities.CommunityMatchAny;
import org.batfish.datamodel.routing_policy.communities.CommunityMatchExpr;
import org.batfish.datamodel.routing_policy.communities.CommunityMatchRegex;
import org.batfish.datamodel.routing_policy.communities.CommunityNot;
import org.batfish.datamodel.routing_policy.communities.CommunitySet;
import org.batfish.datamodel.routing_policy.communities.CommunitySetMatchAll;
import org.batfish.datamodel.routing_policy.communities.CommunitySetMatchExpr;
import org.batfish.datamodel.routing_policy.communities.CommunitySetNot;
import org.batfish.datamodel.routing_policy.communities.HasCommunity;
import org.batfish.datamodel.routing_policy.communities.LiteralCommunitySet;
import org.batfish.datamodel.routing_policy.communities.SetCommunities;
import org.batfish.datamodel.routing_policy.expr.BooleanExpr;
import org.batfish.datamodel.routing_policy.expr.BooleanExprs;
import org.batfish.datamodel.routing_policy.expr.CallExpr;
import org.batfish.datamodel.routing_policy.expr.Conjunction;
import org.batfish.datamodel.routing_policy.expr.ConjunctionChain;
import org.batfish.datamodel.routing_policy.expr.DestinationNetwork;
import org.batfish.datamodel.routing_policy.expr.Disjunction;
import org.batfish.datamodel.routing_policy.expr.ExplicitPrefixSet;
import org.batfish.datamodel.routing_policy.expr.FirstMatchChain;
import org.batfish.datamodel.routing_policy.expr.LiteralInt;
import org.batfish.datamodel.routing_policy.expr.LiteralOrigin;
import org.batfish.datamodel.routing_policy.expr.MatchLocalRouteSourcePrefixLength;
import org.batfish.datamodel.routing_policy.expr.MatchPrefixSet;
import org.batfish.datamodel.routing_policy.expr.MatchProtocol;
import org.batfish.datamodel.routing_policy.expr.NamedPrefixSet;
import org.batfish.datamodel.routing_policy.expr.PrefixExpr;
import org.batfish.datamodel.routing_policy.expr.PrefixSetExpr;
import org.batfish.datamodel.routing_policy.expr.TrackSucceeded;
import org.batfish.datamodel.routing_policy.statement.CallStatement;
import org.batfish.datamodel.routing_policy.statement.Comment;
import org.batfish.datamodel.routing_policy.statement.If;
import org.batfish.datamodel.routing_policy.statement.SetAdministrativeCost;
import org.batfish.datamodel.routing_policy.statement.SetDefaultPolicy;
import org.batfish.datamodel.routing_policy.statement.SetOrigin;
import org.batfish.datamodel.routing_policy.statement.SetOspfMetricType;
import org.batfish.datamodel.routing_policy.statement.Statement;
import org.batfish.datamodel.routing_policy.statement.Statements;
import org.batfish.datamodel.routing_policy.statement.TraceableStatement;
import org.batfish.datamodel.tracking.TrackMethod;
import org.batfish.datamodel.tracking.TrackMethods;
import org.batfish.datamodel.transformation.Transformation;
import org.batfish.datamodel.vxlan.Layer2Vni;
import org.batfish.datamodel.vxlan.Layer3Vni;
import org.batfish.datamodel.vxlan.Vni;
import org.batfish.representation.juniper.BgpGroup.BgpGroupType;
import org.batfish.representation.juniper.FwTerm.Field;
import org.batfish.representation.juniper.Interface.InterfaceType;
import org.batfish.representation.juniper.Interface.VlanTaggingMode;
import org.batfish.representation.juniper.OspfInterfaceSettings.OspfInterfaceType;
import org.batfish.representation.juniper.Zone.AddressBookType;
import org.batfish.vendor.VendorConfiguration;
import org.batfish.vendor.VendorStructureId;

public final class JuniperConfiguration extends VendorConfiguration {

  public static final String ACL_NAME_COMBINED_INCOMING = "~COMBINED_INCOMING_FILTER~";

  public static final String ACL_NAME_GLOBAL_POLICY = "~GLOBAL_SECURITY_POLICY~";

  public static final String ACL_NAME_SCREEN = "~SCREEN~";

  public static final String ACL_NAME_SCREEN_INTERFACE = "~SCREEN_INTERFACE~";

  public static final String ACL_NAME_SCREEN_ZONE = "~SCREEN_ZONE~";

  public static final String ACL_NAME_SECURITY_POLICY = "~SECURITY_POLICIES_TO~";

  /** Juniper uses AD 170 for both EBGP and IBGP routes. */
  public static final int DEFAULT_BGP_ADMIN_DISTANCE = 170;

  /** Juniper's default routing instance is called "master". */
  public static final @Nonnull String DEFAULT_ROUTING_INSTANCE_NAME = "master";

  /** Normalize to VI VRF name. */
  public static @Nonnull String toVrfName(String routingInstanceName) {
    if (routingInstanceName.equals(DEFAULT_ROUTING_INSTANCE_NAME)) {
      // TODO: preserve Junos name, which is tricky. There's too much in Batfish that relies on
      // default vrf being this default.
      return Configuration.DEFAULT_VRF_NAME;
    }
    return routingInstanceName;
  }

  private static final ConnectedRouteMetadata JUNIPER_CONNECTED_ROUTE_METADATA =
      ConnectedRouteMetadata.builder().setGenerateLocalNullRouteIfDown(true).build();

  /** Do not generate any routes for a loopback address. */
  private static final ConnectedRouteMetadata JUNIPER_CONNECTED_ROUTE_METADATA_LOOPBACKS =
      ConnectedRouteMetadata.builder()
          .setGenerateLocalNullRouteIfDown(false)
          .setGenerateConnectedRoute(false)
          .setGenerateLocalRoute(false)
          .build();

  @VisibleForTesting
  static @Nonnull ConnectedRouteMetadata getJuniperConnectedRouteMetadata(
      ConcreteInterfaceAddress addr) {
    if (Prefix.LOOPBACKS.containsIp(addr.getIp())) {
      return JUNIPER_CONNECTED_ROUTE_METADATA_LOOPBACKS;
    }
    return JUNIPER_CONNECTED_ROUTE_METADATA;
  }

  public static @Nonnull String computeFirewallFilterTermName(
      @Nonnull String filterName, @Nonnull String termName) {
    return String.format("%s %s", filterName, termName);
  }

  public static @Nonnull String computeSecurityPolicyTermName(
      @Nonnull String policyName, @Nonnull String termName) {
    return String.format("%s %s", policyName, termName);
  }

  public static @Nonnull String computePolicyStatementTermName(
      @Nonnull String policyStatementName, @Nonnull String termName) {
    return String.format("%s term %s", policyStatementName, termName);
  }

  @VisibleForTesting
  public static @Nonnull String computeConditionTrackName(@Nonnull String conditionName) {
    return String.format("~CONDITION~%s", conditionName);
  }

  @VisibleForTesting
  public static TraceElement matchingFirewallFilter(String filename, String filterName) {
    return TraceElement.builder()
        .add("Matched ")
        .add(
            String.format(
                "%s %s", JuniperStructureType.FIREWALL_FILTER.getDescription(), filterName),
            firewallFilterVendorStructureId(filename, filterName))
        .build();
  }

  /** Returns a trace element for a firewall filter term for the given test config. */
  @VisibleForTesting
  public static TraceElement matchingFirewallFilterTerm(
      String filename, String filterName, String termName) {
    return matchedTraceElement(
        termName, firewallFilterTermVendorStructureId(filename, filterName, termName));
  }

  private static VendorStructureId firewallFilterVendorStructureId(
      String filename, String filterName) {
    return new VendorStructureId(
        filename, JuniperStructureType.FIREWALL_FILTER.getDescription(), filterName);
  }

  public static TraceElement matchedTraceElement(String name, VendorStructureId vsId) {
    return TraceElement.builder().add("Matched ").add(name, vsId).build();
  }

  public static VendorStructureId firewallFilterTermVendorStructureId(
      String filename, String filterName, String termName) {
    return new VendorStructureId(
        filename,
        JuniperStructureType.FIREWALL_FILTER_TERM.getDescription(),
        computeFirewallFilterTermName(filterName, termName));
  }

  /** Returns a trace element for a security policy term for the given test config. */
  @VisibleForTesting
  public static TraceElement matchingSecurityPolicyTerm(
      String filename, String policyName, String termName) {
    return matchedTraceElement(
        termName, securityPolicyTermVendorStructureId(filename, policyName, termName));
  }

  public static VendorStructureId securityPolicyTermVendorStructureId(
      String filename, String policyName, String termName) {
    return new VendorStructureId(
        filename,
        JuniperStructureType.SECURITY_POLICY_TERM.getDescription(),
        computeSecurityPolicyTermName(policyName, termName));
  }

  private static TraceElement matchingAbstractTerm(
      JuniperStructureType aclType, String filename, String filterName, String termName) {
    return matchedTraceElement(
        termName, abstractTermVendorStructureId(aclType, filename, filterName, termName));
  }

  private static VendorStructureId abstractTermVendorStructureId(
      JuniperStructureType aclType, String filename, String filterName, String termName) {
    if (aclType == JuniperStructureType.FIREWALL_FILTER) {
      return firewallFilterTermVendorStructureId(filename, filterName, termName);
    }
    assert aclType == JuniperStructureType.SECURITY_POLICY;
    return securityPolicyTermVendorStructureId(filename, filterName, termName);
  }

  // See
  // https://www.juniper.net/documentation/en_US/junos/topics/reference/configuration-statement/hello-interval-edit-protocols-ospf.html
  static final int DEFAULT_NBMA_HELLO_INTERVAL = 30;

  static final int DEFAULT_HELLO_INTERVAL = 10;

  // Default dead interval is hello interval times 4
  static int OSPF_DEAD_INTERVAL_HELLO_MULTIPLIER = 4;

  // See
  // https://www.juniper.net/documentation/en_US/junos/topics/reference/configuration-statement/dead-interval-edit-protocols-ospf.html
  static final int DEFAULT_NBMA_DEAD_INTERVAL =
      OSPF_DEAD_INTERVAL_HELLO_MULTIPLIER * DEFAULT_NBMA_HELLO_INTERVAL;

  static final int DEFAULT_DEAD_INTERVAL =
      OSPF_DEAD_INTERVAL_HELLO_MULTIPLIER * DEFAULT_HELLO_INTERVAL;

  private static final BgpAuthenticationAlgorithm DEFAULT_BGP_AUTHENTICATION_ALGORITHM =
      BgpAuthenticationAlgorithm.HMAC_SHA_1_96;

  private static final String DEFAULT_BGP_EXPORT_POLICY_NAME = "~DEFAULT_BGP_EXPORT_POLICY~";

  private static final String DEFAULT_BGP_IMPORT_POLICY_NAME = "~DEFAULT_BGP_IMPORT_POLICY~";

  private static final String DEFAULT_PSEUDO_PROTOCOL_IMPORT_POLICY_NAME =
      "~DEFAULT_PSEUDO_PROTOCOL_IMPORT_POLICY~";

  private static final String DEFAULT_REJECT_POLICY_NAME = "~DEFAULT_REJECT_POLICY~";

  private static final Map<RoutingProtocol, String> DEFAULT_IMPORT_POLICIES =
      ImmutableMap.<RoutingProtocol, String>builder()
          .put(RoutingProtocol.CONNECTED, DEFAULT_PSEUDO_PROTOCOL_IMPORT_POLICY_NAME)
          .put(RoutingProtocol.LOCAL, DEFAULT_PSEUDO_PROTOCOL_IMPORT_POLICY_NAME)
          .put(RoutingProtocol.AGGREGATE, DEFAULT_PSEUDO_PROTOCOL_IMPORT_POLICY_NAME)
          .put(RoutingProtocol.STATIC, DEFAULT_PSEUDO_PROTOCOL_IMPORT_POLICY_NAME)
          .put(RoutingProtocol.BGP, DEFAULT_BGP_IMPORT_POLICY_NAME)
          .put(RoutingProtocol.IBGP, DEFAULT_BGP_IMPORT_POLICY_NAME)
          .build();

  private static final IntegerSpace ALL_VLANS = IntegerSpace.of(Range.closed(1, 4094));

  @VisibleForTesting public static final int DEFAULT_ISIS_COST = 10;

  /** Maximum IS-IS route cost if wide-metrics-only is not set */
  @VisibleForTesting static final int MAX_ISIS_COST_WITHOUT_WIDE_METRICS = 63;

  private static final String FIRST_LOOPBACK_INTERFACE_NAME = "lo0";

  Configuration _c;

  /** Map of policy name to routing instances referenced in the policy, in the order they appear */
  private transient Map<String, List<String>> _vrfReferencesInPolicies;

  private transient Set<String> _namedCommunitiesUsedForSet;

  private final Map<String, NodeDevice> _nodeDevices;

  private ConfigurationFormat _vendor;

  private Map<String, LogicalSystem> _logicalSystems;

  private LogicalSystem _masterLogicalSystem;

  private final Map<String, VlanReference> _indirectAccessPorts;

  public JuniperConfiguration() {
    _logicalSystems = new TreeMap<>();
    _masterLogicalSystem = new LogicalSystem("");
    _nodeDevices = new TreeMap<>();
    _indirectAccessPorts = new HashMap<>();
    // Create and then self-reference the default routing instance.
    defineSingleLineStructure(ROUTING_INSTANCE, DEFAULT_ROUTING_INSTANCE_NAME, 0);
    referenceStructure(
        ROUTING_INSTANCE,
        DEFAULT_ROUTING_INSTANCE_NAME,
        JuniperStructureUsage.ROUTING_INSTANCE_SELF_REFERENCE,
        0);
  }

  @Nonnull
  Set<String> getOrCreateNamedCommunitiesUsedForSet() {
    if (_namedCommunitiesUsedForSet == null) {
      _namedCommunitiesUsedForSet = new HashSet<>();
    }
    return _namedCommunitiesUsedForSet;
  }

  private NavigableMap<String, AuthenticationKeyChain> convertAuthenticationKeyChains(
      Map<String, JuniperAuthenticationKeyChain> juniperAuthenticationKeyChains) {
    NavigableMap<String, AuthenticationKeyChain> authenticationKeyChains = new TreeMap<>();
    for (Entry<String, JuniperAuthenticationKeyChain> keyChainEntry :
        juniperAuthenticationKeyChains.entrySet()) {
      JuniperAuthenticationKeyChain juniperAuthenticationKeyChain = keyChainEntry.getValue();
      AuthenticationKeyChain authenticationKeyChain =
          new AuthenticationKeyChain(juniperAuthenticationKeyChain.getName());
      authenticationKeyChain.setDescription(juniperAuthenticationKeyChain.getDescription());
      authenticationKeyChain.setTolerance(juniperAuthenticationKeyChain.getTolerance());
      for (Entry<String, JuniperAuthenticationKey> keyEntry :
          juniperAuthenticationKeyChain.getKeys().entrySet()) {
        JuniperAuthenticationKey juniperAuthenticationKey = keyEntry.getValue();
        AuthenticationKey authenticationKey =
            new AuthenticationKey(juniperAuthenticationKey.getName());
        authenticationKey.setIsisAuthenticationAlgorithm(
            juniperAuthenticationKey.getIsisAuthenticationAlgorithm());
        authenticationKey.setIsisOption(juniperAuthenticationKey.getIsisOption());
        authenticationKey.setSecret(juniperAuthenticationKey.getSecret());
        authenticationKey.setStartTime(juniperAuthenticationKey.getStartTime());
        authenticationKeyChain.getKeys().put(keyEntry.getKey(), authenticationKey);
      }
      authenticationKeyChains.put(keyChainEntry.getKey(), authenticationKeyChain);
    }
    return authenticationKeyChains;
  }

  private static @Nonnull org.batfish.datamodel.BgpProcess.Builder bgpProcessBuilder() {
    return org.batfish.datamodel.BgpProcess.builder()
        .setEbgpAdminCost(DEFAULT_BGP_ADMIN_DISTANCE)
        .setIbgpAdminCost(DEFAULT_BGP_ADMIN_DISTANCE)
        .setLocalAdminCost(DEFAULT_BGP_ADMIN_DISTANCE) /* local admin not relevant for JunOS. */
        // following most likely not relevant for JunOS.
        .setLocalOriginationTypeTieBreaker(NO_PREFERENCE)
        .setNetworkNextHopIpTieBreaker(HIGHEST_NEXT_HOP_IP)
        .setRedistributeNextHopIpTieBreaker(HIGHEST_NEXT_HOP_IP);
  }

  private @Nullable BgpProcess createBgpProcess(RoutingInstance routingInstance) {
    BgpGroup mg = routingInstance.getMasterBgpGroup();
    if (firstNonNull(mg.getDisable(), Boolean.FALSE)) {
      return null;
    }
    initDefaultBgpExportPolicy();
    initDefaultBgpImportPolicy();
    // On Junos, BGP routes only have one administrative distance.
    int bgpAdmin = firstNonNull(mg.getPreference(), DEFAULT_BGP_ADMIN_DISTANCE);
    BgpProcess proc =
        bgpProcessBuilder()
            .setRouterId(getRouterId(routingInstance))
            .setEbgpAdminCost(bgpAdmin)
            .setIbgpAdminCost(bgpAdmin)
            .build();

    // https://www.juniper.net/documentation/us/en/software/junos/bgp/topics/topic-map/basic-routing-policies.html#id-conditional-advertisement-and-import-policy-routing-table-with-certain-match-conditions
    // TODO: To avoid unnecessary route re-evaluation, only record conditions actually used by BGP
    //       export policies. In practice, though, conditions are unlikely to be defined unless they
    //       are used in some export policy.
    if (!_masterLogicalSystem.getConditions().isEmpty()) {
      proc.setTracks(
          _masterLogicalSystem.getConditions().keySet().stream()
              .map(JuniperConfiguration::computeConditionTrackName)
              .collect(ImmutableSet.toImmutableSet()));
    }

    boolean multipathEbgp = false;
    boolean multipathIbgp = false;
    boolean multipathMultipleAs = false;
    boolean multipathEbgpSet = false;
    boolean multipathIbgpSet = false;
    boolean multipathMultipleAsSet = false;

    if (mg.getLocalAs() == null) {
      Long routingInstanceAs = routingInstance.getAs();
      if (routingInstanceAs == null) {
        routingInstanceAs = _masterLogicalSystem.getDefaultRoutingInstance().getAs();
      }
      if (routingInstanceAs != null) {
        mg.setLocalAs(routingInstanceAs);
      }
    }

    // Global confederation config
    Long confederation = routingInstance.getConfederation();
    if (confederation != null && !routingInstance.getConfederationMembers().isEmpty()) {
      proc.setConfederation(
          new BgpConfederation(confederation, routingInstance.getConfederationMembers()));
    }

    // Set default authentication algorithm if missing
    if (mg.getAuthenticationAlgorithm() == null) {
      mg.setAuthenticationAlgorithm(DEFAULT_BGP_AUTHENTICATION_ALGORITHM);
    }
    for (IpBgpGroup ig : routingInstance.getIpBgpGroups().values()) {
      ig.cascadeInheritance();
    }

    /*
     * For new BGP advertisements, i.e. those that are created from non-BGP
     * routes, an origin code must be set. By default, Juniper sets the origin
     * code to IGP.
     */
    If setOriginForNonBgp =
        new If(
            new MatchProtocol(RoutingProtocol.BGP, RoutingProtocol.IBGP),
            ImmutableList.of(),
            ImmutableList.of(new SetOrigin(new LiteralOrigin(OriginType.IGP, null))));

    /*
     * Juniper allows setting BGP communities for static routes. Rather than add communities to VI
     * routes, add statements to peer export policies that set the appropriate communities.
     */
    List<If> staticRouteCommunitySetters = getStaticRouteCommunitySetters(routingInstance);

    for (Entry<Prefix, IpBgpGroup> e : routingInstance.getIpBgpGroups().entrySet()) {
      Prefix prefix = e.getKey();
      IpBgpGroup ig = e.getValue();
      Builder<?, ?> neighbor;
      Ipv4UnicastAddressFamily.Builder ipv4AfBuilder = Ipv4UnicastAddressFamily.builder();

      boolean ibgp;
      if (ig.getType() == BgpGroupType.EXTERNAL) {
        ibgp = false;
        if (ig.getPeerAs() != null && ig.getPeerAs().equals(ig.getLocalAs())) {
          _w.fatalRedFlag(
              "Error in neighbor %s of group %s. External peer's AS (%s) must not be the same as"
                  + " the local AS (%s).",
              prefix.getStartIp(), ig.getGroupName(), ig.getPeerAs(), ig.getLocalAs());
        }
      } else if (ig.getType() == BgpGroupType.INTERNAL) {
        ibgp = true;
        if (ig.getPeerAs() != null && !ig.getPeerAs().equals(ig.getLocalAs())) {
          _w.fatalRedFlag(
              "Error in neighbor %s of group %s. Internal peer's AS (%s) must be the same as local"
                  + " AS (%s).",
              prefix.getStartIp(), ig.getGroupName(), ig.getPeerAs(), ig.getLocalAs());
        }
      } else {
        if (ig.getPeerAs() == null) {
          // type is external by default unless a peer-as is defined
          ibgp = false;
        } else {
          ibgp = ig.getPeerAs().equals(ig.getLocalAs());
        }
      }

      if (!ibgp && ig.getPeerAs() == null) {
        _w.fatalRedFlag(
            "Error in neighbor %s of group %s. Peer AS number must be configured for an external"
                + " peer.",
            prefix.getStartIp(), ig.getGroupName());
      }

      Long remoteAs = ibgp ? ig.getLocalAs() : ig.getPeerAs();

      if (ig.getDynamic()) {
        neighbor =
            BgpPassivePeerConfig.builder()
                .setPeerPrefix(prefix)
                .setRemoteAsns(
                    Optional.ofNullable(remoteAs).map(LongSpace::of).orElse(ALL_AS_NUMBERS));
      } else {
        neighbor =
            BgpActivePeerConfig.builder()
                .setPeerAddress(prefix.getStartIp())
                .setRemoteAsns(
                    Optional.ofNullable(remoteAs).map(LongSpace::of).orElse(LongSpace.EMPTY));
      }
      neighbor.setDescription(ig.getDescription());

      // route reflection
      Ip declaredClusterId = ig.getClusterId();
      if (declaredClusterId != null) {
        ipv4AfBuilder.setRouteReflectorClient(true);
        neighbor.setClusterId(declaredClusterId.asLong());
      } else {
        neighbor.setClusterId(getRouterId(routingInstance).asLong());
      }

      neighbor.setConfederation(routingInstance.getConfederation());

      // multipath multiple-as
      if (!ibgp) {
        // Do not include iBGP peer [groups] in this computation: multiple-as only matters for
        // eBGP multipath (in iBGP, the next AS is always the same, and iBGP routes are always
        // worse than eBGP routes).
        //
        // As the iBGP setting does not matter, don't look for conflicts with it. We have seen
        // it set 'inconsistently' in real configs.
        boolean currentGroupMultipathMultipleAs = ig.getMultipathMultipleAs();
        if (multipathMultipleAsSet && currentGroupMultipathMultipleAs != multipathMultipleAs) {
          _w.redFlag(
              "Currently do not support mixed multipath-multiple-as/non-multipath-multiple-as bgp"
                  + "groups on Juniper - FORCING NON-MULTIPATH-MULTIPLE-AS");
          multipathMultipleAs = false;
        } else {
          multipathMultipleAs = currentGroupMultipathMultipleAs;
          multipathMultipleAsSet = true;
        }
      }

      String authenticationKeyChainName = ig.getAuthenticationKeyChainName();
      if (ig.getAuthenticationKeyChainName() != null) {
        if (!_c.getAuthenticationKeyChains().containsKey(authenticationKeyChainName)) {
          authenticationKeyChainName = null;
        } else if (ig.getAuthenticationKey() != null) {
          _w.redFlag(
              "Both authentication-key and authentication-key-chain specified for neighbor "
                  + ig.getRemoteAddress());
        }
      }
      BgpAuthenticationSettings bgpAuthenticationSettings = new BgpAuthenticationSettings();
      bgpAuthenticationSettings.setAuthenticationAlgorithm(ig.getAuthenticationAlgorithm());
      bgpAuthenticationSettings.setAuthenticationKey(ig.getAuthenticationKey());
      bgpAuthenticationSettings.setAuthenticationKeyChainName(authenticationKeyChainName);
      neighbor.setAuthenticationSettings(bgpAuthenticationSettings);
      Boolean ebgpMultihop = ig.getEbgpMultihop();
      if (ebgpMultihop == null) {
        ebgpMultihop = false;
      }
      neighbor.setEbgpMultihop(ebgpMultihop);
      neighbor.setEnforceFirstAs(firstNonNull(ig.getEnforceFirstAs(), Boolean.FALSE));

      // Check for loops in the following order:
      Integer loops =
          Stream.of(
                  ig.getLoops(),
                  routingInstance.getLoops(),
                  _masterLogicalSystem.getDefaultRoutingInstance().getLoops(),
                  0)
              .filter(Objects::nonNull)
              .findFirst()
              .get();

      boolean allowLocalAsIn = loops > 0;
      AddressFamilyCapabilities.Builder ipv4AfSettingsBuilder = AddressFamilyCapabilities.builder();

      // add-path
      AddPath addPath = ig.getAddPath();
      if (addPath != null) {
        ipv4AfSettingsBuilder.setAdditionalPathsReceive(addPath.getReceive());
        AddPathSend send = addPath.getSend();
        if (send != null) {
          if (send.getPathCount() != null) {
            // path count must be set for add-path send to be enabled on Juniper
            ipv4AfSettingsBuilder.setAdditionalPathsSend(true);
            // TODO: Datamodel property additionalPathsSelectAll needs to be split into at least:
            //       1. select all paths
            //       - use if Juniper path-selection-mode is ALL_PATHS, or neither
            // path-selection-mode
            //         nor multipath is set
            //       2. select all ECMP-best paths
            //       - use if Juniper path-selection-mode is EQUAL_COST_PATHS, or multipath is set
            ipv4AfSettingsBuilder.setAdditionalPathsSelectAll(true);
            // TODO: implement max additional-paths to send in datamodel and populate here
          } else {
            _w.redFlagf(
                "add-path send disabled because add-path send path-count not configured for"
                    + " neighbor %s",
                prefix);
          }
        }
      }
      ipv4AfSettingsBuilder.setAllowLocalAsIn(allowLocalAsIn);
      Boolean advertisePeerAs = ig.getAdvertisePeerAs();
      if (advertisePeerAs == null) {
        advertisePeerAs = false;
      }
      ipv4AfSettingsBuilder.setAllowRemoteAsOut(advertisePeerAs ? ALWAYS : EXCEPT_FIRST);
      Boolean advertiseExternal = ig.getAdvertiseExternal();
      if (advertiseExternal == null) {
        advertiseExternal = false;
      }
      ipv4AfSettingsBuilder.setAdvertiseExternal(advertiseExternal);
      Boolean advertiseInactive = ig.getAdvertiseInactive();
      if (advertiseInactive == null) {
        advertiseInactive = false;
      }
      ipv4AfSettingsBuilder.setAdvertiseInactive(advertiseInactive);
      neighbor.setGroup(ig.getGroupName());

      // import policies
      String peerImportPolicyName = computePeerImportPolicyName(ig.getRemoteAddress());
      ipv4AfBuilder.setImportPolicy(peerImportPolicyName);
      RoutingPolicy peerImportPolicy = new RoutingPolicy(peerImportPolicyName, _c);
      _c.getRoutingPolicies().put(peerImportPolicyName, peerImportPolicy);
      // default import policy is to accept
      peerImportPolicy.getStatements().add(new SetDefaultPolicy(DEFAULT_BGP_IMPORT_POLICY_NAME));
      peerImportPolicy.getStatements().add(Statements.SetDefaultActionAccept.toStaticStatement());
      if (ig.getPreference() != null && ig.getPreference() != bgpAdmin) {
        peerImportPolicy
            .getStatements()
            .add(new SetAdministrativeCost(new LiteralInt(ig.getPreference())));
      }
      List<BooleanExpr> importPolicyCalls = new ArrayList<>();
      ig.getImportPolicies()
          .forEach(
              importPolicyName -> {
                PolicyStatement importPolicy =
                    _masterLogicalSystem.getPolicyStatements().get(importPolicyName);
                if (importPolicy != null) {
                  CallExpr callPolicy = new CallExpr(importPolicyName);
                  importPolicyCalls.add(callPolicy);
                }
              });
      If peerImportPolicyConditional = new If();
      FirstMatchChain importPolicyChain = new FirstMatchChain(importPolicyCalls);
      peerImportPolicyConditional.setGuard(importPolicyChain);
      peerImportPolicy.getStatements().add(peerImportPolicyConditional);
      peerImportPolicyConditional
          .getTrueStatements()
          .add(Statements.ExitAccept.toStaticStatement());
      peerImportPolicyConditional
          .getFalseStatements()
          .add(Statements.ExitReject.toStaticStatement());

      // Apply rib groups
      if (ig.getRibGroup() != null) {
        neighbor.setAppliedRibGroup(
            toRibGroup(
                _masterLogicalSystem.getRibGroups().get(ig.getRibGroup()),
                ibgp ? RoutingProtocol.IBGP : RoutingProtocol.BGP,
                _c,
                routingInstance.getName(),
                _w));
      }

      // export policies
      String peerExportPolicyName = computePeerExportPolicyName(ig.getRemoteAddress());
      ipv4AfBuilder.setExportPolicy(peerExportPolicyName);
      RoutingPolicy peerExportPolicy = new RoutingPolicy(peerExportPolicyName, _c);
      _c.getRoutingPolicies().put(peerExportPolicyName, peerExportPolicy);
      peerExportPolicy.getStatements().add(new SetDefaultPolicy(DEFAULT_BGP_EXPORT_POLICY_NAME));
      applyLocalRoutePolicy(routingInstance, peerExportPolicy);

      // Add route modifier statements
      peerExportPolicy.getStatements().add(setOriginForNonBgp);
      peerExportPolicy.getStatements().addAll(staticRouteCommunitySetters);

      List<BooleanExpr> exportPolicyCalls = new ArrayList<>();
      ig.getExportPolicies()
          .forEach(
              exportPolicyName -> {
                PolicyStatement exportPolicy =
                    _masterLogicalSystem.getPolicyStatements().get(exportPolicyName);
                if (exportPolicy != null) {
                  CallExpr callPolicy = new CallExpr(exportPolicyName);
                  exportPolicyCalls.add(callPolicy);
                }
              });
      If peerExportPolicyConditional = new If();
      FirstMatchChain exportPolicyChain = new FirstMatchChain(exportPolicyCalls);
      peerExportPolicyConditional.setGuard(exportPolicyChain);
      peerExportPolicyConditional
          .getTrueStatements()
          .add(Statements.ExitAccept.toStaticStatement());
      peerExportPolicyConditional
          .getFalseStatements()
          .add(Statements.ExitReject.toStaticStatement());
      peerExportPolicy.getStatements().add(peerExportPolicyConditional);

      // inherit local-as
      neighbor.setLocalAs(ig.getLocalAs());
      if (ig.getLocalAs() == null) {
        _w.redFlag("Missing local-as for neighbor: " + ig.getRemoteAddress());
        continue;
      }

      // Warn if configured to prepend global-as, plus global-as and local-as both exist
      boolean prependGlobalAs = !ibgp && !firstNonNull(ig.getNoPrependGlobalAs(), Boolean.FALSE);
      if (prependGlobalAs
          && mg.getLocalAs() != null
          && mg.getLocalAs().longValue() != ig.getLocalAs().longValue()) {
        _w.redFlag("Unimplemented: prepending both local-as and global-as for BGP routes");
      }

      /* Inherit multipath */
      if (ibgp) {
        boolean currentGroupMultipathIbgp = ig.getMultipath();
        if (multipathIbgpSet && currentGroupMultipathIbgp != multipathIbgp) {
          _w.redFlag(
              "Currently do not support mixed iBGP multipath/non-multipath bgp groups on Juniper "
                  + "- FORCING NON-MULTIPATH IBGP");
          multipathIbgp = false;
        } else {
          multipathIbgp = currentGroupMultipathIbgp;
          multipathIbgpSet = true;
        }
      } else {
        boolean currentGroupMultipathEbgp = ig.getMultipath();
        if (multipathEbgpSet && currentGroupMultipathEbgp != multipathEbgp) {
          _w.redFlag(
              "Currently do not support mixed eBGP multipath/non-multipath bgp groups on Juniper "
                  + "- FORCING NON-MULTIPATH EBGP");
          multipathEbgp = false;
        } else {
          multipathEbgp = currentGroupMultipathEbgp;
          multipathEbgpSet = true;
        }
      }

      // TODO: implement better behavior than setting default metric to 0
      neighbor.setDefaultMetric(0);

      // TODO: find out if there is a juniper equivalent of cisco
      // send-community
      ipv4AfSettingsBuilder.setSendCommunity(true).setSendExtendedCommunity(true);

      // inherit update-source
      neighbor.setLocalIp(ig.getLocalAddress());
      neighbor.setBgpProcess(proc);
      neighbor.setIpv4UnicastAddressFamily(
          ipv4AfBuilder.setAddressFamilyCapabilities(ipv4AfSettingsBuilder.build()).build());
      neighbor.build();
    }
    proc.setMultipathEbgp(multipathEbgp);
    proc.setMultipathIbgp(multipathIbgp);
    MultipathEquivalentAsPathMatchMode multipathEquivalentAsPathMatchMode =
        multipathMultipleAs
            ? MultipathEquivalentAsPathMatchMode.PATH_LENGTH
            : MultipathEquivalentAsPathMatchMode.FIRST_AS;
    proc.setMultipathEquivalentAsPathMatchMode(multipathEquivalentAsPathMatchMode);

    return proc;
  }

  /**
   * Returns the source address used for outgoing packets.
   *
   * <p>Assumes that VI interfaces have been created in {@code c}.
   */
  // Docs:
  // https://www.juniper.net/documentation/us/en/software/junos/transport-ip/topics/ref/statement/default-address-selection-edit-system.html
  // 1. Use lo0 interfaces (in the same VRF) if the address is NOT 127.0.0.1
  // 2. Use primary interface
  @VisibleForTesting
  static @Nonnull Optional<Ip> getDefaultSourceAddress(
      RoutingInstance routingInstance, Configuration c) {
    Optional<Interface> loopback =
        routingInstance.getInterfaces().values().stream()
            .filter(
                iface ->
                    iface.isUnit()
                        && c.getAllInterfaces().get(iface.getName()).getActive()
                        && iface.getName().startsWith(FIRST_LOOPBACK_INTERFACE_NAME))
            // should be at most one: Junos has only lo0 (no lo1) and only one unit in a
            // routing instance
            .findAny();

    if (loopback.isPresent()) {
      Optional<Ip> primaryAddress = getDefaultSourceAddress(loopback.get());
      if (primaryAddress.isPresent()) {
        return primaryAddress;
      }
    }

    Optional<Interface> primaryInterface = getPrimaryInterface(routingInstance, c);
    if (primaryInterface.isPresent()) {
      Optional<Ip> primaryAddress = getDefaultSourceAddress(primaryInterface.get());
      if (primaryAddress.isPresent()) {
        return primaryAddress;
      }
    }
    return Optional.empty();
  }

  /**
   * Returns the primary interface of this routing instance.
   *
   * <p>Assumes that VI interfaces have been created in {@code c}.
   */
  @VisibleForTesting
  static @Nonnull Optional<Interface> getPrimaryInterface(
      RoutingInstance routingInstance, Configuration c) {
    Optional<Interface> explicitPrimary =
        routingInstance.getInterfaces().values().stream()
            .filter(
                iface ->
                    iface.isUnit()
                        && firstNonNull(iface.getPrimary(), false)
                        && c.getAllInterfaces().get(iface.getName()).getActive())
            .min(
                Comparator.comparing(
                    Interface::getName)); // there should be only one; sorting just in case
    if (explicitPrimary.isPresent()) {
      return explicitPrimary;
    }

    // In the absence an explicit primary, Junos method for picking the primary relies on unmodeled
    // properties (and rules aren't clear either). Doc:
    // https://www.juniper.net/documentation/us/en/software/junos/interfaces-fundamentals/topics/ref/statement/primary-edit-interfaces-family.html
    // By default, the multicast-capable interface with the lowest-index address is chosen as the
    // primary interface. If there is no such interface, the point-to-point interface with the
    // lowest-index address is chosen. Otherwise, any interface with an address can be picked. In
    // practice, this means that, on the device, the fxp0 or em0 interface is picked by default.

    // We first try mgmt interfaces (fxp, em), then rest
    // Lexicographical sorting should suffice
    Optional<Interface> mgmtInterfaceWithAddress =
        routingInstance.getInterfaces().values().stream()
            .filter(
                iface ->
                    iface.getType() == InterfaceType.MANAGEMENT_UNIT
                        && c.getAllInterfaces().get(iface.getName()).getActive()
                        && !iface.getAllAddresses().isEmpty())
            .min(Comparator.comparing(Interface::getName));
    if (mgmtInterfaceWithAddress.isPresent()) {
      return mgmtInterfaceWithAddress;
    }

    return routingInstance.getInterfaces().values().stream()
        .filter(
            iface ->
                iface.isUnit()
                    && c.getAllInterfaces().get(iface.getName()).getActive()
                    && !iface.getAllAddresses().isEmpty())
        .min(Comparator.comparing(Interface::getName));
  }

  /** Returns the default source address for this interface. */
  // Docs:
  // https://www.juniper.net/documentation/us/en/software/junos/interfaces-ethernet-switches/topics/ref/statement/primary-edit-interfaces.html
  // The primary address may be explicitly configured. If not, pick the lowest
  // preferred address. Else, pick the lowest address. In each case, 127.0.0.1 is ignored.
  @VisibleForTesting
  static @Nonnull Optional<Ip> getDefaultSourceAddress(Interface iface) {
    Ip ignoredIp = Ip.parse("127.0.0.1");

    Optional<Ip> explicitPrimary =
        Optional.ofNullable(iface.getPrimaryAddress())
            .map(ConcreteInterfaceAddress::getIp)
            .filter(ip -> !ip.equals(ignoredIp));
    if (explicitPrimary.isPresent()) {
      return explicitPrimary;
    }

    Optional<Ip> preferred =
        Optional.ofNullable(iface.getPreferredAddress())
            .filter(addr -> addr instanceof ConcreteInterfaceAddress)
            .map(addr -> ((ConcreteInterfaceAddress) addr).getIp())
            .filter(ip -> !ip.equals(ignoredIp));
    if (preferred.isPresent()) {
      return preferred;
    }

    return iface.getAllAddresses().stream()
        .map(ConcreteInterfaceAddress::getIp)
        .filter(ip -> !ip.equals(ignoredIp))
        .sorted()
        .findFirst();
  }

  /**
   * For each static route in the given {@link RoutingInstance} that has at least one community set,
   * creates an {@link If} that matches that route (specifically, matches static routes with that
   * route's destination network), and sets communities for matching exported routes.
   */
  private static @Nonnull List<If> getStaticRouteCommunitySetters(@Nonnull RoutingInstance ri) {
    MatchProtocol matchStatic = new MatchProtocol(RoutingProtocol.STATIC);
    return ri
        .getRibs()
        .get(RoutingInformationBase.RIB_IPV4_UNICAST)
        .getStaticRoutes()
        .values()
        .stream()
        .filter(route -> !route.getCommunities().isEmpty())
        .map(
            route -> {
              // Create matcher that matches routes that share this route's destination network
              PrefixExpr destNetworkMatcher = DestinationNetwork.instance();
              PrefixSetExpr destNetwork =
                  new ExplicitPrefixSet(new PrefixSpace(PrefixRange.fromPrefix(route.getPrefix())));
              MatchPrefixSet networkMatcher = new MatchPrefixSet(destNetworkMatcher, destNetwork);

              // When a matching static route is exported, set its communities
              return new If(
                  new Conjunction(ImmutableList.of(matchStatic, networkMatcher)),
                  ImmutableList.of(
                      new SetCommunities(
                          new LiteralCommunitySet(CommunitySet.of(route.getCommunities())))));
            })
        .collect(ImmutableList.toImmutableList());
  }

  public static String computePeerExportPolicyName(Prefix remoteAddress) {
    return "~PEER_EXPORT_POLICY:" + remoteAddress + "~";
  }

  public static String computePeerImportPolicyName(Prefix remoteAddress) {
    return "~PEER_IMPORT_POLICY:" + remoteAddress + "~";
  }

  private void convertNamedCommunities() {
    /*
     * Each NamedCommunity is converted into three structures for different usages:
     * - CommunitySet for setting
     * - CommunityMatchExpr for deleting
     * - CommunitySetMatchExpr for matching
     */
    _masterLogicalSystem
        .getNamedCommunities()
        .forEach(
            (name, namedCommunity) -> {
              assert name.equals(namedCommunity.getName());
              @Nullable CommunitySet communitySet = toCommunitySet(namedCommunity);
              if (communitySet != null) {
                _c.getCommunitySets().put(name, communitySet);
              }
              _c.getCommunityMatchExprs().put(name, toCommunityMatchExpr(namedCommunity));
              _c.getCommunitySetMatchExprs().put(name, toCommunitySetMatchExpr(namedCommunity));
            });
  }

  private static class CommunityMemberToCommunity implements CommunityMemberVisitor<Community> {
    @Override
    public Community visitLiteralCommunityMember(LiteralCommunityMember literalCommunityMember) {
      return literalCommunityMember.getCommunity();
    }

    @Override
    public Community visitRegexCommunityMember(RegexCommunityMember regexCommunityMember) {
      return null;
    }

    private static final CommunityMemberToCommunity INSTANCE = new CommunityMemberToCommunity();
  }

  /**
   * Returns a {@link CommunitySet} containing each {@link LiteralCommunityMember} of {@code
   * namedCommunity}, or {@code null} if {@code namedCommunity} doesn't contain any.
   *
   * <p>Note that the value of {@link NamedCommunity#getInvertMatch()} does not affect behavior when
   * using a {@link NamedCommunity} as a {@link CommunitySet}.
   */
  private static @Nullable CommunitySet toCommunitySet(NamedCommunity namedCommunity) {
    Set<Community> communities =
        namedCommunity.getMembers().stream()
            .map(member -> member.accept(CommunityMemberToCommunity.INSTANCE))
            .filter(Objects::nonNull)
            .collect(ImmutableSet.toImmutableSet());
    if (communities.isEmpty()) {
      return null;
    }
    return CommunitySet.of(communities);
  }

  private static class CommunityMemberToCommunityMatchExpr
      implements CommunityMemberVisitor<CommunityMatchExpr> {
    @Override
    public CommunityMatchExpr visitLiteralCommunityMember(
        LiteralCommunityMember literalCommunityMember) {
      return new CommunityIs(literalCommunityMember.getCommunity());
    }

    @Override
    public CommunityMatchExpr visitRegexCommunityMember(RegexCommunityMember regexCommunityMember) {
      // TODO: verify regex semantics and rendering
      return new CommunityMatchRegex(
          ColonSeparatedRendering.instance(), regexCommunityMember.getJavaRegex());
    }

    private static final CommunityMemberToCommunityMatchExpr INSTANCE =
        new CommunityMemberToCommunityMatchExpr();
  }

  /**
   * Returns a {@link CommunityMatchExpr} that matches an individual {@link Community} if it is
   * matched by any {@link CommunityMember} of {@code namedCommunity}.
   */
  private static @Nonnull CommunityMatchExpr toCommunityMatchExpr(NamedCommunity namedCommunity) {
    CommunityMatchExpr match =
        CommunityMatchAny.matchAny(
            namedCommunity.getMembers().stream()
                .map(member -> member.accept(CommunityMemberToCommunityMatchExpr.INSTANCE))
                .collect(ImmutableSet.toImmutableSet()));
    if (namedCommunity.getInvertMatch()) {
      return CommunityNot.not(match);
    }
    return match;
  }

  /**
   * Returns a {@link CommunitySetMatchExpr} that matches a route's {@link CommunitySet} if every
   * {@link CommunityMember} of {@code namedCommunity} matches at least one {@link Community} of the
   * {@link CommunitySet}.
   */
  private static @Nonnull CommunitySetMatchExpr toCommunitySetMatchExpr(
      NamedCommunity namedCommunity) {
    CommunitySetMatchExpr match =
        CommunitySetMatchAll.matchAll(
            namedCommunity.getMembers().stream()
                .map(member -> member.accept(CommunityMemberToCommunityMatchExpr.INSTANCE))
                .map(HasCommunity::new)
                .collect(ImmutableSet.toImmutableSet()));
    if (namedCommunity.getInvertMatch()) {
      return CommunitySetNot.not(match);
    }
    return match;
  }

  private void applyLocalRoutePolicy(RoutingInstance routingInstance, RoutingPolicy targetPolicy) {
    boolean lan = routingInstance.getExportLocalRoutesLan();
    boolean ptp = routingInstance.getExportLocalRoutesPointToPoint();
    if (lan && ptp) {
      // All local routes are allowed, so no need for filter
      return;
    }
    BooleanExpr matchProtocol = new MatchProtocol(RoutingProtocol.LOCAL);
    BooleanExpr match;
    if (!lan && !ptp) {
      // No need to check length, since all local routes will be rejected
      match = matchProtocol;
    } else {
      SubRange rejectedLength =
          !lan
              ? new SubRange(0, Prefix.MAX_PREFIX_LENGTH - 2)
              : SubRange.singleton(Prefix.MAX_PREFIX_LENGTH - 1);
      match =
          new Conjunction(
              ImmutableList.of(
                  matchProtocol, new MatchLocalRouteSourcePrefixLength(rejectedLength)));
    }
    targetPolicy
        .getStatements()
        .add(new If(match, ImmutableList.of(Statements.ExitReject.toStaticStatement())));
  }

  private IsisProcess createIsisProcess(RoutingInstance routingInstance, IsoAddress netAddress) {
    IsisProcess.Builder newProc = IsisProcess.builder();
    newProc.setNetAddress(netAddress);
    IsisSettings settings = _masterLogicalSystem.getDefaultRoutingInstance().getIsisSettings();
    if (!settings.getExportPolicies().isEmpty()) {
      // Process has export policies for redistribution. Create process export policy
      List<Statement> statements = new ArrayList<>();

      // If a route falls through all the specified export policies, it should be rejected.
      // FirstMatchChain applies environment's default policy in this scenario, so set it to reject.
      initDefaultRejectPolicy();
      statements.add(new SetDefaultPolicy(DEFAULT_REJECT_POLICY_NAME));

      // Apply the specified export policies
      List<BooleanExpr> callExprs =
          settings.getExportPolicies().stream()
              .map(
                  calledPolicyName -> {
                    PolicyStatement importPolicy =
                        _masterLogicalSystem.getPolicyStatements().get(calledPolicyName);
                    if (importPolicy != null) {
                      return new CallExpr(calledPolicyName);
                    }
                    return null;
                  })
              .filter(Objects::nonNull)
              .collect(ImmutableList.toImmutableList());
      statements.add(
          new If(
              new FirstMatchChain(callExprs),
              ImmutableList.of(Statements.ReturnTrue.toStaticStatement())));

      String exportPolicyName = computeIsisExportPolicyName(routingInstance.getName());
      newProc.setExportPolicy(exportPolicyName);
      RoutingPolicy.builder()
          .setOwner(_c)
          .setName(exportPolicyName)
          .setStatements(statements)
          .build();
    }
    boolean level1 = settings.getLevel1Settings().getEnabled();
    boolean level2 = settings.getLevel2Settings().getEnabled();
    if (!level1 && !level2) {
      return null;
    }
    if (level1) {
      newProc.setLevel1(toIsisLevelSettings(settings.getLevel1Settings()));
    }
    if (level2) {
      newProc.setLevel2(toIsisLevelSettings(settings.getLevel2Settings()));
    }

    // Process interface settings. Enabled levels in the IS-IS process should be limited to union of
    // enabled levels on non-loopback interfaces.
    IsisLevel allEnabledLevels = processIsisInterfaceSettings(routingInstance, level1, level2);
    if (allEnabledLevels == null || !allEnabledLevels.includes(IsisLevel.LEVEL_1)) {
      newProc.setLevel1(null);
    }
    if (allEnabledLevels == null || !allEnabledLevels.includes(IsisLevel.LEVEL_2)) {
      newProc.setLevel2(null);
    }

    // If overload is set with a timeout, just pretend overload isn't set at all
    if (settings.getOverload() && settings.getOverloadTimeout() == null) {
      newProc.setOverload(true);
    }
    newProc.setReferenceBandwidth(settings.getReferenceBandwidth());
    return newProc.build();
  }

  /** Returns union of IS-IS levels enabled on non-loopback interfaces in {@code routingInstance} */
  @VisibleForTesting
  IsisLevel processIsisInterfaceSettings(
      RoutingInstance routingInstance, boolean level1, boolean level2) {
    return _c.getAllInterfaces(routingInstance.getName()).entrySet().stream()
        .map(
            e -> {
              String ifaceName = e.getKey();
              org.batfish.datamodel.Interface newIface = e.getValue();
              org.batfish.datamodel.isis.IsisInterfaceSettings settings =
                  toIsisInterfaceSettings(
                      routingInstance.getIsisSettings(),
                      routingInstance.getInterfaces().get(ifaceName),
                      level1,
                      level2,
                      newIface.isLoopback());
              newIface.setIsis(settings);
              if (settings == null || newIface.isLoopback()) {
                return null;
              }
              return settings.getEnabledLevels();
            })
        .filter(Objects::nonNull)
        .reduce(IsisLevel::union) // will not return null as long as parameters are not null
        .orElse(null);
  }

  private org.batfish.datamodel.isis.IsisInterfaceSettings toIsisInterfaceSettings(
      @Nonnull IsisSettings settings,
      Interface iface,
      boolean level1,
      boolean level2,
      boolean isLoopback) {
    IsisInterfaceSettings interfaceSettings = iface.getEffectiveIsisSettings();
    if (interfaceSettings == null || !interfaceSettings.getEnabled()) {
      return null;
    }
    // If a reference bandwidth is set, calculate default cost as (reference bandwidth) / (interface
    // bandwidth). This will get overridden later if IS-IS level settings have cost set explicitly.
    long defaultCost = DEFAULT_ISIS_COST;
    if (settings.getReferenceBandwidth() != null) {
      if (iface.getBandwidth() == 0) {
        _w.pedantic(
            String.format(
                "Cannot use IS-IS reference bandwidth for interface '%s' because interface"
                    + " bandwidth is 0.",
                iface.getName()));
      } else {
        defaultCost = Math.max((long) (settings.getReferenceBandwidth() / iface.getBandwidth()), 1);
      }
    }
    org.batfish.datamodel.isis.IsisInterfaceSettings.Builder newInterfaceSettingsBuilder =
        org.batfish.datamodel.isis.IsisInterfaceSettings.builder();
    if (level1) {
      newInterfaceSettingsBuilder.setLevel1(
          toIsisInterfaceLevelSettings(
              settings.getLevel1Settings(),
              interfaceSettings,
              interfaceSettings.getLevel1Settings(),
              defaultCost,
              isLoopback));
    }
    if (level2) {
      newInterfaceSettingsBuilder.setLevel2(
          toIsisInterfaceLevelSettings(
              settings.getLevel2Settings(),
              interfaceSettings,
              interfaceSettings.getLevel2Settings(),
              defaultCost,
              isLoopback));
    }
    return newInterfaceSettingsBuilder
        .setBfdLivenessDetectionMinimumInterval(
            interfaceSettings.getBfdLivenessDetectionMinimumInterval())
        .setBfdLivenessDetectionMultiplier(interfaceSettings.getBfdLivenessDetectionMultiplier())
        .setIsoAddress(iface.getIsoAddress())
        .setPointToPoint(interfaceSettings.getPointToPoint())
        .build();
  }

  private @Nullable org.batfish.datamodel.isis.IsisInterfaceLevelSettings
      toIsisInterfaceLevelSettings(
          IsisLevelSettings levelSettings,
          IsisInterfaceSettings interfaceSettings,
          IsisInterfaceLevelSettings interfaceLevelSettings,
          long defaultCost,
          boolean isLoopback) {
    // Process and interface settings have already been checked to ensure IS-IS is enabled on iface
    if (!interfaceLevelSettings.getEnabled()) {
      return null;
    }
    long cost = firstNonNull(interfaceLevelSettings.getMetric(), defaultCost);
    if (!levelSettings.getWideMetricsOnly()) {
      cost = Math.min(cost, MAX_ISIS_COST_WITHOUT_WIDE_METRICS);
    }
    return org.batfish.datamodel.isis.IsisInterfaceLevelSettings.builder()
        .setCost(cost)
        .setHelloAuthenticationKey(interfaceLevelSettings.getHelloAuthenticationKey())
        .setHelloAuthenticationType(interfaceLevelSettings.getHelloAuthenticationType())
        .setHelloInterval(interfaceLevelSettings.getHelloInterval())
        .setHoldTime(interfaceLevelSettings.getHoldTime())
        .setMode(
            // Loopbacks are always passive regardless of whether it is explicitly configured
            isLoopback || interfaceSettings.getPassive() || interfaceLevelSettings.getPassive()
                ? IsisInterfaceMode.PASSIVE
                : IsisInterfaceMode.ACTIVE)
        .build();
  }

  private org.batfish.datamodel.isis.IsisLevelSettings toIsisLevelSettings(
      IsisLevelSettings levelSettings) {
    return org.batfish.datamodel.isis.IsisLevelSettings.builder()
        .setWideMetricsOnly(levelSettings.getWideMetricsOnly())
        .build();
  }

  private @Nullable OspfProcess createOspfProcess(RoutingInstance routingInstance) {
    if (firstNonNull(routingInstance.getOspfDisable(), Boolean.FALSE)) {
      return null;
    }
    Ip ospfRouterId = getRouterId(routingInstance);
    OspfProcess newProc =
        OspfProcess.builder()
            // Use routing instance name since OSPF processes are not named
            .setProcessId(routingInstance.getName())
            .setReferenceBandwidth(routingInstance.getOspfReferenceBandwidth())
            .setAdminCosts(
                org.batfish.datamodel.ospf.OspfProcess.computeDefaultAdminCosts(
                    _c.getConfigurationFormat()))
            .setSummaryAdminCost(
                RoutingProtocol.OSPF_IA.getSummaryAdministrativeCost(_c.getConfigurationFormat()))
            .setRouterId(ospfRouterId)
            .setSummaryDiscardMetric(OSPF_INTERNAL_SUMMARY_DISCARD_METRIC)
            .build();
    String vrfName = routingInstance.getName();
    // export policies
    String ospfExportPolicyName = computeOspfExportPolicyName(vrfName);
    RoutingPolicy ospfExportPolicy = new RoutingPolicy(ospfExportPolicyName, _c);
    applyLocalRoutePolicy(routingInstance, ospfExportPolicy);
    _c.getRoutingPolicies().put(ospfExportPolicyName, ospfExportPolicy);
    newProc.setExportPolicy(ospfExportPolicyName);
    If ospfExportPolicyConditional = new If();
    // TODO: set default metric-type for special cases based on ospf process
    // settings
    ospfExportPolicy.getStatements().add(new SetOspfMetricType(OspfMetricType.E2));
    ospfExportPolicy.getStatements().add(ospfExportPolicyConditional);
    Disjunction matchSomeExportPolicy = new Disjunction();
    ospfExportPolicyConditional.setGuard(matchSomeExportPolicy);
    ospfExportPolicyConditional.getTrueStatements().add(Statements.ExitAccept.toStaticStatement());
    ospfExportPolicyConditional.getFalseStatements().add(Statements.ExitReject.toStaticStatement());
    routingInstance
        .getOspfExportPolicies()
        .forEach(
            exportPolicyName -> {
              PolicyStatement exportPolicy =
                  _masterLogicalSystem.getPolicyStatements().get(exportPolicyName);
              if (exportPolicy != null) {
                CallExpr callPolicy = new CallExpr(exportPolicyName);
                matchSomeExportPolicy.getDisjuncts().add(callPolicy);
              }
            });
    // areas
    Map<Long, org.batfish.datamodel.ospf.OspfArea.Builder> newAreaBuilders =
        CollectionUtil.toImmutableMap(
            routingInstance.getOspfAreas(),
            Entry::getKey,
            e -> {
              String summaryFilterName =
                  "~OSPF_SUMMARY_FILTER:" + vrfName + ":" + e.getValue().getName() + "~";
              RouteFilterList summaryFilter = new RouteFilterList(summaryFilterName);
              _c.getRouteFilterLists().put(summaryFilterName, summaryFilter);
              return toOspfAreaBuilder(e.getValue(), summaryFilter);
            });
    // place interfaces into areas
    for (Entry<String, Interface> e : routingInstance.getInterfaces().entrySet()) {
      String name = e.getKey();
      Interface iface = e.getValue();
      placeInterfaceIntoArea(newAreaBuilders, name, iface, vrfName);
    }

    // Build areas
    newProc.setAreas(
        newAreaBuilders.entrySet().stream()
            .collect(
                ImmutableSortedMap.toImmutableSortedMap(
                    Comparator.naturalOrder(), Entry::getKey, entry -> entry.getValue().build())));

    // update interface OSPF properties (including pointers to their parent areas and process)
    newProc
        .getAreas()
        .values()
        .forEach(
            area ->
                area.getInterfaces()
                    .forEach(
                        ifaceName -> {
                          org.batfish.datamodel.Interface iface =
                              _c.getAllInterfaces(vrfName).get(ifaceName);
                          Interface vsIface = routingInstance.getInterfaces().get(ifaceName);
                          finalizeOspfInterfaceSettings(
                              iface, vsIface, newProc, area.getAreaNumber());
                        }));
    return newProc;
  }

  /**
   * Update VI interface OSPF properties based on the specified VS interface, optional OSPF process,
   * and optional OSPF area number
   */
  private void finalizeOspfInterfaceSettings(
      org.batfish.datamodel.Interface iface,
      Interface vsIface,
      @Nullable OspfProcess proc,
      @Nullable Long areaNum) {
    org.batfish.datamodel.ospf.OspfInterfaceSettings.Builder ospfSettings =
        org.batfish.datamodel.ospf.OspfInterfaceSettings.builder();
    OspfInterfaceSettings vsOspfSettings = vsIface.getEffectiveOspfSettings();
    if (vsOspfSettings == null) {
      return;
    }
    ospfSettings.setEnabled(!firstNonNull(vsOspfSettings.getOspfDisable(), Boolean.FALSE));
    ospfSettings.setPassive(vsOspfSettings.getOspfPassive());
    Integer ospfCost = vsOspfSettings.getOspfCost();
    if (ospfCost == null && iface.isLoopback()) {
      ospfCost = 0;
    }
    ospfSettings.setCost(ospfCost);
    ospfSettings.setAreaName(areaNum);
    if (proc != null) {
      ospfSettings.setProcess(proc.getProcessId());
    }
    ospfSettings.setDeadInterval(toOspfDeadInterval(vsOspfSettings));
    ospfSettings.setHelloInterval(toOspfHelloInterval(vsOspfSettings));
    // TODO infer interface type based on physical interface: "the software
    // chooses the correct
    // interface type...you should never have to set the interface type" (see
    // https://www.juniper.net/documentation/en_US/junos/topics/reference/configuration-statement/interface-type-edit-protocols-ospf.html)
    ospfSettings.setNetworkType(toOspfNetworkType(vsOspfSettings.getOspfInterfaceTypeOrDefault()));

    if (vsOspfSettings.getOspfInterfaceTypeOrDefault() == OspfInterfaceType.NBMA) {
      // neighbors only for NBMA mode:
      // https://www.juniper.net/documentation/en_US/junos/topics/reference/configuration-statement/neighbor-edit-protocols-ospf.html
      ospfSettings.setNbmaNeighbors(
          vsOspfSettings.getOspfNeighbors().stream()
              .map(InterfaceOspfNeighbor::getIp)
              .collect(ImmutableSet.toImmutableSet()));
    }

    iface.setOspfSettings(ospfSettings.build());
  }

  /**
   * Helper to infer dead interval from configured OSPF settings on an interface. Check explicitly
   * set dead interval, infer from hello interval, or infer from OSPF interface type, in that order.
   * See
   * https://www.juniper.net/documentation/en_US/junos/topics/reference/configuration-statement/dead-interval-edit-protocols-ospf.html
   * for more details.
   */
  @VisibleForTesting
  static int toOspfDeadInterval(OspfInterfaceSettings vsOspfSettings) {
    Integer deadInterval = vsOspfSettings.getOspfDeadInterval();
    if (deadInterval != null) {
      return deadInterval;
    }
    Integer helloInterval = vsOspfSettings.getOspfHelloInterval();
    if (helloInterval != null) {
      return OSPF_DEAD_INTERVAL_HELLO_MULTIPLIER * helloInterval;
    }
    if (vsOspfSettings.getOspfInterfaceTypeOrDefault() == OspfInterfaceType.NBMA) {
      return DEFAULT_NBMA_DEAD_INTERVAL;
    }
    return DEFAULT_DEAD_INTERVAL;
  }

  /**
   * Helper to infer hello interval from configured OSPF settings on an interface. Check explicitly
   * set hello interval or infer from OSPF interface type, in that order. See
   * https://www.juniper.net/documentation/en_US/junos/topics/reference/configuration-statement/hello-interval-edit-protocols-ospf.html
   * for more details.
   */
  @VisibleForTesting
  static int toOspfHelloInterval(OspfInterfaceSettings vsOspfSettings) {
    Integer helloInterval = vsOspfSettings.getOspfHelloInterval();
    if (helloInterval != null) {
      return helloInterval;
    }
    if (vsOspfSettings.getOspfInterfaceTypeOrDefault() == OspfInterfaceType.NBMA) {
      return DEFAULT_NBMA_HELLO_INTERVAL;
    }
    return DEFAULT_HELLO_INTERVAL;
  }

  private org.batfish.datamodel.ospf.OspfArea.Builder toOspfAreaBuilder(
      OspfArea area, RouteFilterList summaryFilter) {
    org.batfish.datamodel.ospf.OspfArea.Builder newAreaBuilder =
        org.batfish.datamodel.ospf.OspfArea.builder();
    newAreaBuilder.setNumber(area.getName());
    newAreaBuilder.setNssaSettings(toNssaSettings(area.getNssaSettings()));
    newAreaBuilder.setStubSettings(toStubSettings(area.getStubSettings()));
    newAreaBuilder.setStubType(area.getStubType());
    newAreaBuilder.addSummaries(area.getSummaries());
    newAreaBuilder.setInjectDefaultRoute(area.getInjectDefaultRoute());
    newAreaBuilder.setMetricOfDefaultRoute(area.getMetricOfDefaultRoute());

    // Add summary filters for each area summary
    for (Entry<Prefix, OspfAreaSummary> e2 : area.getSummaries().entrySet()) {
      Prefix prefix = e2.getKey();
      OspfAreaSummary summary = e2.getValue();
      int prefixLength = prefix.getPrefixLength();
      int filterMinPrefixLength =
          summary.isAdvertised()
              ? Math.min(Prefix.MAX_PREFIX_LENGTH, prefixLength + 1)
              : prefixLength;
      summaryFilter.addLine(
          new org.batfish.datamodel.RouteFilterLine(
              LineAction.DENY,
              IpWildcard.create(prefix),
              new SubRange(filterMinPrefixLength, Prefix.MAX_PREFIX_LENGTH)));
    }
    summaryFilter.addLine(
        new org.batfish.datamodel.RouteFilterLine(
            LineAction.PERMIT,
            IpWildcard.create(Prefix.ZERO),
            new SubRange(0, Prefix.MAX_PREFIX_LENGTH)));
    newAreaBuilder.setSummaryFilter(summaryFilter.getName());
    return newAreaBuilder;
  }

  private org.batfish.datamodel.ospf.NssaSettings toNssaSettings(NssaSettings nssaSettings) {
    if (nssaSettings == null) {
      return null;
    }
    return org.batfish.datamodel.ospf.NssaSettings.builder()
        .setDefaultOriginateType(nssaSettings.getDefaultLsaType())
        .setSuppressType3(nssaSettings.getNoSummaries())
        .build();
  }

  private org.batfish.datamodel.ospf.StubSettings toStubSettings(StubSettings stubSettings) {
    if (stubSettings == null) {
      return null;
    }
    return org.batfish.datamodel.ospf.StubSettings.builder()
        .setSuppressType3(stubSettings.getNoSummaries())
        .build();
  }

  public static String computeOspfExportPolicyName(String vrfName) {
    return "~OSPF_EXPORT_POLICY:" + vrfName + "~";
  }

  /**
   * Generate a {@link RoutingPolicy} for use when importing routes from pseudo-protocols (direct,
   * static, aggregate, generated)
   */
  @VisibleForTesting
  static RoutingPolicy generateDefaultPseudoProtocolImportPolicy(@Nonnull Configuration c) {
    return RoutingPolicy.builder()
        .setOwner(c)
        .setName(DEFAULT_PSEUDO_PROTOCOL_IMPORT_POLICY_NAME)
        .setStatements(
            ImmutableList.of(
                new If(
                    new MatchProtocol(
                        RoutingProtocol.CONNECTED,
                        RoutingProtocol.LOCAL,
                        RoutingProtocol.STATIC,
                        RoutingProtocol.AGGREGATE),
                    ImmutableList.of(Statements.ReturnTrue.toStaticStatement()),
                    ImmutableList.of(ReturnFalse.toStaticStatement()))))
        .build();
  }

  public Map<String, NodeDevice> getNodeDevices() {
    return _nodeDevices;
  }

  private void initDefaultBgpExportPolicy() {
    if (_c.getRoutingPolicies().containsKey(DEFAULT_BGP_EXPORT_POLICY_NAME)) {
      return;
    }
    // set up default export policy (accept bgp routes)
    RoutingPolicy defaultBgpExportPolicy = new RoutingPolicy(DEFAULT_BGP_EXPORT_POLICY_NAME, _c);
    _c.getRoutingPolicies().put(DEFAULT_BGP_EXPORT_POLICY_NAME, defaultBgpExportPolicy);

    If defaultBgpExportPolicyConditional = new If();
    defaultBgpExportPolicy.getStatements().add(defaultBgpExportPolicyConditional);

    // guard
    MatchProtocol isBgp = new MatchProtocol(RoutingProtocol.BGP, RoutingProtocol.IBGP);
    defaultBgpExportPolicyConditional.setGuard(isBgp);

    PsThenAccept.INSTANCE.applyTo(
        defaultBgpExportPolicyConditional.getTrueStatements(), this, _c, _w);
    PsThenReject.INSTANCE.applyTo(
        defaultBgpExportPolicyConditional.getFalseStatements(), this, _c, _w);
  }

  private void initDefaultBgpImportPolicy() {
    if (_c.getRoutingPolicies().containsKey(DEFAULT_BGP_IMPORT_POLICY_NAME)) {
      return;
    }
    // set up default import policy (accept all routes)
    RoutingPolicy defaultBgpImportPolicy = new RoutingPolicy(DEFAULT_BGP_IMPORT_POLICY_NAME, _c);
    _c.getRoutingPolicies().put(DEFAULT_BGP_IMPORT_POLICY_NAME, defaultBgpImportPolicy);
    PsThenAccept.INSTANCE.applyTo(defaultBgpImportPolicy.getStatements(), this, _c, _w);
  }

  /**
   * Initialize default pseudo-protocol import policy (if it does not exist) in the
   * vendor-independent {@link Configuration}.
   *
   * @return the name of the initialized policy
   */
  private void initDefaultPseudoProtocolImportPolicy() {
    if (!_c.getRoutingPolicies().containsKey(DEFAULT_PSEUDO_PROTOCOL_IMPORT_POLICY_NAME)) {
      generateDefaultPseudoProtocolImportPolicy(_c);
    }
  }

  private void initDefaultRejectPolicy() {
    if (!_c.getRoutingPolicies().containsKey(DEFAULT_REJECT_POLICY_NAME)) {
      RoutingPolicy defaultRejectPolicy = new RoutingPolicy(DEFAULT_REJECT_POLICY_NAME, _c);
      _c.getRoutingPolicies().put(DEFAULT_REJECT_POLICY_NAME, defaultRejectPolicy);
      PsThenReject.INSTANCE.applyTo(defaultRejectPolicy.getStatements(), this, _c, _w);
    }
  }

  private void placeInterfaceIntoArea(
      Map<Long, org.batfish.datamodel.ospf.OspfArea.Builder> newAreas,
      String interfaceName,
      Interface iface,
      String vrfName) {
    org.batfish.datamodel.Interface newIface = _c.getAllInterfaces(vrfName).get(interfaceName);
    if (newIface == null) {
      // No VI interface was created. This can happen, for example, for irb; only irb.N are
      // populated in VI.
      return;
    }
    OspfInterfaceSettings ospfInterfaceSettings = iface.getEffectiveOspfSettings();
    if (ospfInterfaceSettings == null) {
      return;
    }
    Ip ospfArea = ospfInterfaceSettings.getOspfArea();
    if (newIface.getConcreteAddress() == null) {
      _w.redFlagf(
          "Cannot assign interface %s to area %s because it has no IP address.",
          interfaceName, ospfArea);
      return;
    }
    long ospfAreaLong = ospfArea.asLong();
    org.batfish.datamodel.ospf.OspfArea.Builder newArea = newAreas.get(ospfAreaLong);
    newArea.addInterface(interfaceName);
  }

  @Override
  public void setVendor(ConfigurationFormat format) {
    _vendor = format;
  }

  private org.batfish.datamodel.GeneratedRoute toGeneratedRoute(GeneratedRoute route) {
    org.batfish.datamodel.GeneratedRoute.Builder newRoute =
        org.batfish.datamodel.GeneratedRoute.builder();

    newRoute.setGenerationPolicy(computeGenerationPolicy(route));
    newRoute.setAdmin(route.getPreference());
    if (route.getAsPath() != null) {
      newRoute.setAsPath(route.getAsPath());
    }
    newRoute.setCommunities(CommunitySet.of(route.getCommunities()));
    newRoute.setMetric(route.getMetric());
    newRoute.setNetwork(route.getPrefix());
    if (route.getTag() != null) {
      newRoute.setTag(route.getTag());
    }

    newRoute.setDiscard(firstNonNull(route.getDrop(), Boolean.FALSE));

    return newRoute.build();
  }

  private org.batfish.datamodel.GeneratedRoute toAggregateRoute(AggregateRoute route) {
    org.batfish.datamodel.GeneratedRoute.Builder newRoute =
        org.batfish.datamodel.GeneratedRoute.builder();

    newRoute.setGenerationPolicy(computeGenerationPolicy(route));
    newRoute.setAdmin(route.getPreference());
    if (route.getAsPath() != null) {
      newRoute.setAsPath(route.getAsPath());
    }
    newRoute.setCommunities(CommunitySet.of(route.getCommunities()));
    newRoute.setMetric(route.getMetric());
    newRoute.setNetwork(route.getPrefix());
    if (route.getTag() != null) {
      newRoute.setTag(route.getTag());
    }

    // sole semantic difference from generated route: aggregate routes are "reject" by default.
    // Note that this can be overridden to "discard", but we model both as discard in Batfish
    // semantics since the sole difference is whether ICMP unreachables are sent.
    newRoute.setDiscard(true);

    return newRoute.build();
  }

  private @Nullable String computeGenerationPolicy(AbstractAggregateRoute route) {
    // passive means it is installed whether or not there is a more specific route; active means the
    // more specific route must be present, and policy should also be checked if present.
    // https://www.juniper.net/documentation/en_US/junos/topics/reference/configuration-statement/active-edit-routing-options.html

    if (!route.getActive()) {
      return null;
    }
    Prefix prefix = route.getPrefix();
    String generationPolicyName = computeRouteGenerationPolicyName(route);
    RoutingPolicy generationPolicy = new RoutingPolicy(generationPolicyName, _c);
    _c.getRoutingPolicies().put(generationPolicyName, generationPolicy);

    // route filter list to match more specific contributing route
    String rflName = computeContributorRouteFilterListName(prefix);
    MatchPrefixSet isContributingRoute =
        new MatchPrefixSet(DestinationNetwork.instance(), new NamedPrefixSet(rflName));
    RouteFilterList rfList = new RouteFilterList(rflName);
    rfList.addLine(
        new org.batfish.datamodel.RouteFilterLine(
            LineAction.PERMIT,
            prefix,
            new SubRange(prefix.getPrefixLength() + 1, Prefix.MAX_PREFIX_LENGTH)));
    _c.getRouteFilterLists().put(rflName, rfList);

    // contributor check that exits for non-contributing routes
    If contributorCheck = new If();
    contributorCheck.setGuard(isContributingRoute);
    contributorCheck.setFalseStatements(
        ImmutableList.of(Statements.ExitReject.toStaticStatement()));
    generationPolicy.getStatements().add(contributorCheck);

    /*
     *  Evaluate policies in order:
     *  - If a policy accepts, stop evaluation and accept.
     *  - If a policy rejects, stop evaulation and reject.
     *  - If no policy takes an action, take default action.
     *  -- Initially, default action is accept.
     *  -- Policy can change default action and fall through.
     */
    generationPolicy.getStatements().add(Statements.SetDefaultActionAccept.toStaticStatement());
    if (!route.getPolicies().isEmpty()) {
      route
          .getPolicies()
          .forEach(
              policyName -> {
                PolicyStatement policy = _masterLogicalSystem.getPolicyStatements().get(policyName);
                boolean defined = policy != null;
                if (defined) {
                  generationPolicy.getStatements().add(new CallStatement(policyName));
                } else {
                  generationPolicy
                      .getStatements()
                      .add(new Comment(String.format("Undefined reference to: %s", policyName)));
                }
              });
    }
    return generationPolicyName;
  }

  public static String computeContributorRouteFilterListName(Prefix prefix) {
    return String.format("~CONTRIBUTOR_TO_%s~", prefix);
  }

  private static String computeRouteGenerationPolicyName(AbstractAggregateRoute route) {
    Prefix prefix = route.getPrefix();
    return route instanceof AggregateRoute
        ? computeAggregatedRouteGenerationPolicyName(prefix)
        : computeGeneratedRouteGenerationPolicyName(prefix);
  }

  public static String computeAggregatedRouteGenerationPolicyName(Prefix prefix) {
    return String.format("~AGGREGATE_ROUTE_POLICY:%s~", prefix);
  }

  public static String computeGeneratedRouteGenerationPolicyName(Prefix prefix) {
    return String.format("~GENERATED_ROUTE_POLICY:%s~", prefix);
  }

  public static String computeIsisExportPolicyName(String routingInstanceName) {
    return String.format("~ISIS_EXPORT_POLICY:%s~", routingInstanceName);
  }

  /**
   * Converts {@link IkePolicy} to {@link IkePhase1Policy} and puts the used pre-shared key as a
   * {@link IkePhase1Key} in the passed-in {@code ikePhase1Keys}
   */
  private static IkePhase1Policy toIkePhase1Policy(
      IkePolicy ikePolicy, ImmutableSortedMap.Builder<String, IkePhase1Key> ikePhase1Keys) {
    String name = ikePolicy.getName();
    IkePhase1Policy ikePhase1Policy = new IkePhase1Policy(name);

    // pre-shared-key
    IkePhase1Key ikePhase1Key = new IkePhase1Key();
    ikePhase1Key.setKeyType(IkeKeyType.PRE_SHARED_KEY_UNENCRYPTED);
    ikePhase1Key.setKeyHash(ikePolicy.getPreSharedKeyHash());

    ikePhase1Keys.put(String.format("~IKE_PHASE1_KEY_%s~", ikePolicy.getName()), ikePhase1Key);

    ikePhase1Policy.setIkePhase1Key(ikePhase1Key);
    ImmutableList.Builder<String> ikePhase1ProposalBuilder = ImmutableList.builder();
    // ike proposals
    ikePolicy.getProposals().forEach(ikePhase1ProposalBuilder::add);
    ikePhase1Policy.setIkePhase1Proposals(ikePhase1ProposalBuilder.build());

    return ikePhase1Policy;
  }

  private IkePhase1Proposal toIkePhase1Proposal(IkeProposal ikeProposal) {
    IkePhase1Proposal ikePhase1Proposal = new IkePhase1Proposal(ikeProposal.getName());
    ikePhase1Proposal.setDiffieHellmanGroup(ikeProposal.getDiffieHellmanGroup());
    ikePhase1Proposal.setAuthenticationMethod(ikeProposal.getAuthenticationMethod());
    ikePhase1Proposal.setEncryptionAlgorithm(ikeProposal.getEncryptionAlgorithm());
    ikePhase1Proposal.setLifetimeSeconds(ikeProposal.getLifetimeSeconds());
    ikePhase1Proposal.setHashingAlgorithm(ikeProposal.getAuthenticationAlgorithm());
    return ikePhase1Proposal;
  }

  /**
   * Convert a non-unit interface to the VI {@link org.batfish.datamodel.Interface}. Returns null if
   * the interface is not eligible for conversion
   *
   * <p>Note that bulk of the configuration is stored at the logical interface level, see {@link
   * #toInterface(Interface)} for those conversions. Here we convert aggregation and bandwidth
   * settings; track VRF membership.
   */
  private @Nullable org.batfish.datamodel.Interface toInterfaceNonUnit(Interface iface) {
    if (!iface.isDefined()) {
      // if this is false then it definitely means interface was created only because it was
      // referred somewhere but never defined so skipping conversion
      return null;
    }
    String name = iface.getName();
    org.batfish.datamodel.Interface newIface =
        org.batfish.datamodel.Interface.builder()
            .setName(name)
            .setType(
                org.batfish.datamodel.Interface.computeInterfaceType(
                    name, _c.getConfigurationFormat()))
            .build();
    newIface.setDeclaredNames(ImmutableSortedSet.of(name));
    newIface.setDescription(iface.getDescription());

    // 802.3ad link aggregation
    if (iface.get8023adInterface() != null) {
      newIface.setChannelGroup(iface.get8023adInterface());
    }
    // Redundant ethernet
    if (iface.getRedundantParentInterface() != null) {
      newIface.setChannelGroup(iface.getRedundantParentInterface());
    }
    if (!iface.getActive()) {
      newIface.adminDown();
    }
    newIface.setBandwidth(iface.getBandwidth());
    if (iface.getMtu() != null) {
      newIface.setMtu(iface.getMtu());
    }
    newIface.setNativeVlan(iface.getNativeVlan());
    newIface.setVrf(_c.getVrfs().get(iface.getRoutingInstance().getName()));
    return newIface;
  }

  /**
   * Converts an Interface unit to VI model. If the interface is not eligible for conversion (is
   * only referred by never defined in the config) then null is returned
   *
   * @param iface a {@link Interface}
   * @return {@link org.batfish.datamodel.Interface}
   */
  private @Nullable org.batfish.datamodel.Interface toInterface(Interface iface) {
    if (!iface.isDefined()) {
      // if this is false then it definitely means interface was created only because it was
      // referred somewhere but never defined so skipping conversion
      return null;
    }
    String name = iface.getName();
    if (iface.getParent().getRedundantParentInterface() != null) {
      _w.redFlagf(
          "Refusing to convert illegal unit '%s' on parent that is a member of a redundant"
              + " ethernet group '%s'",
          name, iface.getParent().getRedundantParentInterface());
      return null;
    }
    org.batfish.datamodel.Interface newIface =
        org.batfish.datamodel.Interface.builder().setName(name).setOwner(_c).build();
    newIface.setDeclaredNames(ImmutableSortedSet.of(name));
    newIface.setDescription(iface.getDescription());
    Integer mtu = iface.getMtu();
    if (mtu != null) {
      newIface.setMtu(mtu);
    }
    newIface.setVrrpGroups(convertVrrpGroups(iface.getName(), iface.getVrrpGroups()));
    newIface.setVrf(_c.getVrfs().get(iface.getRoutingInstance().getName()));
    newIface.setAdditionalArpIps(
        AclIpSpace.union(
            iface.getAdditionalArpIps().stream().map(Ip::toIpSpace).collect(Collectors.toList())));
    Zone zone = _masterLogicalSystem.getInterfaceZones().get(iface.getName());
    if (zone != null) {
      // filter for interface in zone
      FirewallFilter zoneInboundInterfaceFilter =
          zone.getInboundInterfaceFilters().get(iface.getName());
      if (zoneInboundInterfaceFilter != null) {
        String zoneInboundInterfaceFilterName = zoneInboundInterfaceFilter.getName();
        IpAccessList zoneInboundInterfaceFilterList =
            _c.getIpAccessLists().get(zoneInboundInterfaceFilterName);
        newIface.setInboundFilter(zoneInboundInterfaceFilterList);
      } else {
        // filter for zone
        FirewallFilter zoneInboundFilter = zone.getInboundFilter();
        String zoneInboundFilterName = zoneInboundFilter.getName();
        IpAccessList zoneInboundFilterList = _c.getIpAccessLists().get(zoneInboundFilterName);
        newIface.setInboundFilter(zoneInboundFilterList);
      }

      // create session info
      newIface.setFirewallSessionInterfaceInfo(
          new FirewallSessionInterfaceInfo(
              Action.FORWARD_OUT_IFACE,
              zone.getInterfaces(),
              iface.getIncomingFilter(),
              iface.getOutgoingFilter()));
    }

    String incomingFilterName = iface.getIncomingFilter();
    if (incomingFilterName != null) {
      IpAccessList inAcl = _c.getIpAccessLists().get(incomingFilterName);
      if (inAcl != null) {
        FirewallFilter inFilter = _masterLogicalSystem.getFirewallFilters().get(incomingFilterName);
        if (inFilter.isUsedForFBF()) {
          PacketPolicy routingPolicy = _c.getPacketPolicies().get(incomingFilterName);
          if (routingPolicy != null) {
            newIface.setPacketPolicy(incomingFilterName);
          } else {
            newIface.setPacketPolicy(null);
            _w.redFlagf(
                "Interface %s: cannot resolve applied filter %s, defaulting to no filter",
                name, incomingFilterName);
          }
        }
      }
    }
    IpAccessList composedInAcl = buildIncomingFilter(iface);
    newIface.setIncomingFilter(composedInAcl);

    newIface.setIncomingTransformation(buildIncomingTransformation(iface));

    // Assume the config will need security policies only if it has zones
    if (!_masterLogicalSystem.getZones().isEmpty()) {
      String securityPolicyAclName = ACL_NAME_SECURITY_POLICY + iface.getName();
      IpAccessList securityPolicyAcl = buildSecurityPolicyAcl(securityPolicyAclName, zone);
      if (securityPolicyAcl != null) {
        _c.getIpAccessLists().put(securityPolicyAclName, securityPolicyAcl);
        newIface.setPreTransformationOutgoingFilter(securityPolicyAcl);
      }
    }

    // Set outgoing filter
    String outAclName = iface.getOutgoingFilter();
    IpAccessList outAcl = null;
    if (outAclName != null) {
      outAcl = _c.getIpAccessLists().get(outAclName);
    }
    newIface.setOutgoingFilter(outAcl);

    if (iface.getPrimaryAddress() != null) {
      newIface.setAddress(iface.getPrimaryAddress());
    }
    newIface.setAllAddresses(iface.getAllAddresses());
    newIface.setAddressMetadata(
        iface.getAllAddresses().stream()
            .collect(
                ImmutableSortedMap.toImmutableSortedMap(
                    Ordering.natural(),
                    a -> a,
                    JuniperConfiguration::getJuniperConnectedRouteMetadata)));
    if (!iface.getActive()) {
      newIface.adminDown();
    }
    EthernetSwitching es = iface.getEthernetSwitching();
    if (_indirectAccessPorts.containsKey(name)) {
      newIface.setSwitchport(true);
      newIface.setSwitchportMode(SwitchportMode.ACCESS);
      newIface.setAccessVlan(
          _masterLogicalSystem
              .getNamedVlans()
              .get(_indirectAccessPorts.get(name).getName())
              .getVlanId());
    } else if (es != null) {
      if (es.getSwitchportMode() == null || es.getSwitchportMode() == SwitchportMode.ACCESS) {
        Integer accessVlan = computeAccessVlan(iface.getName(), es.getVlanMembers());
        newIface.setAccessVlan(accessVlan);
        if (accessVlan != null) {
          newIface.setSwitchport(true);
          newIface.setSwitchportMode(SwitchportMode.ACCESS);
        } else {
          newIface.setSwitchport(false);
          newIface.setSwitchportMode(SwitchportMode.NONE);
        }
      }
      if (es.getSwitchportMode() == SwitchportMode.TRUNK) {
        newIface.setSwitchportTrunkEncapsulation(SwitchportEncapsulationType.DOT1Q);
        newIface.setAllowedVlans(vlanMembersToIntegerSpace(es.getVlanMembers()));
        // default is no native vlan, untagged are dropped.
        // https://www.juniper.net/documentation/en_US/junos/topics/reference/configuration-statement/native-vlan-id-edit-interfaces-qfx-series.html
        newIface.setNativeVlan(es.getNativeVlan());
        newIface.setSwitchport(true);
        newIface.setSwitchportMode(SwitchportMode.TRUNK);
      }
    } else {
      newIface.setSwitchportMode(SwitchportMode.NONE);
      newIface.setSwitchport(false);
      if (iface.getVlanId() != null) {
        if (iface.getParent().getVlanTagging() == VlanTaggingMode.NONE) {
          _w.redFlagf(
              "%s: VLAN-ID can only be specified on tagged ethernet interfaces, but %s is not"
                  + " configured with vlan-tagging or flexible-vlan-tagging",
              iface.getName(), iface.getParent().getName());
        } else {
          newIface.setEncapsulationVlan(iface.getVlanId());
        }
      }
    }
    newIface.setBandwidth(iface.getBandwidth());
    return newIface;
  }

  private @Nonnull SortedMap<Integer, org.batfish.datamodel.VrrpGroup> convertVrrpGroups(
      String ifaceName, Map<Integer, VrrpGroup> vrrpGroups) {
    ImmutableSortedMap.Builder<Integer, org.batfish.datamodel.VrrpGroup> groupsBuilder =
        ImmutableSortedMap.naturalOrder();
    vrrpGroups.forEach(
        (vrid, vrrpGroup) -> {
          Set<Ip> virtualAddresses = vrrpGroup.getVirtualAddresses();
          if (virtualAddresses.isEmpty()) {
            _w.fatalRedFlag(
                "Cannot create VRRP group for vrid %d on interface '%s' because no virtual-address"
                    + " is assigned.",
                vrid, ifaceName);
            return;
          }
          groupsBuilder.put(
              vrid,
              org.batfish.datamodel.VrrpGroup.builder()
                  .setPreempt(vrrpGroup.getPreempt())
                  .setPriority(vrrpGroup.getPriority())
                  .setSourceAddress(vrrpGroup.getSourceAddress())
                  .setVirtualAddresses(ifaceName, virtualAddresses)
                  .build());
        });
    return groupsBuilder.build();
  }

  private void convertVxlan() {
    for (Vlan vxlan : _masterLogicalSystem.getNamedVlans().values()) {
      if (vxlan.getVniId() == null) {
        continue;
      }
      String l3Interface = vxlan.getL3Interface();
      if (l3Interface == null) {
        if (vxlan.getVlanId() == null) {
          continue;
        }
        // Should be a l2vni
        Layer2Vni vniSettings =
            Layer2Vni.builder()
                .setVni(vxlan.getVniId())
                .setVlan(vxlan.getVlanId())
                .setUdpPort(Vni.DEFAULT_UDP_PORT)
                .setBumTransportMethod(UNICAST_FLOOD_GROUP)
                .setSrcVrf(_masterLogicalSystem.getDefaultRoutingInstance().getName())
                .build();
        _c.getDefaultVrf().addLayer2Vni(vniSettings);
      } else {
        String vtepSource = _masterLogicalSystem.getSwitchOptions().getVtepSourceInterface();
        if (vtepSource == null) {
          continue;
        }
        Interface iface =
            _masterLogicalSystem.getDefaultRoutingInstance().getInterfaces().get(l3Interface);
        if (iface == null) {
          continue;
        }
        if (iface.getPrimaryAddress() == null) {
          continue;
        }
        Layer3Vni vniSettings =
            Layer3Vni.builder()
                .setVni(vxlan.getVniId())
                .setSourceAddress(iface.getPrimaryAddress().getIp())
                .setUdpPort(Vni.DEFAULT_UDP_PORT)
                .setSrcVrf(iface.getRoutingInstance().getName())
                .build();
        _c.getAllInterfaces().get(l3Interface).getVrf().addLayer3Vni(vniSettings);
      }
    }
  }

  private @Nullable Integer computeAccessVlan(String ifaceName, List<VlanMember> vlanMembers) {
    List<VlanMember> effectiveMembers =
        !vlanMembers.isEmpty() ? vlanMembers : ImmutableList.of(DEFAULT_VLAN_MEMBER);
    if (effectiveMembers.size() > 1) {
      _w.redFlagf(
          "Cannot assign access vlan to interface %s: more than one member declared %s",
          ifaceName, effectiveMembers);
      return null;
    }

    VlanMember member = effectiveMembers.get(0);
    IntegerSpace members = vlanMembersToIntegerSpace(effectiveMembers);
    if (members.isSingleton()) {
      // This is the expected case. One member, with one vlan assigned.
      return members.singletonValue();
    } else if (members.isEmpty()) {
      _w.redFlagf(
          "Cannot assign access vlan to interface %s: no vlan-id is assigned to vlan %s",
          ifaceName, member);
      return null;
    }
    _w.redFlagf("Cannot assign more than one access vlan to interface %s: %s", ifaceName, member);
    return null;
  }

  private @Nonnull IntegerSpace vlanMembersToIntegerSpace(Collection<VlanMember> vlanMembers) {
    IntegerSpace.Builder builder = IntegerSpace.builder();
    vlanMembers.stream().map(this::toIntegerSpace).forEach(builder::including);
    return builder.build();
  }

  private @Nonnull IntegerSpace toIntegerSpace(VlanMember vlanMember) {
    if (vlanMember instanceof VlanReference) {
      VlanReference vlanReference = (VlanReference) vlanMember;
      Vlan vlan = _masterLogicalSystem.getNamedVlans().get(vlanReference.getName());
      if (vlan == null || (vlan.getVlanId() == null && vlan.getVlanIdList() == null)) {
        return IntegerSpace.EMPTY;
      }
      // At most one of vlanId and vlanListId can be defined; setting one clears the other
      return vlan.getVlanId() != null
          ? IntegerSpace.of(vlan.getVlanId())
          : IntegerSpace.unionOfSubRanges(vlan.getVlanIdList());
    } else if (vlanMember instanceof VlanRange) {
      return ((VlanRange) vlanMember).getRange();
    } else if (vlanMember instanceof AllVlans) {
      return ALL_VLANS;
    } else {
      _w.redFlagf("Unsupported vlan member type: %s", vlanMember.getClass().getCanonicalName());
      return IntegerSpace.EMPTY;
    }
  }

  private @Nullable OspfNetworkType toOspfNetworkType(
      OspfInterfaceSettings.OspfInterfaceType type) {
    switch (type) {
      case BROADCAST:
        return OspfNetworkType.BROADCAST;
      case P2P:
        return OspfNetworkType.POINT_TO_POINT;
      case NBMA:
        return OspfNetworkType.NON_BROADCAST_MULTI_ACCESS;
      case P2MP:
        return OspfNetworkType.POINT_TO_MULTIPOINT;
      default:
        _w.redFlagf(
            "Conversion of Juniper OSPF network type '%s' is not handled.", type.toString());
        return null;
    }
  }

  @Nullable
  Transformation buildOutgoingTransformation(
      Interface iface,
      Nat nat,
      List<NatRuleSet> orderedRuleSetList,
      Map<NatPacketLocation, AclLineMatchExpr> matchFromLocationExprs,
      @Nullable Transformation orElse) {
    if (orderedRuleSetList == null) {
      return orElse;
    }

    String name = iface.getName();
    String zone =
        Optional.ofNullable(_masterLogicalSystem.getInterfaceZones().get(name))
            .map(Zone::getName)
            .orElse(null);
    String routingInstance = iface.getRoutingInstance().getName();

    List<NatRuleSet> ruleSets =
        orderedRuleSetList.stream()
            .filter(
                ruleSet -> {
                  NatPacketLocation toLocation = ruleSet.getToLocation();
                  return name.equals(toLocation.getInterface())
                      || (zone != null && zone.equals(toLocation.getZone()))
                      || (routingInstance.equals(toLocation.getRoutingInstance()));
                })
            .collect(Collectors.toList());

    if (ruleSets.isEmpty()) {
      return orElse;
    }

    if (iface.getPrimaryAddress() == null) {
      _w.redFlag(
          "Cannot build incoming transformation without an interface IP. Interface name = " + name);
      return orElse;
    }
    Ip interfaceIp = iface.getPrimaryAddress().getIp();

    Transformation transformation = orElse;
    for (NatRuleSet ruleSet : Lists.reverse(ruleSets)) {
      transformation =
          ruleSet
              .toOutgoingTransformation(
                  nat,
                  _masterLogicalSystem
                      .getAddressBooks()
                      .get(LogicalSystem.GLOBAL_ADDRESS_BOOK_NAME)
                      .getEntries(),
                  interfaceIp,
                  matchFromLocationExprs,
                  null,
                  transformation,
                  _w)
              .orElse(transformation);
    }
    return transformation;
  }

  @VisibleForTesting
  Map<NatPacketLocation, AclLineMatchExpr> fromNatPacketLocationMatchExprs() {
    ImmutableMap.Builder<NatPacketLocation, AclLineMatchExpr> builder = ImmutableMap.builder();
    _masterLogicalSystem
        .getInterfaces()
        .values()
        .forEach(
            iface ->
                iface
                    .getUnits()
                    .keySet()
                    .forEach(
                        ifaceUnit ->
                            builder.put(
                                interfaceLocation(ifaceUnit), matchSrcInterface(ifaceUnit))));
    _masterLogicalSystem
        .getZones()
        .values()
        .forEach(
            zone ->
                builder.put(
                    zoneLocation(zone.getName()), new MatchSrcInterface(zone.getInterfaces())));
    _masterLogicalSystem
        .getRoutingInstances()
        .values()
        .forEach(
            routingInstance ->
                builder.put(
                    routingInstanceLocation(routingInstance.getName()),
                    matchSrcInterface(
                        routingInstance.getInterfaces().values().stream()
                            .map(Interface::getName)
                            .toArray(String[]::new))));
    return builder.build();
  }

  @VisibleForTesting
  static @Nullable IpAccessList buildScreen(@Nullable Screen screen, String aclName) {
    if (screen == null || screen.getAction() == ScreenAction.ALARM_WITHOUT_DROP) {
      return null;
    }

    List<AclLineMatchExpr> matches =
        screen.getScreenOptions().stream()
            .map(ScreenOption::getAclLineMatchExpr)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());

    if (matches.isEmpty()) {
      return null;
    }

    return IpAccessList.builder()
        .setName(aclName)
        .setLines(ImmutableList.of(ExprAclLine.rejecting(or(matches)), ExprAclLine.ACCEPT_ALL))
        .build();
  }

  @VisibleForTesting
  @Nullable
  IpAccessList buildScreensPerZone(@Nonnull Zone zone, String aclName) {
    List<AclLineMatchExpr> matches =
        zone.getScreens().stream()
            .map(
                screenName -> {
                  Screen screen = _masterLogicalSystem.getScreens().get(screenName);
                  String screenAclName = ACL_NAME_SCREEN + screenName;
                  IpAccessList screenAcl = _c.getIpAccessLists().get(screenAclName);
                  if (screenAcl == null) {
                    screenAcl = buildScreen(screen, screenAclName);
                    if (screenAcl != null) {
                      _c.getIpAccessLists().put(screenAclName, screenAcl);
                    }
                  }
                  return screenAcl != null ? new PermittedByAcl(screenAcl.getName()) : null;
                })
            .filter(Objects::nonNull)
            .collect(Collectors.toList());

    return matches.isEmpty()
        ? null
        : IpAccessList.builder()
            .setName(aclName)
            .setLines(ImmutableList.of(ExprAclLine.accepting(and(matches))))
            .build();
  }

  @VisibleForTesting
  @Nullable
  IpAccessList buildScreensPerInterface(Interface iface) {
    Zone zone = _masterLogicalSystem.getInterfaceZones().get(iface.getName());
    if (zone == null) {
      return null;
    }

    // build a acl for each zone
    String zoneAclName = ACL_NAME_SCREEN_ZONE + zone.getName();
    IpAccessList zoneAcl = _c.getIpAccessLists().get(zoneAclName);
    if (zoneAcl == null) {
      zoneAcl = buildScreensPerZone(zone, zoneAclName);
      if (zoneAcl != null) {
        _c.getIpAccessLists().put(zoneAclName, zoneAcl);
      }
    }

    return zoneAcl == null
        ? null
        : IpAccessList.builder()
            .setName(ACL_NAME_SCREEN_INTERFACE + iface.getName())
            .setLines(ImmutableList.of(ExprAclLine.accepting(new PermittedByAcl(zoneAclName))))
            .build();
  }

  @Nullable
  IpAccessList buildIncomingFilter(Interface iface) {
    String screenAclName = ACL_NAME_SCREEN_INTERFACE + iface.getName();
    IpAccessList screenAcl = _c.getIpAccessLists().get(screenAclName);
    if (screenAcl == null) {
      screenAcl = buildScreensPerInterface(iface);
      if (screenAcl != null) {
        _c.getIpAccessLists().put(screenAclName, screenAcl);
      }
    }
    // merge screen options to incoming filter
    // but keep both original filters in the config, so we can run search filter queries on them
    String inAclName = iface.getIncomingFilter();
    IpAccessList inAcl = inAclName != null ? _c.getIpAccessLists().get(inAclName) : null;

    Set<AclLineMatchExpr> aclConjunctList;
    if (screenAcl == null) {
      return inAcl;
    } else if (inAcl == null) {
      aclConjunctList = ImmutableSet.of(new PermittedByAcl(screenAcl.getName()));
    } else {
      aclConjunctList =
          ImmutableSet.of(new PermittedByAcl(screenAcl.getName()), new PermittedByAcl(inAclName));
    }

    String combinedAclName = ACL_NAME_COMBINED_INCOMING + iface.getName();
    IpAccessList combinedAcl =
        IpAccessList.builder()
            .setName(combinedAclName)
            .setLines(ImmutableList.of(ExprAclLine.accepting(and(aclConjunctList))))
            .build();

    _c.getIpAccessLists().put(combinedAclName, combinedAcl);
    return combinedAcl;
  }

  private @Nullable Transformation buildIncomingTransformation(
      Nat nat, Interface iface, Transformation orElse) {
    if (nat == null) {
      return orElse;
    }

    String ifaceName = iface.getName();
    String zone =
        Optional.ofNullable(_masterLogicalSystem.getInterfaceZones().get(ifaceName))
            .map(Zone::getName)
            .orElse(null);
    String routingInstance = iface.getRoutingInstance().getName();

    /*
     * Precedence of rule set is by fromLocation: interface > zone > routing instance
     */
    NatRuleSet ifaceLocationRuleSet = null;
    NatRuleSet zoneLocationRuleSet = null;
    NatRuleSet routingInstanceRuleSet = null;
    for (Entry<String, NatRuleSet> entry : nat.getRuleSets().entrySet()) {
      NatRuleSet ruleSet = entry.getValue();
      NatPacketLocation fromLocation = ruleSet.getFromLocation();
      if (ifaceName.equals(fromLocation.getInterface())) {
        ifaceLocationRuleSet = ruleSet;
      } else if (zone != null && zone.equals(fromLocation.getZone())) {
        zoneLocationRuleSet = ruleSet;
      } else if (routingInstance.equals(fromLocation.getRoutingInstance())) {
        routingInstanceRuleSet = ruleSet;
      }
    }

    Transformation transformation = orElse;
    List<NatRuleSet> ruleSets =
        Stream.of(routingInstanceRuleSet, zoneLocationRuleSet, ifaceLocationRuleSet)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());

    if (ruleSets.isEmpty()) {
      return transformation;
    }

    if (iface.getPrimaryAddress() == null) {
      _w.redFlag(
          "Cannot build incoming transformation without an interface IP. Interface name = "
              + iface.getName());
      return null;
    }
    Ip interfaceIp = iface.getPrimaryAddress().getIp();

    for (NatRuleSet ruleSet : ruleSets) {
      transformation =
          ruleSet
              .toIncomingTransformation(
                  nat,
                  _masterLogicalSystem
                      .getAddressBooks()
                      .get(LogicalSystem.GLOBAL_ADDRESS_BOOK_NAME)
                      .getEntries(),
                  interfaceIp,
                  null,
                  transformation,
                  _w)
              .orElse(transformation);
    }
    return transformation;
  }

  private @Nullable Transformation buildIncomingTransformation(Interface iface) {
    Nat dnat = _masterLogicalSystem.getNatDestination();
    Transformation dstTransformation = buildIncomingTransformation(dnat, iface, null);
    Nat staticNat = _masterLogicalSystem.getNatStatic();
    return buildIncomingTransformation(staticNat, iface, dstTransformation);
  }

  /** Generate IpAccessList from the specified to-zone's security policies. */
  @VisibleForTesting
  IpAccessList buildSecurityPolicyAcl(String name, @Nullable Zone zone) {
    List<AclLine> zoneAclLines = new LinkedList<>();

    /* Default policy allows traffic originating from the device to be accepted */
    zoneAclLines.add(
        new ExprAclLine(
            LineAction.PERMIT,
            OriginatingFromDevice.INSTANCE,
            "HOST_OUTBOUND",
            TraceElement.of("Matched Juniper semantics on traffic originated from device"),
            null));

    /* Zone specific policies */
    if (zone != null && !zone.getFromZonePolicies().isEmpty()) {
      String toZone = zone.getName();
      for (Entry<String, FirewallFilter> e : zone.getFromZonePolicies().entrySet()) {
        String filterName = e.getKey();
        FirewallFilter filter = e.getValue();

        // The config is "from-zone junos-host", but we choose to only print "zone" when it's not
        // the host.
        String fromDesc =
            filter.getFromZone().map(s -> String.format("zone %s", s)).orElse("junos-host");
        String policyDesc = String.format("security policy from %s to zone %s", fromDesc, toZone);

        String policyName = zoneToZoneFilter(filter.getFromZone().orElse("junos-host"), toZone);
        VendorStructureId vendorStructureId =
            new VendorStructureId(
                _filename, JuniperStructureType.SECURITY_POLICY.getDescription(), policyName);
        TraceElement traceElement =
            TraceElement.builder().add("Matched ").add(policyDesc, vendorStructureId).build();

        zoneAclLines.add(
            new AclAclLine("Match " + policyDesc, filterName, traceElement, vendorStructureId));
      }
    }

    /* Global policy if applicable */
    if (_masterLogicalSystem.getSecurityPolicies().get(ACL_NAME_GLOBAL_POLICY) != null) {
      /* Handle explicit accept/deny lines for global policy, unmatched lines fall-through to next. */
      VendorStructureId vendorStructureId =
          new VendorStructureId(
              _filename,
              JuniperStructureType.SECURITY_POLICY.getDescription(),
              ACL_NAME_GLOBAL_POLICY);
      zoneAclLines.add(
          new AclAclLine(
              "Match global security policy",
              ACL_NAME_GLOBAL_POLICY,
              TraceElement.builder()
                  .add("Matched ")
                  .add("global security policy", vendorStructureId)
                  .build(),
              vendorStructureId));
    }

    /* Add catch-all line with default action */
    zoneAclLines.add(
        new ExprAclLine(
            _masterLogicalSystem.getDefaultCrossZoneAction(),
            TrueExpr.INSTANCE,
            "DEFAULT_POLICY",
            TraceElement.of("Matched default policy"),
            null));

    IpAccessList zoneAcl = IpAccessList.builder().setName(name).setLines(zoneAclLines).build();
    _c.getIpAccessLists().put(name, zoneAcl);
    return zoneAcl;
  }

  /**
   * Convert firewallFilter terms (headerSpace matching) and optional conjunctMatchExpr into a
   * single ACL.
   */
  @VisibleForTesting
  @Nonnull
  IpAccessList fwTermsToIpAccessList(
      String createdAclName,
      ConcreteFirewallFilter filter,
      @Nullable AclLineMatchExpr conjunctMatchExpr,
      JuniperStructureType aclType)
      throws VendorConversionException {

    List<ExprAclLine> lines =
        filter.getTerms().values().stream()
            .map(term -> convertFwTermToExprAclLine(filter.getName(), term, aclType))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .collect(ImmutableList.toImmutableList());

    return IpAccessList.builder()
        .setName(createdAclName)
        .setLines(mergeIpAccessListLines(lines, conjunctMatchExpr))
        .setSourceName(filter.getName())
        .setSourceType(aclType.getDescription())
        .build();
  }

  private Optional<ExprAclLine> convertFwTermToExprAclLine(
      String aclName, FwTerm term, JuniperStructureType aclType) {
    LineAction action = getLineAction(aclName, term);
    if (action == null) {
      return Optional.empty();
    }

    // We do not support ip options for now. Simply return an empty list assuming this term is
    // unmatchable
    if (term.getFromIpOptions() != null) {
      assert term.getFromHostProtocols().isEmpty()
          && term.getFromHostServices().isEmpty()
          && term.getFromApplicationSetMembers().isEmpty();
      // TODO: implement
      // For now, assume line is unmatchable.
      return Optional.empty();
    }

    List<AclLineMatchExpr> fwFromAndApplicationConjuncts = new ArrayList<>();
    if (!term.getFroms().isEmpty()) {
      fwFromAndApplicationConjuncts.add(toAclLineMatchExpr(term.getFroms(), null));
    }
    if (!term.getFromApplicationSetMembers().isEmpty()) {
      fwFromAndApplicationConjuncts.add(
          or(
              term.getFromApplicationSetMembers().stream()
                  .map(from -> from.toAclLineMatchExpr(this, _w))
                  .collect(ImmutableList.toImmutableList())));
    }

    List<AclLineMatchExpr> fwFromProtocolAndServiceDisjuncts = new ArrayList<>();
    for (HostProtocol from : term.getFromHostProtocols()) {
      from.getMatchExpr().ifPresent(fwFromProtocolAndServiceDisjuncts::add);
    }
    for (HostSystemService from : term.getFromHostServices()) {
      from.getMatchExpr().ifPresent(fwFromProtocolAndServiceDisjuncts::add);
    }

    if (!(term.getFromHostProtocols().isEmpty() && term.getFromHostServices().isEmpty())) {
      assert term.getFroms().isEmpty() && term.getFromApplicationSetMembers().isEmpty();
      return fwFromProtocolAndServiceDisjuncts.isEmpty()
          ? Optional.empty()
          : Optional.of(
              ExprAclLine.builder()
                  .setAction(action)
                  .setMatchCondition(or(fwFromProtocolAndServiceDisjuncts))
                  .setName(term.getName())
                  .setTraceElement(
                      matchingAbstractTerm(aclType, _filename, aclName, term.getName()))
                  .setVendorStructureId(
                      abstractTermVendorStructureId(aclType, _filename, aclName, term.getName()))
                  .build());
    }

    return Optional.of(
        ExprAclLine.builder()
            .setAction(action)
            .setMatchCondition(and(fwFromAndApplicationConjuncts))
            .setName(term.getName())
            .setTraceElement(matchingAbstractTerm(aclType, _filename, aclName, term.getName()))
            .setVendorStructureId(
                abstractTermVendorStructureId(aclType, _filename, aclName, term.getName()))
            .build());
  }

  private @Nullable LineAction getLineAction(String aclName, FwTerm term) {
    if (term.getThens().contains(FwThenAccept.INSTANCE)) {
      return LineAction.PERMIT;
    }

    if (term.getThens().contains(FwThenDiscard.INSTANCE)) {
      return LineAction.DENY;
    }

    if (term.getThens().contains(FwThenNextTerm.INSTANCE)) {
      // TODO: throw error if any transformation is being done
      return null;
    }

    if (term.getThens().contains(FwThenNop.INSTANCE)) {
      // we assume for now that any 'nop' operations imply acceptance
      return LineAction.PERMIT;
    }

    if (term.getThens().stream()
        .map(Object::getClass)
        .anyMatch(Predicate.isEqual(FwThenRoutingInstance.class))) {
      // Should be handled by packet policy, not applicable to ACLs
      return null;
    }

    _w.redFlag(
        "missing action in firewall filter: '" + aclName + "', term: '" + term.getName() + "'");
    return LineAction.DENY;
  }

  /** Merge the list of lines with the specified conjunct match expression. */
  @VisibleForTesting
  static List<AclLine> mergeIpAccessListLines(
      List<ExprAclLine> lines, @Nullable AclLineMatchExpr conjunctMatchExpr) {
    if (conjunctMatchExpr == null) {
      return ImmutableList.copyOf(lines);
    }

    return lines.stream()
        .map(
            l ->
                new ExprAclLine(
                    l.getAction(),
                    and(l.getMatchCondition(), conjunctMatchExpr),
                    l.getName(),
                    l.getTraceElement(),
                    l.getVendorStructureId().orElse(null)))
        .collect(ImmutableList.toImmutableList());
  }

  /** Convert a firewallFilter into an equivalent ACL. */
  @VisibleForTesting
  IpAccessList filterToIpAccessList(FirewallFilter f) throws VendorConversionException {
    String name = f.getName();

    if (f instanceof ConcreteFirewallFilter) {
      ConcreteFirewallFilter filter = (ConcreteFirewallFilter) f;
      assert !filter.getFromZone().isPresent(); // not a security policy

      /* Return an ACL that is the logical AND of srcInterface filter and headerSpace filter */
      return fwTermsToIpAccessList(name, filter, null, JuniperStructureType.FIREWALL_FILTER);
    } else {
      assert f instanceof CompositeFirewallFilter;
      CompositeFirewallFilter filter = (CompositeFirewallFilter) f;
      ImmutableList.Builder<AclLine> lines = ImmutableList.builder();
      for (FirewallFilter inner : filter.getInner()) {
        String filterName = inner.getName();
        lines.add(
            new AclAclLine(
                filterName,
                filterName,
                matchingFirewallFilter(_filename, filterName),
                firewallFilterVendorStructureId(_filename, filterName)));
      }
      return IpAccessList.builder().setName(filter.getName()).setLines(lines.build()).build();
    }
  }

  /** Convert a security policy into an equivalent ACL. */
  @VisibleForTesting
  IpAccessList securityPolicyToIpAccessList(ConcreteFirewallFilter filter)
      throws VendorConversionException {
    // For cross-zone policies, create an ACL that contains purely the policy without the from-zone
    // check. This is not used in the forwarding pipeline, but rather is for policy analysis only.
    if (filter.getName().startsWith("zone~")) {
      IpAccessList purelyPolicy =
          fwTermsToIpAccessList(
              String.format("~%s~pure", filter.getName()),
              filter,
              null,
              JuniperStructureType.SECURITY_POLICY);
      _c.getIpAccessLists().put(purelyPolicy.getName(), purelyPolicy);
    }

    // From zone is present if this is not a global security policy and if the from-zone is not
    // junos-host.
    AclLineMatchExpr matchSrcInterface =
        filter
            .getFromZone()
            .map(
                zoneName ->
                    new MatchSrcInterface(
                        _masterLogicalSystem.getZones().get(zoneName).getInterfaces()))
            .orElse(null);

    // In the forwarding pipeline, the returned ACL has a logical AND of srcInterface filter and
    // headerSpace filter.
    return fwTermsToIpAccessList(
        filter.getName(), filter, matchSrcInterface, JuniperStructureType.SECURITY_POLICY);
  }

  private @Nullable IpsecPeerConfig toIpsecPeerConfig(IpsecVpn ipsecVpn) {
    IpsecStaticPeerConfig.Builder ipsecStaticConfigBuilder = IpsecStaticPeerConfig.builder();
    ipsecStaticConfigBuilder.setTunnelInterface(ipsecVpn.getBindInterface());
    if (ipsecVpn.getGateway() == null) {
      _w.redFlagf("No IKE gateway configured for ipsec vpn %s", ipsecVpn.getName());
      return null;
    }

    IkeGateway ikeGateway = _masterLogicalSystem.getIkeGateways().get(ipsecVpn.getGateway());
    if (ikeGateway == null) {
      _w.redFlagf(
          "Cannot find the IKE gateway %s for ipsec vpn %s",
          ipsecVpn.getGateway(), ipsecVpn.getName());
      return null;
    }
    ipsecStaticConfigBuilder.setDestinationAddress(ikeGateway.getAddress());

    String externalIfaceName = ikeGateway.getExternalInterface();
    String masterIfaceName = interfaceUnitMasterName(externalIfaceName);
    if (masterIfaceName == null) {
      _w.redFlagf(
          "Incorrect non-unit external-interface %s for ipsec vpn %s gateway %s",
          externalIfaceName, ipsecVpn.getName(), ipsecVpn.getGateway());
      return null;
    }
    Interface masterIface = _masterLogicalSystem.getInterfaces().get(masterIfaceName);
    if (masterIface == null) {
      _w.redFlagf(
          "Cannot find the IKE gateway interface %s for ipsec vpn %s gateway %s",
          externalIfaceName, ipsecVpn.getName(), ipsecVpn.getGateway());
      return null;
    }

    Interface externalIface = masterIface.getUnits().get(externalIfaceName);

    ipsecStaticConfigBuilder.setSourceInterface(externalIfaceName);

    Ip localAddress = null;
    if (ikeGateway.getLocalAddress() != null) {
      localAddress = ikeGateway.getLocalAddress();
    } else if (externalIface != null && externalIface.getPrimaryAddress() != null) {
      localAddress = externalIface.getPrimaryAddress().getIp();
    }
    if (localAddress == null || !localAddress.valid()) {
      _w.redFlagf(
          "External interface %s configured on IKE Gateway %s does not have any IP",
          externalIfaceName, ikeGateway.getName());
      return null;
    }
    ipsecStaticConfigBuilder.setLocalAddress(localAddress);

    ipsecStaticConfigBuilder.setIpsecPolicy(ipsecVpn.getIpsecPolicy());
    ipsecStaticConfigBuilder.setIkePhase1Policy(ikeGateway.getIkePolicy());
    return ipsecStaticConfigBuilder.build();
  }

  private static IpsecPhase2Policy toIpsecPhase2Policy(IpsecPolicy ipsecPolicy) {
    IpsecPhase2Policy ipsecPhase2Policy = new IpsecPhase2Policy();
    ipsecPhase2Policy.setPfsKeyGroup(ipsecPolicy.getPfsKeyGroup());
    ipsecPhase2Policy.setProposals(ImmutableList.copyOf(ipsecPolicy.getProposals()));

    return ipsecPhase2Policy;
  }

  private static IpsecPhase2Proposal toIpsecPhase2Proposal(IpsecProposal oldIpsecProposal) {
    IpsecPhase2Proposal ipsecPhase2Proposal = new IpsecPhase2Proposal();
    ipsecPhase2Proposal.setAuthenticationAlgorithm(oldIpsecProposal.getAuthenticationAlgorithm());
    ipsecPhase2Proposal.setEncryptionAlgorithm(oldIpsecProposal.getEncryptionAlgorithm());
    ipsecPhase2Proposal.setProtocols(oldIpsecProposal.getProtocols());
    ipsecPhase2Proposal.setIpsecEncapsulationMode(oldIpsecProposal.getIpsecEncapsulationMode());

    return ipsecPhase2Proposal;
  }

  /** Convert address book into corresponding IpSpaces */
  private Map<String, IpSpace> toIpSpaces(String bookName, AddressBook book) {
    Map<String, IpSpace> ipSpaces = new TreeMap<>();
    book.getEntries()
        .forEach(
            (n, entry) -> {
              String entryName = bookName + "~" + n;

              // If this address book references other entries, add them to an AclIpSpace
              if (!entry.getEntries().isEmpty()) {
                AclIpSpace.Builder aclIpSpaceBuilder = AclIpSpace.builder();
                entry
                    .getEntries()
                    .keySet()
                    .forEach(
                        name -> {
                          String subEntryName = bookName + "~" + name;
                          aclIpSpaceBuilder.thenPermitting(new IpSpaceReference(subEntryName));
                        });
                ipSpaces.put(entryName, aclIpSpaceBuilder.build());
              } else {
                ipSpaces.put(
                    entryName,
                    IpWildcardSetIpSpace.builder().including(entry.getIpWildcards(_w)).build());
              }
            });
    return ipSpaces;
  }

  /**
   * Given a list of policy names, returns a list of the VRFs referenced in those policies, in the
   * order in which they appear (within each policy and within the list of policies). The list will
   * not have duplicates if a VRF is referenced multiple times, nor will it include undefined VRFs.
   *
   * <p>Used for generating the list of VRFs to be imported for instance-import policies.
   */
  private @Nonnull List<String> getVrfsReferencedByPolicies(List<String> instanceImportPolicies) {
    return instanceImportPolicies.stream()
        .filter(pName -> _c.getRoutingPolicies().containsKey(pName))
        .flatMap(pName -> _vrfReferencesInPolicies.getOrDefault(pName, ImmutableList.of()).stream())
        .distinct()
        .filter(vrfName -> _c.getVrfs().containsKey(vrfName))
        .collect(ImmutableList.toImmutableList());
  }

  private static @Nonnull RoutingPolicy buildInstanceImportRoutingPolicy(
      RoutingInstance ri, Configuration c, String vrfName) {
    String policyName = generateInstanceImportPolicyName(vrfName);
    List<BooleanExpr> policyCalls =
        ri.getInstanceImports().stream()
            .filter(c.getRoutingPolicies()::containsKey)
            .map(CallExpr::new)
            .collect(Collectors.toList());
    return RoutingPolicy.builder()
        .setOwner(c)
        .setName(policyName)
        .setStatements(
            ImmutableList.of(
                // TODO implement default policy for instance-import. For now default reject.
                // Once this is fixed, can throw away default reject policy infrastructure.
                new SetDefaultPolicy(DEFAULT_REJECT_POLICY_NAME),
                // Construct a policy chain based on defined instance-import policies
                new If(
                    new FirstMatchChain(policyCalls),
                    ImmutableList.of(Statements.ReturnTrue.toStaticStatement()),
                    ImmutableList.of(ReturnFalse.toStaticStatement()))))
        .build();
  }

  public static String generateInstanceImportPolicyName(String vrfName) {
    return String.format("~INSTANCE_IMPORT_POLICY_%s~", vrfName);
  }

  private static @Nonnull org.batfish.datamodel.dataplane.rib.RibGroup toRibGroup(
      RibGroup rg, RoutingProtocol protocol, Configuration c, String vrfName, Warnings w) {
    ImmutableList<RibId> importRibs =
        rg.getImportRibs().stream()
            .map(rib -> toRibId(c.getHostname(), rib, w))
            .filter(Objects::nonNull)
            // Filter out the primary rib for this rib group, since it's special and bypasses the
            // policy
            .filter(
                rib ->
                    !(rib.getRibName().equals(RibId.DEFAULT_RIB_NAME)
                        && rib.getVrfName().equals(vrfName)))
            .collect(ImmutableList.toImmutableList());

    RibId exportRib =
        rg.getExportRib() != null ? toRibId(c.getHostname(), rg.getExportRib(), w) : null;
    List<BooleanExpr> policyCalls =
        rg.getImportPolicies().stream().map(CallExpr::new).collect(ImmutableList.toImmutableList());

    String policyName = generateRibGroupImportPolicyName(rg, protocol);
    RoutingPolicy.builder()
        .setOwner(c)
        .setName(policyName)
        .setStatements(
            ImmutableList.of(
                // Add default policy
                new SetDefaultPolicy(DEFAULT_IMPORT_POLICIES.get(protocol)),
                // Construct a policy chain based on defined import policies
                new If(
                    new FirstMatchChain(policyCalls),
                    ImmutableList.of(Statements.ReturnTrue.toStaticStatement()),
                    ImmutableList.of(ReturnFalse.toStaticStatement()))))
        .build();
    return new org.batfish.datamodel.dataplane.rib.RibGroup(
        rg.getName(), importRibs, policyName, exportRib);
  }

  private static String generateRibGroupImportPolicyName(RibGroup rg, RoutingProtocol protocol) {
    return String.format("~RIB_GROUP_IMPORT_POLICY_%s_%s~", rg.getName(), protocol);
  }

  @VisibleForTesting
  public static @Nonnull String generateResolutionRibImportPolicyName(String routingInstanceName) {
    return String.format("~RESOLUTION_IMPORT_POLICY_%s~", routingInstanceName);
  }

  @VisibleForTesting
  static @Nullable RibId toRibId(String hostname, String rib, @Nullable Warnings w) {
    String[] parts = rib.split("\\.");
    if (parts.length < 2 || parts.length > 3) {
      throw new VendorConversionException(String.format("Invalid RIB identifier %s", rib));
    }
    String vrfName;
    String addressFamily;
    String ribNumber;
    if (parts.length == 3) {
      vrfName = parts[0];
      addressFamily = parts[1];
      ribNumber = parts[2];
    } else { // length == 2
      vrfName = Configuration.DEFAULT_VRF_NAME;
      addressFamily = parts[0];
      ribNumber = parts[1];
    }

    String ribName = addressFamily + "." + ribNumber;
    // Normalize the common case into vendor-independent language
    if (ribName.equals(RIB_IPV4_UNICAST)) {
      ribName = RibId.DEFAULT_RIB_NAME;
    }

    // We only support IPv4 unicast
    if (!addressFamily.equals("inet") && w != null) {
      w.unimplementedf("Rib name conversion: %s address family is not supported", addressFamily);
      return null;
    }
    return new RibId(hostname, vrfName, ribName);
  }

  /**
   * Convert a firewall filter into a policy that can be used for policy-based routing (or
   * filter-based forwarding, in Juniper parlance).
   */
  private PacketPolicy toPacketPolicy(FirewallFilter filter) {
    if (filter instanceof ConcreteFirewallFilter) {
      return toPacketPolicy((ConcreteFirewallFilter) filter);
    } else {
      assert filter instanceof CompositeFirewallFilter;
      return toPacketPolicy((CompositeFirewallFilter) filter);
    }
  }

  /**
   * Makes a composite {@link PacketPolicy} by (recursively) flattening the statements from all
   * inner packet policies.
   */
  private PacketPolicy toPacketPolicy(CompositeFirewallFilter filter) {
    List<org.batfish.datamodel.packet_policy.Statement> concatenatedStatememnts =
        filter.getInner().stream()
            .map(this::toPacketPolicy)
            .flatMap(p -> p.getStatements().stream())
            .collect(ImmutableList.toImmutableList());
    // Make the policy, with an implicit deny all at the end as the default action
    return new PacketPolicy(filter.getName(), concatenatedStatememnts, new Return(Drop.instance()));
  }

  private AclLineMatchExpr toAclLineMatchExpr(
      Collection<FwFrom> fwFroms, TraceElement traceElement) {
    List<AclLineMatchExpr> conjuncts =
        fwFroms.stream()
            // EnumMap is sorted, which gives us deterministic ordering of the And conjuncts
            .collect(
                groupingBy(FwFrom::getField, () -> new EnumMap<>(Field.class), Collectors.toList()))
            .entrySet()
            .stream()
            .map(
                e -> {
                  Field field = e.getKey();
                  List<FwFrom> froms = e.getValue();
                  List<AclLineMatchExpr> inner =
                      froms.stream()
                          .map(fwFromDisjunct -> fwFromDisjunct.toAclLineMatchExpr(this, _c, _w))
                          .collect(ImmutableList.toImmutableList());
                  switch (field) {
                    case DESTINATION_EXCEPT:
                    case FRAGMENT_OFFSET_EXCEPT:
                    case ICMP_CODE_EXCEPT:
                    case ICMP_TYPE_EXCEPT:
                    case PACKET_LENGTH_EXCEPT:
                    case SOURCE_EXCEPT:
                      // FOO_EXCEPT is already compiled to a list of (Not(MatchFoo),
                      // so combining them needs an AND.
                      return and(inner);
                    default:
                      return or(inner);
                  }
                })
            .collect(ImmutableList.toImmutableList());
    return and(conjuncts, traceElement);
  }

  private PacketPolicy toPacketPolicy(ConcreteFirewallFilter filter) {
    ImmutableList.Builder<org.batfish.datamodel.packet_policy.Statement> builder =
        ImmutableList.builder();
    for (Entry<String, FwTerm> e : filter.getTerms().entrySet()) {
      FwTerm term = e.getValue();

      AclLineMatchExpr matchFwFroms =
          toAclLineMatchExpr(
              term.getFroms(),
              TraceElement.of(String.format("Firewall filter term %s", term.getName())));

      // A term will become an If statement. If (matchCondition) -> execute "then" statements
      builder.add(
          new org.batfish.datamodel.packet_policy.If(
              new PacketMatchExpr(matchFwFroms), TermFwThenToPacketPolicyStatement.convert(term)));
    }

    // Make the policy, with an implicit deny all at the end as the default action
    return new PacketPolicy(filter.getName(), builder.build(), new Return(Drop.instance()));
  }

  private @Nonnull BooleanExpr toBooleanExpr(@Nonnull Condition condition) {
    return new TrackSucceeded(computeConditionTrackName(condition.getName()));
  }

  @VisibleForTesting
  RoutingPolicy toRoutingPolicy(PolicyStatement ps) {
    detectMisplacedTerminalActions(ps);

    // Ensure map of VRFs referenced in routing policies is initialized
    if (_vrfReferencesInPolicies == null) {
      _vrfReferencesInPolicies = new TreeMap<>();
    }
    String name = ps.getName();
    RoutingPolicy routingPolicy = new RoutingPolicy(name, _c);
    List<Statement> statements = routingPolicy.getStatements();
    boolean hasDefaultTerm =
        ps.getDefaultTerm().hasAtLeastOneFrom() || !ps.getDefaultTerm().getThens().isEmpty();
    List<PsTerm> terms = new ArrayList<>(ps.getTerms().values());
    if (hasDefaultTerm) {
      terms.add(ps.getDefaultTerm());
    }
    for (PsTerm term : terms) {
      List<Statement> thens = toStatements(term.getThens().getAllThens());
      if (term.hasAtLeastOneFrom()) {
        If ifStatement = new If();
        ifStatement.setComment(term.getName());
        PsFroms froms = term.getFroms();

        for (PsFromRouteFilter fromRouteFilter : froms.getFromRouteFilters()) {
          int actionLineCounter = 0;
          String routeFilterName = fromRouteFilter.getRouteFilterName();
          RouteFilter rf = _masterLogicalSystem.getRouteFilters().get(routeFilterName);
          if (!rf.getIpv4()) {
            continue;
          }
          for (RouteFilterLine line : rf.getLines()) {
            if (!(line instanceof Route4FilterLine)) {
              continue;
            }
            if (!line.getThens().isEmpty()) {
              String lineListName = name + "_ACTION_LINE_" + actionLineCounter;
              RouteFilterList lineSpecificList = new RouteFilterList(lineListName);
              ((Route4FilterLine) line).applyTo(lineSpecificList);
              actionLineCounter++;
              _c.getRouteFilterLists().put(lineListName, lineSpecificList);
              If lineSpecificIfStatement = new If();
              String lineSpecificClauseName = routeFilterName + "_ACTION_LINE_" + actionLineCounter;
              lineSpecificIfStatement.setComment(lineSpecificClauseName);
              MatchPrefixSet mrf =
                  new MatchPrefixSet(
                      DestinationNetwork.instance(), new NamedPrefixSet(lineListName));
              lineSpecificIfStatement.setGuard(mrf);
              lineSpecificIfStatement
                  .getTrueStatements()
                  .addAll(toStatements(line.getThens().getAllThens()));
              statements.add(lineSpecificIfStatement);
            }
          }
        }
        if (froms.getFromInstance() != null) {
          _vrfReferencesInPolicies
              .computeIfAbsent(name, n -> new ArrayList<>())
              .add(froms.getFromInstance().getRoutingInstanceName());
        }
        ifStatement.setGuard(toGuard(froms));
        ifStatement.setTrueStatements(
            ImmutableList.of(toTraceableStatement(thens, term.getName(), ps.getName(), _filename)));
        statements.add(ifStatement);
      } else {
        statements.add(toTraceableStatement(thens, term.getName(), ps.getName(), _filename));
      }
    }
    If endOfPolicy = new If();
    endOfPolicy.setGuard(BooleanExprs.CALL_EXPR_CONTEXT);
    endOfPolicy.setFalseStatements(
        Collections.singletonList(Statements.Return.toStaticStatement()));
    statements.add(endOfPolicy);
    return routingPolicy;
  }

  private BooleanExpr toGuard(PsFroms froms) {
    if (froms.getFromUnsupported() != null) {
      // Unsupported line will evaluate to BooleanExprs.FALSE. Don't bother continuing
      return froms.getFromUnsupported().toBooleanExpr(this, _c, _w);
    }

    Conjunction conj = new Conjunction();
    List<BooleanExpr> subroutines = new ArrayList<>();
    if (froms.getFromFamily() != null) {
      conj.getConjuncts().add(froms.getFromFamily().toBooleanExpr(this, _c, _w));
    }
    if (!froms.getFromAsPaths().isEmpty()) {
      conj.getConjuncts().add(new Disjunction(toBooleanExprs(froms.getFromAsPaths())));
    }
    if (!froms.getFromAsPathGroups().isEmpty()) {
      // TODO: Figure out how it works when both as-path and as-path-group present
      conj.getConjuncts().add(new Disjunction(toBooleanExprs(froms.getFromAsPathGroups())));
    }
    if (froms.getFromColor() != null) {
      conj.getConjuncts().add(froms.getFromColor().toBooleanExpr(this, _c, _w));
    }
    if (!froms.getFromCommunities().isEmpty()) {
      conj.getConjuncts()
          .add(PsFromCommunity.groupToMatchCommunities(_c, froms.getFromCommunities()));
    }
    if (froms.getFromCommunityCount() != null) {
      conj.getConjuncts().add(froms.getFromCommunityCount().toBooleanExpr(this, _c, _w));
    }
    if (!froms.getFromConditions().isEmpty()) {
      // TODO: verify these are disjoined
      conj.getConjuncts().add(new Disjunction(toBooleanExprs(froms.getFromConditions())));
    }
    if (froms.getFromInstance() != null) {
      conj.getConjuncts().add(froms.getFromInstance().toBooleanExpr(this, _c, _w));
    }
    if (!froms.getFromInterfaces().isEmpty()) {
      conj.getConjuncts().add(new Disjunction(toBooleanExprs(froms.getFromInterfaces())));
    }
    if (froms.getFromLocalPreference() != null) {
      conj.getConjuncts().add(froms.getFromLocalPreference().toBooleanExpr(this, _c, _w));
    }
    if (froms.getFromMetric() != null) {
      conj.getConjuncts().add(froms.getFromMetric().toBooleanExpr(this, _c, _w));
    }
    if (!froms.getFromNeighbor().isEmpty()) {
      conj.getConjuncts().add(toBooleanExprMany(froms.getFromNeighbor()));
    }
    if (!froms.getFromNextHops().isEmpty()) {
      conj.getConjuncts().add(new Disjunction(toBooleanExprs(froms.getFromNextHops())));
    }
    for (PsFromPolicyStatement from : froms.getFromPolicyStatements()) {
      subroutines.add(from.toBooleanExpr(this, _c, _w));
    }
    for (PsFromPolicyStatementConjunction from : froms.getFromPolicyStatementConjunctions()) {
      subroutines.add(from.toBooleanExpr(this, _c, _w));
    }
    if (!froms.getFromPrefixLists().isEmpty()
        || !froms.getFromPrefixListFilterLongers().isEmpty()
        || !froms.getFromPrefixListFilterOrLongers().isEmpty()
        || !froms.getFromRouteFilters().isEmpty()) {
      // TODO check behavior for some edge cases: https://github.com/batfish/batfish/issues/2972
      Disjunction prefixListDisjunction = new Disjunction();
      prefixListDisjunction.getDisjuncts().addAll(toBooleanExprs(froms.getFromPrefixLists()));
      prefixListDisjunction
          .getDisjuncts()
          .addAll(toBooleanExprs(froms.getFromPrefixListFilterLongers()));
      prefixListDisjunction
          .getDisjuncts()
          .addAll(toBooleanExprs(froms.getFromPrefixListFilterOrLongers()));
      prefixListDisjunction.getDisjuncts().addAll(toBooleanExprs(froms.getFromRouteFilters()));
      conj.getConjuncts().add(prefixListDisjunction);
    }
    if (!froms.getFromProtocols().isEmpty()) {
      conj.getConjuncts().add(new Disjunction(toBooleanExprs(froms.getFromProtocols())));
    }
    if (froms.getFromExternal() != null) {
      conj.getConjuncts().add(froms.getFromExternal().toBooleanExpr(this, _c, _w));
    }
    if (froms.getFromRouteType() != null) {
      conj.getConjuncts().add(froms.getFromRouteType().toBooleanExpr(this, _c, _w));
    }
    if (!froms.getFromTags().isEmpty()) {
      conj.getConjuncts().add(new Disjunction(toBooleanExprs(froms.getFromTags())));
    }

    if (!subroutines.isEmpty()) {
      ConjunctionChain chain = new ConjunctionChain(subroutines);
      conj.getConjuncts().add(chain);
    }
    return conj.simplify();
  }

  private List<BooleanExpr> toBooleanExprs(Collection<? extends PsFrom> froms) {
    return froms.stream()
        .map(f -> f.toBooleanExpr(this, _c, _w))
        .collect(ImmutableList.toImmutableList());
  }

  private static boolean isFinalThen(PsThen then) {
    return then instanceof PsThenAccept
        || then instanceof PsThenReject
        || then instanceof PsThenDefaultActionAccept
        || then instanceof PsThenDefaultActionReject
        || then instanceof PsThenNextPolicy;
  }

  private List<Statement> toStatements(Set<PsThen> thens) {
    List<Statement> thenStatements = new ArrayList<>();
    Stream.concat(
            thens.stream().filter(then -> !isFinalThen(then)),
            thens.stream().filter(JuniperConfiguration::isFinalThen))
        .forEach(then -> then.applyTo(thenStatements, this, _c, _w));
    return thenStatements;
  }

  @VisibleForTesting
  static TraceableStatement toTraceableStatement(
      List<Statement> statements, String termName, String policyName, String fileName) {
    return new TraceableStatement(
        TraceElement.builder()
            .add("Matched ")
            .add(
                String.format(
                    "policy-statement %s term %s",
                    escapeNameIfNeeded(policyName), escapeNameIfNeeded(termName)),
                new VendorStructureId(
                    fileName,
                    POLICY_STATEMENT_TERM.getDescription(),
                    computePolicyStatementTermName(policyName, termName)))
            .build(),
        statements);
  }

  private Set<org.batfish.datamodel.StaticRoute> toStaticRoutes(StaticRouteV4 route) {
    String nextTable = route.getNextTable();
    Prefix prefix = route.getPrefix();
    String nextVrf = null;
    if (nextTable != null) {
      RibId ribId = toRibId(getHostname(), nextTable, _w);
      if (ribId == null) {
        _w.redFlagf(
            "Static route for prefix %s contains illegal next-table value: %s", prefix, nextTable);
        return ImmutableSet.of();
      }
      if (!ribId.getRibName().equals(RibId.DEFAULT_RIB_NAME)) {
        _w.unimplementedf("next-table support is currently limited to %s", RIB_IPV4_UNICAST);
        return ImmutableSet.of();
      }
      nextVrf = ribId.getVrfName();
    }
    if (nextVrf != null && !route.getQualifiedNextHops().isEmpty()) {
      _w.redFlagf(
          "Static route for prefix %s illegally contains both next-table and"
              + " qualified-next-hop",
          prefix);
      return ImmutableSet.of();
    }
    if (route.getDrop() && !route.getQualifiedNextHops().isEmpty()) {
      _w.redFlagf(
          "Static route for prefix %s cannot contain both discard nexthop and"
              + " qualified-next-hop. Ignoring this route.",
          prefix);
      return ImmutableSet.of();
    }
    ImmutableSet.Builder<org.batfish.datamodel.StaticRoute> viStaticRoutes = ImmutableSet.builder();

    // static route corresponding to the next hop
    boolean noInstall = !firstNonNull(route.getInstall(), Boolean.TRUE);
    // TOOD: return routing-instance-level default setting instead of false
    boolean resolve = firstNonNull(route.getResolve(), Boolean.FALSE);

    org.batfish.datamodel.StaticRoute.Builder rBuilder =
        org.batfish.datamodel.StaticRoute.builder()
            .setNetwork(prefix)
            .setAdministrativeCost(route.getDistance())
            .setMetric(route.getMetric())
            .setTag(firstNonNull(route.getTag(), Route.UNSET_ROUTE_TAG))
            .setNonForwarding(noInstall)
            .setRecursive(resolve);
    if (route.getDrop() || noInstall) {
      viStaticRoutes.add(rBuilder.setNextHop(NextHopDiscard.instance()).build());
    } else if (nextVrf != null) {
      viStaticRoutes.add(rBuilder.setNextHop(NextHopVrf.of(nextVrf)).build());
    } else if (!route.getNextHopInterface().isEmpty() || !route.getNextHopIp().isEmpty()) {
      for (String nhInt : route.getNextHopInterface()) {
        viStaticRoutes.add(rBuilder.setNextHop(NextHopInterface.of(nhInt)).build());
      }
      for (Ip nhIp : route.getNextHopIp()) {
        viStaticRoutes.add(rBuilder.setNextHop(NextHopIp.of(nhIp)).build());
      }
    }

    // populating static routes from each qualified next hop while overriding applicable properties
    for (QualifiedNextHop qualNextHop : route.getQualifiedNextHops().values()) {
      org.batfish.datamodel.StaticRoute.Builder qrBuilder =
          org.batfish.datamodel.StaticRoute.builder()
              .setNetwork(prefix)
              .setAdministrativeCost(route.getDistance())
              .setMetric(route.getMetric())
              .setTag(firstNonNull(route.getTag(), Route.UNSET_ROUTE_TAG))
              .setNonForwarding(noInstall)
              .setRecursive(resolve);

      qrBuilder.setNextHop(
          noInstall
              ? NextHopDiscard.instance()
              : NextHop.legacyConverter(
                  qualNextHop.getNextHop().getNextHopInterface(),
                  qualNextHop.getNextHop().getNextHopIp()));
      Optional.ofNullable(qualNextHop.getPreference()).ifPresent(qrBuilder::setAdministrativeCost);
      Optional.ofNullable(qualNextHop.getMetric()).ifPresent(qrBuilder::setMetric);
      Optional.ofNullable(qualNextHop.getTag()).ifPresent(qrBuilder::setTag);
      viStaticRoutes.add(qrBuilder.build());
    }
    return viStaticRoutes.build();
  }

  @VisibleForTesting
  static RouteFilterList toRouteFilterList(PrefixList prefixList, String vendorConfigFilename) {
    List<org.batfish.datamodel.RouteFilterLine> lines =
        prefixList.getPrefixes().stream()
            .map(
                prefix ->
                    new org.batfish.datamodel.RouteFilterLine(
                        LineAction.PERMIT, prefix, SubRange.singleton(prefix.getPrefixLength())))
            .collect(ImmutableList.toImmutableList());
    return new RouteFilterList(
        prefixList.getName(),
        lines,
        new VendorStructureId(
            vendorConfigFilename,
            JuniperStructureType.PREFIX_LIST.getDescription(),
            prefixList.getName()));
  }

  @Override
  public List<Configuration> toVendorIndependentConfigurations() throws VendorConversionException {
    ImmutableList.Builder<Configuration> outputConfigurations = ImmutableList.builder();
    _logicalSystems.keySet().stream()
        .map(this::toVendorIndependentConfiguration)
        .forEach(outputConfigurations::add);
    outputConfigurations.add(toVendorIndependentConfiguration());
    return outputConfigurations.build();
  }

  /** Creates and returns a vendor-independent configuration for the named logical-system. */
  private Configuration toVendorIndependentConfiguration(@Nullable String logicalSystemName) {
    JuniperConfiguration lsConfig = cloneConfiguration();
    lsConfig.processLogicalSystemConfiguration(logicalSystemName, this);
    return lsConfig.toVendorIndependentConfiguration();
  }

  private void processLogicalSystemConfiguration(
      @Nonnull String logicalSystemName, @Nonnull JuniperConfiguration masterConfiguration) {
    // Note that 'this' is the cloned configuration

    LogicalSystem ls = _logicalSystems.get(logicalSystemName);

    // Delete logical systems since they are no longer in scope
    _logicalSystems.clear();

    // Apply logical system settings onto cloned master
    // TODO: review which structures are shadowed, and which replaced entirely.
    _masterLogicalSystem.getApplications().putAll(ls.getApplications());
    _masterLogicalSystem.getApplicationSets().putAll(ls.getApplicationSets());
    _masterLogicalSystem.getAsPathGroups().putAll(ls.getAsPathGroups());
    // inherited?
    _masterLogicalSystem.getAuthenticationKeyChains().putAll(ls.getAuthenticationKeyChains());
    _masterLogicalSystem.getNamedCommunities().putAll(ls.getNamedCommunities());
    _masterLogicalSystem.setDefaultAddressSelection(ls.getDefaultAddressSelection());
    if (ls.getDefaultCrossZoneAction() != null) {
      _masterLogicalSystem.setDefaultCrossZoneAction(ls.getDefaultCrossZoneAction());
    }
    if (ls.getDefaultInboundAction() != null) {
      _masterLogicalSystem.setDefaultInboundAction(ls.getDefaultInboundAction());
    }
    _masterLogicalSystem.setDefaultRoutingInstance(ls.getDefaultRoutingInstance());
    _masterLogicalSystem.getDnsServers().clear();
    _masterLogicalSystem.getDnsServers().addAll(ls.getDnsServers());
    _masterLogicalSystem.getFirewallFilters().putAll(ls.getFirewallFilters());
    _masterLogicalSystem.getSecurityPolicies().putAll(ls.getSecurityPolicies());
    _masterLogicalSystem.getAddressBooks().putAll(ls.getAddressBooks());
    _masterLogicalSystem.getIkeGateways().clear();
    _masterLogicalSystem.getIkeGateways().putAll(ls.getIkeGateways());
    _masterLogicalSystem.getIkePolicies().clear();
    _masterLogicalSystem.getIkePolicies().putAll(ls.getIkePolicies());
    _masterLogicalSystem.getIkeProposals().clear();
    _masterLogicalSystem.getIkeProposals().putAll(ls.getIkeProposals());
    ls.getInterfaces()
        .forEach(
            (ifaceName, lsMasterIface) -> {
              Interface masterPhysicalInterface =
                  _masterLogicalSystem.getInterfaces().get(ifaceName);
              if (masterPhysicalInterface == null) {
                // the physical interface is not mentioned globally, so just copy the whole thing
                // from the logical system
                _masterLogicalSystem.getInterfaces().put(ifaceName, lsMasterIface);
                return;
              }
              // copy units from logical system
              masterPhysicalInterface.getUnits().putAll(lsMasterIface.getUnits());
              // delete unassigned units
              masterPhysicalInterface
                  .getUnits()
                  .keySet()
                  .retainAll(lsMasterIface.getUnits().keySet());
              // reset parent on copied units to master physical interface
              masterPhysicalInterface
                  .getUnits()
                  .values()
                  .forEach(unit -> unit.setParent(masterPhysicalInterface));
            });
    // delete unassigned interfaces
    _masterLogicalSystem.getInterfaces().keySet().retainAll(ls.getInterfaces().keySet());
    // TODO: review SRX logical-systems zone semantics
    _masterLogicalSystem.getInterfaceZones().clear();
    _masterLogicalSystem.getInterfaceZones().putAll(ls.getInterfaceZones());
    _masterLogicalSystem.getIpsecPolicies().clear();
    _masterLogicalSystem.getIpsecPolicies().putAll(ls.getIpsecPolicies());
    _masterLogicalSystem.getIpsecProposals().clear();
    _masterLogicalSystem.getIpsecProposals().putAll(ls.getIpsecProposals());
    _masterLogicalSystem.setNatDestination(ls.getNatDestination());
    _masterLogicalSystem.setNatSource(ls.getNatSource());
    _masterLogicalSystem.setNatStatic(ls.getNatStatic());
    // TODO: something with NTP servers?
    _masterLogicalSystem.getPolicyStatements().putAll(ls.getPolicyStatements());
    _masterLogicalSystem.getConditions().putAll(ls.getConditions());
    _masterLogicalSystem.getPrefixLists().putAll(ls.getPrefixLists());
    _masterLogicalSystem.getRouteFilters().putAll(ls.getRouteFilters());
    _masterLogicalSystem.getRoutingInstances().clear();
    _masterLogicalSystem.getRoutingInstances().putAll(ls.getRoutingInstances());
    // TODO: something with syslog hosts?
    // TODO: something with tacplus servers?
    _masterLogicalSystem.getNamedVlans().clear();
    _masterLogicalSystem.getNamedVlans().putAll(ls.getNamedVlans());
    if (ls.getEvpn() != null) {
      _masterLogicalSystem.setEvpn(ls.getEvpn());
    }
    _masterLogicalSystem.getZones().clear();
    _masterLogicalSystem.getZones().putAll(ls.getZones());

    // Ensure unique hostname in case one has not been configured
    if (getHostname() == null) {
      setHostname(
          computeLogicalSystemDefaultHostname(
              masterConfiguration.getHostname(), logicalSystemName));
    }
  }

  public static String computeLogicalSystemDefaultHostname(
      String masterHostname, String logicalSystemName) {
    return String.format("%s~logical_system~%s", masterHostname, logicalSystemName).toLowerCase();
  }

  private @Nonnull JuniperConfiguration cloneConfiguration() {
    JuniperConfiguration clonedConfiguration = SerializationUtils.clone(this);
    clonedConfiguration.setUnrecognized(getUnrecognized());
    clonedConfiguration.setWarnings(_w);
    return clonedConfiguration;
  }

  private Configuration toVendorIndependentConfiguration() throws VendorConversionException {
    String hostname = getHostname();
    _c = new Configuration(hostname, _vendor);
    _c.setAuthenticationKeyChains(
        convertAuthenticationKeyChains(_masterLogicalSystem.getAuthenticationKeyChains()));
    _c.setDnsServers(_masterLogicalSystem.getDnsServers());
    _c.setDomainName(_masterLogicalSystem.getDefaultRoutingInstance().getDomainName());
    _c.setLoggingServers(_masterLogicalSystem.getSyslogHosts());
    _c.setNtpServers(_masterLogicalSystem.getNtpServers());
    _c.setTacacsServers(_masterLogicalSystem.getTacplusServers());
    _c.getVendorFamily().setJuniper(_masterLogicalSystem.getJf());
    _c.setDeviceModel(DeviceModel.JUNIPER_UNSPECIFIED);
    for (String riName : _masterLogicalSystem.getRoutingInstances().keySet()) {
      _c.getVrfs().put(riName, new Vrf(riName));
    }

    // process interface ranges. this changes the _interfaces map
    _masterLogicalSystem.expandInterfaceRanges();

    // convert prefix lists to route filter lists
    for (Entry<String, PrefixList> e : _masterLogicalSystem.getPrefixLists().entrySet()) {
      _c.getRouteFilterLists().put(e.getKey(), toRouteFilterList(e.getValue(), _filename));
    }

    // Convert AddressBooks to IpSpaces
    _masterLogicalSystem
        .getAddressBooks()
        .forEach(
            (name, addressBook) -> {
              Map<String, IpSpace> ipspaces = toIpSpaces(name, addressBook);
              _c.getIpSpaces().putAll(ipspaces);
              ipspaces
                  .keySet()
                  .forEach(
                      ipSpaceName ->
                          _c.getIpSpaceMetadata()
                              .put(
                                  ipSpaceName,
                                  new IpSpaceMetadata(
                                      ipSpaceName, ADDRESS_BOOK.getDescription(), null)));
            });

    // Preprocess filters to do things like handle IPv6, combine filter input-/output-lists
    preprocessFilters();
    convertFirewallFiltersToIpAccessLists();

    // Convert security policies.
    convertSecurityPoliciesToIpAccessLists();

    // convert firewall filters implementing packet policy to PacketPolicy objects
    for (Entry<String, FirewallFilter> e : _masterLogicalSystem.getFirewallFilters().entrySet()) {
      String name = e.getKey();
      FirewallFilter filter = e.getValue();
      if (filter.isUsedForFBF()) {
        // TODO: support other filter families
        if (filter.getFamily() != Family.INET) {
          continue;
        }
        _c.getPacketPolicies().put(name, toPacketPolicy(filter));
      }
    }

    // convert route filters to route filter lists
    for (Entry<String, RouteFilter> e : _masterLogicalSystem.getRouteFilters().entrySet()) {
      String name = e.getKey();
      RouteFilter rf = e.getValue();
      if (rf.getIpv4()) {
        RouteFilterList rfl = new RouteFilterList(name);
        for (RouteFilterLine line : rf.getLines()) {
          if (line instanceof Route4FilterLine && line.getThens().isEmpty()) {
            ((Route4FilterLine) line).applyTo(rfl);
          }
        }
        _c.getRouteFilterLists().put(name, rfl);
      }
    }

    convertNamedCommunities();

    // convert interfaces. Before policies because some policies depend on interfaces
    convertInterfaces();

    // convert conditions to TrackMethod objects
    _masterLogicalSystem
        .getConditions()
        .forEach(
            (conditionName, condition) ->
                _c.getTrackingGroups()
                    .put(computeConditionTrackName(conditionName), toTrackMethod(condition)));

    // convert policy-statements to RoutingPolicy objects
    for (Entry<String, PolicyStatement> e : _masterLogicalSystem.getPolicyStatements().entrySet()) {
      String name = e.getKey();
      PolicyStatement ps = e.getValue();
      RoutingPolicy routingPolicy = toRoutingPolicy(ps);
      _c.getRoutingPolicies().put(name, routingPolicy);
    }

    // set router-id
    if (_masterLogicalSystem.getDefaultRoutingInstance().getRouterId() == null) {
      Interface loopback0 =
          _masterLogicalSystem
              .getDefaultRoutingInstance()
              .getInterfaces()
              .get(FIRST_LOOPBACK_INTERFACE_NAME);
      if (loopback0 != null) {
        Interface loopback0unit0 = loopback0.getUnits().get(FIRST_LOOPBACK_INTERFACE_NAME + ".0");
        if (loopback0unit0 != null) {
          ConcreteInterfaceAddress address = loopback0unit0.getPrimaryAddress();
          if (address != null) {
            // now we should set router-id
            Ip routerId = address.getIp();
            _masterLogicalSystem.getDefaultRoutingInstance().setRouterId(routerId);
          }
        }
      }
    }

    _masterLogicalSystem
        .getIkeProposals()
        .values()
        .forEach(
            ikeProposal ->
                _c.getIkePhase1Proposals()
                    .put(ikeProposal.getName(), toIkePhase1Proposal(ikeProposal)));

    ImmutableSortedMap.Builder<String, IkePhase1Key> ikePhase1KeysBuilder =
        ImmutableSortedMap.naturalOrder();

    // convert ike policies
    for (Entry<String, IkePolicy> e : _masterLogicalSystem.getIkePolicies().entrySet()) {
      String name = e.getKey();
      IkePolicy oldIkePolicy = e.getValue();
      // storing IKE phase 1 policy
      _c.getIkePhase1Policies().put(name, toIkePhase1Policy(oldIkePolicy, ikePhase1KeysBuilder));
    }

    _c.setIkePhase1Keys(ikePhase1KeysBuilder.build());

    // convert ipsec proposals
    ImmutableSortedMap.Builder<String, IpsecPhase2Proposal> ipsecPhase2ProposalsBuilder =
        ImmutableSortedMap.naturalOrder();
    _masterLogicalSystem
        .getIpsecProposals()
        .forEach(
            (ipsecProposalName, ipsecProposal) -> {
              ipsecPhase2ProposalsBuilder.put(
                  ipsecProposalName, toIpsecPhase2Proposal(ipsecProposal));
            });
    _c.setIpsecPhase2Proposals(ipsecPhase2ProposalsBuilder.build());

    // convert ipsec policies
    ImmutableSortedMap.Builder<String, IpsecPhase2Policy> ipsecPhase2PoliciesBuilder =
        ImmutableSortedMap.naturalOrder();
    for (Entry<String, IpsecPolicy> e : _masterLogicalSystem.getIpsecPolicies().entrySet()) {
      ipsecPhase2PoliciesBuilder.put(e.getKey(), toIpsecPhase2Policy(e.getValue()));
    }
    _c.setIpsecPhase2Policies(ipsecPhase2PoliciesBuilder.build());

    // convert Tunnels
    ImmutableSortedMap.Builder<String, IpsecPeerConfig> ipsecPeerConfigBuilder =
        ImmutableSortedMap.naturalOrder();
    for (Entry<String, IpsecVpn> e : _masterLogicalSystem.getIpsecVpns().entrySet()) {
      IpsecPeerConfig ipsecPeerConfig = toIpsecPeerConfig(e.getValue());
      if (ipsecPeerConfig != null) {
        ipsecPeerConfigBuilder.put(e.getKey(), ipsecPeerConfig);
      }
    }
    _c.setIpsecPeerConfigs(ipsecPeerConfigBuilder.build());

    // zones
    for (Zone zone : _masterLogicalSystem.getZones().values()) {
      org.batfish.datamodel.Zone newZone = toZone(zone);
      _c.getZones().put(zone.getName(), newZone);
      if (zone.getAddressBookType() == AddressBookType.INLINED) {
        Map<String, IpSpace> ipSpaces = toIpSpaces(zone.getName(), zone.getAddressBook());
        _c.getIpSpaces().putAll(ipSpaces);
        ipSpaces
            .keySet()
            .forEach(
                ipSpaceName ->
                    _c.getIpSpaceMetadata()
                        .put(
                            ipSpaceName,
                            new IpSpaceMetadata(ipSpaceName, ADDRESS_BOOK.getDescription(), null)));
      }
    }

    // default zone behavior
    _c.setDefaultCrossZoneAction(_masterLogicalSystem.getDefaultCrossZoneAction());
    _c.setDefaultInboundAction(_masterLogicalSystem.getDefaultInboundAction());

    for (Entry<String, RoutingInstance> e : _masterLogicalSystem.getRoutingInstances().entrySet()) {
      String riName = e.getKey();
      RoutingInstance ri = e.getValue();
      Vrf vrf = _c.getVrfs().get(riName);

      // dhcp relay
      for (Entry<String, DhcpRelayGroup> e2 : ri.getDhcpRelayGroups().entrySet()) {
        DhcpRelayGroup rg = e2.getValue();
        List<org.batfish.datamodel.Interface> interfaces = new ArrayList<>();
        if (rg.getAllInterfaces()) {
          interfaces.addAll(_c.getAllInterfaces().values());
        } else {
          rg.getInterfaces().stream()
              .map(_c.getAllInterfaces()::get)
              .filter(Objects::nonNull)
              .forEach(interfaces::add);
        }
        String asgName = rg.getActiveServerGroup();
        if (asgName != null) {
          DhcpRelayServerGroup asg = ri.getDhcpRelayServerGroups().get(asgName);
          if (asg != null) {
            for (org.batfish.datamodel.Interface iface : interfaces) {
              iface.setDhcpRelayAddresses(
                  ImmutableList.<Ip>builder()
                      .addAll(iface.getDhcpRelayAddresses())
                      .addAll(asg.getServers())
                      .build());
            }
          }
        }
      }

      // snmp
      SnmpServer snmpServer = ri.getSnmpServer();
      if (snmpServer != null) {
        snmpServer.getCommunities().values().forEach(this::populateCommunityClientIps);
        vrf.setSnmpServer(snmpServer);
        _c.getSnmpTrapServers().addAll(snmpServer.getHosts().keySet());
      }

      // static routes
      for (StaticRouteV4 route : ri.getRibs().get(RIB_IPV4_UNICAST).getStaticRoutes().values()) {
        vrf.getStaticRoutes().addAll(toStaticRoutes(route));
      }

      // aggregate routes
      for (AggregateRoute route :
          ri.getRibs().get(RIB_IPV4_UNICAST).getAggregateRoutes().values()) {
        route.inheritUnsetFields(ri.getAggregateRouteDefaults());
        org.batfish.datamodel.GeneratedRoute newAggregateRoute = toAggregateRoute(route);
        vrf.getGeneratedRoutes().add(newAggregateRoute);
      }

      // generated routes
      for (GeneratedRoute route :
          ri.getRibs().get(RIB_IPV4_UNICAST).getGeneratedRoutes().values()) {
        route.inheritUnsetFields(ri.getGeneratedRouteDefaults());
        org.batfish.datamodel.GeneratedRoute newGeneratedRoute = toGeneratedRoute(route);
        vrf.getGeneratedRoutes().add(newGeneratedRoute);
      }

      // Set up import policy for cross-VRF route leaking using instance-import
      // At this point configured policy-statements have already been added to _c as RoutingPolicies
      if (!ri.getInstanceImports().isEmpty()) {
        // Only routes from these VRFs will be considered for import
        List<String> referencedVrfs = getVrfsReferencedByPolicies(ri.getInstanceImports());
        initDefaultRejectPolicy();
        RoutingPolicy instanceImportPolicy = buildInstanceImportRoutingPolicy(ri, _c, riName);
        for (String referencedVrf : referencedVrfs) {
          getOrInitVrfLeakConfig(vrf)
              .addMainRibVrfLeakConfig(
                  MainRibVrfLeakConfig.builder()
                      .setImportFromVrf(referencedVrf)
                      .setImportPolicy(instanceImportPolicy.getName())
                      .build());
        }
        _c.getRoutingPolicies().put(instanceImportPolicy.getName(), instanceImportPolicy);
      }

      /*
       * RIB groups applied to each protocol.
       *
       * 1. ensure default import policies exist
       * 2. convert VS rib groups to VI rib groups on a per-protocol basis
       */
      if (!ri.getAppliedRibGroups().isEmpty()) {
        initDefaultImportPolicies();
      }
      vrf.setAppliedRibGroups(
          ri.getAppliedRibGroups().entrySet().stream()
              .collect(
                  ImmutableMap.toImmutableMap(
                      Entry::getKey, // protocol
                      rgEntry ->
                          toRibGroup(
                              _masterLogicalSystem.getRibGroups().get(rgEntry.getValue()),
                              rgEntry.getKey(),
                              _c,
                              riName,
                              _w))));

      // Create OSPF process (oproc will be null iff disable is configured at process level)
      if (!ri.getOspfAreas().isEmpty()) {
        OspfProcess oproc = createOspfProcess(ri);
        if (oproc != null) {
          vrf.setOspfProcesses(ImmutableSortedMap.of(oproc.getProcessId(), oproc));
        }
      }

      // create is-is process
      // is-is runs only if at least one interface has an ISO address, check loopback first
      Optional<IsoAddress> isoAddress =
          _masterLogicalSystem.getDefaultRoutingInstance().getInterfaces().values().stream()
              .filter(i -> i.getName().startsWith(FIRST_LOOPBACK_INTERFACE_NAME))
              .map(Interface::getIsoAddress)
              .filter(Objects::nonNull)
              .min(Comparator.comparing(IsoAddress::toString));
      // Try all the other interfaces if no ISO address on Loopback
      if (!isoAddress.isPresent()) {
        isoAddress =
            _masterLogicalSystem.getDefaultRoutingInstance().getInterfaces().values().stream()
                .map(Interface::getIsoAddress)
                .filter(Objects::nonNull)
                .min(Comparator.comparing(IsoAddress::toString));
      }
      if (isoAddress.isPresent()) {
        // now we should create is-is process
        IsisProcess proc = createIsisProcess(ri, isoAddress.get());
        vrf.setIsisProcess(proc);
      }

      // create bgp process
      if (!ri.getNamedBgpGroups().isEmpty() || !ri.getIpBgpGroups().isEmpty()) {
        BgpProcess proc = createBgpProcess(ri);
        vrf.setBgpProcess(proc);
      }
      if (_masterLogicalSystem.getDefaultAddressSelection()) {
        // When local IP is not explicitly set, the source address is dynamically picked based on
        // the outgoing interface (done in VI land, so we don't need to do anything more here).
        // The exception to this behavior occurs for iBGP and eBGP-multihop sessions when
        // default-address-selection is set.
        getDefaultSourceAddress(ri, _c)
            .ifPresent(ip -> vrf.setSourceIpInference(UseConstantIp.create(ip)));
      }
      convertResolution(ri);
    }

    // static nats
    if (_masterLogicalSystem.getNatStatic() != null) {
      _w.unimplemented("Static NAT is not currently implemented");
    }

    // Count and mark structure usages and identify undefined references
    JuniperStructureType.CONCRETE_STRUCTURES.forEach(this::markConcreteStructure);
    JuniperStructureType.ABSTRACT_STRUCTURES.asMap().forEach(this::markAbstractStructureAllUsages);

    warnEmptyPrefixLists();

    _c.computeRoutingPolicySources(_w);

    // convert vxlan.
    convertVxlan();

    return _c;
  }

  private @Nonnull TrackMethod toTrackMethod(Condition condition) {
    IfRouteExists ifr = condition.getIfRouteExists();
    if (ifr == null) {
      // TODO: verify empty condition means true
      return TrackMethods.alwaysTrue();
    }
    Prefix prefix = ifr.getPrefix();
    if (prefix == null) {
      if (ifr.getPrefix6() != null) {
        return TrackMethods.alwaysFalse();
      }
      _w.fatalRedFlag(
          "Missing route address for if-route-exists condition %s. Config will not pass commit"
              + " checks.",
          condition.getName());
      // TODO: verify missing prefix means true
      return TrackMethods.alwaysTrue();
    }
    String table = ifr.getTable();
    String vrf;
    if (table == null) {
      vrf = Configuration.DEFAULT_VRF_NAME;
    } else {
      RibId ribId = toRibId(getHostname(), table, _w);
      if (ribId == null) {
        // unsupported RIB type, already warned in toRibId
        return TrackMethods.alwaysTrue();
      }
      vrf = ribId.getVrfName();
    }
    return TrackMethods.route(prefix, ImmutableSet.of(), vrf);
  }

  private void applyBridgeDomainVlanIds(Map<String, Integer> irbVlanIds) {
    Stream.concat(
            Stream.of(_masterLogicalSystem.getDefaultRoutingInstance()),
            _masterLogicalSystem.getRoutingInstances().values().stream())
        .forEach(
            ri ->
                ri.getBridgeDomains()
                    .forEach((bdName, bd) -> applyBridgeDomainVlanId(bd, irbVlanIds)));
  }

  private void applyBridgeDomainVlanId(BridgeDomain bd, Map<String, Integer> irbVlanIds) {
    BridgeDomainVlanId vlanId = bd.getVlanId();
    if (vlanId == null) {
      return;
    }
    String routingInterfaceName = bd.getRoutingInterface();
    if (routingInterfaceName == null) {
      return;
    }
    // TODO: optimize
    new BridgeDomainVlanIdVoidVisitor() {
      @Override
      public void visitBridgeDomainVlanIdAll() {
        // nothing to do for now
      }

      @Override
      public void visitBridgeDomainVlanIdNone() {
        // nothing to do for now
      }

      @Override
      public void visitBridgeDomainVlanIdNumber(BridgeDomainVlanIdNumber bridgeDomainVlanIdNumber) {
        int vlan = bridgeDomainVlanIdNumber.getVlan();
        // TODO: Need to determine whether a physical interface is necessary. If so, need to process
        //       several other configuration constructs, at least:
        //       Method 1:
        //       - interfaces <name> unit <num> family bridge interface-mode trunk
        //       - interfaces <name> unit <num> family bridge vlan-id-list <vlan-num>
        //       Method 2:
        //       - interfaces <name> encapsulation extended-vlan-bridge
        //       - interfaces <name> unit <num> vlan-id <vlan-num>
        //       Method 3:
        //       - routing-options bridge-domains <name> interface
        //       Until then, we disable autostate by modifying normal vlan range.
        irbVlanIds.put(routingInterfaceName, vlan);
        _c.setNormalVlanRange(_c.getNormalVlanRange().difference(IntegerSpace.of(vlan)));
      }
    }.visit(vlanId);
  }

  private static @Nonnull VrfLeakConfig getOrInitVrfLeakConfig(Vrf vrf) {
    if (vrf.getVrfLeakConfig() == null) {
      vrf.setVrfLeakConfig(new VrfLeakConfig(false));
    }
    return vrf.getVrfLeakConfig();
  }

  private void convertResolution(RoutingInstance ri) {
    Resolution resolution = ri.getResolution();
    if (resolution == null) {
      return;
    }
    ResolutionRib rib = resolution.getRib();
    if (rib == null) {
      return;
    }
    if (!rib.getName().equals(RIB_IPV4_UNICAST)) {
      // TODO: support other resolution ribs
      return;
    }
    List<BooleanExpr> policyCalls =
        rib.getImportPolicies().stream()
            .map(CallExpr::new)
            .collect(ImmutableList.toImmutableList());

    String policyName = generateResolutionRibImportPolicyName(ri.getName());
    RoutingPolicy.builder()
        .setOwner(_c)
        .setName(policyName)
        .setStatements(
            ImmutableList.of(
                // Add default policy
                new SetDefaultPolicy(DEFAULT_REJECT_POLICY_NAME),
                // Construct a policy chain based on defined import policies
                new If(
                    new FirstMatchChain(policyCalls),
                    ImmutableList.of(Statements.ReturnTrue.toStaticStatement()),
                    ImmutableList.of(ReturnFalse.toStaticStatement()))))
        .build();
    _c.getVrfs().get(ri.getName()).setResolutionPolicy(policyName);
  }

  private void convertFirewallFiltersToIpAccessLists() {
    for (Entry<String, FirewallFilter> e : _masterLogicalSystem.getFirewallFilters().entrySet()) {
      String name = e.getKey();
      FirewallFilter filter = e.getValue();
      // TODO: support other filter families
      if (filter.getFamily() != Family.INET) {
        continue;
      }
      IpAccessList list = filterToIpAccessList(filter);
      _c.getIpAccessLists().put(name, list);
    }
  }

  private void convertSecurityPoliciesToIpAccessLists() {
    for (ConcreteFirewallFilter filter : _masterLogicalSystem.getSecurityPolicies().values()) {
      // TODO: support other filter families
      if (filter.getFamily() != Family.INET) {
        continue;
      }
      IpAccessList acl = securityPolicyToIpAccessList(filter);
      _c.getIpAccessLists().put(acl.getName(), acl);
    }
  }

  private void populateCommunityClientIps(SnmpCommunity community) {
    String clientListName = community.getAccessList();
    if (clientListName == null) {
      return;
    }
    // Could be declared in VS as a prefix-list or an SNMP client-list. Prefer the latter.
    PrefixList pl = _masterLogicalSystem.getSnmpClientLists().get(clientListName);
    if (pl == null) {
      pl = _masterLogicalSystem.getPrefixLists().get(clientListName);
    }
    if (pl == null) {
      // Unreferenced error elsewhere.
      return;
    }
    community.setClientIps(
        AclIpSpace.union(
            pl.getPrefixes().stream().map(Prefix::toIpSpace).collect(Collectors.toList())));
  }

  private void preprocessFilters() {
    _masterLogicalSystem.getInterfaces().values().stream()
        .flatMap(i -> Stream.concat(Stream.of(i), i.getUnits().values().stream()))
        .forEach(this::handleFilterLists);

    // Remove ipv6 lines from firewall filters
    //    TODO: instead make both IpAccessList and Ip6AccessList instances
    for (FirewallFilter aFilter : _masterLogicalSystem.getFirewallFilters().values()) {
      if (aFilter instanceof CompositeFirewallFilter) {
        // Terms will be handled transitively.
        continue;
      }
      assert aFilter instanceof ConcreteFirewallFilter;
      ConcreteFirewallFilter filter = (ConcreteFirewallFilter) aFilter;

      Set<String> toRemove =
          filter.getTerms().entrySet().stream()
              .filter(entry -> entry.getValue().getIpv6())
              .map(Entry::getKey)
              .collect(ImmutableSet.toImmutableSet());

      for (String termName : toRemove) {
        filter.getTerms().remove(termName);
      }
    }

    // remove empty firewall filters (ipv6-only filters)
    Map<String, FirewallFilter> allFilters =
        new LinkedHashMap<>(_masterLogicalSystem.getFirewallFilters());
    for (Entry<String, FirewallFilter> e : allFilters.entrySet()) {
      String name = e.getKey();
      FirewallFilter aFilter = e.getValue();
      if (aFilter instanceof CompositeFirewallFilter) {
        continue;
      }
      assert aFilter instanceof ConcreteFirewallFilter;
      ConcreteFirewallFilter filter = (ConcreteFirewallFilter) aFilter;
      if (filter.getTerms().isEmpty()) {
        _masterLogicalSystem.getFirewallFilters().remove(name);
      }
    }
  }

  /** Converts a filter input-list or output-list into a {@link FirewallFilter}. */
  private void handleFilterLists(Interface i) {
    if (i.getIncomingFilterList() != null) {
      i.setIncomingFilter(
          generateCompositeInterfaceFilter(i.getIncomingFilterList(), i.getName() + "-i"));
    }
    if (i.getOutgoingFilterList() != null) {
      i.setOutgoingFilter(
          generateCompositeInterfaceFilter(i.getOutgoingFilterList(), i.getName() + "-o"));
    }
  }

  /**
   * Generates a {@link FirewallFilter} and stores it in the {@link #_masterLogicalSystem}. Returns
   * the name of the generated filter.
   */
  private @Nonnull String generateCompositeInterfaceFilter(
      @Nonnull List<String> outgoingFilterList, @Nonnull String name) {
    List<FirewallFilter> filtered =
        outgoingFilterList.stream()
            .map(fname -> _masterLogicalSystem.getFirewallFilters().get(fname))
            .filter(Objects::nonNull) // undefined ref
            .collect(ImmutableList.toImmutableList());
    FirewallFilter filter;
    if (!filtered.isEmpty()) {
      filter = new CompositeFirewallFilter(name, filtered);
    } else {
      // No defined terms, instead generate an empty filter which will default-deny.
      filter = new ConcreteFirewallFilter(name, Family.INET);
    }
    _masterLogicalSystem.getFirewallFilters().put(filter.getName(), filter);
    return filter.getName();
  }

  /** Initialize default protocol-specific import policies */
  private void initDefaultImportPolicies() {
    initDefaultBgpImportPolicy();
    initDefaultPseudoProtocolImportPolicy();
  }

  private @Nonnull Optional<Interface> getInterfaceOrUnitByName(@Nonnull String name) {
    for (Interface i : _masterLogicalSystem.getInterfaces().values()) {
      if (name.equals(i.getName())) {
        return Optional.of(i);
      }
      for (Interface u : i.getUnits().values()) {
        if (name.equals(u.getName())) {
          return Optional.of(u);
        }
      }
    }
    return Optional.empty();
  }

  private void convertInterfaces() {
    Map<String, Integer> irbVlanIds = computeIrbVlanIds();

    // Get a stream of all interfaces (including Node interfaces)
    Stream.concat(
            _masterLogicalSystem.getInterfaces().values().stream(),
            _nodeDevices.values().stream()
                .flatMap(nodeDevice -> nodeDevice.getInterfaces().values().stream()))
        .forEach(
            /*
             * For each interface, add it to the VI model. Since Juniper splits attributes
             * between physical and logical (unit) interfaces, do the conversion in two steps.
             * - Physical interface first, with physical attributes: speed, aggregation tracking, etc.
             * - Then all units of the interface. Units have the attributes batfish
             *   cares most about: IPs, MTUs, ACLs, etc.)
             */
            iface -> {
              // Process parent interface
              iface.inheritUnsetFields();
              org.batfish.datamodel.Interface newParentIface = toInterfaceNonUnit(iface);
              if (newParentIface == null) {
                return;
              }
              resolveInterfacePointers(iface.getName(), iface, newParentIface);

              // Process the units, which hold the bulk of the configuration
              iface
                  .getUnits()
                  .values()
                  .forEach(
                      unit -> {
                        unit.inheritUnsetFields();
                        org.batfish.datamodel.Interface newUnitInterface = toInterface(unit);
                        if (newUnitInterface == null) {
                          return;
                        }
                        String name = newUnitInterface.getName();
                        // set IRB VLAN ID if assigned
                        newUnitInterface.setVlan(irbVlanIds.get(name));
                        if (unit.getType() == InterfaceType.IRB_UNIT
                            && newUnitInterface.getVlan() == null) {
                          // TODO: May still be active if part of a bridge, though maybe it still
                          //       needs a vlan.
                          _w.redFlagf("Deactivating %s because it has no assigned vlan", name);
                          newUnitInterface.deactivate(InactiveReason.INCOMPLETE);
                        }

                        // Don't create bind dependency for 'irb.XXX' interfcaes, since there isn't
                        // really an 'irb' interface
                        if (!name.startsWith("irb")) {
                          newUnitInterface.addDependency(
                              new Dependency(newParentIface.getName(), DependencyType.BIND));
                        }
                        resolveInterfacePointers(unit.getName(), unit, newUnitInterface);
                      });
            });

    /*
     * Do a second pass where we look over all interfaces
     * and set dependency pointers for aggregated interfaces in the VI configuration
     */
    Stream.concat(
            _masterLogicalSystem.getInterfaces().values().stream(),
            _nodeDevices.values().stream()
                .flatMap(nodeDevice -> nodeDevice.getInterfaces().values().stream()))
        .forEach(
            iface -> {
              if (iface.get8023adInterface() != null) {
                org.batfish.datamodel.Interface viIface =
                    _c.getAllInterfaces().get(iface.get8023adInterface());
                if (viIface == null) {
                  return;
                }
                viIface.addDependency(new Dependency(iface.getName(), DependencyType.AGGREGATE));
              }
              /*
               * TODO: reth interfaces are NOT aggregates in pure form, but for now approximate them
               * as such. Full support requires chassis clusters and redundancy group support.
               * https://www.juniper.net/documentation/en_US/junos/topics/topic-map/security-chassis-cluster-redundant-ethernet-interfaces.html
               */
              if (iface.getRedundantParentInterface() != null) {
                org.batfish.datamodel.Interface viIface =
                    _c.getAllInterfaces().get(iface.getRedundantParentInterface());
                if (viIface == null) {
                  return;
                }
                viIface.addDependency(new Dependency(iface.getName(), DependencyType.AGGREGATE));
              }
            });

    Nat snat = _masterLogicalSystem.getNatSource();
    Nat staticNat = _masterLogicalSystem.getNatStatic();

    if (snat == null && staticNat == null) {
      return;
    }

    List<NatRuleSet> sourceNatRuleSetList =
        snat == null
            ? null
            : snat.getRuleSets().values().stream()
                .sorted()
                .collect(ImmutableList.toImmutableList());

    Nat reversedStaticNat = staticNat == null ? null : ReverseStaticNat.reverseNat(staticNat);
    List<NatRuleSet> reversedStaticNatRuleSetList =
        reversedStaticNat == null
            ? null
            : reversedStaticNat.getRuleSets().values().stream()
                .sorted()
                .collect(ImmutableList.toImmutableList());

    Map<NatPacketLocation, AclLineMatchExpr> matchFromLocationExprs =
        fromNatPacketLocationMatchExprs();

    Stream.concat(
            _masterLogicalSystem.getInterfaces().values().stream(),
            _nodeDevices.values().stream()
                .flatMap(nodeDevice -> nodeDevice.getInterfaces().values().stream()))
        .flatMap(i -> i.getUnits().values().stream())
        .forEach(
            unit -> {
              org.batfish.datamodel.Interface newUnitInterface =
                  _c.getAllInterfaces().get(unit.getName());
              if (newUnitInterface == null) {
                // This can happen if the interface is used but not defined
                return;
              }
              Transformation srcTransformation =
                  buildOutgoingTransformation(
                      unit, snat, sourceNatRuleSetList, matchFromLocationExprs, null);
              Transformation staticTransformation =
                  buildOutgoingTransformation(
                      unit,
                      reversedStaticNat,
                      reversedStaticNatRuleSetList,
                      matchFromLocationExprs,
                      srcTransformation);
              newUnitInterface.setOutgoingTransformation(staticTransformation);
            });
  }

  private @Nonnull Map<String, Integer> computeIrbVlanIds() {
    // Set IRB vlan IDs by resolving l3-interface from named VLANs.
    // If more than one named vlan refers to a given l3-interface, we just keep the first assignment
    // and warn.
    Map<String, Integer> irbVlanIds = new HashMap<>();
    for (Vlan vlan : _masterLogicalSystem.getNamedVlans().values()) {
      // note: vlan-id-list is not valid on vlans with an L3 interface configured
      Integer vlanId = vlan.getVlanId();
      String l3Interface = vlan.getL3Interface();
      if (l3Interface == null || vlanId == null) {
        continue;
      }
      if (irbVlanIds.containsKey(l3Interface)) {
        _w.redFlagf(
            "Cannot assign '%s' as the l3-interface of vlan '%s' since it is already assigned"
                + " to vlan '%s'",
            l3Interface, vlanId, irbVlanIds.get(l3Interface));
        continue;
      }
      irbVlanIds.put(l3Interface, vlanId);
      for (String memberIfName : vlan.getInterfaces()) {
        Optional<Interface> optionalInterface = getInterfaceOrUnitByName(memberIfName);
        if (!optionalInterface.isPresent()) {
          continue;
        }
        Interface i = optionalInterface.get();
        EthernetSwitching es = i.getEthernetSwitching();
        if (es != null && (es.getSwitchportMode() != null || !es.getVlanMembers().isEmpty())) {
          _w.redFlagf(
              "Cannot assign '%s' as interface of vlan '%s' since it is already has vlan"
                  + " configuration under family ethernet-switching",
              memberIfName, vlanId);
          continue;
        }
        if (_indirectAccessPorts.containsKey(memberIfName)) {
          _w.redFlagf(
              "Cannot assign '%s' as interface of vlan '%s' since it is already interface of"
                  + " vlan '%s'",
              memberIfName, vlanId, _indirectAccessPorts.get(memberIfName).getName());
          continue;
        }
        _indirectAccessPorts.put(memberIfName, new VlanReference(vlan.getName()));
      }
    }
    applyBridgeDomainVlanIds(irbVlanIds);
    return irbVlanIds;
  }

  /** Ensure that the interface is placed in VI {@link Configuration} and {@link Vrf} */
  private void resolveInterfacePointers(
      String ifaceName, Interface iface, org.batfish.datamodel.Interface viIface) {
    Vrf vrf = viIface.getVrf();
    String vrfName = vrf.getName();
    _masterLogicalSystem.getRoutingInstances().get(vrfName).getInterfaces().put(ifaceName, iface);
    if (ifaceName.equals("irb")) {
      // there is no 'irb' interface; it is just a namespace with no inheritable parameters
      return;
    }
    _c.getAllInterfaces().put(ifaceName, viIface);
    if (viIface.getOwner() == null) {
      viIface.setOwner(_c);
    }
  }

  private org.batfish.datamodel.Zone toZone(Zone zone) {
    String zoneName = zone.getName();

    FirewallFilter inboundFilter = zone.getInboundFilter();
    IpAccessList inboundFilterList = null;
    if (inboundFilter != null) {
      inboundFilterList = _c.getIpAccessLists().get(inboundFilter.getName());
    }

    FirewallFilter fromHostFilter = zone.getFromHostFilter();
    IpAccessList fromHostFilterList = null;
    if (fromHostFilter != null) {
      fromHostFilterList = _c.getIpAccessLists().get(fromHostFilter.getName());
    }

    FirewallFilter toHostFilter = zone.getToHostFilter();
    IpAccessList toHostFilterList = null;
    if (toHostFilter != null) {
      toHostFilterList = _c.getIpAccessLists().get(toHostFilter.getName());
    }

    org.batfish.datamodel.Zone newZone = new org.batfish.datamodel.Zone(zoneName);
    if (fromHostFilterList != null) {
      newZone.setFromHostFilterName(fromHostFilterList.getName());
    }
    if (inboundFilterList != null) {
      newZone.setInboundFilterName(inboundFilterList.getName());
    }
    if (toHostFilterList != null) {
      newZone.setToHostFilterName(toHostFilterList.getName());
    }

    newZone.setInboundInterfaceFiltersNames(new TreeMap<>());
    for (Entry<String, ConcreteFirewallFilter> e : zone.getInboundInterfaceFilters().entrySet()) {
      String inboundInterfaceName = e.getKey();
      FirewallFilter inboundInterfaceFilter = e.getValue();
      String inboundInterfaceFilterName = inboundInterfaceFilter.getName();
      org.batfish.datamodel.Interface newIface = _c.getAllInterfaces().get(inboundInterfaceName);
      newZone.getInboundInterfaceFiltersNames().put(newIface.getName(), inboundInterfaceFilterName);
    }

    newZone.setToZonePoliciesNames(new TreeMap<>());
    for (Entry<String, ConcreteFirewallFilter> e : zone.getToZonePolicies().entrySet()) {
      String toZoneName = e.getKey();
      FirewallFilter toZoneFilter = e.getValue();
      String toZoneFilterName = toZoneFilter.getName();
      newZone.getToZonePoliciesNames().put(toZoneName, toZoneFilterName);
    }

    newZone.setInboundInterfaceFiltersNames(new TreeMap<>());
    for (String ifaceName : zone.getInterfaces()) {
      org.batfish.datamodel.Interface newIface = _c.getAllInterfaces().get(ifaceName);
      if (newIface == null) {
        // undefined reference to ifaceName
        continue;
      }
      newIface.setZoneName(zoneName);
      FirewallFilter inboundInterfaceFilter = zone.getInboundInterfaceFilters().get(ifaceName);
      if (inboundInterfaceFilter != null) {
        newZone
            .getInboundInterfaceFiltersNames()
            .put(newIface.getName(), inboundInterfaceFilter.getName());
      } else if (inboundFilterList != null) {
        newZone
            .getInboundInterfaceFiltersNames()
            .put(newIface.getName(), inboundFilterList.getName());
      }
    }

    return newZone;
  }

  private void warnEmptyPrefixLists() {
    for (Entry<String, PrefixList> e : _masterLogicalSystem.getPrefixLists().entrySet()) {
      String name = e.getKey();
      PrefixList prefixList = e.getValue();
      if (!prefixList.getHasIpv6() && prefixList.getPrefixes().isEmpty()) {
        _w.redFlag("Empty prefix-list: '" + name + "'");
      }
    }
  }

  /**
   * Detects potentially risky terminal actions in policy statements.
   *
   * <p>An unconditional terminal action (accept/reject/next-policy with no match conditions)
   * normally makes all subsequent terms unreachable, which is a common source of routing errors.
   *
   * <p>There is one acceptable exception: It's OK to have EXACTLY ONE unconditional terminal term
   * after an unconditional terminal term. For example: - ACCEPT with no match conditions, followed
   * by a REJECT with no match conditions - REJECT with no match conditions, followed by a NEXT
   * POLICY with no match conditions
   *
   * <p>All other cases are flagged as risky: - Unconditional terminal term followed by a
   * conditional term - Unconditional terminal term followed by a non-terminal term - Unconditional
   * terminal term followed by more than one term
   *
   * @param policy The policy statement to analyze
   */
  private void detectMisplacedTerminalActions(PolicyStatement policy) {
    List<PsTerm> terms = ImmutableList.copyOf(policy.getTerms().values());

    for (int i = 0; i < terms.size() - 1; i++) { // Skip last term
      PsTerm currentTerm = terms.get(i);

      // Check if term has no match conditions (matches everything)
      // A term with no match conditions and a terminal action can make subsequent terms unreachable
      if (!currentTerm.hasAtLeastOneFrom() && !currentTerm.hasAtLeastOneTo()) {

        Optional<PsThen> terminalAction = getTerminalAction(currentTerm);
        if (terminalAction.isPresent()) {

          // Special case: It's acceptable to have EXACTLY ONE unconditional terminal term
          // after an unconditional terminal term, with a single terminal "then", as default
          // terminal terms are common.
          if (i == terms.size() - 2) {
            PsTerm nextTerm = terms.get(i + 1);

            // Check if the next term is also unconditional
            if (!nextTerm.hasAtLeastOneFrom() && !nextTerm.hasAtLeastOneTo()) {

              // Check if the next term also has a terminal action
              Optional<PsThen> nextTerminalAction = getTerminalAction(nextTerm);
              if (nextTerminalAction.isPresent()) {
                // Check that the terminal action is the ONLY action.
                // If there are multiple thens, some are likely mutating actions that are skipped
                Set<PsThen> allThens = nextTerm.getThens().getAllThens();
                if (allThens.size() == 1) {
                  return;
                }
              }
            }
          }

          // If we reach here, it's not the acceptable case, so flag it as risky
          // Get the terminal action name for the warning
          String actionName = getTerminalActionName(terminalAction.get());

          // Calculate how many subsequent terms will be ignored
          int subsequentTermsCount = terms.size() - 1 - i;

          _w.riskyRedFlag(
              "'policy-statement %s term %s then %s' always ends processing, but there %s %d"
                  + " subsequent %s that will not be evaluated",
              policy.getName(),
              currentTerm.getName(),
              actionName,
              subsequentTermsCount == 1 ? "is" : "are",
              subsequentTermsCount,
              subsequentTermsCount == 1 ? "term" : "terms");
          // Just warn once per policy statement
          return;
        }
      }
    }
  }

  /** Get the terminal action from a term, if any */
  private Optional<PsThen> getTerminalAction(PsTerm term) {
    if (term.getThens() == null) {
      return Optional.empty();
    }

    return term.getThens().getAllThens().stream()
        .filter(
            then ->
                then instanceof PsThenAccept
                    || then instanceof PsThenReject
                    || then instanceof PsThenNextPolicy)
        .findFirst();
  }

  /** Get the Junos syntax name for a terminal action */
  private String getTerminalActionName(PsThen terminalAction) {
    if (terminalAction instanceof PsThenAccept) {
      return "accept";
    } else if (terminalAction instanceof PsThenReject) {
      return "reject";
    } else if (terminalAction instanceof PsThenNextPolicy) {
      return "next policy";
    } else {
      throw new IllegalArgumentException(
          "Unknown terminal action type: " + terminalAction.getClass().getSimpleName());
    }
  }

  /**
   * Figure out the router ID for a given {@link RoutingInstance}.
   *
   * <p>Returns either the explicitly set router id, an inferred one, or a {@link Ip#ZERO}
   *
   * <p>For logic, see <a
   * href="https://www.juniper.net/documentation/en_US/junos/topics/reference/configuration-statement/router-id-edit-routing-options.html">Juniper
   * router id page</a>
   */
  @VisibleForTesting
  static @Nonnull Ip getRouterId(RoutingInstance routingInstance) {
    Ip routerId = routingInstance.getRouterId();
    if (routerId != null) {
      return routerId;
    }

    Map<String, Interface> allInterfaces = routingInstance.getInterfaces();
    Map<String, Interface> loopbackInterfaces =
        allInterfaces.entrySet().stream()
            .filter(e -> e.getKey().toLowerCase().startsWith("lo"))
            .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
    Map<String, Interface> routerIdCandidates =
        loopbackInterfaces.isEmpty() ? allInterfaces : loopbackInterfaces;

    return routerIdCandidates.values().stream()
        .filter(Interface::getActive)
        .map(Interface::getPrimaryAddress)
        .filter(Objects::nonNull)
        .map(ConcreteInterfaceAddress::getIp)
        .min(Comparator.naturalOrder())
        .orElse(Ip.ZERO);
  }

  public @Nonnull Map<String, LogicalSystem> getLogicalSystems() {
    return _logicalSystems;
  }

  public LogicalSystem getMasterLogicalSystem() {
    return _masterLogicalSystem;
  }

  @Override
  public String getHostname() {
    return _masterLogicalSystem.getHostname();
  }

  @Override
  public void setHostname(String hostname) {
    _masterLogicalSystem.setHostname(hostname);
  }

  private static @Nullable String interfaceUnitMasterName(String unitName) {
    int pos = unitName.indexOf('.');
    if (pos <= 0) {
      return null;
    }
    String master = unitName.substring(0, pos);
    return master;
  }
}
