package org.batfish.vendor.cisco_ftd.representation;

import java.io.Serializable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.LineAction;

/** Represents a single line in a Cisco FTD access list. */
public class FtdAccessListLine implements Serializable {

  public enum AclType {
    EXTENDED,
    ADVANCED,
    REMARK
  }

  private final @Nonnull String _name;
  private final @Nonnull AclType _aclType;
  private @Nullable LineAction _action;
  private @Nullable String _protocol;
  private @Nullable FtdAccessListAddressSpecifier _sourceAddressSpecifier;
  private @Nullable FtdAccessListAddressSpecifier _destinationAddressSpecifier;
  private @Nullable String _sourcePortSpecifier;
  private @Nullable String _destinationPortSpecifier;
  private @Nullable String _interfaceName;
  private @Nullable String _remark;
  private @Nullable Long _ruleId;

  /** Whether this line used the FTD-specific {@code trust} action keyword (semantically PERMIT). */
  private boolean _trust;

  private boolean _inactive;
  private boolean _log;
  private @Nullable String _timeRange;

  private FtdAccessListLine(@Nonnull String name, @Nonnull AclType aclType) {
    _name = name;
    _aclType = aclType;
  }

  public static FtdAccessListLine createRemark(@Nonnull String name, @Nullable String remark) {
    FtdAccessListLine line = new FtdAccessListLine(name, AclType.REMARK);
    line._remark = remark;
    return line;
  }

  public static FtdAccessListLine createExtended(
      @Nonnull String name,
      @Nonnull LineAction action,
      @Nullable String protocol,
      @Nullable FtdAccessListAddressSpecifier source,
      @Nullable FtdAccessListAddressSpecifier destination) {
    FtdAccessListLine line = new FtdAccessListLine(name, AclType.EXTENDED);
    line._action = action;
    line._protocol = protocol;
    line._sourceAddressSpecifier = source;
    line._destinationAddressSpecifier = destination;
    return line;
  }

  public static FtdAccessListLine createAdvanced(
      @Nonnull String name,
      @Nonnull LineAction action,
      @Nullable String protocol,
      @Nullable FtdAccessListAddressSpecifier source,
      @Nullable FtdAccessListAddressSpecifier destination) {
    FtdAccessListLine line = new FtdAccessListLine(name, AclType.ADVANCED);
    line._action = action;
    line._protocol = protocol;
    line._sourceAddressSpecifier = source;
    line._destinationAddressSpecifier = destination;
    return line;
  }

  public @Nonnull String getName() {
    return _name;
  }

  public @Nonnull AclType getAclType() {
    return _aclType;
  }

  public @Nullable LineAction getAction() {
    return _action;
  }

  public @Nullable String getProtocol() {
    return _protocol;
  }

  public @Nullable FtdAccessListAddressSpecifier getSourceAddressSpecifier() {
    return _sourceAddressSpecifier;
  }

  public @Nullable FtdAccessListAddressSpecifier getDestinationAddressSpecifier() {
    return _destinationAddressSpecifier;
  }

  public @Nullable String getSourcePortSpecifier() {
    return _sourcePortSpecifier;
  }

  public void setSourcePortSpecifier(@Nullable String sourcePortSpecifier) {
    _sourcePortSpecifier = sourcePortSpecifier;
  }

  public @Nullable String getDestinationPortSpecifier() {
    return _destinationPortSpecifier;
  }

  public void setDestinationPortSpecifier(@Nullable String destinationPortSpecifier) {
    _destinationPortSpecifier = destinationPortSpecifier;
  }

  public @Nullable String getInterfaceName() {
    return _interfaceName;
  }

  public void setInterfaceName(@Nullable String interfaceName) {
    _interfaceName = interfaceName;
  }

  public @Nullable String getRemark() {
    return _remark;
  }

  public @Nullable Long getRuleId() {
    return _ruleId;
  }

  public void setRuleId(@Nullable Long ruleId) {
    _ruleId = ruleId;
  }

  public boolean isTrust() {
    return _trust;
  }

  public void setTrust(boolean trust) {
    _trust = trust;
  }

  public boolean isInactive() {
    return _inactive;
  }

  public void setInactive(boolean inactive) {
    _inactive = inactive;
  }

  public boolean isLog() {
    return _log;
  }

  public void setLog(boolean log) {
    _log = log;
  }

  public @Nullable String getTimeRange() {
    return _timeRange;
  }

  public void setTimeRange(@Nullable String timeRange) {
    _timeRange = timeRange;
  }

  @Override
  public String toString() {
    if (_aclType == AclType.REMARK) {
      return "remark " + (_remark != null ? _remark : "");
    }
    return String.format(
        "%s %s %s -> %s",
        _action,
        _protocol != null ? _protocol : "ip",
        _sourceAddressSpecifier != null ? _sourceAddressSpecifier : "any",
        _destinationAddressSpecifier != null ? _destinationAddressSpecifier : "any");
  }
}
