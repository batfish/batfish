package org.batfish.vendor.check_point_gateway.representation;

import static com.google.common.collect.Maps.immutableEntry;
import static org.apache.commons.lang3.ObjectUtils.firstNonNull;
import static org.batfish.common.util.CollectionUtil.toImmutableMap;
import static org.batfish.common.util.CommonUtil.forEachWithIndex;
import static org.batfish.datamodel.FirewallSessionInterfaceInfo.Action.POST_NAT_FIB_LOOKUP;
import static org.batfish.datamodel.acl.AclLineMatchExprs.and;
import static org.batfish.datamodel.acl.AclLineMatchExprs.matchDst;
import static org.batfish.datamodel.acl.AclLineMatchExprs.matchSrc;
import static org.batfish.datamodel.acl.AclLineMatchExprs.not;
import static org.batfish.datamodel.acl.AclLineMatchExprs.or;
import static org.batfish.datamodel.transformation.Transformation.always;
import static org.batfish.datamodel.transformation.Transformation.when;
import static org.batfish.vendor.check_point_gateway.representation.CheckPointGatewayConversions.aclName;
import static org.batfish.vendor.check_point_gateway.representation.CheckPointGatewayConversions.createClusterVrrpGroup;
import static org.batfish.vendor.check_point_gateway.representation.CheckPointGatewayConversions.toIpAccessLists;
import static org.batfish.vendor.check_point_gateway.representation.CheckpointNatConversions.HIDE_BEHIND_GATEWAY_IP;
import static org.batfish.vendor.check_point_gateway.representation.CheckpointNatConversions.automaticHideRuleTransformationStep;
import static org.batfish.vendor.check_point_gateway.representation.CheckpointNatConversions.automaticStaticRuleTransformationStep;
import static org.batfish.vendor.check_point_gateway.representation.CheckpointNatConversions.getApplicableNatRules;
import static org.batfish.vendor.check_point_gateway.representation.CheckpointNatConversions.isValidAutomaticHideRule;
import static org.batfish.vendor.check_point_gateway.representation.CheckpointNatConversions.isValidAutomaticStaticRule;
import static org.batfish.vendor.check_point_gateway.representation.CheckpointNatConversions.manualRuleTransformation;
import static org.batfish.vendor.check_point_gateway.representation.CheckpointNatConversions.matchAutomaticStaticRule;
import static org.batfish.vendor.check_point_gateway.representation.CheckpointNatConversions.matchInternalTraffic;
import static org.batfish.vendor.check_point_gateway.representation.CheckpointNatConversions.matchManualRule;
import static org.batfish.vendor.check_point_gateway.representation.CheckpointNatConversions.mergeTransformations;
import static org.batfish.vendor.check_point_gateway.representation.CheckpointNatConversions.shouldConvertAutomaticRule;
import static org.batfish.vendor.check_point_management.AddressSpaceToIpSpaceMetadata.toIpSpaceMetadata;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.common.VendorConversionException;
import org.batfish.common.Warnings;
import org.batfish.common.topology.Layer1Edge;
import org.batfish.datamodel.AclAclLine;
import org.batfish.datamodel.AclIpSpace;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.DeviceModel;
import org.batfish.datamodel.FirewallSessionInterfaceInfo;
import org.batfish.datamodel.Interface.Dependency;
import org.batfish.datamodel.Interface.DependencyType;
import org.batfish.datamodel.InterfaceType;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.IpSpaceMetadata;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.AclLineMatchExprs;
import org.batfish.datamodel.packet_policy.ApplyFilter;
import org.batfish.datamodel.packet_policy.ApplyTransformation;
import org.batfish.datamodel.packet_policy.FibLookup;
import org.batfish.datamodel.packet_policy.If;
import org.batfish.datamodel.packet_policy.IngressInterfaceVrf;
import org.batfish.datamodel.packet_policy.PacketMatchExpr;
import org.batfish.datamodel.packet_policy.PacketPolicy;
import org.batfish.datamodel.packet_policy.Return;
import org.batfish.datamodel.packet_policy.Statement;
import org.batfish.datamodel.route.nh.NextHop;
import org.batfish.datamodel.route.nh.NextHopDiscard;
import org.batfish.datamodel.route.nh.NextHopInterface;
import org.batfish.datamodel.route.nh.NextHopIp;
import org.batfish.datamodel.transformation.Transformation;
import org.batfish.datamodel.transformation.TransformationStep;
import org.batfish.vendor.ConversionContext;
import org.batfish.vendor.VendorConfiguration;
import org.batfish.vendor.check_point_gateway.representation.BondingGroup.Mode;
import org.batfish.vendor.check_point_management.AccessLayer;
import org.batfish.vendor.check_point_management.AddressSpace;
import org.batfish.vendor.check_point_management.AddressSpaceToIpSpace;
import org.batfish.vendor.check_point_management.AddressSpaceToMatchExpr;
import org.batfish.vendor.check_point_management.CheckpointManagementConfiguration;
import org.batfish.vendor.check_point_management.Cluster;
import org.batfish.vendor.check_point_management.ClusterMember;
import org.batfish.vendor.check_point_management.GatewayOrServer;
import org.batfish.vendor.check_point_management.HasNatSettings;
import org.batfish.vendor.check_point_management.ManagementDomain;
import org.batfish.vendor.check_point_management.ManagementPackage;
import org.batfish.vendor.check_point_management.ManagementServer;
import org.batfish.vendor.check_point_management.NamedManagementObject;
import org.batfish.vendor.check_point_management.NatHideBehindGateway;
import org.batfish.vendor.check_point_management.NatRule;
import org.batfish.vendor.check_point_management.NatRulebase;
import org.batfish.vendor.check_point_management.ServiceToMatchExpr;
import org.batfish.vendor.check_point_management.Uid;
import org.batfish.vendor.check_point_management.UnknownTypedManagementObject;

