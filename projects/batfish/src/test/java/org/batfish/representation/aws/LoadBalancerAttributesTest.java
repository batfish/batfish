package org.batfish.representation.aws;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.io.IOException;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.common.util.CommonUtil;
import org.batfish.representation.aws.LoadBalancerAttributes.Attribute;
import org.junit.Test;

public class LoadBalancerAttributesTest {

  @Test
  public void testDeserialization() throws IOException {
    String text =
        CommonUtil.readResource("org/batfish/representation/aws/LoadBalancerAttributesTest.json");

    JsonNode json = BatfishObjectMapper.mapper().readTree(text);
    Region region = new Region("r1");
    region.addConfigElement(json, null, null);

    /** inactive load balancer should not show */
    assertThat(
        region.getLoadBalancerAttributes(),
        equalTo(
            ImmutableMap.of(
                "arn:aws:elasticloadbalancing:us-east-2:554773406868:loadbalancer/net/lb-lb/6f57a43b75d8f2c1",
                new LoadBalancerAttributes(
                    "arn:aws:elasticloadbalancing:us-east-2:554773406868:loadbalancer/net/lb-lb/6f57a43b75d8f2c1",
                    ImmutableList.of(
                        new Attribute("load_balancing.cross_zone.enabled", "false"),
                        new Attribute("access_logs.s3.prefix", ""))))));
  }
}
