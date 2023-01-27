package org.batfish.datamodel.routing_policy.expr;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableSet;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.ConnectedRoute;
import org.batfish.datamodel.EigrpExternalRoute;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.eigrp.EigrpMetricValues;
import org.batfish.datamodel.eigrp.EigrpMetricVersion;
import org.batfish.datamodel.eigrp.WideMetric;
import org.batfish.datamodel.route.nh.NextHopInterface;
import org.batfish.datamodel.routing_policy.Environment;
import org.junit.Test;

/** Tests of {@link MatchProcessAsn} */
public class MatchProcessAsnTest {
  @Test
  public void testSerialization() {
    MatchProcessAsn m = new MatchProcessAsn(ImmutableSet.of(1L, 2L));
    assertThat(BatfishObjectMapper.clone(m, BooleanExpr.class), equalTo(m));
    assertThat(SerializationUtils.clone(m), equalTo(m));
  }

  @Test
  public void testEvaluate() {
    MatchProcessAsn mpa = new MatchProcessAsn(ImmutableSet.of(10L, 20L));
    Configuration c =
        Configuration.builder()
            .setHostname("h")
            .setConfigurationFormat(ConfigurationFormat.CISCO_IOS)
            .build();
    ConnectedRoute e1 =
        ConnectedRoute.builder()
            .setNetwork(Prefix.parse("2.0.0.0/24"))
            .setNextHop(NextHopInterface.of("e1", Ip.parse("2.0.0.1")))
            .build();
    EigrpExternalRoute e2 =
        EigrpExternalRoute.testBuilder()
            .setNetwork(Prefix.parse("2.0.0.0/24"))
            .setEigrpMetric(
                WideMetric.builder()
                    .setValues(EigrpMetricValues.builder().setBandwidth(1000).setDelay(2).build())
                    .build())
            .setEigrpMetricVersion(EigrpMetricVersion.V1)
            .setProcessAsn(10L)
            .setDestinationAsn(2L)
            .build();
    EigrpExternalRoute e3 =
        EigrpExternalRoute.testBuilder()
            .setNetwork(Prefix.parse("2.0.0.0/24"))
            .setEigrpMetric(
                WideMetric.builder()
                    .setValues(EigrpMetricValues.builder().setBandwidth(1000).setDelay(2).build())
                    .build())
            .setEigrpMetricVersion(EigrpMetricVersion.V1)
            .setProcessAsn(20L)
            .setDestinationAsn(2L)
            .build();
    EigrpExternalRoute e4 =
        EigrpExternalRoute.testBuilder()
            .setNetwork(Prefix.parse("2.0.0.0/24"))
            .setEigrpMetric(
                WideMetric.builder()
                    .setValues(EigrpMetricValues.builder().setBandwidth(1000).setDelay(2).build())
                    .build())
            .setEigrpMetricVersion(EigrpMetricVersion.V1)
            .setProcessAsn(30L)
            .setDestinationAsn(2L)
            .build();
    assertFalse(
        mpa.evaluate(Environment.builder(c).setOriginalRoute(e1).build()).getBooleanValue());
    assertTrue(mpa.evaluate(Environment.builder(c).setOriginalRoute(e2).build()).getBooleanValue());
    assertTrue(mpa.evaluate(Environment.builder(c).setOriginalRoute(e3).build()).getBooleanValue());
    assertFalse(
        mpa.evaluate(Environment.builder(c).setOriginalRoute(e4).build()).getBooleanValue());
  }
}
