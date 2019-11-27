package org.batfish.representation.cumulus;

import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/** Class for `update-source IFACE` in bgp neighbor */
@ParametersAreNonnullByDefault
public class BgpNeighborSourceInterface implements BgpNeighborSource {
  @Nonnull private final String _ifaceName;

  public BgpNeighborSourceInterface(@Nonnull String ifaceName) {
    _ifaceName = ifaceName;
  }

  @Nonnull
  public String getInterface() {
    return _ifaceName;
  }

  @Override
  public <T> T accept(BgpNeighborSourceVisitor<T> visitor) {
    return visitor.visitBgpNeighborSourceInterface(this);
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    } else if (!(o instanceof BgpNeighborSourceInterface)) {
      return false;
    }
    BgpNeighborSourceInterface other = (BgpNeighborSourceInterface) o;
    return _ifaceName.equals(other._ifaceName);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(_ifaceName);
  }
}
