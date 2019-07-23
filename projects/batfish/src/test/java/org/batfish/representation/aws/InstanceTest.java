package org.batfish.representation.aws;

import static org.batfish.representation.aws.AwsVpcEntity.JSON_KEY_INSTANCES;
import static org.batfish.representation.aws.AwsVpcEntity.JSON_KEY_RESERVATIONS;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.common.util.CommonUtil;
import org.batfish.representation.aws.Instance.Status;
import org.junit.Test;

/** Test for {@link Instance} */
public class InstanceTest {

  @Test
  public void reservations() throws IOException {
    String text = CommonUtil.readResource("org/batfish/representation/aws/InstanceTest.json");

    JsonNode json = BatfishObjectMapper.mapper().readTree(text);
    ArrayNode reservationArray = (ArrayNode) json.get(JSON_KEY_RESERVATIONS);
    List<Instance> instances = new LinkedList<>();

    for (int index = 0; index < reservationArray.size(); index++) {
      ArrayNode instanceArray = (ArrayNode) reservationArray.get(index).get(JSON_KEY_INSTANCES);
      for (int instIndex = 0; instIndex < instanceArray.size(); instIndex++) {
        instances.add(
            BatfishObjectMapper.mapper()
                .convertValue(instanceArray.get(instIndex), Instance.class));
      }
    }

    assertThat(
        instances,
        equalTo(
            ImmutableList.of(
                new Instance(
                    "i-08e529f98f5659289",
                    "vpc-815775e7",
                    "subnet-9a0c48fc",
                    ImmutableList.of("sg-adcd87d0"),
                    ImmutableList.of("eni-a9d44c8a"),
                    ImmutableMap.of("Name", "fin-app-02"),
                    Status.PENDING),
                new Instance(
                    "i-05f467abe21e9b883",
                    "vpc-815775e7",
                    "subnet-9a0c48fc",
                    ImmutableList.of("sg-adcd87d0"),
                    ImmutableList.of("eni-bd11cc9d"),
                    ImmutableMap.of(
                        "purpose",
                        "solarwinds",
                        "tag",
                        "GNS3-BGP-Test",
                        "creator",
                        "ratul",
                        "Name",
                        "shutdown"),
                    Status.STOPPED))));
  }
}
