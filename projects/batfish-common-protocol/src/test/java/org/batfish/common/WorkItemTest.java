package org.batfish.common;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import io.opentracing.ActiveSpan;
import io.opentracing.NoopActiveSpanSource.NoopActiveSpan;
import io.opentracing.NoopTracerFactory;
import io.opentracing.References;
import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import io.opentracing.mock.MockSpan.MockContext;
import io.opentracing.mock.MockTracer;
import io.opentracing.mock.MockTracer.Propagator;
import io.opentracing.util.ThreadLocalActiveSpan;
import io.opentracing.util.ThreadLocalActiveSpanSource;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/** Tests for {@link WorkItem}. */
public class WorkItemTest {

  private WorkItem _workItem;
  private static Tracer _mockTracer;
  private static Tracer _noopTracer;

  @Before
  public void initWorkItem() {
    _workItem = new WorkItem("testContainer", "testTestrig");
  }

  @BeforeClass
  public static void initTracer() {
    _mockTracer = new MockTracer(new ThreadLocalActiveSpanSource(), Propagator.TEXT_MAP);
    _noopTracer = NoopTracerFactory.create();
  }

  @Test
  public void testNullActiveSpanNoop() {
    _workItem.setSourceSpan(null, _noopTracer);
    SpanContext sourceSpanContext = _workItem.getSourceSpan(_noopTracer);

    try (ActiveSpan childSpan =
        _noopTracer
            .buildSpan("test dangling child")
            .addReference(References.FOLLOWS_FROM, sourceSpanContext)
            .startActive()) {

      assertThat(childSpan, notNullValue());
      assertThat(childSpan, instanceOf(NoopActiveSpan.class));
    }
  }

  @Test
  public void testExtractOnlyNoop() {
    SpanContext sourceSpanContext = _workItem.getSourceSpan(_noopTracer);

    try (ActiveSpan childSpan =
        _noopTracer
            .buildSpan("test dangling child")
            .addReference(References.FOLLOWS_FROM, sourceSpanContext)
            .startActive()) {

      assertThat(childSpan, notNullValue());
      assertThat(childSpan, instanceOf(NoopActiveSpan.class));
    }
  }

  @Test
  public void testInjectExtractNoop() {
    ActiveSpan activeSpan = _noopTracer.buildSpan("test span").startActive();
    _workItem.setSourceSpan(activeSpan, _noopTracer);

    try (ActiveSpan childSpan =
        _noopTracer
            .buildSpan("test span")
            .addReference(References.FOLLOWS_FROM, _workItem.getSourceSpan(_noopTracer))
            .startActive()) {

      assertThat(childSpan, notNullValue());
      assertThat(childSpan, instanceOf(NoopActiveSpan.class));
    }
  }

  @Test
  public void testNullActiveSpan() {
    _workItem.setSourceSpan(null, _mockTracer);
    SpanContext sourceSpanContext = _workItem.getSourceSpan(_mockTracer);

    assertThat(sourceSpanContext, nullValue());

    try (ActiveSpan childSpan =
        _mockTracer
            .buildSpan("test dangling child")
            .addReference(References.FOLLOWS_FROM, sourceSpanContext)
            .startActive()) {

      assertThat(childSpan, notNullValue());
      assertThat(childSpan, instanceOf(ThreadLocalActiveSpan.class));
    }
  }

  @Test
  public void testExtractOnly() {
    SpanContext sourceSpanContext = _workItem.getSourceSpan(_mockTracer);

    assertThat(sourceSpanContext, nullValue());

    try (ActiveSpan childSpan =
        _mockTracer
            .buildSpan("test dangling child")
            .addReference(References.FOLLOWS_FROM, sourceSpanContext)
            .startActive()) {

      assertThat(childSpan, notNullValue());
      assertThat(childSpan, instanceOf(ThreadLocalActiveSpan.class));
    }
  }

  @Test
  public void testInjectExtract() {
    MockContext sourceContext;

    try (ActiveSpan activeSpan = _mockTracer.buildSpan("test span").startActive()) {
      SpanContext sourceContextTmp = activeSpan.context();
      assertThat(sourceContextTmp, instanceOf(MockContext.class));
      sourceContext = (MockContext) sourceContextTmp;

      _workItem.setSourceSpan(activeSpan, _mockTracer);
    }

    SpanContext extractedContextTmp = _workItem.getSourceSpan(_mockTracer);
    assertThat(extractedContextTmp, notNullValue());
    assertThat(extractedContextTmp, instanceOf(MockContext.class));
    MockContext extractedContext = (MockContext) extractedContextTmp;

    // test that injected and extracted spans have same span context data
    assertThat(extractedContext.traceId(), equalTo(sourceContext.traceId()));
    assertThat(extractedContext.spanId(), equalTo(sourceContext.spanId()));
  }
}
