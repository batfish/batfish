package org.batfish.datamodel;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static org.batfish.datamodel.InactiveReason.ADMIN_DOWN;
import static org.batfish.datamodel.InactiveReason.BLACKLISTED;
import static org.batfish.datamodel.InactiveReason.FORCED_LINE_DOWN;
import static org.batfish.datamodel.InactiveReason.NODE_DOWN;
import static org.batfish.datamodel.InactiveReason.PHYSICAL_NEIGHBOR_DOWN;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.ImmutableSortedSet;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.function.Supplier;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.util.ComparableStructure;
import org.batfish.datamodel.collections.NodeInterfacePair;
import org.batfish.datamodel.eigrp.EigrpInterfaceSettings;
import org.batfish.datamodel.hsrp.HsrpGroup;
import org.batfish.datamodel.isis.IsisInterfaceSettings;
import org.batfish.datamodel.ospf.OspfInterfaceSettings;
import org.batfish.datamodel.ospf.OspfNetworkType;
import org.batfish.datamodel.transformation.Transformation;

public final class Interface extends ComparableStructure<String> {

  public static class Builder {

    private @Nullable Integer _accessVlan;
    private boolean _adminUp;
    private InterfaceAddress _address;
    private @Nonnull Map<ConcreteInterfaceAddress, ConnectedRouteMetadata> _addressMetadata;
    private @Nullable IntegerSpace _allowedVlans;
    private boolean _autoState;
    private @Nullable Double _bandwidth;
    private @Nullable String _channelGroup;
    private @Nonnull SortedSet<String> _channelGroupMembers;
    private SortedSet<String> _declaredNames;
    private @Nonnull Set<Dependency> _dependencies = ImmutableSet.of();
    private @Nullable String _description;
    private @Nonnull SortedSet<Ip> _dhcpRelayAddresses;
    private @Nullable EigrpInterfaceSettings _eigrp;
    private @Nullable Integer _encapsulationVlan;
    private boolean _hmm;
    private Map<Integer, HsrpGroup> _hsrpGroups;
    private String _hsrpVersion;
    private @Nullable String _humanName;
    private FirewallSessionInterfaceInfo _firewallSessionInterfaceInfo;
    private @Nullable IpAccessList _incomingFilter;
    private Transformation _incomingTransformation;
    private IsisInterfaceSettings _isis;
    private @Nullable Boolean _lineUp;
    private @Nullable Integer _mlagId;
    private @Nullable Integer _mtu;
    private @Nullable String _name;
    private @Nullable Supplier<String> _nameGenerator;
    private @Nullable Integer _nativeVlan;
    private OspfInterfaceSettings _ospfSettings;
    private @Nullable IpAccessList _outgoingFilter;
    private @Nullable IpAccessList _outgoingOriginalFlowFilter;
    private Transformation _outgoingTransformation;
    private Configuration _owner;
    private String _packetPolicy;
    private @Nullable IpAccessList _postTransformationIncomingFilter;
    private boolean _proxyArp;
    private @Nullable IpAccessList _preTransformationOutgoingFilter;
    private Set<? extends InterfaceAddress> _secondaryAddresses;
    private @Nullable Double _speed;
    private @Nullable Boolean _switchport;
    private @Nullable SwitchportMode _switchportMode;
    private @Nonnull IpSpace _additionalArpIps;
    private @Nullable TunnelConfiguration _tunnelConfig;
    private InterfaceType _type;
    private @Nullable Integer _vlan;
    private Vrf _vrf;
    private SortedMap<Integer, VrrpGroup> _vrrpGroups;
    private @Nullable String _zoneName;

    private Builder(@Nullable Supplier<String> nameGenerator) {
      _addressMetadata = ImmutableMap.of();
      _additionalArpIps = EmptyIpSpace.INSTANCE;
      _adminUp = true;
      _autoState = true;
      _channelGroupMembers = ImmutableSortedSet.of();
      _declaredNames = ImmutableSortedSet.of();
      _dhcpRelayAddresses = ImmutableSortedSet.of();
      _hsrpGroups = ImmutableMap.of();
      _nameGenerator = nameGenerator;
      _secondaryAddresses = ImmutableSet.of();
      _vrrpGroups = ImmutableSortedMap.of();
    }

    public Interface build() {
      checkArgument(_name != null || _nameGenerator != null, "Must set name before building");
      checkArgument(_type != null, "Must set type before building");
      String name = _name != null ? _name : _nameGenerator.get();
      Interface iface = new Interface(name, _owner, _type);
      if (_owner != null) {
        _owner.getAllInterfaces().put(name, iface);
      }

      if (_accessVlan != null) {
        iface.setAccessVlan(_accessVlan);
      }

      // Set addresses. If the primary address is missing from allAddresses, add it.
      ImmutableSet.Builder<InterfaceAddress> allAddresses = ImmutableSet.builder();
      if (_address != null) {
        iface.setAddress(_address);
        allAddresses.add(_address);
      }
      iface.setAllAddresses(allAddresses.addAll(_secondaryAddresses).build());

      iface.setAdditionalArpIps(_additionalArpIps);
      iface.setAddressMetadata(ImmutableSortedMap.copyOf(_addressMetadata));
      if (_allowedVlans != null) {
        iface.setAllowedVlans(_allowedVlans);
      }
      iface.setAutoState(_autoState);
      iface.setBandwidth(_bandwidth);
      iface.setChannelGroup(_channelGroup);
      iface.setChannelGroupMembers(_channelGroupMembers);
      iface.setDeclaredNames(_declaredNames);
      iface.setDescription(_description);
      iface.setDependencies(_dependencies);
      iface.setDhcpRelayAddresses(ImmutableList.copyOf(_dhcpRelayAddresses));
      iface.setEigrp(_eigrp);
      iface.setEncapsulationVlan(_encapsulationVlan);
      iface.setHmm(_hmm);
      iface.setHsrpGroups(_hsrpGroups);
      iface.setHsrpVersion(_hsrpVersion);
      iface.setHumanName(_humanName);
      iface.setFirewallSessionInterfaceInfo(_firewallSessionInterfaceInfo);
      iface.setIncomingFilter(_incomingFilter);
      iface.setIncomingTransformation(_incomingTransformation);
      iface.setIsis(_isis);
      iface.setMlagId(_mlagId);
      if (_mtu != null) {
        iface.setMtu(_mtu);
      }
      if (_nativeVlan != null) {
        iface.setNativeVlan(_nativeVlan);
      }
      iface.setOutgoingFilter(_outgoingFilter);
      iface.setOutgoingOriginalFlowFilter(_outgoingOriginalFlowFilter);
      iface.setOutgoingTransformation(_outgoingTransformation);
      iface.setPostTransformationIncomingFilter(_postTransformationIncomingFilter);
      iface.setPreTransformationOutgoingFilter(_preTransformationOutgoingFilter);
      iface.setProxyArp(_proxyArp);
      iface.setPacketPolicy(_packetPolicy);
      iface.setSpeed(_speed);
      if (_switchport != null) {
        iface.setSwitchport(_switchport);
      }
      if (_switchportMode != null) {
        iface.setSwitchportMode(_switchportMode);
      }
      iface.setTunnelConfig(_tunnelConfig);
      if (_type != null) {
        iface.setInterfaceType(_type);
      }
      iface.setVlan(_vlan);

      iface.setVrf(_vrf);
      iface.setVrrpGroups(_vrrpGroups);
      iface.setZoneName(_zoneName);

      iface.setOspfSettings(_ospfSettings);
      processStatus(iface);
      return iface;
    }

    private void processStatus(Interface iface) {
      // Set interface status to be as "up" as possible:
      //
      // For interfaces without line status:
      // - Set active to value of _adminUp
      // - Throw IllegalStateException if _lineUp is non-null
      // For interfaces with line status:
      // - Set active to false iff _adminUp is false or _lineUp is false
      // - Set _lineUp to true if null.
      checkState(
          _lineUp == null || iface.hasLineStatus(),
          String.format(
              "Cannot set lineUp value for interface type: %s", iface.getInterfaceType()));
      if (!_adminUp) {
        iface.adminDown();
      }
      if (iface.hasLineStatus() && Boolean.FALSE.equals(_lineUp)) {
        iface.disconnect(FORCED_LINE_DOWN);
      }
    }

    public Builder setAdditionalArpIps(IpSpace additionalArpIps) {
      _additionalArpIps = additionalArpIps;
      return this;
    }

    /**
     * Set the primary address of the interface. <br>
     * The {@link Interface#getAllAddresses()} method of the built {@link Interface} will return a
     * set containing the primary address and secondary addresses. <br>
     * The node will accept traffic whose destination IP belongs is among any of the addresses of
     * any of the interfaces. The primary address is the one used by default as the source IP for
     * traffic sent out the interface. A secondary address is another address potentially associated
     * with a different subnet living on the interface. The interface will reply to ARP for the
     * primary or any secondary IP.
     */
    public Builder setAddress(InterfaceAddress address) {
      _address = address;
      return this;
    }

