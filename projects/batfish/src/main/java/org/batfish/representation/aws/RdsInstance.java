package org.batfish.representation.aws;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimap;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.InterfaceAddress;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.StaticRoute;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

public class RdsInstance implements AwsVpcEntity, Serializable {

  public enum Status {
    AVAILABLE,
    UNAVAILABLE
  }

  private static final long serialVersionUID = 1L;

  private String _dbInstanceIdentifier;

  private Status _dbInstanceStatus = Status.UNAVAILABLE;

  private ListMultimap<String, String> _azsSubnetIds;

  private String _availabilityZone;

  private String _vpcId;

  private boolean _multiAz;

  private List<String> _securityGroups;

  public RdsInstance(JSONObject jObj) throws JSONException {
    _azsSubnetIds = ArrayListMultimap.create();
    _securityGroups = new LinkedList<>();
    _dbInstanceIdentifier = jObj.getString(JSON_KEY_DB_INSTANCE_IDENTIFIER);
    _availabilityZone = jObj.getString("AvailabilityZone");
    _vpcId = jObj.getJSONObject(JSON_KEY_DB_SUBNET_GROUP).getString(JSON_KEY_VPC_ID);
    _multiAz = jObj.getBoolean(JSON_KEY_MULTI_AZ);
    if (jObj.getString(JSON_KEY_DB_INSTANCE_STATUS).equalsIgnoreCase("available")) {
      _dbInstanceStatus = Status.AVAILABLE;
    }
    initSubnets(jObj.getJSONObject(JSON_KEY_DB_SUBNET_GROUP).getJSONArray(JSON_KEY_SUBNETS));
    initSecurityGroups(jObj.getJSONArray(JSON_KEY_VPC_SECURITY_GROUPS));
  }

  public static long getSerialVersionUID() {
    return serialVersionUID;
  }

  @Override
  public String getId() {
    return _dbInstanceIdentifier;
  }

  public Multimap<String, String> getAzSubnetIds() {
    return _azsSubnetIds;
  }

  public String getVpcId() {
    return _vpcId;
  }

  public boolean getMultiAz() {
    return _multiAz;
  }

  public String getAvailabilityZone() {
    return _availabilityZone;
  }

  public List<String> getSecurityGroups() {
    return _securityGroups;
  }

  public Status getDbInstanceStatus() {
    return _dbInstanceStatus;
  }

  private void initSecurityGroups(JSONArray securityGroupsArray) throws JSONException {
    for (int index = 0; index < securityGroupsArray.length(); index++) {
      JSONObject securityGroup = securityGroupsArray.getJSONObject(index);
      if (securityGroup.getString(JSON_KEY_STATUS).equalsIgnoreCase("active")) {
        _securityGroups.add(securityGroup.getString(JSON_KEY_VPC_SECURITY_GROUP_ID));
      }
    }
  }

  private void initSubnets(JSONArray subnetsArray) throws JSONException {
    for (int i = 0; i < subnetsArray.length(); i++) {
      JSONObject subnet = subnetsArray.getJSONObject(i);
      if (subnet.getString(JSON_KEY_SUBNET_STATUS).equalsIgnoreCase("active")) {
        _azsSubnetIds.put(
            subnet.getJSONObject(JSON_KEY_SUBNET_AVAILABILITY_ZONE).getString("Name"),
            subnet.getString(JSON_KEY_SUBNET_IDENTIFIER));
      }
    }
  }

  public Configuration toConfigurationNode(
      AwsConfiguration awsVpcConfig, Region region, Warnings warnings) {
    Configuration cfgNode = Utils.newAwsConfiguration(_dbInstanceIdentifier, "aws");

    cfgNode.getVendorFamily().getAws().setVpcId(_vpcId);
    cfgNode.getVendorFamily().getAws().setRegion(region.getName());

    // get subnets for the availability zone set for this instance
    List<String> subnets = _azsSubnetIds.get(_availabilityZone);

    // create an interface per subnet
    for (String subnetId : subnets) {
      Subnet subnet = region.getSubnets().get(subnetId);
      if (subnet == null) {
        warnings.redFlag(
            String.format(
                "Subnet \"%s\" for RDS instance \"%s\" not found",
                subnetId, _dbInstanceIdentifier));
        continue;
      }

      String instancesIfaceName = String.format("%s-%s", _dbInstanceIdentifier, subnetId);
      Ip instancesIfaceIp = subnet.getNextIp();
      InterfaceAddress instancesIfaceAddress =
          new InterfaceAddress(instancesIfaceIp, subnet.getCidrBlock().getPrefixLength());
      Utils.newInterface(instancesIfaceName, cfgNode, instancesIfaceAddress);

      Ip defaultGatewayAddress = subnet.computeInstancesIfaceIp();
      StaticRoute defaultRoute =
          StaticRoute.builder()
              .setAdministrativeCost(Route.DEFAULT_STATIC_ROUTE_ADMIN)
              .setMetric(Route.DEFAULT_STATIC_ROUTE_COST)
              .setNextHopIp(defaultGatewayAddress)
              .setNetwork(Prefix.ZERO)
              .build();
      cfgNode.getDefaultVrf().getStaticRoutes().add(defaultRoute);
    }

    Utils.processSecurityGroups(region, cfgNode, _securityGroups, warnings);

    return cfgNode;
  }
}
