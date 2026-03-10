package org.batfish.representation.frr;

import static org.batfish.representation.frr.RouteMapConvertor.toTraceableStatement;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.io.IOException;
import org.batfish.common.Warnings;
import org.batfish.datamodel.AsPath;
import org.batfish.datamodel.Bgpv4Route;
import org.batfish.datamodel.Bgpv4Route.Builder;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.OriginMechanism;
import org.batfish.datamodel.OriginType;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.ReceivedFromSelf;
import org.batfish.datamodel.RoutingProtocol;
import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.Result;
import org.batfish.datamodel.routing_policy.RoutingPolicy;
import org.batfish.datamodel.routing_policy.expr.CallExpr;
import org.batfish.datamodel.routing_policy.expr.Conjunction;
import org.batfish.datamodel.routing_policy.expr.IntComparator;
import org.batfish.datamodel.routing_policy.expr.LiteralLong;
import org.batfish.datamodel.routing_policy.expr.MatchTag;
import org.batfish.datamodel.routing_policy.statement.If;
import org.batfish.datamodel.routing_policy.statement.SetLocalPreference;
import org.batfish.datamodel.routing_policy.statement.SetMetric;
import org.batfish.datamodel.routing_policy.statement.Statement;
import org.batfish.datamodel.routing_policy.statement.Statements;
import org.junit.Before;
import org.junit.Test;

/** Tests for {@link RouteMapConvertor} */
public class RouteMapConvertorTest {

  //  private static final String FILENAME = "";
  private static final String HOSTNAME = "host";
  private static final String FILENAME = "frr.conf";
  private static FrrConfiguration VC;
  private static Warnings W;
  private static Configuration C;
  private static RouteMap ROUTEMAP;
  private static RouteMapEntry ENTRY1;
  private static RouteMapEntry ENTRY2;
  private static RouteMapEntry ENTRY3;
  private Builder _originalRouteBuilder;

  private void initRouteMap() {
    /*
      if match entry1:
         goto match 3
      else match entry2, entry 3 in order
    */
    ENTRY1 = new RouteMapEntry(10, LineAction.PERMIT);
    ENTRY1.setMatchTag(new RouteMapMatchTag(1));
    ENTRY1.setSetMetric(new RouteMapSetMetric(new LiteralLong(10)));
    ENTRY1.setContinue(new RouteMapContinue(30));

    ENTRY2 = new RouteMapEntry(20, LineAction.DENY);
    ENTRY2.setMatchTag(new RouteMapMatchTag(2));
    ENTRY2.setSetMetric(new RouteMapSetMetric(new LiteralLong(20)));

    ENTRY3 = new RouteMapEntry(30, LineAction.PERMIT);
    ENTRY3.setMatchTag(new RouteMapMatchTag(1));
    ENTRY3.setSetMetric(new RouteMapSetMetric(new LiteralLong(30)));

    ROUTEMAP = new RouteMap("routeMap");
    ROUTEMAP
        .getEntries()
        .putAll(
            ImmutableMap.of(
                10, ENTRY1,
                20, ENTRY2,
                30, ENTRY3));
  }

  @Before
  public void setup() {
    W = new Warnings();
    VC = new FrrConfiguration();
    C = new Configuration(HOSTNAME, ConfigurationFormat.CUMULUS_CONCATENATED);
    initRouteMap();
    _originalRouteBuilder =
        Bgpv4Route.testBuilder()
            .setAsPath(AsPath.ofSingletonAsSets(2L))
            .setOriginatorIp(Ip.ZERO)
            .setOriginType(OriginType.INCOMPLETE)
            .setProtocol(RoutingProtocol.BGP)
            .setNextHopIp(Ip.parse("1.1.1.1"))
            .setNetwork(Prefix.parse("10.20.30.0/31"));
  }

  @Test
  public void testToRouteMap_MatchContinueNext() {
    // match entry1 then continue to next (i.e., entry2)
    ENTRY1.setContinue(new RouteMapContinue(null));
    ENTRY2.setMatchTag(new RouteMapMatchTag(1));
    RouteMapConvertor convertor = new RouteMapConvertor(C, VC, ROUTEMAP, FILENAME, W);
    RoutingPolicy policy = convertor.toRouteMap();

    Builder outputBuilder = Bgpv4Route.testBuilder();
    _originalRouteBuilder.setTag(1L);
    Environment.Builder env = Environment.builder(C);
    Result result =
        policy.call(
            env.setOriginalRoute(_originalRouteBuilder.build())
                .setOutputRoute(outputBuilder)
                .build());
    // not match entry2, so metric is updated to 20
    assertFalse(result.getBooleanValue());
    assertThat(outputBuilder.getMetric(), equalTo(20L));
  }

