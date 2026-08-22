package org.batfish.dataplane.traceroute;

import com.google.common.collect.ImmutableList;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/** Result of tracing one IPv6 forwarding path. */
@ParametersAreNonnullByDefault
public final class Ipv6Trace implements Serializable {

  public Ipv6Trace(
      Ipv6TraceDisposition disposition,
      List<Ipv6TraceHop> hops) {
    _disposition = disposition;
    _hops = ImmutableList.copyOf(hops);
  }

  public @Nonnull Ipv6TraceDisposition
      getDisposition() {
    return _disposition;
  }

  public @Nonnull List<Ipv6TraceHop> getHops() {
    return _hops;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Ipv6Trace)) {
      return false;
    }

    Ipv6Trace rhs =
        (Ipv6Trace) o;

    return _disposition == rhs._disposition
        && _hops.equals(rhs._hops);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        _disposition,
        _hops);
  }

  private final @Nonnull Ipv6TraceDisposition _disposition;
  private final @Nonnull List<Ipv6TraceHop> _hops;
}
