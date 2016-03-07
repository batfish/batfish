package org.batfish.representation.aws_vpcs;

import java.io.Serializable;

import org.batfish.common.BatfishLogger;
import org.batfish.representation.Prefix;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

public class NetworkAclEntry implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Prefix _cidrBlock;
	private boolean _isAllow;
	private boolean _isEgress;
	private String _protocol;
	private int _ruleNumber;

	public NetworkAclEntry(JSONObject jObj, BatfishLogger logger) throws JSONException {
		_cidrBlock = new Prefix(jObj.getString(AwsVpcConfigElement.JSON_KEY_CIDR_BLOCK));

		_isAllow = jObj.getString(AwsVpcConfigElement.JSON_KEY_RULE_ACTION).equals("allow") ? true : false;

		_isEgress = jObj.getBoolean(AwsVpcConfigElement.JSON_KEY_EGRESS);

		_protocol = jObj.getString(AwsVpcConfigElement.JSON_KEY_PROTOCOL);

		_ruleNumber = jObj.getInt(AwsVpcConfigElement.JSON_KEY_RULE_NUMBER);
	}
}