  @Test
  public void testToRouteMap_MatchContinue() {
    // match entry1 then continue to entry3
    RouteMapConvertor convertor = new RouteMapConvertor(C, VC, ROUTEMAP, FILENAME, W);
    RoutingPolicy policy = convertor.toRouteMap();

    Builder outputBuilder = Bgpv4Route.testBuilder();
    Environment.Builder env = Environment.builder(C);
    Result result =
        policy.call(
            env.setOriginalRoute(_originalRouteBuilder.setTag(1L).build())
                .setOutputRoute(outputBuilder)
                .build());
    // match entry3, so metric is updated to 30
    assertTrue(result.getBooleanValue());
    assertThat(outputBuilder.getMetric(), equalTo(30L));
  }

  @Test
  public void testToRouteMap_NotMatchContinue() {
    // match entry2
    RouteMapConvertor convertor = new RouteMapConvertor(C, VC, ROUTEMAP, FILENAME, W);
    RoutingPolicy policy = convertor.toRouteMap();

    Builder outputBuilder = Bgpv4Route.testBuilder();
    Environment.Builder env = Environment.builder(C);
    Result result =
        policy.call(
            env.setOriginalRoute(_originalRouteBuilder.setTag(2L).build())
                .setOutputRoute(outputBuilder)
                .build());
    assertFalse(result.getBooleanValue());
    assertThat(outputBuilder.getMetric(), equalTo(20L));
  }

  @Test
  public void testToStatement_ContinueEntry() {
    RouteMapConvertor convertor = new RouteMapConvertor(C, VC, ROUTEMAP, FILENAME, W);
    Statement statement = convertor.toStatement(ENTRY1);

    Conjunction match =
        new Conjunction(ImmutableList.of(new MatchTag(IntComparator.EQ, new LiteralLong(1))));

    assertThat(
        statement,
        equalTo(
            new If(
                match,
                // true branch
                ImmutableList.of(
                    toTraceableStatement(
                        ImmutableList.of(
                            new SetMetric(new LiteralLong(10)),
                            Statements.SetLocalDefaultActionAccept.toStaticStatement(),
                            new If(
                                new CallExpr(String.format("~%s~SEQ:%d~", "routeMap", 30)),
                                ImmutableList.of(Statements.ReturnTrue.toStaticStatement()),
                                ImmutableList.of(Statements.ReturnFalse.toStaticStatement()))),
                        ENTRY1.getNumber(),
                        ROUTEMAP.getName(),
                        FILENAME)),
                // false branch
                ImmutableList.of())));
  }

  @Test
  public void testToStatement_FalseToNewPolicyEntry() {
    RouteMapConvertor convertor = new RouteMapConvertor(C, VC, ROUTEMAP, FILENAME, W);
    Statement statement = convertor.toStatement(ENTRY2);

    Conjunction match =
        new Conjunction(ImmutableList.of(new MatchTag(IntComparator.EQ, new LiteralLong(2))));

    assertThat(
        statement,
        equalTo(
            new If(
                match,
                // true branch
                ImmutableList.of(
                    toTraceableStatement(
                        ImmutableList.of(
                            new SetMetric(new LiteralLong(20)),
                            Statements.ReturnFalse.toStaticStatement()),
                        ENTRY2.getNumber(),
                        ROUTEMAP.getName(),
                        FILENAME)),

                // false branch
                ImmutableList.of(
                    new If(
                        new CallExpr(String.format("~%s~SEQ:%d~", "routeMap", 30)),
                        ImmutableList.of(Statements.ReturnTrue.toStaticStatement()),
                        ImmutableList.of(Statements.ReturnFalse.toStaticStatement()))))));
  }

  @Test
  public void testToStatement_TargetEntry() {
    RouteMapConvertor convertor = new RouteMapConvertor(C, VC, ROUTEMAP, FILENAME, W);
    Statement statement = convertor.toStatement(ENTRY3);

    Conjunction match =
        new Conjunction(ImmutableList.of(new MatchTag(IntComparator.EQ, new LiteralLong(1))));

    assertThat(
        statement,
        equalTo(
            new If(
                match,
                // true branch
                ImmutableList.of(
                    toTraceableStatement(
                        ImmutableList.of(
                            new SetMetric(new LiteralLong(30)),
                            Statements.ReturnTrue.toStaticStatement()),
                        ENTRY3.getNumber(),
                        ROUTEMAP.getName(),
                        FILENAME)),
                // false branch
                ImmutableList.of())));
  }

