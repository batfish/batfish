package org.batfish.datamodel;

import static org.batfish.datamodel.InactiveReason.ADMIN_DOWN;
import static org.batfish.datamodel.InactiveReason.LINE_DOWN;
import static org.batfish.datamodel.Interface.computeCiscoInterfaceType;
import static org.batfish.datamodel.Interface.isRealInterfaceName;
import static org.batfish.datamodel.InterfaceType.LOGICAL;
import static org.batfish.datamodel.InterfaceType.PHYSICAL;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import com.google.common.testing.EqualsTester;
import javax.annotation.Nullable;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.Interface.Dependency;
import org.batfish.datamodel.Interface.DependencyType;
import org.batfish.datamodel.ospf.OspfInterfaceSettings;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests of {@link org.batfish.datamodel.Interface} */
@RunWith(JUnit4.class)
public class InterfaceTest {

  @Test
  public void testDependencyEquals() {
    new EqualsTester()
        .addEqualityGroup(
            new Dependency("i1", DependencyType.BIND), new Dependency("i1", DependencyType.BIND))
        .addEqualityGroup(new Dependency("i1", DependencyType.AGGREGATE))
        .addEqualityGroup(new Dependency("i2", DependencyType.BIND))
        .testEquals();
  }

  private static void assertInvalidPhysicalInterface(
      boolean active, @Nullable Boolean adminUp, @Nullable Boolean lineUp) {
    assertNotNull(
        Interface.builder()
            .setActive(active)
            .setAdminUp(adminUp)
            .setLineUp(lineUp)
            .getInvalidStatusReason(PHYSICAL));
  }

  private void assertInvalidNonPhysicalInterface(boolean active, @Nullable boolean adminUp) {
    assertNotNull(
        Interface.builder().setActive(active).setAdminUp(adminUp).getInvalidStatusReason(LOGICAL));
  }

  private static void assertPhysicalInterfaceStatus(
      boolean active,
      @Nullable Boolean adminUp,
      @Nullable Boolean lineUp,
      boolean expectedActive,
      boolean expectedAdminUp,
      @Nullable Boolean expectedLineUp,
      @Nullable InactiveReason expectedInactiveReason) {
    Interface i =
        Interface.builder()
            .setName("foo")
            .setType(PHYSICAL)
            .setActive(active)
            .setAdminUp(adminUp)
            .setLineUp(lineUp)
            .build();
    assertThat(i.getActive(), equalTo(expectedActive));
    assertThat(i.getAdminUp(), equalTo(expectedAdminUp));
    assertThat(i.getLineUp(), equalTo(expectedLineUp));
    assertThat(i.getInactiveReason(), equalTo(expectedInactiveReason));
  }

  private static void assertNonPhysicalInterfaceStatus(
      boolean active,
      @Nullable Boolean adminUp,
      boolean expectedActive,
      boolean expectedAdminUp,
      @Nullable InactiveReason expectedInactiveReason) {
    Interface i =
        Interface.builder()
            .setName("foo")
            .setType(LOGICAL)
            .setActive(active)
            .setAdminUp(adminUp)
            .build();
    assertThat(i.getActive(), equalTo(expectedActive));
    assertThat(i.getAdminUp(), equalTo(expectedAdminUp));
    assertThat(i.getInactiveReason(), equalTo(expectedInactiveReason));
  }

  @Test
  public void testInterfaceStatus() {
    // no line status
    assertNonPhysicalInterfaceStatus(false, null, false, false, ADMIN_DOWN);
    assertNonPhysicalInterfaceStatus(false, false, false, false, ADMIN_DOWN);
    assertInvalidNonPhysicalInterface(false, true);
    assertNonPhysicalInterfaceStatus(true, null, true, true, null);
    assertInvalidNonPhysicalInterface(true, false);
    assertNonPhysicalInterfaceStatus(true, true, true, true, null);

    // line status
    assertPhysicalInterfaceStatus(false, null, null, false, true, false, LINE_DOWN);
    assertPhysicalInterfaceStatus(false, null, false, false, true, false, LINE_DOWN);
    assertPhysicalInterfaceStatus(false, null, true, false, false, true, ADMIN_DOWN);
    assertPhysicalInterfaceStatus(false, false, null, false, false, true, ADMIN_DOWN);
    assertPhysicalInterfaceStatus(false, false, false, false, false, false, ADMIN_DOWN);
    assertPhysicalInterfaceStatus(false, false, true, false, false, true, ADMIN_DOWN);
    assertPhysicalInterfaceStatus(false, true, null, false, true, false, LINE_DOWN);
    assertPhysicalInterfaceStatus(false, true, false, false, true, false, LINE_DOWN);
    assertInvalidPhysicalInterface(false, true, true);
    assertPhysicalInterfaceStatus(true, null, null, true, true, true, null);
    assertInvalidPhysicalInterface(true, null, false);
    assertPhysicalInterfaceStatus(true, null, true, true, true, true, null);
    assertInvalidPhysicalInterface(true, false, null);
    assertInvalidPhysicalInterface(true, false, false);
    assertInvalidPhysicalInterface(true, false, true);
    assertPhysicalInterfaceStatus(true, true, null, true, true, true, null);
    assertInvalidPhysicalInterface(true, true, false);
    assertPhysicalInterfaceStatus(true, true, true, true, true, true, null);
  }

