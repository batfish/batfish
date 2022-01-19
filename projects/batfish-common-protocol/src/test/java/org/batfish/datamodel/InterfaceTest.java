package org.batfish.datamodel;

import static org.batfish.datamodel.InactiveReason.ADMIN_DOWN;
import static org.batfish.datamodel.InactiveReason.BLACKLISTED;
import static org.batfish.datamodel.InactiveReason.IGNORE_MGMT;
import static org.batfish.datamodel.InactiveReason.LINE_DOWN;
import static org.batfish.datamodel.InactiveReason.PARENT_DOWN;
import static org.batfish.datamodel.InactiveReason.PHYSICAL_NEIGHBOR_DOWN;
import static org.batfish.datamodel.Interface.computeCiscoInterfaceType;
import static org.batfish.datamodel.Interface.isRealInterfaceName;
import static org.batfish.datamodel.InterfaceType.LOGICAL;
import static org.batfish.datamodel.InterfaceType.PHYSICAL;
import static org.batfish.datamodel.matchers.InterfaceMatchers.hasInactiveReason;
import static org.batfish.datamodel.matchers.InterfaceMatchers.isActive;
import static org.batfish.datamodel.matchers.InterfaceMatchers.isAdminUp;
import static org.batfish.datamodel.matchers.InterfaceMatchers.isBlacklisted;
import static org.batfish.datamodel.matchers.InterfaceMatchers.isLineUp;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import com.google.common.testing.EqualsTester;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.Interface.Dependency;
import org.batfish.datamodel.Interface.DependencyType;
import org.batfish.datamodel.ospf.OspfInterfaceSettings;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests of {@link org.batfish.datamodel.Interface} */
@RunWith(JUnit4.class)
public class InterfaceTest {

  @Rule public ExpectedException _thrown = ExpectedException.none();

  @Test
  public void testDependencyEquals() {
    new EqualsTester()
        .addEqualityGroup(
            new Dependency("i1", DependencyType.BIND), new Dependency("i1", DependencyType.BIND))
        .addEqualityGroup(new Dependency("i1", DependencyType.AGGREGATE))
        .addEqualityGroup(new Dependency("i2", DependencyType.BIND))
        .testEquals();
  }

  @Test
  public void testInterfaceStatus() {
    // no line status
    assertThat(
        Interface.builder().setName("foo").setType(LOGICAL).build(),
        allOf(
            isActive(),
            isAdminUp(),
            isLineUp(nullValue()),
            hasInactiveReason(null),
            isBlacklisted(nullValue())));
    assertThat(
        Interface.builder().setName("foo").setType(LOGICAL).setAdminUp(false).build(),
        allOf(
            isActive(false),
            isAdminUp(false),
            isLineUp(nullValue()),
            hasInactiveReason(ADMIN_DOWN)));

    // line status
    assertThat(
        Interface.builder().setName("foo").setType(PHYSICAL).build(),
        allOf(isActive(), isAdminUp(), isLineUp(), hasInactiveReason(null), isBlacklisted(false)));
    assertThat(
        Interface.builder().setName("foo").setType(PHYSICAL).setAdminUp(false).build(),
        allOf(isActive(false), isAdminUp(false), isLineUp(true), hasInactiveReason(ADMIN_DOWN)));
    assertThat(
        Interface.builder().setName("foo").setType(PHYSICAL).setLineUp(false).build(),
        allOf(isActive(false), isAdminUp(true), isLineUp(false), hasInactiveReason(LINE_DOWN)));
    assertThat(
        Interface.builder()
            .setName("foo")
            .setType(PHYSICAL)
            .setAdminUp(false)
            .setLineUp(false)
            .build(),
        allOf(isActive(false), isAdminUp(false), isLineUp(false), hasInactiveReason(ADMIN_DOWN)));
  }

  @Test
  public void testInterfaceStatusInvalid() {
    _thrown.expect(IllegalStateException.class);
    _thrown.expectMessage("Cannot set lineUp value for interface type: LOGICAL");
    Interface.builder().setName("foo").setType(LOGICAL).setLineUp(true).build();
  }

