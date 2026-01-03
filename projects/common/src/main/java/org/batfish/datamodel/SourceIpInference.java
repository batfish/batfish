package org.batfish.datamodel;

import java.io.Serializable;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** Abstract class for source IP inference of locally generated IP packets. */
public abstract class SourceIpInference implements Serializable {
  /**
   * Returns the potential source IPs of a packet with the given {@code dstIp} originating on the
   * given {@link Configuration} in a VRF with the given {@link Fib}.
   */
  public abstract Set<Ip> getPotentialSourceIps(
      @Nonnull Ip dstIp, @Nullable Fib fib, @Nonnull Configuration c);
}
