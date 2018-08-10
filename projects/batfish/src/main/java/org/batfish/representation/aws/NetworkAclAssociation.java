package org.batfish.representation.aws;

import java.io.Serializable;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

public class NetworkAclAssociation implements Serializable {

  /** */
  private static final long serialVersionUID = 1L;

  private String _subnetId = null;

  public NetworkAclAssociation(JSONObject jObj) throws JSONException {
    _subnetId = jObj.getString(AwsVpcEntity.JSON_KEY_SUBNET_ID);
  }

  public String getSubnetId() {
    return _subnetId;
  }
}
