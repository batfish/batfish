package org.batfish.datamodel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import com.google.common.collect.Comparators;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Ordering;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Set;
import java.util.SortedSet;
import javax.annotation.Nonnull;
import org.batfish.datamodel.visitors.GenericIpSpaceVisitor;

/**
 * Represents a space of IPv4 addresses using a whitelist and blacklist of {@link IpWildcard}s. The
 * blacklist takes priority, so if an {@link Ip} is matched by both lists, it is not in the space.
 *
 * <p>Any empty whitelist is equivalent to an {@link EmptyIpSpace}.
 */
public final class IpWildcardSetIpSpace extends IpSpace {

  /** A Builder for {@link IpWildcardSetIpSpace}. */
  public static class Builder {

    private final ImmutableSortedSet.Builder<IpWildcard> _blacklistBuilder;

    private final ImmutableSortedSet.Builder<IpWildcard> _whitelistBuilder;

    private Builder() {
      _blacklistBuilder = ImmutableSortedSet.naturalOrder();
      _whitelistBuilder = ImmutableSortedSet.naturalOrder();
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
  private static final String PROP_BLACKLIST = "blacklist";
  private static final String PROP_WHITELIST = "whitelist";

  public static Builder builder() {
    return new Builder();
  }

  @Nonnull private final SortedSet<IpWildcard> _blacklist;

  @Nonnull private final SortedSet<IpWildcard> _whitelist;

  public IpWildcardSetIpSpace(
      @Nonnull Set<IpWildcard> blacklist, @Nonnull Set<IpWildcard> whitelist) {
    _blacklist = ImmutableSortedSet.copyOf(blacklist);
    _whitelist = ImmutableSortedSet.copyOf(whitelist);
  }

  @JsonCreator
  private static IpWildcardSetIpSpace jsonCreator(
      @JsonProperty(PROP_BLACKLIST) Set<IpWildcard> blacklist,
      @JsonProperty(PROP_WHITELIST) Set<IpWildcard> whitelist) {
    return new IpWildcardSetIpSpace(
        blacklist == null ? ImmutableSortedSet.of() : ImmutableSortedSet.copyOf(blacklist),
        whitelist == null ? ImmutableSortedSet.of() : ImmutableSortedSet.copyOf(whitelist));
  }

  @Override
  public <R> R accept(GenericIpSpaceVisitor<R> ipSpaceVisitor) {
    return ipSpaceVisitor.visitIpWildcardSetIpSpace(this);
  }

  @Override
  protected int compareSameClass(IpSpace o) {
    return Comparator.comparing(
            IpWildcardSetIpSpace::getBlacklist, Comparators.lexicographical(Ordering.natural()))
        .thenComparing(
            IpWildcardSetIpSpace::getWhitelist, Comparators.lexicographical(Ordering.natural()))
        .compare(this, (IpWildcardSetIpSpace) o);
  }

  @Override
  protected boolean exprEquals(Object o) {
    IpWildcardSetIpSpace rhs = (IpWildcardSetIpSpace) o;
    return (_hashCode == rhs._hashCode || _hashCode == 0 || rhs._hashCode == 0)
        && _blacklist.equals(rhs._blacklist)
        && _whitelist.equals(rhs._whitelist);
  }

  @Nonnull
  @JsonProperty(PROP_BLACKLIST)
  public SortedSet<IpWildcard> getBlacklist() {
    return _blacklist;
  }

  @Nonnull
  @JsonProperty(PROP_WHITELIST)
  public SortedSet<IpWildcard> getWhitelist() {
    return _whitelist;
  }

  @Override
  public int hashCode() {
    int h = _hashCode;
    if (h == 0) {
      h = 31 * _blacklist.hashCode() + _whitelist.hashCode();
      _hashCode = h;
    }
    return h;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(getClass())
        .add(PROP_BLACKLIST, _blacklist)
        .add(PROP_WHITELIST, _whitelist)
        .toString();
  }

  private transient int _hashCode;
}
