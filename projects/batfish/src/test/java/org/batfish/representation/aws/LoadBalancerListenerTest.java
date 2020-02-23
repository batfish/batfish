package org.batfish.representation.aws;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.io.IOException;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.common.util.CommonUtil;
import org.batfish.representation.aws.LoadBalancer.Protocol;
import org.batfish.representation.aws.LoadBalancerListener.ActionType;
import org.batfish.representation.aws.LoadBalancerListener.DefaultAction;
import org.junit.Test;

public class LoadBalancerListenerTest {

  @Test
  public void testDeserialization() throws IOException {
    String text =
        CommonUtil.readResource("org/batfish/representation/aws/LoadBalancerListenerTest.json");

    JsonNode json = BatfishObjectMapper.mapper().readTree(text);
    Region region = new Region("r1");
    region.addConfigElement(json, null, null);

    assertThat(
        region.getLoadBalancerListeners(),
        equalTo(
            ImmutableMap.of(
                "arn:aws:elasticloadbalancing:us-east-2:554773406868:listener/net/lb-lb/6f57a43b75d8f2c1/281a594cec3f37fa",
                new LoadBalancerListener(
                    "arn:aws:elasticloadbalancing:us-east-2:554773406868:listener/net/lb-lb/6f57a43b75d8f2c1/281a594cec3f37fa",
                    "arn:aws:elasticloadbalancing:us-east-2:554773406868:loadbalancer/net/lb-lb/6f57a43b75d8f2c1",
                    ImmutableList.of(
                        new DefaultAction(
                            1,
                            "arn:aws:elasticloadbalancing:us-east-2:554773406868:targetgroup/target1/10b6be82e58c40a8",
                            ActionType.FORWARD)),
                    Protocol.TCP))));
  }
}
