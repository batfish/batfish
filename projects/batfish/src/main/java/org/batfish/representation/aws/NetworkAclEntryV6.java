package org.batfish.representation.aws;

import com.google.common.base.MoreObjects;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.Prefix6;

/** Represents an IPv6 network ACL entry in AWS */
final class NetworkAclEntryV6 extends NetworkAclEntry {

  @Nonnull private final Prefix6 _cidrBlock;

  NetworkAclEntryV6(
      Prefix6 prefix,
      boolean isAllow,
      boolean isEgress,
      String protocol,
      int ruleNumber,
      @Nullable IcmpTypeCode icmpTypeCode,
      @Nullable PortRange portRange) {
    super(isAllow, isEgress, protocol, ruleNumber, icmpTypeCode, portRange);
    _cidrBlock = prefix;
  }

  @Nonnull
  Prefix6 getCidrBlock() {
    return _cidrBlock;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof NetworkAclEntryV6)) {
      return false;
    }
    NetworkAclEntryV6 that = (NetworkAclEntryV6) o;
    return _isAllow == that._isAllow
        && _isEgress == that._isEgress
        && _ruleNumber == that._ruleNumber
        && Objects.equals(_cidrBlock, that._cidrBlock)
        && Objects.equals(_protocol, that._protocol)
        && Objects.equals(_portRange, that._portRange);
  }

  @Override
  public int hashCode() {
    return Objects.hash(_cidrBlock, _isAllow, _isEgress, _protocol, _ruleNumber, _portRange);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(getClass())
        .add("cidrBlock", _cidrBlock)
        .add("base", super.toString())
        .toString();
  }
}
