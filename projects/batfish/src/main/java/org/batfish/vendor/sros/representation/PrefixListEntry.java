package org.batfish.vendor.sros.representation;

import java.io.Serializable;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.Prefix;

/**
 * One entry of an SR-OS {@code prefix-list}. The YANG list is keyed by the composite of the {@code
 * ip-prefix} and the match {@code type} (e.g. {@code exact}, {@code longer}, {@code through},
 * {@code range}), so the same prefix may appear under multiple types.
 *
 * <p>The {@code through} and {@code range} types carry length bounds: {@code through} matches the
 * prefix's own length through {@code through-length}; {@code range} matches {@code start-length}
 * through {@code end-length}. These are captured so conversion can build an exact length window
 * rather than over-approximating (see {@link SrosConversions#toRouteFilterList}).
 */
public final class PrefixListEntry implements Serializable {

  /** The prefix-list entry match type. */
  public enum Type {
    EXACT,
    LONGER,
    THROUGH,
    RANGE,
    TO,
    ADDRESS_MASK;
  }

  public PrefixListEntry(Prefix prefix, Type type) {
    _prefix = prefix;
    _type = type;
  }

  public @Nonnull Prefix getPrefix() {
    return _prefix;
  }

  public @Nonnull Type getType() {
    return _type;
  }

  /** The {@code through-length} bound for a {@code through}-type entry, or {@code null}. */
  public @Nullable Integer getThroughLength() {
    return _throughLength;
  }

  public void setThroughLength(@Nullable Integer throughLength) {
    _throughLength = throughLength;
  }

  /** The {@code start-length} bound for a {@code range}-type entry, or {@code null}. */
  public @Nullable Integer getStartLength() {
    return _startLength;
  }

  public void setStartLength(@Nullable Integer startLength) {
    _startLength = startLength;
  }

  /** The {@code end-length} bound for a {@code range}-type entry, or {@code null}. */
  public @Nullable Integer getEndLength() {
    return _endLength;
  }

  public void setEndLength(@Nullable Integer endLength) {
    _endLength = endLength;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof PrefixListEntry)) {
      return false;
    }
    PrefixListEntry that = (PrefixListEntry) o;
    return _prefix.equals(that._prefix)
        && _type == that._type
        && Objects.equals(_throughLength, that._throughLength)
        && Objects.equals(_startLength, that._startLength)
        && Objects.equals(_endLength, that._endLength);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_prefix, _type, _throughLength, _startLength, _endLength);
  }

  private final @Nonnull Prefix _prefix;
  private final @Nonnull Type _type;
  private @Nullable Integer _throughLength;
  private @Nullable Integer _startLength;
  private @Nullable Integer _endLength;
}