public class CheckPointGatewayConfiguration extends VendorConfiguration {

  public static final String VRF_NAME = "default";
  public static final String INTERFACE_ACL_NAME = "~INTERFACE_ACL~";
  public static final String SYNC_INTERFACE_NAME = "Sync";

  public CheckPointGatewayConfiguration() {
    _bondingGroups = new HashMap<>();
    _interfaces = new HashMap<>();
    _staticRoutes = new HashMap<>();
  }

  public void finalizeStructures() {
    _bondingGroups = toImmutableMap(_bondingGroups);
    _interfaces = toImmutableMap(_interfaces);
    _staticRoutes = toImmutableMap(_staticRoutes);
  }

  public @Nonnull Map<Integer, BondingGroup> getBondingGroups() {
    return _bondingGroups;
  }

  @Override
  public String getHostname() {
    return _hostname;
  }

  public Map<String, Interface> getInterfaces() {
    return _interfaces;
  }

  public Map<Prefix, StaticRoute> getStaticRoutes() {
    return _staticRoutes;
  }

  @Override
  public void setHostname(String hostname) {
    _hostname = hostname;
  }

  @Override
  public void setVendor(ConfigurationFormat format) {
    _vendor = format;
  }

  @Override
  public List<Configuration> toVendorIndependentConfigurations() throws VendorConversionException {
    String hostname = getHostname();
    _c = new Configuration(hostname, _vendor);
    _c.setHumanName(hostname);
    _c.setDeviceModel(DeviceModel.CHECK_POINT_GATEWAY);
    _c.setDefaultCrossZoneAction(LineAction.DENY);
    _c.setDefaultInboundAction(LineAction.PERMIT);

    Optional<CheckpointManagementConfiguration> mgmtConfig =
        Optional.ofNullable(getConversionContext())
            .map(ConversionContext::getCheckpointManagementConfiguration)
            .map(cmc -> (CheckpointManagementConfiguration) cmc);
    Optional<Entry<ManagementDomain, GatewayOrServer>> domainAndGateway =
        mgmtConfig.flatMap(this::findGatewayAndDomain);
    Optional<Cluster> cluster = domainAndGateway.flatMap(e -> getCluster(e.getValue(), e.getKey()));

    if (!mgmtConfig.isPresent()) {
      _w.redFlag(
          "No CheckPoint management configuration found. This gateway will not have any management"
              + " configuration applied, e.g. no ACLs or NAT rules.");
    } else if (!domainAndGateway.isPresent()) {
      _w.redFlag(
          "No domain found for this gateway. This gateway will not have any domain configuration"
              + " applied, e.g. no ACLs or NAT rules.");
    }

    Optional<ManagementPackage> mgmtPackage =
        domainAndGateway.flatMap(e -> findAccessPackage(e.getKey(), e.getValue(), cluster));
    Map<Uid, NamedManagementObject> mgmtObjects =
        mgmtPackage
            .map(pakij -> getAllObjects(pakij, domainAndGateway.get().getKey()))
            .orElse(ImmutableMap.of());

    // Initial management data conversion
    cluster.ifPresent(this::convertCluster);
    mgmtPackage.ifPresent(pakij -> convertPackage(pakij, mgmtObjects, domainAndGateway.get()));

    // Gateways don't have VRFs, so put everything in a generated default VRF
    Vrf vrf = new Vrf(VRF_NAME);
    _c.setVrfs(ImmutableMap.of(VRF_NAME, vrf));

    _interfaces.forEach((ifaceName, iface) -> convertInterface(iface, vrf));

    // Now that VI interfaces exist, convert NAT rulebase if present
    mgmtPackage
        .map(ManagementPackage::getNatRulebase)
        .ifPresent(r -> convertNatRulebase(r, domainAndGateway.get(), mgmtObjects));

    vrf.getStaticRoutes()
        .addAll(
            _staticRoutes.values().stream()
                .flatMap(staticRoute -> convertStaticRoute(staticRoute, _interfaces))
                .collect(ImmutableSet.toImmutableSet()));

    return ImmutableList.of(_c);
  }

  /** Gets the {@link Cluster} for the specified gateway. */
  private Optional<Cluster> getCluster(GatewayOrServer gateway, ManagementDomain domain) {
    if (!(gateway instanceof ClusterMember)) {
      return Optional.empty();
    }
    Class<? extends Cluster> clusterClass = ((ClusterMember) gateway).getClusterClass();
    String gatewayName = gateway.getName();
    Cluster cluster = null;
    for (GatewayOrServer gatewayOrServer : domain.getGatewaysAndServers().values()) {
      if (!clusterClass.isInstance(gatewayOrServer)) {
        continue;
      }
      Cluster clusterCandidate = (Cluster) gatewayOrServer;
      int clusterCandidateMemberIndex =
          clusterCandidate.getClusterMemberNames().indexOf(gatewayName);
      if (clusterCandidateMemberIndex != -1) {
        cluster = clusterCandidate;
        _clusterMemberIndex = clusterCandidateMemberIndex;
        // TODO: verify that a gateway may only be a member of a single cluster
        break;
      }
    }
    if (cluster == null) {
      _w.redFlagf(
          "Could not find matching cluster of type %s for this gateway of type %s",
          clusterClass.getSimpleName(), gateway.getClass().getSimpleName());
      return Optional.empty();
    }
    return Optional.of(cluster);
  }

