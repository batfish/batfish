package org.batfish.datamodel.eigrp;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import java.io.Serializable;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public final class EigrpMetricValues implements Serializable {
  private static final String PROP_BANDWIDTH = "bandwidth";
  private static final String PROP_DELAY = "delay";
  private static final String PROP_EFFECTIVE_BANDWIDTH = "effectiveBandwidth";
  private static final String PROP_RELIABILITY = "reliability";
  private static final String PROP_MTU = "mtu";

  private long _bandwidth;
  private long _delay;
  private int _effectiveBandwidth;
  private int _reliability;
  private long _mtu;

  private EigrpMetricValues(
      long bandwidth, long delay, int effectiveBandwidth, int reliability, long mtu) {
    checkArgument(
        bandwidth >= 0, "Invalid %s value for EIGRP metric: %d", PROP_BANDWIDTH, bandwidth);
    checkArgument(delay >= 0, "Invalid %s value for EIGRP metric: %d", PROP_DELAY, delay);
    checkArgument(
        effectiveBandwidth >= 0 && effectiveBandwidth <= 255,
        "Invalid %s value for EIGRP metric: %d",
        PROP_EFFECTIVE_BANDWIDTH,
        effectiveBandwidth);
    checkArgument(mtu >= 0, "Invalid %s value for EIGRP metric: %d", PROP_MTU, mtu);
    _bandwidth = bandwidth;
    _delay = delay;
    _effectiveBandwidth = effectiveBandwidth;
    _reliability = reliability;
    _mtu = mtu;
  }

  /** Bandwidth value, in Kbps */
  @JsonProperty(PROP_BANDWIDTH)
  public long getBandwidth() {
    return _bandwidth;
  }

  /** Delay value, in picoseconds */
  @JsonProperty(PROP_DELAY)
  public long getDelay() {
    return _delay;
  }

  /** Effective bandwidth (load) value (0-255) */
  @JsonProperty(PROP_EFFECTIVE_BANDWIDTH)
  public int getEffectiveBandwidth() {
    return _effectiveBandwidth;
  }

  /** Reliability parameter (0-255) */
  @JsonProperty(PROP_RELIABILITY)
  public int getReliability() {
    return _reliability;
  }

  /** MTU parameter, in bytes */
  @JsonProperty(PROP_MTU)
  public long getMtu() {
    return _mtu;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof EigrpMetricValues)) {
      return false;
    }
    EigrpMetricValues that = (EigrpMetricValues) o;
    return _bandwidth == that._bandwidth
        && _delay == that._delay
        && _effectiveBandwidth == that._effectiveBandwidth
        && _reliability == that._reliability
        && _mtu == that._mtu;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_bandwidth, _delay, _effectiveBandwidth, _reliability, _mtu);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add(PROP_BANDWIDTH, _bandwidth)
        .add(PROP_DELAY, _delay)
        .add(PROP_EFFECTIVE_BANDWIDTH, _effectiveBandwidth)
        .add(PROP_RELIABILITY, _reliability)
        .add(PROP_MTU, _mtu)
        .toString();
  }

  public static Builder builder() {
    return new Builder();
  }

  public Builder toBuilder() {
    return builder()
        .setBandwidth(_bandwidth)
        .setDelay(_delay)
        .setEffectiveBandwidth(_effectiveBandwidth)
        .setReliability(_reliability)
        .setMtu(_mtu);
  }

  public static final class Builder {
    private Long _bandwidth;
    private Long _delay;
    private int _effectiveBandwidth = 0;
    private int _reliability = 0;
    private long _mtu = 0;

    private Builder() {}

    /** Bandwidth in Kbps */
    @Nonnull
    public Builder setBandwidth(long bandwidth) {
      _bandwidth = bandwidth;
      return this;
    }

    /** Bandwidth in Kbps */
    @Nonnull
    public Builder setBandwidth(double bandwidth) {
      _bandwidth = (long) bandwidth;
      return this;
    }

    /** Delay in picoseconds */
    @Nonnull
    public Builder setDelay(long delay) {
      _delay = delay;
      return this;
    }

    /** Delay in picoseconds */
    @Nonnull
    public Builder setDelay(double delay) {
      _delay = (long) delay;
      return this;
    }

    /** Effective bandwidth (0-255) */
    @Nonnull
    public Builder setEffectiveBandwidth(int effectiveBandwidth) {
      _effectiveBandwidth = effectiveBandwidth;
      return this;
    }

    /** Reliability (0-255) */
    @Nonnull
    public Builder setReliability(int reliability) {
      _reliability = reliability;
      return this;
    }

    /** MTU (in bytes) */
    @Nonnull
    public Builder setMtu(long mtu) {
      _mtu = mtu;
      return this;
    }

    @Nonnull
    public EigrpMetricValues build() {
      checkArgument(_bandwidth != null, "Missing %s", PROP_BANDWIDTH);
      checkArgument(_delay != null, "Missing %s", PROP_DELAY);
      return new EigrpMetricValues(_bandwidth, _delay, _effectiveBandwidth, _reliability, _mtu);
    }
  }

  @JsonCreator
  private static EigrpMetricValues jsonCreator(
      @Nullable @JsonProperty(PROP_BANDWIDTH) Long bandwidth,
      @Nullable @JsonProperty(PROP_DELAY) Long delay,
      @Nullable @JsonProperty(PROP_EFFECTIVE_BANDWIDTH) Integer effectiveBandwidth,
      @Nullable @JsonProperty(PROP_RELIABILITY) Integer reliability,
      @Nullable @JsonProperty(PROP_MTU) Long mtu) {
    checkArgument(bandwidth != null, "Missing %s", PROP_BANDWIDTH);
    checkArgument(delay != null, "Missing %s", PROP_DELAY);
    checkArgument(effectiveBandwidth != null, "Missing %s", PROP_EFFECTIVE_BANDWIDTH);
    checkArgument(reliability != null, "Missing %s", PROP_RELIABILITY);
    checkArgument(mtu != null, "Missing %s", PROP_MTU);
    return new EigrpMetricValues(bandwidth, delay, effectiveBandwidth, reliability, mtu);
  }
}
