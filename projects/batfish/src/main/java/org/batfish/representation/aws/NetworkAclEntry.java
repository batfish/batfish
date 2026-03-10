package org.batfish.representation.aws;

import static com.google.common.base.Preconditions.checkArgument;
import static org.batfish.representation.aws.AwsVpcEntity.JSON_KEY_CIDR_BLOCK;
import static org.batfish.representation.aws.AwsVpcEntity.JSON_KEY_EGRESS;
import static org.batfish.representation.aws.AwsVpcEntity.JSON_KEY_ICMP_TYPE_CODE;
import static org.batfish.representation.aws.AwsVpcEntity.JSON_KEY_IPV6_CIDR_BLOCK;
import static org.batfish.representation.aws.AwsVpcEntity.JSON_KEY_PORT_RANGE;
import static org.batfish.representation.aws.AwsVpcEntity.JSON_KEY_PROTOCOL;
import static org.batfish.representation.aws.AwsVpcEntity.JSON_KEY_RULE_ACTION;
import static org.batfish.representation.aws.AwsVpcEntity.JSON_KEY_RULE_NUMBER;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import java.io.Serializable;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.Prefix6;

/** Represents a network ACL entry for AWS */
@ParametersAreNonnullByDefault
abstract class NetworkAclEntry implements Serializable {

  protected final boolean _isAllow;

  protected final boolean _isEgress;

  protected final @Nonnull String _protocol;

  protected final int _ruleNumber;

  protected final @Nullable IcmpTypeCode _icmpTypeCode;

  protected final @Nullable PortRange _portRange;

  @JsonCreator
  private static NetworkAclEntry create(
      @JsonProperty(JSON_KEY_CIDR_BLOCK) @Nullable Prefix prefix,
      @JsonProperty(JSON_KEY_IPV6_CIDR_BLOCK) @Nullable Prefix6 prefix6,
      @JsonProperty(JSON_KEY_RULE_ACTION) @Nullable String ruleAction,
      @JsonProperty(JSON_KEY_EGRESS) @Nullable Boolean egress,
      @JsonProperty(JSON_KEY_PROTOCOL) @Nullable String protocol,
      @JsonProperty(JSON_KEY_RULE_NUMBER) @Nullable Integer ruleNumber,
      @JsonProperty(JSON_KEY_ICMP_TYPE_CODE) @Nullable IcmpTypeCode icmpTypeCode,
      @JsonProperty(JSON_KEY_PORT_RANGE) @Nullable PortRange portRange) {
    checkArgument(
        prefix != null || prefix6 != null,
        "At least one of v4 or v6 cidr block must be present for network acl entry");
    checkArgument(
        prefix == null || prefix6 == null,
        "At most one of v4 or v6 cidr block must be present for network acl entry");
    checkArgument(ruleAction != null, "Rule action cannot be null for network acl entry");
    checkArgument(egress != null, "Egress cannot be null for network acl entry");
    checkArgument(protocol != null, "Protocol cannot be null for network acl entry");
    checkArgument(ruleNumber != null, "Rule number cannot be null for network acl entry");
    checkArgument(
        !protocol.equals("1") || icmpTypeCode != null,
        "IcmpTypeCode must not be null when protocol is ICMP (1)");

    if (prefix != null) {
      return new NetworkAclEntryV4(
          prefix,
          ruleAction.equals("allow"),
          egress,
          protocol,
          ruleNumber,
          icmpTypeCode,
          portRange);
    } else {
      return new NetworkAclEntryV6(
          prefix6,
          ruleAction.equals("allow"),
          egress,
          protocol,
          ruleNumber,
          icmpTypeCode,
          portRange);
    }
  }

  protected NetworkAclEntry(
      boolean isAllow,
      boolean isEgress,
      String protocol,
      int ruleNumber,
      @Nullable IcmpTypeCode icmpTypeCode,
      @Nullable PortRange portRange) {
    _isAllow = isAllow;
    _isEgress = isEgress;
    _protocol = protocol;
    _ruleNumber = ruleNumber;
    _icmpTypeCode = icmpTypeCode;
    _portRange = portRange;
  }

  boolean getIsAllow() {
    return _isAllow;
  }

  boolean getIsEgress() {
    return _isEgress;
  }

  public @Nullable IcmpTypeCode getIcmpTypeCode() {
    return _icmpTypeCode;
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
  public String toString() {
    return MoreObjects.toStringHelper(getClass())
        .omitNullValues()
        .add("isAllow", _isAllow)
        .add("isEgress", _isEgress)
        .add("protocol", _protocol)
        .add("ruleNumber", _ruleNumber)
        .add("portRange", _portRange)
        .toString();
  }
}
