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
import java.util.Arrays;
import java.util.Comparator;
import java.util.Set;
import java.util.SortedSet;
import javax.annotation.Nonnull;
import org.batfish.datamodel.visitors.GenericIp6SpaceVisitor;

public class Ip6WildcardSetIp6Space extends Ip6Space {
  private static final LoadingCache<Ip6WildcardSetIp6Space, Ip6WildcardSetIp6Space> CACHE =
      Caffeine.newBuilder().maximumSize(1_000_000).build(w -> w);

  public static Ip6WildcardSetIp6Space create(
      @Nonnull Set<Ip6Wildcard> blacklist, @Nonnull Set<Ip6Wildcard> whitelist) {
    return CACHE.get(new Ip6WildcardSetIp6Space(blacklist, whitelist));
  }

  /** A Builder for {@link Ip6WildcardSetIp6Space}. */
  public static class Builder {

    private final ImmutableSet.Builder<Ip6Wildcard> _blacklistBuilder;

    private final ImmutableSet.Builder<Ip6Wildcard> _whitelistBuilder;

    private Builder() {
      _blacklistBuilder = ImmutableSet.builder();
      _whitelistBuilder = ImmutableSet.builder();
    }

    public Ip6WildcardSetIp6Space build() {
      return create(_blacklistBuilder.build(), _whitelistBuilder.build());
    }

    public Ip6WildcardSetIp6Space.Builder excluding(Ip6Wildcard... wildcards) {
      return excluding(Arrays.asList(wildcards));
    }

    public Ip6WildcardSetIp6Space.Builder excluding(Iterable<Ip6Wildcard> wildcards) {
      _blacklistBuilder.addAll(wildcards);
      return this;
    }

    public Ip6WildcardSetIp6Space.Builder including(Ip6Wildcard... wildcards) {
      return including(Arrays.asList(wildcards));
    }

    public Ip6WildcardSetIp6Space.Builder including(Iterable<Ip6Wildcard> wildcards) {
      _whitelistBuilder.addAll(wildcards);
      return this;
    }
  }

  public static final Ip6WildcardSetIp6Space ANY =
      Ip6WildcardSetIp6Space.builder().including(Ip6Wildcard.ANY).build();
  private static final String PROP_BLACKLIST = "blacklist";
  private static final String PROP_WHITELIST = "whitelist";

  public static Ip6WildcardSetIp6Space.Builder builder() {
    return new Ip6WildcardSetIp6Space.Builder();
  }

  private final @Nonnull Set<Ip6Wildcard> _blacklist;

  private final @Nonnull Set<Ip6Wildcard> _whitelist;

  private Ip6WildcardSetIp6Space(
      @Nonnull Set<Ip6Wildcard> blacklist, @Nonnull Set<Ip6Wildcard> whitelist) {
    _blacklist = ImmutableSet.copyOf(blacklist);
    _whitelist = ImmutableSet.copyOf(whitelist);
  }

  @JsonCreator
  private static Ip6WildcardSetIp6Space jsonCreator(
      @JsonProperty(PROP_BLACKLIST) Set<Ip6Wildcard> blacklist,
      @JsonProperty(PROP_WHITELIST) Set<Ip6Wildcard> whitelist) {
    return create(
        blacklist == null ? ImmutableSet.of() : ImmutableSet.copyOf(blacklist),
        whitelist == null ? ImmutableSet.of() : ImmutableSet.copyOf(whitelist));
  }

  @Override
  public <R> R accept(GenericIp6SpaceVisitor<R> Ip6SpaceVisitor) {
    return Ip6SpaceVisitor.visitIp6WildcardSetIp6Space(this);
  }

  @Override
  protected int compareSameClass(Ip6Space o) {
    return Comparator.comparing(
            Ip6WildcardSetIp6Space::getBlacklist, Comparators.lexicographical(Ordering.natural()))
        .thenComparing(
            Ip6WildcardSetIp6Space::getWhitelist, Comparators.lexicographical(Ordering.natural()))
        .compare(this, (Ip6WildcardSetIp6Space) o);
  }

  @Override
  protected boolean exprEquals(Object o) {
    Ip6WildcardSetIp6Space rhs = (Ip6WildcardSetIp6Space) o;
    return (_hashCode == rhs._hashCode || _hashCode == 0 || rhs._hashCode == 0)
        && _blacklist.equals(rhs._blacklist)
        && _whitelist.equals(rhs._whitelist);
  }

  public @Nonnull Set<Ip6Wildcard> getBlacklist() {
    return _blacklist;
  }

  @JsonProperty(PROP_BLACKLIST)
  private @Nonnull SortedSet<Ip6Wildcard> getJsonBlacklist() {
    return ImmutableSortedSet.copyOf(_blacklist);
  }

  public @Nonnull Set<Ip6Wildcard> getWhitelist() {
    return _whitelist;
  }

  @JsonProperty(PROP_WHITELIST)
  private @Nonnull SortedSet<Ip6Wildcard> getJsonWhitelist() {
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
  private Object readResolve() throws ObjectStreamException {
    return CACHE.get(this);
  }
}
