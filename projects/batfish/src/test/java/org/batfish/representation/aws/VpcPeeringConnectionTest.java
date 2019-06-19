package org.batfish.representation.aws;

import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

import org.batfish.common.util.CommonUtil;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.junit.Test;

public class VpcPeeringConnectionTest {

  @Test
  public void vpcPeeringConnections() throws JSONException {
    String text =
        CommonUtil.readResource("org/batfish/representation/aws/VpcPeeringConnections.json");

    JSONObject jObj = new JSONObject(text);
    Region region = new Region("r1");
    region.addConfigElement(jObj, null, null);

    /*
     * We should have an entry for the vpc peering connection with status code "active", but not
     * for the one with status code "deleted".
     */
    assertThat(region.getVpcPeeringConnections(), hasKey("pcx-f754069e"));
    assertThat(region.getVpcPeeringConnections(), not(hasKey("pcx-4ee8b427")));
  }
}