  /** Populates cluster virtual IP metadata for this gateway. */
  private void convertCluster(Cluster cluster) {
    _cluster = cluster;
    _clusterInterfaces =
        cluster.getInterfaces().stream()
            .collect(
                ImmutableMap.toImmutableMap(
                    org.batfish.vendor.check_point_management.Interface::getName,
                    Function.identity()));
    String memberName = cluster.getClusterMemberNames().get(_clusterMemberIndex);
    assert getConversionContext() != null;
    assert getConversionContext().getCheckpointManagementConfiguration() != null;
    ((CheckpointManagementConfiguration)
            getConversionContext().getCheckpointManagementConfiguration())
        .recordClusterMemberNameToHostname(memberName, getHostname());
  }

  private void convertAccessLayers(
      List<AccessLayer> accessLayers,
      Map<Uid, NamedManagementObject> objects,
      Entry<ManagementDomain, GatewayOrServer> domainAndGateway) {
    // TODO support matching multiple access layers
    if (accessLayers.size() > 1) {
      _w.redFlag(
          "Batfish currently only supports matching on a single Access Layer, so only the first"
              + " matching Access Rule will be applied.");
    }
    ServiceToMatchExpr serviceToMatchExpr = new ServiceToMatchExpr(objects);
    AddressSpaceToMatchExpr addressSpaceToMatchExpr = new AddressSpaceToMatchExpr(objects);
    for (AccessLayer al : accessLayers) {
      Map<String, IpAccessList> acl =
          toIpAccessLists(
              al, objects, serviceToMatchExpr, addressSpaceToMatchExpr, domainAndGateway, _w);
      _c.getIpAccessLists().putAll(acl);
    }
    IpAccessList interfaceAcl =
        IpAccessList.builder()
            .setName(INTERFACE_ACL_NAME)
            .setLines(
                accessLayers.stream()
                    .map(l -> new AclAclLine(l.getName(), aclName(l)))
                    .collect(ImmutableList.toImmutableList()))
            .build();
    _c.getIpAccessLists().put(interfaceAcl.getName(), interfaceAcl);
  }

  /**
   * Convert specified objects to their VI model equivalent representation(s) if applicable, and add
   * them to the VI configuration. E.g. convert {@link AddressSpace}s to {@link IpSpace}s.
   *
   * <p><b>Note: different objects (e.g. revisions) of the same name/type are not supported. Only
   * the last-encountered object with a particular name (of the same type) will produce a VI model
   * object.</b>
   *
   * <p>Warns about unknown object types.
   */
  private void convertObjects(Map<Uid, NamedManagementObject> objs) {
    AddressSpaceToIpSpace addressSpaceToIpSpace = new AddressSpaceToIpSpace(objs);
    objs.values()
        .forEach(
            obj -> {
              if (obj instanceof UnknownTypedManagementObject) {
                UnknownTypedManagementObject utmo = (UnknownTypedManagementObject) obj;
                _w.redFlagf(
                    "Batfish does not handle converting objects of type %s. These objects will"
                        + " be ignored.",
                    utmo.getType());
                return;
              }

              if (obj instanceof AddressSpace) {
                AddressSpace addressSpace = (AddressSpace) obj;
                IpSpace ipSpace = addressSpace.accept(addressSpaceToIpSpace);
                _c.getIpSpaces().put(obj.getName(), ipSpace);

                IpSpaceMetadata metadata = toIpSpaceMetadata(addressSpace);
                _c.getIpSpaceMetadata().put(obj.getName(), metadata);
              }
            });
  }

  /**
   * Convert constructs in the specified package to their VI model equivalent and add them to the VI
   * configuration. (Does not include NAT rulebase conversion.)
   */
  private void convertPackage(
      ManagementPackage pakij,
      Map<Uid, NamedManagementObject> objects,
      Entry<ManagementDomain, GatewayOrServer> domainAndGateway) {
    convertObjects(objects);
    convertAccessLayers(pakij.getAccessLayers(), objects, domainAndGateway);
  }

  /**
   * Get all {@link NamedManagementObject} for the specified {@link ManagementPackage} in the
   * specified {@link ManagementDomain}.
   */
  private Map<Uid, NamedManagementObject> getAllObjects(
      ManagementPackage pakij, ManagementDomain domain) {
    Map<Uid, NamedManagementObject> objects = new HashMap<>();
    Optional.ofNullable(pakij.getNatRulebase())
        .map(NatRulebase::getObjectsDictionary)
        .ifPresent(objects::putAll);
    pakij.getAccessLayers().stream()
        .map(AccessLayer::getObjectsDictionary)
        .forEach(objects::putAll);
    domain.getObjects().forEach(object -> objects.put(object.getUid(), object));
    objects.putAll(domain.getGatewaysAndServers());
    return objects;
  }

