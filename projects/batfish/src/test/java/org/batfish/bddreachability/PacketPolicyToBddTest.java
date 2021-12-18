package org.batfish.bddreachability;

import static org.batfish.bddreachability.BDDReachabilityUtils.computePortTransformationProtocolsBdd;
import static org.batfish.bddreachability.EdgeMatchers.edge;
import static org.batfish.bddreachability.transition.Transitions.IDENTITY;
import static org.batfish.bddreachability.transition.Transitions.constraint;
import static org.batfish.bddreachability.transition.Transitions.eraseAndSet;
import static org.batfish.datamodel.acl.AclLineMatchExprs.TRUE;
import static org.batfish.datamodel.acl.AclLineMatchExprs.matchDst;
import static org.batfish.datamodel.transformation.Transformation.always;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.Collections;
import java.util.List;
import net.sf.javabdd.BDD;
import org.batfish.bddreachability.IpsRoutedOutInterfacesFactory.IpsRoutedOutInterfaces;
import org.batfish.bddreachability.PacketPolicyToBdd.BddPacketPolicy;
import org.batfish.bddreachability.PacketPolicyToBdd.BoolExprToBdd;
import org.batfish.bddreachability.transition.GuardEraseAndSet;
import org.batfish.common.bdd.BDDPacket;
import org.batfish.common.bdd.BDDSourceManager;
import org.batfish.common.bdd.IpAccessListToBdd;
import org.batfish.common.bdd.IpAccessListToBddImpl;
import org.batfish.common.bdd.IpSpaceToBDD;
import org.batfish.datamodel.AclLine;
import org.batfish.datamodel.ConnectedRoute;
import org.batfish.datamodel.ExprAclLine;
import org.batfish.datamodel.Fib;
import org.batfish.datamodel.FibEntry;
import org.batfish.datamodel.FibForward;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.MockFib;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.UniverseIpSpace;
import org.batfish.datamodel.acl.MatchHeaderSpace;
import org.batfish.datamodel.packet_policy.ApplyFilter;
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
import org.batfish.symbolic.state.PacketPolicyAction;
import org.batfish.symbolic.state.PacketPolicyStatement;
import org.junit.Before;
import org.junit.Test;

/** Tests of {@link PacketPolicyToBdd} */
public final class PacketPolicyToBddTest {
  private static final IpsRoutedOutInterfaces EMPTY_IPS_ROUTED_OUT_INTERFACES =
      new IpsRoutedOutInterfaces(MockFib.builder().build());

  private BDDPacket _bddPacket;
  private IpAccessListToBdd _ipAccessListToBdd;

  private final String _hostname = "hostname";
  private final String _ingressVrf = "ingressVrf";
  private final String _policyName = "policy";

  private PacketPolicyStatement statement(int id) {
    return new PacketPolicyStatement(_hostname, _ingressVrf, _policyName, id);
  }

  private PacketPolicyAction fibLookupState(String vrfName) {
    return new PacketPolicyAction(
        _hostname, _ingressVrf, _policyName, new FibLookup(new LiteralVrfName(vrfName)));
  }

  @Before
  public void setUp() {
    _bddPacket = new BDDPacket();
    _ipAccessListToBdd =
        new IpAccessListToBddImpl(
            _bddPacket, BDDSourceManager.empty(_bddPacket), ImmutableMap.of(), ImmutableMap.of());
  }

  private final PacketPolicyAction _dropState =
      new PacketPolicyAction(_hostname, _ingressVrf, _policyName, Drop.instance());

  @Test
  public void testDefaultAction() {
    BddPacketPolicy converted =
        PacketPolicyToBdd.evaluate(
            _hostname,
            _ingressVrf,
            new PacketPolicy(_policyName, ImmutableList.of(), new Return(Drop.instance())),
            _ipAccessListToBdd,
            EMPTY_IPS_ROUTED_OUT_INTERFACES);
    // Everything is dropped
    assertThat(converted.getEdges(), contains(edge(statement(0), _dropState, IDENTITY)));
    // Default action is tracked
    assertThat(converted.getActions(), contains(_dropState));
  }

