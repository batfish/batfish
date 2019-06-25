package org.batfish.datamodel.eigrp;

import static org.batfish.datamodel.eigrp.EigrpProcessMode.CLASSIC;
import static org.batfish.datamodel.eigrp.EigrpProcessMode.NAMED;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.Objects;
import java.util.stream.LongStream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNullableByDefault;

/** Handles EIGRP metric information. */
public class EigrpMetric implements Serializable {
  private static final String PROP_CLASSIC_BANDWIDTH = "classic-bandwidth";
  private static final String PROP_CLASSIC_DELAY = "classic-delay";
  private static final String PROP_NAMED_BANDWIDTH = "named-bandwidth";
  private static final String PROP_NAMED_DELAY = "named-delay";
  private static final String PROP_MODE = "mode";

  public static final long EIGRP_BANDWIDTH = 10_000_000L;
  public static final long EIGRP_DELAY_PICO = 1_000_000L;
  private static final long EIGRP_CLASSIC_SCALE = 256L;
  private static final long EIGRP_WIDE_SCALE = 65536L;
  private static final long K1_DEFAULT = 1L;
  private static final long K3_DEFAULT = 1L;
  private final long _classicBandwidth; // 10^7 / Kbps
  private final long _classicDelay; // 10s of uS
  private final long _k1;
  private final long _k3;
  private final EigrpProcessMode _mode;
  private final long _namedBandwidth; // Kbps
  private final long _namedDelay; // Picoseconds
  private transient @Nullable Long _cost;

  /** Called by Builder and used to create interface metrics */
  private EigrpMetric(double bandwidth, double delay, EigrpProcessMode mode) {
    long eigrpBandwidth = (long) (bandwidth / 1000.0);
    long eigrpDelay = (long) delay;

    if (mode == NAMED) {
      _namedBandwidth = eigrpBandwidth;
      _namedDelay = eigrpDelay;
      _classicBandwidth = 0L;
      _classicDelay = 0L;
    } else {
      assert mode == CLASSIC;
      _classicBandwidth = EIGRP_BANDWIDTH / eigrpBandwidth;
      _classicDelay = eigrpDelay / EIGRP_DELAY_PICO / 10L;
      _namedBandwidth = 0L;
      _namedDelay = 0L;
    }

    // TODO set from arguments (from config)
    // https://github.com/batfish/batfish/issues/1946
    _k1 = K1_DEFAULT;
    _k3 = K3_DEFAULT;
    _mode = mode;
  }

  /** Called internally with pre-scaled values */
  @JsonCreator
  private EigrpMetric(
      @JsonProperty(PROP_CLASSIC_BANDWIDTH) long classicBandwidth,
      @JsonProperty(PROP_NAMED_BANDWIDTH) long namedBandwidth,
      @JsonProperty(PROP_CLASSIC_DELAY) long classicDelay,
      @JsonProperty(PROP_NAMED_DELAY) long namedDelay,
      @JsonProperty(PROP_MODE) EigrpProcessMode mode) {
    _classicBandwidth = classicBandwidth;
    _classicDelay = classicDelay;
    _k1 = K1_DEFAULT;
    _k3 = K3_DEFAULT;
    _mode = mode;
    _namedBandwidth = namedBandwidth;
    _namedDelay = namedDelay;
  }

  public static Builder builder() {
    return new Builder();
  }

  @Nonnull
  public EigrpMetric accumulate(EigrpMetric neighborInterfaceMetric, EigrpMetric routeMetric) {
    long classicBandwidth = 0L;
    long classicDelay = 0L;
    long namedBandwidth = 0L;
    long namedDelay = 0L;

    if (_mode == CLASSIC) {
      classicBandwidth =
          LongStream.of(
                  _classicBandwidth,
                  neighborInterfaceMetric._classicBandwidth,
                  routeMetric._classicBandwidth)
              .max()
              .getAsLong();
      classicDelay = _classicDelay + routeMetric._classicDelay;
    } else {
      namedBandwidth =
          LongStream.of(
                  _namedBandwidth,
                  neighborInterfaceMetric._namedBandwidth,
                  routeMetric._namedBandwidth)
              .min()
              .getAsLong();
      namedDelay = _namedDelay + routeMetric._namedDelay;
    }

    // Mode is set by this metric
    return new EigrpMetric(classicBandwidth, namedBandwidth, classicDelay, namedDelay, _mode);
  }

