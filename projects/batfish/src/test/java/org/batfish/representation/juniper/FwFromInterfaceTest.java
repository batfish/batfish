package org.batfish.representation.juniper;

import static org.batfish.common.matchers.WarningMatchers.hasText;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.junit.Assert.assertEquals;

import com.google.common.collect.ImmutableList;
import org.batfish.common.Warnings;
import org.batfish.datamodel.TraceElement;
import org.batfish.datamodel.acl.AclLineMatchExprs;
import org.batfish.datamodel.acl.MatchSrcInterface;
import org.junit.Test;

/** Test for {@link FwFromInterface} */
public class FwFromInterfaceTest {

  @Test
  public void testToAclLineMatchExpr() {
    FwFromInterface from = new FwFromInterface("iface");
    JuniperConfiguration jc = new JuniperConfiguration();
    jc.getMasterLogicalSystem().getInterfaces().put("iface", new Interface("iface"));
    Warnings warnings = new Warnings(true, true, true);

    assertEquals(
        from.toAclLineMatchExpr(jc, null, warnings),
        new MatchSrcInterface(
            ImmutableList.of("iface"), TraceElement.of("Matched source interface iface")));
    assertThat(warnings.getRedFlagWarnings(), empty());
  }

  @Test
  public void testToAclLineMatchExpr_undefinedInterface() {
    FwFromInterface from = new FwFromInterface("iface");
    JuniperConfiguration jc = new JuniperConfiguration();
    Warnings warnings = new Warnings(true, true, true);

    assertEquals(from.toAclLineMatchExpr(jc, null, warnings), AclLineMatchExprs.FALSE);
    assertThat(warnings.getRedFlagWarnings(), contains(hasText("Missing interface 'iface'")));
  }
}
