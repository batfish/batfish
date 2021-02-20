package org.batfish.representation.aws;

import static com.google.common.base.MoreObjects.firstNonNull;
import static com.google.common.base.Preconditions.checkArgument;
import static org.batfish.datamodel.acl.AclLineMatchExprs.not;
import static org.batfish.representation.aws.AwsConfiguration.AWS_SERVICES_GATEWAY_NODE_NAME;
import static org.batfish.representation.aws.ElasticsearchDomain.getNodeName;
import static org.batfish.representation.aws.Utils.getTraceElementForSecurityGroup;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Streams;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.BfConsts;
import org.batfish.common.Warning;
import org.batfish.common.Warnings;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.AclAclLine;
import org.batfish.datamodel.AclLine;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.DeviceType;
import org.batfish.datamodel.ExprAclLine;
import org.batfish.datamodel.FirewallSessionInterfaceInfo;
import org.batfish.datamodel.FirewallSessionInterfaceInfo.Action;
import org.batfish.datamodel.FirewallSessionVrfInfo;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.IpWildcard;
import org.batfish.datamodel.IpWildcardSetIpSpace;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.TraceElement;
import org.batfish.datamodel.acl.MatchHeaderSpace;
import org.batfish.datamodel.answers.ParseVendorConfigurationAnswerElement;
import org.batfish.referencelibrary.AddressGroup;
import org.batfish.referencelibrary.GeneratedRefBookUtils;
import org.batfish.referencelibrary.GeneratedRefBookUtils.BookType;
import org.batfish.referencelibrary.ReferenceBook;
import org.batfish.representation.aws.Instance.Status;

/** Represents an AWS region */
@ParametersAreNonnullByDefault
public final class Region implements Serializable {

  private interface ThrowingConsumer<T, E extends Exception> {
    void accept(T t) throws E, IOException;
  }

  static String eniEgressAclName(String interfaceName) {
    return String.format("~EGRESS_ACL~%s", interfaceName);
  }

  static String eniIngressAclName(String interfaceName) {
    return String.format("~INGRESS_ACL~%s", interfaceName);
  }

  static final TraceElement DENY_SPOOFED_SOURCE_IP_TRACE_ELEMENT =
      TraceElement.of("Deny spoofed source IPs");

  @Nonnull private final Map<String, Address> _addresses;

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

  @Nonnull private final Map<String, VpcEndpoint> _vpcEndpoints;

  @Nonnull private final Map<String, VpnGateway> _vpnGateways;

