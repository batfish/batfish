package org.batfish.vendor.check_point_management;

import com.google.common.base.MoreObjects.ToStringHelper;
import java.util.Objects;
import javax.annotation.Nonnull;
import org.batfish.datamodel.Ip;

/**
 * A gateway or server from the {@code objects} field of the response to the {@code
 * show-gateways-and-servers} command.
 */
public abstract class GatewayOrServer extends TypedManagementObject {

  protected GatewayOrServer(Ip ipv4Address, String name, Uid uid) {
    super(name, uid);
    _ipv4Address = ipv4Address;
  }

  public @Nonnull Ip getIpv4Address() {
    return _ipv4Address;
  }

  @Override
  protected boolean baseEquals(Object o) {
    if (!super.baseEquals(o)) {
      return false;
    }
    GatewayOrServer that = (GatewayOrServer) o;
    return _ipv4Address.equals(that._ipv4Address);
  }

  @Override
  protected int baseHashcode() {
    return Objects.hash(super.baseHashcode(), _ipv4Address);
  }

  @Override
  protected @Nonnull ToStringHelper baseToStringHelper() {
    return super.baseToStringHelper().add(PROP_IPV4_ADDRESS, _ipv4Address);
  }

  protected static final String PROP_IPV4_ADDRESS = "ipv4-address";

  private final @Nonnull Ip _ipv4Address;
}
