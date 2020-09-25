package org.batfish.representation.terraform;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkArgument;
import static org.batfish.representation.terraform.Constants.JSON_KEY_ACTION;
import static org.batfish.representation.terraform.Constants.JSON_KEY_ARN;
import static org.batfish.representation.terraform.Constants.JSON_KEY_CIDR_BLOCK;
import static org.batfish.representation.terraform.Constants.JSON_KEY_EGRESS;
import static org.batfish.representation.terraform.Constants.JSON_KEY_FROM_PORT;
import static org.batfish.representation.terraform.Constants.JSON_KEY_ICMP_CODE;
import static org.batfish.representation.terraform.Constants.JSON_KEY_ICMP_TYPE;
import static org.batfish.representation.terraform.Constants.JSON_KEY_ID;
import static org.batfish.representation.terraform.Constants.JSON_KEY_INGRESS;
import static org.batfish.representation.terraform.Constants.JSON_KEY_IPV6_CIDR_BLOCK;
import static org.batfish.representation.terraform.Constants.JSON_KEY_PROTOCOL;
import static org.batfish.representation.terraform.Constants.JSON_KEY_RULE_NO;
import static org.batfish.representation.terraform.Constants.JSON_KEY_SUBNET_IDS;
import static org.batfish.representation.terraform.Constants.JSON_KEY_TO_PORT;
import static org.batfish.representation.terraform.Constants.JSON_KEY_VPC_ID;
import static org.batfish.representation.terraform.Utils.checkMandatoryAttributes;
import static org.batfish.representation.terraform.Utils.getStrings;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Streams;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.Prefix6;
import org.batfish.representation.aws.IcmpTypeCode;
import org.batfish.representation.aws.NetworkAcl;
import org.batfish.representation.aws.NetworkAcl.NetworkAclAssociation;
import org.batfish.representation.aws.NetworkAclEntry;
import org.batfish.representation.aws.NetworkAclEntryV4;
import org.batfish.representation.aws.NetworkAclEntryV6;
import org.batfish.representation.aws.PortRange;

/** Represents an AWS VPC */
@ParametersAreNonnullByDefault
class AwsNetworkAcl extends AwsResource {

  private static class Rule implements Serializable {

    public enum Action {
      ALLOW,
      DENY
    }

    private static final List<String> MANDATORY_ATTRIBUTES =
        ImmutableList.of(
            JSON_KEY_ACTION,
            JSON_KEY_CIDR_BLOCK,
            JSON_KEY_FROM_PORT,
            JSON_KEY_ICMP_CODE,
            JSON_KEY_ICMP_TYPE,
            JSON_KEY_IPV6_CIDR_BLOCK,
            JSON_KEY_PROTOCOL,
            JSON_KEY_RULE_NO,
            JSON_KEY_TO_PORT);

    @Nonnull private final Action _action;
    @Nullable private final Prefix _cidrBlock;
    private final int _fromPort;
    private final int _icmpCode;
    private final int _icmpType;
    @Nullable private final Prefix6 _ipv6CidrBlock;
    @Nonnull private final String _protocol;
    private final int _ruleNo;
    private final int _toPort;

    static Rule create(Map<String, Object> attributes, String resourceDescription) {
      checkMandatoryAttributes(attributes, MANDATORY_ATTRIBUTES, resourceDescription);
      // these are empty strings when unset
      String cidrBlock = attributes.get(JSON_KEY_CIDR_BLOCK).toString();
      String ipv6CidrBlock = attributes.get(JSON_KEY_IPV6_CIDR_BLOCK).toString();

      return new Rule(
          Action.valueOf(attributes.get(JSON_KEY_ACTION).toString().toUpperCase()),
          cidrBlock.equals("") ? null : Prefix.parse(cidrBlock),
          Integer.parseInt(attributes.get(JSON_KEY_FROM_PORT).toString()),
          Integer.parseInt(firstNonNull(attributes.get(JSON_KEY_ICMP_CODE), 0).toString()),
          Integer.parseInt(firstNonNull(attributes.get(JSON_KEY_ICMP_TYPE), 0).toString()),
          ipv6CidrBlock.equals("") ? null : Prefix6.parse(ipv6CidrBlock),
          attributes.get(JSON_KEY_PROTOCOL).toString(),
          Integer.parseInt(attributes.get(JSON_KEY_RULE_NO).toString()),
          Integer.parseInt(attributes.get(JSON_KEY_TO_PORT).toString()));
    }

