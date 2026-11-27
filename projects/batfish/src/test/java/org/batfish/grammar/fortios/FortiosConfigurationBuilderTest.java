package org.batfish.grammar.fortios;

import static org.batfish.common.matchers.ParseWarningMatchers.hasComment;
import static org.batfish.common.matchers.ParseWarningMatchers.hasText;
import static org.batfish.common.matchers.WarningsMatchers.hasParseWarnings;
import static org.batfish.grammar.fortios.FortiosConfigurationBuilder.addrgrpContains;
import static org.batfish.grammar.fortios.FortiosConfigurationBuilder.policyValid;
import static org.batfish.grammar.fortios.FortiosConfigurationBuilder.serviceGroupContains;
import static org.batfish.grammar.fortios.FortiosConfigurationBuilder.serviceValid;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.common.collect.ImmutableMap;
import org.antlr.v4.runtime.CommonToken;
import org.batfish.common.Warnings;
import org.batfish.config.Settings;
import org.batfish.datamodel.IntegerSpace;
import org.batfish.grammar.fortios.FortiosParser.Cv_editContext;
import org.batfish.grammar.fortios.FortiosParser.Cve_configContext;
import org.batfish.grammar.fortios.FortiosParser.StrContext;
import org.batfish.grammar.fortios.FortiosParser.Str_contentContext;
import org.batfish.grammar.silent_syntax.SilentSyntaxCollection;
import org.batfish.representation.fortios.Addrgrp;
import org.batfish.representation.fortios.BatfishUUID;
import org.batfish.representation.fortios.FortiosConfiguration;
import org.batfish.representation.fortios.Policy;
import org.batfish.representation.fortios.Service;
import org.batfish.representation.fortios.ServiceGroup;
import org.junit.jupiter.api.Test;

class FortiosConfigurationBuilderTest {
  @Test
  void testPolicyValid() {
    assertNull(policyValid(buildCompletePolicy(), true));
  }

  @Test
  void testPolicyValid_markedInvalid() {
    assertThat(policyValid(buildCompletePolicy(), false), equalTo("name is invalid"));
  }

  @Test
  void testPolicyValid_missingSrcIntf() {
    Policy p = buildCompletePolicy();
    p.getSrcIntf().clear();
    assertThat(policyValid(p, true), equalTo("srcintf must be set"));
  }

  @Test
  void testPolicyValid_missingDstIntf() {
    Policy p = buildCompletePolicy();
    p.getDstIntf().clear();
    assertThat(policyValid(p, true), equalTo("dstintf must be set"));
  }

  @Test
  void testPolicyValid_missingSrcAddr() {
    Policy p = buildCompletePolicy();
    p.getSrcAddrUUIDs().clear();
    assertThat(policyValid(p, true), equalTo("srcaddr must be set"));
  }

  @Test
  void testPolicyValid_missingDstAddr() {
    Policy p = buildCompletePolicy();
    p.getDstAddrUUIDs().clear();
    assertThat(policyValid(p, true), equalTo("dstaddr must be set"));
  }

