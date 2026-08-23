package org.batfish.datamodel;

import static java.util.Objects.requireNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Ordered IPv6 prefix list.
 *
 * <p>Lines use first-match semantics. If no line matches, the prefix is
 * implicitly denied.
 */
@ParametersAreNonnullByDefault
public final class PrefixList6 implements Serializable {

  /** One ordered IPv6 prefix-list line. */
  public static final class Line
      implements Serializable {

    @JsonCreator
    public Line(
        @JsonProperty("action")
            @Nullable LineAction action,
        @JsonProperty("prefix")
            @Nullable Prefix6 prefix,
        @JsonProperty("lengthRange")
            @Nullable SubRange lengthRange) {

      _action =
          requireNonNull(action);
      _prefix =
          requireNonNull(prefix);
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

      if (!(o instanceof Line)) {
        return false;
      }

      Line rhs = (Line) o;

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

  @JsonCreator
  public PrefixList6(
      @JsonProperty("lines")
          @Nullable List<Line> lines) {
    _lines =
        lines == null
            ? ImmutableList.of()
            : ImmutableList.copyOf(lines);
  }

  /** Prefix list whose implicit deny rejects every prefix. */
  public static @Nonnull PrefixList6 denyAll() {
    return new PrefixList6(
        ImmutableList.of());
  }

  @JsonProperty("lines")
  public @Nonnull List<Line> getLines() {
    return _lines;
  }

  /**
   * Return whether this prefix list permits the supplied prefix.
   */
  public boolean permits(Prefix6 candidate) {
    for (Line line : _lines) {
      if (!line.matches(candidate)) {
        continue;
      }

      return line.getAction()
          == LineAction.PERMIT;
    }

    return false;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    }

    if (!(o instanceof PrefixList6)) {
      return false;
    }

    PrefixList6 rhs =
        (PrefixList6) o;

    return _lines.equals(rhs._lines);
  }

  @Override
  public int hashCode() {
    return _lines.hashCode();
  }

  private final @Nonnull List<Line> _lines;
}
