package org.batfish.common;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import io.opentracing.References;
import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import io.opentracing.mock.MockSpan.MockContext;
import io.opentracing.mock.MockTracer;
import io.opentracing.noop.NoopSpan;
import io.opentracing.noop.NoopTracerFactory;
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
    _mockTracer = new MockTracer();
    _noopTracer = NoopTracerFactory.create();
  }

  @Test
  public void testNullActiveSpanNoop() {
    _workItem.setSourceSpan(null, _noopTracer);
    SpanContext sourceSpanContext = _workItem.getSourceSpan(_noopTracer);
    Span childSpan =
        _noopTracer
            .buildSpan("test dangling child")
            .addReference(References.FOLLOWS_FROM, sourceSpanContext)
            .start();
    try (Scope scope = _noopTracer.scopeManager().activate(childSpan)) {
      assert scope != null;
      assertThat(childSpan, notNullValue());
      assertThat(childSpan, instanceOf(NoopSpan.class));
    }
    {
      childSpan.finish();
    }
  }

  @Test
  public void testExtractOnlyNoop() {
    SpanContext sourceSpanContext = _workItem.getSourceSpan(_noopTracer);
    Span childSpan =
        _noopTracer
            .buildSpan("test dangling child")
            .addReference(References.FOLLOWS_FROM, sourceSpanContext)
            .start();
    try (Scope scope = _noopTracer.scopeManager().activate(childSpan)) {
      assert scope != null;

      assertThat(childSpan, notNullValue());
      assertThat(childSpan, instanceOf(NoopSpan.class));
    }
  }

  @Test
  public void testInjectExtractNoop() {
    Span activeSpan = _noopTracer.buildSpan("test span").start();
    try (Scope scope = _noopTracer.scopeManager().activate(activeSpan)) {
      assert scope != null;
      _workItem.setSourceSpan(activeSpan, _noopTracer);
      Span childSpan =
          _noopTracer
              .buildSpan("test span")
              .addReference(References.FOLLOWS_FROM, _workItem.getSourceSpan(_noopTracer))
              .start();
      try (Scope childScope = _noopTracer.scopeManager().activate(childSpan)) {
        assert childScope != null;
        assertThat(childSpan, notNullValue());
        assertThat(childSpan, instanceOf(NoopSpan.class));
      }
      childSpan.finish();
    } finally {
      activeSpan.finish();
    }
  }

  @Test
  public void testNullActiveSpan() {
    _workItem.setSourceSpan(null, _mockTracer);
    SpanContext sourceSpanContext = _workItem.getSourceSpan(_mockTracer);

    assertThat(sourceSpanContext, nullValue());
    Span childSpan =
        _mockTracer
            .buildSpan("test dangling child")
            .addReference(References.FOLLOWS_FROM, sourceSpanContext)
            .start();
    try (Scope childScope = _mockTracer.scopeManager().activate(childSpan)) {
      assert childScope != null;
      assertThat(childSpan, notNullValue());
      // check the instance
      assertThat(childSpan, instanceOf(Span.class));
    }
  }

  @Test
  public void testExtractOnly() {
    SpanContext sourceSpanContext = _workItem.getSourceSpan(_mockTracer);

    assertThat(sourceSpanContext, nullValue());

    Span childSpan =
        _mockTracer
            .buildSpan("test dangling child")
            .addReference(References.FOLLOWS_FROM, sourceSpanContext)
            .start();
    try (Scope childScope = _mockTracer.scopeManager().activate(childSpan)) {
      assert childScope != null;
      assertThat(childSpan, notNullValue());
      assertThat(childSpan, instanceOf(Span.class));
    }
  }

  @Test
  public void testInjectExtract() {
    MockContext sourceContext;
    Span activeSpan = _mockTracer.buildSpan("test span").start();
    try (Scope childScope = _mockTracer.scopeManager().activate(activeSpan)) {
      assert childScope != null;
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
