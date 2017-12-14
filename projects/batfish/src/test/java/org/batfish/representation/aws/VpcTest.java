package org.batfish.representation.aws;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.batfish.common.util.CommonUtil;
import org.batfish.datamodel.Prefix;
import org.batfish.representation.aws_vpcs.Vpc;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.junit.Test;

public class VpcTest {

  @Test
  public void cidrBlocks() throws JSONException {
    String vpcs =
        CommonUtil.readResource("org/batfish/representation/aws/VpcTest-multipleCidrBlocks.json");

    JSONObject jObj = new JSONObject(vpcs);
    JSONArray vpcArray = jObj.getJSONArray("Vpcs");

    Vpc vpc = new Vpc(vpcArray.getJSONObject(0), null);

    assertThat(vpc.getCidrBlock(), equalTo(new Prefix("10.100.0.0/16")));
    assertThat(vpc.getCidrBlockAssociations().size(), equalTo(2));
    assertThat(vpc.getCidrBlockAssociations().contains(new Prefix("10.100.0.0/16")), equalTo(true));
    assertThat(vpc.getCidrBlockAssociations().contains(new Prefix("10.200.0.0/16")), equalTo(true));
  }
}
