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
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.google.common.testing.EqualsTester;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.Interface.Dependency;
import org.batfish.datamodel.Interface.DependencyType;
import org.batfish.datamodel.ospf.OspfInterfaceSettings;
import org.junit.jupiter.api.Test;

/** Tests of {@link org.batfish.datamodel.Interface} */
class InterfaceTest {

  @Test
  void testDependencyEquals() {
    new EqualsTester()
        .addEqualityGroup(
            new Dependency("i1", DependencyType.BIND), new Dependency("i1", DependencyType.BIND))
        .addEqualityGroup(new Dependency("i1", DependencyType.AGGREGATE))
        .addEqualityGroup(new Dependency("i2", DependencyType.BIND))
        .testEquals();
  }

  @Test
  void testInterfaceStatus() {
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
  void testInterfaceStatusInvalid() {
    Throwable exception =
        assertThrows(
            IllegalStateException.class,
            () -> TestInterface.builder().setName("foo").setType(LOGICAL).setLineUp(true).build());
    assertThat(
        exception.getMessage(),
        containsString("Cannot set lineUp value for interface type: LOGICAL"));
  }

  @Test
  void testDeactivate() {
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
  void testDeactivateInvalidTwice() {
    Interface i = TestInterface.builder().setName("foo").setType(PHYSICAL).build();
    i.deactivate(IGNORE_MGMT);

    Throwable exception =
        assertThrows(IllegalStateException.class, () -> i.deactivate(IGNORE_MGMT));
    assertThat(exception.getMessage(), containsString("Cannot deactivate an inactive interface"));
  }

  @Test
  void testReactivateForAutostate() {
    Interface i = TestInterface.builder().setName("foo").setType(LOGICAL).build();
    i.deactivate(InactiveReason.AUTOSTATE_FAILURE);
    assertThat(i, allOf(isActive(false), hasInactiveReason(InactiveReason.AUTOSTATE_FAILURE)));

    i.reactivateForAutostate();
    assertThat(i, allOf(isActive(), hasInactiveReason(nullValue())));
  }

  @Test
  void testReactivateForAutostateAlreadyActive() {
    Interface i = TestInterface.builder().setName("foo").setType(LOGICAL).build();
    Throwable exception =
        assertThrows(IllegalStateException.class, () -> i.reactivateForAutostate());
    assertThat(exception.getMessage(), containsString("Cannot reactivate an active interface"));
  }

  @Test
  void testReactivateForAutostateWrongReason() {
    Interface i = TestInterface.builder().setName("foo").setType(PHYSICAL).build();
    i.deactivate(PARENT_DOWN);
    Throwable exception =
        assertThrows(IllegalStateException.class, () -> i.reactivateForAutostate());
    assertThat(exception.getMessage(), containsString("AUTOSTATE_FAILURE"));
  }

  @Test
  void testAdminDown() {
    // admin down
    Interface i = TestInterface.builder().setName("foo").setType(PHYSICAL).build();
    assertThat(i, allOf(isActive(), isAdminUp(), isLineUp()));
    i.adminDown();
    assertThat(
        i, allOf(isActive(false), isAdminUp(false), isLineUp(), hasInactiveReason(ADMIN_DOWN)));
  }

  @Test
  void testAdminDownInvalidTwice() {
    Interface i = TestInterface.builder().setName("foo").setType(PHYSICAL).build();
    i.adminDown();

    Throwable exception = assertThrows(IllegalStateException.class, () -> i.adminDown());
    assertThat(
        exception.getMessage(),
        containsString("Cannot administratively disable an interface that is already admin down"));
  }

  @Test
  void testAdminDownInvalidInactive() {
    Interface i = TestInterface.builder().setName("foo").setType(PHYSICAL).build();
    i.deactivate(IGNORE_MGMT);

    Throwable exception = assertThrows(IllegalStateException.class, () -> i.adminDown());
    assertThat(exception.getMessage(), containsString("Cannot admin down an inactive interface"));
  }

  @Test
  void testBlacklist() {
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
  void testBlacklistInvalidTwice() {
    Interface i = TestInterface.builder().setName("foo").setType(PHYSICAL).build();
    i.blacklist();

    Throwable exception = assertThrows(IllegalStateException.class, () -> i.blacklist());
    assertThat(
        exception.getMessage(),
        containsString("Cannot blacklist an interface that is already blacklisted"));
  }

  @Test
  void testBlacklistInvalidType() {
    Interface i = TestInterface.builder().setName("foo").setType(LOGICAL).build();

    Throwable exception = assertThrows(IllegalStateException.class, () -> i.blacklist());
    assertThat(
        exception.getMessage(),
        containsString("Cannot blacklist an interface of type 'LOGICAL' that has no line status"));
  }

  @Test
  void testDisconnect() {
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
  void testDisconnectInvalidTwice() {
    Interface i = TestInterface.builder().setName("foo").setType(PHYSICAL).build();
    i.disconnect(FORCED_LINE_DOWN);

    Throwable exception =
        assertThrows(IllegalStateException.class, () -> i.disconnect(FORCED_LINE_DOWN));
    assertThat(
        exception.getMessage(), containsString("Cannot disconnect a disconnected interface"));
  }

  @Test
  void testDisconnectInvalidType() {
    Interface i = TestInterface.builder().setName("foo").setType(LOGICAL).build();

    Throwable exception =
        assertThrows(IllegalStateException.class, () -> i.disconnect(FORCED_LINE_DOWN));
    assertThat(
        exception.getMessage(),
        containsString("Cannot disconnect an interface of type 'LOGICAL' that has no line status"));
  }

  @Test
  void testPhysicalNeighborDown() {
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
  void testNodeDown() {
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
  void testActivateForTest() {
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
  void testRealInterfaceName() {
    assertThat(isRealInterfaceName("Ethernet0"), equalTo(true));
    assertThat(isRealInterfaceName("ge-0/0/0"), equalTo(true));
    assertThat(isRealInterfaceName("asdfasdf"), equalTo(true));
    assertThat(isRealInterfaceName("null_interface"), equalTo(false));
    assertThat(isRealInterfaceName("unset_local_interface"), equalTo(false));
    assertThat(isRealInterfaceName("invalid_local_interface"), equalTo(false));
    assertThat(isRealInterfaceName("dynamic"), equalTo(false));
  }

  @Test
  void testRoutingPolicySettingInBuilder() {
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
  void testSerialization() {
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
  void testJacksonSerialization() {
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
  void testEquals() {
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
