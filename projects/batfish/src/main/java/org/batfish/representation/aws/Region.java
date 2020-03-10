package org.batfish.representation.aws;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkArgument;
import static org.batfish.representation.aws.Utils.getTraceElementForSecurityGroup;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.BfConsts;
import org.batfish.common.Warning;
import org.batfish.common.Warnings;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.AclAclLine;
import org.batfish.datamodel.AclLine;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.DeviceType;
import org.batfish.datamodel.FirewallSessionInterfaceInfo;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.answers.ParseVendorConfigurationAnswerElement;
import org.batfish.representation.aws.Instance.Status;

/** Represents an AWS region */
@ParametersAreNonnullByDefault
public final class Region implements Serializable {

  private interface ThrowingConsumer<T, E extends Exception> {
    void accept(T t) throws E, IOException;
  }

  static final String INGRESS = "INGRESS";

  static final String EGRESS = "EGRESS";

  static final String SG_INGRESS_ACL_NAME = "~SECURITY_GROUP_INGRESS_ACL~";

  static final String SG_EGRESS_ACL_NAME = "~SECURITY_GROUP_EGRESS_ACL~";

  @Nonnull private final Map<String, Address> _addresses;

  @Nonnull private final Map<String, Set<SecurityGroup>> _configurationSecurityGroups;

  @Nonnull private final Map<String, CustomerGateway> _customerGateways;

  @Nonnull private final Map<String, ElasticsearchDomain> _elasticsearchDomains;

  @Nonnull private final Map<String, Instance> _instances;

  @Nonnull private final Map<String, InternetGateway> _internetGateways;

  @Nonnull private final Map<String, LoadBalancer> _loadBalancers;

  @Nonnull private final Map<String, LoadBalancerAttributes> _loadBalancerAttributes;

  @Nonnull private final Map<String, LoadBalancerListener> _loadBalancerListeners;

  @Nonnull private final Map<String, LoadBalancerTargetHealth> _loadBalancerTargetHealths;

  @Nonnull private final String _name;

  @Nonnull private final Map<String, NatGateway> _natGateways;

  @Nonnull private final Map<String, NetworkAcl> _networkAcls;

  @Nonnull private final Map<String, NetworkInterface> _networkInterfaces;

  @Nonnull private final Map<String, PrefixList> _prefixLists;

  @Nonnull private final Map<String, RdsInstance> _rdsInstances;

  @Nonnull private final Map<String, RouteTable> _routeTables;

  @Nonnull private final Map<String, SecurityGroup> _securityGroups;

  @Nonnull private final Map<String, Subnet> _subnets;

  @Nonnull private final Map<String, TargetGroup> _targetGroups;

  @Nonnull private final Map<String, TransitGatewayAttachment> _transitGatewayAttachments;

  @Nonnull private final Map<String, TransitGatewayPropagations> _transitGatewayPropagations;

  @Nonnull private final Map<String, TransitGatewayRouteTable> _transitGatewayRouteTables;

  @Nonnull private final Map<String, TransitGatewayStaticRoutes> _transitGatewayStaticRoutes;

  @Nonnull private final Map<String, TransitGatewayVpcAttachment> _transitGatewayVpcAttachments;

  @Nonnull private final Map<String, TransitGateway> _transitGateways;

  @Nonnull private final Map<String, VpcPeeringConnection> _vpcPeerings;

  @Nonnull private final Map<String, Vpc> _vpcs;

  @Nonnull private final Map<String, VpnConnection> _vpnConnections;

  @Nonnull private final Map<String, VpnGateway> _vpnGateways;

  public Region(String name) {
    this(
        name,
        new HashMap<>(),
        new HashMap<>(),
        new HashMap<>(),
        new HashMap<>(),
        new HashMap<>(),
        new HashMap<>(),
        new HashMap<>(),
        new HashMap<>(),
        new HashMap<>(),
        new HashMap<>(),
        new HashMap<>(),
        new HashMap<>(),
        new HashMap<>(),
        new HashMap<>(),
        new HashMap<>(),
        new HashMap<>(),
        new HashMap<>(),
        new HashMap<>(),
        new HashMap<>(),
        new HashMap<>(),
        new HashMap<>(),
        new HashMap<>(),
        new HashMap<>(),
        new HashMap<>(),
        new HashMap<>(),
        new HashMap<>(),
        new HashMap<>(),
        new HashMap<>(),
        new HashMap<>());
  }

