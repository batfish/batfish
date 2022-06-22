package org.batfish.datamodel;

import static com.google.common.base.Preconditions.checkArgument;
import static org.batfish.datamodel.Prefix.MULTICAST;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.visitors.ReceivedFromVisitor;

/**
 * Information indicating that a {@link BgpRoute} was received from a numbered session with a given
 * {@link Ip}.
 */
@ParametersAreNonnullByDefault
public final class ReceivedFromIp implements ReceivedFrom {

  @Override
  public <T> T accept(ReceivedFromVisitor<T> visitor) {
    return visitor.visitReceivedFromIp(this);
  }

  public static @Nonnull ReceivedFromIp of(Ip ip) {
    checkArgument(
        !ip.equals(Ip.ZERO) && !MULTICAST.containsIp(ip), "%s is not a valid unicast IP", ip);
    return new ReceivedFromIp(ip);
  }

  @JsonProperty(PROP_IP)
  public @Nonnull Ip getIp() {
    return _ip;
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    if (this == obj) {
      return true;
    } else if (!(obj instanceof ReceivedFromIp)) {
      return false;
    }
    ReceivedFromIp other = (ReceivedFromIp) obj;
    return _ip.equals(other._ip);
  }

  @Override
  public int hashCode() {
    return _ip.hashCode();
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).add(PROP_IP, _ip).toString();
  }

  @JsonCreator
  private static @Nonnull ReceivedFromIp create(@JsonProperty(PROP_IP) @Nullable Ip ip) {
    checkArgument(ip != null, "Missing %s", PROP_IP);
    return of(ip);
  }

  private ReceivedFromIp(Ip ip) {
    _ip = ip;
  }

  private static final String PROP_IP = "ip";
  private final @Nonnull Ip _ip;
}
