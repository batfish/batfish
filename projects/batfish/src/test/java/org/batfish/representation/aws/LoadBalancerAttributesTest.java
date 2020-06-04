package org.batfish.representation.aws;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.batfish.common.util.Resources.readResource;
import static org.batfish.representation.aws.LoadBalancerAttributes.CROSS_ZONE_KEY;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.io.IOException;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.representation.aws.LoadBalancerAttributes.Attribute;
import org.junit.Test;

public class LoadBalancerAttributesTest {

  @Test
  public void testDeserialization() throws IOException {
    String text =
        readResource("org/batfish/representation/aws/LoadBalancerAttributesTest.json", UTF_8);

    JsonNode json = BatfishObjectMapper.mapper().readTree(text);
    Region region = new Region("r1");
    region.addConfigElement(json, null, null);

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

  @Test
  public void testGetCrossZoneLoadBalancing() {
    // key does not exist
    assertFalse(new LoadBalancerAttributes("arn", ImmutableList.of()).getCrossZoneLoadBalancing());

    assertFalse(
        new LoadBalancerAttributes("arn", ImmutableList.of(new Attribute(CROSS_ZONE_KEY, "false")))
            .getCrossZoneLoadBalancing());

    assertTrue(
        new LoadBalancerAttributes("arn", ImmutableList.of(new Attribute(CROSS_ZONE_KEY, "true")))
            .getCrossZoneLoadBalancing());

    // first one winds
    assertFalse(
        new LoadBalancerAttributes(
                "arn",
                ImmutableList.of(
                    new Attribute(CROSS_ZONE_KEY, "false"), new Attribute(CROSS_ZONE_KEY, "true")))
            .getCrossZoneLoadBalancing());
  }
}
