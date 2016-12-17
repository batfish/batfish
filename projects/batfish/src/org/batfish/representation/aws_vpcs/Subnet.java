package org.batfish.representation.aws_vpcs;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.batfish.common.BatfishException;
import org.batfish.common.BatfishLogger;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.StaticRoute;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

public class Subnet implements AwsVpcEntity, Serializable {

   private static final long serialVersionUID = 1L;

   private Prefix _cidrBlock;

   private transient String _internetGatewayId;

   private String _subnetId;

   private String _vpcId;

   private transient String _vpnGatewayId;

   public Subnet(JSONObject jObj, BatfishLogger logger) throws JSONException {
      _cidrBlock = new Prefix(jObj.getString(JSON_KEY_CIDR_BLOCK));
      _subnetId = jObj.getString(JSON_KEY_SUBNET_ID);
      _vpcId = jObj.getString(JSON_KEY_VPC_ID);
   }

   private NetworkAcl findMyNetworkAcl(Map<String, NetworkAcl> networkAcls) {

      NetworkAcl myNetworkAcl = null;

      for (String networkAclId : networkAcls.keySet()) {

         NetworkAcl networkAcl = networkAcls.get(networkAclId);

         // ignore if the route table is not for the same VPC
         if (!networkAcl.getVpcId().equals(_vpcId)) {
            continue;
         }

         List<NetworkAclAssociation> naAssocs = networkAcl.getAssociations();

         for (NetworkAclAssociation naAssoc : naAssocs) {
            if (_subnetId.equals(naAssoc.getSubnetId())) {
               if (myNetworkAcl != null) {
                  throw new BatfishException(
                        "Found two associated network acls ("
                              + networkAcl.getId() + ", " + myNetworkAcl.getId()
                              + " for subnet " + _subnetId);
               }

               myNetworkAcl = networkAcl;
            }
         }
      }

      return myNetworkAcl;
   }

   private RouteTable findMyRouteTable(Map<String, RouteTable> routeTables) {

      RouteTable myRouteTable = null;
      RouteTable mainRouteTable = null;

      for (String routeTableId : routeTables.keySet()) {

         RouteTable routeTable = routeTables.get(routeTableId);

         // ignore if the route table is not for the same VPC
         if (!routeTable.getVpcId().equals(_vpcId)) {
            continue;
         }

         List<RouteTableAssociation> rtAssocs = routeTable.getAssociations();

         for (RouteTableAssociation rtAssoc : rtAssocs) {
            if (_subnetId.equals(rtAssoc.getSubnetId())) {
               if (myRouteTable != null) {
                  throw new BatfishException(
                        "Found two associated route tables ("
                              + routeTable.getId() + ", " + myRouteTable.getId()
                              + " for subnet " + _subnetId);
               }

               myRouteTable = routeTable;
            }

            if (rtAssoc.isMain()) {
               if (mainRouteTable != null) {
                  throw new BatfishException("Found two main route tables ("
                        + routeTable.getId() + ", " + mainRouteTable.getId()
                        + " for subnet " + _subnetId);
               }

               mainRouteTable = routeTable;

            }
         }
      }

      if (myRouteTable == null) {
         myRouteTable = mainRouteTable;
      }

      return myRouteTable;
   }

   public Prefix getCidrBlock() {
      return _cidrBlock;
   }

   @Override
   public String getId() {
      return _subnetId;
   }

   public String getInternetGatewayId() {
      return _internetGatewayId;
   }

   public String getVpcId() {
      return _vpcId;
   }

   public String getVpnGatewayId() {
      return _vpnGatewayId;
   }

