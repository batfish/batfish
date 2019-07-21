package org.batfish.representation.aws;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.BfConsts;
import org.batfish.common.Warning;
import org.batfish.common.Warnings;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.config.Settings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.DeviceType;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpAccessListLine;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.answers.ParseVendorConfigurationAnswerElement;
import org.batfish.main.Batfish;
import org.batfish.representation.aws.Instance.Status;

/** Represents an AWS region */
@ParametersAreNonnullByDefault
public class Region implements Serializable {

  private interface ThrowingConsumer<T, E extends Exception> {
    void accept(T t) throws E, IOException;
  }

  static final String SG_INGRESS_ACL_NAME = "~SECURITY_GROUP_INGRESS_ACL~";

  static final String SG_EGRESS_ACL_NAME = "~SECURITY_GROUP_EGRESS_ACL~";

  @Nonnull private final Map<String, Address> _addresses;

  @Nonnull private final Map<String, Set<SecurityGroup>> _configurationSecurityGroups;

  @Nonnull private final Map<String, CustomerGateway> _customerGateways;

  @Nonnull private final Map<String, ElasticsearchDomain> _elasticsearchDomains;

  @Nonnull private final Map<String, Instance> _instances;

  @Nonnull private final Map<String, InternetGateway> _internetGateways;

  @Nonnull private final String _name;

  @Nonnull private final Map<String, NatGateway> _natGateways;

  @Nonnull private final Map<String, NetworkAcl> _networkAcls;

  @Nonnull private final Map<String, NetworkInterface> _networkInterfaces;

  @Nonnull private final Map<String, RdsInstance> _rdsInstances;

  @Nonnull private final Map<String, RouteTable> _routeTables;

  @Nonnull private final Map<String, SecurityGroup> _securityGroups;

  @Nonnull private final Map<String, Subnet> _subnets;

  @Nonnull private final Map<String, VpcPeeringConnection> _vpcPeerings;

  @Nonnull private final Map<String, Vpc> _vpcs;

  @Nonnull private final Map<String, VpnConnection> _vpnConnections;

  @Nonnull private final Map<String, VpnGateway> _vpnGateways;

  public Region(String name) {
    _name = name;
    _addresses = new HashMap<>();
    _configurationSecurityGroups = new HashMap<>();
    _customerGateways = new HashMap<>();
    _elasticsearchDomains = new HashMap<>();
    _instances = new HashMap<>();
    _internetGateways = new HashMap<>();
    _natGateways = new HashMap<>();
    _networkAcls = new HashMap<>();
    _networkInterfaces = new HashMap<>();
    _rdsInstances = new HashMap<>();
    _routeTables = new HashMap<>();
    _securityGroups = new HashMap<>();
    _subnets = new HashMap<>();
    _vpcPeerings = new HashMap<>();
    _vpcs = new HashMap<>();
    _vpnConnections = new HashMap<>();
    _vpnGateways = new HashMap<>();
  }

  void addConfigElement(
      JsonNode json, String sourceFileName, ParseVendorConfigurationAnswerElement pvcae)
      throws IOException {

    Iterator<?> keys = json.fieldNames();

    while (keys.hasNext()) {
      String key = (String) keys.next();

      if (ignoreElement(key)) {
        continue;
      }

      // All objects nested under the current key will be subjected to this function, which will
      // integrate them appropriately into this Region. Returns null on unrecognized keys.
      ThrowingConsumer<JsonNode, IOException> integratorFunction = getChildConsumer(key);

      if (integratorFunction == null) {
        // Add warning for unrecognized key in AWS file
        pvcae.addUnimplementedWarning(
            BfConsts.RELPATH_AWS_CONFIGS_FILE,
            new Warning(
                String.format("Unrecognized element '%s' in AWS file %s", key, sourceFileName),
                "AWS"));
        continue;
      }
      if (!json.get(key).isArray()) {
        pvcae.addRedFlagWarning(
            BfConsts.RELPATH_AWS_CONFIGS_FILE,
            new Warning(
                String.format(
                    "Unexpected JSON for element '%s' in AWS file %s. Expected a list.",
                    key, sourceFileName),
                "AWS"));
      }

      ArrayNode array = (ArrayNode) json.get(key);
      for (int index = 0; index < array.size(); index++) {
        integratorFunction.accept(array.get(index));
      }
    }
  }

