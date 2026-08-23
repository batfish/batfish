package org.batfish.datamodel.ospf;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.Prefix6;

/** One configured OSPFv3 area aggregation range. */
@ParametersAreNonnullByDefault
public final class Ospfv3AreaRange
    implements Serializable {

  public enum Type {
    INTER_AREA,
    NSSA
  }

  private static final String PROP_ADVERTISE =
      "advertise";
  private static final String PROP_PREFIX =
      "prefix";
  private static final String PROP_TYPE =
      "type";

  @JsonCreator
  public Ospfv3AreaRange(
      @JsonProperty(PROP_PREFIX)
          @Nullable Prefix6 prefix,
      @JsonProperty(PROP_TYPE)
          @Nullable Type type,
      @JsonProperty(PROP_ADVERTISE)
          @Nullable Boolean advertise) {

    checkArgument(
        prefix != null,
        "Missing OSPFv3 area-range prefix");

    checkArgument(
        type != null,
        "Missing OSPFv3 area-range type");

    _prefix = prefix;
    _type = type;
    _advertise =
        firstNonNull(
            advertise,
            true);
  }

  @JsonProperty(PROP_PREFIX)
  public @Nonnull Prefix6 getPrefix() {
    return _prefix;
  }

  @JsonProperty(PROP_TYPE)
  public @Nonnull Type getType() {
    return _type;
  }

  @JsonProperty(PROP_ADVERTISE)
  public boolean getAdvertise() {
    return _advertise;
  }

  @Override
  public boolean equals(
      @Nullable Object o) {

    if (this == o) {
      return true;
    }

    if (!(o instanceof Ospfv3AreaRange)) {
      return false;
    }

    Ospfv3AreaRange rhs =
        (Ospfv3AreaRange) o;

    return _advertise == rhs._advertise
        && _prefix.equals(rhs._prefix)
        && _type == rhs._type;
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        _prefix,
        _type,
        _advertise);
  }

  private final boolean _advertise;
  private final @Nonnull Prefix6 _prefix;
  private final @Nonnull Type _type;
}
