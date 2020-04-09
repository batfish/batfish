package org.batfish.representation.juniper;

import static org.junit.Assert.assertEquals;

import com.google.common.collect.ImmutableList;
import org.batfish.datamodel.acl.OrMatchExpr;
import org.junit.Test;

/** Test for {@link JunosApplicationSet} */
public class JunosApplicationSetTest {

  @Test
  public void testToAclLineMatchExpr() {
    JuniperConfiguration jc = new JuniperConfiguration();
    jc.setFilename("host");
    assertEquals(
        JunosApplicationSet.JUNOS_CIFS.toAclLineMatchExpr(jc, null),
        new OrMatchExpr(
            ImmutableList.of(
                JunosApplication.JUNOS_NETBIOS_SESSION.toAclLineMatchExpr(jc, null),
                JunosApplication.JUNOS_SMB_SESSION.toAclLineMatchExpr(jc, null)),
            ApplicationSetMember.getTraceElement(
                "host", JuniperStructureType.APPLICATION_SET, "junos-cifs")));
  }
}
