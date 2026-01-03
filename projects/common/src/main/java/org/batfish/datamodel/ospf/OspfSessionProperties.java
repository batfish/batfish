package org.batfish.datamodel.ospf;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.IpLink;

/** Properties of an OSPF session that is compatible (based on two endpoint configurations). */
@ParametersAreNonnullByDefault
public final class OspfSessionProperties {

  private static final String PROP_AREA = "area";
  private static final String PROP_IP_LINK = "ipLink";

  private final long _area;
  private final @Nullable IpLink _ipLink;

  @JsonCreator
  private static @Nonnull OspfSessionProperties create(
      @JsonProperty(PROP_AREA) long area, @JsonProperty(PROP_IP_LINK) @Nullable IpLink ipLink) {
    checkArgument(ipLink != null, "Missing %s", PROP_IP_LINK);
    return new OspfSessionProperties(area, ipLink);
  }

  public OspfSessionProperties(long area, IpLink ipLink) {
    _area = area;
    _ipLink = ipLink;
  }

  @JsonProperty(PROP_AREA)
  public long getArea() {
    return _area;
  }

  @JsonProperty(PROP_IP_LINK)
  public IpLink getIpLink() {
    return _ipLink;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof OspfSessionProperties)) {
      return false;
    }
    OspfSessionProperties that = (OspfSessionProperties) o;
    return _area == that._area && Objects.equals(_ipLink, that._ipLink);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_area, _ipLink);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).add("area", _area).add("ipLink", _ipLink).toString();
  }
}