  /**
   * Given some top-level key from an AWS file, returns a function that will convert children to the
   * appropriate type and add them to the appropriate Map (e.g. _addresses, _instances).
   *
   * <p>Returns null for unrecognized keys.
   */
  @Nullable
  private ThrowingConsumer<JsonNode, IOException> getChildConsumer(String elementType) {
    switch (elementType) {
      case AwsVpcEntity.JSON_KEY_ADDRESSES:
        return json -> {
          Address address = BatfishObjectMapper.mapper().convertValue(json, Address.class);
          _addresses.put(address.getId(), address);
        };
      case AwsVpcEntity.JSON_KEY_INSTANCES:
        return json -> {
          Instance instance = BatfishObjectMapper.mapper().convertValue(json, Instance.class);
          if (instance.getStatus() == Status.RUNNING) {
            _instances.put(instance.getId(), instance);
          }
        };
      case AwsVpcEntity.JSON_KEY_CUSTOMER_GATEWAYS:
        return json -> {
          CustomerGateway cGateway =
              BatfishObjectMapper.mapper().convertValue(json, CustomerGateway.class);
          _customerGateways.put(cGateway.getId(), cGateway);
        };
      case AwsVpcEntity.JSON_KEY_DB_INSTANCES:
        return json -> {
          RdsInstance rdsInstance =
              BatfishObjectMapper.mapper().convertValue(json, RdsInstance.class);
          if (rdsInstance.getDbInstanceStatus() == RdsInstance.Status.AVAILABLE) {
            _rdsInstances.put(rdsInstance.getId(), rdsInstance);
          }
        };
      case AwsVpcEntity.JSON_KEY_DOMAIN_STATUS_LIST:
        return json -> {
          ElasticsearchDomain elasticsearchDomain =
              BatfishObjectMapper.mapper().convertValue(json, ElasticsearchDomain.class);
          // we cannot represent an elasticsearch domain without vpc and subnets as a node
          if (elasticsearchDomain.getAvailable() && elasticsearchDomain.getVpcId() != null) {
            _elasticsearchDomains.put(elasticsearchDomain.getId(), elasticsearchDomain);
          }
        };
      case AwsVpcEntity.JSON_KEY_INTERNET_GATEWAYS:
        return json -> {
          InternetGateway iGateway =
              BatfishObjectMapper.mapper().convertValue(json, InternetGateway.class);
          _internetGateways.put(iGateway.getId(), iGateway);
        };
      case AwsVpcEntity.JSON_KEY_NAT_GATEWAYS:
        return json -> {
          NatGateway natGateway = BatfishObjectMapper.mapper().convertValue(json, NatGateway.class);
          _natGateways.put(natGateway.getId(), natGateway);
        };
      case AwsVpcEntity.JSON_KEY_NETWORK_ACLS:
        return json -> {
          NetworkAcl networkAcl = BatfishObjectMapper.mapper().convertValue(json, NetworkAcl.class);
          _networkAcls.put(networkAcl.getId(), networkAcl);
        };
      case AwsVpcEntity.JSON_KEY_NETWORK_INTERFACES:
        return json -> {
          NetworkInterface networkInterface =
              BatfishObjectMapper.mapper().convertValue(json, NetworkInterface.class);
          _networkInterfaces.put(networkInterface.getId(), networkInterface);
        };
      case AwsVpcEntity.JSON_KEY_RESERVATIONS:
        return json -> {
          // instances are embedded inside reservations
          ArrayNode jsonArray = (ArrayNode) json.get(AwsVpcEntity.JSON_KEY_INSTANCES);
          for (int index = 0; index < jsonArray.size(); index++) {
            JsonNode childObject = jsonArray.get(index);
            Objects.requireNonNull(getChildConsumer(AwsVpcEntity.JSON_KEY_INSTANCES))
                .accept(childObject);
          }
        };
      case AwsVpcEntity.JSON_KEY_ROUTE_TABLES:
        return json -> {
          RouteTable routeTable = BatfishObjectMapper.mapper().convertValue(json, RouteTable.class);
          _routeTables.put(routeTable.getId(), routeTable);
        };
      case AwsVpcEntity.JSON_KEY_SECURITY_GROUPS:
        return json -> {
          SecurityGroup sGroup =
              BatfishObjectMapper.mapper().convertValue(json, SecurityGroup.class);
          _securityGroups.put(sGroup.getId(), sGroup);
        };
      case AwsVpcEntity.JSON_KEY_SUBNETS:
        return json -> {
          Subnet subnet = BatfishObjectMapper.mapper().convertValue(json, Subnet.class);
          _subnets.put(subnet.getId(), subnet);
        };
      case AwsVpcEntity.JSON_KEY_VPCS:
        return json -> {
          Vpc vpc = BatfishObjectMapper.mapper().convertValue(json, Vpc.class);
          _vpcs.put(vpc.getId(), vpc);
        };
      case AwsVpcEntity.JSON_KEY_VPC_PEERING_CONNECTIONS:
        return json -> {
          String code =
              json.get(AwsVpcEntity.JSON_KEY_STATUS).get(AwsVpcEntity.JSON_KEY_CODE).textValue();
          if (!code.equals(AwsVpcEntity.STATUS_DELETED)) {
            VpcPeeringConnection vpcPeerConn =
                BatfishObjectMapper.mapper().convertValue(json, VpcPeeringConnection.class);
            _vpcPeerings.put(vpcPeerConn.getId(), vpcPeerConn);
          }
        };
      case AwsVpcEntity.JSON_KEY_VPN_CONNECTIONS:
        return json -> {
          VpnConnection vpnConnection =
              BatfishObjectMapper.mapper().convertValue(json, VpnConnection.class);
          _vpnConnections.put(vpnConnection.getId(), vpnConnection);
        };
      case AwsVpcEntity.JSON_KEY_VPN_GATEWAYS:
        return json -> {
          VpnGateway vpnGateway = BatfishObjectMapper.mapper().convertValue(json, VpnGateway.class);
          _vpnGateways.put(vpnGateway.getId(), vpnGateway);
        };
      default:
        return null;
    }
  }

