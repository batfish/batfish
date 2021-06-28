package org.batfish.question;

import static org.batfish.question.CompareSameNameQuestionPlugin.CompareSameNameAnswerer.stripTracingHints;
import static org.batfish.question.TracingHintsStripper.STRIP_TOKEN;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.ImmutableList;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.TraceElement;
import org.batfish.datamodel.routing_policy.RoutingPolicy;
import org.batfish.datamodel.routing_policy.statement.If;
import org.batfish.datamodel.routing_policy.statement.TraceableStatement;
import org.batfish.vendor.VendorStructureId;
import org.junit.Test;

public class CompareSameNameQuestionPluginTest {

  @Test
  public void testStripTracingHints() throws JsonProcessingException {
    TraceableStatement traceableStatement =
        new TraceableStatement(
            TraceElement.builder().add("text", new VendorStructureId("a", "b", "c")).build(),
            ImmutableList.of(
                new TraceableStatement(TraceElement.of("inner"), ImmutableList.of(new If()))));

    RoutingPolicy routingPolicy =
        RoutingPolicy.builder()
            .setName("test")
            .setStatements(ImmutableList.of(traceableStatement))
            .build();

    RoutingPolicy expectedStrippedPolicy =
        RoutingPolicy.builder()
            .setName("test")
            .setStatements(
                ImmutableList.of(
                    new TraceableStatement(
                        TraceElement.of(STRIP_TOKEN),
                        ImmutableList.of(
                            new TraceableStatement(
                                TraceElement.of(STRIP_TOKEN),
                                ImmutableList.of(
                                    new If(null, ImmutableList.of(), ImmutableList.of())))))))
            .build();
    assertThat(
        stripTracingHints(routingPolicy),
        equalTo(BatfishObjectMapper.writePrettyString(expectedStrippedPolicy)));
  }
}
