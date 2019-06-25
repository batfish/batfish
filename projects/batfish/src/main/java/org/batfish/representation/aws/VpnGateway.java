package org.batfish.representation.aws;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import org.batfish.common.Warnings;
import org.batfish.datamodel.ConcreteInterfaceAddress;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.StaticRoute;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

public class VpnGateway implements AwsVpcEntity, Serializable {

  private List<String> _attachmentVpcIds = new LinkedList<>();

  private String _vpnGatewayId;

  public VpnGateway(JSONObject jObj) throws JSONException {
    _vpnGatewayId = jObj.getString(JSON_KEY_VPN_GATEWAY_ID);

    JSONArray attachments = jObj.getJSONArray(JSON_KEY_VPC_ATTACHMENTS);
    for (int index = 0; index < attachments.length(); index++) {
      JSONObject childObject = attachments.getJSONObject(index);
      _attachmentVpcIds.add(childObject.getString(JSON_KEY_VPC_ID));
    }
  }

  public List<String> getAttachmentVpcIds() {
    return _attachmentVpcIds;
  }

  @Override
  public String getId() {
    return _vpnGatewayId;
  }

  public Configuration toConfigurationNode(
      AwsConfiguration awsConfiguration, Region region, Warnings warnings) {
    Configuration cfgNode = Utils.newAwsConfiguration(_vpnGatewayId, "aws");
    cfgNode.getVendorFamily().getAws().setRegion(region.getName());

    for (String vpcId : _attachmentVpcIds) {

      String vgwIfaceName = vpcId;
      Prefix vpcLink = awsConfiguration.getNextGeneratedLinkSubnet();
      ConcreteInterfaceAddress vgwIfaceAddress =
          ConcreteInterfaceAddress.create(vpcLink.getStartIp(), vpcLink.getPrefixLength());
      Utils.newInterface(vgwIfaceName, cfgNode, vgwIfaceAddress);

      // add the interface to the vpc router
      Configuration vpcConfigNode = awsConfiguration.getConfigurationNodes().get(vpcId);
      String vpcIfaceName = _vpnGatewayId;
      Interface vpcIface = new Interface(vpcIfaceName, vpcConfigNode);
      ConcreteInterfaceAddress vpcIfaceAddress =
          ConcreteInterfaceAddress.create(vpcLink.getEndIp(), vpcLink.getPrefixLength());
      vpcIface.setAddress(vpcIfaceAddress);
      Utils.newInterface(vpcIfaceName, vpcConfigNode, vpcIfaceAddress);

      // associate this gateway with the vpc
      region.getVpcs().get(vpcId).setVpnGatewayId(_vpnGatewayId);

      // add a route on the gateway to the vpc
      Vpc vpc = region.getVpcs().get(vpcId);
      vpc.getCidrBlockAssociations()
          .forEach(
              prefix -> {
                StaticRoute vgwVpcRoute =
                    StaticRoute.builder()
                        .setNetwork(prefix)
                        .setNextHopIp(vpcIfaceAddress.getIp())
                        .setAdministrativeCost(Route.DEFAULT_STATIC_ROUTE_ADMIN)
                        .setMetric(Route.DEFAULT_STATIC_ROUTE_COST)
                        .build();
                cfgNode.getDefaultVrf().getStaticRoutes().add(vgwVpcRoute);
              });
    }

    return cfgNode;
  }
}
