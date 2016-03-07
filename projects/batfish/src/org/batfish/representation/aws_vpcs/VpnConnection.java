package org.batfish.representation.aws_vpcs;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import org.batfish.common.BatfishLogger;
import org.batfish.representation.Prefix;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

public class VpnConnection implements AwsVpcEntity, Serializable {

	private static final long serialVersionUID = 1L;

	private String _vpnConnectionId;

	private String _customerGatewayId;

	private String _vpnGatewayId;

	private List<Prefix> _routes = new LinkedList<Prefix>();

	private List<VgwTelemetry> _vgwTelemetrys = new LinkedList<VgwTelemetry>();

	private boolean _staticRoutesOnly = false;

	public VpnConnection(JSONObject jObj, BatfishLogger logger) throws JSONException {
		_vpnConnectionId = jObj.getString(JSON_KEY_VPN_CONNECTION_ID);
		_customerGatewayId = jObj.getString(JSON_KEY_CUSTOMER_GATEWAY_ID);
		_vpnGatewayId = jObj.getString(JSON_KEY_VPN_GATEWAY_ID);

		if (jObj.has(JSON_KEY_ROUTES)) {
			JSONArray routes = jObj.getJSONArray(JSON_KEY_ROUTES);
			for (int index = 0; index < routes.length(); index++) {
				JSONObject childObject = routes.getJSONObject(index);
				_routes.add(new Prefix(childObject.getString(JSON_KEY_DESTINATION_CIDR_BLOCK)));         
			}
		}

		JSONArray vgwTelemetry = jObj.getJSONArray(JSON_KEY_VGW_TELEMETRY);
		for (int index = 0; index < vgwTelemetry.length(); index++) {
			JSONObject childObject = vgwTelemetry.getJSONObject(index);
			_vgwTelemetrys.add(new VgwTelemetry(childObject, logger));         
		}

		if (jObj.has(JSON_KEY_OPTIONS)) {
			JSONObject options = jObj.getJSONObject(JSON_KEY_OPTIONS);	 	
			_staticRoutesOnly = Utils.tryGetBoolean(options, JSON_KEY_STATIC_ROUTES_ONLY, false);
		}
	}

	@Override
	public String getId() {
		return _vpnConnectionId;
	}
}
