package org.batfish.representation.aws_vpcs;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import org.batfish.common.BatfishLogger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

public class SecurityGroup implements AwsVpcConfigElement, Serializable {

   private static final long serialVersionUID = 1L;

   private String _groupId;
   
   private String _groupName;

   private List<IpPermissions> _ipPermsEgress = new LinkedList<IpPermissions>();

   private List<IpPermissions> _ipPermsIngress = new LinkedList<IpPermissions>();

   public SecurityGroup(JSONObject jObj, BatfishLogger logger) throws JSONException {
      _groupId = jObj.getString(JSON_KEY_GROUP_ID);
      _groupName = jObj.getString(JSON_KEY_GROUP_NAME);
      
      //logger.debugf("doing security group %s\n", _groupId);

      JSONArray permsEgress = jObj.getJSONArray(JSON_KEY_IP_PERMISSIONS_EGRESS);
      initIpPerms(_ipPermsEgress, permsEgress, logger);      

      JSONArray permsIngress = jObj.getJSONArray(JSON_KEY_IP_PERMISSIONS);
      initIpPerms(_ipPermsIngress, permsIngress, logger);      
   }
   
   @Override
   public String getId() {
      return _groupId;
   }

   private void initIpPerms(List<IpPermissions> ipPermsList, JSONArray ipPermsJson, BatfishLogger logger) throws JSONException {

      for (int index = 0; index < ipPermsJson.length(); index++) {
         JSONObject childObject = ipPermsJson.getJSONObject(index);
         ipPermsList.add(new IpPermissions(childObject, logger));         
      }
   }
}