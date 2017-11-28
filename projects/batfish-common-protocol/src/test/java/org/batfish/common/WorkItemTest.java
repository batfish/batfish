package org.batfish.common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.uber.jaeger.Configuration;
import com.uber.jaeger.Configuration.ReporterConfiguration;
import com.uber.jaeger.Configuration.SamplerConfiguration;
import com.uber.jaeger.samplers.ConstSampler;
import io.opentracing.ActiveSpan;
import io.opentracing.References;
import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import io.opentracing.util.GlobalTracer;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/** Tests for {@link WorkItem}. */
public class WorkItemTest {

  private WorkItem _workItem;
  private static Tracer _tracer;

  @Before
  public void initWorkItem() {
    _workItem = new WorkItem("testContainer", "testTestrig");
  }

  @BeforeClass
  public static void initTracer() {
    _tracer =
        new Configuration(
                "work-item-test",
                new SamplerConfiguration(ConstSampler.TYPE, 1),
                new ReporterConfiguration(
                    false,
                    "localhost",
                    14267,
                    /* flush interval in ms */ 1000,
                    /* max buffered Spans */ 10000))
            .getTracer();
  }

  @Test
  public void testExtractOnlyNoop() {
    SpanContext sourceSpanContext = _workItem.getSourceSpan();
    ActiveSpan childSpan =
        GlobalTracer.get()
            .buildSpan("test dangling child")
            .addReference(References.FOLLOWS_FROM, sourceSpanContext)
            .startActive();

    assertNotNull(childSpan);
  }

  @Test
  public void testInjectExtractNoop() {
    ActiveSpan activeSpan = GlobalTracer.get().buildSpan("test span").startActive();
    _workItem.setSourceSpan(activeSpan);
    ActiveSpan childSpan =
        GlobalTracer.get()
            .buildSpan("test span")
            .addReference(References.FOLLOWS_FROM, _workItem.getSourceSpan())
            .startActive();

    assertNotNull(childSpan);
  }

  @Test
  public void testExtractOnly() {
    GlobalTracer.register(_tracer);
    SpanContext sourceSpanContext = _workItem.getSourceSpan();
    ActiveSpan childSpan =
        GlobalTracer.get()
            .buildSpan("test dangling child")
            .addReference(References.FOLLOWS_FROM, sourceSpanContext)
            .startActive();

    assertNotNull(childSpan);
  }

  @Test
  public void testInjectExtract() {
    GlobalTracer.register(_tracer);
    ActiveSpan activeSpan = GlobalTracer.get().buildSpan("test span").startActive();
    _workItem.setSourceSpan(activeSpan);
    SpanContext sourceSpan = _workItem.getSourceSpan();

    // test that injected and extracted spans have same span context, (traceId, spanId, parentId,
    // flags)
    assertEquals(activeSpan.context().toString(), sourceSpan.toString());
  }
}
