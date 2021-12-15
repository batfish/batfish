package org.batfish.vendor.sonic.representation;

import static org.junit.Assert.assertEquals;

import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.datamodel.Prefix;
import org.batfish.vendor.sonic.representation.AclRule.PacketAction;
import org.junit.Test;

public class AclRuleTest {

  @Test
  public void testJavaSerialization() {
    AclRule obj = AclRule.builder().build();
    assertEquals(obj, SerializationUtils.clone(obj));
  }

  @SuppressWarnings("UnstableApiUsage")
  @Test
  public void testEquals() {
    AclRule.Builder builder = AclRule.builder();
    new EqualsTester()
        .addEqualityGroup(builder.build(), builder.build())
        .addEqualityGroup(builder.setIpProtocol(17).build())
        .addEqualityGroup(builder.setDstIp(Prefix.parse("1.1.1.1/24")).build())
        .addEqualityGroup(builder.setSrcIp(Prefix.parse("1.1.1.1/24")).build())
        .addEqualityGroup(builder.setL4DstPort(17).build())
        .addEqualityGroup(builder.setL4SrcPort(17).build())
        .addEqualityGroup(builder.setPriority(17).build())
        .addEqualityGroup(builder.setPacketAction(PacketAction.ACCEPT).build())
        .testEquals();
  }
}
