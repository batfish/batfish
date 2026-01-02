package org.batfish.datamodel;

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * An implementation of {@link SourceIpInference} that uses a constant source IP regardless of
 * destination IP and outgoing interface.
 */
public final class UseConstantIp extends SourceIpInference {
  private final @Nonnull Ip _ip;

  /** Return a singleton set of the given {@code ip}. */
  @Override
  public Set<Ip> getPotentialSourceIps(
      @Nonnull Ip dstIp, @Nullable Fib fib, @Nonnull Configuration c) {
    return ImmutableSet.of(_ip);
  }

  private UseConstantIp(@Nonnull Ip ip) {
    _ip = ip;
  }

  /** Factory for creating a {@link UseConstantIp}. */
  public static @Nonnull UseConstantIp create(@Nonnull Ip ip) {
    return new UseConstantIp(ip);
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof UseConstantIp)) {
      return false;
    }
    UseConstantIp useConstantIp = (UseConstantIp) o;
    return _ip.equals(useConstantIp._ip);
  }

  @Override
  public int hashCode() {
    return _ip.hashCode();
  }

  @Override
  public String toString() {
    return "UseConstantIp(" + _ip + ")";
  }
}
