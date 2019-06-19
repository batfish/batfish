package org.batfish.datamodel.vendor_family;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.batfish.datamodel.vendor_family.cisco.CiscoFamily;
import org.batfish.datamodel.vendor_family.cumulus.CumulusFamily;
import org.batfish.datamodel.vendor_family.f5_bigip.F5BigipFamily;
import org.batfish.datamodel.vendor_family.juniper.JuniperFamily;

public class VendorFamily implements Serializable {

  public enum Type {
    AWS,
    CISCO,
    CUMULUS,
    F5_BIGIP,
    JUNIPER,
    UNKNOWN
  }

  private static final String PROP_AWS = "aws";
  private static final String PROP_CISCO = "cisco";
  private static final String PROP_CUMULUS = "cumulus";
  private static final String PROP_F5_BIGIP = "f5Bigip";
  private static final String PROP_JUNIPER = "juniper";
  private static final long serialVersionUID = 1L;

  private static Type toFamilyType(Object family) {
    if (family instanceof AwsFamily) {
      return Type.AWS;
    } else if (family instanceof CiscoFamily) {
      return Type.CISCO;
    } else if (family instanceof CumulusFamily) {
      return Type.CUMULUS;
    } else if (family instanceof F5BigipFamily) {
      return Type.F5_BIGIP;
    } else if (family instanceof JuniperFamily) {
      return Type.JUNIPER;
    }
    return Type.UNKNOWN;
  }

  private AwsFamily _aws;
  private CiscoFamily _cisco;
  private CumulusFamily _cumulus;
  private F5BigipFamily _f5Bigip;
  private JuniperFamily _juniper;

  @JsonProperty(PROP_AWS)
  public AwsFamily getAws() {
    return _aws;
  }

  @JsonProperty(PROP_CISCO)
  public CiscoFamily getCisco() {
    return _cisco;
  }

  @JsonProperty(PROP_CUMULUS)
  public CumulusFamily getCumulus() {
    return _cumulus;
  }

  @JsonProperty(PROP_F5_BIGIP)
  public F5BigipFamily getF5Bigip() {
    return _f5Bigip;
  }

  @JsonProperty(PROP_JUNIPER)
  public JuniperFamily getJuniper() {
    return _juniper;
  }

  @JsonProperty(PROP_AWS)
  public void setAws(AwsFamily aws) {
    _aws = aws;
  }

  @JsonProperty(PROP_CISCO)
  public void setCisco(CiscoFamily cisco) {
    _cisco = cisco;
  }

  @JsonProperty(PROP_CUMULUS)
  public void setCumulus(CumulusFamily cumulus) {
    _cumulus = cumulus;
  }

  @JsonProperty(PROP_F5_BIGIP)
  public void setF5Bigip(F5BigipFamily f5Bigip) {
    _f5Bigip = f5Bigip;
  }

  @JsonProperty(PROP_JUNIPER)
  public void setJuniper(JuniperFamily juniper) {
    _juniper = juniper;
  }

  /** Concatenates all non-null family pointers */
  @Override
  public String toString() {
    return String.join(
        " ",
        Stream.of(_aws, _cisco, _cumulus, _f5Bigip, _juniper)
            .filter(f -> f != null)
            .map(f -> Objects.toString(toFamilyType(f)))
            .collect(Collectors.toList()));
  }
}
