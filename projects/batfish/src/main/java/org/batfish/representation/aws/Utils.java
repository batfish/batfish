package org.batfish.representation.aws;

import java.util.List;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.BatfishException;
import org.batfish.common.Warnings;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.vendor_family.AwsFamily;

/** A collection for utilities for AWS vendor model */
@ParametersAreNonnullByDefault
final class Utils {

  private static final NetworkFactory FACTORY = new NetworkFactory();

  static void checkNonNull(@Nullable Object value, String fieldName, String objectType) {
    if (value == null) {
      throw new IllegalArgumentException(
          String.format("Field '%s' must exist for '%s", fieldName, objectType));
    }
  }

  static Configuration newAwsConfiguration(String name, String domainName) {
    Configuration c =
        FACTORY
            .configurationBuilder()
            .setHostname(name)
            .setDomainName(domainName)
            .setConfigurationFormat(ConfigurationFormat.AWS)
            .setDefaultInboundAction(LineAction.PERMIT)
            .setDefaultCrossZoneAction(LineAction.PERMIT)
            .build();
    FACTORY.vrfBuilder().setName(Configuration.DEFAULT_VRF_NAME).setOwner(c).build();
    c.getVendorFamily().setAws(new AwsFamily());
    return c;
  }

  static Interface newInterface(
      String name, Configuration c, ConcreteInterfaceAddress primaryAddress, String description) {
    return FACTORY
        .interfaceBuilder()
        .setName(name)
        .setOwner(c)
        .setVrf(c.getDefaultVrf())
        .setAddress(primaryAddress)
        .setDescription(description)
        .build();
  }

  /**
   * Updates {@link Region}'s mapping between {@link Configuration} names and {@link SecurityGroup}
   * for a given configuration. Also updates {@link org.batfish.datamodel.Ip} of instances in {@link
   * SecurityGroup}
   *
   * @param region {@link Region} in which the configuration is in
   * @param configuration {@link Configuration} for which security groups are to be processed
   * @param securityGroupsIds {@link List} of security group IDs
   * @param warnings {@link Warnings} for the configuration
   */
  static void processSecurityGroups(
      Region region,
      Configuration configuration,
      List<String> securityGroupsIds,
      Warnings warnings) {
    for (String sGroupId : securityGroupsIds) {
      SecurityGroup securityGroup = region.getSecurityGroups().get(sGroupId);
      if (securityGroup == null) {
        warnings.pedantic(
            String.format(
                "Security group \"%s\" for \"%s\" not found",
                sGroupId, configuration.getHostname()));
        continue;
      }
      region.updateConfigurationSecurityGroups(configuration.getHostname(), securityGroup);

      securityGroup.updateConfigIps(configuration);
    }
  }

  @Nullable
  static IpProtocol toIpProtocol(String ipProtocolAsString) {
    switch (ipProtocolAsString) {
      case "tcp":
        return IpProtocol.TCP;
      case "udp":
        return IpProtocol.UDP;
      case "icmp":
        return IpProtocol.ICMP;
      case "-1":
        return null;
      default:
        try {
          int ipProtocolAsInt = Integer.parseInt(ipProtocolAsString);
          return IpProtocol.fromNumber(ipProtocolAsInt);
        } catch (NumberFormatException e) {
          throw new BatfishException(
              "Could not convert AWS IP protocol: \""
                  + ipProtocolAsString
                  + "\" to batfish Ip Protocol",
              e);
        }
    }
  }

  private Utils() {}
}
