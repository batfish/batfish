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
import org.batfish.representation.aws.TargetGroup.Type;
import org.junit.Test;

public class TargetGroupTest {

  @Test
  public void testDeserialization() throws IOException {
    String text = CommonUtil.readResource("org/batfish/representation/aws/TargetGroupTest.json");

    JsonNode json = BatfishObjectMapper.mapper().readTree(text);
    Region region = new Region("r1");
    region.addConfigElement(json, null, null);

    assertThat(
        region.getTargetGroups(),
        equalTo(
            ImmutableMap.of(
                "arn:aws:elasticloadbalancing:us-east-2:554773406868:targetgroup/target1/10b6be82e58c40a8",
                new TargetGroup(
                    "arn:aws:elasticloadbalancing:us-east-2:554773406868:targetgroup/target1/10b6be82e58c40a8",
                    ImmutableList.of(
                        "arn:aws:elasticloadbalancing:us-east-2:554773406868:loadbalancer/net/lb-lb/6f57a43b75d8f2c1"),
                    Protocol.TCP,
                    80,
                    "target1",
                    Type.IP))));
  }
}
