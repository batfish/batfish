package org.batfish.representation.cisco;

import static com.google.common.base.MoreObjects.toStringHelper;

import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.Prefix;

@ParametersAreNonnullByDefault
public class BgpAggregateIpv4Network extends BgpAggregateNetwork {

  public BgpAggregateIpv4Network(Prefix prefix) {
    this(prefix, false, null, null, null, false);
  }

  public BgpAggregateIpv4Network(
      Prefix prefix,
      boolean asSet,
      @Nullable String suppressMap,
      @Nullable String advertiseMap,
      @Nullable String attributeMap,
      boolean summaryOnly) {
    setAsSet(asSet);
    _prefix = prefix;
    setAdvertiseMap(advertiseMap);
    setSuppressMap(suppressMap);
    setAttributeMap(attributeMap);
    setSummaryOnly(summaryOnly);
  }

  public @Nonnull Prefix getPrefix() {
    return _prefix;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (o == this) {
      return true;
    } else if (!(o instanceof BgpAggregateIpv4Network)) {
      return false;
    }
    BgpAggregateIpv4Network rhs = (BgpAggregateIpv4Network) o;
    return baseEquals(rhs) && _prefix.equals(rhs._prefix);
  }

  @Override
  public int hashCode() {
    return Objects.hash(baseHashcode(), _prefix);
  }

  @Override
  public String toString() {
    return toStringHelper(this)
        .omitNullValues()
        .add("prefix", _prefix)
        .add("advertiseMap", getAdvertiseMap())
        .add("asSet", getAsSet())
        .add("attributeMap", getAttributeMap())
        .add("summaryOnly", getSummaryOnly())
        .add("suppressMap", getSuppressMap())
        .toString();
  }

  private final @Nonnull Prefix _prefix;
}
