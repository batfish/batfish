package org.batfish.datamodel.routing_policy.statement;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import com.google.common.testing.EqualsTester;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.Bgpv4Route;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.HasReadableOriginatorIp;
import org.batfish.datamodel.HasWritableOriginatorIp;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.OspfExternalType1Route;
import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.expr.LiteralIp;
import org.batfish.datamodel.routing_policy.expr.NextHopIp;
import org.junit.Test;

/** Test of {@link SetOriginatorIp}. */
public final class SetOriginatorIpTest {

  @Test
  public void testJavaSerialization() {
    Statement obj = SetOriginatorIp.of(LiteralIp.of(Ip.ZERO));
    assertEquals(obj, SerializationUtils.clone(obj));
  }

  @Test
  public void testJacksonSerialization() {
    Statement obj = SetOriginatorIp.of(LiteralIp.of(Ip.ZERO));
    assertEquals(obj, BatfishObjectMapper.clone(obj, Statement.class));
  }

  @Test
  public void testEquals() {
    Statement obj = SetOriginatorIp.of(LiteralIp.of(Ip.ZERO));
    new EqualsTester()
        .addEqualityGroup(obj, SetOriginatorIp.of(LiteralIp.of(Ip.ZERO)))
        .addEqualityGroup(SetOriginatorIp.of(NextHopIp.instance()))
        .testEquals();
  }

  @Test
  public void testExecute() {
    Ip oldOriginatorIp = Ip.parse("1.1.1.1");
    Ip newOriginatorIp = Ip.parse("2.2.2.2");
    Statement obj = SetOriginatorIp.of(LiteralIp.of(newOriginatorIp));
    Configuration c = Configuration.builder().setHostname("h").build();
    Environment env =
        Environment.builder(c)
            .setOutputRoute(Bgpv4Route.builder().setOriginatorIp(oldOriginatorIp))
            .build();
    obj.execute(env);

    assertThat(
        ((HasReadableOriginatorIp) env.getOutputRoute()).getOriginatorIp(),
        equalTo(newOriginatorIp));
    assertNull(env.getIntermediateBgpAttributes());
  }

  @Test
  public void testExecute_writeIntermediate() {
    Ip oldOriginatorIp = Ip.parse("1.1.1.1");
    Ip newOriginatorIp = Ip.parse("2.2.2.2");
    Statement obj = SetOriginatorIp.of(LiteralIp.of(newOriginatorIp));
    Configuration c = Configuration.builder().setHostname("h").build();
    Environment env =
        Environment.builder(c)
            .setOutputRoute(Bgpv4Route.builder().setOriginatorIp(oldOriginatorIp))
            .setIntermediateBgpAttributes(Bgpv4Route.builder().setOriginatorIp(oldOriginatorIp))
            .setWriteToIntermediateBgpAttributes(true)
            .build();
    obj.execute(env);

    assertThat(
        ((HasReadableOriginatorIp) env.getOutputRoute()).getOriginatorIp(),
        equalTo(newOriginatorIp));
    assertThat(env.getIntermediateBgpAttributes().getOriginatorIp(), equalTo(newOriginatorIp));
  }

  @Test
  public void testExecute_notHasWritableOriginatorIp() {
    Ip newOriginatorIp = Ip.parse("2.2.2.2");
    Statement obj = SetOriginatorIp.of(LiteralIp.of(newOriginatorIp));
    Configuration c = Configuration.builder().setHostname("h").build();
    Environment env =
        Environment.builder(c).setOutputRoute(OspfExternalType1Route.builder()).build();

    assertThat(env.getOutputRoute(), not(instanceOf(HasReadableOriginatorIp.class)));
    assertThat(env.getOutputRoute(), not(instanceOf(HasWritableOriginatorIp.class)));
    assertNull(env.getIntermediateBgpAttributes());
    // don't crash
    obj.execute(env);
  }
}
