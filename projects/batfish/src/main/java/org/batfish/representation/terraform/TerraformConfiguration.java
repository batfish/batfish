package org.batfish.representation.terraform;

import static org.batfish.representation.terraform.Constants.JSON_KEY_TERRAFORM_VERSION;
import static org.batfish.representation.terraform.Utils.getArnAcccount;
import static org.batfish.representation.terraform.Utils.getArnRegion;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.common.VendorConversionException;
import org.batfish.common.Warnings;
import org.batfish.common.topology.Layer1Edge;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.isp_configuration.IspConfiguration;
import org.batfish.representation.aws.Account;
import org.batfish.representation.aws.AwsConfiguration;
import org.batfish.representation.aws.Instance;
import org.batfish.representation.aws.InternetGateway;
import org.batfish.representation.aws.NetworkAcl;
import org.batfish.representation.aws.NetworkInterface;
import org.batfish.representation.aws.Region;
import org.batfish.representation.aws.RouteTable;
import org.batfish.representation.aws.SecurityGroup;
import org.batfish.representation.aws.Subnet;
import org.batfish.representation.aws.Vpc;
import org.batfish.representation.terraform.CommonResourceProperties.Mode;
import org.batfish.vendor.VendorConfiguration;

/** Represents configuration information in Terraform state or plan files */
public class TerraformConfiguration extends VendorConfiguration {

  public static TerraformConfiguration fromJson(String filename, String fileText, Warnings warnings)
      throws JsonProcessingException {
    JsonNode node = BatfishObjectMapper.mapper().readValue(fileText, JsonNode.class);
    if (!node.has(JSON_KEY_TERRAFORM_VERSION)) {
      throw new IllegalArgumentException(String.format("%s is not Terraform file", filename));
    }
    if (node.has("serial") && node.has("lineage")) {
      // it is a state file
      return new TerraformConfiguration(
          filename, BatfishObjectMapper.mapper().readValue(fileText, TerraformState.class));
    } else if (node.has("resource_changes") && node.has("configuration")) {
      // it is a plan file
      return new TerraformConfiguration(
          filename, BatfishObjectMapper.mapper().readValue(fileText, TerraformPlan.class));
    } else {
      throw new IllegalArgumentException(
          String.format("Terraform file %s is neither a state file nor a plan file", filename));
    }
  }

  @Nonnull private final TerraformFileContent _fileContent;

  @Nullable private ConvertedConfiguration _convertedConfiguration;

  TerraformConfiguration(String filename, TerraformFileContent fileContent) {
    _filename = filename;
    _fileContent = fileContent;
  }

  @Override
  public String getHostname() {
    // This is an abuse of the interface but we don't have a representative hostname to offer
    return new File(_filename).getName();
  }

  @Override
  public void setHostname(String hostname) {
    throw new IllegalStateException("Setting the hostname is not allowed for terraform configs");
  }

  @Override
  public void setVendor(ConfigurationFormat format) {
    throw new IllegalStateException("Setting the format is not allowed for AWS configs");
  }

  @Override
  public List<Configuration> toVendorIndependentConfigurations() throws VendorConversionException {
    if (_convertedConfiguration == null) {
      convertConfiguration();
    }
    return _convertedConfiguration.toVendorIndependentConfiguration();
  }

  @Nonnull
  @Override
  public Set<Layer1Edge> getLayer1Edges() {
    if (_convertedConfiguration == null) {
      convertConfiguration();
    }
    return _convertedConfiguration.getLayer1Edges();
  }

  @Nonnull
  @Override
  public IspConfiguration getIspConfiguration() {
    if (_convertedConfiguration == null) {
      convertConfiguration();
    }
    return _convertedConfiguration.getIspConfiguration();
  }

