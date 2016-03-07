package org.batfish.representation.aws_vpcs;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import org.batfish.common.BatfishLogger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

public class NetworkAcl implements AwsVpcEntity, Serializable {

   private static final long serialVersionUID = 1L;

   private List<NetworkAclEntry> _entries = new LinkedList<NetworkAclEntry>();
         
   private List<NetworkAclAssociation> _networkAclAssociations = new LinkedList<NetworkAclAssociation>();
   
   private String _networkAclId;
   
   private String _vpcId;

   public NetworkAcl(JSONObject jObj, BatfishLogger logger) throws JSONException {
      _networkAclId = jObj.getString(JSON_KEY_NETWORK_ACL_ID);
      _vpcId = jObj.getString(JSON_KEY_VPC_ID);
      
      JSONArray associations = jObj.getJSONArray(JSON_KEY_ASSOCIATIONS);
      InitAssociations(associations, logger);
      
      JSONArray entries = jObj.getJSONArray(JSON_KEY_ENTRIES);
      InitEntries(entries, logger);            
   }

   public List<NetworkAclAssociation> getAssociations() {
	   return _networkAclAssociations;
   }
   
   @Override
   public String getId() {
      return _networkAclId;
   }
   
   public String getVpcId() {
	   return _vpcId;
   }

   private void InitAssociations(JSONArray associations, BatfishLogger logger) throws JSONException {

      for (int index = 0; index < associations.length(); index++) {
         JSONObject childObject = associations.getJSONObject(index);
         _networkAclAssociations.add(new NetworkAclAssociation(childObject, logger));         
      }
   }

   private void InitEntries(JSONArray entries, BatfishLogger logger) throws JSONException {
      
      for (int index = 0; index < entries.length(); index++) {
         JSONObject childObject = entries.getJSONObject(index);
         _entries.add(new NetworkAclEntry(childObject, logger));         
      }

   }
}