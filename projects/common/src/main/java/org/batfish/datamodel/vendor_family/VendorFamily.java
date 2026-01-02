package org.batfish.datamodel.vendor_family;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.Serializable;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.batfish.datamodel.vendor_family.cisco.CiscoFamily;
import org.batfish.datamodel.vendor_family.cisco_nxos.CiscoNxosFamily;
import org.batfish.datamodel.vendor_family.cisco_xr.CiscoXrFamily;
import org.batfish.datamodel.vendor_family.cumulus.CumulusFamily;
import org.batfish.datamodel.vendor_family.f5_bigip.F5BigipFamily;
import org.batfish.datamodel.vendor_family.juniper.JuniperFamily;

public class VendorFamily implements Serializable {

  public enum Type {
    AWS,
    CISCO,
    CISCO_NXOS,
    CISCO_XR,
    CUMULUS,
    F5_BIGIP,
    JUNIPER,
    UNKNOWN
  }

  private static final String PROP_AWS = "aws";
  private static final String PROP_CISCO = "cisco";
  private static final String PROP_CISCO_NXOS = "cisco_nxos";
  private static final String PROP_CUMULUS = "cumulus";
  @Deprecated private static final String PROP_F5_BIGIP = "f5Bigip";
  private static final String PROP_JUNIPER = "juniper";

  private static Type toFamilyType(Object family) {
    if (family instanceof AwsFamily) {
      return Type.AWS;
    } else if (family instanceof CiscoFamily) {
      return Type.CISCO;
    } else if (family instanceof CiscoNxosFamily) {
      return Type.CISCO_NXOS;
    } else if (family instanceof CiscoXrFamily) {
      return Type.CISCO_XR;
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
  private CiscoNxosFamily _ciscoNxos;
  private CiscoXrFamily _ciscoXr;
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

  @JsonProperty(PROP_CISCO_NXOS)
  public CiscoNxosFamily getCiscoNxos() {
    return _ciscoNxos;
  }

  @JsonIgnore
  public CiscoXrFamily getCiscoXr() {
    return _ciscoXr;
  }

  @JsonProperty(PROP_CUMULUS)
  public CumulusFamily getCumulus() {
    return _cumulus;
  }

  @JsonIgnore
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

  @JsonProperty(PROP_CISCO_NXOS)
  public void setCiscoNxos(CiscoNxosFamily ciscoNxos) {
    _ciscoNxos = ciscoNxos;
  }

  @JsonIgnore
  public void setCiscoXr(CiscoXrFamily ciscoXr) {
    _ciscoXr = ciscoXr;
  }

  @JsonProperty(PROP_CUMULUS)
  public void setCumulus(CumulusFamily cumulus) {
    _cumulus = cumulus;
  }

  @JsonIgnore
  public void setF5Bigip(F5BigipFamily f5Bigip) {
    _f5Bigip = f5Bigip;
  }

  @Deprecated
  @JsonProperty(PROP_F5_BIGIP)
  private void setF5BigipDeprecated(JsonNode ignored) {}

  @JsonProperty(PROP_JUNIPER)
  public void setJuniper(JuniperFamily juniper) {
    _juniper = juniper;
  }

  /** Concatenates all non-null family pointers */
  @Override
  public String toString() {
    return Stream.of(_aws, _cisco, _ciscoNxos, _ciscoXr, _cumulus, _f5Bigip, _juniper)
        .filter(Objects::nonNull)
        .map(f -> Objects.toString(toFamilyType(f)))
        .collect(Collectors.joining(" "));
  }
}