    /**
     * Set the primary address and secondary addresses of the interface. <br>
     * The {@link Interface#getAllAddresses()} method of the built {@link Interface} will return a
     * set containing the primary address and secondary addresses.<br>
     * The node will accept traffic whose destination IP is among any of the addresses of any of the
     * interfaces. The primary address is the one used by default as the source IP for traffic sent
     * out the interface. A secondary address is another address potentially associated with a
     * different subnet living on the interface. The interface will reply to ARP for the primary or
     * any secondary IP.
     */
    public Builder setAddresses(
        InterfaceAddress primaryAddress, InterfaceAddress... secondaryAddresses) {
      return setAddresses(primaryAddress, Arrays.asList(secondaryAddresses));
    }

    /**
     * Set the primary address and secondary addresses of the interface. <br>
     * The {@link Interface#getAllAddresses()} method of the built {@link Interface} will return a
     * set containing the primary address and secondary addresses.<br>
     * The node will accept traffic whose destination IP belongs is among any of the addresses of
     * any of the interfaces. The primary address is the one used by default as the source IP for
     * traffic sent out the interface. A secondary address is another address potentially associated
     * with a different subnet living on the interface. The interface will reply to ARP for the
     * primary or any secondary IP.
     */
    public Builder setAddresses(
        InterfaceAddress primaryAddress, Iterable<InterfaceAddress> secondaryAddresses) {
      _address = primaryAddress;
      _secondaryAddresses = ImmutableSet.copyOf(secondaryAddresses);
      return this;
    }

    public @Nonnull Builder setAccessVlan(@Nullable Integer accessVlan) {
      _accessVlan = accessVlan;
      return this;
    }

    public Builder setAddressMetadata(
        Map<ConcreteInterfaceAddress, ConnectedRouteMetadata> addressMetadata) {
      _addressMetadata = addressMetadata;
      return this;
    }

    public @Nonnull Builder setAdminUp(@Nullable Boolean adminUp) {
      _adminUp = adminUp;
      return this;
    }

    public @Nonnull Builder setAllowedVlans(@Nullable IntegerSpace allowedVlans) {
      _allowedVlans = allowedVlans;
      return this;
    }

    public @Nonnull Builder setAutoState(boolean autoState) {
      _autoState = autoState;
      return this;
    }

    public Builder setBandwidth(@Nullable Double bandwidth) {
      _bandwidth = bandwidth;
      return this;
    }

    public @Nonnull Builder setChannelGroup(@Nullable String channelGroup) {
      _channelGroup = channelGroup;
      return this;
    }

    public @Nonnull Builder setChannelGroupMembers(Iterable<String> channelGroupMembers) {
      _channelGroupMembers = ImmutableSortedSet.copyOf(channelGroupMembers);
      return this;
    }

    public Builder setDeclaredNames(Iterable<String> declaredNames) {
      _declaredNames = ImmutableSortedSet.copyOf(declaredNames);
      return this;
    }

    public Builder setDependencies(@Nonnull Iterable<Dependency> dependencies) {
      _dependencies = ImmutableSet.copyOf(dependencies);
      return this;
    }

    public Builder setDescription(@Nullable String description) {
      _description = description;
      return this;
    }

    public Builder setDhcpRelayAddresses(@Nonnull Iterable<Ip> addresses) {
      _dhcpRelayAddresses = ImmutableSortedSet.copyOf(addresses);
      return this;
    }

    public Builder setEigrp(@Nullable EigrpInterfaceSettings eigrp) {
      _eigrp = eigrp;
      return this;
    }

    public @Nonnull Builder setEncapsulationVlan(@Nullable Integer encapsulationVlan) {
      _encapsulationVlan = encapsulationVlan;
      return this;
    }

    public Builder setFirewallSessionInterfaceInfo(
        @Nullable FirewallSessionInterfaceInfo firewallSessionInterfaceInfo) {
      _firewallSessionInterfaceInfo = firewallSessionInterfaceInfo;
      return this;
    }

    public @Nonnull Builder setHmm(boolean hmm) {
      _hmm = hmm;
      return this;
    }

    public @Nonnull Builder setHsrpGroups(@Nonnull Map<Integer, HsrpGroup> hsrpGroups) {
      _hsrpGroups = ImmutableMap.copyOf(hsrpGroups);
      return this;
    }

    public @Nonnull Builder setHsrpVersion(@Nullable String hsrpVersion) {
      _hsrpVersion = hsrpVersion;
      return this;
    }

    public @Nonnull Builder setHumanName(@Nullable String humanName) {
      _humanName = humanName;
      return this;
    }

    public Builder setIncomingFilter(IpAccessList incomingFilter) {
      _incomingFilter = incomingFilter;
      return this;
    }

    public Builder setIncomingTransformation(Transformation incomingTransformation) {
      _incomingTransformation = incomingTransformation;
      return this;
    }

    public Builder setIsis(IsisInterfaceSettings isis) {
      _isis = isis;
      return this;
    }

    /** Should only be called in tests. */
    @VisibleForTesting
    public @Nonnull Builder setLineUp(@Nullable Boolean lineUp) {
      _lineUp = lineUp;
      return this;
    }

    public @Nonnull Builder setMlagId(@Nullable Integer mlagId) {
      _mlagId = mlagId;
      return this;
    }

    public Builder setMtu(@Nullable Integer mtu) {
      _mtu = mtu;
      return this;
    }

    public Builder setName(String name) {
      _name = name;
      return this;
    }

    public @Nonnull Builder setNativeVlan(@Nullable Integer nativeVlan) {
      _nativeVlan = nativeVlan;
      return this;
    }

    public Builder setOspfSettings(OspfInterfaceSettings ospfSettings) {
      _ospfSettings = ospfSettings;
      return this;
    }

    public Builder setOutgoingFilter(IpAccessList outgoingFilter) {
      _outgoingFilter = outgoingFilter;
      return this;
    }

    public Builder setOutgoingOriginalFlowFilter(IpAccessList outgoingOriginalFlowFilter) {
      _outgoingOriginalFlowFilter = outgoingOriginalFlowFilter;
      return this;
    }

    public Builder setOutgoingTransformation(Transformation outgoingTransformation) {
      _outgoingTransformation = outgoingTransformation;
      return this;
    }

    public Builder setOwner(Configuration owner) {
      _owner = owner;
      return this;
    }

    public Builder setPostTransformationIncomingFilter(
        IpAccessList postTransformationIncomingFilter) {
      _postTransformationIncomingFilter = postTransformationIncomingFilter;
      return this;
    }

    public Builder setPreTransformationOutgoingFilter(
        IpAccessList preTransformationOutgoingFilter) {
      _preTransformationOutgoingFilter = preTransformationOutgoingFilter;
      return this;
    }

    public Builder setProxyArp(boolean proxyArp) {
      _proxyArp = proxyArp;
      return this;
    }

    /** Set the policy-based routing (PBR) policy */
    public Builder setPacketPolicy(String packetPolicy) {
      _packetPolicy = packetPolicy;
      return this;
    }

    /**
     * Set the secondary addresses of the interface. <br>
     * The {@link Interface#getAllConcreteAddresses()} method of the built {@link Interface} will
     * return a set containing the primary address and secondary addresses.<br>
     * The node will accept traffic whose destination IP belongs is among any of the addresses of
     * any of the interfaces. The primary address is the one used by default as the source IP for
     * traffic sent out the interface. A secondary address is another address potentially associated
     * with a different subnet living on the interface. The interface will reply to ARP for the
     * primary or any secondary IP.
     */
    public Builder setSecondaryAddresses(Iterable<? extends InterfaceAddress> secondaryAddresses) {
      _secondaryAddresses = ImmutableSet.copyOf(secondaryAddresses);
      return this;
    }

    public @Nonnull Builder setSwitchport(@Nullable Boolean switchport) {
      _switchport = switchport;
      return this;
    }

    public @Nonnull Builder setSwitchportMode(@Nullable SwitchportMode switchportMode) {
      _switchportMode = switchportMode;
      return this;
    }

    public @Nonnull Builder setSpeed(@Nullable Double speed) {
      _speed = speed;
      return this;
    }

    public Builder setTunnelConfig(@Nullable TunnelConfiguration tunnelConfig) {
      _tunnelConfig = tunnelConfig;
      return this;
    }

    public Builder setType(InterfaceType type) {
      _type = type;
      return this;
    }

    public @Nonnull Builder setVlan(@Nullable Integer vlan) {
      _vlan = vlan;
      return this;
    }

