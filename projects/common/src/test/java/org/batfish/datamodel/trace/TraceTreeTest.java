package org.batfish.datamodel.trace;

import static org.junit.Assert.assertEquals;

import com.google.common.collect.ImmutableList;
import com.google.common.testing.EqualsTester;
import java.util.List;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.TraceElement;
import org.junit.Test;

/** Test for {@link TraceTree}. */
public final class TraceTreeTest {
  @Test
  public void testEquals() {
    TraceElement traceElement1 = TraceElement.of("1");
    TraceElement traceElement2 = TraceElement.of("2");
    List<TraceTree> children1 = ImmutableList.of();
    List<TraceTree> children2 = ImmutableList.of(new TraceTree(null, ImmutableList.of()));
    new EqualsTester()
        .addEqualityGroup(
            new TraceTree(traceElement1, children1), new TraceTree(traceElement1, children1))
        .addEqualityGroup(new TraceTree(traceElement2, children1))
        .addEqualityGroup(new TraceTree(traceElement1, children2))
        .testEquals();
  }

  @Test
  public void testJsonSerialization() {
    TraceTree traceTree =
        new TraceTree(
            TraceElement.of("a"),
            ImmutableList.of(new TraceTree(TraceElement.of("b"), ImmutableList.of())));
    TraceTree clone = BatfishObjectMapper.clone(traceTree, TraceTree.class);
    assertEquals(traceTree, clone);
  }
}
