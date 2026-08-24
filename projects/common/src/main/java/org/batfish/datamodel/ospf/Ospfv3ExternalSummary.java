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

/** One OSPFv3 ASBR external summary-address configuration. */
@ParametersAreNonnullByDefault
public final class Ospfv3ExternalSummary
    implements Serializable {

  private static final String PROP_ADVERTISE =
      "advertise";

  private static final String PROP_PREFIX =
      "prefix";

  private static final String PROP_TAG =
      "tag";

  @JsonCreator
  public Ospfv3ExternalSummary(
      @JsonProperty(PROP_PREFIX)
          @Nullable Prefix6 prefix,
      @JsonProperty(PROP_ADVERTISE)
          @Nullable Boolean advertise,
      @JsonProperty(PROP_TAG)
          @Nullable Long tag) {

    checkArgument(
        prefix != null,
        "Missing OSPFv3 external summary prefix");

    if (tag != null) {
      checkArgument(
          tag >= 0L
              && tag <= 0xFFFFFFFFL,
          "Invalid OSPFv3 external summary tag %s",
          tag);
    }

    _prefix = prefix;

    _advertise =
        firstNonNull(
            advertise,
            true);

    _tag = tag;
  }

  @JsonProperty(PROP_PREFIX)
  public @Nonnull Prefix6 getPrefix() {
    return _prefix;
  }

  @JsonProperty(PROP_ADVERTISE)
  public boolean getAdvertise() {
    return _advertise;
  }

  @JsonProperty(PROP_TAG)
  public @Nullable Long getTag() {
    return _tag;
  }

  @Override
  public boolean equals(
      @Nullable Object o) {

    if (this == o) {
      return true;
    }

    if (!(o instanceof Ospfv3ExternalSummary)) {
      return false;
    }

    Ospfv3ExternalSummary rhs =
        (Ospfv3ExternalSummary) o;

    return _advertise == rhs._advertise
        && _prefix.equals(rhs._prefix)
        && Objects.equals(
            _tag,
            rhs._tag);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        _prefix,
        _advertise,
        _tag);
  }

  private final boolean _advertise;
  private final @Nonnull Prefix6 _prefix;
  private final @Nullable Long _tag;
}