  /**
   * Converts the given {@link NatRulebase} and applies it to this config. Must be called after VI
   * interfaces are created.
   */
  @SuppressWarnings("unused")
  private void convertNatRulebase(
      NatRulebase natRulebase,
      Entry<ManagementDomain, GatewayOrServer> domainAndGateway,
      Map<Uid, NamedManagementObject> objects) {
    // Compile a list of PacketPolicy statements that will apply transformations on ingress.
    List<Statement> transformationStatements =
        getTransformationStatements(natRulebase, domainAndGateway, objects);

    // If there are no transformations to be applied, short-circuit.
    if (transformationStatements.isEmpty()) {
      return;
    }

    /*
    Otherwise, collect packet policy statements that are needed on every interface.
    Overall packet policy structure on each interface will look like this:
    1. If the traffic is denied by the interface's ingress filter, drop it.
    2. If the dest IP is owned by the firewall, return FibLookup (which will result in accept).
    3. Apply any matching transformations (see getTransformationStatements) and return FibLookup.
    */
    List<Statement> generalStatements = new ArrayList<>(); // statements for all packet policies
    Return returnFibLookup = new Return(new FibLookup(IngressInterfaceVrf.instance()));

    // If the traffic is destined for an IP owned by the firewall, do not transform.
    IpSpace ownedByFirewall =
        AclIpSpace.union(
            _c.getActiveInterfaces().values().stream()
                .flatMap(iface -> iface.getAllConcreteAddresses().stream())
                .map(ConcreteInterfaceAddress::getIp)
                .map(Ip::toIpSpace)
                .collect(ImmutableList.toImmutableList()));
    generalStatements.add(
        new If(new PacketMatchExpr(matchDst(ownedByFirewall)), ImmutableList.of(returnFibLookup)));

    // Otherwise, apply transformation statements.
    generalStatements.addAll((transformationStatements));

    // Keep track of which incoming filters we've already generated packet policies for; this way we
    // can create fewer packet policies overall.
    Map<String, PacketPolicy> incomingFiltersToPacketPolicies = new HashMap<>();

    // Now for each interface, apply a packet policy that encompasses the incoming filter and
    // transformations. Add an outgoing transformation to translate any HIDE_BEHIND_GATEWAY src IPs
    // to the egress iface IP.
    _c.getActiveInterfaces()
        .values()
        .forEach(
            iface -> {
              // Incoming filter is ignored when packet policy is present, so apply it explicitly
              String incomingFilter =
                  Optional.ofNullable(iface.getIncomingFilter())
                      .map(IpAccessList::getName)
                      .orElse(null);
              iface.setIncomingFilter(null);
              PacketPolicy policy = incomingFiltersToPacketPolicies.get(incomingFilter);
              if (policy == null) {
                ImmutableList.Builder<Statement> ifacePolicyStatements = ImmutableList.builder();
                if (incomingFilter != null) {
                  ifacePolicyStatements.add(new ApplyFilter(incomingFilter));
                }
                ifacePolicyStatements.addAll(generalStatements);
                String packetPolicyName = packetPolicyName(incomingFilter);
                policy =
                    new PacketPolicy(
                        packetPolicyName, ifacePolicyStatements.build(), returnFibLookup);
                _c.getPacketPolicies().put(packetPolicyName, policy);
                incomingFiltersToPacketPolicies.put(incomingFilter, policy);
              }
              iface.setPacketPolicy(policy.getName());
              // Build outgoing transformation to correctly translate HIDE_BEHIND_GATEWAY src
              if (iface.getConcreteAddress() != null) {
                Transformation outgoing =
                    when(matchSrc(HIDE_BEHIND_GATEWAY_IP))
                        .apply(
                            TransformationStep.assignSourceIp(iface.getConcreteAddress().getIp()))
                        .build();
                iface.setOutgoingTransformation(outgoing);
              }
            });
  }

  private static String packetPolicyName(@Nullable String incomingFilter) {
    if (incomingFilter == null) {
      return "~PACKET_POLICY~NO_INGRESS_FILTER~";
    }
    return String.format("~PACKET_POLICY_%s~", incomingFilter);
  }

