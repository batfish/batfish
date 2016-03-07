package org.batfish.representation.aws_vpcs;

import java.io.Serializable;

import org.batfish.common.BatfishLogger;
import org.batfish.representation.Prefix;
import org.batfish.representation.StaticRoute;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

public class Route implements Serializable {

	public enum TargetType {Gateway, Instance, VpcPeeringConnection, NetworkInterface}

	private static final long serialVersionUID = 1L;

	private Prefix _destinationCidrBlock;
	private String _state;
	private String _target;
	private TargetType _targetType;

	public Route(JSONObject jObj, BatfishLogger logger) throws JSONException {
		_destinationCidrBlock = new Prefix(jObj.getString(AwsVpcEntity.JSON_KEY_DESTINATION_CIDR_BLOCK));
		_state = jObj.getString(AwsVpcEntity.JSON_KEY_STATE);

		if (jObj.has(AwsVpcEntity.JSON_KEY_VPC_PEERING_CONNECTION_ID)) {
			_targetType = TargetType.VpcPeeringConnection;
			_target = jObj.getString(AwsVpcEntity.JSON_KEY_VPC_PEERING_CONNECTION_ID);
		}
		else if (jObj.has(AwsVpcEntity.JSON_KEY_GATEWAY_ID)) {
			_targetType = TargetType.Gateway;
			_target = jObj.getString(AwsVpcEntity.JSON_KEY_GATEWAY_ID);
		}
		else if (jObj.has(AwsVpcEntity.JSON_KEY_INSTANCE_ID)) {
			_targetType = TargetType.Instance;
			_target = jObj.getString(AwsVpcEntity.JSON_KEY_INSTANCE_ID);
		}
		else if (jObj.has(AwsVpcEntity.JSON_KEY_NETWORK_INTERFACE_ID)) {
			_targetType = TargetType.NetworkInterface;
			_target = jObj.getString(AwsVpcEntity.JSON_KEY_NETWORK_INTERFACE_ID);
		}
		else {
			throw new JSONException("Target not found in route " + jObj.toString());
		}      
	}

	public StaticRoute toStaticRoute() {
		// TODO ari : convert this to a static route
		return null;
	}
}