  @Nonnull
  Map<String, Address> getAddresses() {
    return _addresses;
  }

  @Nonnull
  Map<String, Set<SecurityGroup>> getConfigurationSecurityGroups() {
    return _configurationSecurityGroups;
  }

  @Nonnull
  Map<String, CustomerGateway> getCustomerGateways() {
    return _customerGateways;
  }

  @Nonnull
  Map<String, ElasticsearchDomain> getElasticSearchDomains() {
    return _elasticsearchDomains;
  }

  @Nonnull
  Map<String, Instance> getInstances() {
    return _instances;
  }

  @Nonnull
  Map<String, InternetGateway> getInternetGateways() {
    return _internetGateways;
  }

  @Nonnull
  String getName() {
    return _name;
  }

  @Nonnull
  Map<String, NatGateway> getNatGateways() {
    return _natGateways;
  }

  @Nonnull
  Map<String, NetworkAcl> getNetworkAcls() {
    return _networkAcls;
  }

  @Nonnull
  Map<String, NetworkInterface> getNetworkInterfaces() {
    return _networkInterfaces;
  }

  @Nonnull
  Map<String, RdsInstance> getRdsInstances() {
    return _rdsInstances;
  }

  @Nonnull
  Map<String, RouteTable> getRouteTables() {
    return _routeTables;
  }