  /**
   * Returns a list of statements to apply to incoming traffic to apply the correct transformations
   * for the given {@code gateway}. The list will be empty iff no transformations can be matched.
   *
   * <ul>
   *   Statements are formulated as follows:
   *   <li>Match (static and hide) manual rules. On match, these statements apply the transformation
   *       and return a FibLookup. Flows that match a manual rule are not eligible to match any
   *       other rule, regardless of the matched rule's transformation.
   *   <li>Match internal traffic for automatic hide rules (see {@link
   *       CheckpointNatConversions#matchInternalTraffic}) that will not match any automatic static
   *       source NAT rule (because automatic static rules take precedence over automatic hide). If
   *       matched, these statements return a FibLookup with no transformation.
   *   <li>Match automatic dest rules. If a rule is matched, apply the transformation and continue
   *       to automatic source rules, skipping any remaining auto dest rules.
   *   <li>Match automatic static source rules. If a rule is matched, apply the transformation and
   *       return a FibLookup.
   *   <li>Match automatic hide rules (which do source translation only). If a rule is matched,
   *       apply the transformation and return a FibLookup.
   * </ul>
   *
   * <ul>
   *   This statement formulation implies certain assumptions/invariants:
   *   <li>Internal traffic for an automatic hide rule is not subject to destination translation by
   *       an automatic static rule. This needs to be tested.
   *   <li>Aside from matching internal traffic, source translation by automatic rules (static and
   *       hide) matches ONLY on source IP. If it matched on other fields, it would be incorrect to
   *       apply destination NAT before matching auto source rules.
   * </ul>
   */
  private @Nonnull List<Statement> getTransformationStatements(
      NatRulebase natRulebase,
      Entry<ManagementDomain, GatewayOrServer> domainAndGateway,
      Map<Uid, NamedManagementObject> objects) {
    ServiceToMatchExpr serviceToMatchExpr = new ServiceToMatchExpr(objects);
    AddressSpaceToMatchExpr addressSpaceToMatchExpr = new AddressSpaceToMatchExpr(objects);
    Warnings warnings = getWarnings();

    // List of PacketPolicy statements that will apply the transformation on ingress
    ImmutableList.Builder<Statement> statements = ImmutableList.builder();
    Return returnFibLookup = new Return(new FibLookup(IngressInterfaceVrf.instance()));

    // Collect rules
    List<NatRule> manualRules = new ArrayList<>();
    List<NatRule> manualLowPriorityRules = new ArrayList<>();
    List<HasNatSettings> autoHideNatObjects = new ArrayList<>();
    List<HasNatSettings> autoStaticNatObjects = new ArrayList<>();
    boolean encounteredAutoRules = false;
    for (Iterator<NatRule> it = getApplicableNatRules(natRulebase, domainAndGateway).iterator();
        it.hasNext(); ) {
      NatRule rule = it.next();
      if (!rule.isAutoGenerated()) {
        // We consider manual rules encountered after auto rules low priority
        // We also assume they never occur between auto rules
        if (encounteredAutoRules) {
          manualLowPriorityRules.add(rule);
        } else {
          manualRules.add(rule);
        }
      } else if (shouldConvertAutomaticRule(rule, natRulebase.getObjectsDictionary(), warnings)) {
        encounteredAutoRules = true;
        HasNatSettings src =
            (HasNatSettings) natRulebase.getObjectsDictionary().get(rule.getOriginalSource());
        switch (rule.getMethod()) {
          case HIDE:
            if (isValidAutomaticHideRule(src, warnings)) {
              autoHideNatObjects.add(src);
            }
            continue;
          case STATIC:
            if (isValidAutomaticStaticRule(src, warnings)) {
              autoStaticNatObjects.add(src);
            }
            continue;
        }
      }
    }

    // First match any manual rules. If a manual rule is matched, no other rules can be matched,
    // regardless of what translations the manual rule applies, so return.
    manualRules.forEach(
        rule ->
            getManualRuleStatement(
                    rule, serviceToMatchExpr, addressSpaceToMatchExpr, objects, returnFibLookup)
                .ifPresent(statements::add));

    // Convert automatic NAT rules
    if (!autoHideNatObjects.isEmpty() || !autoStaticNatObjects.isEmpty()) {
      statements.add(
          getAutoRuleStatement(
              autoStaticNatObjects, autoHideNatObjects, returnFibLookup, addressSpaceToMatchExpr));
    }

    // Low priority rules come after automatic rules
    manualLowPriorityRules.forEach(
        rule ->
            getManualRuleStatement(
                    rule, serviceToMatchExpr, addressSpaceToMatchExpr, objects, returnFibLookup)
                .ifPresent(statements::add));

    return statements.build();
  }

  /** Generate a statement handling the specified manual NAT rule transformation. */
  private @Nonnull Optional<Statement> getManualRuleStatement(
      NatRule rule,
      ServiceToMatchExpr serviceToMatchExpr,
      AddressSpaceToMatchExpr addressSpaceToMatchExpr,
      Map<Uid, NamedManagementObject> objects,
      Return returnFibLookup) {
    Warnings warnings = getWarnings();
    Optional<AclLineMatchExpr> matchCondition =
        matchManualRule(rule, serviceToMatchExpr, addressSpaceToMatchExpr, objects, warnings);
    if (!matchCondition.isPresent()) {
      return Optional.empty(); // warning already filed
    }
    Optional<Transformation> transformation = manualRuleTransformation(rule, objects, warnings);
    // warning already filed
    return transformation.map(
        value ->
            new If(
                new PacketMatchExpr(matchCondition.get()),
                ImmutableList.of(new ApplyTransformation(value), returnFibLookup)));
  }

