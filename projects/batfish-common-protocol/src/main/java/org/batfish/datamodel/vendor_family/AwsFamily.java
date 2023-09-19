package org.batfish.datamodel.vendor_family;

import java.io.Serializable;
import javax.annotation.Nullable;

public class AwsFamily implements Serializable {

  private @Nullable String _accountId;
  private @Nullable String _region;
  private @Nullable String _subnetId;
  private @Nullable String _vpcId;

  public @Nullable String getAccountId() {
    return _accountId;
  }

  public @Nullable String getRegion() {
    return _region;
  }

  public @Nullable String getSubnetId() {
    return _subnetId;
  }

  public @Nullable String getVpcId() {
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
