package org.batfish.vendor.sros.representation;

import java.io.Serializable;
import java.util.Objects;
import javax.annotation.Nonnull;
import org.batfish.datamodel.Prefix;

/**
 * One entry of an SR-OS {@code prefix-list}. The YANG list is keyed by the composite of the {@code
 * ip-prefix} and the match {@code type} (e.g. {@code exact}, {@code longer}, {@code through},
 * {@code range}), so the same prefix may appear under multiple types.
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

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof PrefixListEntry)) {
      return false;
    }
    PrefixListEntry that = (PrefixListEntry) o;
    return _prefix.equals(that._prefix) && _type == that._type;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_prefix, _type);
  }

  private final @Nonnull Prefix _prefix;
  private final @Nonnull Type _type;
}
