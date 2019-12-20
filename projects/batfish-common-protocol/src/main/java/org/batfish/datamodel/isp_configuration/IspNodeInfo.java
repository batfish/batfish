package org.batfish.datamodel.isp_configuration;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkArgument;
import static org.batfish.common.util.IspModelingUtils.getDefaultIspNodeName;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** Information about auto-generated ISP node */
public class IspNodeInfo {
  private static final String PROP_ASN = "asn";
  private static final String PROP_NAME = "name";

  private final long _asn;

  @Nonnull private final String _name;

  public IspNodeInfo(long asn, @Nonnull String name) {
    _asn = asn;
    _name = name;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof IspNodeInfo)) {
      return false;
    }
    IspNodeInfo that = (IspNodeInfo) o;
    return _asn == that._asn && _name.equals(that._name);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_asn, _name);
  }

  @JsonCreator
  private static IspNodeInfo jsonCreator(
      @JsonProperty(PROP_ASN) @Nullable Long asn, @JsonProperty(PROP_NAME) @Nullable String name) {
    checkArgument(asn != null, "Missing %", PROP_ASN);
    return new IspNodeInfo(asn, firstNonNull(name, getDefaultIspNodeName(asn)));
  }

  @JsonProperty(PROP_ASN)
  @Nonnull
  public long getAsn() {
    return _asn;
  }

  @JsonProperty(PROP_NAME)
  @Nonnull
  public String getName() {
    return _name;
  }
}
