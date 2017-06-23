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
      String instancesIfaceName = _subnetId;
      Interface instancesIface = new Interface(instancesIfaceName, cfgNode);
      cfgNode.getInterfaces().put(instancesIfaceName, instancesIface);
      cfgNode.getDefaultVrf().getInterfaces().put(instancesIfaceName,
            instancesIface);
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
      String subnetIfaceName = _vpcId;
      Interface subnetIface = new Interface(subnetIfaceName, cfgNode);
      cfgNode.getInterfaces().put(subnetIfaceName, subnetIface);
      cfgNode.getDefaultVrf().getInterfaces().put(subnetIfaceName, subnetIface);
      subnetIface.getAllPrefixes().add(subnetIfacePrefix);
      subnetIface.setPrefix(subnetIfacePrefix);

      // add the interface to the vpc router
      Configuration vpcConfigNode = awsVpcConfiguration.getConfigurationNodes()
            .get(_vpcId);
      String vpcIfaceName = _subnetId;
      Interface vpcIface = new Interface(vpcIfaceName, vpcConfigNode);
      vpcConfigNode.getInterfaces().put(vpcIfaceName, vpcIface);
      vpcConfigNode.getDefaultVrf().getInterfaces().put(vpcIfaceName, vpcIface);
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
         String subnetIgwIfaceName = _internetGatewayId;
         Interface subnetIgwIface = new Interface(subnetIgwIfaceName, cfgNode);
         cfgNode.getInterfaces().put(subnetIgwIfaceName, subnetIgwIface);
         cfgNode.getDefaultVrf().getInterfaces().put(subnetIgwIfaceName,
               subnetIgwIface);
         subnetIgwIface.getAllPrefixes().add(subnetIgwIfacePrefix);
         subnetIgwIface.setPrefix(subnetIgwIfacePrefix);

         // add an interface to the igw facing the subnet
         Configuration igwConfigNode = awsVpcConfiguration
               .getConfigurationNodes().get(_internetGatewayId);
         String igwSubnetIfaceName = _subnetId;
         Interface igwSubnetIface = new Interface(igwSubnetIfaceName,
               igwConfigNode);
         igwSubnetIface.setPrefix(igwSubnetIfacePrefix);
         igwSubnetIface.getAllPrefixes().add(igwSubnetIfacePrefix);
         igwConfigNode.getInterfaces().put(igwSubnetIfaceName, igwSubnetIface);
         igwConfigNode.getDefaultVrf().getInterfaces().put(igwSubnetIfaceName,
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
         String subnetVgwIfaceName = _vpnGatewayId;
         Interface subnetVgwIface = new Interface(subnetVgwIfaceName, cfgNode);
         cfgNode.getInterfaces().put(subnetVgwIfaceName, subnetVgwIface);
         cfgNode.getDefaultVrf().getInterfaces().put(subnetVgwIfaceName,
               subnetVgwIface);
         subnetVgwIface.getAllPrefixes().add(subnetVgwIfacePrefix);
         subnetVgwIface.setPrefix(subnetVgwIfacePrefix);

         // add an interface to the igw facing the subnet
         Configuration vgwConfigNode = awsVpcConfiguration
               .getConfigurationNodes().get(_vpnGatewayId);
         String vgwSubnetIfaceName = _subnetId;
         Interface vgwSubnetIface = new Interface(vgwSubnetIfaceName,
               vgwConfigNode);
         vgwSubnetIface.setPrefix(vgwSubnetIfacePrefix);
         vgwSubnetIface.getAllPrefixes().add(vgwSubnetIfacePrefix);
         vgwConfigNode.getInterfaces().put(vgwSubnetIfaceName, vgwSubnetIface);
         vgwConfigNode.getDefaultVrf().getInterfaces().put(vgwSubnetIfaceName,
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
