package org.batfish.datamodel.trace;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.junit.Assert.assertEquals;

import org.batfish.datamodel.acl.TraceEvent;
import org.junit.Test;

/** Test for {@link Tracer}. */
public class TracerTest {
  private static TraceEvent E1 = new TraceEvent("1");
  private static TraceEvent E2 = new TraceEvent("2");

  @Test
  public void testBasic() {
    Tracer tracer = new Tracer();
    tracer.newSubTrace();
    tracer.setEvent(E1);
    tracer.endSubTrace();

    TraceNode trace = tracer.getTrace();
    assertEquals(E1, trace.getTraceEvent());
    assertThat(trace.getChildren(), empty());
  }

  @Test
  public void testSubTrace() {
    Tracer tracer = new Tracer();
    tracer.newSubTrace();
    tracer.setEvent(E1);

    tracer.newSubTrace();
    tracer.setEvent(E2);
    tracer.endSubTrace();

    tracer.endSubTrace();

    TraceNode trace = tracer.getTrace();
    assertEquals(E1, trace.getTraceEvent());
    assertEquals(1, trace.getChildren().size());

    TraceNode subTrace = trace.getChildren().get(0);
    assertEquals(E2, subTrace.getTraceEvent());
    assertThat(subTrace.getChildren(), empty());
  }

  @Test
  public void testDiscardSubTrace() {
    Tracer tracer = new Tracer();
    tracer.newSubTrace();
    tracer.setEvent(E1);

    tracer.newSubTrace();
    tracer.setEvent(E2);
    tracer.discardSubTrace();

    tracer.endSubTrace();

    TraceNode trace = tracer.getTrace();
    assertEquals(E1, trace.getTraceEvent());
    assertThat(trace.getChildren(), empty());
  }
}