  @Test
  public void testToRouteMapSetLocalPref() {
    RouteMap rm = new RouteMap("RM");
    RouteMapEntry rme = new RouteMapEntry(10, LineAction.PERMIT);
    rme.setSetLocalPreference(new RouteMapSetLocalPreference(200L));
    rm.getEntries().put(10, rme);
    RoutingPolicy policy = new RouteMapConvertor(C, VC, rm, FILENAME, W).toRouteMap();

    Builder outputBuilder = Bgpv4Route.testBuilder();
    Environment.Builder env = Environment.builder(C);
    policy.call(
        env.setOriginalRoute(_originalRouteBuilder.build()).setOutputRoute(outputBuilder).build());
    assertThat(outputBuilder.getLocalPreference(), equalTo(200L));
  }

  @Test
  public void testToRouteMapSetTag() {
    RouteMap rm = new RouteMap("RM");
    RouteMapEntry rme = new RouteMapEntry(10, LineAction.PERMIT);
    rme.setSetTag(new RouteMapSetTag(999));
    rm.getEntries().put(10, rme);
    RoutingPolicy policy = new RouteMapConvertor(C, VC, rm, FILENAME, W).toRouteMap();

    Builder outputBuilder = Bgpv4Route.testBuilder();
    Environment.Builder env = Environment.builder(C);
    policy.call(
        env.setOriginalRoute(_originalRouteBuilder.build()).setOutputRoute(outputBuilder).build());
    assertThat(outputBuilder.getTag(), equalTo(999L));
  }

  // Call route map that permits -- accept route, both maps modify route.
  @Test
  public void testToRouteMapCall_Permit() {
    String subMapName = "SUB-MAP";

    // Real value of VS submap does not matter; Actual policy semantics placed in VI
    VC.getRouteMaps().put(subMapName, new RouteMap(subMapName));
    RouteMap rm = new RouteMap("RM");
    RouteMapEntry rme = new RouteMapEntry(10, LineAction.PERMIT);
    rme.setSetTag(new RouteMapSetTag(7777L));
    rme.setCall(new RouteMapCall(subMapName));
    rm.getEntries().put(10, rme);
    RoutingPolicy policy = new RouteMapConvertor(C, VC, rm, FILENAME, W).toRouteMap();

    Builder outputBuilder = Bgpv4Route.testBuilder();
    C.getRoutingPolicies()
        .put(
            subMapName,
            RoutingPolicy.builder()
                .setOwner(C)
                .setName(subMapName)
                .setStatements(
                    ImmutableList.of(
                        new SetLocalPreference(new LiteralLong(2000)),
                        Statements.ExitAccept.toStaticStatement()))
                .build());
    Environment.Builder env = Environment.builder(C);
    Result result =
        policy.call(
            env.setOriginalRoute(_originalRouteBuilder.build())
                .setOutputRoute(outputBuilder)
                .build());
    assertTrue(result.getBooleanValue());
    assertThat(outputBuilder.getTag(), equalTo(7777L));
    assertThat(outputBuilder.getLocalPreference(), equalTo(2000L));
  }

  // Call route map that denies -- route denied.
  @Test
  public void testToRouteMapCall_Reject() {
    String subMapName = "SUB-MAP";
    // Real value of VS submap does not matter; Actual policy semantics placed in VI
    VC.getRouteMaps().put(subMapName, new RouteMap(subMapName));
    RouteMap rm = new RouteMap("RM");
    RouteMapEntry rme = new RouteMapEntry(10, LineAction.PERMIT);
    rme.setCall(new RouteMapCall(subMapName));
    rm.getEntries().put(10, rme);
    RoutingPolicy policy = new RouteMapConvertor(C, VC, rm, FILENAME, W).toRouteMap();

    C.getRoutingPolicies()
        .put(
            subMapName,
            RoutingPolicy.builder()
                .setOwner(C)
                .setName(subMapName)
                .setStatements(ImmutableList.of(Statements.ExitReject.toStaticStatement()))
                .build());

    Builder outputBuilder = Bgpv4Route.testBuilder();
    Environment.Builder env = Environment.builder(C);
    Result result =
        policy.call(
            env.setOriginalRoute(_originalRouteBuilder.build())
                .setOutputRoute(outputBuilder)
                .build());
    assertFalse(result.getBooleanValue());
  }

