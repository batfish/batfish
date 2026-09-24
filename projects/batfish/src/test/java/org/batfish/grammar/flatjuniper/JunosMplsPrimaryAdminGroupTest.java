package org.batfish.grammar.flatjuniper;

import static org.batfish.common.matchers.ParseWarningMatchers.isTodo;
import static org.batfish.datamodel.matchers.ConvertConfigurationAnswerElementMatchers.hasReferencedStructure;
import static org.batfish.grammar.JunosGrammarTestUtils.getBatfish;
import static org.batfish.grammar.JunosGrammarTestUtils.getParseWarnings;
import static org.batfish.representation.juniper.JuniperStructureType.ADMIN_GROUP;
import static org.batfish.representation.juniper.JuniperStructureUsage.MPLS_LSP_PRIMARY_ADMIN_GROUP_EXCLUDE;
import static org.batfish.representation.juniper.JuniperStructureUsage.MPLS_LSP_PRIMARY_ADMIN_GROUP_INCLUDE_ALL;
import static org.batfish.representation.juniper.JuniperStructureUsage.MPLS_LSP_PRIMARY_ADMIN_GROUP_INCLUDE_ANY;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;

import java.io.IOException;
import org.batfish.common.Warnings;
import org.batfish.datamodel.answers.ConvertConfigurationAnswerElement;
import org.batfish.main.Batfish;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public final class JunosMplsPrimaryAdminGroupTest {

  private static final String HOSTNAME = "mpls-primary-admin-groups";

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  @Test
  public void testReferencesAndWarnings() throws IOException {
    Batfish batfish = getBatfish(_folder, HOSTNAME);

    assertThat(
        getParseWarnings(batfish, HOSTNAME),
        contains(
            isTodo("primary PATH2 admin-group exclude [ AVOID ALSO-AVOID ]"),
            isTodo("primary PATH1 admin-group include-all REQUIRED"),
            isTodo("primary PATH1 admin-group include-any PREFERRED")));

    batfish.loadConfigurations(batfish.getSnapshot());
    ConvertConfigurationAnswerElement ccae =
        batfish.loadConvertConfigurationAnswerElementOrReparse(batfish.getSnapshot());
    String filename = "configs/" + HOSTNAME;
    assertThat(
        ccae,
        hasReferencedStructure(
            filename, ADMIN_GROUP, "AVOID", MPLS_LSP_PRIMARY_ADMIN_GROUP_EXCLUDE));
    assertThat(
        ccae,
        hasReferencedStructure(
            filename, ADMIN_GROUP, "ALSO-AVOID", MPLS_LSP_PRIMARY_ADMIN_GROUP_EXCLUDE));
    assertThat(
        ccae,
        hasReferencedStructure(
            filename, ADMIN_GROUP, "REQUIRED", MPLS_LSP_PRIMARY_ADMIN_GROUP_INCLUDE_ALL));
    assertThat(
        ccae,
        hasReferencedStructure(
            filename, ADMIN_GROUP, "PREFERRED", MPLS_LSP_PRIMARY_ADMIN_GROUP_INCLUDE_ANY));
    assertThat(
        ccae.getWarnings().getOrDefault(HOSTNAME, new Warnings()).getRedFlagWarnings(), empty());
  }
}
