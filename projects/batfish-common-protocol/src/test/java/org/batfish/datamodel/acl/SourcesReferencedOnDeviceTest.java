package org.batfish.datamodel.acl;

import static org.batfish.datamodel.ConfigurationFormat.CISCO_IOS;
import static org.batfish.datamodel.acl.AclLineMatchExprs.matchSrcInterface;
import static org.batfish.datamodel.acl.SourcesReferencedOnDevice.activeReferencedSources;
import static org.batfish.datamodel.acl.SourcesReferencedOnDevice.allReferencedSources;
import static org.batfish.datamodel.acl.SourcesReferencedOnDevice.collectPacketPolicyReferences;
import static org.batfish.datamodel.transformation.Transformation.when;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Set;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ExprAclLine;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.InterfaceType;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.packet_policy.ApplyTransformation;
import org.batfish.datamodel.packet_policy.Conjunction;
import org.batfish.datamodel.packet_policy.Drop;
import org.batfish.datamodel.packet_policy.If;
import org.batfish.datamodel.packet_policy.PacketMatchExpr;
import org.batfish.datamodel.packet_policy.PacketPolicy;
import org.batfish.datamodel.packet_policy.Return;
import org.batfish.datamodel.packet_policy.Statement;
import org.junit.Test;

public class SourcesReferencedOnDeviceTest {
  @Test
  public void testActiveReferences() {
    Configuration c =
        Configuration.builder().setHostname("test").setConfigurationFormat(CISCO_IOS).build();
    IpAccessList.builder()
        .setName("acl-has-ref")
        .setOwner(c)
        .setLines(ExprAclLine.accepting(matchSrcInterface("in-acl")))
        .build();
    Interface.builder()
        .setOwner(c)
        .setVrf(c.getDefaultVrf())
        .setName("in-incoming-trans") // so one is active
        .setType(InterfaceType.LOGICAL)
        .setIncomingTransformation(when(matchSrcInterface("in-incoming-trans")).build())
        .setOutgoingTransformation(when(matchSrcInterface("in-outgoing-trans")).build())
        .build();
    PacketPolicy p =
        new PacketPolicy(
            "policy-has-ref",
            ImmutableList.of(
                new If(
                    new PacketMatchExpr(matchSrcInterface("in-packet-policy")),
                    ImmutableList.of())),
            new Return(Drop.instance()));
    c.setPacketPolicies(ImmutableMap.of(p.getName(), p));
    assertThat(
        allReferencedSources(c),
        containsInAnyOrder("in-acl", "in-incoming-trans", "in-outgoing-trans", "in-packet-policy"));
    assertThat(activeReferencedSources(c), containsInAnyOrder("in-incoming-trans"));
  }

  @Test
  public void testCollectPacketPolicyReferences() {
    Statement nestedIf =
        new If(new PacketMatchExpr(matchSrcInterface("nested-if")), ImmutableList.of());
    Statement ifSt =
        new If(new PacketMatchExpr(matchSrcInterface("top-level-if")), ImmutableList.of(nestedIf));
    Statement conjunctionIf =
        new If(
            Conjunction.of(new PacketMatchExpr(matchSrcInterface("conjunction"))),
            ImmutableList.of());
    Statement transformation =
        new ApplyTransformation(
            when(matchSrcInterface("transformation"))
                .setAndThen(when(matchSrcInterface("transformation-andthen")).build())
                .setOrElse(when(matchSrcInterface("transformation-orelse")).build())
                .build());
    PacketPolicy p =
        new PacketPolicy(
            "foo",
            ImmutableList.of(ifSt, conjunctionIf, transformation),
            new Return(Drop.instance()));
    Set<String> referenced = new HashSet<>();
    collectPacketPolicyReferences(
        p, ImmutableMap.of(), Collections.newSetFromMap(new IdentityHashMap<>()), referenced);
    assertThat(
        referenced,
        containsInAnyOrder(
            "top-level-if",
            "conjunction",
            "nested-if",
            "transformation",
            "transformation-andthen",
            "transformation-orelse"));
  }
}
