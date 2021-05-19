package org.batfish.datamodel.routing_policy.as_path;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Range;
import org.batfish.datamodel.AsPath;
import org.batfish.datamodel.AsSet;
import org.batfish.datamodel.routing_policy.expr.IntComparator;
import org.batfish.datamodel.routing_policy.expr.IntComparison;
import org.batfish.datamodel.routing_policy.expr.LiteralInt;
import org.junit.Test;

/** Test of {@link AsPathMatchExprEvaluator}. */
public final class AsPathMatchExprEvaluatorTest {

  @Test
  public void testAsPathMatchAnyEmpty() {
    assertFalse(eval(AsPathMatchAny.of(ImmutableList.of()), AsPath.ofSingletonAsSets(1L)));
  }

  @Test
  public void testAsPathMatchAnyTrueFalse() {
    assertTrue(
        eval(
            AsPathMatchAny.of(
                ImmutableList.of(
                    HasAsPathLength.of(new IntComparison(IntComparator.EQ, new LiteralInt(1))),
                    HasAsPathLength.of(new IntComparison(IntComparator.EQ, new LiteralInt(2))))),
            AsPath.ofSingletonAsSets(1L)));
  }

  @Test
  public void testAsPathMatchAnyFalse() {
    assertFalse(
        eval(
            AsPathMatchAny.of(
                ImmutableList.of(
                    HasAsPathLength.of(new IntComparison(IntComparator.EQ, new LiteralInt(2))))),
            AsPath.ofSingletonAsSets(1L)));
  }

  @Test
  public void testAsPathMatchExprReference() {
    String name = "expr1";
    AsPathMatchExpr target =
        HasAsPathLength.of(new IntComparison(IntComparator.EQ, new LiteralInt(1)));
    AsPathMatchExprReference reference = AsPathMatchExprReference.of(name);
    AsPath asPath = AsPath.ofSingletonAsSets(5L);

    assertTrue(
        reference.accept(
            new AsPathMatchExprEvaluator(
                AsPathContext.builder()
                    .setAsPathMatchExprs(ImmutableMap.of(name, target))
                    .setInputAsPath(asPath)
                    .build()),
            asPath));
  }

  @Test
  public void testAsPathMatchRegex() {
    AsPathMatchRegex match = AsPathMatchRegex.of("^2 5");
    assertTrue(eval(match, AsPath.ofSingletonAsSets(2L, 5L)));
    assertTrue(eval(match, AsPath.ofSingletonAsSets(2L, 5L, 6L)));
    assertFalse(eval(match, AsPath.ofSingletonAsSets(1L, 2L, 5L)));
  }

  @Test
  public void testAsSetsMatchingRangesAnchorNone() {
    assertTrue(
        eval(
            AsSetsMatchingRanges.of(
                false,
                false,
                ImmutableList.of(Range.singleton(2L), Range.singleton(3L), Range.singleton(4L))),
            AsPath.ofSingletonAsSets(2L, 3L, 4L)));
    assertTrue(
        eval(
            AsSetsMatchingRanges.of(
                false,
                false,
                ImmutableList.of(Range.singleton(2L), Range.singleton(3L), Range.singleton(4L))),
            AsPath.ofSingletonAsSets(1L, 2L, 3L, 4L)));
    assertTrue(
        eval(
            AsSetsMatchingRanges.of(
                false,
                false,
                ImmutableList.of(Range.singleton(2L), Range.singleton(3L), Range.singleton(4L))),
            AsPath.ofSingletonAsSets(2L, 3L, 4L, 5L)));
    assertTrue(
        eval(
            AsSetsMatchingRanges.of(
                false,
                false,
                ImmutableList.of(Range.singleton(2L), Range.closed(1L, 100L), Range.singleton(4L))),
            AsPath.ofSingletonAsSets(2L, 3L, 4L, 5L)));

    // non-singleton as-sets
    assertTrue(
        eval(
            AsSetsMatchingRanges.of(
                false,
                false,
                ImmutableList.of(Range.singleton(2L), Range.closed(1L, 100L), Range.singleton(4L))),
            AsPath.of(ImmutableList.of(AsSet.of(2L), AsSet.of(1000L, 50L), AsSet.of(4L)))));
    assertFalse(
        eval(
            AsSetsMatchingRanges.of(
                false,
                false,
                ImmutableList.of(Range.singleton(2L), Range.closed(1L, 100L), Range.singleton(4L))),
            AsPath.of(ImmutableList.of(AsSet.of(2L), AsSet.of(1000L, 2000L), AsSet.of(4L)))));

    // too many ranges
    assertFalse(
        eval(
            AsSetsMatchingRanges.of(
                false,
                false,
                ImmutableList.of(Range.singleton(2L), Range.singleton(3L), Range.singleton(4L))),
            AsPath.ofSingletonAsSets(2L, 3L)));
  }