  /** Generate a statement handling all automatic NAT rule transformations. */
  private @Nonnull Statement getAutoRuleStatement(
      List<HasNatSettings> autoStaticNatObjects,
      List<HasNatSettings> autoHideNatObjects,
      Return returnFibLookup,
      AddressSpaceToMatchExpr addressSpaceToMatchExpr) {
    Warnings warnings = getWarnings();

    // While we have the NAT hide settings, check if any hide behind gateway, and warn if any
    // interfaces are missing an IP because this will cause the source translation to be wrong.
    if (autoHideNatObjects.stream()
            .anyMatch(
                hideRule ->
                    hideRule.getNatSettings().getHideBehind() instanceof NatHideBehindGateway)
        && _c.getActiveInterfaces().values().stream()
            .anyMatch(iface -> iface.getConcreteAddress() == null)) {
      warnings.redFlag(
          "Automatic hide-behind-gateway rules are not supported if matching traffic is routed out"
              + " an interface with no concrete address");
    }

    ImmutableList.Builder<AclLineMatchExpr> matchAnyAutoRule = ImmutableList.builder();
    // Auto static
    autoStaticNatObjects.stream()
        .forEach(natObj -> matchAnyAutoRule.add(matchAutomaticStaticRule(natObj, true)));
    autoStaticNatObjects.stream()
        .forEach(natObj -> matchAnyAutoRule.add(matchAutomaticStaticRule(natObj, false)));
    // Auto hide
    matchAnyAutoRule.addAll(
        autoHideNatObjects.stream().map(addressSpaceToMatchExpr::convertSource).iterator());

    ImmutableList.Builder<Statement> statements = ImmutableList.builder();
    if (!autoHideNatObjects.isEmpty()) {
      // Automatic hide rules configured on network or address-range objects will match, but not
      // translate, traffic whose src and dst are both within that network/address-range. Need to
      // check for this condition before any dst NAT is applied, but avoid matching any traffic that
      // would match a static src NAT rule (because static rules take precedence over hide).
      // TODO Is traffic matching this condition still eligible to match a separate dst NAT rule?
      // TODO Intranet-matching for auto static rules too, once we support them on non-host objects
      List<AclLineMatchExpr> matchInternalTraffic =
          autoHideNatObjects.stream()
              .map(natObj -> matchInternalTraffic(natObj, addressSpaceToMatchExpr))
              .filter(Optional::isPresent)
              .map(Optional::get)
              .collect(ImmutableList.toImmutableList());
      if (!matchInternalTraffic.isEmpty()) {
        AclLineMatchExpr matchAnyInternalTraffic = or(matchInternalTraffic);
        AclLineMatchExpr matchedByStaticSrcRules =
            or(
                autoStaticNatObjects.stream()
                    .map(natObj -> matchAutomaticStaticRule(natObj, true))
                    .collect(ImmutableList.toImmutableList()));
        statements.add(
            new If(
                new PacketMatchExpr(and(not(matchedByStaticSrcRules), matchAnyInternalTraffic)),
                ImmutableList.of(returnFibLookup)));
      }
    }

    // Apply dest translations for automatic static rules. If one of these is matched, the packet
    // skips any remaining dest rules but may still match a source rule, so do not return yet.
    // To achieve these semantics, create one transformation representing all possible destination
    // translations (including match conditions) and apply this transformation unconditionally.
    List<Transformation> autoStaticDstTransformations =
        autoStaticNatObjects.stream()
            .map(
                hasNatSettings ->
                    when(matchAutomaticStaticRule(hasNatSettings, false))
                        .apply(automaticStaticRuleTransformationStep(hasNatSettings, false))
                        .build())
            .collect(ImmutableList.toImmutableList());
    mergeTransformations(autoStaticDstTransformations)
        .ifPresent(t -> statements.add(new ApplyTransformation(t)));

    // Apply source translations for automatic static rules. If one of these is matched, return
    // FibLookup (packet is not eligible to match an auto hide rule).
    for (HasNatSettings autoStaticNatObj : autoStaticNatObjects) {
      AclLineMatchExpr matchCondition = matchAutomaticStaticRule(autoStaticNatObj, true);
      TransformationStep transformationStep =
          automaticStaticRuleTransformationStep(autoStaticNatObj, true);

      statements.add(
          new If(
              new PacketMatchExpr(matchCondition),
              ImmutableList.of(
                  new ApplyTransformation(always().apply(transformationStep).build()),
                  returnFibLookup)));
    }

    // Apply source translations for automatic hide rules. If one is matched, return FibLookup,
    // though it doesn't really matter because these will be the last statements in the policy
    // (and the default action will be FibLookup).
    for (HasNatSettings autoHideNatObj : autoHideNatObjects) {
      AclLineMatchExpr matchCondition = addressSpaceToMatchExpr.convertSource(autoHideNatObj);
      TransformationStep transformationStep = automaticHideRuleTransformationStep(autoHideNatObj);
      statements.add(
          new If(
              new PacketMatchExpr(matchCondition),
              ImmutableList.of(
                  new ApplyTransformation(always().apply(transformationStep).build()),
                  returnFibLookup)));
    }
    // If any auto rule(s) matched, should stop processing rules
    // (e.g. not process low-priority manual)
    statements.add(returnFibLookup);

    return new If(
        new PacketMatchExpr(AclLineMatchExprs.or(matchAnyAutoRule.build())), statements.build());
  }

  /**
   * Get the {@link ManagementPackage} for the specified gateway. If the gateway has a cluster, use
   * the package from the cluster.
   */
  private @Nonnull Optional<ManagementPackage> findAccessPackage(
      ManagementDomain domain, GatewayOrServer gateway, Optional<Cluster> cluster) {
    // Use the cluster's access package, if the gateway is a cluster member
    String accessPackageName =
        cluster.isPresent()
            ? cluster.get().getPolicy().getAccessPolicyName()
            : gateway.getPolicy().getAccessPolicyName();
    if (accessPackageName == null) {
      _w.redFlagf(
          "No access package found for gateway '%s', so no access rules will be added",
          gateway.getName());
      return Optional.empty();
    }

    // TODO: can be more efficient if we also store map: packageName -> package in ManagementDomain
    Optional<ManagementPackage> maybePackage =
        domain.getPackages().values().stream()
            .filter(p -> p.getPackage().getName().equals(accessPackageName))
            .findFirst();
    if (!maybePackage.isPresent()) {
      _w.redFlagf(
          "Gateway or server '%s' access-policy-name refers to non-existent package '%s'",
          gateway.getName(), accessPackageName);
    }
    return maybePackage;
  }

