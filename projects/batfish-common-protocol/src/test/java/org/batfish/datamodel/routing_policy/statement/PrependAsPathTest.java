package org.batfish.datamodel.routing_policy.statement;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Lists;
import java.util.Arrays;
import java.util.List;
import java.util.SortedSet;
import java.util.stream.Collectors;
import org.batfish.datamodel.BgpRoute;
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

  private static Environment newTestEnvironment(BgpRoute.Builder outputRoute) {
    Configuration c = new Configuration("host", ConfigurationFormat.CISCO_IOS);
    return new Environment(c, "vrf", null, null, outputRoute, null, null);
  }

  private static List<SortedSet<Integer>> mkAsPath(Integer... explicitAs) {
    return Arrays.stream(explicitAs).map(ImmutableSortedSet::of).collect(Collectors.toList());
  }

  @Test
  public void testPrepend() {
    List<AsExpr> prepend = Lists.newArrayList(new ExplicitAs(1), new ExplicitAs(2));
    PrependAsPath operation = new PrependAsPath(new LiteralAsList(prepend));
    BgpRoute.Builder builder = new BgpRoute.Builder();
    builder.setAsPath(mkAsPath(3, 4));
    Environment env = newTestEnvironment(builder);

    operation.execute(env);
    assertThat(builder.getAsPath(), equalTo(mkAsPath(1, 2, 3, 4)));
  }

  @Test
  public void testPrependWithIntermediateAttributes() {
    List<AsExpr> prepend = Lists.newArrayList(new ExplicitAs(1), new ExplicitAs(2));
    PrependAsPath operation = new PrependAsPath(new LiteralAsList(prepend));
    BgpRoute.Builder outputRoute = new BgpRoute.Builder();
    outputRoute.setAsPath(mkAsPath(3, 4));

    BgpRoute.Builder intermediateAttributes = new BgpRoute.Builder();
    intermediateAttributes.setAsPath(mkAsPath(5, 6));
    Environment env = newTestEnvironment(outputRoute);
    env.setIntermediateBgpAttributes(intermediateAttributes);
    env.setWriteToIntermediateBgpAttributes(true);

    operation.execute(env);
    assertThat(outputRoute.getAsPath(), equalTo(mkAsPath(1, 2, 3, 4)));
    assertThat(intermediateAttributes.getAsPath(), equalTo(mkAsPath(1, 2, 5, 6)));
  }
}