  // Call route map that does not exist -- route permitted (?)
  @Test
  public void testToRouteMapCall_NoSubMap() {
    String subMapName = "SUB-MAP";
    RouteMap rm = new RouteMap("RM");
    RouteMapEntry rme = new RouteMapEntry(10, LineAction.PERMIT);
    rme.setCall(new RouteMapCall(subMapName));
    rm.getEntries().put(10, rme);
    RoutingPolicy policy = new RouteMapConvertor(C, VC, rm, FILENAME, W).toRouteMap();

    Builder outputBuilder = Bgpv4Route.testBuilder();
    Environment.Builder env = Environment.builder(C);
    Result result =
        policy.call(
            env.setOriginalRoute(_originalRouteBuilder.build())
                .setOutputRoute(outputBuilder)
                .build());
    assertTrue(result.getBooleanValue());
  }

  @Test
  public void testRouteMapExhaustive() throws IOException {
    ENTRY1 = new RouteMapEntry(10, LineAction.DENY);
    ENTRY1.setMatchTag(new RouteMapMatchTag(1L));
    ENTRY1.setSetTag(new RouteMapSetTag(10));
    ENTRY1.setContinue(new RouteMapContinue(20));

    ENTRY2 = new RouteMapEntry(20, LineAction.PERMIT);
    ENTRY2.setMatchTag(new RouteMapMatchTag(2L));
    ENTRY2.setSetTag(new RouteMapSetTag(20));
    ENTRY2.setContinue(new RouteMapContinue(30));

    ENTRY3 = new RouteMapEntry(30, LineAction.PERMIT);
    ENTRY3.setSetLocalPreference(new RouteMapSetLocalPreference(30));

    ROUTEMAP = new RouteMap("routeMap");
    ROUTEMAP
        .getEntries()
        .putAll(
            ImmutableMap.of(
                10, ENTRY1,
                20, ENTRY2,
                30, ENTRY3));

    RouteMapConvertor convertor = new RouteMapConvertor(C, VC, ROUTEMAP, FILENAME, W);
    RoutingPolicy policy = convertor.toRouteMap();

    Bgpv4Route base =
        Bgpv4Route.builder()
            .setTag(0L)
            .setSrcProtocol(RoutingProtocol.BGP)
            .setMetric(0L) // 30 match metric 3
            .setAsPath(AsPath.ofSingletonAsSets(0L))
            .setOriginatorIp(Ip.ZERO)
            .setOriginType(OriginType.EGP)
            .setOriginMechanism(OriginMechanism.REDISTRIBUTE)
            .setProtocol(RoutingProtocol.BGP)
            .setNextHopIp(Ip.parse("192.0.2.254"))
            .setNetwork(Prefix.ZERO)
            .setReceivedFrom(ReceivedFromSelf.instance())
            .build();
    Environment.Builder env = Environment.builder(C);
    Builder outputBuilder = Bgpv4Route.testBuilder();
    // There are 3 paths through the route-map:
    // 10 deny tag 1, continue                OR    fall-through
    // 20 permit tag 2, continue      OR    30 terminate
    {
      // false false -> 30 only
      policy.call(env.setOriginalRoute(base).setOutputRoute(outputBuilder).build());
      assertThat(outputBuilder.getTag(), not(equalTo(10L)));
      assertThat(outputBuilder.getTag(), not(equalTo(20L)));
      assertThat(outputBuilder.getLocalPreference(), equalTo(30L));
    }
    {
      // false true -> 20, 30
      policy.call(
          env.setOriginalRoute(base.toBuilder().setTag(2L).build())
              .setOutputRoute(outputBuilder)
              .build());
      assertThat(outputBuilder.getTag(), equalTo(20L));
      assertThat(outputBuilder.getLocalPreference(), equalTo(30L));
    }
    {
      // true false -> 10 and 30 - check behavior on actual device.

      policy.call(
          env.setOriginalRoute(base.toBuilder().setTag(1L).build())
              .setOutputRoute(outputBuilder)
              .build());
      assertThat(outputBuilder.getTag(), equalTo(10L));
      assertThat(outputBuilder.getLocalPreference(), equalTo(30L));
    }
  }
}
