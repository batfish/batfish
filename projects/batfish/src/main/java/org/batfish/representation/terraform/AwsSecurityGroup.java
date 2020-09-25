package org.batfish.representation.terraform;

import static org.batfish.representation.terraform.Constants.JSON_KEY_ARN;
import static org.batfish.representation.terraform.Constants.JSON_KEY_CIDR_BLOCKS;
import static org.batfish.representation.terraform.Constants.JSON_KEY_DESCRIPTION;
import static org.batfish.representation.terraform.Constants.JSON_KEY_EGRESS;
import static org.batfish.representation.terraform.Constants.JSON_KEY_FROM_PORT;
import static org.batfish.representation.terraform.Constants.JSON_KEY_ID;
import static org.batfish.representation.terraform.Constants.JSON_KEY_INGRESS;
import static org.batfish.representation.terraform.Constants.JSON_KEY_IPV6_CIDR_BLOCKS;
import static org.batfish.representation.terraform.Constants.JSON_KEY_NAME;
import static org.batfish.representation.terraform.Constants.JSON_KEY_PREFIX_LIST_IDS;
import static org.batfish.representation.terraform.Constants.JSON_KEY_PROTOCOL;
import static org.batfish.representation.terraform.Constants.JSON_KEY_SECURITY_GROUPS;
import static org.batfish.representation.terraform.Constants.JSON_KEY_SELF;
import static org.batfish.representation.terraform.Constants.JSON_KEY_TO_PORT;
import static org.batfish.representation.terraform.Constants.JSON_KEY_VPC_ID;
import static org.batfish.representation.terraform.Utils.getAwsResourceTags;
import static org.batfish.representation.terraform.Utils.getStrings;

import com.google.common.collect.ImmutableList;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.Prefix6;
import org.batfish.representation.aws.IpPermissions;
import org.batfish.representation.aws.IpPermissions.IpRange;
import org.batfish.representation.aws.IpPermissions.UserIdGroupPair;
import org.batfish.representation.aws.SecurityGroup;

/** Represents an AWS VPC */
@ParametersAreNonnullByDefault
class AwsSecurityGroup extends AwsResource {

  private static class Rule implements Serializable {

    private static final List<String> MANDATORY_ATTRIBUTES =
        ImmutableList.of(
            JSON_KEY_CIDR_BLOCKS,
            JSON_KEY_DESCRIPTION,
            JSON_KEY_FROM_PORT,
            JSON_KEY_IPV6_CIDR_BLOCKS,
            JSON_KEY_PREFIX_LIST_IDS,
            JSON_KEY_PROTOCOL,
            JSON_KEY_SECURITY_GROUPS,
            JSON_KEY_SELF,
            JSON_KEY_TO_PORT);

    private final List<Prefix> _cidrBlocks;
    private final String _description;
    private final int _fromPort;
    private final List<Prefix6> _ipv6CidrBlocks;
    private final List<String> _prefixListIds;
    private final String _protocol;
    private final List<String> _securityGroups;
    private final boolean _self;
    private final int _toPort;

    static Rule create(Map<String, Object> ruleAttributes, String description) {
      for (String key : MANDATORY_ATTRIBUTES) {
        if (!ruleAttributes.containsKey(key)) {
          throw new IllegalArgumentException(
              String.format("Attribute '%s' not found for %s", key, description));
        }
      }
      return new Rule(
          getStrings(ruleAttributes, JSON_KEY_CIDR_BLOCKS).stream()
              .map(Prefix::parse)
              .collect(ImmutableList.toImmutableList()),
          ruleAttributes.get(JSON_KEY_DESCRIPTION).toString(),
          Integer.parseInt(ruleAttributes.get(JSON_KEY_FROM_PORT).toString()),
          getStrings(ruleAttributes, JSON_KEY_IPV6_CIDR_BLOCKS).stream()
              .map(Prefix6::parse)
              .collect(ImmutableList.toImmutableList()),
          getStrings(ruleAttributes, JSON_KEY_PREFIX_LIST_IDS),
          ruleAttributes.get(JSON_KEY_PROTOCOL).toString(),
          getStrings(ruleAttributes, JSON_KEY_SECURITY_GROUPS),
          Boolean.parseBoolean(ruleAttributes.get(JSON_KEY_SELF).toString()),
          Integer.parseInt(ruleAttributes.get(JSON_KEY_TO_PORT).toString()));
    }

