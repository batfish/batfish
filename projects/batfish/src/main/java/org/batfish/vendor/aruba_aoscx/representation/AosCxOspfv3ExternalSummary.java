package org.batfish.vendor.aruba_aoscx.representation;

import java.io.Serializable;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.Prefix6;

/** One AOS-CX OSPFv3 ASBR external summary-address configuration. */
@ParametersAreNonnullByDefault
public final class AosCxOspfv3ExternalSummary
    implements Serializable {

  public AosCxOspfv3ExternalSummary(
      Prefix6 prefix,
      boolean advertise,
      @Nullable Long tag) {

    _prefix = prefix;
    _advertise = advertise;
    _tag = tag;
  }

  public @Nonnull Prefix6 getPrefix() {
    return _prefix;
  }

  public boolean getAdvertise() {
    return _advertise;
  }

  public @Nullable Long getTag() {
    return _tag;
  }

  @Override
  public boolean equals(
      @Nullable Object o) {

    if (this == o) {
      return true;
    }

    if (!(o
        instanceof AosCxOspfv3ExternalSummary)) {
      return false;
    }

    AosCxOspfv3ExternalSummary rhs =
        (AosCxOspfv3ExternalSummary) o;

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