   public Configuration toConfigurationNode(
         AwsVpcConfiguration awsVpcConfiguration) {
      Configuration cfgNode = new Configuration(_subnetId);

      // add one interface that faces the instances
      Interface instancesIface = new Interface(_subnetId, cfgNode);
      cfgNode.getDefaultVrf().getInterfaces().put(_subnetId, instancesIface);
      Prefix instancesIfacePrefix = new Prefix(_cidrBlock.getEndAddress(),
            _cidrBlock.getPrefixLength());
      instancesIface.setPrefix(instancesIfacePrefix);
      instancesIface.getAllPrefixes().add(instancesIfacePrefix);

      // generate a prefix for the link between the VPC router and the subnet
      Prefix vpcSubnetLinkPrefix = awsVpcConfiguration
            .getNextGeneratedLinkSubnet();
      Prefix subnetIfacePrefix = vpcSubnetLinkPrefix;
      Prefix vpcIfacePrefix = new Prefix(vpcSubnetLinkPrefix.getEndAddress(),
            vpcSubnetLinkPrefix.getPrefixLength());

      // add an interface that faces the VPC router
      Interface subnetIface = new Interface(_vpcId, cfgNode);
      cfgNode.getDefaultVrf().getInterfaces().put(subnetIface.getName(),
            subnetIface);
      subnetIface.getAllPrefixes().add(subnetIfacePrefix);
      subnetIface.setPrefix(subnetIfacePrefix);

      // add the interface to the vpc router
      Configuration vpcConfigNode = awsVpcConfiguration.getConfigurationNodes()
            .get(_vpcId);
      Interface vpcIface = new Interface(_subnetId, vpcConfigNode);
      vpcConfigNode.getDefaultVrf().getInterfaces().put(vpcIface.getName(),
            vpcIface);
      vpcIface.getAllPrefixes().add(vpcIfacePrefix);
      vpcIface.setPrefix(vpcIfacePrefix);
      // add a static route on the vpc router for this subnet
      StaticRoute vpcToSubnetRoute = new StaticRoute(_cidrBlock,
            subnetIfacePrefix.getAddress(), null,
            Route.DEFAULT_STATIC_ROUTE_ADMIN, Route.DEFAULT_STATIC_ROUTE_COST);
      vpcConfigNode.getDefaultVrf().getStaticRoutes().add(vpcToSubnetRoute);

      // attach to igw if it exists
      _internetGatewayId = awsVpcConfiguration.getVpcs().get(_vpcId)
            .getInternetGatewayId();
      Ip igwAddress = null;
      if (_internetGatewayId != null) {
         // generate a prefix for the link between the igw and the subnet
         Prefix igwSubnetLinkPrefix = awsVpcConfiguration
               .getNextGeneratedLinkSubnet();
         Prefix subnetIgwIfacePrefix = igwSubnetLinkPrefix;
         Prefix igwSubnetIfacePrefix = new Prefix(
               igwSubnetLinkPrefix.getEndAddress(),
               igwSubnetLinkPrefix.getPrefixLength());

         // add an interface that faces the igw
         Interface subnetIgwIface = new Interface(_internetGatewayId, cfgNode);
         cfgNode.getDefaultVrf().getInterfaces().put(_internetGatewayId,
               subnetIgwIface);
         subnetIgwIface.getAllPrefixes().add(subnetIgwIfacePrefix);
         subnetIgwIface.setPrefix(subnetIgwIfacePrefix);

         // add an interface to the igw facing the subnet
         Configuration igwConfigNode = awsVpcConfiguration
               .getConfigurationNodes().get(_internetGatewayId);
         Interface igwSubnetIface = new Interface(_subnetId, igwConfigNode);
         igwSubnetIface.setPrefix(igwSubnetIfacePrefix);
         igwSubnetIface.getAllPrefixes().add(igwSubnetIfacePrefix);
         igwConfigNode.getDefaultVrf().getInterfaces().put(_subnetId,
               igwSubnetIface);
         igwAddress = igwSubnetIfacePrefix.getAddress();
      }

      // attach to vgw if it exists
      _vpnGatewayId = awsVpcConfiguration.getVpcs().get(_vpcId)
            .getVpnGatewayId();
      Ip vgwAddress = null;
      if (_vpnGatewayId != null) {
         // generate a prefix for the link between the vgw and the subnet
         Prefix vgwSubnetLinkPrefix = awsVpcConfiguration
               .getNextGeneratedLinkSubnet();
         Prefix subnetVgwIfacePrefix = vgwSubnetLinkPrefix;
         Prefix vgwSubnetIfacePrefix = new Prefix(
               vgwSubnetLinkPrefix.getEndAddress(),
               vgwSubnetLinkPrefix.getPrefixLength());

         // add an interface that faces the vgw
         Interface subnetVgwIface = new Interface(_vpnGatewayId, cfgNode);
         cfgNode.getDefaultVrf().getInterfaces().put(_vpnGatewayId,
               subnetVgwIface);
         subnetVgwIface.getAllPrefixes().add(subnetVgwIfacePrefix);
         subnetVgwIface.setPrefix(subnetVgwIfacePrefix);

         // add an interface to the igw facing the subnet
         Configuration vgwConfigNode = awsVpcConfiguration
               .getConfigurationNodes().get(_vpnGatewayId);
         Interface vgwSubnetIface = new Interface(_subnetId, vgwConfigNode);
         vgwSubnetIface.setPrefix(vgwSubnetIfacePrefix);
         vgwSubnetIface.getAllPrefixes().add(vgwSubnetIfacePrefix);
         vgwConfigNode.getDefaultVrf().getInterfaces().put(_subnetId,
               vgwSubnetIface);
         igwAddress = vgwSubnetIfacePrefix.getAddress();
      }

      // lets find the right route table for this subnet
      RouteTable myRouteTable = findMyRouteTable(
            awsVpcConfiguration.getRouteTables());

      if (myRouteTable == null) {
         throw new BatfishException(
               "Could not find a route table for subnet " + _subnetId);
      }

      for (Route route : myRouteTable.getRoutes()) {
         StaticRoute sRoute = route.toStaticRoute(awsVpcConfiguration,
               vpcIfacePrefix.getAddress(), igwAddress, vgwAddress, this,
               cfgNode);
         if (sRoute != null) {
            cfgNode.getDefaultVrf().getStaticRoutes().add(sRoute);
         }
      }

      NetworkAcl myNetworkAcl = findMyNetworkAcl(
            awsVpcConfiguration.getNetworkAcls());

      if (myNetworkAcl == null) {
         throw new BatfishException(
               "Could not find a network acl for subnet " + _subnetId);
      }

      IpAccessList inAcl = myNetworkAcl.getIngressAcl();
      IpAccessList outAcl = myNetworkAcl.getEgressAcl();
      cfgNode.getIpAccessLists().put(inAcl.getName(), inAcl);
      cfgNode.getIpAccessLists().put(outAcl.getName(), outAcl);

      for (Entry<String, Interface> eIface : cfgNode.getDefaultVrf()
            .getInterfaces().entrySet()) {
         String ifaceName = eIface.getKey();
         if (awsVpcConfiguration.getVpcs().containsKey(ifaceName)
               || awsVpcConfiguration.getInternetGateways()
                     .containsKey(ifaceName)
               || awsVpcConfiguration.getVpnGateways().containsKey(ifaceName)) {
            Interface iface = eIface.getValue();
            iface.setIncomingFilter(inAcl);
            iface.setOutgoingFilter(outAcl);
         }
      }

      // TODO: ari add acls in myNetworkAcl to the interface facing the VPC
      // router

      return cfgNode;
   }
}
