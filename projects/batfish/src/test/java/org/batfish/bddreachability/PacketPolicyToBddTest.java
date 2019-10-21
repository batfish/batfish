package org.batfish.bddreachability;

import static org.batfish.bddreachability.TransitionMatchers.mapsForward;
import static org.batfish.bddreachability.transition.Transitions.IDENTITY;
import static org.batfish.bddreachability.transition.Transitions.ZERO;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.aMapWithSize;
import static org.hamcrest.Matchers.anEmptyMap;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.Collections;
import net.sf.javabdd.BDD;
import org.batfish.bddreachability.IpsRoutedOutInterfacesFactory.IpsRoutedOutInterfaces;
import org.batfish.bddreachability.PacketPolicyToBdd.BoolExprToBdd;
import org.batfish.bddreachability.transition.TransformationToTransition;
import org.batfish.bddreachability.transition.Transition;
import org.batfish.common.bdd.BDDPacket;
import org.batfish.common.bdd.BDDSourceManager;
import org.batfish.common.bdd.IpAccessListToBdd;
import org.batfish.common.bdd.IpAccessListToBddImpl;
import org.batfish.common.bdd.IpSpaceToBDD;
import org.batfish.datamodel.ConnectedRoute;
import org.batfish.datamodel.Fib;
import org.batfish.datamodel.FibEntry;
import org.batfish.datamodel.FibForward;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.MockFib;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.UniverseIpSpace;
import org.batfish.datamodel.acl.MatchHeaderSpace;
import org.batfish.datamodel.packet_policy.ApplyTransformation;
import org.batfish.datamodel.packet_policy.Conjunction;
import org.batfish.datamodel.packet_policy.Drop;
import org.batfish.datamodel.packet_policy.FalseExpr;
import org.batfish.datamodel.packet_policy.FibLookup;
import org.batfish.datamodel.packet_policy.FibLookupOutgoingInterfaceIsOneOf;
import org.batfish.datamodel.packet_policy.If;
import org.batfish.datamodel.packet_policy.LiteralVrfName;
import org.batfish.datamodel.packet_policy.PacketMatchExpr;
import org.batfish.datamodel.packet_policy.PacketPolicy;
import org.batfish.datamodel.packet_policy.Return;
import org.batfish.datamodel.packet_policy.TrueExpr;
import org.batfish.datamodel.transformation.Transformation;
import org.batfish.datamodel.transformation.TransformationStep;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;

/** Tests of {@link PacketPolicyToBdd} */
public final class PacketPolicyToBddTest {
  private static final IpsRoutedOutInterfaces EMPTY_IPS_ROUTED_OUT_INTERFACES =
      new IpsRoutedOutInterfaces(MockFib.builder().build());

  private BDDPacket _bddPacket;
  private IpAccessListToBdd _ipAccessListToBdd;
  private BDD _one;
  private BDD _zero;

  private Matcher<Transition> mapsOne(BDD expected) {
    return mapsForward(_one, expected);
  }

  @Before
  public void setUp() {
    _bddPacket = new BDDPacket();
    _one = _bddPacket.getFactory().one();
    _zero = _bddPacket.getFactory().zero();
    _ipAccessListToBdd =
        new IpAccessListToBddImpl(
            _bddPacket, BDDSourceManager.empty(_bddPacket), ImmutableMap.of(), ImmutableMap.of());
  }

  @Test
  public void testDefaultAction() {
    PacketPolicyToBdd evaluator =
        PacketPolicyToBdd.evaluate(
            new PacketPolicy("name", ImmutableList.of(), new Return(Drop.instance())),
            _ipAccessListToBdd,
            EMPTY_IPS_ROUTED_OUT_INTERFACES);
    // Everything is dropped
    assertEquals(evaluator.getToDrop(), IDENTITY);
    assertThat(evaluator.getFibLookups(), anEmptyMap());
  }

