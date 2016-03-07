package org.batfish.representation.aws_vpcs;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import org.batfish.common.BatfishLogger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

public class VpnGateway implements AwsVpcEntity, Serializable {

	private static final long serialVersionUID = 1L;

	private String _vpnGatewayId;

	private List<String> _attachmentVpcIds = new LinkedList<String>();

	public VpnGateway(JSONObject jObj, BatfishLogger logger) throws JSONException {
		_vpnGatewayId = jObj.getString(JSON_KEY_VPN_GATEWAY_ID);

	      JSONArray attachments = jObj.getJSONArray(JSON_KEY_VPC_ATTACHMENTS);
	      for (int index = 0; index < attachments.length(); index++) {
	          JSONObject childObject = attachments.getJSONObject(index);
	          _attachmentVpcIds.add(childObject.getString(JSON_KEY_VPC_ID));         
	       }

	}
	
	@Override
	public String getId() {
		return _vpnGatewayId;
	}
}
