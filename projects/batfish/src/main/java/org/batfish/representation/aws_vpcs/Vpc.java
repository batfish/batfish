package org.batfish.representation.aws_vpcs;

import java.io.Serializable;
import org.batfish.common.BatfishLogger;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Prefix;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

public class Vpc implements AwsVpcEntity, Serializable {

  private static final long serialVersionUID = 1L;

  private Prefix _cidrBlock;

  private transient String _internetGatewayId;

  private String _vpcId;

  private transient String _vpnGatewayId;

  public Vpc(JSONObject jObj, BatfishLogger logger) throws JSONException {
    _vpcId = jObj.getString(JSON_KEY_VPC_ID);
    _cidrBlock = new Prefix(jObj.getString(JSON_KEY_CIDR_BLOCK));
  }

  public Prefix getCidrBlock() {
    return _cidrBlock;
  }

  @Override
  public String getId() {
    return _vpcId;
  }

  public String getInternetGatewayId() {
    return _internetGatewayId;
  }

  public String getVpnGatewayId() {
    return _vpnGatewayId;
  }

  public void setInternetGatewayId(String internetGatewayId) {
    _internetGatewayId = internetGatewayId;
  }

  public void setVpnGatewayId(String vpnGatewayId) {
    _vpnGatewayId = vpnGatewayId;
  }

  public Configuration toConfigurationNode(AwsVpcConfiguration awsVpcConfiguration) {
    Configuration cfgNode = Utils.newAwsConfiguration(_vpcId);
    cfgNode.getVendorFamily().getAws().setVpcId(_vpcId);

    // we only create a node here
    // interfaces are added to this node as we traverse subnets and
    // internetgateways

    return cfgNode;
  }
}