  private @Nonnull Optional<Entry<ManagementDomain, GatewayOrServer>> findGatewayAndDomain(
      CheckpointManagementConfiguration mgmtConfig) {
    // TODO handle linking to secondary IP addresses, if that is allowed
    Set<Ip> ips =
        _interfaces.values().stream()
            .map(Interface::getAddress)
            .filter(Objects::nonNull)
            .map(ConcreteInterfaceAddress::getIp)
            .collect(ImmutableSet.toImmutableSet());
    // TODO: something special where there is IP reuse?
    for (ManagementServer server : mgmtConfig.getServers().values()) {
      for (ManagementDomain domain : server.getDomains().values()) {
        Optional<GatewayOrServer> maybeGateway =
            domain.getGatewaysAndServers().values().stream()
                .filter(gw -> gw.getIpv4Address() != null)
                .filter(gw -> ips.contains(gw.getIpv4Address()))
                .findFirst();
        if (maybeGateway.isPresent()) {
          return Optional.of(immutableEntry(domain, maybeGateway.get()));
        }
      }
    }
    return Optional.empty();
  }

  /**
   * Returns a {@link Stream} of VI {@link org.batfish.datamodel.StaticRoute} corresponding to the
   * specified VS {@link StaticRoute}. Only routes with valid nexthop targets are returned.
   */
  private static Stream<org.batfish.datamodel.StaticRoute> convertStaticRoute(
      StaticRoute route, Map<String, Interface> interfaces) {
    return route.getNexthops().values().stream()
        .filter(nh -> nexthopIsValid(nh.getNexthopTarget(), interfaces))
        .map(
            nh ->
                org.batfish.datamodel.StaticRoute.builder()
                    .setNetwork(route.getDestination())
                    .setNextHop(toNextHop(nh.getNexthopTarget()))
                    // Unset priority is preferred over other priorities
                    .setAdministrativeCost(firstNonNull(nh.getPriority(), 0))
                    .setRecursive(false)
                    .build());
  }

  /**
   * Returns {@code boolean} indicating if {@link NexthopTarget} is valid, given a map of all {@link
   * Interface}.
   */
  private static boolean nexthopIsValid(NexthopTarget target, Map<String, Interface> interfaces) {
    if (target instanceof NexthopAddress) {
      Ip addr = ((NexthopAddress) target).getAddress();
      return interfaces.values().stream().anyMatch(i -> ifaceContainsAddress(i, addr));
    } else if (target instanceof NexthopLogical) {
      String targetInterface = ((NexthopLogical) target).getInterface();
      // Guaranteed by extraction
      assert interfaces.containsKey(targetInterface);
    }
    return true;
  }

  private static boolean ifaceContainsAddress(Interface iface, Ip address) {
    ConcreteInterfaceAddress addr = iface.getAddress();
    if (addr == null) {
      return false;
    }
    return addr.getPrefix().containsIp(address);
  }

  /** Convert specified VS {@link NexthopTarget} into a VI {@link NextHop}. */
  private static NextHop toNextHop(NexthopTarget target) {
    if (target instanceof NexthopAddress) {
      return NextHopIp.of(((NexthopAddress) target).getAddress());
    } else if (target instanceof NexthopLogical) {
      return NextHopInterface.of(((NexthopLogical) target).getInterface());
    }
    assert target instanceof NexthopBlackhole || target instanceof NexthopReject;
    return NextHopDiscard.instance();
  }

  InterfaceType getInterfaceType(Interface iface) {
    String name = iface.getName();
    if (name.startsWith("eth")) {
      if (name.contains(".")) {
        return InterfaceType.LOGICAL;
      }
      return InterfaceType.PHYSICAL;
    } else if (name.startsWith("lo")) {
      return InterfaceType.LOOPBACK;
    } else if (name.startsWith("bond")) {
      if (name.contains(".")) {
        return InterfaceType.AGGREGATE_CHILD;
      }
      return InterfaceType.AGGREGATED;
    } else if (name.equals(SYNC_INTERFACE_NAME)) {
      return InterfaceType.PHYSICAL;
    }
    return InterfaceType.UNKNOWN;
  }

