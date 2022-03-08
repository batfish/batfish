package org.batfish.representation.juniper;

import static com.google.common.base.MoreObjects.firstNonNull;

import java.io.Serializable;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import org.batfish.datamodel.AclIpSpace;
import org.batfish.datamodel.EmptyIpSpace;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.Prefix;

public class PrefixList implements Serializable {

  private boolean _hasIpv6;
  private final @Nonnull String _name;
  private final @Nonnull Set<Prefix> _prefixes;

  public PrefixList(@Nonnull String name) {
    _name = name;
    _prefixes = new TreeSet<>();
  }

  /**
   * Returns {@code true} if this {@link PrefixList} contains any IPv6 networks.
   *
   * <p>Note that a prefix-list can contain a mix of IPs, so this should {@code NOT} be used to
   * imply that this prefix-list is a no-op on IPv4.
   */
  public boolean getHasIpv6() {
    return _hasIpv6;
  }

  public @Nonnull String getName() {
    return _name;
  }

  public @Nonnull Set<Prefix> getPrefixes() {
    return _prefixes;
  }

  public void setHasIpv6(boolean hasIpv6) {
    _hasIpv6 = hasIpv6;
  }

  public @Nonnull IpSpace toIpSpace() {
    return firstNonNull(
        AclIpSpace.union(_prefixes.stream().map(Prefix::toIpSpace).collect(Collectors.toList())),
        EmptyIpSpace.INSTANCE);
  }
}
