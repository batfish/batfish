package org.batfish.datamodel.routing_policy.as_path;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableMap;
import org.batfish.datamodel.AsPath;
import org.junit.jupiter.api.Test;

/** Test of {@link AsPathExprEvaluator}. */
final class AsPathExprEvaluatorTest {

  @Test
  void testAsPathExprReference() {
    String name = "expr1";
    AsPathExpr target = InputAsPath.instance();
    AsPathExprReference reference = AsPathExprReference.of(name);
    AsPath asPath = AsPath.ofSingletonAsSets(5L);

    assertThat(
        reference.accept(
            AsPathExprEvaluator.instance(),
            AsPathContext.builder()
                .setAsPathExprs(ImmutableMap.of(name, target))
                .setInputAsPath(asPath)
                .build()),
        equalTo(asPath));
  }

  @Test
  void testDedupedAsPath() {
    AsPath asPath = AsPath.ofSingletonAsSets(1L, 1L, 2L, 3L, 2L, 2L, 4L);
    assertThat(
        DedupedAsPath.of(InputAsPath.instance())
            .accept(AsPathExprEvaluator.instance(), context(asPath)),
        equalTo(AsPath.ofSingletonAsSets(1L, 2L, 3L, 2L, 4L)));

    assertThat(
        DedupedAsPath.of(InputAsPath.instance())
            .accept(AsPathExprEvaluator.instance(), context(AsPath.empty())),
        equalTo(AsPath.empty()));
  }

  @Test
  void testInputAsPath() {
    AsPath asPath = AsPath.ofSingletonAsSets(1L);
    assertThat(
        InputAsPath.instance().accept(AsPathExprEvaluator.instance(), context(asPath)),
        equalTo(asPath));
  }

  private static AsPathContext context(AsPath asPath) {
    return AsPathContext.builder().setInputAsPath(asPath).build();
  }
}
