package org.batfish.vendor.check_point_management;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.google.common.base.MoreObjects.ToStringHelper;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.Ip;

/**
 * A gateway or server from the {@code objects} field of the response to the {@code
 * show-gateways-and-servers} command.
 */
@JsonTypeInfo(use = Id.NAME, property = "type")
@JsonSubTypes({
  @JsonSubTypes.Type(value = CpmiClusterMember.class, name = "CpmiClusterMember"),
  @JsonSubTypes.Type(value = CpmiGatewayCluster.class, name = "CpmiGatewayCluster"),
  @JsonSubTypes.Type(value = CpmiHostCkp.class, name = "CpmiHostCkp"),
  @JsonSubTypes.Type(value = CpmiVsClusterNetobj.class, name = "CpmiVsClusterNetobj"),
  @JsonSubTypes.Type(value = CpmiVsNetobj.class, name = "CpmiVsNetobj"),
  @JsonSubTypes.Type(value = CpmiVsxClusterMember.class, name = "CpmiVsxClusterMember"),
  @JsonSubTypes.Type(value = CpmiVsxClusterNetobj.class, name = "CpmiVsxClusterNetobj"),
  @JsonSubTypes.Type(value = CpmiVsxNetobj.class, name = "CpmiVsxNetobj"),
  @JsonSubTypes.Type(value = SimpleGateway.class, name = "simple-gateway"),
})
public abstract class GatewayOrServer extends NamedManagementObject implements AddressSpace {

  protected GatewayOrServer(
      @Nullable Ip ipv4Address,
      String name,
      List<Interface> interfaces,
      GatewayOrServerPolicy policy,
      Uid uid) {
    super(name, uid);
    _interfaces = interfaces;
    _ipv4Address = ipv4Address;
    _policy = policy;
  }

  @Override
  public final <T> T accept(AddressSpaceVisitor<T> visitor) {
    // TODO: more granularity?
    return visitor.visitGatewayOrServer(this);
  }

  public @Nonnull List<Interface> getInterfaces() {
    return _interfaces;
  }

  public @Nullable Ip getIpv4Address() {
    return _ipv4Address;
  }

  @Nonnull
  public GatewayOrServerPolicy getPolicy() {
    return _policy;
  }

  @Override
  protected boolean baseEquals(Object o) {
    if (!super.baseEquals(o)) {
      return false;
    }
    GatewayOrServer that = (GatewayOrServer) o;
    return _interfaces.equals(that._interfaces)
        && Objects.equals(_ipv4Address, that._ipv4Address)
        && _policy.equals(that._policy);
  }

  @Override
  protected int baseHashcode() {
    return Objects.hash(super.baseHashcode(), _interfaces, _ipv4Address, _policy);
  }

  @Override
  protected @Nonnull ToStringHelper baseToStringHelper() {
    return super.baseToStringHelper()
        .add(PROP_INTERFACES, _interfaces)
        .add(PROP_IPV4_ADDRESS, _ipv4Address)
        .add(PROP_POLICY, _policy);
  }

  protected static final String PROP_INTERFACES = "interfaces";
  protected static final String PROP_IPV4_ADDRESS = "ipv4-address";
  protected static final String PROP_POLICY = "policy";

  private final @Nonnull List<Interface> _interfaces;
  private final @Nullable Ip _ipv4Address;
  private final @Nonnull GatewayOrServerPolicy _policy;
}
