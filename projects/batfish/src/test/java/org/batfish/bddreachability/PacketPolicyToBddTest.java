package org.batfish.bddreachability;

import static org.batfish.bddreachability.EdgeMatchers.edge;
import static org.batfish.bddreachability.PacketPolicyToBdd.STATEMENTS_BEFORE_BREAK;
import static org.batfish.bddreachability.transition.Transitions.IDENTITY;
import static org.batfish.bddreachability.transition.Transitions.constraint;
import static org.batfish.bddreachability.transition.Transitions.or;
import static org.batfish.datamodel.acl.AclLineMatchExprs.TRUE;
import static org.batfish.datamodel.acl.AclLineMatchExprs.matchDst;
import static org.batfish.datamodel.transformation.Transformation.always;
import static org.batfish.datamodel.transformation.TransformationStep.assignDestinationIp;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Table;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.sf.javabdd.BDD;
import org.batfish.bddreachability.IpsRoutedOutInterfacesFactory.IpsRoutedOutInterfaces;
import org.batfish.bddreachability.PacketPolicyToBdd.BddPacketPolicy;
import org.batfish.bddreachability.PacketPolicyToBdd.BoolExprToBdd;
import org.batfish.bddreachability.transition.Transform;
import org.batfish.bddreachability.transition.Transition;
import org.batfish.bddreachability.transition.Transitions;
import org.batfish.common.bdd.BDDPacket;
import org.batfish.common.bdd.BDDPairingFactory;
import org.batfish.common.bdd.BDDSourceManager;
import org.batfish.common.bdd.IpAccessListToBdd;
import org.batfish.common.bdd.IpAccessListToBddImpl;
import org.batfish.common.bdd.IpSpaceToBDD;
import org.batfish.common.bdd.PrimedBDDInteger;
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
import org.batfish.datamodel.acl.AclLineMatchExprs;
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
import org.batfish.datamodel.packet_policy.Statement;
import org.batfish.datamodel.packet_policy.TrueExpr;
import org.batfish.datamodel.transformation.Transformation;
import org.batfish.datamodel.transformation.TransformationStep;
import org.batfish.symbolic.state.PacketPolicyAction;
import org.batfish.symbolic.state.PacketPolicyStatement;
import org.batfish.symbolic.state.StateExpr;
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
    Ip ip1 = Ip.parse("1.1.1.1");
    Ip ip2 = Ip.parse("2.2.2.2");
    Ip ip3 = Ip.parse("3.3.3.3");
    Transformation transformation = always().apply(assignDestinationIp(ip3)).build();
    String vrf = "vrf";
    List<Edge> edges =
        PacketPolicyToBdd.evaluate(
                _hostname,
                _ingressVrf,
                new PacketPolicy(
                    _policyName,
                    ImmutableList.of(
                        new If(
                            new PacketMatchExpr(AclLineMatchExprs.matchDst(ip1)),
                            ImmutableList.of(new ApplyTransformation(transformation))),
                        new If(
                            new PacketMatchExpr(AclLineMatchExprs.matchSrc(ip2)),
                            ImmutableList.of(new Return(new FibLookup(new LiteralVrfName(vrf)))))),
                    new Return(Drop.instance())),
                _ipAccessListToBdd,
                EMPTY_IPS_ROUTED_OUT_INTERFACES)
            .getEdges();

    BDD dstIp1Bdd = _bddPacket.getDstIp().toBDD(ip1);
    BDD srcIp2Bdd = _bddPacket.getSrcIp().toBDD(ip2);
    BDD dstIpPrime3Bdd = _bddPacket.getDstIpPrimedBDDInteger().getPrimeVar().toBDD(ip3);

    BDDPairingFactory dstPairingFactory = _bddPacket.getDstIpPrimedBDDInteger().getPairingFactory();
    assertThat(
        edges,
        containsInAnyOrder(
            // fib lookup
            edge(
                statement(0),
                fibLookupState(vrf),
                or(
                    new Transform(dstIp1Bdd.and(dstIpPrime3Bdd).and(srcIp2Bdd), dstPairingFactory),
                    constraint(dstIp1Bdd.not().and(srcIp2Bdd)))),
            // drop
            edge(
                statement(0),
                _dropState,
                or(
                    new Transform(
                        dstIp1Bdd.and(dstIpPrime3Bdd).and(srcIp2Bdd.not()), dstPairingFactory),
                    constraint(dstIp1Bdd.not().and(srcIp2Bdd.not()))))));
  }

  @Test
  public void testNoFallThrough() {
    Ip ip1 = Ip.parse("1.1.1.1");
    Ip ip2 = Ip.parse("2.2.2.2");
    Transformation transformation = always().apply(assignDestinationIp(ip2)).build();
    String vrf = "vrf";
    List<Edge> edges =
        PacketPolicyToBdd.evaluate(
                _hostname,
                _ingressVrf,
                new PacketPolicy(
                    _policyName,
                    ImmutableList.of(
                        new If(
                            new PacketMatchExpr(AclLineMatchExprs.matchDst(ip1)),
                            ImmutableList.of(
                                new ApplyTransformation(transformation),
                                new Return(new FibLookup(new LiteralVrfName(vrf)))))),
                    new Return(Drop.instance())),
                _ipAccessListToBdd,
                EMPTY_IPS_ROUTED_OUT_INTERFACES)
            .getEdges();

    BDD dstIp1Bdd = _bddPacket.getDstIp().toBDD(ip1);
    BDD dstIpPrime2Bdd = _bddPacket.getDstIpPrimedBDDInteger().getPrimeVar().toBDD(ip2);

    assertThat(
        edges,
        containsInAnyOrder(
            // fibLookup
            edge(
                statement(0),
                fibLookupState(vrf),
                new Transform(
                    dstIp1Bdd.and(dstIpPrime2Bdd),
                    _bddPacket.getDstIpPrimedBDDInteger().getPairingFactory())),
            // drop
            edge(statement(0), _dropState, constraint(dstIp1Bdd.not()))));
  }

  @Test
  public void testIfTrue_fallThrough() {
    List<Edge> edges =
        PacketPolicyToBdd.evaluate(
                _hostname,
                _ingressVrf,
                new PacketPolicy(
                    _policyName,
                    ImmutableList.of(new If(new PacketMatchExpr(TRUE), ImmutableList.of())),
                    new Return(Drop.instance())),
                _ipAccessListToBdd,
                EMPTY_IPS_ROUTED_OUT_INTERFACES)
            .getEdges();

    assertThat(
        edges,
        containsInAnyOrder(
            // fall through to default action
            edge(statement(0), _dropState, IDENTITY)));
  }

  @Test
  public void testIfTrue_noFallThrough() {
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
                        ImmutableList.of(new Return(new FibLookup(new LiteralVrfName(vrf)))))),
                new Return(Drop.instance())),
            _ipAccessListToBdd,
            EMPTY_IPS_ROUTED_OUT_INTERFACES);

    assertThat(
        converted.getEdges(),
        containsInAnyOrder(
            // if TRUE, then noop
            edge(statement(0), fibLookupState(vrf), IDENTITY)));
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

    PrimedBDDInteger srcIp = _bddPacket.getSrcIpPrimedBDDInteger();
    Transition transform =
        Transitions.transform(srcIp.getPrimeVar().toBDD(ip), srcIp.getPairingFactory());
    assertThat(edges, contains(edge(statement(0), fibLookupState("vrf"), transform)));
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
                          new FibEntry(FibForward.of(Ip.ZERO, iface1), ImmutableList.of(route1)))))
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
                          new FibEntry(FibForward.of(Ip.ZERO, iface1), ImmutableList.of(route1)))))
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
                          new FibEntry(FibForward.of(Ip.ZERO, iface1), ImmutableList.of(route1)),
                          new FibEntry(FibForward.of(Ip.ZERO, iface2), ImmutableList.of(route2)))))
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
  public void testLongPolicyBreaks() {
    Return fl = new Return(new FibLookup(new LiteralVrfName("vrf")));
    Return drop = new Return(Drop.instance());

    // Construct a long packet policy, with more statements than required to create a break.
    long tenZero = Ip.parse("10.0.0.0").asLong();
    List<Statement> statements = new ArrayList<>();
    int numStatements = STATEMENTS_BEFORE_BREAK + 5;
    for (int i = 0; i < numStatements; ++i) {
      statements.add(
          new If(new PacketMatchExpr(matchDst(Ip.create(tenZero + i))), ImmutableList.of(drop)));
    }
    PacketPolicy longPolicy = new PacketPolicy(_policyName, statements, fl);

    // Convert longPolicy to a graph, and evaluate it from the initial node.
    BddPacketPolicy asBdd =
        PacketPolicyToBdd.evaluate(
            _hostname,
            _ingressVrf,
            longPolicy,
            _ipAccessListToBdd,
            EMPTY_IPS_ROUTED_OUT_INTERFACES);
    Table<StateExpr, StateExpr, Transition> table =
        asBdd.getEdges().stream()
            .collect(
                ImmutableTable.toImmutableTable(
                    Edge::getPreState, Edge::getPostState, Edge::getTransition, Transitions::or));
    // Ensure there is a break.
    StateExpr startState = new PacketPolicyStatement(_hostname, _ingressVrf, _policyName, 0);
    StateExpr secondState = new PacketPolicyStatement(_hostname, _ingressVrf, _policyName, 1);
    StateExpr thirdState = new PacketPolicyStatement(_hostname, _ingressVrf, _policyName, 2);
    assertThat(
        table.rowMap(), allOf(hasKey(startState), hasKey(secondState), not(hasKey(thirdState))));

    // Actually traverse the packet policy.
    Map<StateExpr, BDD> reachableSet = new HashMap<>();
    reachableSet.put(startState, _bddPacket.getFactory().one());
    BDDReachabilityUtils.fixpoint(reachableSet, table, Transition::transitForward);
    StateExpr dropped =
        new PacketPolicyAction(_hostname, _ingressVrf, _policyName, drop.getAction());
    StateExpr lookedUp =
        new PacketPolicyAction(_hostname, _ingressVrf, _policyName, fl.getAction());
    assertThat(reachableSet, allOf(hasKey(dropped), hasKey(lookedUp)));

    // Ensure that the right things are dropped and looked up.
    BDD droppedBDDs = _bddPacket.getDstIp().range(tenZero, tenZero + numStatements - 1);
    assertThat(reachableSet, hasEntry(dropped, droppedBDDs));
    BDD lookedUpBDDs = droppedBDDs.not();
    assertThat(reachableSet, hasEntry(lookedUp, lookedUpBDDs));
  }

  /** Test that we don't insert breaks in long policies if the conversion itself creates breaks. */
  @Test
  public void testLongPolicyBreaks_reset() {
    Return fl = new Return(new FibLookup(new LiteralVrfName("vrf")));
    Return drop = new Return(Drop.instance());

    // Construct a long packet policy, with more statements than required to create a break.
    long tenZero = Ip.parse("10.0.0.0").asLong();
    List<Statement> statements = new ArrayList<>();
    int numStatements = STATEMENTS_BEFORE_BREAK + 5;
    int fallThroughStatement =
        STATEMENTS_BEFORE_BREAK - 1; // this will create a statement and reset the count
    assertTrue(fallThroughStatement >= 0);

    for (int i = 0; i < numStatements; ++i) {
      if (i == fallThroughStatement) {
        statements.add(
            // fall through
            new If(new PacketMatchExpr(matchDst(Ip.create(tenZero + i))), ImmutableList.of()));
      } else {
        statements.add(
            new If(new PacketMatchExpr(matchDst(Ip.create(tenZero + i))), ImmutableList.of(drop)));
      }
    }
    PacketPolicy longPolicy = new PacketPolicy(_policyName, statements, fl);

    // Convert longPolicy to a graph, and evaluate it from the initial node.
    BddPacketPolicy asBdd =
        PacketPolicyToBdd.evaluate(
            _hostname,
            _ingressVrf,
            longPolicy,
            _ipAccessListToBdd,
            EMPTY_IPS_ROUTED_OUT_INTERFACES);
    Table<StateExpr, StateExpr, Transition> table =
        asBdd.getEdges().stream()
            .collect(
                ImmutableTable.toImmutableTable(
                    Edge::getPreState, Edge::getPostState, Edge::getTransition, Transitions::or));
    // Ensure there is a break.
    StateExpr startState = new PacketPolicyStatement(_hostname, _ingressVrf, _policyName, 0);
    StateExpr secondState = new PacketPolicyStatement(_hostname, _ingressVrf, _policyName, 1);
    StateExpr thirdState = new PacketPolicyStatement(_hostname, _ingressVrf, _policyName, 2);
    assertThat(
        table.rowMap(), allOf(hasKey(startState), hasKey(secondState), not(hasKey(thirdState))));

    // Check that the break happens after the fall-through
    StateExpr dropped =
        new PacketPolicyAction(_hostname, _ingressVrf, _policyName, drop.getAction());
    StateExpr lookedUp =
        new PacketPolicyAction(_hostname, _ingressVrf, _policyName, fl.getAction());
    BDD startStateDroppedBDDs =
        _bddPacket.getDstIp().range(tenZero, tenZero + fallThroughStatement - 1);
    BDD secondStateDroppedBDDs =
        _bddPacket
            .getDstIp()
            .range(tenZero + fallThroughStatement + 1, tenZero + numStatements - 1);

    assertEquals(table.get(startState, dropped), constraint(startStateDroppedBDDs));
    assertEquals(table.get(startState, secondState), constraint(startStateDroppedBDDs.not()));
    assertEquals(table.get(secondState, dropped), constraint(secondStateDroppedBDDs));
    assertEquals(table.get(secondState, lookedUp), constraint(secondStateDroppedBDDs.not()));

    // Actually traverse the packet policy.
    BDD droppedBDDs = startStateDroppedBDDs.or(secondStateDroppedBDDs);
    BDD lookedUpBDDs = droppedBDDs.not();

    Map<StateExpr, BDD> reachableSet = new HashMap<>();
    reachableSet.put(startState, _bddPacket.getFactory().one());
    BDDReachabilityUtils.fixpoint(reachableSet, table, Transition::transitForward);
    assertThat(reachableSet, allOf(hasKey(dropped), hasKey(lookedUp)));

    // Ensure that the right things are dropped and looked up.
    assertThat(reachableSet, hasEntry(dropped, droppedBDDs));
    assertThat(reachableSet, hasEntry(lookedUp, lookedUpBDDs));
  }
}
