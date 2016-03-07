package org.batfish.representation.aws_vpcs;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.batfish.common.BatfishException;
import org.batfish.common.BatfishLogger;
import org.batfish.representation.Configuration;
import org.batfish.representation.Interface;
import org.batfish.representation.Prefix;
import org.batfish.representation.StaticRoute;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

public class Subnet implements AwsVpcEntity, Serializable {

   private static final long serialVersionUID = 1L;

   private Prefix _cidrBlock;

   private String _subnetId;
   
   private String _vpcId;

   public Subnet(JSONObject jObj, BatfishLogger logger) throws JSONException {
      _cidrBlock = new Prefix(jObj.getString(JSON_KEY_CIDR_BLOCK));
      _subnetId = jObj.getString(JSON_KEY_SUBNET_ID);
      _vpcId = jObj.getString(JSON_KEY_VPC_ID);
   }
   
   @Override
   public String getId() {
      return _subnetId;
   }

   public Configuration toConfigurationNode(AwsVpcConfiguration awsVpcConfiguration) {
	   Configuration cfgNode = new Configuration(_subnetId);

	   //TODO: ari add one interface that faces the instances

	   //add an interface that faces the VPC router
	   Interface subnetIface = new Interface(_vpcId, cfgNode);			   
	   cfgNode.getInterfaces().put(subnetIface.getName(), subnetIface);
	   
	   //add the interface to the vpc router
	   Configuration vpcConfigNode = awsVpcConfiguration.getConfigurationNodes().get(_vpcId);
	   Interface vpcIface = new Interface(_subnetId, vpcConfigNode);			   
	   vpcConfigNode.getInterfaces().put(vpcIface.getName(), vpcIface);
	   
	   //TODO ari : assign addresses to both interfaces above
	   
	   //lets find the right route table for this subnet
	   RouteTable myRouteTable = null;
	   RouteTable mainRouteTable = null;

	   Map<String,RouteTable> routeTables = awsVpcConfiguration.getRouteTables();
	   
	   for (String routeTableId : routeTables.keySet()) {
		   
		   RouteTable routeTable = routeTables.get(routeTableId);
		   
		   //ignore if the route table is not for the same VPC
		   if (! routeTable.getVpcId().equals(_vpcId)) 
			   continue;
			   
		   List<RouteTableAssociation> rtAssocs = routeTable.getRouteTableAssociations();
		   
		   for (RouteTableAssociation rtAssoc : rtAssocs) {
			   if (rtAssoc.getSubnetId().equals(_subnetId)) {
				   if (myRouteTable != null) 
					   throw new BatfishException("Found two associated route tables (" 
							   + routeTable.getId() + ", " + myRouteTable.getId() 
							   + " for subnet " + _subnetId);
				   
				  myRouteTable = routeTable;			
			   }

			   if (rtAssoc.isMain()) {
					   if (mainRouteTable != null) 
						   throw new BatfishException("Found two main route tables (" 
								   + routeTable.getId() + ", " + mainRouteTable.getId() 
								   + " for subnet " + _subnetId);

					   mainRouteTable = routeTable;
					  
			   }
		   }
		   
		   if (myRouteTable == null) 
			   myRouteTable = mainRouteTable;
		   
		   if (myRouteTable != null) 
			   throw new BatfishException("Could not find a route table for subnet " 
					   + _subnetId);
		   
		   //TODO: ari convert routes in the route table to static routes
		   for (Route route : myRouteTable.getRoutes()) {
			   StaticRoute sRoute = route.toStaticRoute();
			   cfgNode.getStaticRoutes().add(sRoute);
		   }
	   }
	   
	   return cfgNode;
   }
}