  @Test
  public void testDeactivate() {
    {
      // default case
      Interface i = Interface.builder().setName("foo").setType(PHYSICAL).build();
      i.deactivate(PARENT_DOWN);
      assertThat(
          i,
          allOf(
              isActive(false),
              isAdminUp(),
              isLineUp(),
              hasInactiveReason(PARENT_DOWN),
              isBlacklisted(false)));
    }
    {
      // special case: ADMIN_DOWN
      Interface i = Interface.builder().setName("foo").setType(PHYSICAL).build();
      assertThat(i, isActive());
      i.deactivate(ADMIN_DOWN);
      assertThat(
          i,
          allOf(
              isActive(false),
              isAdminUp(false),
              isLineUp(),
              hasInactiveReason(ADMIN_DOWN),
              isBlacklisted(false)));
    }
    {
      // special case: BLACKLISTED
      Interface i = Interface.builder().setName("foo").setType(PHYSICAL).build();
      assertThat(i, isActive());
      i.deactivate(BLACKLISTED);
      assertThat(
          i,
          allOf(
              isActive(false),
              isAdminUp(),
              isLineUp(false),
              hasInactiveReason(BLACKLISTED),
              isBlacklisted()));
    }
    {
      // special case: LINE_DOWN
      Interface i = Interface.builder().setName("foo").setType(PHYSICAL).build();
      assertThat(i, isActive());
      i.deactivate(LINE_DOWN);
      assertThat(
          i,
          allOf(
              isActive(false),
              isAdminUp(),
              isLineUp(false),
              hasInactiveReason(LINE_DOWN),
              isBlacklisted(false)));
    }
    {
      // special case: PHYSICAL_NEIGHBOR_DOWN
      Interface i = Interface.builder().setName("foo").setType(PHYSICAL).build();
      assertThat(i, isActive());
      i.deactivate(PHYSICAL_NEIGHBOR_DOWN);
      assertThat(
          i,
          allOf(
              isActive(false),
              isAdminUp(),
              isLineUp(false),
              hasInactiveReason(PHYSICAL_NEIGHBOR_DOWN),
              isBlacklisted(false)));
    }
  }

  @Test
  public void testDeactivateInvalidTwice() {
    Interface i = Interface.builder().setName("foo").setType(PHYSICAL).build();
    i.deactivate(IGNORE_MGMT);

    _thrown.expect(IllegalStateException.class);
    _thrown.expectMessage("Cannot deactivate an inactive interface");
    i.deactivate(IGNORE_MGMT);
  }

  @Test
  public void testAdminDown() {
    // admin down
    Interface i = Interface.builder().setName("foo").setType(PHYSICAL).build();
    assertThat(i, allOf(isActive(), isAdminUp(), isLineUp()));
    i.adminDown();
    assertThat(
        i, allOf(isActive(false), isAdminUp(false), isLineUp(), hasInactiveReason(ADMIN_DOWN)));
  }

  @Test
  public void testAdminDownInvalidTwice() {
    Interface i = Interface.builder().setName("foo").setType(PHYSICAL).build();
    i.adminDown();

    _thrown.expect(IllegalStateException.class);
    _thrown.expectMessage(
        "Cannot administratively disable an interface that is already admin down");
    i.adminDown();
  }

  @Test
  public void testAdminDownInvalidInactive() {
    Interface i = Interface.builder().setName("foo").setType(PHYSICAL).build();
    i.deactivate(IGNORE_MGMT);

    _thrown.expect(IllegalStateException.class);
    _thrown.expectMessage("Cannot admin down an inactive interface");
    i.adminDown();
  }

