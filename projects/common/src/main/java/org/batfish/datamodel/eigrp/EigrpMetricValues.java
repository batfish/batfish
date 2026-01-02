package org.batfish.datamodel.eigrp;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
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

  private @Nullable Long _bandwidth;
  private final long _delay;
  private final int _effectiveBandwidth;
  private final int _reliability;
  private final long _mtu;

  private EigrpMetricValues(
      @Nullable Long bandwidth, long delay, int effectiveBandwidth, int reliability, long mtu) {
    checkArgument(
        bandwidth == null || bandwidth >= 0,
        "Invalid %s value for EIGRP metric: %s",
        PROP_BANDWIDTH,
        bandwidth);
    checkArgument(delay >= 0, "Invalid %s value for EIGRP metric: %s", PROP_DELAY, delay);
    checkArgument(
        effectiveBandwidth >= 0 && effectiveBandwidth <= 255,
        "Invalid %s value for EIGRP metric: %s",
        PROP_EFFECTIVE_BANDWIDTH,
        effectiveBandwidth);
    checkArgument(mtu >= 0, "Invalid %s value for EIGRP metric: %s", PROP_MTU, mtu);
    _bandwidth = bandwidth;
    _delay = delay;
    _effectiveBandwidth = effectiveBandwidth;
    _reliability = reliability;
    _mtu = mtu;
  }

  /** Bandwidth value, in Kbps. Nonnull after snapshot postprocessing. */
  @JsonProperty(PROP_BANDWIDTH)
  public @Nullable Long getBandwidth() {
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

  @JsonIgnore
  public void setBandwidth(long bandwidth) {
    checkArgument(
        bandwidth >= 0, "Invalid %s value for EIGRP metric: %s", PROP_BANDWIDTH, bandwidth);
    _bandwidth = bandwidth;
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
    return Objects.equals(_bandwidth, that._bandwidth)
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
    private @Nullable Long _bandwidth;
    private Long _delay;
    private int _effectiveBandwidth = 0;
    private int _reliability = 0;
    private long _mtu = 0;

    private Builder() {}

    /** Bandwidth in Kbps */
    public @Nonnull Builder setBandwidth(@Nullable Long bandwidth) {
      _bandwidth = bandwidth;
      return this;
    }

    /** Bandwidth in Kbps */
    public @Nonnull Builder setBandwidth(double bandwidth) {
      _bandwidth = (long) bandwidth;
      return this;
    }

    /** Delay in picoseconds */
    public @Nonnull Builder setDelay(long delay) {
      _delay = delay;
      return this;
    }

    /** Delay in picoseconds */
    public @Nonnull Builder setDelay(double delay) {
      _delay = (long) delay;
      return this;
    }

    /** Effective bandwidth (0-255) */
    public @Nonnull Builder setEffectiveBandwidth(int effectiveBandwidth) {
      _effectiveBandwidth = effectiveBandwidth;
      return this;
    }

    /** Reliability (0-255) */
    public @Nonnull Builder setReliability(int reliability) {
      _reliability = reliability;
      return this;
    }

    /** MTU (in bytes) */
    public @Nonnull Builder setMtu(long mtu) {
      _mtu = mtu;
      return this;
    }

    public @Nonnull EigrpMetricValues build() {
      checkArgument(_delay != null, "Missing %s", PROP_DELAY);
      return new EigrpMetricValues(_bandwidth, _delay, _effectiveBandwidth, _reliability, _mtu);
    }
  }

  @JsonCreator
  private static EigrpMetricValues jsonCreator(
      @JsonProperty(PROP_BANDWIDTH) @Nullable Long bandwidth,
      @JsonProperty(PROP_DELAY) @Nullable Long delay,
      @JsonProperty(PROP_EFFECTIVE_BANDWIDTH) @Nullable Integer effectiveBandwidth,
      @JsonProperty(PROP_RELIABILITY) @Nullable Integer reliability,
      @JsonProperty(PROP_MTU) @Nullable Long mtu) {
    checkArgument(bandwidth != null, "Missing %s", PROP_BANDWIDTH);
    checkArgument(delay != null, "Missing %s", PROP_DELAY);
    checkArgument(effectiveBandwidth != null, "Missing %s", PROP_EFFECTIVE_BANDWIDTH);
    checkArgument(reliability != null, "Missing %s", PROP_RELIABILITY);
    checkArgument(mtu != null, "Missing %s", PROP_MTU);
    return new EigrpMetricValues(bandwidth, delay, effectiveBandwidth, reliability, mtu);
  }
}