    Rule(
        List<Prefix> cidrBlocks,
        String description,
        int fromPort,
        List<Prefix6> ipv6CidrBlocks,
        List<String> prefixListIds,
        String protocol,
        List<String> securityGroups,
        boolean self,
        int toPort) {
      _cidrBlocks = cidrBlocks;
      _description = description;
      _fromPort = fromPort;
      _ipv6CidrBlocks = ipv6CidrBlocks;
      _prefixListIds = prefixListIds;
      _protocol = protocol;
      _securityGroups = securityGroups;
      _self = self;
      _toPort = toPort;
    }

    IpPermissions convert(String selfSecurityGroupId) {

      return new IpPermissions(
          _protocol,
          _fromPort == 0 ? null : _fromPort,
          _toPort == 0 ? null : _toPort,
          _cidrBlocks.stream()
              .map(prefix -> new IpRange(_description, prefix))
              .collect(ImmutableList.toImmutableList()),
          _prefixListIds,
          (_self
                  ? ImmutableList.<String>builder()
                      .addAll(_securityGroups)
                      .add(selfSecurityGroupId)
                      .build()
                  : _securityGroups)
              .stream()
                  .map(sgid -> new UserIdGroupPair(sgid, null))
                  .collect(ImmutableList.toImmutableList()));
    }
  }

  private static final List<String> MANDATORY_ATTRIBUTES =
      ImmutableList.of(
          JSON_KEY_ARN,
          JSON_KEY_ID,
          JSON_KEY_NAME,
          JSON_KEY_DESCRIPTION,
          JSON_KEY_VPC_ID,
          JSON_KEY_INGRESS,
          JSON_KEY_EGRESS);

  @Nonnull private final String _arn;
  @Nonnull private final String _id;
  @Nonnull private final String _name;
  @Nonnull private final String _description;
  @Nonnull private final String _vpcId;
  @Nonnull private final List<Rule> _ingressRules;
  @Nonnull private final List<Rule> _egressRules;
  @Nonnull private final Map<String, String> _tags;

  static AwsSecurityGroup create(CommonResourceProperties common, Map<String, Object> attributes) {
    for (String key : MANDATORY_ATTRIBUTES) {
      if (!attributes.containsKey(key)) {
        throw new IllegalArgumentException(
            String.format(
                "Attribute '%s' not found for AWS Network Acl %s", key, common.getName()));
      }
    }

    return new AwsSecurityGroup(
        common,
        attributes.get(JSON_KEY_ARN).toString(),
        attributes.get(JSON_KEY_ID).toString(),
        attributes.get(JSON_KEY_NAME).toString(),
        attributes.get(JSON_KEY_DESCRIPTION).toString(),
        attributes.get(JSON_KEY_VPC_ID).toString(),
        getRules(attributes.get(JSON_KEY_INGRESS), "ingress rules for " + common.getName()),
        getRules(attributes.get(JSON_KEY_EGRESS), "egress rules for " + common.getName()),
        getAwsResourceTags(attributes));
  }

  @SuppressWarnings("unchecked")
  private static List<Rule> getRules(Object rules, String description) {
    return ((List<?>) rules)
        .stream()
            .map(m -> Rule.create((Map<String, Object>) m, description))
            .collect(ImmutableList.toImmutableList());
  }

  public AwsSecurityGroup(
      CommonResourceProperties common,
      String arn,
      String id,
      String name,
      String description,
      String vpcId,
      List<Rule> ingressRules,
      List<Rule> egressRules,
      Map<String, String> tags) {
    super(common);
    _arn = arn;
    _id = id;
    _name = name;
    _description = description;
    _vpcId = vpcId;
    _ingressRules = ingressRules;
    _egressRules = egressRules;
    _tags = tags;
  }

  public SecurityGroup convert() {
    return new SecurityGroup(
        _description,
        _id,
        _name,
        _egressRules.stream().map(r -> r.convert(_id)).collect(ImmutableList.toImmutableList()),
        _ingressRules.stream().map(r -> r.convert(_id)).collect(ImmutableList.toImmutableList()),
        _tags,
        _vpcId);
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
  public String getName() {
    return _name;
  }

  @Nonnull
  public String getDescription() {
    return _description;
  }

  @Nonnull
  public String getVpcId() {
    return _vpcId;
  }
}
