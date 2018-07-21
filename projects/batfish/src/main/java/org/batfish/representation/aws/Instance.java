package org.batfish.representation.aws;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSortedSet;
import java.io.Serializable;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import org.batfish.common.BatfishException;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.InterfaceAddress;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.StaticRoute;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

public class Instance implements AwsVpcEntity, Serializable {

  public enum Status {
    PENDING("pending"),
    RUNNING("running"),
    SHUTTING_DOWN("shutting-down"),
    TERMINATED("terminated"),
    STOPPING("stopping"),
    STOPPED("stopped");

    private static final Map<String, Status> MAP = initMap();

    @JsonCreator
    public static Status fromString(String name) {
      Status value = MAP.get(name.toLowerCase());
      if (value == null) {
        throw new BatfishException(
            "No " + Status.class.getSimpleName() + " with name: '" + name + "'");
      }
      return value;
    }

    private static Map<String, Status> initMap() {
      ImmutableMap.Builder<String, Status> map = ImmutableMap.builder();
      for (Status value : Status.values()) {
        String name = value._name.toLowerCase();
        map.put(name, value);
      }
      return map.build();
    }

    private final String _name;

    Status(String name) {
      _name = name;
    }

    @JsonValue
    public String getName() {
      return _name;
    }
  }

  private static final long serialVersionUID = 1L;

  private final String _instanceId;

  private final List<String> _networkInterfaces;

  private final List<String> _securityGroups;

  private final Status _status;

  private final String _subnetId;

  private final Map<String, String> _tags;

  private final String _vpcId;

  public Instance(JSONObject jObj) throws JSONException {
    _securityGroups = new LinkedList<>();
    _networkInterfaces = new LinkedList<>();
    _instanceId = jObj.getString(JSON_KEY_INSTANCE_ID);

    boolean hasVpcId = jObj.has(JSON_KEY_VPC_ID);
    _vpcId = hasVpcId ? jObj.getString(JSON_KEY_VPC_ID) : null;
    _subnetId = hasVpcId ? jObj.getString(JSON_KEY_SUBNET_ID) : null;

    JSONArray securityGroups = jObj.getJSONArray(JSON_KEY_SECURITY_GROUPS);
    initSecurityGroups(securityGroups);

    JSONArray networkInterfaces = jObj.getJSONArray(JSON_KEY_NETWORK_INTERFACES);
    initNetworkInterfaces(networkInterfaces);

    _tags = new HashMap<>();
    JSONArray tagArray = jObj.getJSONArray(JSON_KEY_TAGS);
    for (int index = 0; index < tagArray.length(); index++) {
      JSONObject childObject = tagArray.getJSONObject(index);
      _tags.put(childObject.getString("Key"), childObject.getString("Value"));
    }

    String stateName = jObj.getJSONObject(JSON_KEY_STATE).getString("Name");
    _status = Status.fromString(stateName);

    // check if the public and private ip addresses are associated with an
    // interface
  }

  @Override
  public String getId() {
    return _instanceId;
  }

  public String getInstanceId() {
    return _instanceId;
  }

  public List<String> getNetworkInterfaces() {
    return _networkInterfaces;
  }

  public List<String> getSecurityGroups() {
    return _securityGroups;
  }

  public Status getStatus() {
    return _status;
  }

  public String getSubnetId() {
    return _subnetId;
  }

  public String getVpcId() {
    return _vpcId;
  }

  private void initNetworkInterfaces(JSONArray routes) throws JSONException {

    for (int index = 0; index < routes.length(); index++) {
      JSONObject childObject = routes.getJSONObject(index);
      _networkInterfaces.add(childObject.getString(JSON_KEY_NETWORK_INTERFACE_ID));
    }
  }

  private void initSecurityGroups(JSONArray associations) throws JSONException {

    for (int index = 0; index < associations.length(); index++) {
      JSONObject childObject = associations.getJSONObject(index);
      _securityGroups.add(childObject.getString(JSON_KEY_GROUP_ID));
    }
  }

  public Configuration toConfigurationNode(Region region, Warnings warnings) {
    String name = _tags.getOrDefault("Name", _instanceId);
    Configuration cfgNode = Utils.newAwsConfiguration(name, "aws");

    for (String interfaceId : _networkInterfaces) {

      NetworkInterface netInterface = region.getNetworkInterfaces().get(interfaceId);
      if (netInterface == null) {
        warnings.redFlag(
            String.format(
                "Network interface \"%s\" for instance \"%s\" not found",
                interfaceId, _instanceId));
        continue;
      }

      ImmutableSortedSet.Builder<InterfaceAddress> ifaceAddressesBuilder =
          new ImmutableSortedSet.Builder<>(Comparator.naturalOrder());

      Subnet subnet = region.getSubnets().get(netInterface.getSubnetId());
      Prefix ifaceSubnet = subnet.getCidrBlock();
      Ip defaultGatewayAddress = subnet.computeInstancesIfaceIp();
      StaticRoute defaultRoute =
          StaticRoute.builder()
              .setAdministrativeCost(Route.DEFAULT_STATIC_ROUTE_ADMIN)
              .setMetric(Route.DEFAULT_STATIC_ROUTE_COST)
              .setNextHopIp(defaultGatewayAddress)
              .setNetwork(Prefix.ZERO)
              .build();
      cfgNode.getDefaultVrf().getStaticRoutes().add(defaultRoute);

      for (Ip ip : netInterface.getIpAddressAssociations().keySet()) {
        if (!ifaceSubnet.containsIp(ip)) {
          warnings.pedantic(
              String.format(
                  "Instance subnet \"%s\" does not contain private ip: \"%s\"", ifaceSubnet, ip));
          continue;
        }

        if (ip.equals(ifaceSubnet.getEndIp())) {
          warnings.pedantic(
              String.format("Expected end address \"%s\" to be used by generated subnet node", ip));
          continue;
        }

        InterfaceAddress address = new InterfaceAddress(ip, ifaceSubnet.getPrefixLength());
        ifaceAddressesBuilder.add(address);
      }
      SortedSet<InterfaceAddress> ifaceAddresses = ifaceAddressesBuilder.build();
      Interface iface = Utils.newInterface(interfaceId, cfgNode, ifaceAddresses.first());
      iface.setAllAddresses(ifaceAddresses);

      cfgNode.getVendorFamily().getAws().setVpcId(_vpcId);
      cfgNode.getVendorFamily().getAws().setSubnetId(_subnetId);
      cfgNode.getVendorFamily().getAws().setRegion(region.getName());
    }

    Utils.processSecurityGroups(region, cfgNode, _securityGroups, warnings);

    return cfgNode;
  }
}
