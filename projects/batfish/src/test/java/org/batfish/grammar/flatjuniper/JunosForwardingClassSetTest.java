package org.batfish.grammar.flatjuniper;

import static org.batfish.common.matchers.ParseWarningMatchers.isTodo;
import static org.batfish.datamodel.matchers.ConvertConfigurationAnswerElementMatchers.hasDefinedStructure;
import static org.batfish.datamodel.matchers.ConvertConfigurationAnswerElementMatchers.hasReferencedStructure;
import static org.batfish.grammar.JunosGrammarTestUtils.getBatfish;
import static org.batfish.grammar.JunosGrammarTestUtils.getParseWarnings;
import static org.batfish.grammar.JunosGrammarTestUtils.getVendorConfiguration;
import static org.batfish.representation.juniper.JuniperStructureType.CLASS_OF_SERVICE_FORWARDING_CLASS;
import static org.batfish.representation.juniper.JuniperStructureType.CLASS_OF_SERVICE_FORWARDING_CLASS_SET;
import static org.batfish.representation.juniper.JuniperStructureUsage.CLASS_OF_SERVICE_FORWARDING_CLASS_SETS_CLASS;
import static org.batfish.representation.juniper.JuniperStructureUsage.CLASS_OF_SERVICE_INTERFACES_FORWARDING_CLASS_SET;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;

import java.io.IOException;
import org.batfish.common.Warnings;
import org.batfish.datamodel.answers.ConvertConfigurationAnswerElement;
import org.batfish.main.Batfish;
import org.batfish.representation.juniper.ForwardingClassSet;
import org.batfish.representation.juniper.JuniperConfiguration;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public final class JunosForwardingClassSetTest {

  private static final String HOSTNAME = "forwarding-class-sets";

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  @Test
  public void testForwardingClassSets() throws IOException {
    Batfish batfish = getBatfish(_folder, HOSTNAME);
    JuniperConfiguration configuration = getVendorConfiguration(batfish, HOSTNAME);
    ForwardingClassSet priority =
        configuration.getMasterLogicalSystem().getForwardingClassSets().get("PRIORITY");

    assertThat(priority.getForwardingClasses(), containsInAnyOrder("HIGH", "network-control"));
    assertThat(
        getParseWarnings(batfish, HOSTNAME),
        contains(isTodo("forwarding-class-set PRIORITY output-traffic-control-profile PROFILE")));

    batfish.loadConfigurations(batfish.getSnapshot());
    ConvertConfigurationAnswerElement ccae =
        batfish.loadConvertConfigurationAnswerElementOrReparse(batfish.getSnapshot());
    String filename = "configs/" + HOSTNAME;
    assertThat(
        ccae, hasDefinedStructure(filename, CLASS_OF_SERVICE_FORWARDING_CLASS_SET, "PRIORITY"));
    assertThat(
        ccae,
        hasReferencedStructure(
            filename,
            CLASS_OF_SERVICE_FORWARDING_CLASS,
            "HIGH",
            CLASS_OF_SERVICE_FORWARDING_CLASS_SETS_CLASS));
    assertThat(
        ccae,
        hasReferencedStructure(
            filename,
            CLASS_OF_SERVICE_FORWARDING_CLASS_SET,
            "PRIORITY",
            CLASS_OF_SERVICE_INTERFACES_FORWARDING_CLASS_SET));
    assertThat(
        ccae.getWarnings().getOrDefault(HOSTNAME, new Warnings()).getRedFlagWarnings(), empty());
  }
}
