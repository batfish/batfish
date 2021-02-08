package org.batfish.representation.cumulus;

import com.google.common.base.Objects;
import java.io.Serializable;
import javax.annotation.Nonnull;
import org.batfish.datamodel.Prefix;

/**
 * Represents {@code network 1.2.3.4/5 area 6} under {@code router ospf}.
 *
 * <p>See https://docs.frrouting.org/en/latest/ospfd.html#clicmd-networkA.B.C.D/MareaA.B.C.D.
 */
public final class OspfNetworkArea implements Serializable {
  private final long _area;
  private final @Nonnull Prefix _prefix;

  public OspfNetworkArea(@Nonnull Prefix prefix, long area) {
    _area = area;
    _prefix = prefix;
  }

  public long getArea() {
    return _area;
  }

  public @Nonnull Prefix getPrefix() {
    return _prefix;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof OspfNetworkArea)) {
      return false;
    }
    OspfNetworkArea that = (OspfNetworkArea) o;
    return _area == that._area && Objects.equal(_prefix, that._prefix);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(_area, _prefix);
  }
}
