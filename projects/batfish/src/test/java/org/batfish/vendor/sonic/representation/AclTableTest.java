package org.batfish.vendor.sonic.representation;

import static org.junit.Assert.assertEquals;

import com.google.common.collect.ImmutableList;
import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.junit.Test;

public class AclTableTest {

  @Test
  public void testJavaSerialization() {
    AclTable obj =
        AclTable.builder().setPorts(ImmutableList.of()).setStage("a").setType("b").build();
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
        .addEqualityGroup(builder.setStage("s").build())
        .addEqualityGroup(builder.setType("t").build())
        .testEquals();
  }
}
