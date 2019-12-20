package org.batfish.representation.host;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.common.VendorConversionException;
import org.batfish.common.Warnings;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.AclLine;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
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
  private static final String PROP_STATIC_ROUTES = "staticRoutes";

  private static final String RAW_OUTPUT = "raw::OUTPUT";

  private static final String RAW_PREROUTING = "raw::PREROUTING";

  public static HostConfiguration fromJson(String filename, String text, Warnings warnings)
      throws IOException {
    HostConfiguration hostConfiguration =
        BatfishObjectMapper.mapper().readValue(text, HostConfiguration.class);
    hostConfiguration.setWarnings(warnings);
    hostConfiguration.setFilename(filename);
    return hostConfiguration;
  }

  private Configuration _c;

  private final Map<String, HostInterface> _hostInterfaces;

  private String _hostname;

  private String _iptablesFile;

  private IptablesVendorConfiguration _iptablesVendorConfig;

  private boolean _overlay;

  private final Set<HostStaticRoute> _staticRoutes;

  private transient VendorConfiguration _underlayConfiguration;

  @JsonCreator
  private static @Nonnull HostConfiguration create(
      @JsonProperty(PROP_HOST_INTERFACES) @Nullable JsonNode hostInterfacesNode,
      @JsonProperty(PROP_STATIC_ROUTES) @Nullable List<HostStaticRoute> staticRoutes) {
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
              .collect(ImmutableMap.toImmutableMap(HostInterface::getName, Function.identity()));
    }
    return new HostConfiguration(
        hostInterfaces, ImmutableSet.copyOf(firstNonNull(staticRoutes, ImmutableSet.of())));
  }

  private HostConfiguration(
      Map<String, HostInterface> hostInterfaces, Set<HostStaticRoute> staticRoutes) {
    _hostInterfaces = hostInterfaces;
    _staticRoutes = staticRoutes;
  }

  public HostConfiguration() {
    this(ImmutableMap.of(), ImmutableSet.of());
  }

  @JsonIgnore
  public @Nonnull Map<String, HostInterface> getHostInterfaces() {
    return _hostInterfaces;
  }

  @JsonIgnore
  @Override
  public String getHostname() {
    return _hostname;
  }

  @JsonIgnore
  public String getIptablesFile() {
    return _iptablesFile;
  }

  @JsonIgnore
  public IptablesVendorConfiguration getIptablesVendorConfig() {
    return _iptablesVendorConfig;
  }

  @JsonIgnore
  public boolean getOverlay() {
    return _overlay;
  }

  @Override
  @JsonProperty(PROP_HOSTNAME)
  public void setHostname(String hostname) {
    checkNotNull(hostname, "'hostname' cannot be null");
    _hostname = hostname.toLowerCase();
  }

  @JsonProperty(PROP_IPTABLES_FILE)
  public void setIptablesFile(String file) {
    _iptablesFile = file;
  }

  @JsonIgnore
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
              _c.getDefaultVrf().getInterfaces().put(canonicalName, newIface);
            });

    // add iptables
    if (_iptablesVendorConfig != null) {
      _iptablesVendorConfig.addAsIpAccessLists(_c, this, _w);
    }

    // apply acls to interfaces
    if (simple()) {
      for (Interface iface : _c.getDefaultVrf().getInterfaces().values()) {
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
    return ImmutableList.of(_c);
  }

  public List<Configuration> toVendorIndependentConfigurations(
      VendorConfiguration underlayConfiguration) {
    _underlayConfiguration = underlayConfiguration;
    return toVendorIndependentConfigurations();
  }
}
