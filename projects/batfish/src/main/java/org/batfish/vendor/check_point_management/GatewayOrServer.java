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

  protected GatewayOrServer(Ip ipv4Address, String name, GatewayOrServerPolicy policy, Uid uid) {
    super(name, uid);
    _ipv4Address = ipv4Address;
    _policy = policy;
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
    return _ipv4Address.equals(that._ipv4Address) && _policy.equals(that._policy);
  }

  @Override
  protected int baseHashcode() {
    return Objects.hash(super.baseHashcode(), _ipv4Address, _policy);
  }

  @Override
  protected @Nonnull ToStringHelper baseToStringHelper() {
    return super.baseToStringHelper()
        .add(PROP_IPV4_ADDRESS, _ipv4Address)
        .add(PROP_POLICY, _policy);
  }

  protected static final String PROP_IPV4_ADDRESS = "ipv4-address";
  protected static final String PROP_POLICY = "policy";

  private final @Nonnull Ip _ipv4Address;
  private final @Nonnull GatewayOrServerPolicy _policy;
}