    public Builder setVrf(Vrf vrf) {
      _vrf = vrf;
      return this;
    }

    public Builder setVrrpGroups(SortedMap<Integer, VrrpGroup> vrrpGroups) {
      _vrrpGroups = ImmutableSortedMap.copyOf(vrrpGroups);
      return this;
    }

    public Builder setZoneName(@Nullable String zoneName) {
      _zoneName = zoneName;
      return this;
    }
  }

  /** Type of interface dependency. Informs failure analysis and bandwidth computation */
  public enum DependencyType {
    /**
     * Aggregate dependency: interface which depends its constituents (aggregate interface stays up
     * as long as one of its dependencies is up)
     */
    AGGREGATE,
    /**
     * A bind dependency: required for fate sharing (bind interface goes down if its dependency goes
     * down)
     */
    BIND
  }

  /**
   * Represents a directional dependency between two interfaces. Owner of this object <b>depends
   * on</b> the interface name described by this object.
   *
   * <p>Note that it is important to record even mis-configured dependencies. Therefore callers may
   * not assume that the interface named by {@link #getInterfaceName()} actually exists.
   */
  @ParametersAreNonnullByDefault
  public static final class Dependency implements Serializable {
    private final @Nonnull String _interfaceName;
    private final @Nonnull DependencyType _type;

    public Dependency(String interfaceName, DependencyType type) {
      _interfaceName = interfaceName;
      _type = type;
    }

    /**
     * Note that the named interface may not actually exist on the device, e.g., if the user
     * configured a tunnel update-source that does not exist.
     */
    public @Nonnull String getInterfaceName() {
      return _interfaceName;
    }

    public @Nonnull DependencyType getType() {
      return _type;
    }

    @Override
    public boolean equals(@Nullable Object o) {
      if (this == o) {
        return true;
      }
      if (!(o instanceof Dependency)) {
        return false;
      }
      Dependency that = (Dependency) o;
      return _interfaceName.equals(that._interfaceName) && _type == that._type;
    }

    @Override
    public int hashCode() {
      return Objects.hash(_interfaceName, _type.ordinal());
    }

    @Override
    public String toString() {
      return MoreObjects.toStringHelper(Dependency.class)
          .add("interfaceName", _interfaceName)
          .add("type", _type)
          .toString();
    }
  }

  public static final int DEFAULT_MTU = 1500;
  public static final String DYNAMIC_INTERFACE_NAME = "dynamic";
  public static final String NULL_INTERFACE_NAME = "null_interface";
  public static final String UNSET_LOCAL_INTERFACE = "unset_local_interface";
  public static final String INVALID_LOCAL_INTERFACE = "invalid_local_interface";
  private static final String PROP_ACCESS_VLAN = "accessVlan";
  private static final String PROP_ACTIVE = "active";
  private static final String PROP_ADMIN_UP = "adminUp";
  private static final String PROP_ADDITIONAL_ARP_IPS = "additionalArpIps";
  private static final String PROP_ADDRESS_METADATA = "addressMetadata";
  private static final String PROP_ALL_PREFIXES = "allPrefixes";
  private static final String PROP_ALLOWED_VLANS = "allowedVlans";
  private static final String PROP_AUTOSTATE = "autostate";
  private static final String PROP_BANDWIDTH = "bandwidth";
  private static final String PROP_BLACKLISTED = "blacklisted";
  private static final String PROP_CHANNEL_GROUP = "channelGroup";
  private static final String PROP_CHANNEL_GROUP_MEMBERS = "channelGroupMembers";
  private static final String PROP_CRYPTO_MAP = "cryptoMap";
  private static final String PROP_DECLARED_NAMES = "declaredNames";
  private static final String PROP_DESCRIPTION = "description";
  private static final String PROP_DHCP_RELAY_ADDRESSES = "dhcpRelayAddresses";
  private static final String PROP_EIGRP = "eigrp";
  private static final String PROP_ENCAPSULATION_VLAN = "encapsulationVlan";
  private static final String PROP_FIREWALL_SESSION_INTERFACE_INFO = "firewallSessionInterfaceInfo";
  private static final String PROP_HMM = "hmm";
  private static final String PROP_HSRP_GROUPS = "hsrpGroups";
  private static final String PROP_HSRP_VERSION = "hsrpVersion";
  private static final String PROP_HUMAN_NAME = "humanName";
  private static final String PROP_INACTIVE_REASON = "inactiveReason";
  private static final String PROP_INBOUND_FILTER = "inboundFilter";
  private static final String PROP_INCOMING_FILTER = "incomingFilter";
  private static final String PROP_INCOMING_TRANSFORMATION = "incomingTransformation";
  private static final String PROP_INTERFACE_TYPE = "type";
  private static final String PROP_ISIS = "isis";
  private static final String PROP_LINE_UP = "lineUp";
  private static final String PROP_MLAG_ID = "mlagId";
  private static final String PROP_MTU = "mtu";
  private static final String PROP_NATIVE_VLAN = "nativeVlan";
  private static final String PROP_OSPF_SETTINGS = "ospfSettings";
  private static final String PROP_OUTGOING_FILTER = "outgoingFilter";
  private static final String PROP_OUTGOING_ORIGINAL_FLOW_FILTER = "outgoingOriginalFlowFilter";
  private static final String PROP_OUTGOING_TRANSFORMATION = "outgoingTransformation";
  private static final String PROP_POST_TRANSFORMATION_INCOMING_FILTER =
      "postTransformationIncomingFilter";
  private static final String PROP_PREFIX = "prefix";
  private static final String PROP_PRE_TRANSFORMATION_OUTGOING_FILTER =
      "preTransformationOutgoingFilter";
  private static final String PROP_PROXY_ARP = "proxyArp";
  private static final String PROP_RIP_ENABLED = "ripEnabled";
  private static final String PROP_RIP_PASSIVE = "ripPassive";
  private static final String PROP_ROUTING_POLICY = "routingPolicy";
  private static final String PROP_SPANNING_TREE_PORTFAST = "spanningTreePortfast";
  private static final String PROP_SPEED = "speed";
  private static final String PROP_SWITCHPORT = "switchport";
  private static final String PROP_SWITCHPORT_MODE = "switchportMode";
  private static final String PROP_SWITCHPORT_TRUNK_ENCAPSULATION = "switchportTrunkEncapsulation";
  private static final String PROP_TUNNEL_CONFIG = "tunnelConfig";
  private static final String PROP_VLAN = "vlan";
  private static final String PROP_VRF = "vrf";
  private static final String PROP_VRRP_GROUPS = "vrrpGroups";
  private static final String PROP_ZONE = "zone";

  public static Builder builder() {
    return new Builder(null);
  }

  public static Builder builder(Supplier<String> nameGenerator) {
    return new Builder(nameGenerator);
  }

  /** Returns {@code true} if this {@link Interface} is active and has L3 configuration. */
  @JsonIgnore
  public boolean isActiveL3() {
    return getActive() && !getSwitchport() && !getAllAddresses().isEmpty();
  }

  /**
   * Returns {@code true} if this {@link Interface} can be the source of an IPv4 packet.
   *
   * <p>Note that this means originating traffic in the VRF, not that traffic can be forwarded out a
   * Layer 3 link attached to this interface.
   */
  @JsonIgnore
  public boolean canOriginateIpTraffic() {
    return isActiveL3();
  }

  /**
   * Returns {@code true} if this {@link Interface} can send or receive an IPv4 packet on an L3 link
   * (which may not be in the snapshot).
   */
  @JsonIgnore
  public boolean canReceiveIpTraffic() {
    if (!isActiveL3()) {
      return false;
    } else if (isLoopback()) {
      // Loopbacks cannot have Layer 3 edges.
      return false;
    } else if (getLinkLocalAddress() != null) {
      // An LLA implies a link.
      return true;
    }
    for (ConcreteInterfaceAddress addr : getAllConcreteAddresses()) {
      if (addr.getPrefix().getPrefixLength() < Prefix.MAX_PREFIX_LENGTH) {
        // Any prefix shorter than /32 implies a possible (even if not in snapshot) L3 link.
        return true;
      }
    }
    return false;
  }

  /**
   * Returns {@code true} if this {@link Interface} can send an IPv4 packet on an L3 link (which may
   * not be in the snapshot).
   */
  @JsonIgnore
  public boolean canSendIpTraffic() {
    // TODO: intuitively, it feels like sending and receiving should be symmetric. However,
    // NHint routes will send arps that can be answered by any same-broadcast-domain device,
    // regardless of L3 compatibility. We are not yet able to fix this for the `enter[iface]`
    // case, but we can for the `exit[iface]` case.
    return isActiveL3() && !isLoopback();
  }

