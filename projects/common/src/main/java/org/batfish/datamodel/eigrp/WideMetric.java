package org.batfish.datamodel.eigrp;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import com.google.common.primitives.UnsignedLong;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public final class WideMetric implements EigrpMetric {
  private static final String PROP_VALUES = "values";
  private static final String PROP_K1 = "k1";
  private static final String PROP_K2 = "k2";
  private static final String PROP_K3 = "k3";
  private static final String PROP_K4 = "k4";
  private static final String PROP_K5 = "k5";
  private static final String PROP_K6 = "k6";
  private static final String PROP_RIB_SCALE = "ribScale";

  private static final UnsignedLong WIDE_FACTOR = UnsignedLong.valueOf(1 << 16);
  private static final int BANDWIDTH_FACTOR = 10_000_000;

  private final @Nonnull EigrpMetricValues _values;
  private final short _k1;
  private final short _k2; // default to 0, https://github.com/batfish/batfish/issues/1946
  private final short _k3;
  private final short _k4; // default to 0, https://github.com/batfish/batfish/issues/1946
  private final short _k5; // default to 0, https://github.com/batfish/batfish/issues/1946
  // K6 reserved for future use. Applies to "jitter" and "energy" -- values not yet represented in
  // EigrpMetricValues
  private final short _k6;
  private final long _ribScale;

  private WideMetric(
      EigrpMetricValues values,
      short k1,
      short k2,
      short k3,
      short k4,
      short k5,
      short k6,
      long ribScale) {
    _values = values;
    _k1 = k1;
    _k2 = k2;
    _k3 = k3;
    _k4 = k4;
    _k5 = k5;
    _k6 = k6;
    _ribScale = ribScale;
  }

  /** {@link EigrpMetricValues metric values} */
  @Override
  @JsonProperty(PROP_VALUES)
  public @Nonnull EigrpMetricValues getValues() {
    return _values;
  }

  @JsonProperty(PROP_K1)
  public short getK1() {
    return _k1;
  }

  @JsonProperty(PROP_K2)
  public short getK2() {
    return _k2;
  }

  @JsonProperty(PROP_K3)
  public short getK3() {
    return _k3;
  }

  @JsonProperty(PROP_K4)
  public short getK4() {
    return _k4;
  }

  @JsonProperty(PROP_K5)
  public short getK5() {
    return _k5;
  }

  @JsonProperty(PROP_K6)
  public short getK6() {
    return _k6;
  }

  @JsonProperty(PROP_RIB_SCALE)
  public long getRibScale() {
    return _ribScale;
  }

  @Override
  public UnsignedLong cost(EigrpMetricVersion version) {
    return switch (version) {
      case V1 -> costV1();
      case V2 ->
          /* TODO */
          costV1();
    };
  }

  private UnsignedLong costV1() {
    checkState(_values.getBandwidth() != null, "Cannot calculate cost before bandwidth is set");
    UnsignedLong scaledBw =
        UnsignedLong.valueOf(BANDWIDTH_FACTOR)
            .times(WIDE_FACTOR)
            .dividedBy(UnsignedLong.valueOf(_values.getBandwidth()));

    // Delay (i.e., "total latency") is computed differently based on whether bandwidth is under 1
    // Gb/s. Since we keep bandwidth in kilobits, 10e6 is the magic constant
    UnsignedLong scaledDelay =
        _values.getBandwidth() < 1_000_000
            ? UnsignedLong.valueOf(_values.getDelay())
                .times(WIDE_FACTOR)
                .dividedBy(UnsignedLong.valueOf(10))
            // WARNING: do not trust formulas in CISCO docs or random internet posts
            // Use the RFC formula: https://tools.ietf.org/html/rfc7868#section-5.6.2.4
            // 10^6 * WIDE_FACTOR / bandwidth does not give correct results (does not match up with
            // GNS3)
            : UnsignedLong.valueOf(_values.getDelay())
                .times(WIDE_FACTOR)
                .dividedBy(UnsignedLong.valueOf(BANDWIDTH_FACTOR / 10));
    UnsignedLong metric =
        scaledBw
            .times(UnsignedLong.valueOf(_k1))
            .plus(
                scaledBw
                    .times(UnsignedLong.valueOf(_k2))
                    .dividedBy(UnsignedLong.valueOf(256 - _values.getEffectiveBandwidth())))
            .plus(scaledDelay.times(UnsignedLong.valueOf(_k3)));
    // + _k6 * "extended attributes" would go here as well
    if (_k5 != 0) {
      metric = metric.times(UnsignedLong.valueOf(((long) _k5) / (_values.getReliability() + _k4)));
    }
    return metric;
  }

  @Override
  public long ribMetric(EigrpMetricVersion version) {
    return cost(version).dividedBy(UnsignedLong.valueOf(_ribScale)).longValue();
  }

  @Override
  public boolean isCompatible(EigrpMetric o) {
    if (!(o instanceof WideMetric)) {
      return false;
    }
    WideMetric that = (WideMetric) o;
    return _k1 == that._k1
        && _k2 == that._k2
        && _k3 == that._k3
        && _k4 == that._k4
        && _k5 == that._k5
        && _k6 == that._k6;
  }

  @Override
  public WideMetric add(EigrpMetric o) {
    checkArgument(isCompatible(o), "Cannot add incompatible EIGRP metrics");
    assert _values.getBandwidth() != null && o.getValues().getBandwidth() != null;
    return toBuilder()
        .setValues(
            _values.toBuilder()
                .setBandwidth(Math.min(_values.getBandwidth(), o.getValues().getBandwidth()))
                .setDelay(_values.getDelay() + o.getValues().getDelay())
                .build())
        .build();
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof WideMetric)) {
      return false;
    }
    WideMetric that = (WideMetric) o;
    return _k1 == that._k1
        && _k2 == that._k2
        && _k3 == that._k3
        && _k4 == that._k4
        && _k5 == that._k5
        && _k6 == that._k6
        && _ribScale == that._ribScale
        && _values.equals(that._values);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_values, _k1, _k2, _k3, _k4, _k5, _k6, _ribScale);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add(PROP_VALUES, _values)
        .add(PROP_RIB_SCALE, _ribScale)
        .toString();
  }

  public Builder toBuilder() {
    return builder().setValues(_values).setK1(_k1).setK3(_k3);
  }

  @JsonCreator
  private static WideMetric jsonCreator(
      @JsonProperty(PROP_VALUES) @Nullable EigrpMetricValues values,
      @JsonProperty(PROP_K1) short k1,
      @JsonProperty(PROP_K2) short k2,
      @JsonProperty(PROP_K3) short k3,
      @JsonProperty(PROP_K4) short k4,
      @JsonProperty(PROP_K5) short k5,
      @JsonProperty(PROP_K6) short k6,
      @JsonProperty(PROP_RIB_SCALE) long ribScale) {
    checkArgument(values != null, "Missing %s", PROP_VALUES);
    return new WideMetric(values, k1, k2, k3, k4, k5, k6, ribScale);
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder {
    private @Nullable EigrpMetricValues _values;
    private short _k1 = 1;
    private short _k3 = 1;
    private long _ribScale = 128; // default value

    private Builder() {}

    public @Nonnull Builder setValues(@Nonnull EigrpMetricValues values) {
      _values = values;
      return this;
    }

    public @Nonnull Builder setK1(short k1) {
      checkArgument(k1 <= 255);
      _k1 = k1;
      return this;
    }

    public @Nonnull Builder setK1(int k1) {
      checkArgument(k1 <= 255);
      _k1 = (short) k1;
      return this;
    }

    public @Nonnull Builder setK3(short k3) {
      checkArgument(k3 <= 255);
      _k3 = k3;
      return this;
    }

    public @Nonnull Builder setK3(int k3) {
      checkArgument(k3 <= 255);
      _k3 = (short) k3;
      return this;
    }

    public @Nonnull Builder setRibScale(long ribScale) {
      _ribScale = ribScale;
      return this;
    }

    public @Nonnull WideMetric build() {
      checkArgument(_values != null, "Missing %s", PROP_VALUES);
      return new WideMetric(
          _values, _k1, (short) 0, _k3, (short) 0, (short) 0, (short) 0, _ribScale);
    }
  }
}
