package org.batfish.representation.fortios;

import static com.google.common.base.Preconditions.checkState;
import static org.batfish.datamodel.acl.AclLineMatchExprs.and;
import static org.batfish.datamodel.acl.AclLineMatchExprs.or;
import static org.batfish.representation.fortios.FortiosTraceElementCreators.matchPolicyTraceElement;
import static org.batfish.representation.fortios.FortiosTraceElementCreators.matchServiceTraceElement;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.common.VendorConversionException;
import org.batfish.datamodel.AclLine;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.DeviceModel;
import org.batfish.datamodel.ExprAclLine;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.InterfaceType;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpSpaceReference;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.TraceElement;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.AclLineMatchExprs;
import org.batfish.datamodel.acl.DeniedByAcl;
import org.batfish.datamodel.acl.MatchHeaderSpace;
import org.batfish.datamodel.acl.MatchSrcInterface;
import org.batfish.datamodel.acl.OrMatchExpr;
import org.batfish.datamodel.acl.PermittedByAcl;
import org.batfish.vendor.VendorConfiguration;
import org.batfish.vendor.VendorStructureId;

public class FortiosConfiguration extends VendorConfiguration {

  public FortiosConfiguration() {
    _addresses = new HashMap<>();
    _interfaces = new HashMap<>();
    _policies = new LinkedHashMap<>();
    _renameableObjects = new HashMap<>();
    _replacemsgs = new HashMap<>();
    _services = new HashMap<>();
    _zones = new HashMap<>();
  }

  @Override
  public String getHostname() {
    return _hostname;
  }

  @Override
  public void setHostname(String hostname) {
    _hostname = hostname;
  }

  @Override
  public void setVendor(ConfigurationFormat format) {}

  @Override
  public List<Configuration> toVendorIndependentConfigurations() throws VendorConversionException {
    return ImmutableList.of(toVendorIndependentConfiguration());
  }

  public @Nonnull Map<String, Address> getAddresses() {
    return _addresses;
  }

  public @Nonnull Map<String, Interface> getInterfaces() {
    return _interfaces;
  }

  /** name -> policy */
  public @Nonnull Map<String, Policy> getPolicies() {
    return _policies;
  }

  /** majorType -> minorType -> replacemsg config */
  public @Nonnull Map<String, Map<String, Replacemsg>> getReplacemsgs() {
    return _replacemsgs;
  }

  /** UUID -> renameable object */
  public @Nonnull Map<BatfishUUID, FortiosRenameableObject> getRenameableObjects() {
    return _renameableObjects;
  }

  /** name -> service */
  public @Nonnull Map<String, Service> getServices() {
    return _services;
  }

  /** name -> zone */
  public @Nonnull Map<String, Zone> getZones() {
    return _zones;
  }

  private String _hostname;
  private final @Nonnull Map<String, Address> _addresses;
  private final @Nonnull Map<String, Interface> _interfaces;
  // Note: this is a LinkedHashMap to preserve insertion order
  private final @Nonnull Map<String, Policy> _policies;
  private final @Nonnull Map<BatfishUUID, FortiosRenameableObject> _renameableObjects;
  private final @Nonnull Map<String, Map<String, Replacemsg>> _replacemsgs;
  private final @Nonnull Map<String, Service> _services;
  private final @Nonnull Map<String, Zone> _zones;

  private @Nonnull Configuration toVendorIndependentConfiguration() {
    Configuration c = new Configuration(_hostname, ConfigurationFormat.FORTIOS);
    c.setDeviceModel(DeviceModel.FORTIOS_UNSPECIFIED);
    // TODO: verify
    c.setDefaultCrossZoneAction(LineAction.DENY);
    // TODO: verify
    c.setDefaultInboundAction(LineAction.DENY);

    // Convert addresses
    _addresses
        .values()
        .forEach(address -> c.getIpSpaces().put(address.getName(), address.toIpSpace(_w)));

    // Convert policies. Must happen after c._ipSpaces is populated (addresses are converted)
    Map<String, AclLineMatchExpr> convertedServices =
        _services.values().stream()
            .collect(ImmutableMap.toImmutableMap(Service::getName, this::toMatchExpr));
    _policies.values().forEach(policy -> convertPolicy(policy, c, convertedServices));

    // Convert interfaces. Must happen after converting policies
    _interfaces.values().forEach(iface -> convertInterface(iface, c));

    // Count structure references
    markConcreteStructure(FortiosStructureType.ADDRESS);
    markConcreteStructure(FortiosStructureType.SERVICE_CUSTOM);
    markConcreteStructure(FortiosStructureType.INTERFACE);
    return c;
  }

