package org.batfish.representation.aws_vpcs;

import java.io.Serializable;

import org.batfish.common.BatfishLogger;
import org.batfish.representation.Prefix;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

public class NetworkAclEntry implements Serializable {

   /**
	 *
	 */
   private static final long serialVersionUID = 1L;

   private Prefix _cidrBlock;

   private boolean _isAllow;

   private boolean _isEgress;

   private String _protocol;

   private int _ruleNumber;

   private int _fromPort=-1;
   
   private int _toPort=-1;
   
   public NetworkAclEntry(JSONObject jObj, BatfishLogger logger)
         throws JSONException {
      _cidrBlock = new Prefix(jObj.getString(AwsVpcEntity.JSON_KEY_CIDR_BLOCK));

      _isAllow = jObj.getString(AwsVpcEntity.JSON_KEY_RULE_ACTION).equals(
            "allow") ? true : false;

      _isEgress = jObj.getBoolean(AwsVpcEntity.JSON_KEY_EGRESS);

      _protocol = jObj.getString(AwsVpcEntity.JSON_KEY_PROTOCOL);

      _ruleNumber = jObj.getInt(AwsVpcEntity.JSON_KEY_RULE_NUMBER);
      
      if (jObj.has(AwsVpcEntity.JSON_KEY_PORT_RANGE)) {
         JSONObject portRange = jObj.getJSONObject(AwsVpcEntity.JSON_KEY_PORT_RANGE);

         _fromPort = portRange.getInt(AwsVpcEntity.JSON_KEY_FROM);

         _toPort = portRange.getInt(AwsVpcEntity.JSON_KEY_TO);
      }
   }

   public Prefix getCidrBlock() {
      return _cidrBlock;
   }

   public boolean getIsAllow() {
      return _isAllow;
   }

   public boolean getIsEgress() {
      return _isEgress;
   }

   public String getProtocol() {
      return _protocol;
   }

   public int getRuleNumber() {
      return _ruleNumber;
   }
}
