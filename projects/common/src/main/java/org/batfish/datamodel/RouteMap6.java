package org.batfish.datamodel;

import static java.util.Objects.requireNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedMap;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.SortedMap;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Ordered IPv6 route-map representation.
 *
 * <p>Each route-map entry optionally references an embedded IPv6
 * prefix-list. Route-map entries and prefix-list lines both use first-match
 * semantics, with implicit deny when no entry matches.
 */
@ParametersAreNonnullByDefault
public final class RouteMap6 implements Serializable {

  /** One line in an IPv6 prefix list. */
  public static final class PrefixListLine
      implements Serializable {

    @JsonCreator
    public PrefixListLine(
        @JsonProperty("action")
            @Nullable LineAction action,
        @JsonProperty("prefix")
            @Nullable Prefix6 prefix,
        @JsonProperty("lengthRange")
            @Nullable SubRange lengthRange) {
      _action = requireNonNull(action);
      _prefix = requireNonNull(prefix);
      _lengthRange =
          requireNonNull(lengthRange);
    }

    @JsonProperty("action")
    public @Nonnull LineAction getAction() {
      return _action;
    }

    @JsonProperty("prefix")
    public @Nonnull Prefix6 getPrefix() {
      return _prefix;
    }

    @JsonProperty("lengthRange")
    public @Nonnull SubRange getLengthRange() {
      return _lengthRange;
    }

    public boolean matches(Prefix6 candidate) {
      int candidateLength =
          candidate.getPrefixLength();

      return candidateLength
              >= _prefix.getPrefixLength()
          && _lengthRange.includes(
              candidateLength)
          && _prefix.contains(
              candidate.getNetworkAddress());
    }

    @Override
    public boolean equals(@Nullable Object o) {
      if (this == o) {
        return true;
      }

      if (!(o instanceof PrefixListLine)) {
        return false;
      }

      PrefixListLine rhs =
          (PrefixListLine) o;

      return _action == rhs._action
          && _prefix.equals(rhs._prefix)
          && _lengthRange.equals(
              rhs._lengthRange);
    }

    @Override
    public int hashCode() {
      return Objects.hash(
          _action,
          _prefix,
          _lengthRange);
    }

    private final @Nonnull LineAction _action;
    private final @Nonnull Prefix6 _prefix;
    private final @Nonnull SubRange _lengthRange;
  }

  /** One ordered route-map sequence. */
  public static final class Entry
      implements Serializable {

    @JsonCreator
    public Entry(
        @JsonProperty("action")
            @Nullable LineAction action,
        @JsonProperty("matchPrefixList")
            @Nullable List<PrefixListLine>
                matchPrefixList,
        @JsonProperty("setMetric")
            @Nullable Long setMetric,
        @JsonProperty("setTag")
            @Nullable Long setTag) {
      _action = requireNonNull(action);

      /*
       * Null means this entry has no IPv6 prefix-list match and therefore
       * matches all IPv6 prefixes. An empty list means a prefix-list that
       * can never permit anything (for example, an undefined reference).
       */
      _matchPrefixList =
          matchPrefixList == null
              ? null
              : ImmutableList.copyOf(
                  matchPrefixList);

      _setMetric = setMetric;
      _setTag = setTag;
    }

    @JsonProperty("action")
    public @Nonnull LineAction getAction() {
      return _action;
    }

    @JsonProperty("matchPrefixList")
    public @Nullable List<PrefixListLine>
        getMatchPrefixList() {
      return _matchPrefixList;
    }

    @JsonProperty("setMetric")
    public @Nullable Long getSetMetric() {
      return _setMetric;
    }

    @JsonProperty("setTag")
    public @Nullable Long getSetTag() {
      return _setTag;
    }

    private boolean matches(Prefix6 prefix) {
      if (_matchPrefixList == null) {
        return true;
      }

      for (PrefixListLine line :
          _matchPrefixList) {
        if (!line.matches(prefix)) {
          continue;
        }

        return line.getAction()
            == LineAction.PERMIT;
      }

      // Prefix-list implicit deny.
      return false;
    }

    @Override
    public boolean equals(@Nullable Object o) {
      if (this == o) {
        return true;
      }

      if (!(o instanceof Entry)) {
        return false;
      }

      Entry rhs = (Entry) o;

      return _action == rhs._action
          && Objects.equals(
              _matchPrefixList,
              rhs._matchPrefixList)
          && Objects.equals(
              _setMetric,
              rhs._setMetric)
          && Objects.equals(
              _setTag,
              rhs._setTag);
    }

    @Override
    public int hashCode() {
      return Objects.hash(
          _action,
          _matchPrefixList,
          _setMetric,
          _setTag);
    }

    private final @Nonnull LineAction _action;
    private final @Nullable
        List<PrefixListLine> _matchPrefixList;
    private final @Nullable Long _setMetric;
    private final @Nullable Long _setTag;
  }

  /** Result of a permitted route-map evaluation. */
  public static final class Result {

    public Result(
        long metric,
        long tag) {
      _metric = metric;
      _tag = tag;
    }

    public long getMetric() {
      return _metric;
    }

    public long getTag() {
      return _tag;
    }

    @Override
    public boolean equals(@Nullable Object o) {
      if (this == o) {
        return true;
      }

      if (!(o instanceof Result)) {
        return false;
      }

      Result rhs = (Result) o;

      return _metric == rhs._metric
          && _tag == rhs._tag;
    }

    @Override
    public int hashCode() {
      return Objects.hash(
          _metric,
          _tag);
    }

    private final long _metric;
    private final long _tag;
  }

  @JsonCreator
  public RouteMap6(
      @JsonProperty("entries")
          @Nullable Map<Long, Entry> entries) {
    _entries =
        entries == null
            ? ImmutableSortedMap.of()
            : ImmutableSortedMap.copyOf(
                entries);
  }

  /** Route map whose implicit deny rejects every IPv6 route. */
  public static @Nonnull RouteMap6 denyAll() {
    return new RouteMap6(
        ImmutableSortedMap.of());
  }

  @JsonProperty("entries")
  public @Nonnull SortedMap<Long, Entry>
      getEntries() {
    return _entries;
  }

  /**
   * Evaluate this route map against one IPv6 prefix.
   *
   * @return empty when denied; otherwise the resulting metric/tag
   */
  public @Nonnull Optional<Result> process(
      Prefix6 prefix,
      long initialMetric,
      long initialTag) {

    for (Entry entry :
        _entries.values()) {

      if (!entry.matches(prefix)) {
        continue;
      }

      if (entry.getAction()
          == LineAction.DENY) {
        return Optional.empty();
      }

      return Optional.of(
          new Result(
              entry.getSetMetric() != null
                  ? entry.getSetMetric()
                  : initialMetric,
              entry.getSetTag() != null
                  ? entry.getSetTag()
                  : initialTag));
    }

    // Route-map implicit deny.
    return Optional.empty();
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    }

    if (!(o instanceof RouteMap6)) {
      return false;
    }

    RouteMap6 rhs = (RouteMap6) o;

    return _entries.equals(rhs._entries);
  }

  @Override
  public int hashCode() {
    return _entries.hashCode();
  }

  private final @Nonnull
      SortedMap<Long, Entry> _entries;
}
