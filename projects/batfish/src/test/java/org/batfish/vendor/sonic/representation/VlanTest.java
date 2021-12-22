package org.batfish.vendor.sonic.representation;

import static org.junit.Assert.assertEquals;

import com.google.common.collect.ImmutableList;
import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.junit.Test;

public class VlanTest {

  @Test
  public void testJavaSerialization() {
    Vlan obj =
        Vlan.builder()
            .setDhcpServers(ImmutableList.of("a"))
            .setMembers(ImmutableList.of("a"))
            .setVlanId(1)
            .build();
    assertEquals(obj, SerializationUtils.clone(obj));
  }

  @SuppressWarnings("UnstableApiUsage")
  @Test
  public void testEquals() {
    Vlan.Builder builder = Vlan.builder();
    new EqualsTester()
        .addEqualityGroup(builder.build(), builder.build())
        .addEqualityGroup(builder.setDhcpServers(ImmutableList.of("a")))
        .addEqualityGroup(builder.setMembers(ImmutableList.of("a")).build())
        .addEqualityGroup(builder.setVlanId(1).build())
        .testEquals();
  }
}
