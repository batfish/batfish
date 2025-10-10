package org.batfish.datamodel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.google.common.base.MoreObjects;
import com.google.common.collect.Comparators;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Ordering;
import java.io.ObjectStreamException;
import java.io.Serial;
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
  private static final LoadingCache<IpWildcardSetIpSpace, IpWildcardSetIpSpace> CACHE =
      Caffeine.newBuilder().maximumSize(1_000_000).build(w -> w);

  public static IpWildcardSetIpSpace create(
      @Nonnull Set<IpWildcard> blacklist, @Nonnull Set<IpWildcard> whitelist) {
    return CACHE.get(new IpWildcardSetIpSpace(blacklist, whitelist));
  }

  /** A Builder for {@link IpWildcardSetIpSpace}. */
  public static class Builder {

    private final ImmutableSet.Builder<IpWildcard> _blacklistBuilder;

    private final ImmutableSet.Builder<IpWildcard> _whitelistBuilder;

    private Builder() {
      _blacklistBuilder = ImmutableSet.builder();
      _whitelistBuilder = ImmutableSet.builder();
    }

    public IpWildcardSetIpSpace build() {
      return create(_blacklistBuilder.build(), _whitelistBuilder.build());
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

  private final @Nonnull Set<IpWildcard> _blacklist;

  private final @Nonnull Set<IpWildcard> _whitelist;

  private IpWildcardSetIpSpace(
      @Nonnull Set<IpWildcard> blacklist, @Nonnull Set<IpWildcard> whitelist) {
    _blacklist = ImmutableSet.copyOf(blacklist);
    _whitelist = ImmutableSet.copyOf(whitelist);
  }

  @JsonCreator
  private static IpWildcardSetIpSpace jsonCreator(
      @JsonProperty(PROP_BLACKLIST) Set<IpWildcard> blacklist,
      @JsonProperty(PROP_WHITELIST) Set<IpWildcard> whitelist) {
    return create(
        blacklist == null ? ImmutableSet.of() : ImmutableSet.copyOf(blacklist),
        whitelist == null ? ImmutableSet.of() : ImmutableSet.copyOf(whitelist));
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
  public IpSpace complement() {
    if (_whitelist.isEmpty()) {
      // Pure blacklist is actually equivalent to EmptyIpSpace.
      return UniverseIpSpace.INSTANCE;
    } else if (_blacklist.isEmpty()) {
      // Pure whitelist, so block that and allow everything else.
      return IpWildcardSetIpSpace.create(_whitelist, ImmutableSet.of(IpWildcard.ANY));
    } else if (_whitelist.equals(ImmutableSet.of(IpWildcard.ANY))) {
      // A complement of a pure whitelist.
      return IpWildcardSetIpSpace.create(ImmutableSet.of(), _blacklist);
    }
    return super.complement();
  }

  @Override
  protected boolean exprEquals(Object o) {
    IpWildcardSetIpSpace rhs = (IpWildcardSetIpSpace) o;
    return (_hashCode == rhs._hashCode || _hashCode == 0 || rhs._hashCode == 0)
        && _blacklist.equals(rhs._blacklist)
        && _whitelist.equals(rhs._whitelist);
  }

  public @Nonnull Set<IpWildcard> getBlacklist() {
    return _blacklist;
  }

  @JsonProperty(PROP_BLACKLIST)
  private @Nonnull SortedSet<IpWildcard> getJsonBlacklist() {
    return ImmutableSortedSet.copyOf(_blacklist);
  }

  public @Nonnull Set<IpWildcard> getWhitelist() {
    return _whitelist;
  }

  @JsonProperty(PROP_WHITELIST)
  private @Nonnull SortedSet<IpWildcard> getJsonWhitelist() {
    return ImmutableSortedSet.copyOf(_whitelist);
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

  /** Re-intern after deserialization. */
  @Serial
  private Object readResolve() throws ObjectStreamException {
    return CACHE.get(this);
  }
}
