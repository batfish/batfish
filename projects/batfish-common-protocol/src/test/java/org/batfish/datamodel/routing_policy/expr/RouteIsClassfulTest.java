package org.batfish.datamodel.routing_policy.expr;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.Lists;
import java.util.List;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.ConnectedRoute;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.Result;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class RouteIsClassfulTest {
  private static boolean evaluateIsClassful(Prefix network) {
    Configuration c = new Configuration("host", ConfigurationFormat.CISCO_IOS);
    ConnectedRoute route = new ConnectedRoute(network, "Ethernet0");
    Environment env = Environment.builder(c).setOriginalRoute(route).build();
    Result res = RouteIsClassful.instance().evaluate(env);
    return res.getBooleanValue();
  }

  @Test
  public void testJavaSerialization() {
    RouteIsClassful ric = RouteIsClassful.instance();
    assertThat(SerializationUtils.clone(ric), equalTo(ric));
  }

  @Test
  public void testRouteIsClassful() {
    List<String> classful =
        Lists.newArrayList("16.0.0.0/8", "128.0.0.0/16", "128.0.0.0/2", "192.168.1.0/24");
    for (String prefixStr : classful) {
      Prefix network = Prefix.parse(prefixStr);
      assertThat(network + " is classful", evaluateIsClassful(network), equalTo(true));
    }
  }

  @Test
  public void testRouteIsNotClassful() {
    List<String> notClassful = Lists.newArrayList("16.0.0.0/9", "128.0.0.0/17", "192.168.1.0/25");
    for (String prefixStr : notClassful) {
      Prefix network = Prefix.parse(prefixStr);
      assertThat(network + " is classful", evaluateIsClassful(network), equalTo(false));
    }
  }
}
