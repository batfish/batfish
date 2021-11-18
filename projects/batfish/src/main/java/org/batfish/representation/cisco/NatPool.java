package org.batfish.representation.cisco;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.Comparators.max;
import static com.google.common.collect.Comparators.min;

import java.io.Serializable;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;

/** Representation of a NAT pool for Cisco devices. */
public final class NatPool implements Serializable {

  private final Ip _first;

  private final Ip _last;

  public NatPool(Ip first, Ip last) {
    _first = first;
    _last = last;
  }

  /**
   * Create a {@link NatPool} for the input first/last Ips, restricted to host Ips of the input
   * subnet.
   */
  public static NatPool create(Ip first, Ip last, Prefix subnet) {
    checkArgument(first.compareTo(last) <= 0, "first IP cannot be greater than last IP");
    Ip firstHostIp = subnet.getFirstHostIp();
    Ip lastHostIp = subnet.getLastHostIp();
    return new NatPool(
        min(max(first, firstHostIp), lastHostIp), min(max(last, firstHostIp), lastHostIp));
  }

  public Ip getFirst() {
    return _first;
  }

  public Ip getLast() {
    return _last;
  }
}
