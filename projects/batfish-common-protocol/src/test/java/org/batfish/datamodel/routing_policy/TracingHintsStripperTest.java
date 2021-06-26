package org.batfish.datamodel.routing_policy;

import static org.batfish.datamodel.routing_policy.TracingHintsStripper.STRIP_TOKEN;
import static org.batfish.datamodel.routing_policy.expr.BooleanExprs.TRUE;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.collect.ImmutableList;
import java.util.List;
import org.batfish.datamodel.TraceElement;
import org.batfish.datamodel.routing_policy.statement.If;
import org.batfish.datamodel.routing_policy.statement.Statement;
import org.batfish.datamodel.routing_policy.statement.TraceableStatement;
import org.batfish.vendor.VendorStructureId;
import org.junit.Test;

public class TracingHintsStripperTest {

  private static final TracingHintsStripper TRACING_HINTS_STRIPPER = new TracingHintsStripper();

  @Test
  public void testVisitIf() {
    List<Statement> trueStatements =
        ImmutableList.of(
            new TraceableStatement(TraceElement.of("true"), ImmutableList.of(new If())));
    List<Statement> falseStatements =
        ImmutableList.of(new TraceableStatement(TraceElement.of("false"), ImmutableList.of()));

    If ifStatement = new If(TRUE, trueStatements, falseStatements);

    assertThat(
        ifStatement.accept(TRACING_HINTS_STRIPPER, null),
        equalTo(
            new If(
                TRUE,
                ImmutableList.of(
                    new TraceableStatement(
                        TraceElement.of(STRIP_TOKEN), ImmutableList.of(new If()))),
                ImmutableList.of(
                    new TraceableStatement(TraceElement.of(STRIP_TOKEN), ImmutableList.of())))));
  }

  @Test
  public void testVisitTraceableStatement() {
    TraceableStatement strippedStatement =
        (TraceableStatement)
            new TraceableStatement(
                    TraceElement.builder()
                        .add("text", new VendorStructureId("a", "b", "c"))
                        .build(),
                    ImmutableList.of(
                        new TraceableStatement(
                            TraceElement.of("inner"), ImmutableList.of(new If()))))
                .accept(TRACING_HINTS_STRIPPER, null);

    assertThat(
        strippedStatement,
        equalTo(
            new TraceableStatement(
                TraceElement.of(STRIP_TOKEN),
                ImmutableList.of(
                    new TraceableStatement(
                        TraceElement.of(STRIP_TOKEN), ImmutableList.of(new If()))))));
  }
}
