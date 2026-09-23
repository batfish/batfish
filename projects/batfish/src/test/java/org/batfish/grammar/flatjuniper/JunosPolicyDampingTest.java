package org.batfish.grammar.flatjuniper;

import static org.batfish.common.matchers.ParseWarningMatchers.hasComment;
import static org.batfish.datamodel.matchers.ConvertConfigurationAnswerElementMatchers.hasNumReferrers;
import static org.batfish.datamodel.matchers.ConvertConfigurationAnswerElementMatchers.hasUndefinedReference;
import static org.batfish.grammar.JunosGrammarTestUtils.getBatfish;
import static org.batfish.grammar.JunosGrammarTestUtils.getParseWarnings;
import static org.batfish.grammar.JunosGrammarTestUtils.getVendorConfiguration;
import static org.batfish.representation.juniper.JuniperStructureType.DAMPING_PROFILE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;

import java.io.IOException;
import org.batfish.common.Warnings;
import org.batfish.datamodel.answers.ConvertConfigurationAnswerElement;
import org.batfish.main.Batfish;
import org.batfish.representation.juniper.DampingProfile;
import org.batfish.representation.juniper.JuniperConfiguration;
import org.batfish.representation.juniper.PsThenDamping;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public final class JunosPolicyDampingTest {

  private static final String POLICY_DAMPING = "policy-options-damping";
  private static final String ROUTE_FILTER_DAMPING = "policy-route-filter-damping";
  private static final String THEN_DAMPING = "policy-statement-then-damping";

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  @Test
  public void testProfileExtractionAndReferences() throws IOException {
    Batfish batfish = getBatfish(_folder, POLICY_DAMPING);
    JuniperConfiguration configuration = getVendorConfiguration(batfish, POLICY_DAMPING);

    DampingProfile conservative =
        configuration.getMasterLogicalSystem().getDampingProfiles().get("CONSERVATIVE");
    assertThat(conservative.getDisabled(), equalTo(false));
    assertThat(conservative.getHalfLife(), equalTo(10));
    assertThat(conservative.getMaxSuppress(), equalTo(60));
    assertThat(conservative.getReuse(), equalTo(750));
    assertThat(conservative.getSuppress(), equalTo(10000));
    assertThat(
        configuration.getMasterLogicalSystem().getDampingProfiles().get("NONE").getDisabled(),
        equalTo(true));
    DampingProfile invalid =
        configuration.getMasterLogicalSystem().getDampingProfiles().get("INVALID");
    assertThat(invalid.getHalfLife(), nullValue());
    assertThat(invalid.getMaxSuppress(), nullValue());
    assertThat(invalid.getReuse(), nullValue());
    assertThat(invalid.getSuppress(), nullValue());
    assertThat(
        configuration
            .getMasterLogicalSystem()
            .getPolicyStatements()
            .get("DAMPING-POLICY")
            .getTerms()
            .get("PROFILE")
            .getThens()
            .getAllThens(),
        contains(new PsThenDamping("CONSERVATIVE")));
    assertThat(
        getParseWarnings(batfish, POLICY_DAMPING),
        containsInAnyOrder(
            hasComment("Expected damping half-life in range 1-45, but got '0'"),
            hasComment("Expected damping max-suppress in range 1-720, but got '721'"),
            hasComment("Expected damping reuse in range 1-20000, but got '0'"),
            hasComment("Expected damping suppress in range 1-20000, but got '20001'")));

    ConvertConfigurationAnswerElement ccae =
        batfish.loadConvertConfigurationAnswerElementOrReparse(batfish.getSnapshot());
    String filename = "configs/" + POLICY_DAMPING;
    assertThat(ccae, hasNumReferrers(filename, DAMPING_PROFILE, "CONSERVATIVE", 1));
    assertThat(ccae, hasNumReferrers(filename, DAMPING_PROFILE, "NONE", 0));
    assertThat(ccae, hasUndefinedReference(filename, DAMPING_PROFILE, "UNDEFINED"));
    assertThat(
        ccae.getWarnings().getOrDefault(POLICY_DAMPING, new Warnings()).getRedFlagWarnings(),
        empty());
  }

  @Test
  public void testRouteFilterAction() throws IOException {
    Batfish batfish = getBatfish(_folder, ROUTE_FILTER_DAMPING);
    batfish.loadConfigurations(batfish.getSnapshot());
    ConvertConfigurationAnswerElement ccae =
        batfish.loadConvertConfigurationAnswerElementOrReparse(batfish.getSnapshot());

    assertThat(getParseWarnings(batfish, ROUTE_FILTER_DAMPING), empty());
    assertThat(
        ccae, hasNumReferrers("configs/" + ROUTE_FILTER_DAMPING, DAMPING_PROFILE, "TIMID", 1));
    assertThat(
        ccae.getWarnings().getOrDefault(ROUTE_FILTER_DAMPING, new Warnings()).getRedFlagWarnings(),
        empty());
  }

  @Test
  public void testNoneAction() throws IOException {
    Batfish batfish = getBatfish(_folder, THEN_DAMPING);
    JuniperConfiguration configuration = getVendorConfiguration(batfish, THEN_DAMPING);

    assertThat(
        configuration
            .getMasterLogicalSystem()
            .getPolicyStatements()
            .get("DAMPING-POLICY")
            .getTerms()
            .get("NONE")
            .getThens()
            .getAllThens(),
        contains(new PsThenDamping("none")));
    assertThat(getParseWarnings(batfish, THEN_DAMPING), empty());

    batfish.loadConfigurations(batfish.getSnapshot());
    ConvertConfigurationAnswerElement ccae =
        batfish.loadConvertConfigurationAnswerElementOrReparse(batfish.getSnapshot());
    assertThat(
        ccae.getWarnings().getOrDefault(THEN_DAMPING, new Warnings()).getRedFlagWarnings(),
        empty());
  }
}
