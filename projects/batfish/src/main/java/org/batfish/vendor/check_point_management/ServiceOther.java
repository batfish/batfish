package org.batfish.vendor.check_point_management;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.annotations.VisibleForTesting;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class ServiceOther extends TypedManagementObject implements Service {
  @Override
  public <T> T accept(ServiceVisitor<T> visitor) {
    return visitor.visitServiceOther(this);
  }

  /** Docs: IP protocol number. */
  public int getIpProtocol() {
    return _ipProtocol;
  }

  /** Docs: A string in INSPECT syntax on packet matching. */
  public @Nullable String getMatch() {
    return _match;
  }

  @Override
  public boolean equals(Object o) {
    if (!baseEquals(o)) {
      return false;
    }
    ServiceOther other = (ServiceOther) o;
    return _ipProtocol == other._ipProtocol && Objects.equals(_match, other._match);
  }

  @Override
  public int hashCode() {
    return Objects.hash(baseHashcode(), _ipProtocol, _match);
  }

  @Override
  public String toString() {
    return baseToStringHelper().add(PROP_IP_PROTOCOL, _ipProtocol).toString();
  }

  @JsonCreator
  private static @Nonnull ServiceOther create(
      @JsonProperty(PROP_NAME) @Nullable String name,
      @JsonProperty(PROP_IP_PROTOCOL) @Nullable Integer ipProtocol,
      @JsonProperty(PROP_MATCH) @Nullable String match,
      @JsonProperty(PROP_UID) @Nullable Uid uid) {
    checkArgument(name != null, "Missing %s", PROP_NAME);
    checkArgument(ipProtocol != null, "Missing %s", PROP_IP_PROTOCOL);
    checkArgument(uid != null, "Missing %s", PROP_UID);
    return new ServiceOther(name, ipProtocol, match, uid);
  }

  @VisibleForTesting
  public ServiceOther(String name, int ipProtocol, @Nullable String match, Uid uid) {
    super(name, uid);
    _ipProtocol = ipProtocol;
    _match = match;
  }

  private static final String PROP_IP_PROTOCOL = "ip-protocol";
  private static final String PROP_MATCH = "match";

  private final int _ipProtocol;
  private final @Nullable String _match;
}
