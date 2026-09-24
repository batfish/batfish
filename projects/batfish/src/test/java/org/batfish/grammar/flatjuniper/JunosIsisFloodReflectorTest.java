package org.batfish.grammar.flatjuniper;

import static org.batfish.common.matchers.ParseWarningMatchers.hasComment;
import static org.batfish.common.matchers.ParseWarningMatchers.isTodo;
import static org.batfish.grammar.JunosGrammarTestUtils.getBatfish;
import static org.batfish.grammar.JunosGrammarTestUtils.getParseWarnings;
import static org.batfish.grammar.JunosGrammarTestUtils.getVendorConfiguration;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;

import java.io.IOException;
import org.batfish.common.Warnings;
import org.batfish.datamodel.answers.ConvertConfigurationAnswerElement;
import org.batfish.main.Batfish;
import org.batfish.representation.juniper.IsisFloodReflector;
import org.batfish.representation.juniper.IsisFloodReflector.Role;
import org.batfish.representation.juniper.JuniperConfiguration;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public final class JunosIsisFloodReflectorTest {

  private static final String HOSTNAME = "junos-isis-flood-reflector";

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  @Test
  public void testFloodReflector() throws IOException {
    Batfish batfish = getBatfish(_folder, HOSTNAME);
    JuniperConfiguration jc = getVendorConfiguration(batfish, HOSTNAME);
    IsisFloodReflector client =
        jc.getMasterLogicalSystem()
            .getDefaultRoutingInstance()
            .getIsisSettings()
            .getLevel2Settings()
            .getFloodReflector();
    IsisFloodReflector reflector =
        jc.getMasterLogicalSystem()
            .getRoutingInstances()
            .get("reflector")
            .getIsisSettings()
            .getLevel2Settings()
            .getFloodReflector();
    IsisFloodReflector interfaceReflector =
        jc.getMasterLogicalSystem()
            .getInterfaces()
            .get("fti0")
            .getUnits()
            .get("fti0.1")
            .getIsisSettings()
            .getLevel2Settings()
            .getFloodReflector();
    IsisFloodReflector invalid =
        jc.getMasterLogicalSystem()
            .getRoutingInstances()
            .get("invalid")
            .getIsisSettings()
            .getLevel2Settings()
            .getFloodReflector();

    assertThat(client.getRole(), equalTo(Role.CLIENT));
    assertThat(client.getClusterId(), nullValue());
    assertThat(reflector.getRole(), equalTo(Role.REFLECTOR));
    assertThat(reflector.getClusterId(), equalTo(200L));
    assertThat(interfaceReflector.getRole(), nullValue());
    assertThat(interfaceReflector.getClusterId(), equalTo(100L));
    assertThat(invalid, nullValue());
    assertThat(getParseWarnings(batfish, HOSTNAME), hasSize(4));
    assertThat(
        getParseWarnings(batfish, HOSTNAME),
        hasItems(
            isTodo("flood-reflector client"),
            isTodo("flood-reflector cluster-id 100"),
            isTodo("flood-reflector reflector cluster-id 200"),
            hasComment("Expected IS-IS cluster ID in range 1-4294967295, but got '0'")));
    batfish.loadConfigurations(batfish.getSnapshot());
    ConvertConfigurationAnswerElement ccae =
        batfish.loadConvertConfigurationAnswerElementOrReparse(batfish.getSnapshot());
    assertThat(
        ccae.getWarnings().getOrDefault("configs/" + HOSTNAME, new Warnings()).getRedFlagWarnings(),
        empty());
  }
}
