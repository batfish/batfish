package org.batfish.datamodel;

import static org.junit.Assert.assertEquals;

import com.google.common.collect.ImmutableList;
import org.junit.Test;

/** Tests of {@link IpAccessList}. */
public class IpAccessListTest {
  @Test
  public void testToBuilder() {
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
