package org.batfish.representation.cisco_asa;

import java.io.Serializable;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpWildcard;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.eigrp.EigrpMetric;
import org.batfish.datamodel.eigrp.EigrpProcessMode;

public class EigrpProcess implements Serializable {

  private static final String DEFAULT_ADDRESS_FAMILY = "ipv4-unicast";

  private final Map<String, Boolean> _interfacePassiveStatus;
  private final EigrpProcessMode _mode;
  private final Set<IpWildcard> _wildcardNetworks;
  private final Set<Prefix> _networks;
  private @Nullable DistributeList _inboundGlobalDistributeList;
  private @Nullable DistributeList _outboundGlobalDistributeList;
  private @Nonnull Map<String, DistributeList> _inboundInterfaceDistributeLists;
  private @Nonnull Map<String, DistributeList> _outboundInterfaceDistributeLists;
  private final Map<RoutingProtocol, EigrpRedistributionPolicy> _redistributionPolicies;
  private final @Nonnull String _vrfName;
  private String _addressFamily;
  private @Nullable Long _asn;
  private boolean _autoSummary;
  private @Nullable EigrpMetric _defaultMetric;
  private @Nullable Ip _routerId;
  private boolean _passiveInterfaceDefault;
  private @Nullable Boolean _shutdown;

  public EigrpProcess(@Nullable Long asn, EigrpProcessMode mode, @Nonnull String vrfName) {
    _asn = asn;
    _addressFamily = DEFAULT_ADDRESS_FAMILY;
    _autoSummary = false;
    _interfacePassiveStatus = new TreeMap<>();
    _mode = mode;
    _networks = new TreeSet<>();
    _inboundInterfaceDistributeLists = new HashMap<>(0);
    _outboundInterfaceDistributeLists = new HashMap<>(0);
    _redistributionPolicies = new EnumMap<>(RoutingProtocol.class);
    _vrfName = vrfName;
    _wildcardNetworks = new TreeSet<>();
  }

  public void computeNetworks(Collection<Interface> interfaces) {
    for (Interface i : interfaces) {
      ConcreteInterfaceAddress address = i.getAddress();
      if (address == null) {
        continue;
      }
      for (IpWildcard wn : _wildcardNetworks) {
        // Check if the interface IP address matches the eigrp network
        // when the wildcard is ORed to both
        long wildcardLong = wn.getWildcardMask();
        long eigrpNetworkLong = wn.getIp().asLong();
        long intIpLong = address.getIp().asLong();
        long wildcardedNetworkLong = eigrpNetworkLong | wildcardLong;
        long wildcardedIntIpLong = intIpLong | wildcardLong;
        if (wildcardedNetworkLong == wildcardedIntIpLong) {
          // since we have a match, we add the INTERFACE network, ignoring
          // the wildcard stuff from before
          _networks.add(address.getPrefix());
          break;
        }
      }
    }
  }

  public String getAddressFamily() {
    return _addressFamily;
  }

  public void setAddressFamily(String af) {
    _addressFamily = af;
  }

  public @Nullable Long getAsn() {
    return _asn;
  }

  public void setAsn(@Nullable Long asn) {
    _asn = asn;
  }

  public boolean getAutoSummary() {
    return _autoSummary;
  }

  public void setAutoSummary(boolean enable) {
    _autoSummary = enable;
  }

  public @Nullable EigrpMetric getDefaultMetric() {
    return _defaultMetric;
  }

  public void setDefaultMetric(@Nullable EigrpMetric metric) {
    _defaultMetric = metric;
  }

  public Map<String, Boolean> getInterfacePassiveStatus() {
    return _interfacePassiveStatus;
  }

  public EigrpProcessMode getMode() {
    return _mode;
  }

  public Set<Prefix> getNetworks() {
    return _networks;
  }

  public @Nullable DistributeList getInboundGlobalDistributeList() {
    return _inboundGlobalDistributeList;
  }

  public void setInboundGlobalDistributeList(@Nullable DistributeList inboundGlobalDistributeList) {
    _inboundGlobalDistributeList = inboundGlobalDistributeList;
  }

  public @Nullable DistributeList getOutboundGlobalDistributeList() {
    return _outboundGlobalDistributeList;
  }

  public void setOutboundGlobalDistributeList(
      @Nullable DistributeList outboundGlobalDistributeList) {
    _outboundGlobalDistributeList = outboundGlobalDistributeList;
  }

  public @Nonnull Map<String, DistributeList> getInboundInterfaceDistributeLists() {
    return _inboundInterfaceDistributeLists;
  }

  public @Nonnull Map<String, DistributeList> getOutboundInterfaceDistributeLists() {
    return _outboundInterfaceDistributeLists;
  }

  public boolean getPassiveInterfaceDefault() {
    return _passiveInterfaceDefault;
  }

  public void setPassiveInterfaceDefault(boolean passiveInterfaceDefault) {
    _passiveInterfaceDefault = passiveInterfaceDefault;
  }

  public Map<RoutingProtocol, EigrpRedistributionPolicy> getRedistributionPolicies() {
    return _redistributionPolicies;
  }

  public @Nullable Ip getRouterId() {
    return _routerId;
  }

  public void setRouterId(@Nullable Ip routerId) {
    _routerId = routerId;
  }

  public @Nullable Boolean getShutdown() {
    return _shutdown;
  }

  public void setShutdown(boolean shutdown) {
    _shutdown = shutdown;
  }

  public Set<IpWildcard> getWildcardNetworks() {
    return _wildcardNetworks;
  }

  public String getVrf() {
    return _vrfName;
  }
}
