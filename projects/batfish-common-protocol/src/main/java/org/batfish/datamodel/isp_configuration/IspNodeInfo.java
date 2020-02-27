package org.batfish.datamodel.isp_configuration;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkArgument;
import static org.batfish.common.util.IspModelingUtils.getDefaultIspNodeName;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.Prefix;

/** Information about auto-generated ISP node */
@ParametersAreNonnullByDefault
public class IspNodeInfo {
  private static final String PROP_ASN = "asn";
  private static final String PROP_NAME = "name";
  private static final String PROP_ADDITIONAL_PREFIXES_TO_INTERNET = "additionalPrefixesToInternet";

  private final long _asn;

  @Nonnull private final String _name;

  @Nonnull private final List<Prefix> _additionalPrefixes;

  public IspNodeInfo(long asn, String name) {
    this(asn, name, ImmutableList.of());
  }

  public IspNodeInfo(long asn, String name, List<Prefix> additionalPrefixes) {
    _asn = asn;
    _name = name;
    _additionalPrefixes = additionalPrefixes;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof IspNodeInfo)) {
      return false;
    }
    IspNodeInfo that = (IspNodeInfo) o;
    return _asn == that._asn
        && _name.equals(that._name)
        && _additionalPrefixes.equals(that._additionalPrefixes);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_asn, _name, _additionalPrefixes);
  }

  @JsonCreator
  private static IspNodeInfo jsonCreator(
      @JsonProperty(PROP_ASN) @Nullable Long asn,
      @JsonProperty(PROP_NAME) @Nullable String name,
      @Nullable @JsonProperty(PROP_ADDITIONAL_PREFIXES_TO_INTERNET)
          List<Prefix> additionalPrefixes) {
    checkArgument(asn != null, "Missing %", PROP_ASN);
    return new IspNodeInfo(
        asn,
        firstNonNull(name, getDefaultIspNodeName(asn)),
        firstNonNull(additionalPrefixes, ImmutableList.of()));
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

  @JsonProperty(PROP_ADDITIONAL_PREFIXES_TO_INTERNET)
  @Nonnull
  public List<Prefix> getAdditionalPrefixes() {
    return _additionalPrefixes;
  }
}
