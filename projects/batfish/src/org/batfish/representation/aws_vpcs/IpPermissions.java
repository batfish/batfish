package org.batfish.representation.aws_vpcs;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import org.batfish.common.BatfishLogger;
import org.batfish.representation.Prefix;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

public class IpPermissions implements Serializable {

   private static final long serialVersionUID = 1L;

   private List<Prefix> _ipRanges = new LinkedList<Prefix>();
   
   private String _ipProtocol;
   
   private int _fromPort;

   private int _toPort;
            
   public IpPermissions(JSONObject jObj, BatfishLogger logger) throws JSONException {
      _ipProtocol = jObj.getString(AwsVpcConfigElement.JSON_KEY_IP_PROTOCOL);
      _fromPort = jObj.getInt(AwsVpcConfigElement.JSON_KEY_FROM_PORT);
      _toPort = jObj.getInt(AwsVpcConfigElement.JSON_KEY_TO_PORT);
      
      JSONArray ranges = jObj.getJSONArray(AwsVpcConfigElement.JSON_KEY_IP_RANGES);

      for (int index = 0; index < ranges.length(); index++) {
          JSONObject childObject = ranges.getJSONObject(index);
          _ipRanges.add(new Prefix(childObject.getString(AwsVpcConfigElement.JSON_KEY_CIDR_IP)));         
       }
   }
   
}