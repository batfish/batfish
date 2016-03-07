package org.batfish.representation.aws_vpcs;

import java.io.Serializable;
import org.batfish.common.BatfishLogger;
import org.batfish.representation.Prefix;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

public class VpcPeeringConnection implements AwsVpcEntity, Serializable {

   private static final long serialVersionUID = 1L;

   private Prefix _accepterVpcCidrBlock;

   private String _accepterVpcId;

   private Prefix _requesterVpcCidrBlock;

   private String _requesterVpcId;

   private String _vpcPeeringConnectionId;

   public VpcPeeringConnection(JSONObject jObj, BatfishLogger logger)
         throws JSONException {
      _vpcPeeringConnectionId = jObj
            .getString(JSON_KEY_VPC_PEERING_CONNECTION_ID);

      JSONObject accepterJson = jObj.getJSONObject(JSON_KEY_ACCEPTER_VPC_INFO);
      _accepterVpcId = accepterJson.getString(JSON_KEY_VPC_ID);
      _accepterVpcCidrBlock = new Prefix(
            accepterJson.getString(JSON_KEY_CIDR_BLOCK));

      JSONObject requesterJson = jObj
            .getJSONObject(JSON_KEY_REQUESTER_VPC_INFO);
      _requesterVpcId = accepterJson.getString(JSON_KEY_VPC_ID);
      _requesterVpcCidrBlock = new Prefix(
            requesterJson.getString(JSON_KEY_CIDR_BLOCK));
   }

   @Override
   public String getId() {
      return _vpcPeeringConnectionId;
   }
}
