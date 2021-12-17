package org.batfish.vendor.sonic.representation;

import static org.junit.Assert.assertEquals;

import com.google.common.collect.ImmutableList;
import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.vendor.sonic.representation.AclTable.Stage;
import org.batfish.vendor.sonic.representation.AclTable.Type;
import org.junit.Test;

public class AclTableTest {

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
        .addEqualityGroup(builder.setStage(Stage.INGRESS).build())
        .addEqualityGroup(builder.setType(Type.L3).build())
        .testEquals();
  }
}
