package org.batfish.datamodel.routing_policy.statement;

import static org.batfish.datamodel.matchers.TraceTreeMatchers.hasChildren;
import static org.batfish.datamodel.matchers.TraceTreeMatchers.hasTraceElement;
import static org.batfish.datamodel.matchers.TraceTreeMatchers.isTraceTree;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.common.testing.EqualsTester;
import java.util.List;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.TraceElement;
import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.expr.BooleanExprs;
import org.batfish.datamodel.trace.TraceTree;
import org.batfish.datamodel.trace.Tracer;
import org.junit.Test;

public class TraceableStatementTest {

  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(
            new TraceableStatement(ImmutableList.of(), TraceElement.of("text")),
            new TraceableStatement(ImmutableList.of(), TraceElement.of("text")))
        .addEqualityGroup(
            new TraceableStatement(ImmutableList.of(new If()), TraceElement.of("text")))
        .addEqualityGroup(new TraceableStatement(ImmutableList.of(), TraceElement.of("other")))
        .testEquals();
  }

  /* Trivial test: don't barf if there is not tracer in the environment */
  @Test
  public void testExecute_noTracer() {
    Configuration c = Configuration.builder().setHostname("host").build();
    Environment environment = Environment.builder(c).build();
    EMPTY_STATEMENT.execute(environment);
  }

  @Test
  public void testExecute() {
    // no inner statements
    assertThat(executeHelper(EMPTY_STATEMENT), contains(isTraceTree("empty")));

    // two traceable children
    assertThat(
        executeHelper(
            new TraceableStatement(
                ImmutableList.of(
                    new TraceableStatement(ImmutableList.of(), TraceElement.of("child1")),
                    new TraceableStatement(ImmutableList.of(), TraceElement.of("child2"))),
                TraceElement.of("parent"))),
        contains(
            allOf(
                hasTraceElement("parent"),
                hasChildren(isTraceTree("child1"), isTraceTree("child2")))));

    // mix of traceable and non-traceable children
    assertThat(
        executeHelper(
            new TraceableStatement(
                ImmutableList.of(
                    new If(BooleanExprs.TRUE, ImmutableList.of(), ImmutableList.of()),
                    new TraceableStatement(ImmutableList.of(), TraceElement.of("child1"))),
                TraceElement.of("parent"))),
        contains(allOf(hasTraceElement("parent"), hasChildren(isTraceTree("child1")))));
  }

  private static final TraceableStatement EMPTY_STATEMENT =
      new TraceableStatement(ImmutableList.of(), TraceElement.of("empty"));

  private static List<TraceTree> executeHelper(Statement statement) {
    Configuration c = Configuration.builder().setHostname("host").build();
    Tracer tracer = new Tracer();
    Environment environment = Environment.builder(c).setTracer(tracer).build();
    statement.execute(environment);
    return tracer.getTrace();
  }
}
