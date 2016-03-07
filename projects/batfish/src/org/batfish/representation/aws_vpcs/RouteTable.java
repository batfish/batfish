package org.batfish.representation.aws_vpcs;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import org.batfish.common.BatfishLogger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

public class RouteTable implements AwsVpcConfigElement, Serializable {

   private static final long serialVersionUID = 1L;

   private List<Route> _routes = new LinkedList<Route>();
         
   private List<RouteTableAssociation> _routeTableAssociations = new LinkedList<RouteTableAssociation>();
   
   private String _routeTableId;
   
   private String _vpcId;

   public RouteTable(JSONObject jObj, BatfishLogger logger) throws JSONException {
      _routeTableId = jObj.getString(JSON_KEY_ROUTE_TABLE_ID);
      _vpcId = jObj.getString(JSON_KEY_VPC_ID);
      
      JSONArray associations = jObj.getJSONArray(JSON_KEY_ASSOCIATIONS);
      InitAssociations(associations, logger);
      
      JSONArray routes = jObj.getJSONArray(JSON_KEY_ROUTES);
      InitRoutes(routes, logger);            
   }
   
   @Override
   public String getId() {
      return _routeTableId;
   }

   private void InitAssociations(JSONArray associations, BatfishLogger logger) throws JSONException {

      for (int index = 0; index < associations.length(); index++) {
         JSONObject childObject = associations.getJSONObject(index);
         _routeTableAssociations.add(new RouteTableAssociation(childObject, logger));         
      }
   }

   private void InitRoutes(JSONArray routes, BatfishLogger logger) throws JSONException {
      
      for (int index = 0; index < routes.length(); index++) {
         JSONObject childObject = routes.getJSONObject(index);
         _routes.add(new Route(childObject, logger));         
      }

   }
}