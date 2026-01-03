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

/** Classic EIGRP metric */
@ParametersAreNonnullByDefault
public final class ClassicMetric implements EigrpMetric {

  private static final String PROP_VALUES = "values";
  private static final String PROP_K1 = "k1";
  private static final String PROP_K2 = "k2";
  private static final String PROP_K3 = "k3";
  private static final String PROP_K4 = "k4";
  private static final String PROP_K5 = "k5";

  private static final int BANDWIDTH_FACTOR = 10_000_000;
  private static final int PICO_TO_TENS_OF_MS_FACTOR = 10_000_000;

  private final @Nonnull EigrpMetricValues _values;
  private final short _k1;
  private final short _k2; // default to 0, https://github.com/batfish/batfish/issues/1946
  private final short _k3;
  private final short _k4; // default to 0, https://github.com/batfish/batfish/issues/1946
  private final short _k5; // default to 0, https://github.com/batfish/batfish/issues/1946

  private ClassicMetric(
      EigrpMetricValues values, short k1, short k2, short k3, short k4, short k5) {
    _values = values;
    _k1 = k1;
    _k2 = k2;
    _k3 = k3;
    _k4 = k4;
    _k5 = k5;
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

  @Override
  public UnsignedLong cost(EigrpMetricVersion version) {
    return switch (version) {
      case V1 -> costV1();
      case V2 -> costV2();
    };
  }

  private UnsignedLong costV1() {
    checkState(_values.getBandwidth() != null, "Cannot calculate cost before bandwidth is set");
    long scaledBw = BANDWIDTH_FACTOR / _values.getBandwidth();
    long metric =
        ((_k1 * scaledBw)
                + ((_k2 * scaledBw) / (256 - _values.getEffectiveBandwidth())
                    // Scale delay from picoseconds to 10s of microseconds
                    // Do that first, to avoid overflow
                    + (_k3 * (_values.getDelay() / PICO_TO_TENS_OF_MS_FACTOR))))
            * 256;
    if (_k5 != 0) {
      metric = metric * ((long) _k5 / ((long) _values.getReliability() + _k4));
    }
    return UnsignedLong.valueOf(metric);
  }

  private UnsignedLong costV2() {
    checkState(_values.getBandwidth() != null, "Cannot calculate cost before bandwidth is set");
    long scaledBw = 256L * BANDWIDTH_FACTOR / _values.getBandwidth();
    long metricBw = (_k1 * scaledBw) + (_k2 * scaledBw) / (256 - _values.getEffectiveBandwidth());
    long metricDelay =
        // Scale delay from picoseconds to 10s of microseconds
        // Do that first, to avoid overflow
        256L * _k3 * (_values.getDelay() / PICO_TO_TENS_OF_MS_FACTOR);
    long metric = metricBw + metricDelay;
    if (_k5 != 0) {
      metric = metric * ((long) _k5 / ((long) _values.getReliability() + _k4));
    }
    return UnsignedLong.valueOf(metric);
  }

  @Override
  public long ribMetric(EigrpMetricVersion version) {
    return cost(version).longValue();
  }

  @Override
  public boolean isCompatible(EigrpMetric o) {
    if (!(o instanceof ClassicMetric)) {
      return false;
    }
    ClassicMetric that = (ClassicMetric) o;
    return _k1 == that._k1
        && _k2 == that._k2
        && _k3 == that._k3
        && _k4 == that._k4
        && _k5 == that._k5;
  }

  @Override
  public ClassicMetric add(EigrpMetric o) {
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
  public boolean equals(@Nonnull Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof ClassicMetric)) {
      return false;
    }
    ClassicMetric that = (ClassicMetric) o;
    return _k1 == that._k1
        && _k2 == that._k2
        && _k3 == that._k3
        && _k4 == that._k4
        && _k5 == that._k5
        && _values.equals(that._values);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_values, _k1, _k2, _k3, _k4, _k5);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).add(PROP_VALUES, _values).toString();
  }

  public Builder toBuilder() {
    return builder().setValues(_values).setK1(_k1).setK3(_k3);
  }

  @JsonCreator
  private static ClassicMetric jsonCreator(
      @JsonProperty(PROP_VALUES) @Nullable EigrpMetricValues values,
      @JsonProperty(PROP_K1) short k1,
      @JsonProperty(PROP_K2) short k2,
      @JsonProperty(PROP_K3) short k3,
      @JsonProperty(PROP_K4) short k4,
      @JsonProperty(PROP_K5) short k5) {
    checkArgument(values != null, "Missing %s", PROP_VALUES);
    return new ClassicMetric(values, k1, k2, k3, k4, k5);
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder {
    private @Nullable EigrpMetricValues _values;
    private short _k1 = 1;
    private short _k3 = 1;

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

    public @Nonnull ClassicMetric build() {
      checkArgument(_values != null, "Missing %s", PROP_VALUES);
      return new ClassicMetric(_values, _k1, (short) 0, _k3, (short) 0, (short) 0);
    }
  }
}
