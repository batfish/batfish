package org.batfish.representation.aws;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.batfish.common.util.Resources.readResource;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.io.IOException;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.representation.aws.LoadBalancerTargetHealth.HealthState;
import org.batfish.representation.aws.LoadBalancerTargetHealth.TargetHealth;
import org.batfish.representation.aws.LoadBalancerTargetHealth.TargetHealthDescription;
import org.junit.Test;

public class LoadBalancerTargetHealthTest {

  @Test
  public void testDeserialization() throws IOException {
    String text =
        readResource("org/batfish/representation/aws/LoadBalancerTargetHealthTest.json", UTF_8);

    JsonNode json = BatfishObjectMapper.mapper().readTree(text);
    Region region = new Region("r1");
    region.addConfigElement(json, null, null);

    assertThat(
        region.getLoadBalancerTargetHealths(),
        equalTo(
            ImmutableMap.of(
                "arn:aws:elasticloadbalancing:us-east-2:554773406868:targetgroup/target1/10b6be82e58c40a8",
                new LoadBalancerTargetHealth(
                    "arn:aws:elasticloadbalancing:us-east-2:554773406868:targetgroup/target1/10b6be82e58c40a8",
                    ImmutableList.of(
                        new TargetHealthDescription(
                            new LoadBalancerTarget("us-east-2a", "10.10.1.4", 80),
                            new TargetHealth(
                                "Health checks failed",
                                "Target.FailedHealthChecks",
                                HealthState.UNHEALTHY)),
                        new TargetHealthDescription(
                            new LoadBalancerTarget("all", "10.11.1.4", 80),
                            new TargetHealth(
                                "Health checks failed",
                                "Target.FailedHealthChecks",
                                HealthState.HEALTHY)))))));
  }
}
