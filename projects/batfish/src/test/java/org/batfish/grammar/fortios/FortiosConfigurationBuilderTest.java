package org.batfish.grammar.fortios;

import static org.batfish.grammar.fortios.FortiosConfigurationBuilder.policyValid;
import static org.batfish.grammar.fortios.FortiosConfigurationBuilder.serviceGroupContains;
import static org.batfish.grammar.fortios.FortiosConfigurationBuilder.serviceValid;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableMap;
import org.batfish.datamodel.IntegerSpace;
import org.batfish.representation.fortios.BatfishUUID;
import org.batfish.representation.fortios.Policy;
import org.batfish.representation.fortios.Service;
import org.batfish.representation.fortios.ServiceGroup;
import org.junit.Test;

public class FortiosConfigurationBuilderTest {
  @Test
  public void testPolicyValid() {
    assertNull(policyValid(buildCompletePolicy(), true));
  }

  @Test
  public void testPolicyValid_markedInvalid() {
    assertThat(policyValid(buildCompletePolicy(), false), equalTo("name is invalid"));
  }

  @Test
  public void testPolicyValid_missingSrcIntf() {
    Policy p = buildCompletePolicy();
    p.getSrcIntf().clear();
    assertThat(policyValid(p, true), equalTo("srcintf must be set"));
  }

  @Test
  public void testPolicyValid_missingDstIntf() {
    Policy p = buildCompletePolicy();
    p.getDstIntf().clear();
    assertThat(policyValid(p, true), equalTo("dstintf must be set"));
  }

  @Test
  public void testPolicyValid_missingSrcAddr() {
    Policy p = buildCompletePolicy();
    p.getSrcAddrUUIDs().clear();
    assertThat(policyValid(p, true), equalTo("srcaddr must be set"));
  }

  @Test
  public void testPolicyValid_missingDstAddr() {
    Policy p = buildCompletePolicy();
    p.getDstAddrUUIDs().clear();
    assertThat(policyValid(p, true), equalTo("dstaddr must be set"));
  }

  @Test
  public void testPolicyValid_missingService() {
    Policy p = buildCompletePolicy();
    p.getServiceUUIDs().clear();
    assertThat(policyValid(p, true), equalTo("service must be set"));
  }

  private static Policy buildCompletePolicy() {
    Policy p = new Policy("1");
    p.getSrcIntf().add("srcIntf");
    p.getDstIntf().add("dstIntf");
    p.getSrcAddrUUIDs().add(new BatfishUUID(1));
    p.getDstAddrUUIDs().add(new BatfishUUID(2));
    p.getServiceUUIDs().add(new BatfishUUID(3));
    return p;
  }

  @Test
  public void testServiceValid() {
    Service s = buildCompleteService();
    assertNull(serviceValid(s, true));

    // Default protocol is TCP/UDP/SCTP. Ensure setting protocol explicitly does not affect validity
    s.setProtocol(Service.Protocol.TCP_UDP_SCTP);
    assertNull(serviceValid(s, true));

    // ICMP, ICMP6, and IP services do not need any specific fields set to be valid
    s.setProtocol(Service.Protocol.ICMP);
    assertNull(serviceValid(s, true));
    s.setProtocol(Service.Protocol.ICMP6);
    assertNull(serviceValid(s, true));
    s.setProtocol(Service.Protocol.IP);
    assertNull(serviceValid(s, true));
  }

  @Test
  public void testServiceValid_markedInvalid() {
    assertThat(serviceValid(buildCompleteService(), false), equalTo("name is invalid"));
  }

  @Test
  public void testServiceValid_noDstPorts() {
    Service s = buildCompleteService(); // builds service with TCP dst ports set
    s.setTcpPortRangeDst(null);
    assertThat(serviceValid(s, true), equalTo("TCP/UDP/SCTP portrange cannot all be empty"));
  }

  @Test
  public void testServiceGroupContainsDirectly() {
    ServiceGroup parent = new ServiceGroup("parent", new BatfishUUID(0));

    BatfishUUID childId1 = new BatfishUUID(1);
    parent.getMemberUUIDs().add(childId1);

    BatfishUUID childId2 = new BatfishUUID(2);
    parent.getMemberUUIDs().add(childId2);

    assertTrue(serviceGroupContains(parent, childId1, ImmutableMap.of()));
    assertTrue(serviceGroupContains(parent, childId2, ImmutableMap.of()));
  }

  @Test
  public void testServiceGroupContainsIndirectly() {
    ServiceGroup parent = new ServiceGroup("parent", new BatfishUUID(0));

    BatfishUUID childId1 = new BatfishUUID(1);
    parent.getMemberUUIDs().add(childId1);

    BatfishUUID childId2 = new BatfishUUID(2);
    ServiceGroup child2 = new ServiceGroup("child2", childId2);
    parent.getMemberUUIDs().add(childId2);

    BatfishUUID grandChildId1 = new BatfishUUID(3);
    ServiceGroup grandChild1 = new ServiceGroup("grandChild1", grandChildId1);
    child2.getMemberUUIDs().add(grandChildId1);

    BatfishUUID greatGrandChildId1 = new BatfishUUID(4);
    grandChild1.getMemberUUIDs().add(greatGrandChildId1);

    assertTrue(
        serviceGroupContains(
            parent, grandChildId1, ImmutableMap.of(childId2, child2, grandChildId1, grandChild1)));
    assertTrue(
        serviceGroupContains(
            parent,
            greatGrandChildId1,
            ImmutableMap.of(childId2, child2, grandChildId1, grandChild1)));
  }

  @Test
  public void testServiceGroupContainsNoMatch() {
    ServiceGroup parent = new ServiceGroup("name", new BatfishUUID(0));

    BatfishUUID childId1 = new BatfishUUID(1);
    parent.getMemberUUIDs().add(childId1);

    BatfishUUID childId2 = new BatfishUUID(2);
    ServiceGroup child2 = new ServiceGroup("child2", childId2);
    parent.getMemberUUIDs().add(childId2);

    BatfishUUID grandChildId1 = new BatfishUUID(3);
    child2.getMemberUUIDs().add(grandChildId1);

    BatfishUUID orphan = new BatfishUUID(4);

    assertFalse(serviceGroupContains(parent, orphan, ImmutableMap.of(childId2, child2)));
  }

  private static Service buildCompleteService() {
    Service s = new Service("name", new BatfishUUID(1));
    s.setTcpPortRangeDst(IntegerSpace.of(1));
    return s;
  }
}
