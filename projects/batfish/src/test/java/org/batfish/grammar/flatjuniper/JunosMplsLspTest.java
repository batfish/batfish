package org.batfish.grammar.flatjuniper;

import static org.batfish.datamodel.matchers.ConvertConfigurationAnswerElementMatchers.hasDefinedStructure;
import static org.batfish.datamodel.matchers.ConvertConfigurationAnswerElementMatchers.hasReferencedStructure;
import static org.batfish.datamodel.matchers.ConvertConfigurationAnswerElementMatchers.hasUndefinedReference;
import static org.batfish.grammar.JunosGrammarTestUtils.getBatfish;
import static org.batfish.representation.juniper.JuniperStructureType.FIREWALL_FILTER;
import static org.batfish.representation.juniper.JuniperStructureType.MPLS_PATH;
import static org.batfish.representation.juniper.JuniperStructureUsage.MPLS_LSP_POLICING_FILTER;
import static org.batfish.representation.juniper.JuniperStructureUsage.MPLS_LSP_PRIMARY_PATH;
import static org.batfish.representation.juniper.JuniperStructureUsage.MPLS_LSP_SECONDARY_PATH;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasKey;

import java.io.IOException;
import org.batfish.datamodel.answers.ConvertConfigurationAnswerElement;
import org.batfish.main.Batfish;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class JunosMplsLspTest {

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  @Test
  public void testMplsLspComprehensiveParsing() throws IOException {
    String hostname = "mpls-lsp-comprehensive";
    Batfish batfish = getBatfish(_folder, hostname);
    // Doesn't crash, produces configuration successfully.
    assertThat(batfish.loadConfigurations(batfish.getSnapshot()), hasKey(hostname));
  }

  @Test
  public void testMplsPathReferences() throws IOException {
    String hostname = "mpls-lsp-comprehensive";
    String filename = "configs/" + hostname;
    Batfish batfish = getBatfish(_folder, hostname);
    batfish.loadConfigurations(batfish.getSnapshot());
    ConvertConfigurationAnswerElement ccae =
        batfish.loadConvertConfigurationAnswerElementOrReparse(batfish.getSnapshot());

    // Verify path definition
    assertThat(ccae, hasDefinedStructure(filename, MPLS_PATH, "PRI"));

    // Verify path references
    assertThat(ccae, hasReferencedStructure(filename, MPLS_PATH, "PRI", MPLS_LSP_PRIMARY_PATH));

    // Verify undefined path reference
    assertThat(ccae, hasUndefinedReference(filename, MPLS_PATH, "SEC", MPLS_LSP_SECONDARY_PATH));

    // LSP policing references a firewall filter.
    assertThat(
        ccae,
        hasReferencedStructure(
            filename, FIREWALL_FILTER, "signaled-lsp-filter", MPLS_LSP_POLICING_FILTER));
  }

  /** Names containing {@code >}, e.g. LSP name "WASH->ATLA", lex so the LSP body parses. */
  @Test
  public void testLspNameWithArrowParsing() throws IOException {
    String hostname = "junos-lsp-name-with-arrow";
    String filename = "configs/" + hostname;
    Batfish batfish = getBatfish(_folder, hostname);
    batfish.loadConfigurations(batfish.getSnapshot());
    ConvertConfigurationAnswerElement ccae =
        batfish.loadConvertConfigurationAnswerElementOrReparse(batfish.getSnapshot());
    assertThat(ccae, hasDefinedStructure(filename, MPLS_PATH, "PRI"));
    assertThat(ccae, hasReferencedStructure(filename, MPLS_PATH, "PRI", MPLS_LSP_PRIMARY_PATH));
  }
}
