package org.batfish.representation.fortios;

import java.io.Serializable;
import javax.annotation.Nullable;

/**
 * Internal UUID for tracking FortiOS structures that may be renamed or changed. The UUID persists
 * across renames and structure edits and can be used to track structure references when the
 * structure might change.
 */
public class BatfishUUID implements Serializable {
  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof BatfishUUID)) {
      return false;
    }
    BatfishUUID that = (BatfishUUID) o;
    return _uuid == that._uuid;
  }

  @Override
  public int hashCode() {
    return _uuid;
  }

  public BatfishUUID(int uuid) {
    _uuid = uuid;
  }

  private final int _uuid;
}
