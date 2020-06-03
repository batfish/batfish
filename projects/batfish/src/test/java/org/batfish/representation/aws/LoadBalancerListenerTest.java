package org.batfish.representation.aws;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.common.util.Resources;
import org.batfish.representation.aws.LoadBalancer.Protocol;
import org.batfish.representation.aws.LoadBalancerListener.ActionType;
import org.batfish.representation.aws.LoadBalancerListener.DefaultAction;
import org.batfish.representation.aws.LoadBalancerListener.Listener;
import org.junit.Test;

public class LoadBalancerListenerTest {

  @Test
  public void testDeserialization() throws IOException {
    String text =
        Resources.readResource(
            "org/batfish/representation/aws/LoadBalancerListenerTest.json", StandardCharsets.UTF_8);

    JsonNode json = BatfishObjectMapper.mapper().readTree(text);
    Region region = new Region("r1");
    region.addConfigElement(json, null, null);

    assertThat(
        region.getLoadBalancerListeners(),
        equalTo(
            ImmutableMap.of(
                "arn:aws:elasticloadbalancing:us-east-2:554773406868:loadbalancer/net/lb-lb/6f57a43b75d8f2c1",
                new LoadBalancerListener(
                    "arn:aws:elasticloadbalancing:us-east-2:554773406868:loadbalancer/net/lb-lb/6f57a43b75d8f2c1",
                    ImmutableList.of(
                        new Listener(
                            "arn:aws:elasticloadbalancing:us-east-2:554773406868:listener/net/lb-lb/6f57a43b75d8f2c1/c866a2736f6c4d68",
                            ImmutableList.of(
                                new DefaultAction(
                                    1,
                                    "arn:aws:elasticloadbalancing:us-east-2:554773406868:targetgroup/target1/10b6be82e58c40a8",
                                    ActionType.FORWARD)),
                            Protocol.TCP,
                            80))))));
  }

  @Test
  public void testDeserializationDefaultAction_nullOrder() throws IOException {
    String text =
        Resources.readResource(
            "org/batfish/representation/aws/DefaultAction.json", StandardCharsets.UTF_8);

    DefaultAction defaultAction = BatfishObjectMapper.mapper().readValue(text, DefaultAction.class);
    assertThat(
        defaultAction,
        equalTo(
            new DefaultAction(
                null,
                "arn:aws:elasticloadbalancing:us-east-2:554773406868:targetgroup/target1/10b6be82e58c40a8",
                ActionType.FORWARD)));
  }
}
