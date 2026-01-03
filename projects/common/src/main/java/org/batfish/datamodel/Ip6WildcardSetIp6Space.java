package org.batfish.datamodel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
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
import org.batfish.datamodel.visitors.GenericIp6SpaceVisitor;

public class Ip6WildcardSetIp6Space extends Ip6Space {
  private static final LoadingCache<Ip6WildcardSetIp6Space, Ip6WildcardSetIp6Space> CACHE =
      Caffeine.newBuilder().maximumSize(1_000_000).build(w -> w);

  public static Ip6WildcardSetIp6Space create(
      @Nonnull Set<Ip6Wildcard> blocklist, @Nonnull Set<Ip6Wildcard> allowlist) {
    return CACHE.get(new Ip6WildcardSetIp6Space(blocklist, allowlist));
  }

  /** A Builder for {@link Ip6WildcardSetIp6Space}. */
  public static class Builder {

    private final ImmutableSet.Builder<Ip6Wildcard> _blocklistBuilder;

    private final ImmutableSet.Builder<Ip6Wildcard> _allowlistBuilder;

    private Builder() {
      _blocklistBuilder = ImmutableSet.builder();
      _allowlistBuilder = ImmutableSet.builder();
    }

    public Ip6WildcardSetIp6Space build() {
      return create(_blocklistBuilder.build(), _allowlistBuilder.build());
    }

    public Ip6WildcardSetIp6Space.Builder excluding(Ip6Wildcard... wildcards) {
      return excluding(Arrays.asList(wildcards));
    }

    public Ip6WildcardSetIp6Space.Builder excluding(Iterable<Ip6Wildcard> wildcards) {
      _blocklistBuilder.addAll(wildcards);
      return this;
    }

    public Ip6WildcardSetIp6Space.Builder including(Ip6Wildcard... wildcards) {
      return including(Arrays.asList(wildcards));
    }

    public Ip6WildcardSetIp6Space.Builder including(Iterable<Ip6Wildcard> wildcards) {
      _allowlistBuilder.addAll(wildcards);
      return this;
    }
  }

  public static final Ip6WildcardSetIp6Space ANY =
      Ip6WildcardSetIp6Space.builder().including(Ip6Wildcard.ANY).build();
  private static final String PROP_BLOCKLIST = "blocklist";
  private static final String PROP_ALLOWLIST = "allowlist";

  public static Ip6WildcardSetIp6Space.Builder builder() {
    return new Ip6WildcardSetIp6Space.Builder();
  }

  private final @Nonnull Set<Ip6Wildcard> _blocklist;

  private final @Nonnull Set<Ip6Wildcard> _allowlist;

  private Ip6WildcardSetIp6Space(
      @Nonnull Set<Ip6Wildcard> blocklist, @Nonnull Set<Ip6Wildcard> allowlist) {
    _blocklist = ImmutableSet.copyOf(blocklist);
    _allowlist = ImmutableSet.copyOf(allowlist);
  }

  @JsonCreator
  private static Ip6WildcardSetIp6Space jsonCreator(
      @JsonProperty(PROP_BLOCKLIST) Set<Ip6Wildcard> blocklist,
      @JsonProperty(PROP_ALLOWLIST) Set<Ip6Wildcard> allowlist) {
    return create(
        blocklist == null ? ImmutableSet.of() : ImmutableSet.copyOf(blocklist),
        allowlist == null ? ImmutableSet.of() : ImmutableSet.copyOf(allowlist));
  }

  @Override
  public <R> R accept(GenericIp6SpaceVisitor<R> ip6SpaceVisitor) {
    return ip6SpaceVisitor.visitIp6WildcardSetIp6Space(this);
  }

  @Override
  protected int compareSameClass(Ip6Space o) {
    return Comparator.comparing(
            Ip6WildcardSetIp6Space::getBlockList, Comparators.lexicographical(Ordering.natural()))
        .thenComparing(
            Ip6WildcardSetIp6Space::getAllowList, Comparators.lexicographical(Ordering.natural()))
        .compare(this, (Ip6WildcardSetIp6Space) o);
  }

  @Override
  protected boolean exprEquals(Object o) {
    Ip6WildcardSetIp6Space rhs = (Ip6WildcardSetIp6Space) o;
    return (_hashCode == rhs._hashCode || _hashCode == 0 || rhs._hashCode == 0)
        && _blocklist.equals(rhs._blocklist)
        && _allowlist.equals(rhs._allowlist);
  }

  @JsonIgnore
  public @Nonnull Set<Ip6Wildcard> getBlockList() {
    return _blocklist;
  }

  @JsonProperty(PROP_BLOCKLIST)
  private @Nonnull SortedSet<Ip6Wildcard> getJsonBlockList() {
    return ImmutableSortedSet.copyOf(_blocklist);
  }

  @JsonIgnore
  public @Nonnull Set<Ip6Wildcard> getAllowList() {
    return _allowlist;
  }

  @JsonProperty(PROP_ALLOWLIST)
  private @Nonnull SortedSet<Ip6Wildcard> getJsonAllowList() {
    return ImmutableSortedSet.copyOf(_allowlist);
  }

  @Override
  public int hashCode() {
    int h = _hashCode;
    if (h == 0) {
      h = 31 * _blocklist.hashCode() + _allowlist.hashCode();
      _hashCode = h;
    }
    return h;
  }

  @Override
  public @Nonnull String toString() {
    return MoreObjects.toStringHelper(getClass())
        .add(PROP_BLOCKLIST, _blocklist)
        .add(PROP_ALLOWLIST, _allowlist)
        .toString();
  }

  private transient int _hashCode;

  /** Re-intern after deserialization. */
  @Serial
  private Object readResolve() throws ObjectStreamException {
    return CACHE.get(this);
  }
}
