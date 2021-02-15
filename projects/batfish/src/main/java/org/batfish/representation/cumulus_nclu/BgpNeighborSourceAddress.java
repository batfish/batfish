package org.batfish.representation.cumulus_nclu;

import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.Ip;

/** Class for `update-source ADDRESS` in bgp neighbor */
@ParametersAreNonnullByDefault
public class BgpNeighborSourceAddress implements BgpNeighborSource {
  @Nonnull private final Ip _address;

  public BgpNeighborSourceAddress(Ip address) {
    _address = address;
  }

  @Nonnull
  public Ip getAddress() {
    return _address;
  }

  @Override
  public <T> T accept(BgpNeighborSourceVisitor<T> visitor) {
    return visitor.visitBgpNeighborSourceAddress(this);
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    } else if (!(o instanceof BgpNeighborSourceAddress)) {
      return false;
    }
    BgpNeighborSourceAddress other = (BgpNeighborSourceAddress) o;
    return _address.equals(other._address);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(_address);
  }
}
