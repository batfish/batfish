package org.batfish.grammar.flatjuniper;

import static org.batfish.datamodel.matchers.ConvertConfigurationAnswerElementMatchers.hasReferencedStructure;
import static org.batfish.grammar.JunosGrammarTestUtils.getBatfish;
import static org.batfish.grammar.JunosGrammarTestUtils.getParseWarnings;
import static org.batfish.representation.juniper.JuniperStructureType.ROUTING_INSTANCE;
import static org.batfish.representation.juniper.JuniperStructureUsage.SNMP_COMMUNITY_ROUTING_INSTANCE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;

import java.io.IOException;
import org.batfish.datamodel.answers.ConvertConfigurationAnswerElement;
import org.batfish.main.Batfish;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public final class JunosSnmpRoutingInstancesTest {

  private static final String HOSTNAME = "junos-snmp-routing-instances";

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  @Test
  public void testRoutingInstances() throws IOException {
    Batfish batfish = getBatfish(_folder, HOSTNAME);

    assertThat(getParseWarnings(batfish, HOSTNAME), empty());
    batfish.loadConfigurations(batfish.getSnapshot());
    ConvertConfigurationAnswerElement ccae =
        batfish.loadConvertConfigurationAnswerElementOrReparse(batfish.getSnapshot());
    assertThat(
        ccae,
        hasReferencedStructure(
            "configs/" + HOSTNAME, ROUTING_INSTANCE, "MGMT", SNMP_COMMUNITY_ROUTING_INSTANCE));
  }
}