  @Test
  public void testGetInvalidStatusReason() {
    {
      Interface.Builder b = Interface.builder().setActive(true).setAdminUp(false);
      assertThat(b.getInvalidStatusReason(LOGICAL), equalTo("Cannot be active when admin down"));
    }
    {
      Interface.Builder b = Interface.builder().setLineUp(true);
      assertThat(
          b.getInvalidStatusReason(LOGICAL),
          equalTo("Cannot set lineUp value for interface type: LOGICAL"));
    }
    {
      Interface.Builder b = Interface.builder().setActive(false).setAdminUp(true);
      assertThat(
          b.getInvalidStatusReason(LOGICAL),
          equalTo("Interface type 'LOGICAL' without line status cannot be inactive when admin up"));
    }
    {
      Interface.Builder b = Interface.builder().setActive(true).setLineUp(false);
      assertThat(
          b.getInvalidStatusReason(InterfaceType.PHYSICAL),
          equalTo("Cannot be active when line down"));
    }
    {
      Interface.Builder b = Interface.builder().setActive(false).setAdminUp(true).setLineUp(true);
      assertThat(
          b.getInvalidStatusReason(InterfaceType.PHYSICAL),
          equalTo("Cannot be inactive when admin up and line up"));
    }
  }

  @Test
  public void testInterfaceType() {
    assertThat(computeCiscoInterfaceType("TenGigE0/5/0/5/8"), equalTo(InterfaceType.PHYSICAL));
    assertThat(computeCiscoInterfaceType("TenGigE0/5/0/5/8.1"), equalTo(LOGICAL));
  }

  @Test
  public void testRealInterfaceName() {
    assertThat(isRealInterfaceName("Ethernet0"), equalTo(true));
    assertThat(isRealInterfaceName("ge-0/0/0"), equalTo(true));
    assertThat(isRealInterfaceName("asdfasdf"), equalTo(true));
    assertThat(isRealInterfaceName("null_interface"), equalTo(false));
    assertThat(isRealInterfaceName("unset_local_interface"), equalTo(false));
    assertThat(isRealInterfaceName("invalid_local_interface"), equalTo(false));
    assertThat(isRealInterfaceName("dynamic"), equalTo(false));
  }

  @Test
  public void testRoutingPolicySettingInBuilder() {
    String policy = "some_policy";
    Interface i = Interface.builder().setName("iface").setPacketPolicy(policy).build();
    assertThat(i.getPacketPolicyName(), equalTo(policy));
  }

  @Test
  public void testSerialization() {
    // TODO: more thorough testing
    Interface i =
        Interface.builder()
            .setMtu(7)
            .setName("ifaceName")
            .setOspfSettings(OspfInterfaceSettings.defaultSettingsBuilder().build())
            .setHmm(true)
            .build();

    // test (de)serialization
    Interface iDeserial = BatfishObjectMapper.clone(i, Interface.class);
    assertThat(i, equalTo(iDeserial));
  }

  @Test
  public void testJacksonSerialization() {
    // TODO: more thorough testing
    Interface obj = Interface.builder().setName("ifaceName").setHmm(true).build();
    assertEquals(obj, BatfishObjectMapper.clone(obj, Interface.class));
  }

  @Test
  public void testEquals() {
    // TODO: more thorough testing
    Interface.Builder b = Interface.builder().setName("ifaceName");
    new EqualsTester()
        .addEqualityGroup(b.build(), b.build())
        .addEqualityGroup(b.setHmm(true).build())
        .addEqualityGroup(b.setName("iface2").build())
        .testEquals();
  }
}
