package org.batfish.datamodel;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Comparator;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/** Represents a Layer 3 link by a pair of IP addresses. */
@ParametersAreNonnullByDefault
public final class IpLink implements Comparable<IpLink> {

  private static final String PROP_IP1 = "ip1";
  private static final String PROP_IP2 = "ip2";

  private final @Nonnull Ip _ip1;
  private final @Nonnull Ip _ip2;

  @JsonCreator
  private static IpLink create(
      @JsonProperty(PROP_IP1) @Nullable Ip ip1, @JsonProperty(PROP_IP2) @Nullable Ip ip2) {
    checkArgument(ip1 != null);
    checkArgument(ip2 != null);
    return new IpLink(ip1, ip2);
  }

  /** Create a new IpLink based on two IP addresses. */
  public IpLink(@Nonnull Ip ip1, @Nonnull Ip ip2) {
    _ip1 = ip1;
    _ip2 = ip2;
  }

  @JsonProperty(PROP_IP1)
  public @Nonnull Ip getIp1() {
    return _ip1;
  }

  @JsonProperty(PROP_IP2)
  public @Nonnull Ip getIp2() {
    return _ip2;
  }

  @Override
  public int compareTo(@Nonnull IpLink other) {
    return Comparator.comparing(IpLink::getIp1).thenComparing(IpLink::getIp2).compare(this, other);
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    } else if (!(o instanceof IpLink)) {
      return false;
    }
    IpLink other = (IpLink) o;
    return _ip1.equals(other._ip1) && _ip2.equals(other._ip2);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_ip1, _ip2);
  }

  @Override
  public String toString() {
    return String.format("<%s:%s>", _ip1, _ip2);
  }
}