  /** Convert specified {@link Service} into its corresponding {@link AclLineMatchExpr}. */
  @VisibleForTesting
  @Nonnull
  AclLineMatchExpr toMatchExpr(Service service) {
    List<AclLineMatchExpr> matchExprs =
        service
            .toHeaderSpaces()
            .map(MatchHeaderSpace::new)
            .collect(ImmutableList.toImmutableList());
    if (matchExprs.isEmpty()) {
      _w.redFlag(String.format("Service %s does not match any packets", service.getName()));
      return AclLineMatchExprs.FALSE;
    }
    return new OrMatchExpr(matchExprs, matchServiceTraceElement(service, _filename));
  }

  private void convertPolicy(
      Policy policy, Configuration c, Map<String, AclLineMatchExpr> convertedServices) {
    if (policy.getStatusEffective() != Policy.Status.ENABLE) {
      return;
    }

    String number = policy.getNumber();
    @Nullable String name = policy.getName();
    String numAndName = name == null ? number : String.format("%s (%s)", number, name);

    ExprAclLine.Builder line;
    switch (policy.getActionEffective()) {
      case ALLOW:
        line = ExprAclLine.accepting();
        break;
      case DENY:
        line = ExprAclLine.rejecting();
        break;
      default: // TODO: Support policies with action IPSEC
        _w.redFlag(
            String.format(
                "Ignoring policy %s: Action %s is not supported",
                numAndName, policy.getActionEffective()));
        return;
    }

    // TODO Incorporate policy.getComments()
    Set<String> srcAddrs = policy.getSrcAddr();
    Set<String> dstAddrs = policy.getDstAddr();
    Set<String> services = policy.getService();

    // Make sure references were finalized
    assert srcAddrs != null && dstAddrs != null && services != null;

    // Note that src/dst interface filtering will be done in generated export policies.
    ImmutableList.Builder<AclLineMatchExpr> matchConjuncts = ImmutableList.builder();

    // Match src addresses, dst addresses, and services
    List<AclLineMatchExpr> srcAddrExprs =
        Sets.intersection(srcAddrs, c.getIpSpaces().keySet()).stream()
            .map(
                addr -> {
                  HeaderSpace hs =
                      HeaderSpace.builder().setSrcIps(new IpSpaceReference(addr)).build();
                  VendorStructureId vsi =
                      new VendorStructureId(
                          _filename, FortiosStructureType.ADDRESS.getDescription(), addr);
                  return new MatchHeaderSpace(
                      hs, TraceElement.builder().add("Match source address", vsi).build());
                })
            .collect(ImmutableList.toImmutableList());
    List<AclLineMatchExpr> dstAddrExprs =
        Sets.intersection(dstAddrs, c.getIpSpaces().keySet()).stream()
            .map(
                addr -> {
                  HeaderSpace hs =
                      HeaderSpace.builder().setDstIps(new IpSpaceReference(addr)).build();
                  VendorStructureId vsi =
                      new VendorStructureId(
                          _filename, FortiosStructureType.ADDRESS.getDescription(), addr);
                  return new MatchHeaderSpace(
                      hs, TraceElement.builder().add("Match destination address", vsi).build());
                })
            .collect(ImmutableList.toImmutableList());
    List<AclLineMatchExpr> svcExprs =
        Sets.intersection(services, convertedServices.keySet()).stream()
            .map(convertedServices::get)
            .collect(ImmutableList.toImmutableList());
    if (srcAddrExprs.isEmpty() || dstAddrExprs.isEmpty() || services.isEmpty()) {
      String emptyField =
          srcAddrExprs.isEmpty()
              ? "source addresses"
              : dstAddrExprs.isEmpty() ? "destination addresses" : "services";
      _w.redFlag(
          String.format(
              "Policy %s will not match any packets because none of its %s were successfully"
                  + " converted",
              numAndName, emptyField));
    }
    matchConjuncts.add(or(srcAddrExprs));
    matchConjuncts.add(or(dstAddrExprs));
    matchConjuncts.add(or(svcExprs)); // TODO confirm services should be disjoined

    line.setMatchCondition(and(matchConjuncts.build()));
    String viName = computeViPolicyName(policy);
    IpAccessList.builder().setOwner(c).setName(viName).setLines(line.build()).build();
  }