  @Test
  void testPolicyValid_missingService() {
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
  void testServiceValid() {
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
  void testServiceValid_markedInvalid() {
    assertThat(serviceValid(buildCompleteService(), false), equalTo("name is invalid"));
  }

  @Test
  void testServiceValid_noDstPorts() {
    Service s = buildCompleteService(); // builds service with TCP dst ports set
    s.setTcpPortRangeDst(null);
    assertThat(serviceValid(s, true), equalTo("TCP/UDP/SCTP portrange cannot all be empty"));
  }

  @Test
  void testServiceGroupContainsDirectly() {
    ServiceGroup parent = new ServiceGroup("parent", new BatfishUUID(0));

    BatfishUUID childId1 = new BatfishUUID(1);
    parent.getMemberUUIDs().add(childId1);

    BatfishUUID childId2 = new BatfishUUID(2);
    parent.getMemberUUIDs().add(childId2);

    assertTrue(serviceGroupContains(parent, childId1, ImmutableMap.of()));
    assertTrue(serviceGroupContains(parent, childId2, ImmutableMap.of()));
  }

  @Test
  void testServiceGroupContainsIndirectly() {
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
  void testServiceGroupContainsNoMatch() {
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

  @Test
  void testAddrgrpContainsDirectly() {
    BatfishUUID parentId = new BatfishUUID(0);
    Addrgrp parent = new Addrgrp("parent", parentId);

    BatfishUUID childId1 = new BatfishUUID(1);
    parent.getMemberUUIDs().add(childId1);

    BatfishUUID childId2 = new BatfishUUID(2);
    parent.getMemberUUIDs().add(childId2);

    assertTrue(addrgrpContains(parent, childId1, ImmutableMap.of()));
    assertTrue(addrgrpContains(parent, childId2, ImmutableMap.of()));
    assertTrue(addrgrpContains(parent, parentId, ImmutableMap.of()));
  }

  @Test
  void testAddrgrpContainsIndirectly() {
    Addrgrp parent = new Addrgrp("parent", new BatfishUUID(0));

    BatfishUUID childId1 = new BatfishUUID(1);
    parent.getMemberUUIDs().add(childId1);

    BatfishUUID childId2 = new BatfishUUID(2);
    Addrgrp child2 = new Addrgrp("child2", childId2);
    parent.getMemberUUIDs().add(childId2);

    BatfishUUID grandChildId1 = new BatfishUUID(3);
    Addrgrp grandChild1 = new Addrgrp("grandChild1", grandChildId1);
    child2.getMemberUUIDs().add(grandChildId1);

    BatfishUUID greatGrandChildId1 = new BatfishUUID(4);
    grandChild1.getMemberUUIDs().add(greatGrandChildId1);

    assertTrue(
        addrgrpContains(
            parent, grandChildId1, ImmutableMap.of(childId2, child2, grandChildId1, grandChild1)));
    assertTrue(
        addrgrpContains(
            parent,
            greatGrandChildId1,
            ImmutableMap.of(childId2, child2, grandChildId1, grandChild1)));
  }

  @Test
  void testAddrgrpContainsNoMatch() {
    Addrgrp parent = new Addrgrp("name", new BatfishUUID(0));

    BatfishUUID childId1 = new BatfishUUID(1);
    parent.getMemberUUIDs().add(childId1);

    BatfishUUID childId2 = new BatfishUUID(2);
    Addrgrp child2 = new Addrgrp("child2", childId2);
    parent.getMemberUUIDs().add(childId2);

    BatfishUUID grandChildId1 = new BatfishUUID(3);
    child2.getMemberUUIDs().add(grandChildId1);

    BatfishUUID orphan = new BatfishUUID(4);

    assertFalse(addrgrpContains(parent, orphan, ImmutableMap.of(childId2, child2)));
  }

  @Test
  void testEnterCvEditMissingName() {
    FortiosCombinedParser parser = new FortiosCombinedParser("", new Settings());
    Warnings warnings = new Warnings(true, true, true);
    FortiosConfigurationBuilder builder =
        new FortiosConfigurationBuilder(
            "", parser, warnings, new FortiosConfiguration(), new SilentSyntaxCollection());

    CommonToken editToken = new CommonToken(FortiosLexer.EDIT, "edit");
    editToken.setLine(1);

    Cv_editContext nullName = new Cv_editContext(null, 0);
    nullName.addChild(editToken);
    builder.enterCv_edit(nullName);

    Cv_editContext nullStrContent = new Cv_editContext(null, 0);
    nullStrContent.addChild(editToken);
    StrContext emptyStr = new StrContext(nullStrContent, 0);
    nullStrContent.name = emptyStr;
    builder.enterCv_edit(nullStrContent);

    Cv_editContext nullChildren = new Cv_editContext(null, 0);
    nullChildren.addChild(editToken);
    StrContext strWithEmptyContent = new StrContext(nullChildren, 0);
    strWithEmptyContent.addChild(new Str_contentContext(strWithEmptyContent, 0));
    nullChildren.name = strWithEmptyContent;
    builder.enterCv_edit(nullChildren);

    assertThat(
        warnings,
        hasParseWarnings(
            contains(
                allOf(hasText("edit"), hasComment("VDOM edit block ignored: missing VDOM name")),
                allOf(hasText("edit"), hasComment("VDOM edit block ignored: missing VDOM name")),
                allOf(hasText("edit"), hasComment("VDOM edit block ignored: missing VDOM name")))));
    assertTrue(builder.skipVdomConfig(new Cve_configContext(null, 0)));
    builder.exitCv_edit(nullChildren);
    assertFalse(builder.skipVdomConfig(new Cve_configContext(null, 0)));
  }

  private static Service buildCompleteService() {
    Service s = new Service("name", new BatfishUUID(1));
    s.setTcpPortRangeDst(IntegerSpace.of(1));
    return s;
  }
}
