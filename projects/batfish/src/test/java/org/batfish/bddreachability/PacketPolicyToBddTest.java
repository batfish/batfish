package org.batfish.bddreachability;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.aMapWithSize;
import static org.hamcrest.Matchers.anEmptyMap;
import static org.hamcrest.Matchers.hasEntry;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import net.sf.javabdd.BDD;
import org.batfish.common.bdd.BDDPacket;
import org.batfish.common.bdd.BDDSourceManager;
import org.batfish.common.bdd.IpAccessListToBdd;
import org.batfish.common.bdd.IpAccessListToBddImpl;
import org.batfish.common.bdd.IpSpaceToBDD;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.UniverseIpSpace;
import org.batfish.datamodel.acl.MatchHeaderSpace;
import org.batfish.datamodel.packet_policy.Drop;
import org.batfish.datamodel.packet_policy.FibLookup;
import org.batfish.datamodel.packet_policy.If;
import org.batfish.datamodel.packet_policy.PacketMatchExpr;
import org.batfish.datamodel.packet_policy.PacketPolicy;
import org.batfish.datamodel.packet_policy.Return;
import org.junit.Before;
import org.junit.Test;

public class PacketPolicyToBddTest {

  private BDDPacket _bddPacket;
  private IpAccessListToBdd _ipAccessListToBdd;

  @Before
  public void setUp() {
    _bddPacket = new BDDPacket();
    _ipAccessListToBdd =
        new IpAccessListToBddImpl(
            _bddPacket, BDDSourceManager.empty(_bddPacket), ImmutableMap.of(), ImmutableMap.of());
  }

  @Test
  public void testDefaultAction() {
    PacketPolicyToBdd evaluator =
        PacketPolicyToBdd.evaluate(
            new PacketPolicy("name", ImmutableList.of(), new Return(Drop.instance())),
            _bddPacket,
            _ipAccessListToBdd);
    // Everything is dropped
    assertTrue(evaluator.getToDrop().isOne());
    assertThat(evaluator.getFibLookups(), anEmptyMap());
  }

  @Test
  public void testReturn() {
    PacketPolicyToBdd evaluator =
        PacketPolicyToBdd.evaluate(
            new PacketPolicy(
                "name",
                ImmutableList.of(new Return(new FibLookup("vrf"))),
                new Return(Drop.instance())),
            _bddPacket,
            _ipAccessListToBdd);
    // Everything is looked up in "vrf"
    assertTrue(evaluator.getToDrop().isZero());
    assertThat(evaluator.getFibLookups(), aMapWithSize(1));
    assertThat(
        evaluator.getFibLookups(), hasEntry(new FibLookup("vrf"), _bddPacket.getFactory().one()));
  }

  @Test
  public void testNestedIfs() {
    String vrf1 = "vrf1";
    String vrf2 = "vrf2";
    String vrf3 = "vrf3";

    Prefix dstIps = Prefix.parse("10.0.0.0/8");
    If innerIf =
        new If(
            new PacketMatchExpr(
                new MatchHeaderSpace(HeaderSpace.builder().setDstIps(dstIps.toIpSpace()).build())),
            ImmutableList.of(new Return(new FibLookup(vrf1))));
    If outerIf =
        new If(
            new PacketMatchExpr(
                new MatchHeaderSpace(
                    HeaderSpace.builder().setDstIps(UniverseIpSpace.INSTANCE).build())),
            ImmutableList.of(innerIf, new Return(new FibLookup(vrf2))));
    PacketPolicyToBdd evaluator =
        PacketPolicyToBdd.evaluate(
            new PacketPolicy(
                "name",
                ImmutableList.of(outerIf, new Return(new FibLookup(vrf3))),
                new Return(Drop.instance())),
            _bddPacket,
            _ipAccessListToBdd);

    BDD dstIpBdd = new IpSpaceToBDD(_bddPacket.getDstIp()).toBDD(dstIps);

    // Nothing to Drop
    assertTrue(evaluator.getToDrop().isZero());
    assertThat(evaluator.getFibLookups(), aMapWithSize(3));
    // Inner if captures 10.0.0.0/8, vrf 1
    assertThat(evaluator.getFibLookups(), hasEntry(new FibLookup(vrf1), dstIpBdd));
    // Outer if captures everything else, vrf 2
    assertThat(evaluator.getFibLookups(), hasEntry(new FibLookup(vrf2), dstIpBdd.not()));
    // Last statement captures no packets, but is visited in the evaluation
    assertThat(
        evaluator.getFibLookups(), hasEntry(new FibLookup(vrf3), _bddPacket.getFactory().zero()));
  }
}
