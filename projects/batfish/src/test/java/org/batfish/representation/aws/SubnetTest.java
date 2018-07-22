package org.batfish.representation.aws;

import static org.batfish.representation.aws.AwsVpcEntity.JSON_KEY_SUBNETS;
import static org.batfish.representation.aws.matchers.SubnetMatchers.hasCidrBlock;
import static org.batfish.representation.aws.matchers.SubnetMatchers.hasId;
import static org.batfish.representation.aws.matchers.SubnetMatchers.hasVpcId;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

import java.util.LinkedList;
import java.util.List;
import org.batfish.common.util.CommonUtil;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

public class SubnetTest {

  private List<Subnet> _subnetList;

  @Before
  public void setup() throws JSONException {
    String text = CommonUtil.readResource("org/batfish/representation/aws/SubnetTest.json");
    JSONObject jObj = new JSONObject(text);
    JSONArray subnetArray = jObj.getJSONArray(JSON_KEY_SUBNETS);
    _subnetList = new LinkedList<>();
    for (int i = 0; i < subnetArray.length(); i++) {
      _subnetList.add(new Subnet(subnetArray.getJSONObject(i)));
    }
  }

  @Test
  public void testSubnet() {
    // checking the count of subnets initialized
    assertThat(_subnetList, hasSize(1));

    // checking the different attributes of subnet
    Subnet subnet = _subnetList.get(0);
    assertThat(subnet, hasCidrBlock(Prefix.parse("172.31.0.0/20")));
    assertThat(subnet, hasId("subnet-1"));
    assertThat(subnet, hasVpcId("vpc-1"));
  }

  @Test
  public void testGetNextIp() {
    // checking the count of subnets initialized
    assertThat(_subnetList, hasSize(1));

    // test getNextIp()
    Subnet subnet = _subnetList.get(0);
    assertThat(subnet.getNextIp(), equalTo(new Ip("172.31.0.2")));
    assertThat(subnet.getNextIp(), equalTo(new Ip("172.31.0.3")));
    assertThat(subnet.getNextIp(), equalTo(new Ip("172.31.0.4")));
  }
}
