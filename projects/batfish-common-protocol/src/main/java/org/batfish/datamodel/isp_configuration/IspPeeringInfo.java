package org.batfish.datamodel.isp_configuration;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Specification for peering between two modeled ISPs. It allows for specifying the two ISPs by
 * ASNs. Batfish will create a direct L3 link between the ISPs and a BGP session that does not
 * filter any routes.
 */
@ParametersAreNonnullByDefault
public class IspPeeringInfo {
  private static final String PROP_ASN1 = "asn1";
  private static final String PROP_ASN2 = "asn2";

  private final long _asn1;
  private final long _asn2;

  public IspPeeringInfo(long asn1, long asn2) {
    _asn1 = asn1;
    _asn2 = asn2;
  }

  @JsonCreator
  private static IspPeeringInfo jsonCreator(
      @JsonProperty(PROP_ASN1) @Nullable Long asn1, @JsonProperty(PROP_ASN2) @Nullable Long asn2) {
    checkArgument(asn1 != null, "Missing %", PROP_ASN1);
    checkArgument(asn2 != null, "Missing %", PROP_ASN2);
    return new IspPeeringInfo(asn1, asn2);
  }

  @JsonProperty(PROP_ASN1)
  public long getAsn1() {
    return _asn1;
  }

  @JsonProperty(PROP_ASN2)
  public long getAsn2() {
    return _asn2;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof IspPeeringInfo)) {
      return false;
    }
    IspPeeringInfo that = (IspPeeringInfo) o;
    return _asn1 == that._asn1 && _asn2 == that._asn2;
  }

  @Override
  public int hashCode() {
    return Objects.hash(_asn1, _asn2);
  }
}
