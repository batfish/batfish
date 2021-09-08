package org.batfish.vendor.check_point_management;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.annotations.VisibleForTesting;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class ServiceTcp extends TypedManagementObject implements Service {
  @Override
  public <T> T accept(ServiceVisitor<T> visitor) {
    return visitor.visitServiceTcp(this);
  }

  /** Docs: Destination ports, a comma separated list of ports/ranges. */
  @Nonnull
  public String getPort() {
    return _port;
  }

  @Override
  public boolean equals(Object o) {
    if (!baseEquals(o)) {
      return false;
    }
    ServiceTcp serviceTcp = (ServiceTcp) o;
    return _port.equals(serviceTcp._port);
  }

  @Override
  public int hashCode() {
    return Objects.hash(baseHashcode(), _port);
  }

  @Override
  public String toString() {
    return baseToStringHelper().toString();
  }

  @JsonCreator
  private static @Nonnull ServiceTcp create(
      @JsonProperty(PROP_NAME) @Nullable String name,
      @JsonProperty(PROP_PORT) @Nullable String port,
      @JsonProperty(PROP_UID) @Nullable Uid uid) {
    checkArgument(name != null, "Missing %s", PROP_NAME);
    checkArgument(port != null, "Missing %s", PROP_PORT);
    checkArgument(uid != null, "Missing %s", PROP_UID);
    return new ServiceTcp(name, port, uid);
  }

  @VisibleForTesting
  public ServiceTcp(String name, String port, Uid uid) {
    super(name, uid);
    _port = port;
  }

  private static final String PROP_PORT = "port";

  @Nonnull private final String _port;
}