  private Region(
      String name,
      Map<String, Address> addresses,
      Map<String, Set<SecurityGroup>> configurationSecurityGroups,
      Map<String, CustomerGateway> customerGateways,
      Map<String, ElasticsearchDomain> elasticsearchDomains,
      Map<String, Instance> instances,
      Map<String, InternetGateway> internetGateways,
      Map<String, LoadBalancer> loadBalancers,
      Map<String, LoadBalancerAttributes> loadBalancerAttributes,
      Map<String, LoadBalancerListener> loadBalancerListeners,
      Map<String, LoadBalancerTargetHealth> loadBalancerTargetHealths,
      Map<String, NatGateway> natGateways,
      Map<String, NetworkAcl> networkAcls,
      Map<String, NetworkInterface> networkInterfaces,
      Map<String, PrefixList> prefixLists,
      Map<String, RdsInstance> rdsInstances,
      Map<String, RouteTable> routeTables,
      Map<String, SecurityGroup> securityGroups,
      Map<String, Subnet> subnets,
      Map<String, TargetGroup> targetGroups,
      Map<String, TransitGatewayAttachment> transitGatewayAttachments,
      Map<String, TransitGatewayPropagations> transitGatewayPropagations,
      Map<String, TransitGatewayRouteTable> transitGatewayRouteTables,
      Map<String, TransitGatewayStaticRoutes> transitGatewayStaticRoutes,
      Map<String, TransitGatewayVpcAttachment> transitGatewayVpcAttachments,
      Map<String, TransitGateway> transitGateways,
      Map<String, VpcPeeringConnection> vpcPeerings,
      Map<String, Vpc> vpcs,
      Map<String, VpnConnection> vpnConnections,
      Map<String, VpnGateway> vpnGateways) {
    _name = name;
    _addresses = addresses;
    _configurationSecurityGroups = configurationSecurityGroups;
    _customerGateways = customerGateways;
    _elasticsearchDomains = elasticsearchDomains;
    _instances = instances;
    _internetGateways = internetGateways;
    _loadBalancers = loadBalancers;
    _loadBalancerAttributes = loadBalancerAttributes;
    _loadBalancerListeners = loadBalancerListeners;
    _loadBalancerTargetHealths = loadBalancerTargetHealths;
    _natGateways = natGateways;
    _networkAcls = networkAcls;
    _networkInterfaces = networkInterfaces;
    _prefixLists = prefixLists;
    _rdsInstances = rdsInstances;
    _routeTables = routeTables;
    _securityGroups = securityGroups;
    _subnets = subnets;
    _targetGroups = targetGroups;
    _transitGatewayAttachments = transitGatewayAttachments;
    _transitGatewayPropagations = transitGatewayPropagations;
    _transitGatewayRouteTables = transitGatewayRouteTables;
    _transitGatewayStaticRoutes = transitGatewayStaticRoutes;
    _transitGatewayVpcAttachments = transitGatewayVpcAttachments;
    _transitGateways = transitGateways;
    _vpcPeerings = vpcPeerings;
    _vpcs = vpcs;
    _vpnConnections = vpnConnections;
    _vpnGateways = vpnGateways;
  }