  private void convertConfiguration() {
    _convertedConfiguration = new ConvertedConfiguration();
    Warnings warnings = getWarnings();
    List<TerraformResource> tfResources = _fileContent.toConvertedResources(warnings);

    // log a warning about unsupported resources
    Set<String> unknownResourceTypes =
        tfResources.stream()
            .filter(r -> r instanceof StateResource && r.getCommon().getMode() != Mode.DATA)
            .map(r -> r.getCommon().getType())
            .collect(ImmutableSet.toImmutableSet());
    if (!unknownResourceTypes.isEmpty()) {
      warnings.redFlag(
          String.format(
              "Following types of resources are not currently supported: %s",
              unknownResourceTypes));
    }

    // convert aws resources
    List<AwsResource> awsResources =
        tfResources.stream()
            .filter(r -> r instanceof AwsResource)
            .map(r -> (AwsResource) r)
            .collect(ImmutableList.toImmutableList());
    _convertedConfiguration.setAwsConfiguration(toAwsConfiguration(awsResources, warnings));
  }

  private static AwsConfiguration toAwsConfiguration(
      List<AwsResource> awsResources, Warnings warnings) {
    // accountId --> regionName --> region data
    Map<String, Map<String, AwsRegionBuilder>> regions = new HashMap<>();

    for (AwsResource resource : awsResources) {
      if (resource instanceof AwsRouteTableAssociation) {
        // resources that do not need conversion
        continue;
      }
      if (resource instanceof AwsInternetGateway) {
        AwsInternetGateway awsIgw = (AwsInternetGateway) resource;
        getOrCreateAwsConfigCollector(
                regions, getArnAcccount(awsIgw.getArn()), getArnRegion(awsIgw.getArn()))
            .addInternetGateway(awsIgw.convert());
        continue;
      }
      if (resource instanceof AwsVpc) {
        AwsVpc awsVpc = (AwsVpc) resource;
        getOrCreateAwsConfigCollector(
                regions, getArnAcccount(awsVpc.getArn()), getArnRegion(awsVpc.getArn()))
            .addVpc(awsVpc.convert());
        continue;
      }
      if (resource instanceof AwsSubnet) {
        AwsSubnet awsSubnet = (AwsSubnet) resource;
        getOrCreateAwsConfigCollector(
                regions, getArnAcccount(awsSubnet.getArn()), getArnRegion(awsSubnet.getArn()))
            .addSubnet(awsSubnet.convert());
        continue;
      }
      if (resource instanceof AwsRouteTable) {
        AwsRouteTable awsRouteTable = (AwsRouteTable) resource;
        Optional<AwsVpc> awsVpc =
            findAwsVpc(awsRouteTable.getVpcId(), awsRouteTable.getOwnerId(), awsResources);
        if (!awsVpc.isPresent()) {
          warnings.redFlag(
              String.format(
                  "VPC %s not found; needed for route table %s",
                  awsRouteTable.getId(), awsRouteTable.getCommon().getName()));
          continue;
        }
        getOrCreateAwsConfigCollector(
                regions, getArnAcccount(awsVpc.get().getArn()), getArnRegion(awsVpc.get().getArn()))
            .addRouteTable(
                awsRouteTable.convert(awsRouteTable.findSubnetAssociations(awsResources)));
        continue;
      }

      if (resource instanceof AwsNetworkAcl) {
        AwsNetworkAcl awsNacl = (AwsNetworkAcl) resource;
        getOrCreateAwsConfigCollector(
                regions, getArnAcccount(awsNacl.getArn()), getArnRegion(awsNacl.getArn()))
            .addNetworkAcl(awsNacl.convert());
        continue;
      }
      if (resource instanceof AwsSecurityGroup) {
        AwsSecurityGroup awsSecurityGroup = (AwsSecurityGroup) resource;
        getOrCreateAwsConfigCollector(
                regions,
                getArnAcccount(awsSecurityGroup.getArn()),
                getArnRegion(awsSecurityGroup.getArn()))
            .addSecurityGroup(awsSecurityGroup.convert());
        continue;
      }
      if (resource instanceof AwsInstance) {
        AwsInstance awsInstance = (AwsInstance) resource;
        String account = getArnAcccount(awsInstance.getArn());
        String region = getArnRegion(awsInstance.getArn());
        Optional<AwsSubnet> awsSubnet =
            awsResources.stream()
                .filter(r -> r instanceof AwsSubnet)
                .map(r -> (AwsSubnet) r)
                .filter(awsInstance::isMySubnet)
                .findFirst();
        if (!awsSubnet.isPresent()) {
          warnings.redFlag(
              String.format("Subnet resource not found for AWS instance %s", awsInstance.getArn()));
        }
        @Nullable String vpcId = awsSubnet.map(AwsSubnet::getVpcId).orElse(null);
        AwsRegionBuilder awsRegionBuilder = getOrCreateAwsConfigCollector(regions, account, region);
        awsRegionBuilder.addInstance(awsInstance.convert(vpcId));
        if (vpcId != null) {
          Optional<NetworkInterface> networkInterface =
              awsInstance.getImplicitNetworkInterface(vpcId);
          networkInterface.ifPresent(ni -> awsRegionBuilder.addNetworkInterface(ni));
        }

        continue;
      }
      warnings.redFlag(
          String.format(
              "Did not convert resource of type %s from Terraform to AWS",
              resource.getClass().getName()));
    }

    Map<String, Account> accounts = new HashMap<>();
    for (String accountId : regions.keySet()) {
      Account account = new Account(accountId);
      regions
          .get(accountId)
          .forEach(
              (regionName, awsRegionConfigCollector) -> {
                account.addRegion(awsRegionConfigCollector.build(regionName));
              });
      accounts.put(accountId, account);
    }
    AwsConfiguration awsConfiguration = new AwsConfiguration(accounts);
    awsConfiguration.setWarnings(warnings);
    return awsConfiguration;
  }

