package org.batfish.representation.aws;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Ip;

/**
 * Represents an AWS Direct Connect Virtual Interface (Transit VIF).
 * https://docs.aws.amazon.com/directconnect/latest/APIReference/API_DescribeVirtualInterfaces.html
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@ParametersAreNonnullByDefault
final class DirectConnectVirtualInterface implements AwsVpcEntity, Serializable {

  static final String JSON_KEY_VIRTUAL_INTERFACES = "VirtualInterfaces";
  static final String JSON_KEY_VIRTUAL_INTERFACE_ID = "VirtualInterfaceId";
  static final String JSON_KEY_VIRTUAL_INTERFACE_NAME = "VirtualInterfaceName";
  static final String JSON_KEY_VIRTUAL_INTERFACE_TYPE = "VirtualInterfaceType";
  static final String JSON_KEY_CONNECTION_ID = "ConnectionId";
  static final String JSON_KEY_VLAN = "Vlan";
  static final String JSON_KEY_ASN = "Asn";
  static final String JSON_KEY_AMAZON_ADDRESS = "AmazonAddress";
  static final String JSON_KEY_CUSTOMER_ADDRESS = "CustomerAddress";
  static final String JSON_KEY_VIRTUAL_INTERFACE_STATE = "VirtualInterfaceState";
  static final String JSON_KEY_DIRECT_CONNECT_GATEWAY_ID = "DirectConnectGatewayId";
  static final String JSON_KEY_BGP_PEERS = "BgpPeers";

  @JsonIgnoreProperties(ignoreUnknown = true)
  @ParametersAreNonnullByDefault
  static final class BgpPeer implements Serializable {

    static final String JSON_KEY_BGP_PEER_STATE = "BgpPeerState";
    static final String JSON_KEY_BGP_STATUS = "BgpStatus";

    private final long _asn;
    private final @Nonnull ConcreteInterfaceAddress _amazonAddress;
    private final @Nonnull ConcreteInterfaceAddress _customerAddress;

    @JsonCreator
    private static BgpPeer create(
        @JsonProperty(JSON_KEY_ASN) @Nullable Long asn,
        @JsonProperty(JSON_KEY_AMAZON_ADDRESS) @Nullable String amazonAddress,
        @JsonProperty(JSON_KEY_CUSTOMER_ADDRESS) @Nullable String customerAddress) {
      checkArgument(asn != null, "ASN cannot be null for BGP peer");
      checkArgument(amazonAddress != null, "Amazon address cannot be null for BGP peer");
      checkArgument(customerAddress != null, "Customer address cannot be null for BGP peer");
      return new BgpPeer(
          asn,
          ConcreteInterfaceAddress.parse(amazonAddress),
          ConcreteInterfaceAddress.parse(customerAddress));
    }

    BgpPeer(
        long asn,
        ConcreteInterfaceAddress amazonAddress,
        ConcreteInterfaceAddress customerAddress) {
      _asn = asn;
      _amazonAddress = amazonAddress;
      _customerAddress = customerAddress;
    }

    public long getAsn() {
      return _asn;
    }

    public @Nonnull ConcreteInterfaceAddress getAmazonAddress() {
      return _amazonAddress;
    }

    public @Nonnull ConcreteInterfaceAddress getCustomerAddress() {
      return _customerAddress;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (!(o instanceof BgpPeer)) {
        return false;
      }
      BgpPeer that = (BgpPeer) o;
      return _asn == that._asn
          && Objects.equals(_amazonAddress, that._amazonAddress)
          && Objects.equals(_customerAddress, that._customerAddress);
    }

    @Override
    public int hashCode() {
      return Objects.hash(_asn, _amazonAddress, _customerAddress);
    }
  }

  private final @Nonnull String _virtualInterfaceId;

  private final @Nonnull String _virtualInterfaceName;

  private final @Nonnull String _virtualInterfaceType;

  private final @Nonnull String _connectionId;

  private final @Nullable String _directConnectGatewayId;

  private final int _vlan;

  private final long _asn;

  private final @Nonnull ConcreteInterfaceAddress _amazonAddress;

  private final @Nonnull ConcreteInterfaceAddress _customerAddress;

  private final @Nonnull List<BgpPeer> _bgpPeers;

  private final @Nonnull Map<String, String> _tags;

  @JsonCreator
  private static DirectConnectVirtualInterface create(
      @JsonProperty(JSON_KEY_VIRTUAL_INTERFACE_ID) @Nullable String virtualInterfaceId,
      @JsonProperty(JSON_KEY_VIRTUAL_INTERFACE_NAME) @Nullable String virtualInterfaceName,
      @JsonProperty(JSON_KEY_VIRTUAL_INTERFACE_TYPE) @Nullable String virtualInterfaceType,
      @JsonProperty(JSON_KEY_CONNECTION_ID) @Nullable String connectionId,
      @JsonProperty(JSON_KEY_DIRECT_CONNECT_GATEWAY_ID) @Nullable String directConnectGatewayId,
      @JsonProperty(JSON_KEY_VLAN) @Nullable Integer vlan,
      @JsonProperty(JSON_KEY_ASN) @Nullable Long asn,
      @JsonProperty(JSON_KEY_AMAZON_ADDRESS) @Nullable String amazonAddress,
      @JsonProperty(JSON_KEY_CUSTOMER_ADDRESS) @Nullable String customerAddress,
      @JsonProperty(JSON_KEY_BGP_PEERS) @Nullable List<BgpPeer> bgpPeers,
      @JsonProperty(JSON_KEY_TAGS) @Nullable List<Tag> tags) {
    checkArgument(virtualInterfaceId != null, "Virtual interface id cannot be null");
    checkArgument(virtualInterfaceName != null, "Virtual interface name cannot be null");
    checkArgument(virtualInterfaceType != null, "Virtual interface type cannot be null");
    checkArgument(connectionId != null, "Connection id cannot be null for virtual interface");
    checkArgument(vlan != null, "VLAN cannot be null for virtual interface");
    checkArgument(asn != null, "ASN cannot be null for virtual interface");
    checkArgument(amazonAddress != null, "Amazon address cannot be null for virtual interface");
    checkArgument(customerAddress != null, "Customer address cannot be null for virtual interface");

    return new DirectConnectVirtualInterface(
        virtualInterfaceId,
        virtualInterfaceName,
        virtualInterfaceType,
        connectionId,
        directConnectGatewayId,
        vlan,
        asn,
        ConcreteInterfaceAddress.parse(amazonAddress),
        ConcreteInterfaceAddress.parse(customerAddress),
        firstNonNull(bgpPeers, ImmutableList.of()),
        firstNonNull(tags, ImmutableList.<Tag>of()).stream()
            .collect(ImmutableMap.toImmutableMap(Tag::getKey, Tag::getValue)));
  }

  DirectConnectVirtualInterface(
      String virtualInterfaceId,
      String virtualInterfaceName,
      String virtualInterfaceType,
      String connectionId,
      @Nullable String directConnectGatewayId,
      int vlan,
      long asn,
      ConcreteInterfaceAddress amazonAddress,
      ConcreteInterfaceAddress customerAddress,
      List<BgpPeer> bgpPeers,
      Map<String, String> tags) {
    _virtualInterfaceId = virtualInterfaceId;
    _virtualInterfaceName = virtualInterfaceName;
    _virtualInterfaceType = virtualInterfaceType;
    _connectionId = connectionId;
    _directConnectGatewayId = directConnectGatewayId;
    _vlan = vlan;
    _asn = asn;
    _amazonAddress = amazonAddress;
    _customerAddress = customerAddress;
    _bgpPeers = bgpPeers;
    _tags = tags;
  }

  @Override
  public @Nonnull String getId() {
    return _virtualInterfaceId;
  }

  public @Nonnull String getVirtualInterfaceName() {
    return _virtualInterfaceName;
  }

  public @Nonnull String getVirtualInterfaceType() {
    return _virtualInterfaceType;
  }

  public @Nonnull String getConnectionId() {
    return _connectionId;
  }

  public @Nullable String getDirectConnectGatewayId() {
    return _directConnectGatewayId;
  }

  public int getVlan() {
    return _vlan;
  }

  /** The customer-side ASN */
  public long getAsn() {
    return _asn;
  }

  public @Nonnull ConcreteInterfaceAddress getAmazonAddress() {
    return _amazonAddress;
  }

  public @Nonnull ConcreteInterfaceAddress getCustomerAddress() {
    return _customerAddress;
  }

  public @Nonnull Ip getAmazonIp() {
    return _amazonAddress.getIp();
  }

  public @Nonnull Ip getCustomerIp() {
    return _customerAddress.getIp();
  }

  public @Nonnull List<BgpPeer> getBgpPeers() {
    return _bgpPeers;
  }

  public @Nonnull Map<String, String> getTags() {
    return _tags;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof DirectConnectVirtualInterface)) {
      return false;
    }
    DirectConnectVirtualInterface that = (DirectConnectVirtualInterface) o;
    return _vlan == that._vlan
        && _asn == that._asn
        && Objects.equals(_virtualInterfaceId, that._virtualInterfaceId)
        && Objects.equals(_virtualInterfaceName, that._virtualInterfaceName)
        && Objects.equals(_virtualInterfaceType, that._virtualInterfaceType)
        && Objects.equals(_connectionId, that._connectionId)
        && Objects.equals(_directConnectGatewayId, that._directConnectGatewayId)
        && Objects.equals(_amazonAddress, that._amazonAddress)
        && Objects.equals(_customerAddress, that._customerAddress)
        && Objects.equals(_bgpPeers, that._bgpPeers)
        && Objects.equals(_tags, that._tags);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        _virtualInterfaceId,
        _virtualInterfaceName,
        _virtualInterfaceType,
        _connectionId,
        _directConnectGatewayId,
        _vlan,
        _asn,
        _amazonAddress,
        _customerAddress,
        _bgpPeers,
        _tags);
  }
}
