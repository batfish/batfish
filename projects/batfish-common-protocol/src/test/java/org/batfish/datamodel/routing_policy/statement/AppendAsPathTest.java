package org.batfish.datamodel.routing_policy.statement;

import static org.batfish.datamodel.AsPath.ofSingletonAsSets;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.common.testing.EqualsTester;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import org.apache.commons.lang3.SerializationUtils;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.Bgpv4Route;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.expr.AsExpr;
import org.batfish.datamodel.routing_policy.expr.ExplicitAs;
import org.batfish.datamodel.routing_policy.expr.LiteralAsList;
import org.junit.Test;

/** Test of {@link AppendAsPath}. */
@ParametersAreNonnullByDefault
public final class AppendAsPathTest {

  private static @Nonnull Environment newTestEnvironment(Bgpv4Route.Builder outputRoute) {
    Configuration c = new Configuration("host", ConfigurationFormat.CISCO_IOS);
    return Environment.builder(c).setOutputRoute(outputRoute).build();
  }

  @Test
  public void testJavaSerialization() {
    Statement obj = new AppendAsPath(new LiteralAsList(ImmutableList.of(new ExplicitAs(1L))));
    assertThat(SerializationUtils.clone(obj), equalTo(obj));
  }

  @Test
  public void testJacksonSerialization() {
    Statement obj = new AppendAsPath(new LiteralAsList(ImmutableList.of(new ExplicitAs(1L))));
    assertThat(BatfishObjectMapper.clone(obj, Statement.class), equalTo(obj));
  }

  @Test
  public void testEquals() {
    Statement obj = new AppendAsPath(new LiteralAsList(ImmutableList.of(new ExplicitAs(1L))));
    new EqualsTester()
        .addEqualityGroup(
            obj, new AppendAsPath(new LiteralAsList(ImmutableList.of(new ExplicitAs(1L)))))
        .addEqualityGroup(new AppendAsPath(new LiteralAsList(ImmutableList.of(new ExplicitAs(2L)))))
        .testEquals();
  }

  @Test
  public void testEvaluate() {
    List<AsExpr> toAppend = ImmutableList.of(new ExplicitAs(1L), new ExplicitAs(2L));
    AppendAsPath operation = new AppendAsPath(new LiteralAsList(toAppend));
    Bgpv4Route.Builder builder = Bgpv4Route.testBuilder();
    builder.setAsPath(ofSingletonAsSets(3L, 4L));
    Environment env = newTestEnvironment(builder);

    operation.execute(env);
    assertThat(builder.getAsPath(), equalTo(ofSingletonAsSets(3L, 4L, 1L, 2L)));
  }

  @Test
  public void testEvaluateWriteIntermediateAttributes() {
    List<AsExpr> toAppend = ImmutableList.of(new ExplicitAs(1L), new ExplicitAs(2L));
    AppendAsPath operation = new AppendAsPath(new LiteralAsList(toAppend));
    Bgpv4Route.Builder outputRoute = Bgpv4Route.testBuilder();
    outputRoute.setAsPath(ofSingletonAsSets(3L, 4L));

    Bgpv4Route.Builder intermediateAttributes = Bgpv4Route.testBuilder();
    intermediateAttributes.setAsPath(ofSingletonAsSets(5L, 6L));
    Environment env = newTestEnvironment(outputRoute);
    env.setIntermediateBgpAttributes(intermediateAttributes);
    env.setWriteToIntermediateBgpAttributes(true);

    operation.execute(env);
    assertThat(outputRoute.getAsPath(), equalTo(ofSingletonAsSets(3L, 4L, 1L, 2L)));
    assertThat(intermediateAttributes.getAsPath(), equalTo(ofSingletonAsSets(3L, 4L, 1L, 2L)));
  }

  @Test
  public void testEvaluateReadAndWriteIntermediateAttributes() {
    List<AsExpr> toAppend = ImmutableList.of(new ExplicitAs(1L), new ExplicitAs(2L));
    AppendAsPath operation = new AppendAsPath(new LiteralAsList(toAppend));
    Bgpv4Route.Builder outputRoute = Bgpv4Route.testBuilder();
    outputRoute.setAsPath(ofSingletonAsSets(3L, 4L));

    Bgpv4Route.Builder intermediateAttributes = Bgpv4Route.testBuilder();
    intermediateAttributes.setAsPath(ofSingletonAsSets(5L, 6L));
    Environment env = newTestEnvironment(outputRoute);
    env.setIntermediateBgpAttributes(intermediateAttributes);
    env.setReadFromIntermediateBgpAttributes(true);
    env.setWriteToIntermediateBgpAttributes(true);

    operation.execute(env);
    assertThat(outputRoute.getAsPath(), equalTo(ofSingletonAsSets(5L, 6L, 1L, 2L)));
    assertThat(intermediateAttributes.getAsPath(), equalTo(ofSingletonAsSets(5L, 6L, 1L, 2L)));
  }
}
