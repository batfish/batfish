package org.batfish.vendor.check_point_management;

import static com.google.common.base.MoreObjects.toStringHelper;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.google.common.base.MoreObjects.ToStringHelper;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;
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
public abstract class GatewayOrServer implements AddressSpace, Machine, Serializable {

  protected GatewayOrServer(
      Ip ipv4Address,
      String name,
      List<Interface> interfaces,
      GatewayOrServerPolicy policy,
      Uid uid) {
    _interfaces = interfaces;
    _ipv4Address = ipv4Address;
    _name = name;
    _policy = policy;
    _uid = uid;
  }

  @Override
  public final <T> T accept(AddressSpaceVisitor<T> visitor) {
    // do we need individual implementations?
    return visitor.visitGatewayOrServer(this);
  }

  @Override
  public final <T> T accept(MachineVisitor<T> visitor) {
    return visitor.visitGatewayOrServer(this);
  }

  public @Nonnull List<Interface> getInterfaces() {
    return _interfaces;
  }

  public @Nonnull Ip getIpv4Address() {
    return _ipv4Address;
  }

  public @Nonnull String getName() {
    return _name;
  }

  @Nonnull
  public GatewayOrServerPolicy getPolicy() {
    return _policy;
  }

  public @Nonnull Uid getUid() {
    return _uid;
  }

  protected boolean baseEquals(Object o) {
    if (!(o instanceof GatewayOrServer)) {
      return false;
    }
    GatewayOrServer that = (GatewayOrServer) o;
    return _interfaces.equals(that._interfaces)
        && _ipv4Address.equals(that._ipv4Address)
        && _name.equals(that._name)
        && _policy.equals(that._policy)
        && _uid.equals(that._uid);
  }

  protected int baseHashcode() {
    return Objects.hash(_interfaces, _ipv4Address, _name, _policy, _uid);
  }

  protected @Nonnull ToStringHelper baseToStringHelper() {
    return toStringHelper(getClass())
        .add(PROP_INTERFACES, _interfaces)
        .add(PROP_IPV4_ADDRESS, _ipv4Address)
        .add(PROP_NAME, _name)
        .add(PROP_POLICY, _policy)
        .add(PROP_UID, _uid);
  }

  protected static final String PROP_INTERFACES = "interfaces";
  protected static final String PROP_IPV4_ADDRESS = "ipv4-address";
  protected static final String PROP_NAME = "name";
  protected static final String PROP_POLICY = "policy";
  protected static final String PROP_UID = "uid";

  private final @Nonnull List<Interface> _interfaces;
  private final @Nonnull Ip _ipv4Address;
  private final @Nonnull String _name;
  private final @Nonnull GatewayOrServerPolicy _policy;
  private final @Nonnull Uid _uid;
}
