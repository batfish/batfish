package org.batfish.datamodel;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.Test;

/** Tests of {@link IpAccessList}. */
class IpAccessListTest {
  @Test
  void testToBuilder() {
    IpAccessList acl =
        IpAccessList.builder()
            .setName("name")
            .setLines(ImmutableList.of(new AclAclLine("name", "aclName")))
            .setSourceName("sourceName")
            .setSourceType("sourceType")
            .build();
    assertEquals(acl, acl.toBuilder().build());
  }
}
