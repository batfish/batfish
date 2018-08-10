package org.batfish.representation.aws;

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

public class ElasticsearchDomain implements AwsVpcEntity, Serializable {

  private static final long serialVersionUID = 1L;

  private List<String> _securityGroups;

  private String _domainName;

  private String _vpcId;

  private List<String> _subnets;

  private boolean _available;

  public boolean getAvailable() {
    return _available;
  }

  @Override
  public String getId() {
    return _domainName;
  }

  public List<String> getSecurityGroups() {
    return _securityGroups;
  }

  public List<String> getSubnets() {
    return _subnets;
  }

  public String getVpcId() {
    return _vpcId;
  }

  public ElasticsearchDomain(JSONObject jObj) throws JSONException {
    _securityGroups = new LinkedList<>();
    _subnets = new LinkedList<>();
    _domainName = jObj.getString(JSON_KEY_DOMAIN_NAME);
    if (jObj.has(JSON_KEY_VPC_OPTIONS)) {
      initVpcOptions(jObj.getJSONObject(JSON_KEY_VPC_OPTIONS));
    }
    if (jObj.getBoolean(JSON_KEY_CREATED) && !jObj.getBoolean(JSON_KEY_DELETED)) {
      _available = true;
    }
  }

  private void initVpcOptions(JSONObject vpcOptions) throws JSONException {
    _vpcId = vpcOptions.getString(JSON_KEY_ES_VPC_ID);
    JSONArray securityGroupIds = vpcOptions.getJSONArray(JSON_KEY_SECURITY_GROUP_IDS);
    for (int i = 0; i < securityGroupIds.length(); i++) {
      _securityGroups.add(securityGroupIds.getString(i));
    }
    JSONArray subnetIds = vpcOptions.getJSONArray(JSON_KEY_SUBNET_IDS);
    for (int i = 0; i < subnetIds.length(); i++) {
      _subnets.add(subnetIds.getString(i));
    }
  }

  public Configuration toConfigurationNode(
      AwsConfiguration awsVpcConfig, Region region, Warnings warnings) {
    Configuration cfgNode = Utils.newAwsConfiguration(_domainName, "aws");

    cfgNode.getVendorFamily().getAws().setVpcId(_vpcId);
    cfgNode.getVendorFamily().getAws().setRegion(region.getName());

    // create an interface per subnet
    for (String subnetId : _subnets) {
      Subnet subnet = region.getSubnets().get(subnetId);
      if (subnet == null) {
        warnings.redFlag(
            String.format(
                "Subnet \"%s\" for Elasticsearch domain \"%s\" not found", subnetId, _domainName));
        continue;
      }
      String instancesIfaceName = String.format("%s-%s", _domainName, subnetId);
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