    Rule(
        Action action,
        @Nullable Prefix cidrBlock,
        int fromPort,
        int icmpCode,
        int icmpType,
        @Nullable Prefix6 ipv6CidrBlock,
        String protocol,
        int ruleNo,
        int toPort) {
      checkArgument(
          cidrBlock != null || ipv6CidrBlock != null,
          "At least one of v4 and v6 cidr block must not be null");
      checkArgument(
          cidrBlock == null || ipv6CidrBlock == null,
          "At least one of v4 and v6 cidr block must be null");
      _action = action;
      _cidrBlock = cidrBlock;
      _fromPort = fromPort;
      _icmpCode = icmpCode;
      _icmpType = icmpType;
      _ipv6CidrBlock = ipv6CidrBlock;
      _protocol = protocol;
      _ruleNo = ruleNo;
      _toPort = toPort;
    }

    NetworkAclEntry convert(boolean isEgress) {
      if (_cidrBlock != null) {
        return new NetworkAclEntryV4(
            _cidrBlock,
            _action == Action.ALLOW,
            isEgress,
            _protocol,
            _ruleNo,
            _protocol.equals("1") ? new IcmpTypeCode(_icmpType, _icmpCode) : null,
            _protocol.equals("6") || _protocol.equals("17")
                ? new PortRange(_fromPort, _toPort)
                : null);
      }
      return new NetworkAclEntryV6(
          _ipv6CidrBlock,
          _action == Action.ALLOW,
          isEgress,
          _protocol,
          _ruleNo,
          _protocol.equals("1") ? new IcmpTypeCode(_icmpType, _icmpCode) : null,
          _protocol.equals("6") || _protocol.equals("17")
              ? new PortRange(_fromPort, _toPort)
              : null);
    }
  }

  private static final List<String> MANDATORY_ATTRIBUTES =
      ImmutableList.of(
          JSON_KEY_ARN, JSON_KEY_ID, JSON_KEY_VPC_ID, JSON_KEY_INGRESS, JSON_KEY_EGRESS);

  @Nonnull private final String _arn;
  @Nonnull private final String _id;
  @Nonnull private final String _vpcId;
  @Nonnull private final List<String> _subnetIds;
  @Nonnull private final List<Rule> _ingressRules;
  @Nonnull private final List<Rule> _egressRules;

  static AwsNetworkAcl create(CommonResourceProperties common, Map<String, Object> attributes) {
    for (String key : MANDATORY_ATTRIBUTES) {
      if (!attributes.containsKey(key)) {
        throw new IllegalArgumentException(
            String.format(
                "Attribute '%s' not found for AWS Network Acl %s", key, common.getName()));
      }
    }

    return new AwsNetworkAcl(
        common,
        attributes.get(JSON_KEY_ARN).toString(),
        attributes.get(JSON_KEY_ID).toString(),
        attributes.get(JSON_KEY_VPC_ID).toString(),
        getStrings(attributes, JSON_KEY_SUBNET_IDS),
        getRules(attributes.get(JSON_KEY_INGRESS), "ingress rules for " + common.getName()),
        getRules(attributes.get(JSON_KEY_EGRESS), "egress rules for " + common.getName()));
  }

  @SuppressWarnings("unchecked")
  private static List<Rule> getRules(Object rules, String resourceDescription) {
    return ((List<?>) rules)
        .stream()
            .map(m -> Rule.create((Map<String, Object>) m, resourceDescription))
            .collect(ImmutableList.toImmutableList());
  }

  public AwsNetworkAcl(
      CommonResourceProperties common,
      String arn,
      String id,
      String vpcId,
      List<String> subnetIds,
      List<Rule> ingressRules,
      List<Rule> egressRules) {
    super(common);
    _arn = arn;
    _id = id;
    _vpcId = vpcId;
    _subnetIds = subnetIds;
    _ingressRules = ingressRules;
    _egressRules = egressRules;
  }

  public NetworkAcl convert() {
    return new NetworkAcl(
        _id,
        _vpcId,
        _subnetIds.stream()
            .map(NetworkAclAssociation::new)
            .collect(ImmutableList.toImmutableList()),
        Streams.concat(
                _ingressRules.stream().map(r -> r.convert(false)),
                _egressRules.stream().map(r -> r.convert(true)))
            .collect(ImmutableList.toImmutableList()),
        false);
  }

  @Nonnull
  public String getArn() {
    return _arn;
  }

  @Nonnull
  public String getId() {
    return _id;
  }

  @Nonnull
  public String getVpcId() {
    return _vpcId;
  }
}
