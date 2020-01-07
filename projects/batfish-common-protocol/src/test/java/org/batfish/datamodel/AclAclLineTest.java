package org.batfish.datamodel;

import static org.junit.Assert.assertEquals;

import com.google.common.testing.EqualsTester;
import java.io.IOException;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.Test;

/** Tests of {@link AclAclLine} */
public class AclAclLineTest {

  @Test
  public void testEquals() throws IOException {
    AclAclLine aclAclLine = new AclAclLine("lineName", "aclName");
    AclAclLine clone = BatfishObjectMapper.clone(aclAclLine, AclAclLine.class);
    assertEquals(aclAclLine, clone);

    new EqualsTester()
        .addEqualityGroup(new AclAclLine("name1", "acl1"), new AclAclLine("name1", "acl1"))
        .addEqualityGroup(new AclAclLine("name2", "acl1"))
        .addEqualityGroup(new AclAclLine("name1", "acl2"))
        .addEqualityGroup(new Object())
        .testEquals();
  }

  @Test
  public void testJsonSerialization() throws IOException {
    AclAclLine aclAclLine = new AclAclLine("lineName", "aclName");
    AclAclLine clone = BatfishObjectMapper.clone(aclAclLine, AclAclLine.class);
    assertEquals(aclAclLine, clone);
  }
}
