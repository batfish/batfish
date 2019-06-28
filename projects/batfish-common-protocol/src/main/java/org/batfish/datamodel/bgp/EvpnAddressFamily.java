package org.batfish.datamodel.bgp;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableSortedSet;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/** Configuration settings for EVPN address family */
@ParametersAreNonnullByDefault
public final class EvpnAddressFamily extends AddressFamily {

  private static final String PROP_L2_VNIS = "l2Vnis";
  private static final String PROP_L3_VNIS = "l3Vnis";
  private static final String PROP_PROPAGATE_UNMATCHED = "propagateUnmatched";

  @Nonnull private final SortedSet<Layer2VniConfig> _l2VNIs;
  @Nonnull private final SortedSet<Layer3VniConfig> _l3VNIs;
  private final boolean _propagateUnmatched;

  private EvpnAddressFamily(
      Set<Layer2VniConfig> l2Vnis, Set<Layer3VniConfig> l3Vnis, boolean propagateUnmatched) {
    _l2VNIs = ImmutableSortedSet.copyOf(l2Vnis);
    _l3VNIs = ImmutableSortedSet.copyOf(l3Vnis);
    _propagateUnmatched = propagateUnmatched;
  }

  @JsonCreator
  private static EvpnAddressFamily create(
      @Nullable @JsonProperty(PROP_L2_VNIS) Set<Layer2VniConfig> l2Vnis,
      @Nullable @JsonProperty(PROP_L3_VNIS) Set<Layer3VniConfig> l3Vnis,
      @Nullable @JsonProperty(PROP_PROPAGATE_UNMATCHED) Boolean propagateUnmatched) {
    checkArgument(propagateUnmatched != null, "Missing %s", PROP_PROPAGATE_UNMATCHED);
    return new Builder()
        .setL2Vnis(firstNonNull(l2Vnis, ImmutableSortedSet.of()))
        .setL3Vnis(firstNonNull(l3Vnis, ImmutableSortedSet.of()))
        .setPropagateUnmatched(propagateUnmatched)
        .build();
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

  /**
   * Whether or not to re-advertise (propagate) EVPN routes that do not match any VNIs. If true,
   * such routes will be advertised to neighbors. If false, routes that do not match any VNI will be
   * dropped.
   */
  @JsonProperty(PROP_PROPAGATE_UNMATCHED)
  public boolean getPropagateUnmatched() {
    return _propagateUnmatched;
  }

  @Override
  public Type getType() {
    return Type.EVPN;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof EvpnAddressFamily)) {
      return false;
    }
    EvpnAddressFamily that = (EvpnAddressFamily) o;
    return _l2VNIs.equals(that._l2VNIs)
        && _l3VNIs.equals(that._l3VNIs)
        && _propagateUnmatched == that._propagateUnmatched;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_l2VNIs, _l3VNIs, _propagateUnmatched);
  }

  @Nonnull
  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder extends AddressFamily.Builder<Builder, EvpnAddressFamily> {
    @Nonnull private SortedSet<Layer2VniConfig> _l2Vnis;
    @Nonnull private SortedSet<Layer3VniConfig> _l3Vnis;
    @Nullable private Boolean _propagateUnmatched;

    private Builder() {
      _l2Vnis = ImmutableSortedSet.of();
      _l3Vnis = ImmutableSortedSet.of();
    }

    @Nonnull
    public Builder setL2Vnis(Collection<Layer2VniConfig> l2Vnis) {
      _l2Vnis = ImmutableSortedSet.copyOf(l2Vnis);
      return getThis();
    }

    @Nonnull
    public Builder setL3Vnis(Collection<Layer3VniConfig> l3Vnis) {
      _l3Vnis = ImmutableSortedSet.copyOf(l3Vnis);
      return getThis();
    }

    @Nonnull
    public Builder setPropagateUnmatched(boolean propagateUnmatched) {
      _propagateUnmatched = propagateUnmatched;
      return getThis();
    }

    @Nonnull
    @Override
    public Builder getThis() {
      return this;
    }

    @Nonnull
    @Override
    public EvpnAddressFamily build() {
      checkArgument(_propagateUnmatched != null, "Missing %s", PROP_PROPAGATE_UNMATCHED);
      return new EvpnAddressFamily(_l2Vnis, _l3Vnis, _propagateUnmatched);
    }
  }
}
