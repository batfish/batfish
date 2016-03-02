package org.batfish.representation.aws_vpcs;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import org.batfish.common.BatfishLogger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

public class Instance implements AwsVpcConfigElement, Serializable {

   private static final long serialVersionUID = 1L;

   private List<String> _securityGroups = new LinkedList<String>();
         
   private List<String> _networkInterfaces = new LinkedList<String>();
   
   private String _instanceId;
   
   private String _subnetId;
   
   private String _vpcId;

   public Instance(JSONObject jObj, BatfishLogger logger) throws JSONException {
      _instanceId = jObj.getString(JSON_KEY_INSTANCE_ID);
      _vpcId = jObj.getString(JSON_KEY_VPC_ID);
      _subnetId = jObj.getString(JSON_KEY_SUBNET_ID);
      
      JSONArray securityGroups = jObj.getJSONArray(JSON_KEY_SECURITY_GROUPS);
      initSecurityGroups(securityGroups, logger);
      
      JSONArray networkInterfaces = jObj.getJSONArray(JSON_KEY_NETWORK_INTERFACES);
      initNetworkInterfaces(networkInterfaces, logger);     
      
      //check if the public and private ip addresses are associated with an interface
   }
   
   @Override
   public String getId() {
      return _instanceId;
   }

   private void initSecurityGroups(JSONArray associations, BatfishLogger logger) throws JSONException {

      for (int index = 0; index < associations.length(); index++) {
         JSONObject childObject = associations.getJSONObject(index);
         _securityGroups.add(childObject.getString(JSON_KEY_GROUP_ID));         
      }
   }

   private void initNetworkInterfaces(JSONArray routes, BatfishLogger logger) throws JSONException {
      
      for (int index = 0; index < routes.length(); index++) {
         JSONObject childObject = routes.getJSONObject(index);
         _networkInterfaces.add(childObject.getString(JSON_KEY_NETWORK_INTERFACE_ID));         
      }

   }
}