package org.batfish.datamodel.trace;

import static org.junit.Assert.assertEquals;

import com.google.common.collect.ImmutableList;
import com.google.common.testing.EqualsTester;
import java.io.IOException;
import java.util.List;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.TraceElement;
import org.junit.Test;

/** Test for {@link TraceNode}. */
public final class TraceNodeTest {
  @Test
  public void testEquals() {
    TraceElement traceElement1 = TraceElement.of("1");
    TraceElement traceElement2 = TraceElement.of("2");
    List<TraceNode> children1 = ImmutableList.of();
    List<TraceNode> children2 = ImmutableList.of(new TraceNode(null, ImmutableList.of()));
    new EqualsTester()
        .addEqualityGroup(
            new TraceNode(traceElement1, children1), new TraceNode(traceElement1, children1))
        .addEqualityGroup(new TraceNode(traceElement2, children1))
        .addEqualityGroup(new TraceNode(traceElement1, children2))
        .testEquals();
  }

  @Test
  public void testJsonSerialization() throws IOException {
    TraceNode traceNode =
        new TraceNode(
            TraceElement.of("a"),
            ImmutableList.of(new TraceNode(TraceElement.of("b"), ImmutableList.of())));
    TraceNode clone = BatfishObjectMapper.clone(traceNode, TraceNode.class);
    assertEquals(traceNode, clone);
  }
}