  void addConfigElement(
      JsonNode json, String sourceFileName, ParseVendorConfigurationAnswerElement pvcae) {

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
        // Add warning for unrecognized key in AWS file but don't warn if there is no data
        if (json.get(key).isArray() && json.get(key).size() == 0) {
          continue;
        }
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
        try {
          integratorFunction.accept(array.get(index));
        } catch (IOException | IllegalArgumentException e) {
          pvcae.addRedFlagWarning(
              BfConsts.RELPATH_AWS_CONFIGS_FILE,
              new Warning(
                  String.format(
                      "Exception while parsing '%s' in AWS file %s: %s",
                      key, sourceFileName, e.getMessage()),
                  "AWS"));
        }
      }
    }
  }

  static RegionBuilder builder(String name) {
    return new RegionBuilder(name);
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
      case AwsVpcEntity.JSON_KEY_LOAD_BALANCER_ATTRIBUTES:
        return json -> {
          LoadBalancerAttributes loadBalancerAttributes =
              BatfishObjectMapper.mapper().convertValue(json, LoadBalancerAttributes.class);
          _loadBalancerAttributes.put(loadBalancerAttributes.getId(), loadBalancerAttributes);
        };
      case AwsVpcEntity.JSON_KEY_LOAD_BALANCER_LISTENERS:
        return json -> {
          LoadBalancerListener loadBalancerListener =
              BatfishObjectMapper.mapper().convertValue(json, LoadBalancerListener.class);
          _loadBalancerListeners.put(loadBalancerListener.getId(), loadBalancerListener);
        };
      case AwsVpcEntity.JSON_KEY_LOAD_BALANCER_TARGET_HEALTH:
        return json -> {
          LoadBalancerTargetHealth loadBalancerTargetHealth =
              BatfishObjectMapper.mapper().convertValue(json, LoadBalancerTargetHealth.class);
          _loadBalancerTargetHealths.put(
              loadBalancerTargetHealth.getId(), loadBalancerTargetHealth);
        };
      case AwsVpcEntity.JSON_KEY_LOAD_BALANCERS:
        return json -> {
          String stateCode =
              json.get(AwsVpcEntity.JSON_KEY_STATE).get(AwsVpcEntity.JSON_KEY_CODE).textValue();
          if (stateCode.equals(AwsVpcEntity.STATUS_ACTIVE)) {
            LoadBalancer loadBalancer =
                BatfishObjectMapper.mapper().convertValue(json, LoadBalancer.class);
            _loadBalancers.put(loadBalancer.getId(), loadBalancer);
          }
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
      case AwsVpcEntity.JSON_KEY_PREFIX_LISTS:
        return json -> {
          PrefixList prefixList = BatfishObjectMapper.mapper().convertValue(json, PrefixList.class);
          _prefixLists.put(prefixList.getId(), prefixList);
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
      case AwsVpcEntity.JSON_KEY_TARGET_GROUPS:
        return json -> {
          TargetGroup targetGroup =
              BatfishObjectMapper.mapper().convertValue(json, TargetGroup.class);
          _targetGroups.put(targetGroup.getId(), targetGroup);
        };
      case AwsVpcEntity.JSON_KEY_TRANSIT_GATEWAY_ATTACHMENTS:
        return json -> {
          String state = json.get(AwsVpcEntity.JSON_KEY_STATE).textValue();
          if (state.equals(AwsVpcEntity.STATE_AVAILABLE)) {
            TransitGatewayAttachment tGatewayAttachment =
                BatfishObjectMapper.mapper().convertValue(json, TransitGatewayAttachment.class);
            _transitGatewayAttachments.put(tGatewayAttachment.getId(), tGatewayAttachment);
          }
        };
      case AwsVpcEntity.JSON_KEY_TRANSIT_GATEWAY_PROPAGATIONS:
        return json -> {
          TransitGatewayPropagations propagations =
              BatfishObjectMapper.mapper().convertValue(json, TransitGatewayPropagations.class);
          _transitGatewayPropagations.put(propagations.getId(), propagations);
        };
      case AwsVpcEntity.JSON_KEY_TRANSIT_GATEWAY_ROUTE_TABLES:
        return json -> {
          String state = json.get(AwsVpcEntity.JSON_KEY_STATE).textValue();
          if (state.equals(AwsVpcEntity.STATE_AVAILABLE)) {
            TransitGatewayRouteTable transitGatewayRouteTable =
                BatfishObjectMapper.mapper().convertValue(json, TransitGatewayRouteTable.class);
            _transitGatewayRouteTables.put(
                transitGatewayRouteTable.getId(), transitGatewayRouteTable);
          }
        };
      case AwsVpcEntity.JSON_KEY_TRANSIT_GATEWAY_STATIC_ROUTES:
        return json -> {
          TransitGatewayStaticRoutes transitGatewayStaticRoutes =
              BatfishObjectMapper.mapper().convertValue(json, TransitGatewayStaticRoutes.class);
          _transitGatewayStaticRoutes.put(
              transitGatewayStaticRoutes.getId(), transitGatewayStaticRoutes);
        };
      case AwsVpcEntity.JSON_KEY_TRANSIT_GATEWAY_VPC_ATTACHMENTS:
        return json -> {
          String state = json.get(AwsVpcEntity.JSON_KEY_STATE).textValue();
          if (state.equals(AwsVpcEntity.STATE_AVAILABLE)) {
            TransitGatewayVpcAttachment tGatewayVpcAttachment =
                BatfishObjectMapper.mapper().convertValue(json, TransitGatewayVpcAttachment.class);
            _transitGatewayVpcAttachments.put(tGatewayVpcAttachment.getId(), tGatewayVpcAttachment);
          }
        };
      case AwsVpcEntity.JSON_KEY_TRANSIT_GATEWAYS:
        return json -> {
          String state = json.get(AwsVpcEntity.JSON_KEY_STATE).textValue();
          if (state.equals(AwsVpcEntity.STATE_AVAILABLE)) {
            TransitGateway tGateway =
                BatfishObjectMapper.mapper().convertValue(json, TransitGateway.class);
            _transitGateways.put(tGateway.getId(), tGateway);
          }
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
          if (code.equals(AwsVpcEntity.STATUS_ACTIVE)) {
            VpcPeeringConnection vpcPeerConn =
                BatfishObjectMapper.mapper().convertValue(json, VpcPeeringConnection.class);
            _vpcPeerings.put(vpcPeerConn.getId(), vpcPeerConn);
          }
        };
      case AwsVpcEntity.JSON_KEY_VPN_CONNECTIONS:
        return json -> {
          String state = json.get(AwsVpcEntity.JSON_KEY_STATE).textValue();
          if (!state.equals(AwsVpcEntity.STATUS_DELETED)) {
            VpnConnection vpnConnection =
                BatfishObjectMapper.mapper().convertValue(json, VpnConnection.class);
            _vpnConnections.put(vpnConnection.getId(), vpnConnection);
          }
        };
      case AwsVpcEntity.JSON_KEY_VPN_GATEWAYS:
        return json -> {
          String state = json.get(AwsVpcEntity.JSON_KEY_STATE).textValue();
          if (state.equals(AwsVpcEntity.STATE_AVAILABLE)) {
            VpnGateway vpnGateway =
                BatfishObjectMapper.mapper().convertValue(json, VpnGateway.class);
            _vpnGateways.put(vpnGateway.getId(), vpnGateway);
          }
        };
      default:
        return null;
    }
  }

  @Nonnull
  Set<TransitGatewayRouteTable> getTransitGatewayRouteTables(String transitGatewayId) {
    return _transitGatewayRouteTables.values().stream()
        .filter(rt -> rt.getGatewayId().equals(transitGatewayId))
        .collect(ImmutableSet.toImmutableSet());
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
  public Map<String, Instance> getInstances() {
    return _instances;
  }

  @Nonnull
  Map<String, InternetGateway> getInternetGateways() {
    return _internetGateways;
  }

  @Nonnull
  Map<String, LoadBalancer> getLoadBalancers() {
    return _loadBalancers;
  }

  @Nonnull
  Map<String, LoadBalancerAttributes> getLoadBalancerAttributes() {
    return _loadBalancerAttributes;
  }

  @Nonnull
  Map<String, LoadBalancerListener> getLoadBalancerListeners() {
    return _loadBalancerListeners;
  }

  @Nonnull
  Map<String, LoadBalancerTargetHealth> getLoadBalancerTargetHealths() {
    return _loadBalancerTargetHealths;
  }

  @Nonnull
  public String getName() {
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
  Map<String, PrefixList> getPrefixLists() {
    return _prefixLists;
  }

  @Nonnull
  public Map<String, NetworkInterface> getNetworkInterfaces() {
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
  public Map<String, SecurityGroup> getSecurityGroups() {
    return _securityGroups;
  }

  @Nonnull
  Map<String, Subnet> getSubnets() {
    return _subnets;
  }

  @Nonnull
  Map<String, TargetGroup> getTargetGroups() {
    return _targetGroups;
  }

  @Nonnull
  Map<String, TransitGatewayAttachment> getTransitGatewayAttachments() {
    return _transitGatewayAttachments;
  }

  @Nonnull
  Map<String, TransitGatewayPropagations> getTransitGatewayPropagations() {
    return _transitGatewayPropagations;
  }

  @Nonnull
  Map<String, TransitGatewayRouteTable> getTransitGatewayRouteTables() {
    return _transitGatewayRouteTables;
  }

  @Nonnull
  Map<String, TransitGatewayStaticRoutes> getTransitGatewayStaticRoutes() {
    return _transitGatewayStaticRoutes;
  }

  @Nonnull
  Map<String, TransitGatewayVpcAttachment> getTransitGatewayVpcAttachments() {
    return _transitGatewayVpcAttachments;
  }

  @Nonnull
  Map<String, TransitGateway> getTransitGateways() {
    return _transitGateways;
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

  void toConfigurationNodes(ConvertedConfiguration awsConfiguration, Warnings warnings) {

    // updates the Ips which have been allocated already in subnets of all interfaces
    updateAllocatedIps();

    for (Vpc vpc : getVpcs().values()) {
      Configuration cfgNode = vpc.toConfigurationNode(awsConfiguration, this, warnings);
      awsConfiguration.addNode(cfgNode);
    }

    for (ElasticsearchDomain elasticsearchDomain : getElasticSearchDomains().values()) {
      Configuration cfgNode =
          elasticsearchDomain.toConfigurationNode(awsConfiguration, this, warnings);
      cfgNode.setDeviceType(DeviceType.HOST);
      awsConfiguration.addNode(cfgNode);
    }

    for (InternetGateway igw : getInternetGateways().values()) {
      Configuration cfgNode = igw.toConfigurationNode(awsConfiguration, this);
      awsConfiguration.addNode(cfgNode);
    }

    for (NatGateway ngw : getNatGateways().values()) {
      Configuration cfgNode = ngw.toConfigurationNode(awsConfiguration, this, warnings);
      awsConfiguration.addNode(cfgNode);
    }

    for (VpnGateway vgw : getVpnGateways().values()) {
      Configuration cfgNode = vgw.toConfigurationNode(awsConfiguration, this, warnings);
      awsConfiguration.addNode(cfgNode);
    }

    for (Instance instance : getInstances().values()) {
      Configuration cfgNode = instance.toConfigurationNode(awsConfiguration, this, warnings);
      cfgNode.setDeviceType(DeviceType.HOST);
      awsConfiguration.addNode(cfgNode);
    }

    for (RdsInstance rdsInstance : getRdsInstances().values()) {
      Configuration cfgNode = rdsInstance.toConfigurationNode(awsConfiguration, this, warnings);
      cfgNode.setDeviceType(DeviceType.HOST);
      awsConfiguration.addNode(cfgNode);
    }

    for (LoadBalancer loadBalancer : getLoadBalancers().values()) {
      List<Configuration> cfgNodes =
          loadBalancer.toConfigurationNodes(awsConfiguration, this, warnings);
      cfgNodes.forEach(cfgNode -> awsConfiguration.addNode(cfgNode));
    }

    for (Subnet subnet : getSubnets().values()) {
      Configuration cfgNode = subnet.toConfigurationNode(awsConfiguration, this, warnings);
      awsConfiguration.addNode(cfgNode);
    }

    for (TransitGateway tgw : getTransitGateways().values()) {
      Configuration cfgNode = tgw.toConfigurationNode(awsConfiguration, this, warnings);
      awsConfiguration.addNode(cfgNode);
    }

    // VpcPeeringConnections are processed in AwsConfiguration since they can be cross region

    applySecurityGroupsAcls(awsConfiguration.getConfigurationNodes(), warnings);

    // TODO: for now, set all interfaces to have the same bandwidth
    for (Configuration cfgNode : awsConfiguration.getConfigurationNodes().values()) {
      for (Interface iface : cfgNode.getAllInterfaces().values()) {
        iface.setBandwidth(1E12d);
      }
    }
  }

  /** Convert security groups of all nodes to IpAccessLists and apply to all interfaces */
  @VisibleForTesting
  void applySecurityGroupsAcls(Map<String, Configuration> cfgNodes, Warnings warnings) {
    for (Entry<String, Set<SecurityGroup>> entry : _configurationSecurityGroups.entrySet()) {
      Configuration cfgNode = cfgNodes.get(entry.getKey());
      List<AclLine> inAclAclLines = new ArrayList<>();
      List<AclLine> outAclAclLines = new ArrayList<>();
      entry.getValue().stream()
          .sorted(Comparator.comparing(SecurityGroup::getId)) // for stable ordering of lines
          .forEach(
              securityGroup -> {
                String sgName = String.format("Security Group %s", securityGroup.getGroupName());
                Optional.ofNullable(
                        securityGroupToIpAccessList(securityGroup, true, cfgNode, warnings))
                    .map(
                        acl ->
                            new AclAclLine(
                                sgName,
                                acl.getName(),
                                getTraceElementForSecurityGroup(securityGroup.getGroupName())))
                    .ifPresent(inAclAclLines::add);
                Optional.ofNullable(
                        securityGroupToIpAccessList(securityGroup, false, cfgNode, warnings))
                    .map(
                        acl ->
                            new AclAclLine(
                                sgName,
                                acl.getName(),
                                getTraceElementForSecurityGroup(securityGroup.getGroupName())))
                    .ifPresent(outAclAclLines::add);
              });
      applyAclLinesToInterfaces(inAclAclLines, outAclAclLines, cfgNode);
    }
  }

  private static void applyAclLinesToInterfaces(
      List<AclLine> inAclLines, List<AclLine> outAclLines, Configuration configuration) {
    // create a combined in ACL and out ACL using the inputs
    IpAccessList inAcl =
        IpAccessList.builder()
            .setName(SG_INGRESS_ACL_NAME)
            .setLines(inAclLines)
            .setOwner(configuration)
            .build();
    IpAccessList outAcl =
        IpAccessList.builder()
            .setName(SG_EGRESS_ACL_NAME)
            .setLines(outAclLines)
            .setOwner(configuration)
            .build();

    // applying the filters to all interfaces in the node
    configuration
        .getAllInterfaces()
        .values()
        .forEach(
            iface -> {
              iface.setIncomingFilter(inAcl);
              iface.setOutgoingFilter(outAcl);
              iface.setFirewallSessionInterfaceInfo(
                  new FirewallSessionInterfaceInfo(
                      false, ImmutableList.of(iface.getName()), null, null));
            });
  }

  @Nullable
  private IpAccessList securityGroupToIpAccessList(
      SecurityGroup securityGroup, boolean ingress, Configuration owner, Warnings warnings) {
    List<AclLine> aclLines = securityGroup.toAclLines(this, ingress, warnings);
    if (aclLines.isEmpty()) {
      return null;
    }
    return IpAccessList.builder()
        .setName(
            String.format(
                "~%s~SECURITY-GROUP~%s~%s~",
                ingress ? INGRESS : EGRESS, securityGroup.getGroupName(), securityGroup.getId()))
        .setLines(aclLines)
        .setOwner(owner)
        .build();
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
                        networkInterface.getPrivateIpAddresses().stream()
                            .map(PrivateIpAddress::getPrivateIp)
                            .map(Ip::asLong)
                            .collect(Collectors.toSet())));
  }

  void updateConfigurationSecurityGroups(String configName, SecurityGroup securityGroup) {
    Set<SecurityGroup> securityGroups =
        getConfigurationSecurityGroups().computeIfAbsent(configName, k -> new HashSet<>());
    securityGroups.add(securityGroup);
  }

  /**
   * Returns the Internet gateway associated with the VPC with provided id, if one exists.
   *
   * <p>AWS limits VPCs to one gateway. If more are present, return the first one found.
   */
  Optional<InternetGateway> findInternetGateway(String vpcId) {
    // AWS does not allow multiple internet gateways to be attached to a VPC
    return _internetGateways.values().stream()
        .filter(igw -> igw.getAttachmentVpcIds().contains(vpcId))
        .findFirst();
  }

  /** Returns the {@link TransitGateway} that owns the provided route table */
  Optional<TransitGateway> findTransitGateway(String routeTableId) {
    return _transitGateways.values().stream()
        .filter(
            tgw ->
                _transitGatewayRouteTables.values().stream()
                    .anyMatch(tgwRt -> tgwRt.getId().equals(routeTableId)))
        .findFirst();
  }

  /** Returns {@link TransitGatewayAttachment} given the gateway and attachment id's */
  Optional<TransitGatewayAttachment> findTransitGatewayAttachment(
      String attachmentId, String transitGatewayId) {
    return _transitGatewayAttachments.values().stream()
        .filter(a -> a.getId().equals(attachmentId) && a.getGatewayId().equals(transitGatewayId))
        .findFirst();
  }

  /** Returns {@link TransitGatewayVpcAttachment} between the VPC and Transit Gateway */
  Optional<TransitGatewayVpcAttachment> findTransitGatewayVpcAttachment(
      String vpcId, String transitGatewayId) {
    return _transitGatewayVpcAttachments.values().stream()
        .filter(a -> a.getVpcId().equals(vpcId) && a.getGatewayId().equals(transitGatewayId))
        .findFirst();
  }

  /** Returns {@link VpnConnection} for the Transit Gateway */
  Optional<VpnConnection> findTransitGatewayVpnConnection(String vpnId, String transitGatewayId) {
    return _vpnConnections.values().stream()
        .filter(c -> c.getId().equals(vpnId) && c.getAwsGatewayId().equals(transitGatewayId))
        .findFirst();
  }

  /**
   * Returns the VPN gateway associated with the VPC with provided id, if one exists.
   *
   * <p>AWS limits VPCs to one gateway. If more are present, return the first one found.
   */
  Optional<VpnGateway> findVpnGateway(String vpcId) {
    // AWS does not allow multiple vpc gateways to be attached to a VPC
    return _vpnGateways.values().stream()
        .filter(igw -> igw.getAttachmentVpcIds().contains(vpcId))
        .findFirst();
  }

  /**
   * Returns the route table gateway associated with the subnet with provided id, if one exists.
   *
   * <p>AWS limits subnets to one route table. If more are present, return the first one found. If
   * none is found, returns the main route table associated with the VPC (and AWS limits VPCs to one
   * main route table).
   */
  Optional<RouteTable> findRouteTable(String vpcId, String subnetId) {
    List<RouteTable> vpcTables =
        _routeTables.values().stream()
            .filter(rt -> rt.getVpcId().equals(vpcId))
            .collect(ImmutableList.toImmutableList());

    // First we look for the custom route table associated to the subnet.
    Optional<RouteTable> customSubnetTable =
        vpcTables.stream()
            .filter(
                rt ->
                    rt.getAssociations().stream()
                        .anyMatch(assoc -> Objects.equals(assoc.getSubnetId(), subnetId)))
            .findFirst();

    if (customSubnetTable.isPresent()) {
      return customSubnetTable;
    }

    // Return the main table for the vpc
    return vpcTables.stream()
        .filter(rt -> rt.getAssociations().stream().anyMatch(RouteTable.Association::isMain))
        .findFirst();
  }

  static final class RegionBuilder {
    private Map<String, Address> _addresses;
    private Map<String, Set<SecurityGroup>> _configurationSecurityGroups;
    private Map<String, CustomerGateway> _customerGateways;
    private Map<String, ElasticsearchDomain> _elasticsearchDomains;
    private Map<String, Instance> _instances;
    private Map<String, InternetGateway> _internetGateways;
    private Map<String, LoadBalancer> _loadBalancers;
    private Map<String, LoadBalancerAttributes> _loadBalancerAttributes;
    private Map<String, LoadBalancerListener> _loadBalancerListeners;
    private Map<String, LoadBalancerTargetHealth> _loadBalancerTargetHealths;
    private String _name;
    private Map<String, NatGateway> _natGateways;
    private Map<String, NetworkAcl> _networkAcls;
    private Map<String, NetworkInterface> _networkInterfaces;
    private Map<String, PrefixList> _prefixLists;
    private Map<String, RdsInstance> _rdsInstances;
    private Map<String, RouteTable> _routeTables;
    private Map<String, SecurityGroup> _securityGroups;
    private Map<String, Subnet> _subnets;
    private Map<String, TargetGroup> _targetGroups;
    private Map<String, TransitGatewayAttachment> _transitGatewayAttachments;
    private Map<String, TransitGatewayPropagations> _transitGatewayPropagations;
    private Map<String, TransitGatewayRouteTable> _transitGatewayRouteTables;
    private Map<String, TransitGatewayStaticRoutes> _transitGatewayStaticRoutes;
    private Map<String, TransitGatewayVpcAttachment> _transitGatewayVpcAttachments;
    private Map<String, TransitGateway> _transitGateways;
    private Map<String, VpcPeeringConnection> _vpcPeerings;
    private Map<String, Vpc> _vpcs;
    private Map<String, VpnConnection> _vpnConnections;
    private Map<String, VpnGateway> _vpnGateways;

    private RegionBuilder(String name) {
      _name = name;
    }

    public RegionBuilder setAddresses(Map<String, Address> addresses) {
      _addresses = addresses;
      return this;
    }

    public RegionBuilder setConfigurationSecurityGroups(
        Map<String, Set<SecurityGroup>> configurationSecurityGroups) {
      _configurationSecurityGroups = configurationSecurityGroups;
      return this;
    }

    public RegionBuilder setCustomerGateways(Map<String, CustomerGateway> customerGateways) {
      _customerGateways = customerGateways;
      return this;
    }

    public RegionBuilder setElasticsearchDomains(
        Map<String, ElasticsearchDomain> elasticsearchDomains) {
      _elasticsearchDomains = elasticsearchDomains;
      return this;
    }

    public RegionBuilder setInstances(Map<String, Instance> instances) {
      _instances = instances;
      return this;
    }

    public RegionBuilder setInternetGateways(Map<String, InternetGateway> internetGateways) {
      _internetGateways = internetGateways;
      return this;
    }

    public RegionBuilder setLoadBalancers(Map<String, LoadBalancer> loadBalancers) {
      _loadBalancers = loadBalancers;
      return this;
    }

    public RegionBuilder setLoadBalancerAttributes(
        Map<String, LoadBalancerAttributes> loadBalancerAttributes) {
      _loadBalancerAttributes = loadBalancerAttributes;
      return this;
    }

    public RegionBuilder setLoadBalancerListeners(
        Map<String, LoadBalancerListener> loadBalancerListeners) {
      _loadBalancerListeners = loadBalancerListeners;
      return this;
    }

    public RegionBuilder setLoadBalancerTargetHealths(
        Map<String, LoadBalancerTargetHealth> loadBalancerTargetHealths) {
      _loadBalancerTargetHealths = loadBalancerTargetHealths;
      return this;
    }

    public RegionBuilder setName(String name) {
      _name = name;
      return this;
    }

    public RegionBuilder setNatGateways(Map<String, NatGateway> natGateways) {
      _natGateways = natGateways;
      return this;
    }

    public RegionBuilder setNetworkAcls(Map<String, NetworkAcl> networkAcls) {
      _networkAcls = networkAcls;
      return this;
    }

    public RegionBuilder setNetworkInterfaces(Map<String, NetworkInterface> networkInterfaces) {
      _networkInterfaces = networkInterfaces;
      return this;
    }

    public RegionBuilder setPrefixLists(Map<String, PrefixList> prefixLists) {
      _prefixLists = prefixLists;
      return this;
    }

    public RegionBuilder setRdsInstances(Map<String, RdsInstance> rdsInstances) {
      _rdsInstances = rdsInstances;
      return this;
    }

    public RegionBuilder setRouteTables(Map<String, RouteTable> routeTables) {
      _routeTables = routeTables;
      return this;
    }

    public RegionBuilder setSecurityGroups(Map<String, SecurityGroup> securityGroups) {
      _securityGroups = securityGroups;
      return this;
    }

    public RegionBuilder setSubnets(Map<String, Subnet> subnets) {
      _subnets = subnets;
      return this;
    }

    public RegionBuilder setTargetGroups(Map<String, TargetGroup> targetGroups) {
      _targetGroups = targetGroups;
      return this;
    }

    public RegionBuilder setTransitGatewayAttachments(
        Map<String, TransitGatewayAttachment> transitGatewayAttachments) {
      _transitGatewayAttachments = transitGatewayAttachments;
      return this;
    }

    public RegionBuilder setTransitGatewayPropagations(
        Map<String, TransitGatewayPropagations> transitGatewayPropagations) {
      _transitGatewayPropagations = transitGatewayPropagations;
      return this;
    }

    public RegionBuilder setTransitGatewayRouteTables(
        Map<String, TransitGatewayRouteTable> transitGatewayRouteTables) {
      _transitGatewayRouteTables = transitGatewayRouteTables;
      return this;
    }

    public RegionBuilder setTransitGatewayStaticRoutes(
        Map<String, TransitGatewayStaticRoutes> transitGatewayStaticRoutes) {
      _transitGatewayStaticRoutes = transitGatewayStaticRoutes;
      return this;
    }

    public RegionBuilder setTransitGatewayVpcAttachments(
        Map<String, TransitGatewayVpcAttachment> transitGatewayVpcAttachments) {
      _transitGatewayVpcAttachments = transitGatewayVpcAttachments;
      return this;
    }

    public RegionBuilder setTransitGateways(Map<String, TransitGateway> transitGateways) {
      _transitGateways = transitGateways;
      return this;
    }

    public RegionBuilder setVpcPeerings(Map<String, VpcPeeringConnection> vpcPeerings) {
      _vpcPeerings = vpcPeerings;
      return this;
    }

    public RegionBuilder setVpcs(Map<String, Vpc> vpcs) {
      _vpcs = vpcs;
      return this;
    }

    public RegionBuilder setVpnConnections(Map<String, VpnConnection> vpnConnections) {
      _vpnConnections = vpnConnections;
      return this;
    }

    public RegionBuilder setVpnGateways(Map<String, VpnGateway> vpnGateways) {
      _vpnGateways = vpnGateways;
      return this;
    }

    public Region build() {
      checkArgument(_name != null, "Region name must be set");
      return new Region(
          _name,
          firstNonNull(_addresses, ImmutableMap.of()),
          firstNonNull(_configurationSecurityGroups, ImmutableMap.of()),
          firstNonNull(_customerGateways, ImmutableMap.of()),
          firstNonNull(_elasticsearchDomains, ImmutableMap.of()),
          firstNonNull(_instances, ImmutableMap.of()),
          firstNonNull(_internetGateways, ImmutableMap.of()),
          firstNonNull(_loadBalancers, ImmutableMap.of()),
          firstNonNull(_loadBalancerAttributes, ImmutableMap.of()),
          firstNonNull(_loadBalancerListeners, ImmutableMap.of()),
          firstNonNull(_loadBalancerTargetHealths, ImmutableMap.of()),
          firstNonNull(_natGateways, ImmutableMap.of()),
          firstNonNull(_networkAcls, ImmutableMap.of()),
          firstNonNull(_networkInterfaces, ImmutableMap.of()),
          firstNonNull(_prefixLists, ImmutableMap.of()),
          firstNonNull(_rdsInstances, ImmutableMap.of()),
          firstNonNull(_routeTables, ImmutableMap.of()),
          firstNonNull(_securityGroups, ImmutableMap.of()),
          firstNonNull(_subnets, ImmutableMap.of()),
          firstNonNull(_targetGroups, ImmutableMap.of()),
          firstNonNull(_transitGatewayAttachments, ImmutableMap.of()),
          firstNonNull(_transitGatewayPropagations, ImmutableMap.of()),
          firstNonNull(_transitGatewayRouteTables, ImmutableMap.of()),
          firstNonNull(_transitGatewayStaticRoutes, ImmutableMap.of()),
          firstNonNull(_transitGatewayVpcAttachments, ImmutableMap.of()),
          firstNonNull(_transitGateways, ImmutableMap.of()),
          firstNonNull(_vpcPeerings, ImmutableMap.of()),
          firstNonNull(_vpcs, ImmutableMap.of()),
          firstNonNull(_vpnConnections, ImmutableMap.of()),
          firstNonNull(_vpnGateways, ImmutableMap.of()));
    }
  }
}
