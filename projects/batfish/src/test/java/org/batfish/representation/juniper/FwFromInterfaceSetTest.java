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

/** Test for {@link FwFromInterfaceSet} */
public class FwFromInterfaceSetTest {

  @Test
  public void testToAclLineMatchExpr() {
    FwFromInterfaceSet from = new FwFromInterfaceSet("ifset");
    InterfaceSet ifaceSet = new InterfaceSet();
    ifaceSet.addInterface("iface1");
    ifaceSet.addInterface("iface1.0");

    Interface iface1 = new Interface("iface1");
    iface1.getUnits().put("iface1.0", new Interface("iface1.0"));
    JuniperConfiguration jc = new JuniperConfiguration();
    jc.getMasterLogicalSystem().getInterfaceSets().put("ifset", ifaceSet);
    jc.getMasterLogicalSystem().getInterfaces().put("iface1", iface1);
    Warnings warnings = new Warnings(true, true, true);

    assertEquals(
        from.toAclLineMatchExpr(jc, null, warnings),
        new MatchSrcInterface(
            ImmutableList.of("iface1", "iface1.0"),
            TraceElement.of("Matched interface-set ifset")));
    assertThat(warnings.getRedFlagWarnings(), empty());
  }

  @Test
  public void testToAclLineMatchExpr_undefinedInterface() {
    FwFromInterfaceSet from = new FwFromInterfaceSet("ifset");
    InterfaceSet ifaceSet = new InterfaceSet();
    ifaceSet.addInterface("iface1");
    ifaceSet.addInterface("iface2");

    JuniperConfiguration jc = new JuniperConfiguration();
    jc.getMasterLogicalSystem().getInterfaceSets().put("ifset", ifaceSet);

    {
      // Interface set contains only undefined interfaces
      Warnings warnings = new Warnings(true, true, true);
      assertEquals(from.toAclLineMatchExpr(jc, null, warnings), AclLineMatchExprs.FALSE);
      assertThat(
          warnings.getRedFlagWarnings(),
          contains(hasText("Interface-set ifset does not contain any valid interfaces")));
    }
    {
      // Interface set contains both defined and undefined interfaces
      jc.getMasterLogicalSystem().getInterfaces().put("iface1", new Interface("iface1"));
      Warnings warnings = new Warnings(true, true, true);
      assertEquals(
          from.toAclLineMatchExpr(jc, null, warnings),
          new MatchSrcInterface(
              ImmutableList.of("iface1"), TraceElement.of("Matched interface-set ifset")));
      assertThat(warnings.getRedFlagWarnings(), empty());
    }
  }

  @Test
  public void testToAclLineMatchExpr_undefinedInterfaceSet() {
    FwFromInterfaceSet from = new FwFromInterfaceSet("ifset");
    JuniperConfiguration jc = new JuniperConfiguration();
    Warnings warnings = new Warnings(true, true, true);
    assertEquals(from.toAclLineMatchExpr(jc, null, warnings), AclLineMatchExprs.FALSE);
    assertThat(
        warnings.getRedFlagWarnings(), contains(hasText("Missing firewall interface-set 'ifset'")));
  }
}