  @Nonnull
  Map<String, SecurityGroup> getSecurityGroups() {
    return _securityGroups;
  }

  @Nonnull
  Map<String, Subnet> getSubnets() {
    return _subnets;
  }

  @Nonnull
  Map<String, VpcPeeringConnection> getVpcPeeringConnections() {
    return _vpcPeerings;
  }

  @Nonnull
  Map<String, Vpc> getVpcs() {
    return _vpcs;
  }

  @Nonnull
  Map<String, VpnConnection> getVpnConnections() {
    return _vpnConnections;
  }

  @Nonnull
  Map<String, VpnGateway> getVpnGateways() {
    return _vpnGateways;
  }

  private static boolean ignoreElement(String key) {
    switch (key) {
      case AwsVpcEntity.JSON_KEY_AVAILABILITY_ZONES:
      case AwsVpcEntity.JSON_KEY_DHCP_OPTIONS:
      case AwsVpcEntity.JSON_KEY_REGIONS:
      case AwsVpcEntity.JSON_KEY_INSTANCE_STATUSES:
      case AwsVpcEntity.JSON_KEY_PLACEMENT_GROUPS:
      case AwsVpcEntity.JSON_KEY_SERVICE_NAMES:
      case AwsVpcEntity.JSON_KEY_TAGS:
        return true;
      default:
        return false;
    }
  }

  void toConfigurationNodes(
      AwsConfiguration awsConfiguration,
      Map<String, Configuration> configurationNodes,
      Settings settings,
      Map<String, Warnings> warningsByHost) {

    // updates the Ips which have been allocated already in subnets of all interfaces
    updateAllocatedIps();

    for (Vpc vpc : getVpcs().values()) {
      Warnings warnings = Batfish.buildWarnings(settings);
      Configuration cfgNode = vpc.toConfigurationNode(awsConfiguration, this, warnings);
      configurationNodes.put(cfgNode.getHostname(), cfgNode);
      warningsByHost.put(cfgNode.getHostname(), warnings);
    }

    for (ElasticsearchDomain elasticsearchDomain : getElasticSearchDomains().values()) {
      Warnings warnings = Batfish.buildWarnings(settings);
      Configuration cfgNode =
          elasticsearchDomain.toConfigurationNode(awsConfiguration, this, warnings);
      cfgNode.setDeviceType(DeviceType.HOST);
      configurationNodes.put(cfgNode.getHostname(), cfgNode);
      warningsByHost.put(cfgNode.getHostname(), warnings);
    }

    for (InternetGateway igw : getInternetGateways().values()) {
      Warnings warnings = Batfish.buildWarnings(settings);
      Configuration cfgNode = igw.toConfigurationNode(awsConfiguration, this);
      configurationNodes.put(cfgNode.getHostname(), cfgNode);
      warningsByHost.put(cfgNode.getHostname(), warnings);
    }

    for (NatGateway ngw : getNatGateways().values()) {
      Warnings warnings = Batfish.buildWarnings(settings);
      warnings.redFlag("NAT functionality not yet implemented for " + ngw.getId());
      Configuration cfgNode = ngw.toConfigurationNode(awsConfiguration, this, warnings);
      configurationNodes.put(cfgNode.getHostname(), cfgNode);
      warningsByHost.put(cfgNode.getHostname(), warnings);
    }

    for (VpnGateway vgw : getVpnGateways().values()) {
      Warnings warnings = Batfish.buildWarnings(settings);
      Configuration cfgNode = vgw.toConfigurationNode(awsConfiguration, this, warnings);
      configurationNodes.put(cfgNode.getHostname(), cfgNode);
      warningsByHost.put(cfgNode.getHostname(), warnings);
    }

    for (Instance instance : getInstances().values()) {
      Warnings warnings = Batfish.buildWarnings(settings);
      Configuration cfgNode = instance.toConfigurationNode(this, warnings);
      cfgNode.setDeviceType(DeviceType.HOST);
      configurationNodes.put(cfgNode.getHostname(), cfgNode);
      warningsByHost.put(cfgNode.getHostname(), warnings);
    }

    for (RdsInstance rdsInstance : getRdsInstances().values()) {
      Warnings warnings = Batfish.buildWarnings(settings);
      Configuration cfgNode = rdsInstance.toConfigurationNode(awsConfiguration, this, warnings);
      cfgNode.setDeviceType(DeviceType.HOST);
      configurationNodes.put(cfgNode.getHostname(), cfgNode);
      warningsByHost.put(cfgNode.getHostname(), warnings);
    }

    for (Subnet subnet : getSubnets().values()) {
      Warnings warnings = Batfish.buildWarnings(settings);
      Configuration cfgNode = subnet.toConfigurationNode(awsConfiguration, this, warnings);
      configurationNodes.put(cfgNode.getHostname(), cfgNode);
      warningsByHost.put(cfgNode.getHostname(), warnings);
    }

    for (VpnConnection vpnConnection : getVpnConnections().values()) {
      Warnings warnings = Batfish.buildWarnings(settings);
      vpnConnection.applyToVpnGateway(awsConfiguration, this, warnings);
      warningsByHost.put(vpnConnection.getId(), warnings);
    }

    applySecurityGroupsAcls(configurationNodes);

    // TODO: for now, set all interfaces to have the same bandwidth
    for (Configuration cfgNode : configurationNodes.values()) {
      for (Vrf vrf : cfgNode.getVrfs().values()) {
        for (Interface iface : vrf.getInterfaces().values()) {
          iface.setBandwidth(1E12d);
        }
      }
    }
  }

