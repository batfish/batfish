package org.batfish.specifier;

import static org.batfish.datamodel.FlowDisposition.DELIVERED_TO_SUBNET;
import static org.batfish.datamodel.FlowDisposition.EXITS_NETWORK;
import static org.batfish.datamodel.FlowDisposition.INSUFFICIENT_INFO;
import static org.batfish.specifier.DispositionSpecifier.FAILURE;
import static org.batfish.specifier.DispositionSpecifier.FAILURE_SPECIFIER;
import static org.batfish.specifier.DispositionSpecifier.SUCCESS;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import java.io.IOException;
import java.util.Set;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.datamodel.FlowDisposition;
import org.batfish.datamodel.answers.AutocompleteSuggestion;
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
        equalTo(
            ImmutableSet.of(
                FlowDisposition.ACCEPTED,
                FlowDisposition.DELIVERED_TO_SUBNET,
                FlowDisposition.EXITS_NETWORK)));
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
  public void testTrimming() {
    assertThat(
        DispositionSpecifier.create("no_route  ,       null_routed").getDispositions(),
        equalTo(ImmutableSet.of(FlowDisposition.NO_ROUTE, FlowDisposition.NULL_ROUTED)));
  }

  @Test
  public void testSuccess() {
    assertThat(
        DispositionSpecifier.create("success").getDispositions(),
        equalTo(
            ImmutableSet.of(
                FlowDisposition.ACCEPTED,
                FlowDisposition.DELIVERED_TO_SUBNET,
                FlowDisposition.EXITS_NETWORK)));
  }

  @Test
  public void testFailure() {
    assertThat(
        DispositionSpecifier.create("FAILURE").getDispositions(),
        equalTo(FAILURE_SPECIFIER.getDispositions()));
  }

  @Test
  public void testSuccessDisjointFromFailure() {
    Set<FlowDisposition> success = DispositionSpecifier.create("SUCCESS").getDispositions();
    Set<FlowDisposition> failure = DispositionSpecifier.create("FAILURE").getDispositions();
    assertThat(
        "No overlap between success and failure dispositions",
        Sets.intersection(success, failure),
        empty());
  }

  @Test
  public void testAllDispositionsCovered() {
    assertThat(
        DispositionSpecifier.create("success,failure").getDispositions(),
        equalTo(Sets.difference(ImmutableSet.copyOf(FlowDisposition.values()), ImmutableSet.of())));
  }

  @Test
  public void serializationCheck() throws IOException {
    String serialized = BatfishObjectMapper.writePrettyString(FAILURE_SPECIFIER);
    assertThat(
        BatfishObjectMapper.mapper().readValue(serialized, DispositionSpecifier.class),
        equalTo(FAILURE_SPECIFIER));
  }

  @Test
  public void testAutocomplete() {
    // should include values from FlowDisposition enum as well as "success" and "failure"
    assertThat(
        DispositionSpecifier.autoComplete("s").stream()
            .map(AutocompleteSuggestion::getText)
            .collect(ImmutableSet.toImmutableSet()),
        equalTo(
            ImmutableSet.of(
                SUCCESS,
                INSUFFICIENT_INFO.name().toLowerCase(),
                DELIVERED_TO_SUBNET.name().toLowerCase(),
                EXITS_NETWORK.name().toLowerCase())));

    assertThat(
        DispositionSpecifier.autoComplete("f").stream()
            .map(AutocompleteSuggestion::getText)
            .collect(ImmutableSet.toImmutableSet()),
        equalTo(ImmutableSet.of(FAILURE, INSUFFICIENT_INFO.name().toLowerCase())));
  }
}
