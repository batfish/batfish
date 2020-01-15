package org.batfish.datamodel.trace;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.junit.Assert.assertEquals;

import org.batfish.datamodel.TraceElement;
import org.junit.Test;

/** Test for {@link Tracer}. */
public class TracerTest {
  private static TraceElement E1 = TraceElement.of("1");
  private static TraceElement E2 = TraceElement.of("2");

  @Test
  public void testBasic() {
    Tracer tracer = new Tracer();
    tracer.newSubTrace();
    tracer.setTraceElement(E1);
    tracer.endSubTrace();

    TraceTree trace = tracer.getTrace();
    assertEquals(E1, trace.getTraceElement());
    assertThat(trace.getChildren(), empty());
  }

  @Test
  public void testSubTrace() {
    Tracer tracer = new Tracer();
    tracer.newSubTrace();
    tracer.setTraceElement(E1);

    tracer.newSubTrace();
    tracer.setTraceElement(E2);
    tracer.endSubTrace();

    tracer.endSubTrace();

    TraceTree trace = tracer.getTrace();
    assertEquals(E1, trace.getTraceElement());
    assertEquals(1, trace.getChildren().size());

    TraceTree subTrace = trace.getChildren().get(0);
    assertEquals(E2, subTrace.getTraceElement());
    assertThat(subTrace.getChildren(), empty());
  }

  @Test
  public void testDiscardSubTrace() {
    Tracer tracer = new Tracer();
    tracer.newSubTrace();
    tracer.setTraceElement(E1);

    tracer.newSubTrace();
    tracer.setTraceElement(E2);
    tracer.discardSubTrace();

    tracer.endSubTrace();

    TraceTree trace = tracer.getTrace();
    assertEquals(E1, trace.getTraceElement());
    assertThat(trace.getChildren(), empty());
  }
}
