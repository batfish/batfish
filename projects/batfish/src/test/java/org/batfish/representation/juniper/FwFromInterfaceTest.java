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
    JuniperConfiguration jc = new JuniperConfiguration();
    Interface physical = new Interface("iface");
    Interface logical = new Interface("iface.0");
    physical.getUnits().put(logical.getName(), logical);
    jc.getMasterLogicalSystem().getInterfaces().put(physical.getName(), physical);
    {
      // match traffic from a physical interface
      String ifaceName = physical.getName();
      FwFromInterface from = new FwFromInterface(ifaceName);
      Warnings warnings = new Warnings(true, true, true);
      assertEquals(
          from.toAclLineMatchExpr(jc, null, warnings),
          new MatchSrcInterface(
              ImmutableList.of(ifaceName),
              TraceElement.of("Matched source interface " + ifaceName)));
      assertThat(warnings.getRedFlagWarnings(), empty());
    }
    {
      // match traffic from a logical interface
      String ifaceName = logical.getName();
      FwFromInterface from = new FwFromInterface(ifaceName);
      Warnings warnings = new Warnings(true, true, true);
      assertEquals(
          from.toAclLineMatchExpr(jc, null, warnings),
          new MatchSrcInterface(
              ImmutableList.of(ifaceName),
              TraceElement.of("Matched source interface " + ifaceName)));
      assertThat(warnings.getRedFlagWarnings(), empty());
    }
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
