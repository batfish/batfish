package org.batfish.datamodel.vendor_family;

import java.io.Serializable;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.batfish.datamodel.vendor_family.cisco.CiscoFamily;
import org.batfish.datamodel.vendor_family.juniper.JuniperFamily;

public class VendorFamily implements Serializable {

  public enum Type {
    AWS,
    CISCO,
    JUNIPER,
    UNKNOWN
  }

  /** */
  private static final long serialVersionUID = 1L;

  private AwsFamily _aws;

  private CiscoFamily _cisco;

  private JuniperFamily _juniper;

  public AwsFamily getAws() {
    return _aws;
  }

  public CiscoFamily getCisco() {
    return _cisco;
  }

  public JuniperFamily getJuniper() {
    return _juniper;
  }

  public void setAws(AwsFamily aws) {
    _aws = aws;
  }

  public void setCisco(CiscoFamily cisco) {
    _cisco = cisco;
  }

  public void setJuniper(JuniperFamily juniper) {
    _juniper = juniper;
  }

  private static Type toFamilyType(Object family) {
    if (family instanceof AwsFamily) {
      return Type.AWS;
    } else if (family instanceof CiscoFamily) {
      return Type.CISCO;
    } else if (family instanceof JuniperFamily) {
      return Type.JUNIPER;
    }
    return Type.UNKNOWN;
  }

  /** Concatenates all non-null family pointers */
  @Override
  public String toString() {
    return String.join(
        " ",
        Stream.of(_aws, _cisco, _juniper)
            .filter(f -> f != null)
            .map(f -> Objects.toString(toFamilyType(f)))
            .collect(Collectors.toList()));
  }
}