  private @Nullable Integer _accessVlan;
  private boolean _active;
  private @Nonnull IpSpace _additionalArpIps;
  private boolean _adminUp;
  private @Nonnull IntegerSpace _allowedVlans;
  private @Nonnull SortedSet<InterfaceAddress> _allAddresses;
  private @Nonnull SortedMap<ConcreteInterfaceAddress, ConnectedRouteMetadata> _addressMetadata;

  /** Cache of all concrete addresses */
  private @Nullable transient Set<ConcreteInterfaceAddress> _allConcreteAddresses;

  /** Cache of all link-local addresses */
  private @Nullable transient Set<LinkLocalAddress> _allLinkLocalAddresses;

  private boolean _autoState;
  private @Nullable Double _bandwidth;
  private @Nullable Boolean _blacklisted;
  private String _channelGroup;
  private SortedSet<String> _channelGroupMembers;
  private String _cryptoMap;
  private SortedSet<String> _declaredNames;

  /** Set of interface dependencies required for this interface to active */
  private @Nonnull Set<Dependency> _dependencies;

  private String _description;
  private List<Ip> _dhcpRelayAddresses;
  private @Nullable EigrpInterfaceSettings _eigrp;
  private @Nullable Integer _encapsulationVlan;
  private @Nullable FirewallSessionInterfaceInfo _firewallSessionInterfaceInfo;
  private boolean _hmm;
  private Map<Integer, HsrpGroup> _hsrpGroups;
  private @Nullable String _humanName;
  private @Nullable InactiveReason _inactiveReason;
  private @Nullable String _inboundFilterName;
  private @Nullable String _incomingFilterName;
  private Transformation _incomingTransformation;
  private InterfaceType _interfaceType;
  private IsisInterfaceSettings _isis;
  private @Nullable Boolean _lineUp;
  private @Nullable Integer _mlagId;
  private int _mtu;
  private @Nullable Integer _nativeVlan;
  private @Nullable OspfInterfaceSettings _ospfSettings;
  private @Nullable String _outgoingFilterName;
  private @Nullable String _outgoingOriginalFlowFilterName;
  private Transformation _outgoingTransformation;
  private Configuration _owner;
  private InterfaceAddress _address;
  private @Nullable String _postTransformationIncomingFilterName;
  private boolean _proxyArp;
  private @Nullable String _preTransformationOutgoingFilterName;
  private boolean _ripEnabled;
  private boolean _ripPassive;
  private String _packetPolicyName;
  private boolean _spanningTreePortfast;
  private @Nullable Double _speed;
  private boolean _switchport;
  private SwitchportMode _switchportMode;
  private SwitchportEncapsulationType _switchportTrunkEncapsulation;
  private @Nullable TunnelConfiguration _tunnelConfig;
  private Integer _vlan;
  private Vrf _vrf;
  private transient String _vrfName;
  private SortedMap<Integer, VrrpGroup> _vrrpGroups;
  private String _zoneName;
  private String _hsrpVersion;

  @JsonCreator
  private Interface(@JsonProperty(PROP_NAME) @Nullable String name) {
    this(name, null, InterfaceType.UNKNOWN);
  }

  private Interface(String name, Configuration owner, @Nonnull InterfaceType interfaceType) {
    super(name);
    _active = true;
    _adminUp = true;
    _additionalArpIps = EmptyIpSpace.INSTANCE;
    _addressMetadata = ImmutableSortedMap.of();
    _autoState = true;
    _allowedVlans = IntegerSpace.EMPTY;
    _allAddresses = ImmutableSortedSet.of();
    _channelGroupMembers = ImmutableSortedSet.of();
    _declaredNames = ImmutableSortedSet.of();
    _dependencies = ImmutableSet.of();
    _dhcpRelayAddresses = ImmutableList.of();
    _hsrpGroups = ImmutableSortedMap.of();
    updateInterfaceType(interfaceType);
    _mtu = DEFAULT_MTU;
    _owner = owner;
    _switchportMode = SwitchportMode.NONE;
    _switchportTrunkEncapsulation = SwitchportEncapsulationType.DOT1Q;
    _vrfName = Configuration.DEFAULT_VRF_NAME;
    _vrrpGroups = ImmutableSortedMap.of();
  }

  /**
   * Update interface type. Resets values for {@link #getBlacklisted()} and {@link #getLineUp()} to
   * defaults for {@code interfaceType}.
   *
   * <p>Should only be called on init or during conversion.
   */
  public void updateInterfaceType(InterfaceType interfaceType) {
    _interfaceType = interfaceType;
    if (hasLineStatus()) {
      _lineUp = true;
      _blacklisted = false;
    } else {
      _lineUp = null;
      _blacklisted = null;
    }
  }

  // TODO: add missing fields, clean up, implement hashCode
  @Override
  public boolean equals(@Nullable Object o) {
    if (o == this) {
      return true;
    } else if (!(o instanceof Interface)) {
      return false;
    }
    Interface other = (Interface) o;
    if (!Objects.equals(_accessVlan, other._accessVlan)) {
      return false;
    }
    if (_active != other._active) {
      return false;
    }
    if (!Objects.equals(_address, other._address)) {
      return false;
    }
    if (!Objects.equals(_allowedVlans, other._allowedVlans)) {
      return false;
    }
    if (!Objects.equals(_allAddresses, other._allAddresses)) {
      return false;
    }
    if (_autoState != other._autoState) {
      return false;
    }
    if (!Objects.equals(_bandwidth, other._bandwidth)) {
      return false;
    }
    if (!Objects.equals(_cryptoMap, other._cryptoMap)) {
      return false;
    }
    // we check ACLs for name match only -- full ACL diff can be done
    // elsewhere.
    if (!Objects.equals(_inboundFilterName, other._inboundFilterName)) {
      return false;
    }
    if (!Objects.equals(_incomingFilterName, other._incomingFilterName)) {
      return false;
    }
    if (_interfaceType != other._interfaceType) {
      return false;
    }
    if (!Objects.equals(_key, other._key)) {
      return false;
    }

    if (!Objects.equals(_eigrp, other._eigrp)) {
      return false;
    }

    if (!Objects.equals(_encapsulationVlan, other._encapsulationVlan)) {
      return false;
    }

    // TODO: check ISIS settings for equality.
    if (_mtu != other._mtu) {
      return false;
    }
    if (!Objects.equals(_nativeVlan, other._nativeVlan)) {
      return false;
    }
    if (!Objects.equals(_ospfSettings, other._ospfSettings)) {
      return false;
    }
    // TODO: check OSPF settings for equality.
    if (!Objects.equals(_outgoingFilterName, other._outgoingFilterName)) {
      return false;
    }
    if (!Objects.equals(_outgoingOriginalFlowFilterName, other._outgoingOriginalFlowFilterName)) {
      return false;
    }
    if (!_proxyArp == other._proxyArp) {
      return false;
    }
    if (!Objects.equals(_packetPolicyName, other._packetPolicyName)) {
      return false;
    }
    if (!Objects.equals(_speed, other._speed)) {
      return false;
    }
    if (!Objects.equals(_switchportMode, other._switchportMode)) {
      return false;
    }
    if (!Objects.equals(_zoneName, other._zoneName)) {
      return false;
    }
    if (!Objects.equals(
        _postTransformationIncomingFilterName, other._postTransformationIncomingFilterName)) {
      return false;
    }
    if (!Objects.equals(
        _preTransformationOutgoingFilterName, other._preTransformationOutgoingFilterName)) {
      return false;
    }
    return _hmm == other._hmm;
  }

  @Override
  public @Nonnull String toString() {
    return NodeInterfacePair.of(this).toString();
  }

  /** Number of access VLAN when switchport mode is ACCESS. */
  @JsonProperty(PROP_ACCESS_VLAN)
  public @Nullable Integer getAccessVlan() {
    return _accessVlan;
  }

  /** Whether this interface is administratively active (true) or disabled (false). */
  @JsonProperty(PROP_ACTIVE)
  public boolean getActive() {
    return _active;
  }

  @JsonProperty(PROP_ADDITIONAL_ARP_IPS)
  public IpSpace getAdditionalArpIps() {
    return _additionalArpIps;
  }

  @JsonProperty(PROP_ADDRESS_METADATA)
  public @Nonnull SortedMap<ConcreteInterfaceAddress, ConnectedRouteMetadata> getAddressMetadata() {
    return _addressMetadata;
  }

  @JsonProperty(PROP_ADMIN_UP)
  public boolean getAdminUp() {
    return _adminUp;
  }

  /** Ranges of allowed VLANs when switchport mode is TRUNK. */
  @JsonProperty(PROP_ALLOWED_VLANS)
  public @Nonnull IntegerSpace getAllowedVlans() {
    return _allowedVlans;
  }