  @Test
  public void testReturn() {
    BddPacketPolicy converted =
        PacketPolicyToBdd.evaluate(
            _hostname,
            _ingressVrf,
            new PacketPolicy(
                _policyName,
                ImmutableList.of(new Return(new FibLookup(new LiteralVrfName("vrf")))),
                new Return(Drop.instance())),
            _ipAccessListToBdd,
            EMPTY_IPS_ROUTED_OUT_INTERFACES);
    // Everything is looked up in "vrf"
    assertThat(converted.getEdges(), contains(edge(statement(0), fibLookupState("vrf"), IDENTITY)));
    // Unreachable default is not tracked.
    assertThat(converted.getActions(), contains(fibLookupState("vrf")));
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
    BddPacketPolicy converted =
        PacketPolicyToBdd.evaluate(
            _hostname,
            _ingressVrf,
            new PacketPolicy(
                _policyName,
                ImmutableList.of(outerIf, new Return(new FibLookup(new LiteralVrfName(vrf3)))),
                new Return(Drop.instance())),
            _ipAccessListToBdd,
            EMPTY_IPS_ROUTED_OUT_INTERFACES);

    BDD dstIpBdd = new IpSpaceToBDD(_bddPacket.getDstIp()).toBDD(dstIps);

    assertThat(
        converted.getEdges(),
        containsInAnyOrder(
            edge(statement(0), fibLookupState(vrf1), constraint(dstIpBdd)),
            edge(statement(0), fibLookupState(vrf2), constraint(dstIpBdd.not()))));
    assertThat(
        converted.getActions(), containsInAnyOrder(fibLookupState(vrf1), fibLookupState(vrf2)));
  }

  @Test
  public void testIfFallThrough() {
    Transformation noop = always().build();
    Ip ip1 = Ip.parse("1.1.1.1");
    Ip ip2 = Ip.parse("2.2.2.2");
    String vrf = "vrf";
    List<Edge> edges =
        PacketPolicyToBdd.evaluate(
                _hostname,
                _ingressVrf,
                new PacketPolicy(
                    _policyName,
                    ImmutableList.of(
                        new If(
                            new PacketMatchExpr(matchDst(ip1)),
                            ImmutableList.of(new ApplyTransformation(noop))),
                        new If(
                            new PacketMatchExpr(matchDst(ip2)),
                            ImmutableList.of(new Return(new FibLookup(new LiteralVrfName(vrf)))))),
                    new Return(Drop.instance())),
                _ipAccessListToBdd,
                EMPTY_IPS_ROUTED_OUT_INTERFACES)
            .getEdges();

    BDD ip2Bdd = new IpSpaceToBDD(_bddPacket.getDstIp()).toBDD(ip2);

    assertThat(
        edges,
        containsInAnyOrder(
            edge(statement(0), fibLookupState(vrf), constraint(ip2Bdd)),
            edge(statement(0), _dropState, constraint(ip2Bdd.not()))));
  }

  @Test
  public void testNoFallThrough() {
    Transformation noop = always().build();
    Ip ip1 = Ip.parse("1.1.1.1");
    String vrf = "vrf";
    List<Edge> edges =
        PacketPolicyToBdd.evaluate(
                _hostname,
                _ingressVrf,
                new PacketPolicy(
                    _policyName,
                    ImmutableList.of(
                        new If(
                            new PacketMatchExpr(matchDst(ip1)),
                            ImmutableList.of(
                                new ApplyTransformation(noop),
                                new Return(new FibLookup(new LiteralVrfName(vrf)))))),
                    new Return(Drop.instance())),
                _ipAccessListToBdd,
                EMPTY_IPS_ROUTED_OUT_INTERFACES)
            .getEdges();

    BDD ip1Bdd = new IpSpaceToBDD(_bddPacket.getDstIp()).toBDD(ip1);

    assertThat(
        edges,
        containsInAnyOrder(
            edge(statement(0), fibLookupState(vrf), constraint(ip1Bdd)),
            // else
            edge(statement(0), _dropState, constraint(ip1Bdd.not()))));
  }

  @Test
  public void testIfTrue_fallThrough() {
    Transformation noop = always().build();
    List<Edge> edges =
        PacketPolicyToBdd.evaluate(
                _hostname,
                _ingressVrf,
                new PacketPolicy(
                    _policyName,
                    ImmutableList.of(
                        new If(
                            new PacketMatchExpr(TRUE),
                            ImmutableList.of(new ApplyTransformation(noop)))),
                    new Return(Drop.instance())),
                _ipAccessListToBdd,
                EMPTY_IPS_ROUTED_OUT_INTERFACES)
            .getEdges();

    assertThat(edges, containsInAnyOrder(edge(statement(0), _dropState, IDENTITY)));
  }

