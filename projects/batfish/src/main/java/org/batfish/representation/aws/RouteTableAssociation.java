package org.batfish.representation.aws;

import java.io.Serializable;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

public class RouteTableAssociation implements Serializable {

  /** */
  private static final long serialVersionUID = 1L;

  private boolean _isMain;
  private String _subnetId = null;

  public RouteTableAssociation(JSONObject jObj) throws JSONException {

    _isMain = jObj.getBoolean(AwsVpcEntity.JSON_KEY_MAIN);

    if (jObj.has(AwsVpcEntity.JSON_KEY_SUBNET_ID)) {
      _subnetId = jObj.getString(AwsVpcEntity.JSON_KEY_SUBNET_ID);
    }
  }

  public String getSubnetId() {
    return _subnetId;
  }

  public boolean isMain() {
    return _isMain;
  }
}
