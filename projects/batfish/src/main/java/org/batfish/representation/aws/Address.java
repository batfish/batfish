package org.batfish.representation.aws;

import java.io.Serializable;
import org.batfish.datamodel.Ip;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

public class Address implements AwsVpcEntity, Serializable {

  private static final long serialVersionUID = 1L;

  private final String _instanceId;

  private final Ip _privateIp;

  private final Ip _publicIp;

  public Address(JSONObject jObj) throws JSONException {
    _publicIp = new Ip(jObj.getString(JSON_KEY_PUBLIC_IP));

    _instanceId = jObj.has(JSON_KEY_INSTANCE_ID) ? jObj.getString(JSON_KEY_INSTANCE_ID) : null;

    _privateIp =
        jObj.has(JSON_KEY_PRIVATE_IP_ADDRESS)
            ? new Ip(jObj.getString(JSON_KEY_PRIVATE_IP_ADDRESS))
            : null;

    // TODO: not sure what other information we need to pull
  }

  @Override
  public String getId() {
    return _publicIp.toString();
  }

  public String getInstanceId() {
    return _instanceId;
  }

  public Ip getPrivateIp() {
    return _privateIp;
  }

  public Ip getPublicIp() {
    return _publicIp;
  }
}
