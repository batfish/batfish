package org.batfish.representation.aws_vpcs;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import org.batfish.common.BatfishLogger;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.batfish.representation.Configuration;
import org.batfish.representation.Interface;
import org.batfish.representation.StaticRoute;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

public class VpnGateway implements AwsVpcEntity, Serializable {

   private static final long serialVersionUID = 1L;

   private List<String> _attachmentVpcIds = new LinkedList<String>();

   private String _vpnGatewayId;

   public VpnGateway(JSONObject jObj, BatfishLogger logger)
         throws JSONException {
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
         AwsVpcConfiguration awsVpcConfiguration) {
      Configuration cfgNode = new Configuration(_vpnGatewayId);

      for (String vpcId : _attachmentVpcIds) {

         Interface vgwIface = new Interface(vpcId, cfgNode);
         Prefix vgwIfacePrefix = awsVpcConfiguration
               .getNextGeneratedLinkSubnet();
         vgwIface.setPrefix(vgwIfacePrefix);
         cfgNode.getInterfaces().put(vgwIface.getName(), vgwIface);

         // add the interface to the vpc router
         Configuration vpcConfigNode = awsVpcConfiguration
               .getConfigurationNodes().get(vpcId);
         Interface vpcIface = new Interface(_vpnGatewayId, vpcConfigNode);
         Ip vpcIfaceIp = vgwIfacePrefix.getEndAddress();
         Prefix vpcIfacePrefix = new Prefix(vpcIfaceIp,
               vgwIfacePrefix.getPrefixLength());
         vpcIface.setPrefix(vpcIfacePrefix);
         vpcConfigNode.getInterfaces().put(vpcIface.getName(), vpcIface);

         // associate this gateway with the vpc
         awsVpcConfiguration.getVpcs().get(vpcId)
               .setVpnGatewayId(_vpnGatewayId);

         // add a route on the gateway to the vpc
         Vpc vpc = awsVpcConfiguration.getVpcs().get(vpcId);
         StaticRoute vgwVpcRoute = new StaticRoute(vpc.getCidrBlock(),
               vpcIfaceIp, null, Route.DEFAULT_STATIC_ROUTE_ADMIN,
               Route.DEFAULT_STATIC_ROUTE_COST);
         cfgNode.getStaticRoutes().add(vgwVpcRoute);
      }

      return cfgNode;
   }
}
