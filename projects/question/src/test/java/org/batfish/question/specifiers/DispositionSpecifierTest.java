package org.batfish.question.specifiers;

import static org.batfish.question.specifiers.DispositionSpecifier.FAILURE_DISPOSITIONS;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import java.util.Set;
import org.batfish.datamodel.FlowDisposition;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests of {@link DispositionSpecifier} */
@RunWith(JUnit4.class)
public class DispositionSpecifierTest {
  @Test
  public void testCreationDefault() {
    assertThat(
        DispositionSpecifier.create(null).getDispositions(),
        equalTo(ImmutableSet.of(FlowDisposition.ACCEPTED)));
  }

  @Test
  public void testCaseInsensitive() {
    assertThat(
        DispositionSpecifier.create("SUCCESS").getDispositions(),
        equalTo(DispositionSpecifier.create("suCCess").getDispositions()));
  }

  @Test
  public void testSupportsPrimitiveDispositions() {
    assertThat(
        DispositionSpecifier.create("no_route").getDispositions(),
        equalTo(ImmutableSet.of(FlowDisposition.NO_ROUTE)));
  }

  @Test
  public void testSuccess() {
    assertThat(
        DispositionSpecifier.create("success").getDispositions(),
        equalTo(ImmutableSet.of(FlowDisposition.ACCEPTED)));
  }

  @Test
  public void testFailure() {
    assertThat(
        DispositionSpecifier.create("FAILURE").getDispositions(), equalTo(FAILURE_DISPOSITIONS));
  }

  @Test
  public void testSuccessDisjointFromFailure() {
    Set<FlowDisposition> success = DispositionSpecifier.create("SUCCESS").getDispositions();
    Set<FlowDisposition> failure = DispositionSpecifier.create("FAILURE").getDispositions();
    assertThat(
        "No overlap between success and failure dispositions",
        Sets.intersection(success, failure).isEmpty());
  }

  @Test
  public void testUnionEqualsCardinality() {
    assertThat(
        DispositionSpecifier.create("success,failure").getDispositions(),
        equalTo(ImmutableSet.copyOf(FlowDisposition.values())));
  }
}
