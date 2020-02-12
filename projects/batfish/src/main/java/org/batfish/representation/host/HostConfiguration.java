package org.batfish.representation.host;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Maps.immutableEntry;
import static org.batfish.specifier.Location.interfaceLinkLocation;
import static org.batfish.specifier.Location.interfaceLocation;
import static org.batfish.specifier.LocationInfoUtils.configuredIps;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import org.batfish.common.VendorConversionException;
import org.batfish.common.Warnings;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.AclLine;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.EmptyIpSpace;
import org.batfish.datamodel.ExprAclLine;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.Route;
import org.batfish.datamodel.StaticRoute;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.acl.AclLineMatchExpr;
import org.batfish.datamodel.acl.FalseExpr;
import org.batfish.datamodel.acl.MatchHeaderSpace;
import org.batfish.datamodel.acl.TrueExpr;
import org.batfish.representation.iptables.IptablesVendorConfiguration;
import org.batfish.specifier.LocationInfo;
import org.batfish.vendor.VendorConfiguration;

public class HostConfiguration extends VendorConfiguration {

  private static final String FILTER_FORWARD = "filter::FORWARD";

  private static final String FILTER_INPUT = "filter::INPUT";

  private static final String FILTER_OUTPUT = "filter::OUTPUT";

  private static final String MANGLE_FORWARD = "mangle::FORWARD";

  private static final String MANGLE_INPUT = "mangle::INPUT";

  private static final String MANGLE_OUTPUT = "mangle::OUTPUT";

  private static final String MANGLE_POSTROUTING = "mangle::POSTROUTING";

  private static final String MANGLE_PREROUTING = "mangle::PREROUTING";

  private static final String NAT_OUTPUT = "nat::OUTPUT";

  private static final String NAT_PREROUTING = "nat::PREROUTING";
  private static final String PROP_HOST_INTERFACES = "hostInterfaces";
  private static final String PROP_HOSTNAME = "hostname";
  private static final String PROP_IPTABLES_FILE = "iptablesFile";
  private static final String PROP_OVERLAY = "overlay";

  private static final String RAW_OUTPUT = "raw::OUTPUT";

  private static final String RAW_PREROUTING = "raw::PREROUTING";

  private static final LocationInfo HOST_INTERFACE_LINK_LOCATION_INFO =
      new LocationInfo(false, EmptyIpSpace.INSTANCE, EmptyIpSpace.INSTANCE);

  private static LocationInfo hostInterfaceLocationInfo(Interface iface) {
    return new LocationInfo(
        // host interface is a traffic source
        true,
        // default source IPs
        configuredIps(iface),
        // interface locations do not have external ARP IPs
        EmptyIpSpace.INSTANCE);
  }

  public static HostConfiguration fromJson(String filename, String text, Warnings warnings)
      throws IOException {
    HostConfiguration hostConfiguration =
        BatfishObjectMapper.mapper().readValue(text, HostConfiguration.class);
    hostConfiguration.setWarnings(warnings);
    hostConfiguration.setFilename(filename);
    return hostConfiguration;
  }

  private Configuration _c;

  private Map<String, HostInterface> _hostInterfaces;

  private String _hostname;

  private String _iptablesFile;

  private IptablesVendorConfiguration _iptablesVendorConfig;

  private boolean _overlay;

  private final Set<HostStaticRoute> _staticRoutes;

  private transient VendorConfiguration _underlayConfiguration;

  public HostConfiguration() {
    _hostInterfaces = new TreeMap<>();
    _staticRoutes = new TreeSet<>();
  }

  @JsonProperty(PROP_HOST_INTERFACES)
  public Map<String, HostInterface> getHostInterfaces() {
    return _hostInterfaces;
  }

  @JsonProperty(PROP_HOSTNAME)
  @Override
  public String getHostname() {
    return _hostname;
  }

  @JsonProperty(PROP_IPTABLES_FILE)
  public String getIptablesFile() {
    return _iptablesFile;
  }