  /**
   * All IPV4 address/network assignments on this interface. These are addresses that are routable
   * to/through this interface.
   */
  @JsonProperty(PROP_ALL_PREFIXES)
  public Set<ConcreteInterfaceAddress> getAllConcreteAddresses() {
    if (_allConcreteAddresses == null) {
      _allConcreteAddresses =
          _allAddresses.stream()
              .filter(a -> a instanceof ConcreteInterfaceAddress)
              .map(a -> (ConcreteInterfaceAddress) a)
              .collect(ImmutableSet.toImmutableSet());
    }
    return _allConcreteAddresses;
  }

  /**
   * Return all link-local addresses of this interface. These addresses are valid only in a given
   * layer 2 broadcast domain, and cannot be routed to across a broadcast domain
   */
  @JsonIgnore
  public Set<LinkLocalAddress> getAllLinkLocalAddresses() {
    if (_allLinkLocalAddresses == null) {
      _allLinkLocalAddresses =
          _allAddresses.stream()
              .filter(a -> a instanceof LinkLocalAddress)
              .map(a -> (LinkLocalAddress) a)
              .collect(ImmutableSet.toImmutableSet());
    }
    return _allLinkLocalAddresses;
  }

  public @Nonnull Set<InterfaceAddress> getAllAddresses() {
    return _allAddresses;
  }

  /**
   * Whether this VLAN interface's operational status is dependent on corresponding member
   * switchports.
   */
  @JsonProperty(PROP_AUTOSTATE)
  public boolean getAutoState() {
    return _autoState;
  }

  /** The nominal bandwidth of this interface in bits/sec for use in protocol cost calculations. */
  @JsonProperty(PROP_BANDWIDTH)
  public @Nullable Double getBandwidth() {
    return _bandwidth;
  }

  @JsonProperty(PROP_BLACKLISTED)
  public @Nullable Boolean getBlacklisted() {
    return _blacklisted;
  }

  @JsonProperty(PROP_LINE_UP)
  public @Nullable Boolean getLineUp() {
    return _lineUp;
  }

  @JsonProperty(PROP_CHANNEL_GROUP)
  public String getChannelGroup() {
    return _channelGroup;
  }

  @JsonProperty(PROP_CHANNEL_GROUP_MEMBERS)
  public SortedSet<String> getChannelGroupMembers() {
    return _channelGroupMembers;
  }

  @JsonProperty(PROP_CRYPTO_MAP)
  public String getCryptoMap() {
    return _cryptoMap;
  }

  @JsonProperty(PROP_DECLARED_NAMES)
  public SortedSet<String> getDeclaredNames() {
    return _declaredNames;
  }

  /** Return the set of interfaces this interface depends on (see {@link Dependency}). */
  @JsonIgnore
  public @Nonnull Set<Dependency> getDependencies() {
    return _dependencies;
  }

  /** Description of this interface. */
  @JsonProperty(PROP_DESCRIPTION)
  public String getDescription() {
    return _description;
  }

  @JsonProperty(PROP_DHCP_RELAY_ADDRESSES)
  public List<Ip> getDhcpRelayAddresses() {
    return _dhcpRelayAddresses;
  }

  @JsonProperty(PROP_EIGRP)
  public @Nullable EigrpInterfaceSettings getEigrp() {
    return _eigrp;
  }

  /** VLAN for a L3 subinterface. */
  @JsonProperty(PROP_ENCAPSULATION_VLAN)
  public @Nullable Integer getEncapsulationVlan() {
    return _encapsulationVlan;
  }

  @JsonProperty(PROP_FIREWALL_SESSION_INTERFACE_INFO)
  public @Nullable FirewallSessionInterfaceInfo getFirewallSessionInterfaceInfo() {
    return _firewallSessionInterfaceInfo;
  }

  /** Whether Host Mobility Manager (HMM) route generation for neighbor IPs is enabled. */
  @JsonProperty(PROP_HMM)
  public boolean getHmm() {
    return _hmm;
  }

  /** Mapping: hsrpGroupID -&gt; HsrpGroup */
  @JsonProperty(PROP_HSRP_GROUPS)
  public Map<Integer, HsrpGroup> getHsrpGroups() {
    return _hsrpGroups;
  }

  /** Version of the HSRP protocol to use */
  @JsonProperty(PROP_HSRP_VERSION)
  public @Nullable String getHsrpVersion() {
    return _hsrpVersion;
  }

  @JsonProperty(PROP_HUMAN_NAME)
  public @Nullable String getHumanName() {
    return _humanName;
  }

  @JsonProperty(PROP_INACTIVE_REASON)
  public @Nullable InactiveReason getInactiveReason() {
    return _inactiveReason;
  }

  @JsonIgnore
  public @Nullable IpAccessList getInboundFilter() {
    return getIpAccessList(_inboundFilterName);
  }

  /** The IPV4 access-list used to filter traffic destined for this device on this interface. */
  @JsonProperty(PROP_INBOUND_FILTER)
  private String getInboundFilterName() {
    return _inboundFilterName;
  }

  @JsonIgnore
  public IpAccessList getIncomingFilter() {
    return getIpAccessList(_incomingFilterName);
  }

  /** The IPV4 access-list used to filter traffic that arrives on this interface. */
  @JsonProperty(PROP_INCOMING_FILTER)
  private String getIncomingFilterName() {
    return _incomingFilterName;
  }

  @JsonProperty(PROP_INCOMING_TRANSFORMATION)
  public Transformation getIncomingTransformation() {
    return _incomingTransformation;
  }

  /** The type of this interface. */
  @JsonProperty(PROP_INTERFACE_TYPE)
  public InterfaceType getInterfaceType() {
    return _interfaceType;
  }

  @JsonProperty(PROP_ISIS)
  public @Nullable IsisInterfaceSettings getIsis() {
    return _isis;
  }

  @JsonProperty(PROP_MLAG_ID)
  public @Nullable Integer getMlagId() {
    return _mlagId;
  }

  /** The maximum transmission unit (MTU) of this interface in bytes. */
  @JsonProperty(PROP_MTU)
  public int getMtu() {
    return _mtu;
  }

  /** The native VLAN of this interface when switchport mode is TRUNK. */
  @JsonProperty(PROP_NATIVE_VLAN)
  public @Nullable Integer getNativeVlan() {
    return _nativeVlan;
  }

  /** {@link OspfInterfaceSettings} associated with this interface. */
  @JsonProperty(PROP_OSPF_SETTINGS)
  public @Nullable OspfInterfaceSettings getOspfSettings() {
    return _ospfSettings;
  }

  /** The OSPF area to which this interface belongs. */
  @JsonIgnore
  public @Nullable Long getOspfAreaName() {
    return (_ospfSettings != null) ? _ospfSettings.getAreaName() : null;
  }

  /** The explicit OSPF cost of this interface. If unset, the cost is automatically calculated. */
  @JsonIgnore
  public @Nullable Integer getOspfCost() {
    return (_ospfSettings != null) ? _ospfSettings.getCost() : null;
  }

  /** Whether or not OSPF is enabled at all on this interface (either actively or passively). */
  @JsonIgnore
  public boolean getOspfEnabled() {
    return (_ospfSettings != null) ? firstNonNull(_ospfSettings.getEnabled(), false) : false;
  }

  /**
   * "Returns name of the routing policy which is generated from the Global and Interface level
   * inbound distribute-lists for OSPF"
   */
  @JsonIgnore
  public @Nullable String getOspfInboundDistributeListPolicy() {
    return (_ospfSettings != null) ? _ospfSettings.getInboundDistributeListPolicy() : null;
  }

  /** Returns the OSPF network type for this interface. */
  @JsonIgnore
  public @Nullable OspfNetworkType getOspfNetworkType() {
    return (_ospfSettings != null) ? _ospfSettings.getNetworkType() : null;
  }

  /**
   * Whether or not OSPF is enabled passively on this interface. If passive, this interface is
   * included in the OSPF RIB, but no OSPF packets are sent from it.
   */
  @JsonIgnore
  public boolean getOspfPassive() {
    return (_ospfSettings != null) ? firstNonNull(_ospfSettings.getPassive(), false) : false;
  }

  @JsonIgnore
  public @Nullable String getOspfProcess() {
    return (_ospfSettings != null) ? _ospfSettings.getProcess() : null;
  }

  @JsonIgnore
  public @Nullable IpAccessList getOutgoingFilter() {
    return getIpAccessList(_outgoingFilterName);
  }

  @JsonIgnore
  public @Nullable IpAccessList getOutgoingOriginalFlowFilter() {
    return getIpAccessList(_outgoingOriginalFlowFilterName);
  }

