package org.batfish.vendor.sonic.representation;

import static org.junit.Assert.assertEquals;

import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.vendor.sonic.representation.VlanMember.TaggingMode;
import org.junit.Test;

public class VlanMemberTest {

  @Test
  public void testJavaSerialization() {
    VlanMember obj = VlanMember.builder().setTaggingMode(TaggingMode.UNTAGGED).build();
    assertEquals(obj, SerializationUtils.clone(obj));
  }

  @SuppressWarnings("UnstableApiUsage")
  @Test
  public void testEquals() {
    VlanMember.Builder builder = VlanMember.builder();
    new EqualsTester()
        .addEqualityGroup(builder.build(), builder.build())
        .addEqualityGroup(builder.setTaggingMode(TaggingMode.UNTAGGED).build())
        .testEquals();
  }
}
