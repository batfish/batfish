package org.batfish.datamodel.routing_policy.statement;

import static org.batfish.datamodel.AsPath.ofSingletonAsSets;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.collect.Lists;
import java.util.List;
import org.batfish.datamodel.Bgpv4Route;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.expr.AsExpr;
import org.batfish.datamodel.routing_policy.expr.ExplicitAs;
import org.batfish.datamodel.routing_policy.expr.LiteralAsList;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class PrependAsPathTest {

  private static Environment newTestEnvironment(Bgpv4Route.Builder outputRoute) {
    Configuration c = new Configuration("host", ConfigurationFormat.CISCO_IOS);
    return Environment.builder(c).setOutputRoute(outputRoute).build();
  }

  @Test
  public void testPrepend() {
    List<AsExpr> prepend = Lists.newArrayList(new ExplicitAs(1), new ExplicitAs(2));
    PrependAsPath operation = new PrependAsPath(new LiteralAsList(prepend));
    Bgpv4Route.Builder builder = Bgpv4Route.testBuilder();
    builder.setAsPath(ofSingletonAsSets(3L, 4L));
    Environment env = newTestEnvironment(builder);

    operation.execute(env);
    assertThat(builder.getAsPath(), equalTo(ofSingletonAsSets(1L, 2L, 3L, 4L)));
  }

  @Test
  public void testPrependWithIntermediateAttributes() {
    List<AsExpr> prepend = Lists.newArrayList(new ExplicitAs(1), new ExplicitAs(2));
    PrependAsPath operation = new PrependAsPath(new LiteralAsList(prepend));
    Bgpv4Route.Builder outputRoute = Bgpv4Route.testBuilder();
    outputRoute.setAsPath(ofSingletonAsSets(3L, 4L));

    Bgpv4Route.Builder intermediateAttributes = Bgpv4Route.testBuilder();
    intermediateAttributes.setAsPath(ofSingletonAsSets(5L, 6L));
    Environment env = newTestEnvironment(outputRoute);
    env.setIntermediateBgpAttributes(intermediateAttributes);
    env.setWriteToIntermediateBgpAttributes(true);

    operation.execute(env);
    assertThat(outputRoute.getAsPath(), equalTo(ofSingletonAsSets(1L, 2L, 3L, 4L)));
    assertThat(intermediateAttributes.getAsPath(), equalTo(ofSingletonAsSets(1L, 2L, 5L, 6L)));
  }
}
