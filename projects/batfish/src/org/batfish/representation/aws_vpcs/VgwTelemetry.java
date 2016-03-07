package org.batfish.representation.aws_vpcs;

import java.io.Serializable;

import org.batfish.common.BatfishLogger;
import org.batfish.representation.Ip;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

public class VgwTelemetry implements Serializable {

	private static final long serialVersionUID = 1L;

	private String _status;
	private String _statusMessage;
	private int _acceptedRouteCount;
	private Ip _outsideIpAddress;

	public VgwTelemetry(JSONObject jObj, BatfishLogger logger) throws JSONException {
		_status = jObj.getString(AwsVpcConfigElement.JSON_KEY_STATUS);
		_statusMessage = jObj.getString(AwsVpcConfigElement.JSON_KEY_STATUS_MESSAGE);
		_acceptedRouteCount = jObj.getInt(AwsVpcConfigElement.JSON_KEY_ACCEPTED_ROUTE_COUNT);
		_outsideIpAddress = new Ip(jObj.getString(AwsVpcConfigElement.JSON_KEY_OUTSIDE_IP_ADDRESS));

	}
}
