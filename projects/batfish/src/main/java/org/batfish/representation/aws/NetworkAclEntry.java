package org.batfish.representation.aws;

import static com.google.common.base.Preconditions.checkArgument;
import static org.batfish.representation.aws.AwsVpcEntity.JSON_KEY_CIDR_BLOCK;
import static org.batfish.representation.aws.AwsVpcEntity.JSON_KEY_EGRESS;
import static org.batfish.representation.aws.AwsVpcEntity.JSON_KEY_PORT_RANGE;
import static org.batfish.representation.aws.AwsVpcEntity.JSON_KEY_PROTOCOL;
import static org.batfish.representation.aws.AwsVpcEntity.JSON_KEY_RULE_ACTION;
import static org.batfish.representation.aws.AwsVpcEntity.JSON_KEY_RULE_NUMBER;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import java.io.Serializable;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.Prefix;

/** Represents a network ACL entry for AWS */
@ParametersAreNonnullByDefault
final class NetworkAclEntry implements Serializable {

  @Nonnull private final Prefix _cidrBlock;

  private final boolean _isAllow;

  private final boolean _isEgress;

  @Nonnull private final String _protocol;

  private final int _ruleNumber;

  @Nullable private final PortRange _portRange;

  @JsonCreator
  private static NetworkAclEntry create(
      @Nullable @JsonProperty(JSON_KEY_CIDR_BLOCK) Prefix prefix,
      @Nullable @JsonProperty(JSON_KEY_RULE_ACTION) String ruleAction,
      @Nullable @JsonProperty(JSON_KEY_EGRESS) Boolean egress,
      @Nullable @JsonProperty(JSON_KEY_PROTOCOL) String protocol,
      @Nullable @JsonProperty(JSON_KEY_RULE_NUMBER) Integer ruleNumber,
      @Nullable @JsonProperty(JSON_KEY_PORT_RANGE) PortRange portRange) {
    checkArgument(prefix != null, "Prefix cannot be null for network acl entry");
    checkArgument(ruleAction != null, "Rule action cannot be null for network acl entry");
    checkArgument(egress != null, "Egress cannot be null for network acl entry");
    checkArgument(protocol != null, "Protocol cannot be null for network acl entry");
    checkArgument(ruleNumber != null, "Rule number cannot be null for network acl entry");

    return new NetworkAclEntry(
        prefix, ruleAction.equals("allow"), egress, protocol, ruleNumber, portRange);
  }

  NetworkAclEntry(
      Prefix prefix,
      boolean isAllow,
      boolean isEgress,
      String protocol,
      int ruleNumber,
      @Nullable PortRange portRange) {
    _cidrBlock = prefix;
    _isAllow = isAllow;
    _isEgress = isEgress;
    _protocol = protocol;
    _ruleNumber = ruleNumber;
    _portRange = portRange;
  }

  @Nonnull
  Prefix getCidrBlock() {
    return _cidrBlock;
  }

  boolean getIsAllow() {
    return _isAllow;
  }

  boolean getIsEgress() {
    return _isEgress;
  }

  @Nullable
  PortRange getPortRange() {
    return _portRange;
  }

  @Nonnull
  String getProtocol() {
    return _protocol;
  }

  int getRuleNumber() {
    return _ruleNumber;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof NetworkAclEntry)) {
      return false;
    }
    NetworkAclEntry that = (NetworkAclEntry) o;
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
        .omitNullValues()
        .add("cidrBlock", _cidrBlock)
        .add("isAllow", _isAllow)
        .add("isEgress", _isEgress)
        .add("protocol", _protocol)
        .add("ruleNumber", _ruleNumber)
        .add("portRange", _portRange)
        .toString();
  }
}
