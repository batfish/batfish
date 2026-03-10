package org.batfish.vendor.cisco_nxos.representation;

import java.io.Serializable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/** Configuration for an SNMP community. */
public final class SnmpCommunity implements Serializable {
  public SnmpCommunity(String community) {
    _community = community;
  }

  public @Nonnull String getCommunity() {
    return _community;
  }

  public @Nullable String getAclName() {
    return _aclName;
  }

  public void setAclName(@Nullable String aclName) {
    _aclName = aclName;
  }

  public @Nullable String getAclNameV4() {
    return _aclNameV4;
  }

  public void setAclNameV4(@Nullable String aclNameV4) {
    _aclNameV4 = aclNameV4;
  }

  public @Nullable String getAclNameV6() {
    return _aclNameV6;
  }

  public void setAclNameV6(@Nullable String aclNameV6) {
    _aclNameV6 = aclNameV6;
  }

  private final @Nonnull String _community;
  private @Nullable String _aclName;
  private @Nullable String _aclNameV4;
  private @Nullable String _aclNameV6;
}
