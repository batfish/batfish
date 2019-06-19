package org.batfish.datamodel.vendor_family;

import java.io.Serializable;

public class AwsFamily implements Serializable {

  private static final long serialVersionUID = 1L;

  private String _region;

  private String _subnetId;

  private String _vpcId;

  public String getRegion() {
    return _region;
  }

  public String getSubnetId() {
    return _subnetId;
  }

  public String getVpcId() {
    return _vpcId;
  }

  public void setRegion(String region) {
    _region = region;
  }

  public void setSubnetId(String subnetId) {
    _subnetId = subnetId;
  }

  public void setVpcId(String vpcId) {
    _vpcId = vpcId;
  }
}