  private void convertInterface(Interface iface, Configuration c) {
    InterfaceType type = toViType(iface.getTypeEffective());
    if (type == null) {
      _w.redFlag(
          String.format(
              "Interface %s has unsupported type %s and will not be converted",
              iface.getName(), iface.getTypeEffective()));
      return;
    }
    String vdom = iface.getVdom();
    assert vdom != null; // An interface with no VDOM set should fail in extraction
    String vrfName = computeVrfName(vdom, iface.getVrfEffective());
    // Referencing a VRF in an interface implicitly creates it
    Vrf vrf = c.getVrfs().computeIfAbsent(vrfName, name -> Vrf.builder().setName(name).build());
    org.batfish.datamodel.Interface.Builder viIface =
        org.batfish.datamodel.Interface.builder()
            .setOwner(c)
            .setName(iface.getName())
            .setVrf(vrf)
            .setDescription(iface.getDescription())
            .setActive(iface.getStatusEffective())
            .setAddress(iface.getIp())
            .setMtu(iface.getMtuEffective())
            .setType(type)
            // TODO Check whether FortiOS should use outgoing filter or outgoing original flow
            //  filter (i.e. whether policies act on post-NAT or original flows)
            .setOutgoingFilter(generateOutgoingFilter(iface, c));
    // TODO Is this the right VI field for interface alias?
    Optional.ofNullable(iface.getAlias())
        .ifPresent(alias -> viIface.setDeclaredNames(ImmutableList.of(iface.getAlias())));
    viIface.build();
  }

  private @Nullable InterfaceType toViType(Interface.Type vsType) {
    switch (vsType) {
      case LOOPBACK:
        return InterfaceType.LOOPBACK;
      case PHYSICAL:
        return InterfaceType.PHYSICAL;
      case TUNNEL:
        return InterfaceType.TUNNEL;
      case EMAC_VLAN:
      case VLAN:
        return InterfaceType.VLAN;
      case AGGREGATE: // TODO Distinguish between AGGREGATED and AGGREGATE_CHILD
      case REDUNDANT: // TODO Distinguish between REDUNDANT and REDUNDANT_CHILD
      case WL_MESH: // TODO Support this type
      default:
        return null;
    }
  }

  private @Nonnull IpAccessList generateOutgoingFilter(Interface iface, Configuration c) {
    List<AclLine> lines = new ArrayList<>();
    for (Policy policy : _policies.values()) {
      if (!policy.getDstIntf().contains(iface.getName())) {
        continue; // policy doesn't apply to traffic out this interface
      }
      String viPolicyName = computeViPolicyName(policy);
      if (!c.getIpAccessLists().containsKey(viPolicyName)) {
        continue; // policy didn't convert
      }

      // Policy applies to traffic out this iface. Match traffic from its specified source ifaces.
      AclLineMatchExpr matchSources = new MatchSrcInterface(policy.getSrcIntf());

      // Each policy can only either allow or deny, so no need to create separate lines to match
      // permitted and denied traffic. (Ideally would use an AclAclLine, but can't AND that with the
      // matchSources expr.)
      // TODO This may need to change once we support action IPSEC.
      Policy.Action policyAction = policy.getActionEffective();
      checkState( // not a warning because other policies should not have been converted
          policyAction == Policy.Action.ALLOW || policyAction == Policy.Action.DENY,
          "Policies with actions other than ALLOW and DENY are not supported");
      boolean policyPermits = policy.getActionEffective() == Policy.Action.ALLOW;
      AclLineMatchExpr policyMatches =
          policyPermits ? new PermittedByAcl(viPolicyName) : new DeniedByAcl(viPolicyName);
      AclLineMatchExpr matchExpr =
          and(matchPolicyTraceElement(policy, _filename), matchSources, policyMatches);
      lines.add(
          policyPermits ? ExprAclLine.accepting(matchExpr) : ExprAclLine.rejecting(matchExpr));
    }

    lines.add(ExprAclLine.REJECT_ALL); // Default reject (including if no policies apply)
    return IpAccessList.builder()
        .setOwner(c)
        .setName(computeOutgoingFilterName(iface.getName()))
        .setLines(lines)
        .build();
  }

  /** Computes the VI name for the given policy. */
  public static @Nonnull String computeViPolicyName(Policy policy) {
    return computeViPolicyName(policy.getName(), policy.getNumber());
  }

  /** Computes the VI name for a policy with the given name and number. */
  @VisibleForTesting
  public static @Nonnull String computeViPolicyName(@Nullable String name, String number) {
    // TODO: Might need to generate IpAccessList names per VRF/VDOM
    return Optional.ofNullable(name).orElseGet(() -> String.format("~UNNAMED~POLICY~%s~", number));
  }

  /** Computes the VI name for a VRF in the given VDOM with the given VRF number. */
  @VisibleForTesting
  public static @Nonnull String computeVrfName(String vdom, int vrf) {
    return String.format("%s:%s", vdom, vrf);
  }

  /** Computes the VI name for the given interface's outgoing filter. */
  public static @Nonnull String computeOutgoingFilterName(String iface) {
    return String.format("~%s~outgoing~", iface);
  }
}
