package org.batfish.vendor.sros.representation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;

/**
 * One entry of an SR-OS {@code prefix-list}. The YANG list is keyed by the composite of the {@code
 * ip-prefix} and the match {@code type} (e.g. {@code exact}, {@code longer}, {@code through},
 * {@code range}, {@code to}, {@code address-mask}), so the same prefix may appear under multiple
 * types.
 *
 * <p>The {@code through} and {@code range} types carry length bounds: {@code through} matches the
 * prefix's own length through {@code through-length}; {@code range} matches {@code start-length}
 * through {@code end-length}. The {@code to} type carries a list of {@code to-prefix}es, each
 * nested in the base prefix: it matches every prefix contained in the base whose length is in
 * {@code [base-length .. to-prefix-length]} (a length range like {@code through}, with the upper
 * bound supplied by the to-prefix — confirmed on SR-SIM 26.3.R1). The {@code address-mask} type
 * carries {@code mask-pattern}s; with a contiguous mask equal to the base prefix's length it is an
 * exact match. These are captured so conversion can build an exact length window rather than
 * over-approximating (see {@link SrosConversions#toRouteFilterList}).
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

  /**
   * The {@code to-prefix}es for a {@code to}-type entry (each nested in {@link #getPrefix()}).
   * Empty for other types.
   */
  public @Nonnull List<Prefix> getToPrefixes() {
    return _toPrefixes;
  }

  /** The {@code mask-pattern}s for an {@code address-mask}-type entry. Empty for other types. */
  public @Nonnull List<Ip> getMaskPatterns() {
    return _maskPatterns;
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
        && Objects.equals(_endLength, that._endLength)
        && _toPrefixes.equals(that._toPrefixes)
        && _maskPatterns.equals(that._maskPatterns);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        _prefix, _type, _throughLength, _startLength, _endLength, _toPrefixes, _maskPatterns);
  }

  private final @Nonnull Prefix _prefix;
  private final @Nonnull Type _type;
  private @Nullable Integer _throughLength;
  private @Nullable Integer _startLength;
  private @Nullable Integer _endLength;
  private final @Nonnull List<Prefix> _toPrefixes = new ArrayList<>();
  private final @Nonnull List<Ip> _maskPatterns = new ArrayList<>();
}
