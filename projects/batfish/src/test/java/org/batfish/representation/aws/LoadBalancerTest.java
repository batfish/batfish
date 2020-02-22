package org.batfish.representation.aws;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.io.IOException;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.common.util.CommonUtil;
import org.batfish.representation.aws.LoadBalancer.AvailabilityZone;
import org.batfish.representation.aws.LoadBalancer.Scheme;
import org.batfish.representation.aws.LoadBalancer.Type;
import org.junit.Test;

public class LoadBalancerTest {

  @Test
  public void testDeserialization() throws IOException {
    String text = CommonUtil.readResource("org/batfish/representation/aws/LoadBalancerTest.json");

    JsonNode json = BatfishObjectMapper.mapper().readTree(text);
    Region region = new Region("r1");
    region.addConfigElement(json, null, null);

    /** inactive load balancer should not show */
    assertThat(
        region.getLoadBalancers(),
        equalTo(
            ImmutableMap.of(
                "arn:aws:elasticloadbalancing:us-east-2:554773406868:loadbalancer/net/lb-lb/6f57a43b75d8f2c1",
                new LoadBalancer(
                    "arn:aws:elasticloadbalancing:us-east-2:554773406868:loadbalancer/net/lb-lb/6f57a43b75d8f2c1",
                    ImmutableList.of(
                        new AvailabilityZone("subnet-01822d50b2db5a4a0", "us-east-2a"),
                        new AvailabilityZone("subnet-09f78a95e9df6b959", "us-east-2b")),
                    "lb-lb",
                    Scheme.INTERNET_FACING,
                    Type.NETWORK,
                    "vpc-08afc01f5013ddc43"),
                "arn-application",
                new LoadBalancer(
                    "arn-application",
                    ImmutableList.of(),
                    null,
                    Scheme.INTERNAL,
                    Type.APPLICATION,
                    "vpc-08afc01f5013ddc43"))));
  }
}
