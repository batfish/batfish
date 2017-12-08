package org.batfish.datamodel.vendor_family;

import java.io.Serializable;
import org.batfish.datamodel.vendor_family.cisco.CiscoFamily;
import org.batfish.datamodel.vendor_family.juniper.JuniperFamily;

public class VendorFamily implements Serializable {

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
}
