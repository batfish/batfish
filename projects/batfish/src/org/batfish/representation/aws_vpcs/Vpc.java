package org.batfish.representation.aws_vpcs;

import java.io.Serializable;

import org.batfish.common.BatfishLogger;
import org.batfish.representation.Configuration;
import org.batfish.representation.Prefix;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

public class Vpc implements AwsVpcEntity, Serializable {

   private static final long serialVersionUID = 1L;

   private Prefix _cidrBlock;

   private String _vpcId;

   public Vpc(JSONObject jObj, BatfishLogger logger) throws JSONException {
      _vpcId = jObj.getString(JSON_KEY_VPC_ID);
      _cidrBlock = new Prefix(jObj.getString(JSON_KEY_CIDR_BLOCK));
   }

   @Override
   public String getId() {
      return _vpcId;
   }

   public Configuration toConfigurationNode(
         AwsVpcConfiguration awsVpcConfiguration) {
      Configuration cfgNode = new Configuration(_vpcId);

      // we only create a node here
      // interfaces are added to this node as we traverse subnets and
      // internetgateways

      return cfgNode;
   }
}