  @Test
  public void testReturn() {
    PacketPolicyToBdd evaluator =
        PacketPolicyToBdd.evaluate(
            new PacketPolicy(
                "name",
                ImmutableList.of(new Return(new FibLookup(new LiteralVrfName("vrf")))),
                new Return(Drop.instance())),
            _ipAccessListToBdd,
            EMPTY_IPS_ROUTED_OUT_INTERFACES);
    // Everything is looked up in "vrf"
    assertEquals(ZERO, evaluator.getToDrop());
    assertThat(evaluator.getFibLookups(), aMapWithSize(1));
    assertThat(
        evaluator.getFibLookups(), hasEntry(new FibLookup(new LiteralVrfName("vrf")), IDENTITY));
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
            ImmutableList.of(new Return(new FibLookup(new LiteralVrfName(vrf1)))));
    If outerIf =
        new If(
            new PacketMatchExpr(
                new MatchHeaderSpace(
                    HeaderSpace.builder().setDstIps(UniverseIpSpace.INSTANCE).build())),
            ImmutableList.of(innerIf, new Return(new FibLookup(new LiteralVrfName(vrf2)))));
    PacketPolicyToBdd evaluator =
        PacketPolicyToBdd.evaluate(
            new PacketPolicy(
                "name",
                ImmutableList.of(outerIf, new Return(new FibLookup(new LiteralVrfName(vrf3)))),
                new Return(Drop.instance())),
            _ipAccessListToBdd,
            EMPTY_IPS_ROUTED_OUT_INTERFACES);

    BDD dstIpBdd = new IpSpaceToBDD(_bddPacket.getDstIp()).toBDD(dstIps);

