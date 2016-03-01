package org.batfish.representation.aws_vpcs;

import java.io.Serializable;

import org.batfish.common.BatfishLogger;
import org.batfish.representation.Prefix;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

public class Subnet implements AwsVpcConfigElement, Serializable {

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
}