  private long computeCost() {
    if (_mode == CLASSIC) {
      return (_k1 * _classicBandwidth + _k3 * _classicDelay) * EIGRP_CLASSIC_SCALE;
    } else {
      return _k1 * (EIGRP_BANDWIDTH * EIGRP_WIDE_SCALE / _namedBandwidth)
          + _k3 * (_namedDelay * EIGRP_WIDE_SCALE / EIGRP_DELAY_PICO);
    }
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof EigrpMetric)) {
      return false;
    }
    EigrpMetric rhs = (EigrpMetric) obj;
    return Objects.equals(_classicBandwidth, rhs._classicBandwidth)
        && Objects.equals(_namedBandwidth, rhs._namedBandwidth)
        && Objects.equals(_classicDelay, rhs._classicDelay)
        && Objects.equals(_namedDelay, rhs._namedDelay)
        && Objects.equals(_mode, rhs._mode);
  }

  @Override
  public final int hashCode() {
    return Objects.hash(
        _classicBandwidth, _classicDelay, _mode.ordinal(), _namedBandwidth, _namedDelay);
  }

  /** The classic bandwidth metric */
  @JsonProperty(PROP_CLASSIC_BANDWIDTH)
  public long getClassicBandwidth() {
    return _classicBandwidth;
  }

  /** The classic delay metric */
  @JsonProperty(PROP_CLASSIC_DELAY)
  public long getClassicDelay() {
    return _classicDelay;
  }

  /** The composite cost */
  @JsonIgnore
  public long getCost() {
    if (_cost == null) {
      _cost = computeCost();
    }
    return _cost;
  }

  /** The named bandwidth metric */
  @JsonProperty(PROP_NAMED_BANDWIDTH)
  private long getNamedBandwidth() {
    return _namedBandwidth;
  }

  /** The named delay metric */
  @JsonProperty(PROP_NAMED_DELAY)
  private long getNamedDelay() {
    return _namedDelay;
  }

  /** EIGRP process mode of this metric */
  @JsonProperty(PROP_MODE)
  public EigrpProcessMode getMode() {
    return _mode;
  }

  /** The composite cost, after scaling for RIB */
  @JsonIgnore
  public long getRibMetric() {
    // TODO make configurable using 'metric rib-scale'
    int namedRibScale = 128;
    return getCost() / ((_mode == EigrpProcessMode.NAMED) ? namedRibScale : 1);
  }

  @ParametersAreNullableByDefault
  public static class Builder {
    @Nullable private Double _bandwidth;
    @Nullable private Double _defaultBandwidth;
    @Nullable private Double _defaultDelay;
    @Nullable private Double _delay;
    private EigrpProcessMode _mode;

    @Nullable
    public EigrpMetric build() {
      Double delay = _delay == null ? _defaultDelay : _delay;
      Double bandwidth = _bandwidth == null ? _defaultBandwidth : _bandwidth;

      if (bandwidth == null || bandwidth == 0D || delay == null || _mode == null) {
        return null;
      }
      return new EigrpMetric(bandwidth, delay, _mode);
    }

    public Builder setBandwidth(Double bandwidth) {
      _bandwidth = bandwidth;
      return this;
    }

    public Builder setDefaultDelay(Double defaultDelay) {
      _defaultDelay = defaultDelay;
      return this;
    }

    public Builder setDefaultBandwidth(Double defaultBandwidth) {
      _defaultBandwidth = defaultBandwidth;
      return this;
    }

    public Builder setDelay(Double delay) {
      _delay = delay;
      return this;
    }

    public Builder setMode(@Nonnull EigrpProcessMode mode) {
      _mode = mode;
      return this;
    }
  }
}