  /** The IPV4 access-list used to filter traffic that is sent out this interface. Stored as @id. */
  @JsonProperty(PROP_OUTGOING_FILTER)
  private @Nullable String getOutgoingFilterName() {
    return _outgoingFilterName;
  }

  /**
   * The IPV4 access-list used to filter traffic sent out this interface matching on the original
   * flow that entered the node (before any transformations).
   */
  @JsonProperty(PROP_OUTGOING_ORIGINAL_FLOW_FILTER)
  private @Nullable String getOutgoingOriginalFlowFilterName() {
    return _outgoingOriginalFlowFilterName;
  }

  @JsonProperty(PROP_OUTGOING_TRANSFORMATION)
  public Transformation getOutgoingTransformation() {
    return _outgoingTransformation;
  }

  @JsonIgnore
  public Configuration getOwner() {
    return _owner;
  }

  /** The primary IPV4 address/network of this interface. */
  @JsonProperty(PROP_PREFIX)
  public @Nullable InterfaceAddress getAddress() {
    return _address;
  }

  @JsonIgnore
  public @Nullable ConcreteInterfaceAddress getConcreteAddress() {
    return _address instanceof ConcreteInterfaceAddress
        ? (ConcreteInterfaceAddress) _address
        : null;
  }

  @JsonIgnore
  public @Nullable LinkLocalAddress getLinkLocalAddress() {
    return _address instanceof LinkLocalAddress ? (LinkLocalAddress) _address : null;
  }

  @JsonIgnore
  public @Nullable IpAccessList getPostTransformationIncomingFilter() {
    return getIpAccessList(_postTransformationIncomingFilterName);
  }

  /** The IPV4 access-list used to filter incoming traffic after applying destination NAT. */
  @JsonProperty(PROP_POST_TRANSFORMATION_INCOMING_FILTER)
  private @Nullable String getPostTransformationIncomingFilterName() {
    return _postTransformationIncomingFilterName;
  }

  @JsonIgnore
  public @Nullable IpAccessList getPreTransformationOutgoingFilter() {
    return getIpAccessList(_preTransformationOutgoingFilterName);
  }

  /** The IPV4 access-list used to filter outgoing traffic before applying source NAT. */
  @JsonProperty(PROP_PRE_TRANSFORMATION_OUTGOING_FILTER)
  private @Nullable String getPreTransformationOutgoingFilterName() {
    return _preTransformationOutgoingFilterName;
  }

  @JsonIgnore
  public @Nullable Prefix getPrimaryNetwork() {
    if (_address instanceof ConcreteInterfaceAddress) {
      return ((ConcreteInterfaceAddress) _address).getPrefix();
    }
    return null;
  }

  /** Whether or not proxy-ARP is enabled on this interface. */
  @JsonProperty(PROP_PROXY_ARP)
  public boolean getProxyArp() {
    return _proxyArp;
  }

  @JsonProperty(PROP_RIP_ENABLED)
  public boolean getRipEnabled() {
    return _ripEnabled;
  }

  @JsonProperty(PROP_RIP_PASSIVE)
  public boolean getRipPassive() {
    return _ripPassive;
  }

  /**
   * The name of the packet policy used on this interface for policy routing (as opposed to
   * destination-based routing).
   */
  @JsonProperty(PROP_ROUTING_POLICY)
  public String getPacketPolicyName() {
    return _packetPolicyName;
  }

  /** Whether or not spanning-tree portfast feature is enabled. */
  @JsonProperty(PROP_SPANNING_TREE_PORTFAST)
  public boolean getSpanningTreePortfast() {
    return _spanningTreePortfast;
  }

  /** The link speed of this interface, in bits per second (bps) */
  public @Nullable Double getSpeed() {
    return _speed;
  }

  /** Whether this interface is configured as a switchport. */
  @JsonProperty(PROP_SWITCHPORT)
  public boolean getSwitchport() {
    return _switchport;
  }

  /** The switchport mode (if any) of this interface. */
  @JsonProperty(PROP_SWITCHPORT_MODE)
  public SwitchportMode getSwitchportMode() {
    return _switchportMode;
  }

  /**
   * The switchport trunk encapsulation type of this interface. Only relevant when switchport mode
   * is TRUNK.
   */
  @JsonProperty(PROP_SWITCHPORT_TRUNK_ENCAPSULATION)
  public SwitchportEncapsulationType getSwitchportTrunkEncapsulation() {
    return _switchportTrunkEncapsulation;
  }

  /** Return non-IPSec tunnel settings associated with this interface */
  public @Nullable TunnelConfiguration getTunnelConfig() {
    return _tunnelConfig;
  }

  /** VLAN for an IRB interface. */
  @JsonProperty(PROP_VLAN)
  public @Nullable Integer getVlan() {
    return _vlan;
  }

  @JsonIgnore
  public Vrf getVrf() {
    return _vrf;
  }

  /** The name of the VRF to which this interface belongs. */
  @JsonProperty(PROP_VRF)
  public String getVrfName() {
    if (_vrf != null) {
      return _vrf.getName();
    } else {
      return _vrfName;
    }
  }

  /** VRID -> VRID configuration */
  @JsonProperty(PROP_VRRP_GROUPS)
  public SortedMap<Integer, VrrpGroup> getVrrpGroups() {
    return _vrrpGroups;
  }

  /** The firewall zone to which this interface belongs. */
  @JsonProperty(PROP_ZONE)
  public String getZoneName() {
    return _zoneName;
  }

  @JsonIgnore
  public boolean isLoopback() {
    return _interfaceType == InterfaceType.LOOPBACK;
  }

  @JsonProperty(PROP_ACCESS_VLAN)
  public void setAccessVlan(@Nullable Integer vlan) {
    _accessVlan = vlan;
  }

  @JsonProperty(PROP_ACTIVE)
  private void setActive(boolean active) {
    _active = active;
  }

  @JsonProperty(PROP_ADDITIONAL_ARP_IPS)
  public void setAdditionalArpIps(IpSpace additionalArpIps) {
    _additionalArpIps = firstNonNull(additionalArpIps, EmptyIpSpace.INSTANCE);
  }

  @JsonProperty(PROP_ADDRESS_METADATA)
  public void setAddressMetadata(
      @Nullable SortedMap<ConcreteInterfaceAddress, ConnectedRouteMetadata> addressMetadata) {
    _addressMetadata =
        ImmutableSortedMap.copyOf(firstNonNull(addressMetadata, ImmutableSortedMap.of()));
  }

  @JsonProperty(PROP_ADMIN_UP)
  private void setAdminUp(boolean adminUp) {
    _adminUp = adminUp;
  }

  @JsonProperty(PROP_ALLOWED_VLANS)
  public void setAllowedVlans(@Nullable IntegerSpace allowedVlans) {
    _allowedVlans = firstNonNull(allowedVlans, IntegerSpace.EMPTY);
  }

  @JsonProperty(PROP_ALL_PREFIXES)
  public void setAllAddresses(Iterable<? extends InterfaceAddress> allAddresses) {
    _allAddresses = ImmutableSortedSet.copyOf(allAddresses);
    // Clear cached values
    _allLinkLocalAddresses = null;
    _allConcreteAddresses = null;
  }

  @JsonProperty(PROP_AUTOSTATE)
  public void setAutoState(boolean autoState) {
    _autoState = autoState;
  }

  @JsonProperty(PROP_BANDWIDTH)
  public void setBandwidth(@Nullable Double bandwidth) {
    _bandwidth = bandwidth;
  }

  @JsonProperty(PROP_BLACKLISTED)
  private void setBlacklisted(@Nullable Boolean blacklisted) {
    _blacklisted = blacklisted;
  }

  @JsonProperty(PROP_CHANNEL_GROUP)
  public void setChannelGroup(String channelGroup) {
    _channelGroup = channelGroup;
  }

  @JsonProperty(PROP_CHANNEL_GROUP_MEMBERS)
  public void setChannelGroupMembers(Iterable<String> channelGroupMembers) {
    _channelGroupMembers = ImmutableSortedSet.copyOf(channelGroupMembers);
  }

  @JsonProperty(PROP_CRYPTO_MAP)
  public void setCryptoMap(String cryptoMap) {
    _cryptoMap = cryptoMap;
  }

  @JsonProperty(PROP_DECLARED_NAMES)
  public void setDeclaredNames(SortedSet<String> declaredNames) {
    _declaredNames = ImmutableSortedSet.copyOf(declaredNames);
  }

  /** Set (overwrite) all dependencies for this interface */
  @JsonIgnore
  public void setDependencies(@Nonnull Collection<Dependency> dependencies) {
    _dependencies = ImmutableSet.copyOf(dependencies);
  }

  /** Add an interface dependency to this interface */
  public void addDependency(@Nonnull Dependency dependency) {
    _dependencies =
        ImmutableSet.<Dependency>builder().addAll(_dependencies).add(dependency).build();
  }

