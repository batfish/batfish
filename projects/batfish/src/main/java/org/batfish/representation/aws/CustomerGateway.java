package org.batfish.representation.aws;

import java.io.Serializable;
import org.batfish.datamodel.Ip;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

public class CustomerGateway implements AwsVpcEntity, Serializable {

  private static final long serialVersionUID = 1L;

  private final String _bgpAsn;

  private final String _customerGatewayId;

  private final Ip _ipAddress;

  private final String _type;

  public CustomerGateway(JSONObject jObj) throws JSONException {
    _customerGatewayId = jObj.getString(JSON_KEY_CUSTOMER_GATEWAY_ID);
    _ipAddress = new Ip(jObj.getString(JSON_KEY_IP_ADDRESS));
    _type = jObj.getString(JSON_KEY_TYPE);
    _bgpAsn = jObj.getString(JSON_KEY_BGP_ASN);
  }

  public String getBgpAsn() {
    return _bgpAsn;
  }

  public String getCustomerGatewayId() {
    return _customerGatewayId;
  }

  @Override
  public String getId() {
    return _customerGatewayId;
  }

  public Ip getIpAddress() {
    return _ipAddress;
  }

  public String getType() {
    return _type;
  }
}