  private static Optional<AwsVpc> findAwsVpc(
      String vpcId, String ownerId, List<AwsResource> awsResources) {
    return awsResources.stream()
        .filter(r -> r instanceof AwsVpc)
        .map(r -> (AwsVpc) r)
        .filter(vpc -> getArnAcccount(vpc.getArn()).equals(ownerId) && vpc.getId().equals(vpcId))
        .findFirst();
  }

  private static AwsRegionBuilder getOrCreateAwsConfigCollector(
      Map<String, Map<String, AwsRegionBuilder>> resources, String accountId, String regionName) {
    return resources
        .computeIfAbsent(accountId, k -> new HashMap<>())
        .computeIfAbsent(regionName, k -> new AwsRegionBuilder());
  }

  // TODO: Move these add APIs to Region.Builder
  private static class AwsRegionBuilder {
    Map<String, Vpc> vpcs = new HashMap<>();
    Map<String, Subnet> subnets = new HashMap<>();
    Map<String, RouteTable> routeTables = new HashMap<>();
    Map<String, NetworkAcl> networkAcls = new HashMap<>();
    Map<String, NetworkInterface> networkInterfaces = new HashMap<>();
    Map<String, SecurityGroup> securityGroups = new HashMap<>();
    Map<String, Instance> instances = new HashMap<>();
    Map<String, InternetGateway> internetGateways = new HashMap<>();

    public Region build(String regionName) {
      return Region.builder(regionName)
          .setVpcs(vpcs)
          .setSubnets(subnets)
          .setRouteTables(routeTables)
          .setNetworkAcls(networkAcls)
          .setNetworkInterfaces(networkInterfaces)
          .setSecurityGroups(securityGroups)
          .setInstances(instances)
          .setInternetGateways(internetGateways)
          .build();
    }

    void addVpc(Vpc vpc) {
      vpcs.put(vpc.getId(), vpc);
    }

    void addSubnet(Subnet subnet) {
      subnets.put(subnet.getId(), subnet);
    }

    void addRouteTable(RouteTable routeTable) {
      routeTables.put(routeTable.getId(), routeTable);
    }

    void addNetworkAcl(NetworkAcl nacl) {
      networkAcls.put(nacl.getId(), nacl);
    }

    void addNetworkInterface(NetworkInterface networkInterface) {
      networkInterfaces.put(networkInterface.getId(), networkInterface);
    }

    void addSecurityGroup(SecurityGroup securityGroup) {
      securityGroups.put(securityGroup.getId(), securityGroup);
    }

    void addInstance(Instance instance) {
      instances.put(instance.getId(), instance);
    }

    void addInternetGateway(InternetGateway internetGateway) {
      internetGateways.put(internetGateway.getId(), internetGateway);
    }
  }
}
