package org.batfish.datamodel.trace;

import static org.batfish.datamodel.matchers.TraceTreeMatchers.hasChildren;
import static org.batfish.datamodel.matchers.TraceTreeMatchers.hasTraceElement;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;

import java.util.List;
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

    List<TraceTree> trace = tracer.getTrace();
    assertThat(trace, contains(allOf(hasTraceElement(E1), hasChildren(empty()))));
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

    List<TraceTree> trace = tracer.getTrace();
    assertThat(
        trace,
        contains(
            allOf(
                hasTraceElement(E1),
                hasChildren(contains(allOf(hasTraceElement(E2), hasChildren(empty())))))));
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

    List<TraceTree> trace = tracer.getTrace();
    assertThat(trace, contains(allOf(hasTraceElement(E1), hasChildren(empty()))));
  }

  @Test
  public void testNoTraceElement_root() {
    Tracer tracer = new Tracer();
    tracer.newSubTrace();

    tracer.newSubTrace();
    tracer.setTraceElement(E1);
    tracer.endSubTrace();

    tracer.newSubTrace();
    tracer.setTraceElement(E2);
    tracer.endSubTrace();

    tracer.endSubTrace();

    List<TraceTree> trace = tracer.getTrace();
    assertThat(
        trace,
        contains(
            allOf(hasTraceElement(E1), hasChildren(empty())),
            allOf(hasTraceElement(E2), hasChildren(empty()))));
  }

  @Test
  public void testNoTraceElement_leaf() {
    Tracer tracer = new Tracer();
    tracer.newSubTrace();
    tracer.setTraceElement(E1);

    tracer.newSubTrace();
    tracer.endSubTrace();

    tracer.endSubTrace();

    List<TraceTree> trace = tracer.getTrace();
    assertThat(trace, contains(allOf(hasTraceElement(E1), hasChildren(empty()))));
  }

  @Test
  public void testNoTraceElement_internal() {
    Tracer tracer = new Tracer();
    tracer.newSubTrace(); // root
    tracer.setTraceElement(E1);

    tracer.newSubTrace(); // internal

    tracer.newSubTrace(); // leaf
    tracer.setTraceElement(E2);
    tracer.endSubTrace(); // leaf

    tracer.endSubTrace(); // internal
    tracer.endSubTrace(); // root

    List<TraceTree> trace = tracer.getTrace();
    assertThat(
        trace,
        contains(
            allOf(
                hasTraceElement(E1),
                hasChildren(contains(allOf(hasTraceElement(E2), hasChildren(empty())))))));
  }
}
