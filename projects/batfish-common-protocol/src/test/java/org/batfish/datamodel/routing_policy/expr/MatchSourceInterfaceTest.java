package org.batfish.datamodel.routing_policy.expr;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.testing.EqualsTester;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.ConnectedRoute;
import org.batfish.datamodel.InterfaceAddress;
import org.batfish.datamodel.NetworkFactory;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.routing_policy.Environment;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests of {@link MatchSourceInterface} */
@RunWith(JUnit4.class)
public class MatchSourceInterfaceTest {

  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(new MatchSourceInterface("eth0"), new MatchSourceInterface("eth0"))
        .addEqualityGroup(new MatchSourceInterface("eth1"))
        .testEquals();
  }

  @Test
  public void testRouteMatchesInterface() {
    NetworkFactory nf = new NetworkFactory();
    Configuration c1 =
        nf.configurationBuilder()
            .setHostname("c1")
            .setConfigurationFormat(ConfigurationFormat.JUNIPER)
            .build();
    Vrf vrf = nf.vrfBuilder().setOwner(c1).setName("vrf").build();
    nf.interfaceBuilder()
        .setName("lo0")
        .setOwner(c1)
        .setVrf(vrf)
        .setAddress(new InterfaceAddress("1.1.1.1/32"))
        .build();

    Environment environmentMatch =
        Environment.builder(c1)
            .setOriginalRoute(new ConnectedRoute(Prefix.parse("1.1.1.1/32"), "lo0"))
            .setVrf(vrf.getName())
            .build();
    Environment environmentNoMatch =
        Environment.builder(c1)
            .setVrf(vrf.getName())
            .setOriginalRoute(new ConnectedRoute(Prefix.parse("1.1.1.2/32"), "lo1"))
            .build();

    MatchSourceInterface expr = new MatchSourceInterface("lo0");
    MatchSourceInterface exprNoInterface = new MatchSourceInterface("nonExistent");

    assertThat(expr.evaluate(environmentMatch).getBooleanValue(), equalTo(true));
    assertThat(expr.evaluate(environmentNoMatch).getBooleanValue(), equalTo(false));
    assertThat(exprNoInterface.evaluate(environmentNoMatch).getBooleanValue(), equalTo(false));
  }
}
