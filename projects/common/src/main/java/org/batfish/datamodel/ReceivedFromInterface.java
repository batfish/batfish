package org.batfish.datamodel;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.google.common.base.MoreObjects;
import java.io.ObjectStreamException;
import java.io.Serial;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.visitors.ReceivedFromVisitor;

/**
 * Information indicating that a {@link BgpRoute} was received from a BGP unnumbered session on a
 * given interface.
 */
@ParametersAreNonnullByDefault
public final class ReceivedFromInterface implements ReceivedFrom {

  @Override
  public <T> T accept(ReceivedFromVisitor<T> visitor) {
    return visitor.visitReceivedFromInterface(this);
  }

  public static @Nonnull ReceivedFromInterface of(String iface, Ip linkLocalIp) {
    checkArgument(LINK_LOCAL_IPS.containsIp(linkLocalIp), "%s is not a link-local IP", linkLocalIp);
    ReceivedFromInterface ret = CACHE.get(new ReceivedFromInterface(iface, linkLocalIp));
    assert ret != null;
    return ret;
  }

  @JsonProperty(PROP_INTERFACE)
  public @Nonnull String getInterface() {
    return _interface;
  }

  @JsonProperty(PROP_LINK_LOCAL_IP)
  public @Nonnull Ip getLinkLocalIp() {
    return _linkLocalIp;
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    if (this == obj) {
      return true;
    } else if (!(obj instanceof ReceivedFromInterface)) {
      return false;
    }
    ReceivedFromInterface other = (ReceivedFromInterface) obj;
    return _interface.equals(other._interface) && _linkLocalIp.equals(other._linkLocalIp);
  }

  @Override
  public int hashCode() {
    return _interface.hashCode() * 31 + _linkLocalIp.hashCode();
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add(PROP_INTERFACE, _interface)
        .add(PROP_LINK_LOCAL_IP, _linkLocalIp)
        .toString();
  }

  @JsonCreator
  private static @Nonnull ReceivedFromInterface create(
      @JsonProperty(PROP_INTERFACE) @Nullable String iface,
      @JsonProperty(PROP_LINK_LOCAL_IP) @Nullable Ip linkLocalIp) {
    checkArgument(iface != null, "Missing %s", PROP_INTERFACE);
    checkArgument(linkLocalIp != null, "Missing %s", PROP_LINK_LOCAL_IP);
    return of(iface, linkLocalIp);
  }

  private ReceivedFromInterface(String iface, Ip linkLocalIp) {
    _interface = iface;
    _linkLocalIp = linkLocalIp;
  }

  private static final Prefix LINK_LOCAL_IPS = Prefix.strict("169.254.0.0/16");

  private static final String PROP_INTERFACE = "interface";
  private static final String PROP_LINK_LOCAL_IP = "linkLocalIp";

  // Soft values: let it be garbage collected in times of pressure.
  // Maximum size 2^20: Just some upper bound on cache size, well less than GiB.
  //   (16 bytes for int+ip (excluding interned string space), would be 16 MiB total).
  private static final LoadingCache<ReceivedFromInterface, ReceivedFromInterface> CACHE =
      Caffeine.newBuilder().softValues().maximumSize(1 << 20).build(r -> r);

  private final @Nonnull String _interface;
  private final @Nonnull Ip _linkLocalIp;

  /** Cache after deserialization. */
  @Serial
  private Object readResolve() throws ObjectStreamException {
    return CACHE.get(this);
  }
}
