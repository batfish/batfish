package org.batfish.question.specifiers;

import static org.batfish.question.specifiers.DispositionSpecifier.failureDispositions;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableSet;
import java.util.Collections;
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
    assertThat(
        DispositionSpecifier.create(ImmutableSet.of()).getDispositions(),
        equalTo(ImmutableSet.of(FlowDisposition.ACCEPTED)));
  }

  @Test
  public void testSuccess() {
    assertThat(
        DispositionSpecifier.create(Collections.singleton("success")).getDispositions(),
        equalTo(ImmutableSet.of(FlowDisposition.ACCEPTED)));
  }

  @Test
  public void testFailure() {
    assertThat(
        DispositionSpecifier.create(Collections.singleton("FAILURE")).getDispositions(),
        equalTo(failureDispositions));
  }
}
