package org.batfish.datamodel;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableSet;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import javax.annotation.Nonnull;
import org.batfish.datamodel.visitors.GenericIpSpaceVisitor;

/**
 * Represents a space of IPv4 addresses using a whitelist and blacklist of {@link IpWildcard}s. The
 * blacklist takes priority, so if an {@link Ip} is matched by both lists, it is not in the space.
 *
 * <p>Any empty whitelist is equivalent to an {@link EmptyIpSpace}.
 */
public final class IpWildcardSetIpSpace implements IpSpace, Serializable {

  public static class Builder {

    private final ImmutableSet.Builder<IpWildcard> _blacklistBuilder;

    private final ImmutableSet.Builder<IpWildcard> _whitelistBuilder;

    private Builder() {
      _blacklistBuilder = ImmutableSet.builder();
      _whitelistBuilder = ImmutableSet.builder();
    }

    public IpWildcardSetIpSpace build() {
      return new IpWildcardSetIpSpace(_blacklistBuilder.build(), _whitelistBuilder.build());
    }

    public Builder excluding(IpWildcard... wildcards) {
      return excluding(Arrays.asList(wildcards));
    }

    public Builder excluding(Iterable<IpWildcard> wildcards) {
      _blacklistBuilder.addAll(wildcards);
      return this;
    }

    public Builder including(IpWildcard... wildcards) {
      return including(Arrays.asList(wildcards));
    }

    public Builder including(Iterable<IpWildcard> wildcards) {
      _whitelistBuilder.addAll(wildcards);
      return this;
    }
  }

  public static final IpWildcardSetIpSpace ANY =
      IpWildcardSetIpSpace.builder().including(IpWildcard.ANY).build();

  /** */
  private static final long serialVersionUID = 1L;

  public static Builder builder() {
    return new Builder();
  }

  private final Set<IpWildcard> _blacklist;

  private final Set<IpWildcard> _whitelist;

  private IpWildcardSetIpSpace(Set<IpWildcard> blacklist, Set<IpWildcard> whitelist) {
    _blacklist = ImmutableSet.copyOf(blacklist);
    _whitelist = ImmutableSet.copyOf(whitelist);
  }

  @Override
  public <R> R accept(GenericIpSpaceVisitor<R> ipSpaceVisitor) {
    return ipSpaceVisitor.visitIpWildcardSetIpSpace(this);
  }

  @Override
  public boolean containsIp(@Nonnull Ip ip) {
    return _blacklist.stream().noneMatch(w -> w.containsIp(ip))
        && _whitelist.stream().anyMatch(w -> w.containsIp(ip));
  }

  @Override
  public IpSpace complement() {
    /*
     * the current is first reject everything in blacklist.
     * then of what's left over accept everything in whitelist.
     * and then reject everything else
     *
     * the complement then is to accept everything in the blacklist
     * then reject everything in the whitelist
     * then accept everything else.
     */
    AclIpSpace.Builder builder = AclIpSpace.builder();
    _blacklist.forEach(builder::thenPermitting);
    _whitelist.forEach(builder::thenRejecting);
    builder.thenPermitting(UniverseIpSpace.INSTANCE);
    return builder.build();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || !(o instanceof IpWildcardSetIpSpace)) {
      return false;
    }
    IpWildcardSetIpSpace rhs = (IpWildcardSetIpSpace) o;
    return Objects.equals(_blacklist, rhs._blacklist) && Objects.equals(_whitelist, rhs._whitelist);
  }

  public Set<IpWildcard> getBlacklist() {
    return _blacklist;
  }

  public Set<IpWildcard> getWhitelist() {
    return _whitelist;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_blacklist, _whitelist);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(getClass())
        .add("blacklist", _blacklist)
        .add("whitelist", _whitelist)
        .toString();
  }
}
