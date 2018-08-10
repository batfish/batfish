package org.batfish.representation.aws;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import org.batfish.common.Pair;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.InterfaceAddress;
import org.batfish.datamodel.StaticRoute;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

public class InternetGateway implements AwsVpcEntity, Serializable {

  private static final long serialVersionUID = 1L;

  private List<String> _attachmentVpcIds = new LinkedList<>();

  private String _internetGatewayId;

  public InternetGateway(JSONObject jObj) throws JSONException {
    _internetGatewayId = jObj.getString(JSON_KEY_INTERNET_GATEWAY_ID);

    JSONArray attachments = jObj.getJSONArray(JSON_KEY_ATTACHMENTS);
    for (int index = 0; index < attachments.length(); index++) {
      JSONObject childObject = attachments.getJSONObject(index);
      _attachmentVpcIds.add(childObject.getString(JSON_KEY_VPC_ID));
    }
  }

  @Override
  public String getId() {
    return _internetGatewayId;
  }

  public Configuration toConfigurationNode(AwsConfiguration awsConfiguration, Region region) {
    Configuration cfgNode = Utils.newAwsConfiguration(_internetGatewayId, "aws");
    cfgNode.getVendorFamily().getAws().setRegion(region.getName());

    for (String vpcId : _attachmentVpcIds) {

      String igwIfaceName = vpcId;
      Pair<InterfaceAddress, InterfaceAddress> igwAddresses =
          awsConfiguration.getNextGeneratedLinkSubnet();
      InterfaceAddress igwIfaceAddress = igwAddresses.getFirst();
      Utils.newInterface(igwIfaceName, cfgNode, igwIfaceAddress);

      // add the interface to the vpc router
      Configuration vpcConfigNode = awsConfiguration.getConfigurationNodes().get(vpcId);
      String vpcIfaceName = _internetGatewayId;
      InterfaceAddress vpcIfaceAddress = igwAddresses.getSecond();
      Utils.newInterface(vpcIfaceName, vpcConfigNode, vpcIfaceAddress);

      // associate this gateway with the vpc
      region.getVpcs().get(vpcId).setInternetGatewayId(_internetGatewayId);

      // add a route on the gateway to the vpc
      Vpc vpc = region.getVpcs().get(vpcId);
      vpc.getCidrBlockAssociations()
          .forEach(
              prefix -> {
                StaticRoute igwVpcRoute =
                    StaticRoute.builder()
                        .setNetwork(prefix)
                        .setNextHopIp(vpcIfaceAddress.getIp())
                        .setAdministrativeCost(Route.DEFAULT_STATIC_ROUTE_ADMIN)
                        .setMetric(Route.DEFAULT_STATIC_ROUTE_COST)
                        .build();
                cfgNode.getDefaultVrf().getStaticRoutes().add(igwVpcRoute);
              });
    }

    return cfgNode;
  }
}
