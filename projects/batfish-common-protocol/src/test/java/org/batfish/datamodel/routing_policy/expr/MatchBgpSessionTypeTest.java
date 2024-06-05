package org.batfish.datamodel.routing_policy.expr;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableList;
import com.google.common.testing.EqualsTester;
import javax.annotation.Nullable;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.BgpSessionProperties;
import org.batfish.datamodel.BgpSessionProperties.SessionType;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.Result;
import org.batfish.datamodel.routing_policy.expr.MatchBgpSessionType.Type;
import org.junit.Test;

public class MatchBgpSessionTypeTest {
  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(new MatchBgpSessionType(Type.EBGP), new MatchBgpSessionType(Type.EBGP))
        .addEqualityGroup(new MatchBgpSessionType(Type.EBGP, Type.IBGP))
        .addEqualityGroup(new MatchBgpSessionType(Type.IBGP))
        .testEquals();
  }

  @Test
  public void testSerialization() {
    BooleanExpr ebgp = new MatchBgpSessionType(Type.EBGP);
    assertThat(SerializationUtils.clone(ebgp), equalTo(ebgp));
    assertThat(BatfishObjectMapper.clone(ebgp, BooleanExpr.class), equalTo(ebgp));
  }

  private static void runAssertion(
      boolean expected, MatchBgpSessionType match, @Nullable SessionType type) {
    NetworkFactory nf = new NetworkFactory();
    Configuration c =
        nf.configurationBuilder()
            .setHostname("h")
            .setConfigurationFormat(ConfigurationFormat.CISCO_IOS)
            .build();
    Environment.Builder env = Environment.builder(c);
    if (type != null) {
      env.setBgpSessionProperties(
          BgpSessionProperties.builder()
              .setSessionType(type)
              .setRemoteAs(1234)
              .setRemoteIp(Ip.parse("1.2.3.4"))
              .setLocalAs(2345)
              .setLocalIp(Ip.parse("2.3.4.5"))
              .build());
    }
    Result result = match.evaluate(env.build());
    assertThat(result.getBooleanValue(), equalTo(expected));
  }

  @Test
  public void testEvaluate() {
    MatchBgpSessionType ebgp = new MatchBgpSessionType(Type.EBGP);
    MatchBgpSessionType ibgp = new MatchBgpSessionType(Type.IBGP);
    MatchBgpSessionType eibgp = new MatchBgpSessionType(Type.EBGP, Type.IBGP);

    // No BGP props, no match
    runAssertion(false, ebgp, null);
    runAssertion(false, ibgp, null);
    runAssertion(false, eibgp, null);

    // EBGP session types
    for (SessionType type :
        new SessionType[] {
          SessionType.EBGP_MULTIHOP, SessionType.EBGP_SINGLEHOP, SessionType.EBGP_UNNUMBERED
        }) {
      runAssertion(true, ebgp, type);
      runAssertion(false, ibgp, type);
      runAssertion(true, eibgp, type);
    }

    // IBGP session types
    for (SessionType type : new SessionType[] {SessionType.IBGP, SessionType.IBGP_UNNUMBERED}) {
      runAssertion(false, ebgp, type);
      runAssertion(true, ibgp, type);
      runAssertion(true, eibgp, type);
    }
  }

  @Test(expected = IllegalStateException.class)
  public void testUnsetCrashes() {
    runAssertion(false, new MatchBgpSessionType(Type.EBGP), SessionType.UNSET);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testTypeRequired() {
    new MatchBgpSessionType(ImmutableList.of());
  }
}