  public IptablesVendorConfiguration getIptablesVendorConfig() {
    return _iptablesVendorConfig;
  }

  @JsonProperty(PROP_OVERLAY)
  public boolean getOverlay() {
    return _overlay;
  }

  @Override
  public void setHostname(String hostname) {
    checkNotNull(hostname, "'hostname' cannot be null");
    _hostname = hostname.toLowerCase();
  }

  public void setIptablesFile(String file) {
    _iptablesFile = file;
  }

  public void setIptablesVendorConfig(IptablesVendorConfiguration config) {
    _iptablesVendorConfig = config;
  }

  @JsonProperty(PROP_OVERLAY)
  public void setOverlay(boolean overlay) {
    _overlay = overlay;
  }

  @JsonIgnore
  @Override
  public void setVendor(ConfigurationFormat format) {
    throw new UnsupportedOperationException("Cannot set vendor for host configuration");
  }

  private boolean simple() {
    String[] aclsToCheck =
        new String[] {
          RAW_PREROUTING,
          MANGLE_PREROUTING,
          NAT_PREROUTING,
          MANGLE_INPUT,
          RAW_OUTPUT,
          MANGLE_OUTPUT,
          NAT_OUTPUT,
          MANGLE_FORWARD,
          FILTER_FORWARD,
          MANGLE_POSTROUTING
        };
    for (String aclName : aclsToCheck) {
      IpAccessList acl = _c.getIpAccessLists().get(aclName);
      if (acl != null) {
        for (AclLine l : acl.getLines()) {
          // Based on the below comment, no non-ExprAclLine line counts as simple
          if (!(l instanceof ExprAclLine)) {
            return false;
          }
          ExprAclLine line = (ExprAclLine) l;
          if (line.getAction() == LineAction.DENY) {
            return false;
          }
          /*
           * This will have to change when ACLs are more complicated.
           * For now, a simple line is either TrueExpr, FalseExpr, or unrestricted MatchHeaderSpace.
           */
          AclLineMatchExpr matchCondition = line.getMatchCondition();
          if (!(matchCondition instanceof TrueExpr
              || matchCondition instanceof FalseExpr
              || (matchCondition instanceof MatchHeaderSpace
                  && ((MatchHeaderSpace) matchCondition).getHeaderspace().unrestricted()))) {
            /* At least one line is complicated, so the whole ACL is complicated */
            return false;
          }
        }
      }
    }
    return true;
  }

