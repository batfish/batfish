package org.batfish.vendor.sonic.representation;

import static org.junit.Assert.assertEquals;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.ImmutableList;
import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.vendor.sonic.representation.AclTable.Stage;
import org.batfish.vendor.sonic.representation.AclTable.Type;
import org.junit.Test;

public class AclTableTest {

  @Test
  public void testJacksonDeserialization() throws JsonProcessingException {
    String input =
        " {"
            + "    \"services\": ["
            + "        \"SSH\""
            + "    ],"
            + "    \"type\": \"CTRLPLANE\","
            + "    \"policy_desc\": \"ACLSSH\","
            + "    \"stage\": \"ingress\","
            + "    \"ports\": ["
            + "        \"Ethernet0\""
            + "    ]"
            + "}";
    assertEquals(
        AclTable.builder()
            .setPorts(ImmutableList.of("Ethernet0"))
            .setServices(ImmutableList.of("SSH"))
            .setStage(Stage.INGRESS)
            .setType(Type.CTRLPLANE)
            .build(),
        BatfishObjectMapper.mapper().readValue(input, AclTable.class));
  }

  @Test
  public void testJavaSerialization() {
    AclTable obj =
        AclTable.builder()
            .setPorts(ImmutableList.of())
            .setStage(Stage.EGRESS)
            .setType(Type.L3)
            .build();
    assertEquals(obj, SerializationUtils.clone(obj));
  }

  @SuppressWarnings("UnstableApiUsage")
  @Test
  public void testEquals() {
    AclTable.Builder builder = AclTable.builder();
    new EqualsTester()
        .addEqualityGroup(
            builder.build(), builder.build(), builder.setPorts(ImmutableList.of()).build())
        .addEqualityGroup(builder.setPorts(ImmutableList.of("a")).build())
        .addEqualityGroup(builder.setServices(ImmutableList.of("SNMP")).build())
        .addEqualityGroup(builder.setStage(Stage.INGRESS).build())
        .addEqualityGroup(builder.setType(Type.L3).build())
        .testEquals();
  }
}