  private Map<String, IpAccessList> _sgIngressAcls;
  private Map<String, IpAccessList> _sgEgressAcls;

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
      Map<String, VpcEndpoint> vpcEndpoints,
      Map<String, VpcPeeringConnection> vpcPeerings,
      Map<String, Vpc> vpcs,
      Map<String, VpnConnection> vpnConnections,
      Map<String, VpnGateway> vpnGateways) {
    _name = name;
    _addresses = addresses;
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
    _vpcEndpoints = vpcEndpoints;
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

  public static RegionBuilder builder(String name) {
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
          String stateCode = json.get(AwsVpcEntity.JSON_KEY_STATE).textValue();
          if (stateCode.equals(AwsVpcEntity.STATE_AVAILABLE)) {
            CustomerGateway cGateway =
                BatfishObjectMapper.mapper().convertValue(json, CustomerGateway.class);
            _customerGateways.put(cGateway.getId(), cGateway);
          }
        };
      case AwsVpcEntity.JSON_KEY_DB_INSTANCES:
        return json -> {
          RdsInstance rdsInstance =
              BatfishObjectMapper.mapper().convertValue(json, RdsInstance.class);
          if (rdsInstance.isUp()) {
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
          String stateCode = json.get(AwsVpcEntity.JSON_KEY_STATE).textValue();
          if (stateCode.equals(AwsVpcEntity.STATE_AVAILABLE)) {
            NatGateway natGateway =
                BatfishObjectMapper.mapper().convertValue(json, NatGateway.class);
            _natGateways.put(natGateway.getId(), natGateway);
          }
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
      case AwsVpcEntity.JSON_KEY_VPC_ENDPOINTS:
        return json -> {
          String state = json.get(AwsVpcEntity.JSON_KEY_STATE).textValue();
          if (state.equals(AwsVpcEntity.STATE_AVAILABLE)) {
            VpcEndpoint vpcEndpoint =
                BatfishObjectMapper.mapper().convertValue(json, VpcEndpoint.class);
            _vpcEndpoints.put(vpcEndpoint.getId(), vpcEndpoint);
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
          if (state.equals(AwsVpcEntity.STATE_AVAILABLE)) {
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

  /** Adds a generated references book for prefix lists on the AWS services gateway node */
  void addPrefixListReferenceBook(
      ConvertedConfiguration convertedConfiguration, Warnings warnings) {
    Configuration awsServicesNode = convertedConfiguration.getNode(AWS_SERVICES_GATEWAY_NODE_NAME);
    if (awsServicesNode == null) {
      warnings.redFlag("AWS service gateway not found. Cannot generate prefix list address books");
      return;
    }
    String bookName =
        GeneratedRefBookUtils.getName(awsServicesNode.getHostname(), BookType.AwsSeviceIps);
    final ReferenceBook currentBook =
        awsServicesNode
            .getGeneratedReferenceBooks()
            .getOrDefault(bookName, ReferenceBook.builder(bookName).build());

    List<AddressGroup> addressGroups =
        _prefixLists.values().stream()
            // prefix lists names contain region in them (e.g., "com.amazonaws.us-east-1.s3"), but
            // address group for a prefix list may already have been created via another account in
            // the region. we keep the original list in that case.
            .filter(plist -> !currentBook.getAddressGroup(plist.getPrefixListName()).isPresent())
            .filter(plist -> !plist.getCidrs().isEmpty())
            .map(
                plist -> {
                  Prefix cidr = plist.getCidrs().get(0);
                  // get a *.1 address (looks better than a *.0 address)
                  Ip representativeIp =
                      cidr.getPrefixLength() == Prefix.MAX_PREFIX_LENGTH
                          ? cidr.getStartIp()
                          : Ip.create(cidr.getStartIp().asLong() + 1);
                  return new AddressGroup(
                      ImmutableSortedSet.of(representativeIp.toString()),
                      plist.getPrefixListName());
                })
            .collect(ImmutableList.toImmutableList());

    if (!addressGroups.isEmpty()) { // no change needed if no new address groups were produced
      awsServicesNode
          .getGeneratedReferenceBooks()
          .put(
              bookName,
              ReferenceBook.builder(bookName)
                  .setAddressGroups(
                      Streams.concat(
                              currentBook.getAddressGroups().stream(), addressGroups.stream())
                          .collect(ImmutableList.toImmutableList()))
                  .build());
    }
  }

  @Nonnull
  Set<TransitGatewayRouteTable> getTransitGatewayRouteTables(String transitGatewayId) {
    return _transitGatewayRouteTables.values().stream()
        .filter(rt -> rt.getGatewayId().equals(transitGatewayId))
        .collect(ImmutableSet.toImmutableSet());
  }

  @Nonnull
  public Set<Address> getAddresses() {
    return ImmutableSet.copyOf(_addresses.values());
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
  public Set<LoadBalancer> getLoadBalancers() {
    return ImmutableSet.copyOf(_loadBalancers.values());
  }

  @Nonnull
  Map<String, LoadBalancer> getLoadBalancersMap() {
    return ImmutableMap.copyOf(_loadBalancers);
  }

  @Nonnull
  Map<String, LoadBalancerAttributes> getLoadBalancerAttributes() {
    return _loadBalancerAttributes;
  }

  @Nonnull
  Map<String, LoadBalancerListener> getLoadBalancerListeners() {
    return _loadBalancerListeners;
  }

  @Nullable
  public LoadBalancerListener getLoadBalancerListener(String loadBalancerArn) {
    return _loadBalancerListeners.get(loadBalancerArn);
  }

  @Nonnull
  Map<String, LoadBalancerTargetHealth> getLoadBalancerTargetHealths() {
    return _loadBalancerTargetHealths;
  }

  @Nullable
  public LoadBalancerTargetHealth getLoadBalancerTargetHealth(String targetGroupArn) {
    return _loadBalancerTargetHealths.get(targetGroupArn);
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
  public Map<String, NetworkAcl> getNetworkAcls() {
    return _networkAcls;
  }

  @Nonnull
  public Map<String, PrefixList> getPrefixLists() {
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
  public Map<String, Subnet> getSubnets() {
    return _subnets;
  }

  @Nonnull
  Map<String, TargetGroup> getTargetGroups() {
    return _targetGroups;
  }

  @Nullable
  public TargetGroup getTargetGroup(String targetGroupArn) {
    return _targetGroups.get(targetGroupArn);
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
  public Map<String, VpcEndpoint> getVpcEndpoints() {
    return _vpcEndpoints;
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

    updateSubnetAllocatedIps();

    for (Vpc vpc : getVpcs().values()) {
      Configuration cfgNode = vpc.toConfigurationNode(awsConfiguration, this, warnings);
      awsConfiguration.addNode(cfgNode);
    }

    for (ElasticsearchDomain elasticsearchDomain : getElasticSearchDomains().values()) {
      elasticsearchDomain
          .toConfigurationNodes(awsConfiguration, this, warnings)
          .forEach(
              cfgNode -> {
                cfgNode.setDeviceType(DeviceType.HOST);
                awsConfiguration.addNode(cfgNode);
              });
    }

    for (InternetGateway igw : getInternetGateways().values()) {
      Configuration cfgNode = igw.toConfigurationNode(awsConfiguration, this, warnings);
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

    for (LoadBalancer loadBalancer : getLoadBalancers()) {
      List<Configuration> cfgNodes =
          loadBalancer.toConfigurationNodes(awsConfiguration, this, warnings);
      cfgNodes.forEach(awsConfiguration::addNode);
    }

    for (VpcEndpoint vpcEndpoint : getVpcEndpoints().values()) {
      vpcEndpoint
          .toConfigurationNodes(awsConfiguration, this, warnings)
          .forEach(awsConfiguration::addNode);
    }

    // Must happen after ESD and RDS are converted (for security groups to get all referrer IPs) but
    // before subnets are converted (so that target instances' special interfaces can get the
    // instance's security groups applied; these interfaces are not covered by applySecurityGroups).
    // It's safe to evaluate security groups' IP spaces before converting subnets because all
    // interfaces created during subnet conversion either use link local addresses or are on subnet
    // nodes, whose interface addresses don't affect security group IP spaces.
    convertSecurityGroups(awsConfiguration, warnings);

    for (Subnet subnet : getSubnets().values()) {
      Configuration cfgNode = subnet.toConfigurationNode(awsConfiguration, this, warnings);
      awsConfiguration.addNode(cfgNode);
    }

    // VpcPeeringConnections and TransitGateways are processed in AwsConfiguration since they can be
    // cross region (or cross-account)

    // Should happen after all interfaces are created
    applySecurityGroups(awsConfiguration, warnings);

    // TODO: for now, set all interfaces to have the same bandwidth
    for (Configuration cfgNode : awsConfiguration.getAllNodes()) {
      for (Interface iface : cfgNode.getAllInterfaces().values()) {
        iface.setBandwidth(1E12d);
      }
    }
  }

  /** Convert security groups of all nodes to IpAccessLists and apply to all interfaces */
  private void applyNetworkInterfaceAclsToInstances(ConvertedConfiguration cfg) {
    for (NetworkInterface ni : _networkInterfaces.values()) {
      Optional<Configuration> configuration =
          Optional.ofNullable(ni.getAttachmentInstanceId()).map(cfg::getNode);
      if (!configuration.isPresent()) {
        continue;
      }
      Configuration c = configuration.get();
      Interface i = c.getAllInterfaces().get(ni.getId());
      if (i == null) {
        continue;
      }
      applyAclsToInterfaceBasedOnSecurityGroups(ni.getGroups(), c, i);
    }
  }

  /**
   * For applications (e.g., RDS or ElasticSearch), applies their security groups to their
   * interfaces.
   */
  private void applyApplicationSecurityGroups(ConvertedConfiguration cfg) {
    for (ElasticsearchDomain esd : _elasticsearchDomains.values()) {
      IntStream.range(0, esd.getInstanceCount())
          .forEach(
              instanceNum -> {
                Configuration c =
                    cfg.getNode(getNodeName(instanceNum, esd.getId(), esd.getVpcEndpoint()));
                if (c == null) {
                  return;
                }
                for (Interface i : c.getAllInterfaces().values()) {
                  applyAclsToInterfaceBasedOnSecurityGroups(esd.getSecurityGroups(), c, i);
                }
              });
    }
    for (RdsInstance rds : _rdsInstances.values()) {
      Configuration c = cfg.getNode(rds.getId());
      if (c == null) {
        continue;
      }
      for (Interface i : c.getAllInterfaces().values()) {
        applyAclsToInterfaceBasedOnSecurityGroups(rds.getSecurityGroups(), c, i);
      }
    }
  }

  public void applyAclsToInterfaceBasedOnSecurityGroups(
      Iterable<String> groups, Configuration c, Interface i) {
    // Sorted for stability across snapshots; order does not matter semantically and is not
    // consistent/preserved in AWS.
    Iterable<String> stableGroups = ImmutableSortedSet.copyOf(groups);
    applyIngressAcl(stableGroups, c, i);
    applyEgressAcl(stableGroups, c, i);
    // Set up reverse sessions for outbound traffic.
    i.setFirewallSessionInterfaceInfo(
        new FirewallSessionInterfaceInfo(
            Action.FORWARD_OUT_IFACE, ImmutableList.of(i.getName()), null, null));
    i.getVrf().setFirewallSessionVrfInfo(new FirewallSessionVrfInfo(false));
  }

  private void applyIngressAcl(Iterable<String> groups, Configuration c, Interface i) {
    // Create one AclAclLine to allow the flows matched in each security group.
    ImmutableList.Builder<AclLine> lines = ImmutableList.builder();
    for (String g : groups) {
      IpAccessList acl = _sgIngressAcls.get(g);
      if (acl == null || acl.getLines().isEmpty()) {
        // undefined (we already warned about this) or empty (we choose not to include).
        continue;
      }
      SecurityGroup sg = _securityGroups.get(g);
      assert sg != null; // or else acl would be null.
      c.getIpAccessLists().put(acl.getName(), acl);
      lines.add(
          new AclAclLine(
              String.format("Security Group %s", sg.getGroupName()),
              acl.getName(),
              getTraceElementForSecurityGroup(sg.getGroupName())));
    }

    IpAccessList inAcl =
        IpAccessList.builder()
            .setName(eniIngressAclName(i.getName()))
            .setLines(lines.build())
            .setOwner(c)
            .build();
    i.setIncomingFilter(inAcl);
  }

  private void applyEgressAcl(Iterable<String> groups, Configuration c, Interface i) {
    // Create one AclAclLine to allow the flows matched in each security group.
    ImmutableList.Builder<AclLine> lines = ImmutableList.builder();
    for (String g : groups) {
      IpAccessList acl = _sgEgressAcls.get(g);
      if (acl == null || acl.getLines().isEmpty()) {
        // undefined (we already warned about this) or empty (we choose not to include).
        continue;
      }
      SecurityGroup sg = _securityGroups.get(g);
      assert sg != null; // or else acl would be null.
      c.getIpAccessLists().put(acl.getName(), acl);
      lines.add(
          new AclAclLine(
              String.format("Security Group %s", sg.getGroupName()),
              acl.getName(),
              getTraceElementForSecurityGroup(sg.getGroupName())));
    }

    // egress ACL is spoofing protection plus egress SGs
    IpAccessList outAcl =
        IpAccessList.builder()
            .setName(eniEgressAclName(i.getName()))
            .setLines(
                ImmutableList.<AclLine>builder()
                    .add(computeAntiSpoofingFilter(i))
                    .addAll(lines.build())
                    .build())
            .setOwner(c)
            .build();
    i.setOutgoingFilter(outAcl);
  }

  @VisibleForTesting
  static AclLine computeAntiSpoofingFilter(Interface iface) {
    IpSpace validSourceIpSpace =
        IpWildcardSetIpSpace.builder()
            .including(
                iface.getAllConcreteAddresses().stream()
                    .map(addr -> IpWildcard.create(addr.getIp()))
                    .collect(Collectors.toList()))
            .build();
    return ExprAclLine.builder()
        .setTraceElement(DENY_SPOOFED_SOURCE_IP_TRACE_ELEMENT)
        .setMatchCondition(
            not(new MatchHeaderSpace(HeaderSpace.builder().setSrcIps(validSourceIpSpace).build())))
        .setAction(LineAction.DENY)
        .build();
  }

  /** Updates the Ips which have been allocated already in subnets of all interfaces. */
  private void updateSubnetAllocatedIps() {
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

  /**
   * Describes the given {@link NetworkInterface}. If the interface is attached to an {@link
   * Instance}, the instance name will be reflected in the result.
   */
  private @Nonnull String getDescriptionForNetworkInterface(NetworkInterface ni) {
    Optional<Instance> attachedInstance =
        Optional.ofNullable(ni.getAttachmentInstanceId()).map(_instances::get);
    return attachedInstance
        .map(instance -> ni.getHumanName() + " on " + instance.getHumanName())
        .orElseGet(ni::getHumanName);
  }

  @VisibleForTesting
  void convertSecurityGroups(ConvertedConfiguration awsConfiguration, Warnings warnings) {
    // Finalize security groups by adding referrer IPs from all interfaces (real and generated), so
    // that the security-group-as-IpSpace is correct, then convert security groups to ACLs.
    addNetworkInterfaceIpsToSecurityGroups(warnings);
    addApplicationInterfaceIpsToSecurityGroups(awsConfiguration, warnings);
    _sgIngressAcls =
        _securityGroups.entrySet().stream()
            .collect(
                ImmutableMap.toImmutableMap(
                    Entry::getKey, e -> e.getValue().toAcl(this, true, warnings)));
    _sgEgressAcls =
        _securityGroups.entrySet().stream()
            .collect(
                ImmutableMap.toImmutableMap(
                    Entry::getKey, e -> e.getValue().toAcl(this, false, warnings)));
  }

  @VisibleForTesting
  void applySecurityGroups(ConvertedConfiguration awsConfiguration, Warnings warnings) {
    // Apply the correct security groups to all interfaces (real and generated).
    applyNetworkInterfaceAclsToInstances(awsConfiguration);
    applyApplicationSecurityGroups(awsConfiguration);
  }

  /** Adds all private IPs for {@code ni} as referred IPs to all security groups in use. */
  private void addNetworkInterfaceIpsToSecurityGroups(NetworkInterface ni, Warnings warnings) {
    String description = getDescriptionForNetworkInterface(ni);
    for (String sgName : ni.getGroups()) {
      SecurityGroup sg = _securityGroups.get(sgName);
      if (sg == null) {
        warnings.pedantic(
            String.format("Security group \"%s\" for \"%s\" not found", sgName, description));
        continue;
      }
      sg.addReferrerIps(ni.getPrivateIpAddresses(), description);
    }
  }

  /**
   * Updates the set of IP addresses that a security group holds (corresponding to all the enis) in
   * that group.
   */
  @VisibleForTesting
  void addNetworkInterfaceIpsToSecurityGroups(Warnings warnings) {
    for (NetworkInterface networkInterface : _networkInterfaces.values()) {
      addNetworkInterfaceIpsToSecurityGroups(networkInterface, warnings);
    }
  }

  /**
   * Updates the set of IP addresses that a security group holds for all application (RDS,
   * Elasticsearch, etc.) instances in that group.
   */
  @VisibleForTesting
  void addApplicationInterfaceIpsToSecurityGroups(ConvertedConfiguration cfg, Warnings warnings) {
    for (ElasticsearchDomain esd : _elasticsearchDomains.values()) {
      Configuration c = cfg.getNode(esd.getId());
      if (c == null) {
        continue;
      }
      Set<Ip> ips =
          c.getAllInterfaces().values().stream()
              .flatMap(i -> i.getAllConcreteAddresses().stream())
              .map(ConcreteInterfaceAddress::getIp)
              .collect(ImmutableSet.toImmutableSet());
      for (String sgName : esd.getSecurityGroups()) {
        SecurityGroup sg = _securityGroups.get(sgName);
        if (sg == null) {
          continue;
        }
        ips.forEach(
            ip -> sg.addReferrerIp(ip, String.format("Elasticsearch domain %s", esd.getId())));
      }
    }

    for (RdsInstance rds : _rdsInstances.values()) {
      Configuration c = cfg.getNode(rds.getId());
      if (c == null) {
        continue;
      }
      Set<Ip> ips =
          c.getAllInterfaces().values().stream()
              .flatMap(i -> i.getAllConcreteAddresses().stream())
              .map(ConcreteInterfaceAddress::getIp)
              .collect(ImmutableSet.toImmutableSet());
      for (String sgName : rds.getSecurityGroups()) {
        SecurityGroup sg = _securityGroups.get(sgName);
        if (sg == null) {
          continue;
        }
        ips.forEach(ip -> sg.addReferrerIp(ip, String.format("RDS database %s", rds.getId())));
      }
    }
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

  public static final class RegionBuilder {
    private Map<String, Address> _addresses;
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
    private Map<String, VpcEndpoint> _vpcEndpoints;
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

    public RegionBuilder setVpcEndpoints(Map<String, VpcEndpoint> vpcEndpoints) {
      _vpcEndpoints = vpcEndpoints;
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
          firstNonNull(_vpcEndpoints, ImmutableMap.of()),
          firstNonNull(_vpcPeerings, ImmutableMap.of()),
          firstNonNull(_vpcs, ImmutableMap.of()),
          firstNonNull(_vpnConnections, ImmutableMap.of()),
          firstNonNull(_vpnGateways, ImmutableMap.of()));
    }
  }
}
