package org.batfish.representation.cisco;

import java.io.Serializable;
import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import javax.annotation.Nullable;
import org.batfish.datamodel.InterfaceAddress;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpWildcard;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.eigrp.EigrpMetric;
import org.batfish.datamodel.eigrp.EigrpProcessMode;

public class EigrpProcess implements Serializable {

  private static final String DEFAULT_ADDRESS_FAMILY = "ipv4-unicast";
  private static final long serialVersionUID = 1L;
  private String _addressFamily;
  private long _asn;
  private boolean _autoSummary;
  private @Nullable EigrpMetric _defaultMetric;
  private EigrpProcessMode _mode;
  private Ip _routerId;
  private Set<IpWildcard> _wildcardNetworks;
  private Set<Prefix> _networks;
  private boolean _passiveInterfaceDefault;
  private Map<RoutingProtocol, EigrpRedistributionPolicy> _redistributionPolicies;

  public EigrpProcess(Long asn, EigrpProcessMode mode) {
    _asn = asn;
    _addressFamily = DEFAULT_ADDRESS_FAMILY;
    _autoSummary = false;
    _mode = mode;
    _networks = new TreeSet<>();
    _redistributionPolicies = new EnumMap<>(RoutingProtocol.class);
    _wildcardNetworks = new TreeSet<>();
  }

  public void computeNetworks(Collection<Interface> interfaces) {
    for (Interface i : interfaces) {
      InterfaceAddress address = i.getAddress();
      if (address == null) {
        continue;
      }
      for (IpWildcard wn : _wildcardNetworks) {
        // Check if the interface IP address matches the eigrp network
        // when the wildcard is ORed to both
        long wildcardLong = wn.getWildcard().asLong();
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

  public long getAsn() {
    return _asn;
  }

  public void setAsn(long asn) {
    _asn = asn;
  }

  public boolean getAutoSummary() {
    return _autoSummary;
  }

  public void setAutoSummary(boolean enable) {
    _autoSummary = enable;
  }

  @Nullable
  public EigrpMetric getDefaultMetric() {
    return _defaultMetric;
  }

  public void setDefaultMetric(@Nullable EigrpMetric metric) {
    _defaultMetric = metric;
  }

  public EigrpProcessMode getMode() {
    return _mode;
  }

  public Set<Prefix> getNetworks() {
    return _networks;
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

  public Ip getRouterId() {
    return _routerId;
  }

  public void setRouterId(Ip routerId) {
    _routerId = routerId;
  }

  public Set<IpWildcard> getWildcardNetworks() {
    return _wildcardNetworks;
  }
}
