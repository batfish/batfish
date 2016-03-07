package org.batfish.representation.aws_vpcs;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import org.batfish.common.BatfishException;
import org.batfish.common.BatfishLogger;
import org.batfish.main.ConfigurationFormat;
import org.batfish.representation.Configuration;
import org.batfish.representation.Interface;
import org.batfish.representation.IpAccessListLine;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

public class Instance implements AwsVpcEntity, Serializable {

   private static final long serialVersionUID = 1L;

   private List<String> _securityGroups = new LinkedList<String>();
         
   private List<String> _networkInterfaces = new LinkedList<String>();
   
   private String _instanceId;
   
   private String _subnetId;
   
   private String _vpcId;

   public Instance(JSONObject jObj, BatfishLogger logger) throws JSONException {
      _instanceId = jObj.getString(JSON_KEY_INSTANCE_ID);
      
      if (jObj.has(JSON_KEY_VPC_ID)) {
    	  _vpcId = jObj.getString(JSON_KEY_VPC_ID);

    	  _subnetId = jObj.getString(JSON_KEY_SUBNET_ID);
      }
      
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
   
   public Configuration toConfigurationNode(AwsVpcConfiguration awsVpcConfig) {
	   Configuration cfgNode = new Configuration(_instanceId);
	   	   	   
	   List<IpAccessListLine> inboundRules = new LinkedList<IpAccessListLine>();
	   List<IpAccessListLine> outboundRules = new LinkedList<IpAccessListLine>();
	   
	   for (String sGroupId : _securityGroups) {
		   SecurityGroup sGroup = awsVpcConfig.getSecurityGroups().get(sGroupId);
		   
		   if (sGroup == null)
			   throw new BatfishException("Security group " + sGroupId 
					   + " for instance " + _instanceId + " not found");
		   
		   sGroup.addInOutAccessLines(inboundRules, outboundRules);
	   }
	   
	   for (String interfaceId : _networkInterfaces) {

		   NetworkInterface netInterface = awsVpcConfig.getNetworkInterfaces().get(interfaceId);

		   if (netInterface == null)
			   throw new BatfishException("Network interface " + interfaceId 
					   + " for instance " + _instanceId + " not found");		   		   
		   
		   Interface iface = new Interface(interfaceId, cfgNode);
		   
		   //TODO: ari: configure the interface's address(es)
		   
		   //TODO: ari: attach inbound and outbound filters
		
		   cfgNode.getInterfaces().put(interfaceId, iface);
		   
	   }
	   
	   return cfgNode;	   
   }
}