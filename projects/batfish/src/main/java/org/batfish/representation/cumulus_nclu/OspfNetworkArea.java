package org.batfish.representation.cumulus_nclu;

import java.io.Serializable;
import java.util.Objects;
import javax.annotation.Nonnull;
import org.batfish.datamodel.Prefix;

/**
 * Represents {@code network A.B.C.D/M area AREA} under {@code router ospf}.
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
    return _area == that._area && Objects.equals(_prefix, that._prefix);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_area, _prefix);
  }
}
