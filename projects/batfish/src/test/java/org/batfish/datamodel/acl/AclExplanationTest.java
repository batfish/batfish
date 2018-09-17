package org.batfish.datamodel.acl;

import static org.batfish.datamodel.acl.AclLineMatchExprs.ORIGINATING_FROM_DEVICE;
import static org.batfish.datamodel.acl.AclLineMatchExprs.and;
import static org.batfish.datamodel.acl.AclLineMatchExprs.matchDst;
import static org.batfish.datamodel.acl.AclLineMatchExprs.matchDstIp;
import static org.batfish.datamodel.acl.AclLineMatchExprs.matchSrcInterface;
import static org.batfish.datamodel.acl.AclLineMatchExprs.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.SubRange;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class AclExplanationTest {
  @Rule public ExpectedException _exception = ExpectedException.none();

  AclExplanation _explanation;

  @Before
  public void setup() {
    _explanation = new AclExplanation();
  }

  @Test
  public void testSimple() {
    MatchHeaderSpace require = matchDst(Prefix.parse("1.2.3.0/24"));
    MatchHeaderSpace forbid = matchDst(new Ip("1.2.3.4"));

    _explanation.requireHeaderSpace(require.getHeaderspace());
    _explanation.forbidHeaderSpace(forbid.getHeaderspace());
    assertThat(_explanation.build(), equalTo(and(require, not(forbid))));
  }

  @Test
  public void testIntersectOriginateFromDevice() {
    _explanation.requireOriginatingFromDevice();
    _explanation.requireOriginatingFromDevice();
    assertThat(_explanation.build(), equalTo(ORIGINATING_FROM_DEVICE));
  }

  @Test
  public void testIntersectSources() {
    _explanation.requireSourceInterfaces(ImmutableSet.of("foo", "bar", "baz"));
    _explanation.requireSourceInterfaces(ImmutableSet.of("foo"));
    assertThat(_explanation.build(), equalTo(matchSrcInterface("foo")));
  }

  @Test
  public void testMultipleForbids() {
    MatchHeaderSpace forbid1 = matchDst(new Ip("1.2.3.4"));
    MatchHeaderSpace forbid2 = matchDst(new Ip("1.2.3.5"));

    _explanation.forbidHeaderSpace(forbid1.getHeaderspace());
    _explanation.forbidHeaderSpace(forbid2.getHeaderspace());
    assertThat(_explanation.build(), equalTo(and(not(forbid1), not(forbid2))));
  }

  @Test
  public void testIntersectRequire() {
    MatchHeaderSpace matchDstIp = matchDst(new Ip("1.2.3.4"));
    MatchHeaderSpace matchDstPrefix = matchDst(Prefix.parse("1.2.3.0/24"));
    MatchHeaderSpace matchDstPort =
        new MatchHeaderSpace(
            HeaderSpace.builder().setDstPorts(ImmutableList.of(new SubRange(80, 80))).build());
    MatchHeaderSpace matchDstPrefixAndPort =
        new MatchHeaderSpace(
            matchDstPrefix
                .getHeaderspace()
                .toBuilder()
                .setDstPorts(ImmutableList.of(new SubRange(80, 80)))
                .build());

    _explanation.requireHeaderSpace(matchDstPrefix.getHeaderspace());
    _explanation.requireHeaderSpace(matchDstPort.getHeaderspace());
    _explanation.forbidHeaderSpace(matchDstIp.getHeaderspace());
    AclLineMatchExpr expr = and(matchDstPrefixAndPort, not(matchDstIp));
    assertThat(_explanation.build(), equalTo(expr));
  }

  @Test
  public void testExplainLiterals() {
    MatchHeaderSpace matchHeaderSpace = matchDstIp("1.2.3.4");
    AclLineMatchExpr notMatchHeaderSpace = not(matchHeaderSpace);
    MatchSrcInterface matchSrcInterface = matchSrcInterface("foo");
    AclLineMatchExpr expr =
        AclExplanation.explainLiterals(
            ImmutableList.of(matchHeaderSpace, notMatchHeaderSpace, matchSrcInterface));
    assertThat(expr, equalTo(and(matchHeaderSpace, matchSrcInterface, notMatchHeaderSpace)));
  }

  @Test
  public void testUnsatSource1() {
    _explanation.requireOriginatingFromDevice();
    _exception.expect(IllegalStateException.class);
    _exception.expectMessage("AclExplanation is unsatisfiable");
    _explanation.requireSourceInterfaces(ImmutableSet.of("foo"));
  }

  @Test
  public void testUnsatSource2() {
    _explanation.requireSourceInterfaces(ImmutableSet.of("foo"));
    _exception.expect(IllegalStateException.class);
    _exception.expectMessage("AclExplanation is unsatisfiable");
    _explanation.requireOriginatingFromDevice();
  }

  @Test
  public void testUnsatSource3() {
    _explanation.requireSourceInterfaces(ImmutableSet.of("foo"));
    _exception.expect(IllegalStateException.class);
    _exception.expectMessage("AclExplanation is unsatisfiable");
    _explanation.requireSourceInterfaces(ImmutableSet.of("bar"));
  }
}
