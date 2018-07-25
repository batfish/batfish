package org.batfish.representation.aws;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Ip;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

public class NatGateway implements AwsVpcEntity, Serializable {

  private static final long serialVersionUID = 1L;

  // we ignore the state and tags fields

  private List<NatGatewayAddress> _natGatewayAddresses = new LinkedList<>();

  private String _natGatewayId;

  private String _subnetId;

  private String _vpcId;

  public NatGateway(JSONObject jObj) throws JSONException {
    _natGatewayId = jObj.getString(JSON_KEY_NAT_GATEWAY_ID);
    _subnetId = jObj.getString(JSON_KEY_SUBNET_ID);
    _vpcId = jObj.getString(JSON_KEY_VPC_ID);

    JSONArray addresses = jObj.getJSONArray(JSON_KEY_NAT_GATEWAY_ADDRESSES);
    for (int index = 0; index < addresses.length(); index++) {
      JSONObject childObject = addresses.getJSONObject(index);
      String allocationId = childObject.getString(JSON_KEY_ALLOCATION_ID);
      String networkInterfaceId = childObject.getString(JSON_KEY_NETWORK_INTERFACE_ID);
      Ip privateIp = new Ip(childObject.getString(JSON_KEY_PRIVATE_IP));
      Ip publicIp = new Ip(childObject.getString(JSON_KEY_PUBLIC_IP));
      _natGatewayAddresses.add(
          new NatGatewayAddress(allocationId, networkInterfaceId, privateIp, publicIp));
    }
  }

  @Override
  public String getId() {
    return _natGatewayId;
  }

  public List<NatGatewayAddress> getNatGatewayAddresses() {
    return _natGatewayAddresses;
  }

  public String getSubnetId() {
    return _subnetId;
  }

  public String getVpcId() {
    return _vpcId;
  }

  public Configuration toConfigurationNode(
      AwsConfiguration awsConfiguration, Region region, Warnings warnings) {
    Configuration cfgNode = Utils.newAwsConfiguration(_natGatewayId, "aws");
    cfgNode.getVendorFamily().getAws().setRegion(region.getName());

    // TODO: Configure forwarding for this NAT
    //    for (NatGatewayAddress natAddress : _natGatewayAddresses) {
    // foreach natgatewayaddress create interfaces for public and private IPs, configure NAT rules
    // also connect the nat to the VPC router
    //    }

    return cfgNode;
  }
}
