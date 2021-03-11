package org.batfish.representation.fortios;

import static org.batfish.datamodel.acl.AclLineMatchExprs.and;
import static org.batfish.datamodel.acl.AclLineMatchExprs.or;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.common.VendorConversionException;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.DeviceModel;
import org.batfish.datamodel.ExprAclLine;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpSpaceReference;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.TraceElement;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.MatchHeaderSpace;
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

  private String _hostname;
  private final @Nonnull Map<String, Address> _addresses;
  private final @Nonnull Map<String, Interface> _interfaces;
  // Note: this is a LinkedHashMap to preserve insertion order
  private final @Nonnull Map<String, Policy> _policies;
  private final @Nonnull Map<BatfishUUID, FortiosRenameableObject> _renameableObjects;
  private final @Nonnull Map<String, Map<String, Replacemsg>> _replacemsgs;
  private final @Nonnull Map<String, Service> _services;

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
            .collect(ImmutableMap.toImmutableMap(Service::getName, svc -> svc.toMatchExpr(_w)));
    _policies.values().forEach(policy -> convertPolicy(policy, c, convertedServices));

    // Count structure references
    markConcreteStructure(FortiosStructureType.ADDRESS);
    markConcreteStructure(FortiosStructureType.SERVICE_CUSTOM);
    markConcreteStructure(FortiosStructureType.INTERFACE);
    return c;
  }

  private void convertPolicy(
      Policy policy, Configuration c, Map<String, AclLineMatchExpr> convertedServices) {
    if (policy.getStatusEffective() != Policy.Status.ENABLE) {
      return;
    }

    // TODO Incorporate policy.getComments()
    String number = policy.getNumber();
    @Nullable String name = policy.getName();
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
      String numAndName = name == null ? number : String.format("%s (%s)", number, name);
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

    // construct line
    ExprAclLine.Builder line =
        policy.getActionEffective() == Policy.Action.ALLOW
            ? ExprAclLine.accepting()
            : ExprAclLine.rejecting();
    line.setMatchCondition(and(matchConjuncts.build()));

    String viName = computeViPolicyName(policy);
    IpAccessList.builder().setOwner(c).setName(viName).setLines(line.build()).build();
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
}
