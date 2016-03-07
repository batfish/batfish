package org.batfish.representation.aws_vpcs;

import java.io.Serializable;

import org.batfish.common.BatfishLogger;
import org.batfish.representation.Ip;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

public class Address implements AwsVpcEntity, Serializable {

   private static final long serialVersionUID = 1L;

   private Ip _publicIp;

   private String _instanceId;
   
   private Ip _privateIp;
   
   public Address(JSONObject jObj, BatfishLogger logger) throws JSONException {
      _publicIp = new Ip(jObj.getString(JSON_KEY_PUBLIC_IP));
      
      if (jObj.has(JSON_KEY_INSTANCE_ID))
    	  _instanceId = jObj.getString(JSON_KEY_INSTANCE_ID);

      if (jObj.has(JSON_KEY_PRIVATE_IP_ADDRESS))
    	  _privateIp = new Ip(jObj.getString(JSON_KEY_PRIVATE_IP_ADDRESS));
      
      //TODO: not sure what other information we need to pull
   }
   
   @Override
   public String getId() {
      return _publicIp.toString();
   }
}
