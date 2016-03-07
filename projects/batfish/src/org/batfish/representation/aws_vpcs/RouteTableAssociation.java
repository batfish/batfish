package org.batfish.representation.aws_vpcs;

import java.io.Serializable;

import org.batfish.common.BatfishLogger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

public class RouteTableAssociation implements Serializable {

   /**
    * 
    */
   private static final long serialVersionUID = 1L;
   
   private boolean _isMain;
   private String _subnetId = null;
   
   public RouteTableAssociation(JSONObject jObj, BatfishLogger logger) throws JSONException {

       _isMain = jObj.getBoolean(AwsVpcConfigElement.JSON_KEY_MAIN);

      if (jObj.has(AwsVpcConfigElement.JSON_KEY_SUBNET_ID)) {
         _subnetId = jObj.getString(AwsVpcConfigElement.JSON_KEY_SUBNET_ID);
      }
   }
}
