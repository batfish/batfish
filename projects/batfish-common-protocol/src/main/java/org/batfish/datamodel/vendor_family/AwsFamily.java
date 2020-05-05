package org.batfish.datamodel.vendor_family;

import java.io.Serializable;
import javax.annotation.Nullable;

public class AwsFamily implements Serializable {

  @Nullable private String _accountId;
  @Nullable private String _region;
  @Nullable private String _subnetId;
  @Nullable private String _vpcId;

  @Nullable
  public String getAccountId() {
    return _accountId;
  }

  @Nullable
  public String getRegion() {
    return _region;
  }

  @Nullable
  public String getSubnetId() {
    return _subnetId;
  }

  @Nullable
  public String getVpcId() {
    return _vpcId;
  }

  public void setAccountId(@Nullable String accountId) {
    _accountId = accountId;
  }

  public void setRegion(@Nullable String region) {
    _region = region;
  }

  public void setSubnetId(@Nullable String subnetId) {
    _subnetId = subnetId;
  }

  public void setVpcId(@Nullable String vpcId) {
    _vpcId = vpcId;
  }
}
