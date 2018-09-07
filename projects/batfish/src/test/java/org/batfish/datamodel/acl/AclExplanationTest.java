package org.batfish.datamodel.acl;

import static org.batfish.datamodel.acl.AclLineMatchExprs.and;
import static org.batfish.datamodel.acl.AclLineMatchExprs.matchDst;
import static org.batfish.datamodel.acl.AclLineMatchExprs.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.batfish.datamodel.HeaderSpace;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.SubRange;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class AclExplanationTest {
  @Rule public ExpectedException _exception = ExpectedException.none();

  @Test
  public void testSimple() {
    MatchHeaderSpace require = matchDst(Prefix.parse("1.2.3.0/24"));
    MatchHeaderSpace forbid = matchDst(new Ip("1.2.3.4"));

    AclExplanation explanation = new AclExplanation();
    explanation.requireHeaderSpace(require.getHeaderspace());
    explanation.forbidHeaderSpace(forbid.getHeaderspace());
    assertThat(explanation.build(), equalTo(and(require, not(forbid))));
  }

  @Test
  public void testMultipleForbids() {
    MatchHeaderSpace forbid1 = matchDst(new Ip("1.2.3.4"));
    MatchHeaderSpace forbid2 = matchDst(new Ip("1.2.3.5"));

    AclExplanation explanation = new AclExplanation();
    explanation.forbidHeaderSpace(forbid1.getHeaderspace());
    explanation.forbidHeaderSpace(forbid2.getHeaderspace());
    assertThat(explanation.build(), equalTo(and(not(forbid1), not(forbid2))));
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

    AclExplanation explanation = new AclExplanation();
    explanation.requireHeaderSpace(matchDstPrefix.getHeaderspace());
    explanation.requireHeaderSpace(matchDstPort.getHeaderspace());
    explanation.forbidHeaderSpace(matchDstIp.getHeaderspace());
    AclLineMatchExpr expr = and(matchDstPrefixAndPort, not(matchDstIp));
    assertThat(explanation.build(), equalTo(expr));
  }

  @Test
  public void testUnsatSource1() {
    AclExplanation explanation = new AclExplanation();
    explanation.requireOriginatingFromDevice();
    _exception.expect(IllegalStateException.class);
    _exception.expectMessage("AclExplanation is unsatisfiable");
    explanation.requireSourceInterfaces(ImmutableSet.of("foo"));
  }

  @Test
  public void testUnsatSource2() {
    AclExplanation explanation = new AclExplanation();
    explanation.requireSourceInterfaces(ImmutableSet.of("foo"));
    _exception.expect(IllegalStateException.class);
    _exception.expectMessage("AclExplanation is unsatisfiable");
    explanation.requireOriginatingFromDevice();
  }

  @Test
  public void testUnsatSource3() {
    AclExplanation explanation = new AclExplanation();
    explanation.requireSourceInterfaces(ImmutableSet.of("foo"));
    _exception.expect(IllegalStateException.class);
    _exception.expectMessage("AclExplanation is unsatisfiable");
    explanation.requireSourceInterfaces(ImmutableSet.of("bar"));
  }
}