  @Test
  public void testAsSetsMatchingRangesAnchorStart() {
    assertTrue(
        eval(
            AsSetsMatchingRanges.of(
                false,
                true,
                ImmutableList.of(Range.singleton(2L), Range.singleton(3L), Range.singleton(4L))),
            AsPath.ofSingletonAsSets(2L, 3L, 4L)));
    assertFalse(
        eval(
            AsSetsMatchingRanges.of(
                false,
                true,
                ImmutableList.of(Range.singleton(2L), Range.singleton(3L), Range.singleton(4L))),
            AsPath.ofSingletonAsSets(1L, 2L, 3L, 4L)));
    assertTrue(
        eval(
            AsSetsMatchingRanges.of(
                false,
                true,
                ImmutableList.of(Range.singleton(2L), Range.singleton(3L), Range.singleton(4L))),
            AsPath.ofSingletonAsSets(2L, 3L, 4L, 5L)));
  }

  @Test
  public void testAsSetsMatchingRangesAnchorEnd() {
    assertTrue(
        eval(
            AsSetsMatchingRanges.of(
                true,
                false,
                ImmutableList.of(Range.singleton(2L), Range.singleton(3L), Range.singleton(4L))),
            AsPath.ofSingletonAsSets(2L, 3L, 4L)));
    assertTrue(
        eval(
            AsSetsMatchingRanges.of(
                true,
                false,
                ImmutableList.of(Range.singleton(2L), Range.singleton(3L), Range.singleton(4L))),
            AsPath.ofSingletonAsSets(1L, 2L, 3L, 4L)));
    assertFalse(
        eval(
            AsSetsMatchingRanges.of(
                true,
                false,
                ImmutableList.of(Range.singleton(2L), Range.singleton(3L), Range.singleton(4L))),
            AsPath.ofSingletonAsSets(2L, 3L, 4L, 5L)));
  }

  @Test
  public void testAsSetsMatchingRangesAnchorBoth() {
    assertTrue(
        eval(
            AsSetsMatchingRanges.of(
                true,
                true,
                ImmutableList.of(Range.singleton(2L), Range.singleton(3L), Range.singleton(4L))),
            AsPath.ofSingletonAsSets(2L, 3L, 4L)));
    assertFalse(
        eval(
            AsSetsMatchingRanges.of(
                true,
                true,
                ImmutableList.of(Range.singleton(2L), Range.singleton(3L), Range.singleton(4L))),
            AsPath.ofSingletonAsSets(1L, 2L, 3L, 4L)));
    assertFalse(
        eval(
            AsSetsMatchingRanges.of(
                true,
                true,
                ImmutableList.of(Range.singleton(2L), Range.singleton(3L), Range.singleton(4L))),
            AsPath.ofSingletonAsSets(2L, 3L, 4L, 5L)));
    assertFalse(
        eval(
            AsSetsMatchingRanges.of(
                true,
                true,
                ImmutableList.of(Range.singleton(2L), Range.singleton(3L), Range.singleton(5L))),
            AsPath.ofSingletonAsSets(2L, 3L, 4L)));
  }

  @Test
  public void testHasAsPathLength() {
    {
      // test singleton
      AsPath asPath = AsPath.ofSingletonAsSets(5L);
      assertTrue(
          eval(HasAsPathLength.of(new IntComparison(IntComparator.EQ, new LiteralInt(1))), asPath));
      assertFalse(
          eval(HasAsPathLength.of(new IntComparison(IntComparator.EQ, new LiteralInt(0))), asPath));
      assertFalse(
          eval(HasAsPathLength.of(new IntComparison(IntComparator.EQ, new LiteralInt(2))), asPath));
    }
    // test set
    {
      AsPath asPath = AsPath.of(ImmutableList.of(AsSet.of(1L), AsSet.of(2L, 3L)));
      assertTrue(
          eval(HasAsPathLength.of(new IntComparison(IntComparator.EQ, new LiteralInt(2))), asPath));
      assertFalse(
          eval(HasAsPathLength.of(new IntComparison(IntComparator.EQ, new LiteralInt(1))), asPath));
      assertFalse(
          eval(HasAsPathLength.of(new IntComparison(IntComparator.EQ, new LiteralInt(3))), asPath));
    }
  }

  private static boolean eval(AsPathMatchExpr expr, AsPath asPath) {
    return expr.accept(
        new AsPathMatchExprEvaluator(AsPathContext.builder().setInputAsPath(asPath).build()),
        asPath);
  }
}