  void convertInterface(Interface iface, Vrf vrf) {
    String ifaceName = iface.getName();
    InterfaceType type = getInterfaceType(iface);
    org.batfish.datamodel.Interface.Builder newIface =
        org.batfish.datamodel.Interface.builder()
            .setName(ifaceName)
            .setOwner(_c)
            .setVrf(vrf)
            .setType(type);

    Optional<Integer> parentBondingGroupOpt = getParentBondingGroupNumber(iface);
    if (parentBondingGroupOpt.isPresent()) {
      String parentBondIfaceName = getBondInterfaceName(parentBondingGroupOpt.get());
      Interface parentBondInterface = _interfaces.get(parentBondIfaceName);
      assert parentBondInterface != null;
      newIface
          .setChannelGroup(parentBondIfaceName)
          // Member interface inherits some configuration from parent bonding group
          .setMtu(parentBondInterface.getMtuEffective());
    } else {
      newIface
          .setAddress(iface.getAddress())
          .setAdminUp(iface.getState())
          .setMtu(iface.getMtuEffective());
    }

    Double speed = iface.getLinkSpeedEffective();
    if (speed != null) {
      newIface.setSpeed(speed);
      newIface.setBandwidth(speed);
    }
    if (iface.getVlanId() != null) {
      newIface.setEncapsulationVlan(iface.getVlanId());
    }
    if (iface.getParentInterface() != null) {
      Interface parent = _interfaces.get(iface.getParentInterface());
      // This is a subinterface. Its speed can't be set explicitly.
      // If its parent is physical, this interface should inherit the parent's speed/bw now.
      // If its parent is a bond interface, then this interface's bandwidth will be set after
      // the parent's bandwidth is calculated post-conversion.
      assert parent != null;
      Double parentSpeed = parent.getLinkSpeedEffective();
      if (parentSpeed != null) {
        newIface.setSpeed(parentSpeed);
        newIface.setBandwidth(parentSpeed);
      }
      newIface.setDependencies(
          ImmutableList.of(new Dependency(iface.getParentInterface(), DependencyType.BIND)));
    }

    getBondingGroup(ifaceName)
        .ifPresent(
            bg -> {
              newIface.setChannelGroupMembers(bg.getInterfaces());
              newIface.setDependencies(
                  bg.getInterfaces().stream()
                      .map(member -> new Dependency(member, DependencyType.AGGREGATE))
                      .collect(ImmutableSet.toImmutableSet()));

              if (bg.getModeEffective() == Mode.ACTIVE_BACKUP) {
                _w.redFlagf(
                    "Bonding group mode active-backup is not yet supported in Batfish."
                        + " Deactivating interface %s.",
                    ifaceName);
                newIface.setAdminUp(false);
              }
            });

    // TODO confirm AccessRule interaction with NAT
    newIface.setIncomingFilter(_c.getIpAccessLists().get(INTERFACE_ACL_NAME));
    newIface.setFirewallSessionInterfaceInfo(
        new FirewallSessionInterfaceInfo(
            POST_NAT_FIB_LOOKUP, ImmutableList.of(ifaceName), null, null));

    if (ifaceName.equals(SYNC_INTERFACE_NAME)) {
      if (getConversionContext().getCheckpointManagementConfiguration() != null) {
        ((CheckpointManagementConfiguration)
                getConversionContext().getCheckpointManagementConfiguration())
            .recordSyncInterface(_hostname);
      }
      createClusterVrrpGroup(iface, newIface, _clusterInterfaces, _clusterMemberIndex, _w);
    }

    newIface.build();
  }

  /**
   * Get the {@link BondingGroup} corresponding to the specified bond interface. Returns {@link
   * Optional#empty} if the interface is not a bond interface or if the bonding group does not
   * exist.
   */
  private @Nonnull Optional<BondingGroup> getBondingGroup(String ifaceName) {
    Pattern p = Pattern.compile("bond(\\d+)");
    Matcher res = p.matcher(ifaceName);
    if (res.matches()) {
      return Optional.ofNullable(_bondingGroups.get(Integer.valueOf(res.group(1))));
    }
    return Optional.empty();
  }

  /**
   * Returns the parent bonding group number for the specified interface, or {@link Optional#empty}
   * if it is not a member of a bonding group.
   */
  private @Nonnull Optional<Integer> getParentBondingGroupNumber(Interface iface) {
    return _bondingGroups.values().stream()
        .filter(bg -> bg.getInterfaces().contains(iface.getName()))
        .findFirst()
        .map(BondingGroup::getNumber);
  }

  @Override
  public @Nonnull Set<Layer1Edge> getLayer1Edges() {
    if (_cluster == null) {
      return ImmutableSet.of();
    }
    assert getConversionContext() != null;
    assert getConversionContext().getCheckpointManagementConfiguration() != null;
    CheckpointManagementConfiguration mc =
        ((CheckpointManagementConfiguration)
            getConversionContext().getCheckpointManagementConfiguration());
    ImmutableSet.Builder<Layer1Edge> builder = ImmutableSet.builder();
    forEachWithIndex(
        _cluster.getClusterMemberNames(),
        (i, memberName) -> {
          if (_clusterMemberIndex == i) {
            // no self-edges
            return;
          }
          Optional<String> maybeRemoteHost = mc.getHostnameForGateway(memberName);
          if (!_c.getAllInterfaces().containsKey(SYNC_INTERFACE_NAME)) {
            _w.redFlag("Cannot generate Sync interface edges because Sync interface is missing");
            return;
          }
          if (!maybeRemoteHost.isPresent()) {
            _w.redFlagf(
                "Cannot generate Sync interface edge to cluster member '%s' whose hostname"
                    + " cannot be determined",
                memberName);
            return;
          }
          String remoteHost = maybeRemoteHost.get();
          if (!mc.hasSyncInterface(remoteHost)) {
            _w.redFlagf(
                "Cannot generate Sync interface edge to remote host '%s' because it lacks a"
                    + " Sync interface",
                remoteHost);
            return;
          }
          builder.add(
              new Layer1Edge(_hostname, SYNC_INTERFACE_NAME, remoteHost, SYNC_INTERFACE_NAME));
        });
    return builder.build();
  }

  /** Get bonding interface name from its bonding group number. */
  public static @Nonnull String getBondInterfaceName(int groupNumber) {
    return "bond" + groupNumber;
  }

  private @Nonnull Map<Integer, BondingGroup> _bondingGroups;
  private Configuration _c;
  private String _hostname;

  private Map<String, Interface> _interfaces;

  /** destination prefix -> static route definition */
  private Map<Prefix, StaticRoute> _staticRoutes;

  private transient @Nullable Map<String, org.batfish.vendor.check_point_management.Interface>
      _clusterInterfaces;

  private transient @Nullable Cluster _cluster;

  private transient int _clusterMemberIndex;

  private ConfigurationFormat _vendor;
}