  @JsonProperty(PROP_DESCRIPTION)
  public void setDescription(String description) {
    _description = description;
  }

  @JsonProperty(PROP_DHCP_RELAY_ADDRESSES)
  public void setDhcpRelayAddresses(List<Ip> dhcpRelayAddresses) {
    _dhcpRelayAddresses = ImmutableList.copyOf(dhcpRelayAddresses);
  }

  @JsonProperty(PROP_EIGRP)
  public void setEigrp(@Nullable EigrpInterfaceSettings eigrp) {
    _eigrp = eigrp;
  }

  @JsonProperty(PROP_ENCAPSULATION_VLAN)
  public void setEncapsulationVlan(@Nullable Integer encapsulationVlan) {
    _encapsulationVlan = encapsulationVlan;
  }

  @JsonProperty(PROP_FIREWALL_SESSION_INTERFACE_INFO)
  public void setFirewallSessionInterfaceInfo(
      @Nullable FirewallSessionInterfaceInfo firewallSessionInterfaceInfo) {
    _firewallSessionInterfaceInfo = firewallSessionInterfaceInfo;
  }

  @JsonProperty(PROP_HMM)
  public void setHmm(boolean hmm) {
    _hmm = hmm;
  }

  @JsonProperty(PROP_HSRP_GROUPS)
  public void setHsrpGroups(@Nonnull Map<Integer, HsrpGroup> hsrpGroups) {
    _hsrpGroups = hsrpGroups;
  }

  @JsonProperty(PROP_HSRP_VERSION)
  public void setHsrpVersion(String hsrpVersion) {
    _hsrpVersion = hsrpVersion;
  }

  @JsonProperty(PROP_HUMAN_NAME)
  public void setHumanName(@Nullable String humanName) {
    _humanName = humanName;
  }

  @JsonProperty(PROP_INACTIVE_REASON)
  private void setInactiveReason(@Nullable InactiveReason inactiveReason) {
    _inactiveReason = inactiveReason;
  }

  @JsonIgnore
  public void setInboundFilter(@Nullable IpAccessList inboundFilter) {
    _inboundFilterName = inboundFilter == null ? null : inboundFilter.getName();
  }

  @JsonProperty(PROP_INBOUND_FILTER)
  private void setInboundFilterName(@Nullable String inboundFilterName) {
    _inboundFilterName = inboundFilterName;
  }

  @JsonIgnore
  public void setIncomingFilter(@Nullable IpAccessList incomingFilter) {
    _incomingFilterName = incomingFilter == null ? null : incomingFilter.getName();
  }

  @JsonProperty(PROP_INCOMING_TRANSFORMATION)
  public void setIncomingTransformation(Transformation incomingTransformation) {
    _incomingTransformation = incomingTransformation;
  }

  @JsonProperty(PROP_INCOMING_FILTER)
  public void setIncomingFilterName(@Nullable String incomingFilterName) {
    _incomingFilterName = incomingFilterName;
  }

  @JsonProperty(PROP_INTERFACE_TYPE)
  private void setInterfaceType(InterfaceType it) {
    _interfaceType = it;
  }

  @JsonProperty(PROP_ISIS)
  public void setIsis(@Nullable IsisInterfaceSettings isis) {
    _isis = isis;
  }

  @JsonProperty(PROP_MLAG_ID)
  public void setMlagId(Integer mlagId) {
    _mlagId = mlagId;
  }

  @JsonProperty(PROP_LINE_UP)
  private void setLineUp(@Nullable Boolean lineUp) {
    _lineUp = lineUp;
  }

  @JsonProperty(PROP_MTU)
  public void setMtu(int mtu) {
    _mtu = mtu;
  }

  @JsonProperty(PROP_NATIVE_VLAN)
  public void setNativeVlan(@Nullable Integer vlan) {
    _nativeVlan = vlan;
  }

  @JsonProperty(PROP_OSPF_SETTINGS)
  public void setOspfSettings(@Nullable OspfInterfaceSettings ospfSettings) {
    _ospfSettings = ospfSettings;
  }

  @JsonIgnore
  public void setOutgoingFilter(@Nullable IpAccessList outgoingFilter) {
    _outgoingFilterName = outgoingFilter == null ? null : outgoingFilter.getName();
  }

  @JsonProperty(PROP_OUTGOING_FILTER)
  public void setOutgoingFilterName(@Nullable String outgoingFilterName) {
    _outgoingFilterName = outgoingFilterName;
  }

  @JsonIgnore
  public void setOutgoingOriginalFlowFilter(@Nullable IpAccessList outgoingOriginalFlowFilter) {
    _outgoingOriginalFlowFilterName =
        outgoingOriginalFlowFilter == null ? null : outgoingOriginalFlowFilter.getName();
  }

  @JsonProperty(PROP_OUTGOING_ORIGINAL_FLOW_FILTER)
  private void setOutgoingOriginalFlowFilter(@Nullable String outgoingOriginalFlowFilterName) {
    _outgoingOriginalFlowFilterName = outgoingOriginalFlowFilterName;
  }

  @JsonProperty(PROP_OUTGOING_TRANSFORMATION)
  public void setOutgoingTransformation(Transformation outgoingTransformation) {
    _outgoingTransformation = outgoingTransformation;
  }

  @JsonIgnore
  public void setOwner(Configuration owner) {
    _owner = owner;
  }

  @JsonProperty(PROP_PREFIX)
  public void setAddress(InterfaceAddress address) {
    _address = address;
    // Clear cached values
    _allLinkLocalAddresses = null;
    _allConcreteAddresses = null;
  }

  @JsonIgnore
  public void setPostTransformationIncomingFilter(
      @Nullable IpAccessList postTransformationIncomingFilter) {
    _postTransformationIncomingFilterName =
        postTransformationIncomingFilter == null
            ? null
            : postTransformationIncomingFilter.getName();
  }

  @JsonProperty(PROP_POST_TRANSFORMATION_INCOMING_FILTER)
  private void setPostTransformationIncomingFilter(String postTransformationIncomingFilterName) {
    _postTransformationIncomingFilterName = postTransformationIncomingFilterName;
  }

  @JsonIgnore
  public void setPreTransformationOutgoingFilter(
      @Nullable IpAccessList preTransformationOutgoingFilter) {
    _preTransformationOutgoingFilterName =
        preTransformationOutgoingFilter == null ? null : preTransformationOutgoingFilter.getName();
  }

  @JsonProperty(PROP_PRE_TRANSFORMATION_OUTGOING_FILTER)
  private void setPreTransformationOutgoingFilter(String preTransformationOutgoingFilterName) {
    _preTransformationOutgoingFilterName = preTransformationOutgoingFilterName;
  }

  @JsonProperty(PROP_PROXY_ARP)
  public void setProxyArp(boolean proxyArp) {
    _proxyArp = proxyArp;
  }

  @JsonProperty(PROP_RIP_ENABLED)
  public void setRipEnabled(boolean ripEnabled) {
    _ripEnabled = ripEnabled;
  }

  @JsonProperty(PROP_RIP_PASSIVE)
  public void setRipPassive(boolean ripPassive) {
    _ripPassive = ripPassive;
  }

  @JsonProperty(PROP_ROUTING_POLICY)
  public void setPacketPolicy(String packetPolicyName) {
    _packetPolicyName = packetPolicyName;
  }

  @JsonProperty(PROP_SPANNING_TREE_PORTFAST)
  public void setSpanningTreePortfast(boolean spanningTreePortfast) {
    _spanningTreePortfast = spanningTreePortfast;
  }

  @JsonProperty(PROP_SPEED)
  public void setSpeed(@Nullable Double speed) {
    _speed = speed;
  }

  @JsonProperty(PROP_SWITCHPORT)
  public void setSwitchport(boolean switchport) {
    _switchport = switchport;
  }

  @JsonProperty(PROP_SWITCHPORT_MODE)
  public void setSwitchportMode(SwitchportMode switchportMode) {
    _switchportMode = switchportMode;
  }

  @JsonProperty(PROP_SWITCHPORT_TRUNK_ENCAPSULATION)
  public void setSwitchportTrunkEncapsulation(SwitchportEncapsulationType encapsulation) {
    _switchportTrunkEncapsulation = encapsulation;
  }

  @JsonProperty(PROP_TUNNEL_CONFIG)
  public void setTunnelConfig(@Nullable TunnelConfiguration tunnelConfig) {
    _tunnelConfig = tunnelConfig;
  }

  @JsonProperty(PROP_VLAN)
  public void setVlan(@Nullable Integer vlan) {
    _vlan = vlan;
  }

  @JsonIgnore
  public void setVrf(Vrf vrf) {
    _vrf = vrf;
    if (vrf != null) {
      _vrfName = vrf.getName();
    }
  }

