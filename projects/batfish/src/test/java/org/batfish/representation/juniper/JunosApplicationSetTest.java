package org.batfish.representation.juniper;

import static org.junit.Assert.assertEquals;

import com.google.common.collect.ImmutableList;
import org.batfish.datamodel.TraceElement;
import org.batfish.datamodel.acl.OrMatchExpr;
import org.junit.Test;

/** Test for {@link JunosApplicationSet} */
public class JunosApplicationSetTest {

  @Test
  public void testToAclLineMatchExpr() {
    assertEquals(
        JunosApplicationSet.JUNOS_CIFS.toAclLineMatchExpr(null, null),
        new OrMatchExpr(
            ImmutableList.of(
                JunosApplication.JUNOS_NETBIOS_SESSION.toAclLineMatchExpr(null, null),
                JunosApplication.JUNOS_SMB_SESSION.toAclLineMatchExpr(null, null)),
            TraceElement.of("Matched application-set junos-cifs")));
  }
}
