package org.batfish.representation.aws;

import java.io.Serializable;
import org.batfish.datamodel.Ip;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

public class VgwTelemetry implements Serializable {

  private static final long serialVersionUID = 1L;

  private final int _acceptedRouteCount;

  private final Ip _outsideIpAddress;

  private final String _status;

  private final String _statusMessage;

  public VgwTelemetry(JSONObject jObj) throws JSONException {
    _status = jObj.getString(AwsVpcEntity.JSON_KEY_STATUS);
    _statusMessage = jObj.getString(AwsVpcEntity.JSON_KEY_STATUS_MESSAGE);
    _acceptedRouteCount = jObj.getInt(AwsVpcEntity.JSON_KEY_ACCEPTED_ROUTE_COUNT);
    _outsideIpAddress = new Ip(jObj.getString(AwsVpcEntity.JSON_KEY_OUTSIDE_IP_ADDRESS));
  }

  public int getAcceptedRouteCount() {
    return _acceptedRouteCount;
  }

  public Ip getOutsideIpAddress() {
    return _outsideIpAddress;
  }

  public String getStatus() {
    return _status;
  }

  public String getStatusMessage() {
    return _statusMessage;
  }
}
