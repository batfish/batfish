package org.batfish.datamodel;

import static org.batfish.datamodel.InactiveReason.ADMIN_DOWN;
import static org.batfish.datamodel.InactiveReason.BLACKLISTED;
import static org.batfish.datamodel.InactiveReason.FORCED_LINE_DOWN;
import static org.batfish.datamodel.InactiveReason.IGNORE_MGMT;
import static org.batfish.datamodel.InactiveReason.NODE_DOWN;
import static org.batfish.datamodel.InactiveReason.PARENT_DOWN;
import static org.batfish.datamodel.InactiveReason.PHYSICAL_NEIGHBOR_DOWN;
import static org.batfish.datamodel.Interface.isRealInterfaceName;
import static org.batfish.datamodel.InterfaceType.LOGICAL;
import static org.batfish.datamodel.InterfaceType.PHYSICAL;
import static org.batfish.datamodel.matchers.InterfaceMatchers.hasInactiveReason;
import static org.batfish.datamodel.matchers.InterfaceMatchers.isActive;
import static org.batfish.datamodel.matchers.InterfaceMatchers.isAdminUp;
import static org.batfish.datamodel.matchers.InterfaceMatchers.isBlacklisted;
import static org.batfish.datamodel.matchers.InterfaceMatchers.isLineUp;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertEquals;

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
        TestInterface.builder().setName("foo").setType(LOGICAL).build(),
        allOf(
            isActive(),
            isAdminUp(),
            isLineUp(nullValue()),
            hasInactiveReason(nullValue()),
            isBlacklisted(nullValue())));
    assertThat(
        TestInterface.builder().setName("foo").setType(LOGICAL).setAdminUp(false).build(),
        allOf(
            isActive(false),
            isAdminUp(false),
            isLineUp(nullValue()),
            hasInactiveReason(ADMIN_DOWN)));

    // line status
    assertThat(
        TestInterface.builder().setName("foo").setType(PHYSICAL).build(),
        allOf(
            isActive(),
            isAdminUp(),
            isLineUp(),
            hasInactiveReason(nullValue()),
            isBlacklisted(false)));
    assertThat(
        TestInterface.builder().setName("foo").setType(PHYSICAL).setAdminUp(false).build(),
        allOf(isActive(false), isAdminUp(false), isLineUp(true), hasInactiveReason(ADMIN_DOWN)));
    assertThat(
        TestInterface.builder().setName("foo").setType(PHYSICAL).setLineUp(false).build(),
        allOf(
            isActive(false),
            isAdminUp(true),
            isLineUp(false),
            hasInactiveReason(FORCED_LINE_DOWN)));
    assertThat(
        TestInterface.builder()
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
    TestInterface.builder().setName("foo").setType(LOGICAL).setLineUp(true).build();
  }

  @Test
  public void testDeactivate() {
    {
      // default case
      Interface i = TestInterface.builder().setName("foo").setType(PHYSICAL).build();
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
      Interface i = TestInterface.builder().setName("foo").setType(PHYSICAL).build();
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
      Interface i = TestInterface.builder().setName("foo").setType(PHYSICAL).build();
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
      // special case: FORCED_LINE_DOWN
      Interface i = TestInterface.builder().setName("foo").setType(PHYSICAL).build();
      assertThat(i, isActive());
      i.deactivate(FORCED_LINE_DOWN);
      assertThat(
          i,
          allOf(
              isActive(false),
              isAdminUp(),
              isLineUp(false),
              hasInactiveReason(FORCED_LINE_DOWN),
              isBlacklisted(false)));
    }
    {
      // special case: NODE_DOWN
      Interface i = TestInterface.builder().setName("foo").setType(PHYSICAL).build();
      assertThat(i, isActive());
      i.deactivate(NODE_DOWN);
      assertThat(
          i,
          allOf(
              isActive(false),
              isAdminUp(),
              isLineUp(false),
              hasInactiveReason(NODE_DOWN),
              isBlacklisted(false)));
    }
    {
      // special case: PHYSICAL_NEIGHBOR_DOWN
      Interface i = TestInterface.builder().setName("foo").setType(PHYSICAL).build();
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
    Interface i = TestInterface.builder().setName("foo").setType(PHYSICAL).build();
    i.deactivate(IGNORE_MGMT);

    _thrown.expect(IllegalStateException.class);
    _thrown.expectMessage("Cannot deactivate an inactive interface");
    i.deactivate(IGNORE_MGMT);
  }

  @Test
  public void testAdminDown() {
    // admin down
    Interface i = TestInterface.builder().setName("foo").setType(PHYSICAL).build();
    assertThat(i, allOf(isActive(), isAdminUp(), isLineUp()));
    i.adminDown();
    assertThat(
        i, allOf(isActive(false), isAdminUp(false), isLineUp(), hasInactiveReason(ADMIN_DOWN)));
  }

  @Test
  public void testAdminDownInvalidTwice() {
    Interface i = TestInterface.builder().setName("foo").setType(PHYSICAL).build();
    i.adminDown();

    _thrown.expect(IllegalStateException.class);
    _thrown.expectMessage(
        "Cannot administratively disable an interface that is already admin down");
    i.adminDown();
  }

  @Test
  public void testAdminDownInvalidInactive() {
    Interface i = TestInterface.builder().setName("foo").setType(PHYSICAL).build();
    i.deactivate(IGNORE_MGMT);

    _thrown.expect(IllegalStateException.class);
    _thrown.expectMessage("Cannot admin down an inactive interface");
    i.adminDown();
  }

  @Test
  public void testBlacklist() {
    {
      // blacklist
      Interface i = TestInterface.builder().setName("foo").setType(PHYSICAL).build();
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
      Interface i = TestInterface.builder().setName("foo").setType(PHYSICAL).build();
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
      // node down, then blacklist
      Interface i = TestInterface.builder().setName("foo").setType(PHYSICAL).build();
      assertThat(i, allOf(isActive(), isAdminUp(), isLineUp()));
      i.nodeDown();
      assertThat(
          i,
          allOf(
              isActive(false),
              isAdminUp(true),
              isLineUp(false),
              isBlacklisted(false),
              hasInactiveReason(NODE_DOWN)));
      i.blacklist();
      assertThat(
          i,
          allOf(
              isActive(false),
              isAdminUp(true),
              isLineUp(false),
              isBlacklisted(),
              hasInactiveReason(NODE_DOWN)));
    }
    {
      // deactivate, then blacklist
      Interface i = TestInterface.builder().setName("foo").setType(PHYSICAL).build();
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
              hasInactiveReason(IGNORE_MGMT)));
    }
  }

  @Test
  public void testBlacklistInvalidTwice() {
    Interface i = TestInterface.builder().setName("foo").setType(PHYSICAL).build();
    i.blacklist();

    _thrown.expect(IllegalStateException.class);
    _thrown.expectMessage("Cannot blacklist an interface that is already blacklisted");
    i.blacklist();
  }

  @Test
  public void testBlacklistInvalidType() {
    Interface i = TestInterface.builder().setName("foo").setType(LOGICAL).build();

    _thrown.expect(IllegalStateException.class);
    _thrown.expectMessage(
        "Cannot blacklist an interface of type 'LOGICAL' that has no line status");
    i.blacklist();
  }

  @Test
  public void testDisconnect() {
    {
      // disconnect
      Interface i = TestInterface.builder().setName("foo").setType(PHYSICAL).build();
      assertThat(i, allOf(isActive(), isAdminUp(), isLineUp()));
      i.disconnect(FORCED_LINE_DOWN);
      assertThat(
          i,
          allOf(
              isActive(false), isAdminUp(), isLineUp(false), hasInactiveReason(FORCED_LINE_DOWN)));
    }
    {
      // admin down, then disconnect
      Interface i = TestInterface.builder().setName("foo").setType(PHYSICAL).build();
      i.adminDown();
      assertThat(
          i, allOf(isActive(false), isAdminUp(false), isLineUp(), hasInactiveReason(ADMIN_DOWN)));
      i.disconnect(FORCED_LINE_DOWN);
      assertThat(
          i,
          allOf(isActive(false), isAdminUp(false), isLineUp(false), hasInactiveReason(ADMIN_DOWN)));
    }
    {
      // deactivate, then disconnect
      Interface i = TestInterface.builder().setName("foo").setType(PHYSICAL).build();
      i.deactivate(InactiveReason.IGNORE_MGMT);
      assertThat(
          i, allOf(isActive(false), isAdminUp(), isLineUp(), hasInactiveReason(IGNORE_MGMT)));
      i.disconnect(FORCED_LINE_DOWN);
      assertThat(
          i, allOf(isActive(false), isAdminUp(), isLineUp(false), hasInactiveReason(IGNORE_MGMT)));
    }
  }

  @Test
  public void testDisconnectInvalidTwice() {
    Interface i = TestInterface.builder().setName("foo").setType(PHYSICAL).build();
    i.disconnect(FORCED_LINE_DOWN);

    _thrown.expect(IllegalStateException.class);
    _thrown.expectMessage("Cannot disconnect a disconnected interface");
    i.disconnect(FORCED_LINE_DOWN);
  }

  @Test
  public void testDisconnectInvalidType() {
    Interface i = TestInterface.builder().setName("foo").setType(LOGICAL).build();

    _thrown.expect(IllegalStateException.class);
    _thrown.expectMessage(
        "Cannot disconnect an interface of type 'LOGICAL' that has no line status");
    i.disconnect(FORCED_LINE_DOWN);
  }

  @Test
  public void testPhysicalNeighborDown() {
    Interface i = TestInterface.builder().setName("foo").setType(PHYSICAL).build();
    assertThat(i, allOf(isActive(), isAdminUp(), isLineUp()));
    i.physicalNeighborDown();
    assertThat(
        i,
        allOf(
            isActive(false),
            isAdminUp(),
            isLineUp(false),
            hasInactiveReason(PHYSICAL_NEIGHBOR_DOWN)));
  }

  @Test
  public void testNodeDown() {
    // physical
    {
      Interface i = TestInterface.builder().setName("foo").setType(PHYSICAL).build();
      assertThat(i, allOf(isActive(), isAdminUp(), isLineUp()));
      i.nodeDown();
      assertThat(
          i, allOf(isActive(false), isAdminUp(), isLineUp(false), hasInactiveReason(NODE_DOWN)));
    }
    // non-physical
    {
      Interface i = TestInterface.builder().setName("foo").setType(LOGICAL).build();
      assertThat(i, allOf(isActive(), isAdminUp(), isLineUp(nullValue())));
      i.nodeDown();
      assertThat(
          i,
          allOf(isActive(false), isAdminUp(), isLineUp(nullValue()), hasInactiveReason(NODE_DOWN)));
    }
    // admin down, then node down
    {
      Interface i = TestInterface.builder().setName("foo").setType(PHYSICAL).build();
      assertThat(i, allOf(isActive(), isAdminUp(), isLineUp()));
      i.adminDown();
      i.nodeDown();
      assertThat(
          i,
          allOf(isActive(false), isAdminUp(false), isLineUp(false), hasInactiveReason(ADMIN_DOWN)));
    }
  }

  @Test
  public void testActivateForTest() {
    {
      // physical
      Interface i =
          TestInterface.builder().setName("foo").setType(PHYSICAL).setAdminUp(false).build();
      i.blacklist();
      assertThat(
          i,
          allOf(
              isActive(false),
              isAdminUp(false),
              isLineUp(false),
              isBlacklisted(),
              hasInactiveReason(not(nullValue()))));

      i.activateForTest();
      assertThat(
          i,
          allOf(
              isActive(),
              isAdminUp(),
              isLineUp(),
              isBlacklisted(false),
              hasInactiveReason(nullValue())));
    }
    {
      // non-physical
      Interface i =
          TestInterface.builder().setName("foo").setType(LOGICAL).setAdminUp(false).build();
      assertThat(
          i,
          allOf(
              isActive(false),
              isAdminUp(false),
              isLineUp(nullValue()),
              isBlacklisted(nullValue()),
              hasInactiveReason(not(nullValue()))));

      i.activateForTest();
      assertThat(
          i,
          allOf(
              isActive(),
              isAdminUp(),
              isLineUp(nullValue()),
              isBlacklisted(nullValue()),
              hasInactiveReason(nullValue())));
    }
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
    Interface i =
        TestInterface.builder()
            .setName("iface")
            .setType(InterfaceType.PHYSICAL)
            .setPacketPolicy(policy)
            .build();
    assertThat(i.getPacketPolicyName(), equalTo(policy));
  }

  @Test
  public void testSerialization() {
    // TODO: more thorough testing
    Interface i =
        TestInterface.builder()
            .setMtu(7)
            .setName("ifaceName")
            .setType(InterfaceType.PHYSICAL)
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
    Interface obj =
        TestInterface.builder()
            .setName("ifaceName")
            .setType(InterfaceType.PHYSICAL)
            .setHmm(true)
            .build();
    assertEquals(obj, BatfishObjectMapper.clone(obj, Interface.class));
  }

  @Test
  public void testEquals() {
    // TODO: more thorough testing
    Configuration c =
        Configuration.builder()
            .setHostname("c")
            .setConfigurationFormat(ConfigurationFormat.CISCO_IOS)
            .build();
    Interface.Builder b =
        TestInterface.builder().setOwner(c).setName("ifaceName").setType(InterfaceType.PHYSICAL);
    new EqualsTester()
        .addEqualityGroup(b.build(), b.build())
        .addEqualityGroup(b.setHmm(true).build())
        .addEqualityGroup(b.setName("iface2").build())
        .testEquals();
  }
}
