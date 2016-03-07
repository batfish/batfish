package org.batfish.representation.aws_vpcs;

import java.io.Serializable;
import org.batfish.common.BatfishLogger;
import org.batfish.representation.Ip;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

public class CustomerGateway implements AwsVpcEntity, Serializable {

   private static final long serialVersionUID = 1L;

   private String _bgpAsn;

   private String _customerGatewayId;

   private Ip _ipAddress;

   private String _type;

   public CustomerGateway(JSONObject jObj, BatfishLogger logger)
         throws JSONException {
      _customerGatewayId = jObj.getString(JSON_KEY_CUSTOMER_GATEWAY_ID);
      _ipAddress = new Ip(jObj.getString(JSON_KEY_IP_ADDRESS));
      _type = jObj.getString(JSON_KEY_TYPE);
      _bgpAsn = jObj.getString(JSON_KEY_BGP_ASN);
   }

   @Override
   public String getId() {
      return _customerGatewayId;
   }
}
