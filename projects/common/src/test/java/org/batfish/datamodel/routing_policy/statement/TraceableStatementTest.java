package org.batfish.datamodel.routing_policy.statement;

import static org.batfish.datamodel.matchers.TraceTreeMatchers.hasChildren;
import static org.batfish.datamodel.matchers.TraceTreeMatchers.hasTraceElement;
import static org.batfish.datamodel.matchers.TraceTreeMatchers.isTraceTree;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;

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
            new TraceableStatement(TraceElement.of("text"), ImmutableList.of()),
            new TraceableStatement(TraceElement.of("text"), ImmutableList.of()))
        .addEqualityGroup(
            new TraceableStatement(TraceElement.of("text"), ImmutableList.of(new If())))
        .addEqualityGroup(new TraceableStatement(TraceElement.of("other"), ImmutableList.of()))
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
                TraceElement.of("parent"),
                ImmutableList.of(
                    new TraceableStatement(TraceElement.of("child1"), ImmutableList.of()),
                    new TraceableStatement(TraceElement.of("child2"), ImmutableList.of())))),
        contains(
            allOf(
                hasTraceElement("parent"),
                hasChildren(isTraceTree("child1"), isTraceTree("child2")))));

    // mix of traceable and non-traceable children
    assertThat(
        executeHelper(
            new TraceableStatement(
                TraceElement.of("parent"),
                ImmutableList.of(
                    new If(BooleanExprs.TRUE, ImmutableList.of(), ImmutableList.of()),
                    new TraceableStatement(TraceElement.of("child1"), ImmutableList.of())))),
        contains(allOf(hasTraceElement("parent"), hasChildren(isTraceTree("child1")))));
  }

  @Test
  public void testSimplify() {
    Statement simplifiable =
        new If(
            BooleanExprs.TRUE,
            ImmutableList.of(Statements.ReturnTrue.toStaticStatement()),
            ImmutableList.of(Statements.ReturnFalse.toStaticStatement()));
    Statement simplified = Statements.ReturnTrue.toStaticStatement();

    assertThat(
        new TraceableStatement(
                TraceElement.of("text"), ImmutableList.of(simplifiable, simplifiable))
            .simplify(),
        equalTo(
            ImmutableList.of(
                new TraceableStatement(
                    TraceElement.of("text"), ImmutableList.of(simplified, simplified)))));
  }

  private static final TraceableStatement EMPTY_STATEMENT =
      new TraceableStatement(TraceElement.of("empty"), ImmutableList.of());

  private static List<TraceTree> executeHelper(Statement statement) {
    Configuration c = Configuration.builder().setHostname("host").build();
    Tracer tracer = new Tracer();
    Environment environment = Environment.builder(c).setTracer(tracer).build();
    statement.execute(environment);
    return tracer.getTrace();
  }
}
