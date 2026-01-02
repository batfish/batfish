package org.batfish.datamodel.acl;

import static org.junit.Assert.assertEquals;

import com.google.common.testing.EqualsTester;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.TraceElement;
import org.junit.Test;

/** Test for {@link DeniedByAcl}. */
public final class DeniedByAclTest {
  @Test
  public void testEquals() {
    String name1 = "name1";
    String name2 = "name2";
    TraceElement te1 = TraceElement.of("te1");
    TraceElement te2 = TraceElement.of("te2");
    new EqualsTester()
        .addEqualityGroup(new DeniedByAcl(name1, te1), new DeniedByAcl(name1, te1))
        .addEqualityGroup(new DeniedByAcl(name2, te1))
        .addEqualityGroup(new DeniedByAcl(name1, te2))
        .testEquals();
  }

  @Test
  public void testJsonSerialization() {
    DeniedByAcl expected = new DeniedByAcl("name", "te");
    DeniedByAcl clone = (DeniedByAcl) BatfishObjectMapper.clone(expected, AclLineMatchExpr.class);
    assertEquals(expected, clone);
  }
}
