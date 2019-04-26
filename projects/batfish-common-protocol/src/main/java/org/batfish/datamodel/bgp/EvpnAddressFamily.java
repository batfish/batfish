package org.batfish.datamodel.bgp;

import static com.google.common.base.MoreObjects.firstNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableSortedSet;
import java.io.Serializable;
import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/** Configuration settings for EVPN address family */
@ParametersAreNonnullByDefault
public final class EvpnAddressFamily implements Serializable {
  private static final long serialVersionUID = 1L;
  private static final String PROP_L2_VNIS = "l2Vnis";
  private static final String PROP_L3_VNIS = "l3Vnis";

  @Nonnull private final SortedSet<Layer2VniConfig> _l2VNIs;
  @Nonnull private final SortedSet<Layer3VniConfig> _l3VNIs;

  public EvpnAddressFamily(Set<Layer2VniConfig> l2Vnis, Set<Layer3VniConfig> l3Vnis) {
    _l2VNIs = ImmutableSortedSet.copyOf(l2Vnis);
    _l3VNIs = ImmutableSortedSet.copyOf(l3Vnis);
  }

  @JsonCreator
  private static EvpnAddressFamily create(
      @Nullable @JsonProperty(PROP_L2_VNIS) Set<Layer2VniConfig> l2Vnis,
      @Nullable @JsonProperty(PROP_L3_VNIS) Set<Layer3VniConfig> l3Vnis) {
    return new EvpnAddressFamily(
        firstNonNull(l2Vnis, ImmutableSortedSet.of()),
        firstNonNull(l3Vnis, ImmutableSortedSet.of()));
  }

  /** L2 VNI associations and config */
  @Nonnull
  @JsonProperty(PROP_L2_VNIS)
  public SortedSet<Layer2VniConfig> getL2VNIs() {
    return _l2VNIs;
  }

  /** L3 VNI associations and config */
  @Nonnull
  @JsonProperty(PROP_L3_VNIS)
  public SortedSet<Layer3VniConfig> getL3VNIs() {
    return _l3VNIs;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof EvpnAddressFamily)) {
      return false;
    }
    EvpnAddressFamily that = (EvpnAddressFamily) o;
    return _l2VNIs.equals(that._l2VNIs) && _l3VNIs.equals(that._l3VNIs);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_l2VNIs, _l3VNIs);
  }
}
