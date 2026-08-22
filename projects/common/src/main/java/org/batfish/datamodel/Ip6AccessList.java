package org.batfish.datamodel;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.collect.ImmutableList;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/** An ordered access-list for filtering IPv6 flows. */
@ParametersAreNonnullByDefault
public final class Ip6AccessList implements Serializable {

  public static final class Builder {

    private @Nonnull List<Ip6AccessListLine> _lines;
    private @Nullable String _name;

    private Builder() {
      _lines = ImmutableList.of();
    }

    public Ip6AccessList build() {
      checkArgument(
          _name != null,
          "IPv6 ACL must have a name");

      return new Ip6AccessList(
          _name,
          _lines);
    }

    public Builder setLines(
        List<Ip6AccessListLine> lines) {
      _lines = ImmutableList.copyOf(lines);
      return this;
    }

    public Builder setLines(
        Ip6AccessListLine... lines) {
      return setLines(
          ImmutableList.copyOf(lines));
    }

    public Builder setName(String name) {
      _name = name;
      return this;
    }
  }

  public static Builder builder() {
    return new Builder();
  }

  private Ip6AccessList(
      String name,
      List<Ip6AccessListLine> lines) {
    _name = name;
    _lines = ImmutableList.copyOf(lines);
  }

  /**
   * Filter an IPv6 flow using first-match semantics.
   *
   * <p>If no line matches, the result is an implicit deny with no matched
   * line number.
   */
  public @Nonnull FilterResult filter(
      Flow6 flow) {
    for (int i = 0; i < _lines.size(); i++) {
      Ip6AccessListLine line =
          _lines.get(i);

      if (line.matches(flow)) {
        return new FilterResult(
            i,
            line.getAction());
      }
    }

    return new FilterResult(
        null,
        LineAction.DENY);
  }

  public @Nonnull List<Ip6AccessListLine>
      getLines() {
    return _lines;
  }

  public @Nonnull String getName() {
    return _name;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Ip6AccessList)) {
      return false;
    }

    Ip6AccessList rhs =
        (Ip6AccessList) o;

    return _name.equals(rhs._name)
        && _lines.equals(rhs._lines);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        _name,
        _lines);
  }

  private final @Nonnull List<Ip6AccessListLine> _lines;
  private final @Nonnull String _name;
}