  @Test
  public void testIfTrue_noFallThrough() {
    Transformation noop = always().build();
    String vrf = "vrf";
    BddPacketPolicy converted =
        PacketPolicyToBdd.evaluate(
            _hostname,
            _ingressVrf,
            new PacketPolicy(
                _policyName,
                ImmutableList.of(
                    new If(
                        new PacketMatchExpr(TRUE),
                        ImmutableList.of(
                            new ApplyTransformation(noop),
                            new Return(new FibLookup(new LiteralVrfName(vrf)))))),
                new Return(Drop.instance())),
            _ipAccessListToBdd,
            EMPTY_IPS_ROUTED_OUT_INTERFACES);

    assertThat(
        converted.getEdges(),
        containsInAnyOrder(edge(statement(0), fibLookupState(vrf), IDENTITY)));
    // Unreachable drop is not tracked.
    assertThat(converted.getActions(), contains(fibLookupState(vrf)));
  }

  @Test
  public void testApplyFilter() {
    // Set up an ACL that permits traffic to 1.1.1.0/24
    Prefix permittedPrefix = Prefix.parse("1.1.1.0/24");
    AclLine line =
        new ExprAclLine(
            LineAction.PERMIT,
            new MatchHeaderSpace(
                HeaderSpace.builder().setDstIps(permittedPrefix.toIpSpace()).build()),
            "foo");
    IpAccessList acl = IpAccessList.builder().setName("acl").setLines(line).build();
    IpAccessListToBdd ipAccessListToBdd =
        new IpAccessListToBddImpl(
            _bddPacket,
            BDDSourceManager.empty(_bddPacket),
            ImmutableMap.of(acl.getName(), acl),
            ImmutableMap.of());

    // Evaluate a PacketPolicy that uses an ApplyFilter for the above ACL
    FibLookup fl = new FibLookup(new LiteralVrfName("vrf"));
    PacketPolicy policy =
        new PacketPolicy(
            _policyName, ImmutableList.of(new ApplyFilter(acl.getName())), new Return(fl));
    List<Edge> edges =
        PacketPolicyToBdd.evaluate(
                _hostname, _ingressVrf, policy, ipAccessListToBdd, EMPTY_IPS_ROUTED_OUT_INTERFACES)
            .getEdges();

    // Traffic not destined for 1.1.1.0/24 should be dropped
    BDD permitted = _bddPacket.getDstIpSpaceToBDD().toBDD(permittedPrefix);
    assertThat(
        edges,
        containsInAnyOrder(
            edge(statement(0), _dropState, constraint(permitted.not())),
            edge(statement(0), fibLookupState("vrf"), constraint(permitted))));
  }

  @Test
  public void testApplyTransformation() {
    FibLookup fl = new FibLookup(new LiteralVrfName("vrf"));
    Ip ip = Ip.parse("8.8.8.8");
    Transformation transformation =
        always().apply(TransformationStep.assignSourceIp(ip, ip)).build();
    List<Edge> edges =
        PacketPolicyToBdd.evaluate(
                _hostname,
                _ingressVrf,
                new PacketPolicy(
                    _policyName,
                    ImmutableList.of(new ApplyTransformation(transformation), new Return(fl)),
                    new Return(Drop.instance())),
                _ipAccessListToBdd,
                EMPTY_IPS_ROUTED_OUT_INTERFACES)
            .getEdges();
    BDD ipBdd = _bddPacket.getSrcIpSpaceToBDD().toBDD(ip);
    assertThat(
        edges,
        containsInAnyOrder(
            edge(statement(0), fibLookupState("vrf"), eraseAndSet(_bddPacket.getSrcIp(), ipBdd))));
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
      List<Edge> edges =
          PacketPolicyToBdd.evaluate(
                  _hostname,
                  _ingressVrf,
                  new PacketPolicy(
                      _policyName,
                      ImmutableList.of(
                          new If(
                              Conjunction.of(TrueExpr.instance()),
                              Collections.singletonList(new Return(fl)))),
                      new Return(Drop.instance())),
                  _ipAccessListToBdd,
                  EMPTY_IPS_ROUTED_OUT_INTERFACES)
              .getEdges();
      assertThat(edges, contains(edge(statement(0), fibLookupState("vrf"), IDENTITY)));
    }