  @JsonProperty(PROP_VRF)
  public void setVrfName(String vrfName) {
    _vrfName = vrfName;
  }

  @JsonProperty(PROP_VRRP_GROUPS)
  public void setVrrpGroups(SortedMap<Integer, VrrpGroup> vrrpGroups) {
    _vrrpGroups = vrrpGroups;
  }

  @JsonProperty(PROP_ZONE)
  public void setZoneName(String zoneName) {
    _zoneName = zoneName;
  }

  public void addVrrpGroup(Integer num, @Nonnull VrrpGroup group) {
    _vrrpGroups =
        ImmutableSortedMap.<Integer, VrrpGroup>naturalOrder()
            .putAll(_vrrpGroups)
            .put(num, group)
            .build();
  }

  /** Return {code} iff this interface is of a type that has line status. */
  public boolean hasLineStatus() {
    return hasLineStatus(_interfaceType);
  }

  /** Return {code} iff this {@code type} of interface has line status. */
  public static boolean hasLineStatus(InterfaceType type) {
    switch (type) {
      case PHYSICAL:
      case UNKNOWN:
        return true;
      default:
        return false;
    }
  }

  /**
   * Check if the given interface name is <b>not</b> one of the special values defined by batfish or
   * virtual null interface.
   */
  public static boolean isRealInterfaceName(@Nonnull String name) {
    return !ImmutableList.of(
            UNSET_LOCAL_INTERFACE,
            DYNAMIC_INTERFACE_NAME,
            NULL_INTERFACE_NAME,
            INVALID_LOCAL_INTERFACE)
        .contains(name);
  }

  /**
   * Administratively disable this active interface, after which:
   *
   * <ul>
   *   <li>{@link #getActive()} will return {@code false}.
   *   <li>{@link #getAdminUp()} will return {@code false}.
   *   <li>{@link #getInactiveReason()} will return InactiveReason#ADMIN_DOWN}.
   * </ul>
   *
   * <p>Should only be called during conversion.
   *
   * @throws IllegalStateException if this interface is already administratively disabled or
   *     inactive.
   */
  public void adminDown() {
    checkState(_adminUp, "Cannot administratively disable an interface that is already admin down");
    checkState(_active, "Cannot admin down an inactive interface");
    _adminUp = false;
    _active = false;
    _inactiveReason = ADMIN_DOWN;
  }

  /**
   * Blacklist an interface because input data suggests it is down for maintenance, after which:
   *
   * <ul>
   *   <li>{@link #getActive()} will return {@code false}.
   *   <li>{@link #getLineUp()} will return {@code false}.
   *   <li>{@link #getBlacklisted()} will return {@code true}.
   *   <li>{@link #getInactiveReason()} will return {@link InactiveReason#BLACKLISTED} if this
   *       interface was not already inactive.
   * </ul>
   *
   * <p>Should only be called after conversion.
   *
   * @throws IllegalStateException if this interface is already blacklisted or is not of a type that
   *     has line status.
   */
  public void blacklist() {
    checkState(
        hasLineStatus(),
        "Cannot blacklist an interface of type '%s' that has no line status",
        _interfaceType);
    checkState(!_blacklisted, "Cannot blacklist an interface that is already blacklisted");
    if (_active) {
      _inactiveReason = BLACKLISTED;
    }
    _active = false;
    _blacklisted = true;
    _lineUp = false;
  }

  /**
   * Disconnect this interface, after which:
   *
   * <ul>
   *   <li>{@link #getActive()} will return {@code false}.
   *   <li>{@link #getLineUp()} will return {@code false}.
   *   <li>{@link #getInactiveReason()} will return {@code inactiveReason} if this interface was not
   *       already inactive.
   * </ul>
   *
   * @throws IllegalStateException if this interface is already disconnected, or not of a type that
   *     has line status.
   */
  @VisibleForTesting
  void disconnect(InactiveReason inactiveReason) {
    checkState(
        hasLineStatus(),
        "Cannot disconnect an interface of type '%s' that has no line status",
        _interfaceType);
    checkState(_lineUp, "Cannot disconnect a disconnected interface.");
    _lineUp = false;
    if (_active) {
      _active = false;
      _inactiveReason = inactiveReason;
    }
  }

  /**
   * Disconnect this physical interface because its physical neighbor is down, after which:
   *
   * <ul>
   *   <li>{@link #getActive()} will return {@code false}.
   *   <li>{@link #getLineUp()} will return {@code false}.
   *   <li>{@link #getInactiveReason()} will return {@link InactiveReason#PHYSICAL_NEIGHBOR_DOWN}.
   * </ul>
   *
   * <p>Should only be called after conversion.
   *
   * @throws IllegalStateException if this interface is already disconnected, or not of a type that
   *     has line status.
   */
  public void physicalNeighborDown() {
    disconnect(PHYSICAL_NEIGHBOR_DOWN);
  }

  /**
   * Disable this interface because the node that owns it is down, after which:
   *
   * <ul>
   *   <li>{@link #getActive()} will return {@code false}.
   *   <li>{@link #getLineUp()} will return {@code false} if this is a physical interface.
   *   <li>{@link #getInactiveReason()} will return {@link InactiveReason#NODE_DOWN} if this
   *       interface is not already inactive.
   * </ul>
   *
   * <p>Should only be called after conversion.
   */
  public void nodeDown() {
    if (_active) {
      _inactiveReason = NODE_DOWN;
    }
    _active = false;
    if (hasLineStatus()) {
      _lineUp = false;
    }
  }

  /**
   * Deactivate this active interface, after which:
   *
   * <ul>
   *   <li>{@link #getActive()} will return {@code false}.
   *   <li>{@link #getInactiveReason()} will return {@code inactiveReason} in default cases not
   *       enumerated below. For enumerated cases, see respective javdocs.
   * </ul>
   *
   * <p>Special cases:
   *
   * <ul>
   *   <li>Calling with {@link InactiveReason#ADMIN_DOWN} is equivalent to calling {@link
   *       #adminDown()}.
   *   <li>Calling with {@link InactiveReason#BLACKLISTED} is equivalent to calling {@link
   *       #blacklist()}.
   *   <li>Calling with {@link InactiveReason#FORCED_LINE_DOWN} is equivalent to calling {@link
   *       #disconnect(InactiveReason)} with argument {@link InactiveReason#FORCED_LINE_DOWN} (to be
   *       used only internally or in tests).
   *   <li>Calling with {@link InactiveReason#NODE_DOWN} is equivalent to calling {@link
   *       #nodeDown()}.
   *   <li>Calling with {@link InactiveReason#PHYSICAL_NEIGHBOR_DOWN} is equivalent to calling
   *       {@link #physicalNeighborDown()}.
   * </ul>
   *
   * <p>Should only be called after conversion.
   *
   * @throws IllegalStateException in default cases not enumerated above if interface is already
   *     inactive. For cases enumerated above, see respective javadocs.
   */
  public void deactivate(InactiveReason inactiveReason) {
    switch (inactiveReason) {
      case ADMIN_DOWN:
        adminDown();
        break;
      case BLACKLISTED:
        blacklist();
        break;
      case FORCED_LINE_DOWN:
        disconnect(FORCED_LINE_DOWN);
        break;
      case PHYSICAL_NEIGHBOR_DOWN:
        physicalNeighborDown();
        break;
      case NODE_DOWN:
        nodeDown();
        break;
      default:
        checkState(_active, "Cannot deactivate an inactive interface");
        _active = false;
        _inactiveReason = inactiveReason;
        break;
    }
  }

  /**
   * Activate this inactive interface, after which:
   *
   * <ul>
   *   <li>{@link #getActive()} will return {@code true}.
   *   <li>{@link #getAdminUp()} will return {@code true}.
   *   <li>{@link #getBlacklisted()} will return {@code false} if this is a physical interface, else
   *       {@code null}.
   *   <li>{@link #getLineUp()} will return {@code true} if this is a physical interface, else
   *       {@code null}.
   *   <li>{@link #getInactiveReason()} will return {@code null}.
   * </ul>
   *
   * <p>Should only be called from test code.
   *
   * @throws IllegalStateException if this interface is already active.
   */
  @VisibleForTesting
  public void activateForTest() {
    checkState(!_active, "Cannot activate an active interface");
    _active = true;
    _adminUp = true;
    _inactiveReason = null;
    if (hasLineStatus()) {
      _lineUp = true;
      _blacklisted = false;
    }
  }

  /** Helper to get an IpAccessList object given its name. */
  private @Nullable IpAccessList getIpAccessList(@Nullable String name) {
    return _owner == null || name == null
        ? null
        : checkNotNull(_owner.getIpAccessLists().get(name));
  }
}
