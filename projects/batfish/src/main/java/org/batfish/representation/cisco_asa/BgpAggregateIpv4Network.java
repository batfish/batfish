package org.batfish.representation.cisco_asa;

import com.google.common.annotations.VisibleForTesting;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.Prefix;

@ParametersAreNonnullByDefault
public class BgpAggregateIpv4Network extends BgpAggregateNetwork {

  @Nonnull private final Prefix _prefix;

  public BgpAggregateIpv4Network(Prefix prefix) {
    _prefix = prefix;
  }

  @VisibleForTesting
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

  public @Nonnull Prefix getPrefix() {
    return _prefix;
  }

  @Override
  public int hashCode() {
    return Objects.hash(baseHashcode(), _prefix);
  }
}