  @Override
  public List<Configuration> toVendorIndependentConfigurations() throws VendorConversionException {
    if (_underlayConfiguration != null) {
      _hostInterfaces.forEach(
          (name, iface) ->
              iface.setCanonicalName(_underlayConfiguration.canonicalizeInterfaceName(name)));
    } else {
      _hostInterfaces.forEach((name, iface) -> iface.setCanonicalName(name));
    }
    String hostname = getHostname();
    _c = new Configuration(hostname, ConfigurationFormat.HOST);
    _c.setDefaultCrossZoneAction(LineAction.PERMIT);
    _c.setDefaultInboundAction(LineAction.PERMIT);
    _c.getVrfs().put(Configuration.DEFAULT_VRF_NAME, new Vrf(Configuration.DEFAULT_VRF_NAME));

    // add interfaces
    _hostInterfaces
        .values()
        .forEach(
            hostInterface -> {
              String canonicalName = hostInterface.getCanonicalName();
              Interface newIface = hostInterface.toInterface(_c, _w);
              _c.getAllInterfaces().put(canonicalName, newIface);
            });

    // add iptables
    if (_iptablesVendorConfig != null) {
      _iptablesVendorConfig.addAsIpAccessLists(_c, this, _w);
    }

    // apply acls to interfaces
    if (simple()) {
      for (Interface iface : _c.getAllInterfaces(Configuration.DEFAULT_VRF_NAME).values()) {
        iface.setIncomingFilter(_c.getIpAccessLists().get(FILTER_INPUT));
        iface.setOutgoingFilter(_c.getIpAccessLists().get(FILTER_OUTPUT));
      }
    } else {
      _w.unimplemented("Do not support complicated iptables rules yet");
    }

    _c.getDefaultVrf()
        .getStaticRoutes()
        .addAll(
            _staticRoutes.stream().map(HostStaticRoute::toStaticRoute).collect(Collectors.toSet()));
    Set<StaticRoute> staticRoutes = _c.getDefaultVrf().getStaticRoutes();
    for (HostInterface iface : _hostInterfaces.values()) {
      Ip gateway = iface.getGateway();
      if (gateway != null) {
        staticRoutes.add(
            StaticRoute.builder()
                .setNetwork(Prefix.ZERO)
                .setNextHopIp(gateway)
                .setNextHopInterface(iface.getName())
                .setAdministrativeCost(HostStaticRoute.DEFAULT_ADMINISTRATIVE_COST)
                .setTag(Route.UNSET_ROUTE_TAG)
                .build());
        break;
      }
    }
    if (_staticRoutes.isEmpty() && staticRoutes.isEmpty() && !_c.getAllInterfaces().isEmpty()) {
      String ifaceName = _c.getAllInterfaces().values().iterator().next().getName();
      _c.getDefaultVrf()
          .getStaticRoutes()
          .add(
              StaticRoute.builder()
                  .setNetwork(Prefix.ZERO)
                  .setNextHopInterface(ifaceName)
                  .setAdministrativeCost(HostStaticRoute.DEFAULT_ADMINISTRATIVE_COST)
                  .setTag(Route.UNSET_ROUTE_TAG)
                  .build());
    }

    // create LocationInfo for each link location on the instance.
    _c.setLocationInfo(
        _c.getAllInterfaces().values().stream()
            .flatMap(
                iface ->
                    Stream.of(
                        immutableEntry(interfaceLocation(iface), hostInterfaceLocationInfo(iface)),
                        immutableEntry(
                            interfaceLinkLocation(iface), HOST_INTERFACE_LINK_LOCATION_INFO)))
            .collect(ImmutableMap.toImmutableMap(Entry::getKey, Entry::getValue)));

    return ImmutableList.of(_c);
  }

  public List<Configuration> toVendorIndependentConfigurations(
      VendorConfiguration underlayConfiguration) {
    _underlayConfiguration = underlayConfiguration;
    return toVendorIndependentConfigurations();
  }

  @VisibleForTesting
  static @Nonnull Map<String, HostInterface> toHostInterfaces(JsonNode hostInterfacesNode) {
    Map<String, HostInterface> hostInterfaces;
    if (hostInterfacesNode == null) {
      hostInterfaces = ImmutableMap.of();
    } else if (hostInterfacesNode instanceof ObjectNode) {
      hostInterfaces =
          BatfishObjectMapper.mapper()
              .convertValue(hostInterfacesNode, new TypeReference<Map<String, HostInterface>>() {});
      hostInterfaces.forEach(
          (name, hostInterface) ->
              checkArgument(
                  name.equals(hostInterface.getName()),
                  "Mismatch between hostInterface key '%s' and name '%s'",
                  name,
                  hostInterface.getName()));
    } else {
      assert hostInterfacesNode instanceof ArrayNode;
      List<HostInterface> hostInterfacesList =
          BatfishObjectMapper.mapper()
              .convertValue(hostInterfacesNode, new TypeReference<List<HostInterface>>() {});
      hostInterfaces =
          hostInterfacesList.stream()
              .collect(
                  ImmutableMap.toImmutableMap(
                      // use latest definition in case of duplicates
                      HostInterface::getName, Function.identity(), (hi1, hi2) -> hi2));
    }
    return hostInterfaces;
  }

  @JsonProperty(PROP_HOST_INTERFACES)
  private void setHostInterfaces(JsonNode hostInterfacesNode) {
    _hostInterfaces = toHostInterfaces(hostInterfacesNode);
  }
}