  private void applySecurityGroupsAcls(Map<String, Configuration> cfgNodes) {
    for (Entry<String, Set<SecurityGroup>> entry : _configurationSecurityGroups.entrySet()) {
      Configuration cfgNode = cfgNodes.get(entry.getKey());
      List<IpAccessListLine> inboundRules = new LinkedList<>();
      List<IpAccessListLine> outboundRules = new LinkedList<>();
      entry
          .getValue()
          .forEach(
              securityGroup ->
                  securityGroup.addInOutAccessLines(inboundRules, outboundRules, this));

      // create ACLs from inboundRules and outboundRules
      IpAccessList inAcl =
          IpAccessList.builder().setName(SG_INGRESS_ACL_NAME).setLines(inboundRules).build();
      IpAccessList outAcl =
          IpAccessList.builder().setName(SG_EGRESS_ACL_NAME).setLines(outboundRules).build();

      cfgNode.getIpAccessLists().put(SG_INGRESS_ACL_NAME, inAcl);
      cfgNode.getIpAccessLists().put(SG_EGRESS_ACL_NAME, outAcl);

      // applying the filters to all interfaces in the node
      cfgNode
          .getAllInterfaces()
          .values()
          .forEach(
              iface -> {
                iface.setIncomingFilter(inAcl);
                iface.setOutgoingFilter(outAcl);
              });
    }
  }

  private void updateAllocatedIps() {
    _networkInterfaces
        .values()
        .forEach(
            networkInterface ->
                _subnets
                    .get(networkInterface.getSubnetId())
                    .getAllocatedIps()
                    .addAll(
                        networkInterface.getIpAddressAssociations().keySet().stream()
                            .map(Ip::asLong)
                            .collect(Collectors.toSet())));
  }

  void updateConfigurationSecurityGroups(String configName, SecurityGroup securityGroup) {
    Set<SecurityGroup> securityGroups =
        getConfigurationSecurityGroups().computeIfAbsent(configName, k -> new HashSet<>());
    securityGroups.add(securityGroup);
  }
}
