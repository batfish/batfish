package org.batfish.grammar.flatjuniper;

import static org.batfish.datamodel.matchers.DataModelMatchers.hasDefinedStructure;
import static org.batfish.datamodel.matchers.DataModelMatchers.hasReferencedStructure;
import static org.batfish.datamodel.matchers.DataModelMatchers.hasUndefinedReference;
import static org.batfish.representation.juniper.JuniperStructureType.MPLS_PATH;
import static org.batfish.representation.juniper.JuniperStructureUsage.MPLS_LSP_PRIMARY_PATH;
import static org.batfish.representation.juniper.JuniperStructureUsage.MPLS_LSP_SECONDARY_PATH;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasKey;

import java.io.IOException;
import org.batfish.datamodel.answers.ConvertConfigurationAnswerElement;
import org.batfish.main.Batfish;
import org.batfish.main.BatfishTestUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class JunosMplsLspTest {

  private static final String TESTCONFIGS_PREFIX = "org/batfish/grammar/juniper/testconfigs/";

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  private Batfish getBatfishForConfigurationNames(String... configurationNames) throws IOException {
    String[] names =
        java.util.Arrays.stream(configurationNames)
            .map(s -> TESTCONFIGS_PREFIX + s)
            .toArray(String[]::new);
    return BatfishTestUtils.getBatfishForTextConfigs(_folder, names);
  }

  @Test
  public void testMplsLspComprehensiveParsing() throws IOException {
    String hostname = "mpls-lsp-comprehensive";
    Batfish batfish = getBatfishForConfigurationNames(hostname);
    // Doesn't crash, produces configuration successfully.
    assertThat(batfish.loadConfigurations(batfish.getSnapshot()), hasKey(hostname));
  }

  @Test
  public void testMplsPathReferences() throws IOException {
    String hostname = "mpls-lsp-comprehensive";
    String filename = "configs/" + hostname;
    Batfish batfish = getBatfishForConfigurationNames(hostname);
    batfish.loadConfigurations(batfish.getSnapshot());
    ConvertConfigurationAnswerElement ccae =
        batfish.loadConvertConfigurationAnswerElementOrReparse(batfish.getSnapshot());

    // Verify path definition
    assertThat(ccae, hasDefinedStructure(filename, MPLS_PATH, "PRI"));

    // Verify path references
    assertThat(ccae, hasReferencedStructure(filename, MPLS_PATH, "PRI", MPLS_LSP_PRIMARY_PATH));

    // Verify undefined path reference
    assertThat(ccae, hasUndefinedReference(filename, MPLS_PATH, "SEC", MPLS_LSP_SECONDARY_PATH));
  }
}
