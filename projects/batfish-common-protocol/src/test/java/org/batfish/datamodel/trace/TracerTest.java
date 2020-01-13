package org.batfish.datamodel.trace;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.batfish.datamodel.acl.TraceEvent;
import org.junit.Test;

/** Test for {@link Tracer}. */
public class TracerTest {
  private static final class TestTraceEvent implements TraceEvent {
    @Override
    public String getDescription() {
      return null;
    }
  }

  private static TraceEvent E1 = new TestTraceEvent();
  private static TraceEvent E2 = new TestTraceEvent();

  @Test
  public void testBasic() {
    Tracer tracer = new Tracer();
    tracer.newSubTrace();
    tracer.setEvent(E1);
    tracer.endSubTrace();

    TraceNode trace = tracer.getTrace();
    assertEquals(E1, trace.getTraceEvent());
    assertTrue(trace.getChildren().isEmpty());
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
    assertEquals(1,trace.getChildren().size());

    TraceNode subTrace = trace.getChildren().get(0);
    assertEquals(E2, subTrace.getTraceEvent());
    assertTrue(subTrace.getChildren().isEmpty());
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
    assertTrue(trace.getChildren().isEmpty());
  }
}