    {
      List<Edge> edges =
          PacketPolicyToBdd.evaluate(
                  _hostname,
                  _ingressVrf,
                  new PacketPolicy(
                      _policyName,
                      ImmutableList.of(
                          new If(
                              Conjunction.of(TrueExpr.instance(), FalseExpr.instance()),
                              Collections.singletonList(new Return(fl)))),
                      new Return(Drop.instance())),
                  _ipAccessListToBdd,
                  EMPTY_IPS_ROUTED_OUT_INTERFACES)
              .getEdges();
      assertThat(edges, contains(edge(statement(0), _dropState, IDENTITY)));
    }
  }

  @Test
  public void testTransformationChain1() {
    Ip ip1 = Ip.parse("1.1.1.1");
    Ip ip2 = Ip.parse("2.2.2.2");
    Ip ip3 = Ip.parse("3.3.3.3");
    Ip ip4 = Ip.parse("4.4.4.4");
    ApplyTransformation t1 =
        new ApplyTransformation(
            always()
                .apply(TransformationStep.assignDestinationIp(ip1, ip1))
                //                                    TransformationStep.assignSourcePort(1),
                //                                    TransformationStep.assignSourceIp(ip1, ip1))
                .build());
    //    ApplyTransformation t2 =
    //            new ApplyTransformation(
    //                    always()
    //                            .apply(
    //                                    TransformationStep.assignDestinationIp(ip2, ip2),
    //                                    TransformationStep.assignSourcePort(2),
    //                                    TransformationStep.assignSourceIp(ip2, ip2))
    //                            .build());
    //    ApplyTransformation t3 =
    //            new ApplyTransformation(
    //                    always()
    //                            .apply(
    //                                    TransformationStep.assignDestinationIp(ip3, ip3),
    //                                    TransformationStep.assignSourcePort(3),
    //                                    TransformationStep.assignSourceIp(ip3, ip3))
    //                            .build());
    Return ret = new Return(new FibLookup(new LiteralVrfName("vrf")));
    PacketPolicy policy =
        new PacketPolicy(
            _policyName,
            ImmutableList.of(
                //                            new If(new PacketMatchExpr(matchDst(ip1)),
                // ImmutableList.of(t2, ret)),
                //                            new If(new PacketMatchExpr(matchDst(ip2)),
                // ImmutableList.of(t3, ret)),
                new If(new PacketMatchExpr(matchDst(ip3)), ImmutableList.of(t1, ret))),
            new Return(Drop.instance()));
    PacketPolicyToBdd.BddPacketPolicy result =
        PacketPolicyToBdd.evaluate(
            _hostname, _ingressVrf, policy, _ipAccessListToBdd, EMPTY_IPS_ROUTED_OUT_INTERFACES);
    BDD ip1Bdd = _bddPacket.getDstIpSpaceToBDD().toBDD(ip1);
    BDD ip3Bdd = _bddPacket.getDstIpSpaceToBDD().toBDD(ip3);
    BDD protocolsWithPortsBdd = computePortTransformationProtocolsBdd(_bddPacket.getIpProtocol());
    BDD vars = _bddPacket.getDstIp().getVars();
    assertThat(
        result.getEdges(),
        containsInAnyOrder(
            edge(statement(0), _dropState, constraint(ip3Bdd.not())),
            edge(
                statement(0),
                fibLookupState("vrf"),
                GuardEraseAndSet.orAll(
                    ImmutableList.of(
                        //                                            new GuardEraseAndSet(vars,
                        // ip1Bdd, ip2Bdd.and(srcIp2Bdd).and(srcPort2Bdd)),
                        //                                            new GuardEraseAndSet(vars,
                        // ip2Bdd, ip3Bdd.and(srcIp3Bdd).and(srcPort3Bdd)),
                        new GuardEraseAndSet(vars, ip3Bdd, ip1Bdd))))));
  }

  @Test
  public void testTransformationChain2() {
    Ip ip1 = Ip.parse("1.1.1.1");
    Ip ip2 = Ip.parse("2.2.2.2");
    Ip ip3 = Ip.parse("3.3.3.3");
    Ip ip4 = Ip.parse("4.4.4.4");
    ApplyTransformation t1 =
        new ApplyTransformation(
            always().apply(TransformationStep.assignDestinationIp(ip1, ip1)).build());
    ApplyTransformation t2 =
        new ApplyTransformation(
            always().apply(TransformationStep.assignDestinationIp(ip2, ip2)).build());
    Return ret = new Return(new FibLookup(new LiteralVrfName("vrf")));
    PacketPolicy policy =
        new PacketPolicy(
            _policyName,
            ImmutableList.of(
                new If(new PacketMatchExpr(matchDst(ip1)), ImmutableList.of(t2, ret)),
                new If(new PacketMatchExpr(matchDst(ip3)), ImmutableList.of(t1, ret))),
            new Return(Drop.instance()));
    PacketPolicyToBdd.BddPacketPolicy result =
        PacketPolicyToBdd.evaluate(
            _hostname, _ingressVrf, policy, _ipAccessListToBdd, EMPTY_IPS_ROUTED_OUT_INTERFACES);
    BDD ip1Bdd = _bddPacket.getDstIpSpaceToBDD().toBDD(ip1);
    BDD ip2Bdd = _bddPacket.getDstIpSpaceToBDD().toBDD(ip2);
    BDD ip3Bdd = _bddPacket.getDstIpSpaceToBDD().toBDD(ip3);
    BDD vars = _bddPacket.getDstIp().getVars();
    assertThat(
        result.getEdges(),
        containsInAnyOrder(
            edge(statement(0), _dropState, constraint(ip1Bdd.or(ip3Bdd).not())),
            edge(
                statement(0),
                fibLookupState("vrf"),
                GuardEraseAndSet.orAll(
                    ImmutableList.of(
                        new GuardEraseAndSet(vars, ip1Bdd, ip2Bdd),
                        new GuardEraseAndSet(vars, ip3Bdd, ip1Bdd))))));
  }

  @Test
  public void testTransformationChain() {
    Ip ip1 = Ip.parse("1.1.1.1");
    Ip ip2 = Ip.parse("2.2.2.2");
    Ip ip3 = Ip.parse("3.3.3.3");
    Ip ip4 = Ip.parse("4.4.4.4");
    ApplyTransformation t1 =
        new ApplyTransformation(
            always()
                .apply(
                    TransformationStep.assignDestinationIp(ip1, ip1),
                    TransformationStep.assignSourcePort(1),
                    TransformationStep.assignSourceIp(ip1, ip1))
                .build());
    ApplyTransformation t2 =
        new ApplyTransformation(
            always()
                .apply(
                    TransformationStep.assignDestinationIp(ip2, ip2),
                    TransformationStep.assignSourcePort(2),
                    TransformationStep.assignSourceIp(ip2, ip2))
                .build());
    ApplyTransformation t3 =
        new ApplyTransformation(
            always()
                .apply(
                    TransformationStep.assignDestinationIp(ip3, ip3),
                    TransformationStep.assignSourcePort(3),
                    TransformationStep.assignSourceIp(ip3, ip3))
                .build());
    Return ret = new Return(new FibLookup(new LiteralVrfName("vrf")));
    PacketPolicy policy =
        new PacketPolicy(
            _policyName,
            ImmutableList.of(
                new If(new PacketMatchExpr(matchDst(ip1)), ImmutableList.of(t2, ret)),
                new If(new PacketMatchExpr(matchDst(ip2)), ImmutableList.of(t3, ret)),
                new If(new PacketMatchExpr(matchDst(ip3)), ImmutableList.of(t1, ret))),
            new Return(Drop.instance()));
    PacketPolicyToBdd.BddPacketPolicy result =
        PacketPolicyToBdd.evaluate(
            _hostname, _ingressVrf, policy, _ipAccessListToBdd, EMPTY_IPS_ROUTED_OUT_INTERFACES);
    BDD ip1Bdd = _bddPacket.getDstIpSpaceToBDD().toBDD(ip1);
    BDD ip2Bdd = _bddPacket.getDstIpSpaceToBDD().toBDD(ip2);
    BDD ip3Bdd = _bddPacket.getDstIpSpaceToBDD().toBDD(ip3);
    BDD srcIp1Bdd = _bddPacket.getSrcIpSpaceToBDD().toBDD(ip1);
    BDD srcIp2Bdd = _bddPacket.getSrcIpSpaceToBDD().toBDD(ip2);
    BDD srcIp3Bdd = _bddPacket.getSrcIpSpaceToBDD().toBDD(ip3);
    BDD srcPort1Bdd = (_bddPacket.getSrcPort().value(1));
    BDD srcPort2Bdd = (_bddPacket.getSrcPort().value(2));
    BDD srcPort3Bdd = (_bddPacket.getSrcPort().value(3));
    BDD vars =
        _bddPacket
            .getDstIp()
            .getVars()
            .and(_bddPacket.getSrcIp().getVars())
            .and(_bddPacket.getSrcPort().getVars());
    assertThat(
        result.getEdges(),
        containsInAnyOrder(
            edge(statement(0), _dropState, constraint(ip1Bdd.or(ip2Bdd).or(ip3Bdd).not())),
            edge(
                statement(0),
                fibLookupState("vrf"),
                GuardEraseAndSet.orAll(
                    ImmutableList.of(
                        new GuardEraseAndSet(vars, ip1Bdd, ip2Bdd.and(srcIp2Bdd).and(srcPort2Bdd)),
                        new GuardEraseAndSet(vars, ip2Bdd, ip3Bdd.and(srcIp3Bdd).and(srcPort3Bdd)),
                        new GuardEraseAndSet(
                            vars, ip3Bdd, ip1Bdd.and(srcIp1Bdd).and(srcPort1Bdd)))))));
  }
}