  @Test
  public void testBlacklist() {
    {
      // blacklist
      Interface i = Interface.builder().setName("foo").setType(PHYSICAL).build();
      assertThat(i, allOf(isActive(), isAdminUp(), isLineUp()));
      i.blacklist();
      assertThat(
          i,
          allOf(
              isActive(false),
              isAdminUp(true),
              isLineUp(false),
              isBlacklisted(),
              hasInactiveReason(BLACKLISTED)));
    }
    {
      // admin down, then blacklist
      Interface i = Interface.builder().setName("foo").setType(PHYSICAL).build();
      assertThat(i, allOf(isActive(), isAdminUp(), isLineUp()));
      i.adminDown();
      assertThat(
          i,
          allOf(
              isActive(false),
              isAdminUp(false),
              isLineUp(true),
              isBlacklisted(false),
              hasInactiveReason(ADMIN_DOWN)));
      i.blacklist();
      assertThat(
          i,
          allOf(
              isActive(false),
              isAdminUp(false),
              isLineUp(false),
              isBlacklisted(),
              hasInactiveReason(ADMIN_DOWN)));
    }
    {
      // deactivate, then blacklist
      Interface i = Interface.builder().setName("foo").setType(PHYSICAL).build();
      assertThat(i, allOf(isActive(), isAdminUp(), isLineUp()));
      i.deactivate(IGNORE_MGMT);
      assertThat(
          i,
          allOf(
              isActive(false),
              isAdminUp(),
              isLineUp(),
              isBlacklisted(false),
              hasInactiveReason(IGNORE_MGMT)));
      i.blacklist();
      assertThat(
          i,
          allOf(
              isActive(false),
              isAdminUp(true),
              isLineUp(false),
              isBlacklisted(),
              hasInactiveReason(BLACKLISTED)));
    }
  }

  @Test
  public void testBlacklistInvalidTwice() {
    Interface i = Interface.builder().setName("foo").setType(PHYSICAL).build();
    i.blacklist();

    _thrown.expect(IllegalStateException.class);
    _thrown.expectMessage("Cannot blacklist an interface that is already blacklisted");
    i.blacklist();
  }

  @Test
  public void testBlacklistInvalidLineDown() {
    Interface i = Interface.builder().setName("foo").setType(PHYSICAL).build();
    i.disconnect();

    _thrown.expect(IllegalStateException.class);
    _thrown.expectMessage("Cannot blacklist an interface that is already line down");
    i.blacklist();
  }

  @Test
  public void testBlacklistInvalidType() {
    Interface i = Interface.builder().setName("foo").setType(LOGICAL).build();

    _thrown.expect(IllegalStateException.class);
    _thrown.expectMessage(
        "Cannot blacklist an interface of type 'LOGICAL' that has no line status");
    i.blacklist();
  }

  @Test
  public void testDisconnect() {
    {
      // disconnect
      Interface i = Interface.builder().setName("foo").setType(PHYSICAL).build();
      assertThat(i, allOf(isActive(), isAdminUp(), isLineUp()));
      i.disconnect();
      assertThat(
          i, allOf(isActive(false), isAdminUp(), isLineUp(false), hasInactiveReason(LINE_DOWN)));
    }
    {
      // admin down, then disconnect
      Interface i = Interface.builder().setName("foo").setType(PHYSICAL).build();
      i.adminDown();
      assertThat(
          i, allOf(isActive(false), isAdminUp(false), isLineUp(), hasInactiveReason(ADMIN_DOWN)));
      i.disconnect();
      assertThat(
          i,
          allOf(isActive(false), isAdminUp(false), isLineUp(false), hasInactiveReason(ADMIN_DOWN)));
    }
    {
      // deactivate, then disconnect
      Interface i = Interface.builder().setName("foo").setType(PHYSICAL).build();
      i.deactivate(InactiveReason.IGNORE_MGMT);
      assertThat(
          i, allOf(isActive(false), isAdminUp(), isLineUp(), hasInactiveReason(IGNORE_MGMT)));
      i.disconnect();
      assertThat(
          i, allOf(isActive(false), isAdminUp(), isLineUp(false), hasInactiveReason(IGNORE_MGMT)));
    }
  }

  @Test
  public void testDisconnectInvalidTwice() {
    Interface i = Interface.builder().setName("foo").setType(PHYSICAL).build();
    i.disconnect();

    _thrown.expect(IllegalStateException.class);
    _thrown.expectMessage("Cannot disconnect a disconnected interface");
    i.disconnect();
  }

  @Test
  public void testDisconnectInvalidType() {
    Interface i = Interface.builder().setName("foo").setType(LOGICAL).build();

    _thrown.expect(IllegalStateException.class);
    _thrown.expectMessage(
        "Cannot disconnect an interface of type 'LOGICAL' that has no line status");
    i.disconnect();
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