    // Nothing to Drop
    assertEquals(ZERO, evaluator.getToDrop());
    assertThat(evaluator.getFibLookups(), aMapWithSize(3));
    // Inner if captures 10.0.0.0/8, vrf 1
    assertThat(
        evaluator.getFibLookups(),
        hasEntry(equalTo(new FibLookup(new LiteralVrfName(vrf1))), mapsOne(dstIpBdd)));
    // Outer if captures everything else, vrf 2
    assertThat(
        evaluator.getFibLookups(),
        hasEntry(equalTo(new FibLookup(new LiteralVrfName(vrf2))), mapsOne(dstIpBdd.not())));
    // Last statement captures no packets, but is visited in the evaluation
    assertThat(
        evaluator.getFibLookups(),
        hasEntry(equalTo(new FibLookup(new LiteralVrfName(vrf3))), mapsOne(_zero)));
  }

  @Test
  public void testApplyTransformation() {
    FibLookup fl = new FibLookup(new LiteralVrfName("vrf"));
    Ip ip = Ip.parse("8.8.8.8.");
    Transformation transformation =
        Transformation.always().apply(TransformationStep.assignSourceIp(ip, ip)).build();
    PacketPolicyToBdd evaluator =
        PacketPolicyToBdd.evaluate(
            new PacketPolicy(
                "name",
                ImmutableList.of(new ApplyTransformation(transformation), new Return(fl)),
                new Return(Drop.instance())),
            _ipAccessListToBdd,
            EMPTY_IPS_ROUTED_OUT_INTERFACES);
    assertThat(
        evaluator.getFibLookups().get(fl),
        equalTo(
            new TransformationToTransition(_ipAccessListToBdd.getBDDPacket(), _ipAccessListToBdd)
                .toTransition(transformation)));
  }

  @Test
  public void testFibLookupOutgoingInterfaceIsOneOf() {
    String iface1 = "iface1";
    String iface2 = "iface2";
    FibLookupOutgoingInterfaceIsOneOf expr =
        new FibLookupOutgoingInterfaceIsOneOf(
            new LiteralVrfName("vrf"), ImmutableList.of(iface1, iface2));

    Prefix prefix1 = Prefix.parse("1.2.3.0/24");
    Prefix prefix2 = Prefix.parse("2.2.3.0/24");
    ConnectedRoute route1 = new ConnectedRoute(prefix1, iface1);
    ConnectedRoute route2 = new ConnectedRoute(prefix2, iface2);
    BDD prefix1Bdd = _bddPacket.getDstIpSpaceToBDD().toBDD(prefix1);
    BDD prefix2Bdd = _bddPacket.getDstIpSpaceToBDD().toBDD(prefix2);

    // empty fib
    {
      Fib fib = MockFib.builder().build();
      IpsRoutedOutInterfaces ipsRoutedOutInterfaces = new IpsRoutedOutInterfaces(fib);
      BoolExprToBdd toBdd = new BoolExprToBdd(_ipAccessListToBdd, ipsRoutedOutInterfaces);
      assertTrue(toBdd.visit(expr).isZero());
    }

    // single fib entry with missing matching Ips
    {
      Fib fib =
          MockFib.builder()
              .setFibEntries(
                  ImmutableMap.of(
                      Ip.ZERO,
                      ImmutableSet.of(
                          new FibEntry(new FibForward(Ip.ZERO, iface1), ImmutableList.of(route1)))))
              .build();
      IpsRoutedOutInterfaces ipsRoutedOutInterfaces = new IpsRoutedOutInterfaces(fib);
      BoolExprToBdd toBdd = new BoolExprToBdd(_ipAccessListToBdd, ipsRoutedOutInterfaces);
      assertTrue(toBdd.visit(expr).isZero());
    }

    // single fib entry with matching Ips
    {
      Fib fib =
          MockFib.builder()
              .setFibEntries(
                  ImmutableMap.of(
                      Ip.ZERO,
                      ImmutableSet.of(
                          new FibEntry(new FibForward(Ip.ZERO, iface1), ImmutableList.of(route1)))))
              .setMatchingIps(ImmutableMap.of(prefix1, prefix1.toIpSpace()))
              .build();
      IpsRoutedOutInterfaces ipsRoutedOutInterfaces = new IpsRoutedOutInterfaces(fib);
      BoolExprToBdd toBdd = new BoolExprToBdd(_ipAccessListToBdd, ipsRoutedOutInterfaces);
      assertEquals(prefix1Bdd, toBdd.visit(expr));
    }

    // two fib entries
    {
      Fib fib =
          MockFib.builder()
              .setFibEntries(
                  ImmutableMap.of(
                      Ip.ZERO,
                      ImmutableSet.of(
                          new FibEntry(new FibForward(Ip.ZERO, iface1), ImmutableList.of(route1)),
                          new FibEntry(new FibForward(Ip.ZERO, iface2), ImmutableList.of(route2)))))
              .setMatchingIps(
                  ImmutableMap.of(prefix1, prefix1.toIpSpace(), prefix2, prefix2.toIpSpace()))
              .build();
      IpsRoutedOutInterfaces ipsRoutedOutInterfaces = new IpsRoutedOutInterfaces(fib);
      BoolExprToBdd toBdd = new BoolExprToBdd(_ipAccessListToBdd, ipsRoutedOutInterfaces);
      assertEquals(prefix1Bdd.or(prefix2Bdd), toBdd.visit(expr));
    }
  }

  @Test
  public void testConjunction() {
    FibLookup fl = new FibLookup(new LiteralVrfName("vrf"));
    {
      PacketPolicyToBdd evaluator =
          PacketPolicyToBdd.evaluate(
              new PacketPolicy(
                  "name",
                  ImmutableList.of(
                      new If(
                          Conjunction.of(TrueExpr.instance()),
                          Collections.singletonList(new Return(fl)))),
                  new Return(Drop.instance())),
              _ipAccessListToBdd,
              EMPTY_IPS_ROUTED_OUT_INTERFACES);
      assertThat(evaluator.getFibLookups().get(fl), mapsOne(_one));
    }

    {
      PacketPolicyToBdd evaluator =
          PacketPolicyToBdd.evaluate(
              new PacketPolicy(
                  "name",
                  ImmutableList.of(
                      new If(
                          Conjunction.of(TrueExpr.instance(), FalseExpr.instance()),
                          Collections.singletonList(new Return(fl)))),
                  new Return(Drop.instance())),
              _ipAccessListToBdd,
              EMPTY_IPS_ROUTED_OUT_INTERFACES);
      assertThat(evaluator.getToDrop(), mapsOne(_one));
    }
  }
}
