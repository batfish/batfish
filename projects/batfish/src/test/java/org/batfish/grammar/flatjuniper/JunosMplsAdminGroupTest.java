package org.batfish.grammar.flatjuniper;

import static org.batfish.datamodel.matchers.DataModelMatchers.hasDefinedStructure;
import static org.batfish.datamodel.matchers.DataModelMatchers.hasDefinedStructureWithDefinitionLines;
import static org.batfish.datamodel.matchers.DataModelMatchers.hasNumReferrers;
import static org.batfish.datamodel.matchers.DataModelMatchers.hasReferencedStructure;
import static org.batfish.datamodel.matchers.DataModelMatchers.hasUndefinedReference;
import static org.batfish.representation.juniper.JuniperStructureType.ADMIN_GROUP;
import static org.batfish.representation.juniper.JuniperStructureUsage.MPLS_INTERFACE_ADMIN_GROUP;
import static org.batfish.representation.juniper.JuniperStructureUsage.MPLS_LSP_ADMIN_GROUP_EXCLUDE;
import static org.batfish.representation.juniper.JuniperStructureUsage.MPLS_LSP_ADMIN_GROUP_INCLUDE_ALL;
import static org.batfish.representation.juniper.JuniperStructureUsage.MPLS_LSP_ADMIN_GROUP_INCLUDE_ANY;
import static org.batfish.representation.juniper.JuniperStructureUsage.MPLS_LSP_SECONDARY_ADMIN_GROUP_EXCLUDE;
import static org.batfish.representation.juniper.JuniperStructureUsage.MPLS_LSP_SECONDARY_ADMIN_GROUP_INCLUDE_ALL;
import static org.batfish.representation.juniper.JuniperStructureUsage.MPLS_LSP_SECONDARY_ADMIN_GROUP_INCLUDE_ANY;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;

import java.io.IOException;
import org.batfish.datamodel.answers.ConvertConfigurationAnswerElement;
import org.batfish.main.Batfish;
import org.batfish.main.BatfishTestUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/** Tests for Juniper MPLS admin-group parsing. */
public final class JunosMplsAdminGroupTest {

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  private Batfish getBatfishForConfigurationNames(String... configurationNames) throws IOException {
    String[] names =
        java.util.Arrays.stream(configurationNames)
            .map(s -> "org/batfish/grammar/juniper/testconfigs/" + s)
            .toArray(String[]::new);
    return BatfishTestUtils.getBatfishForTextConfigs(_folder, names);
  }

  @Test
  public void testAdminGroupDefinitions() throws IOException {
    String hostname = "junos-mpls-admin-groups";
    String filename = "configs/" + hostname;
    Batfish batfish = getBatfishForConfigurationNames(hostname);
    batfish.loadConfigurations(batfish.getSnapshot());
    ConvertConfigurationAnswerElement ccae =
        batfish.loadConvertConfigurationAnswerElementOrReparse(batfish.getSnapshot());

    // Verify admin-group definitions
    assertThat(ccae, hasDefinedStructure(filename, ADMIN_GROUP, "group1"));
    assertThat(ccae, hasDefinedStructure(filename, ADMIN_GROUP, "group2"));
    assertThat(ccae, hasDefinedStructure(filename, ADMIN_GROUP, "group3"));
    assertThat(ccae, hasDefinedStructure(filename, ADMIN_GROUP, "group4"));

    // Verify definition lines
    assertThat(
        ccae, hasDefinedStructureWithDefinitionLines(filename, ADMIN_GROUP, "group1", contains(5)));
    assertThat(
        ccae, hasDefinedStructureWithDefinitionLines(filename, ADMIN_GROUP, "group2", contains(6)));
    assertThat(
        ccae, hasDefinedStructureWithDefinitionLines(filename, ADMIN_GROUP, "group3", contains(7)));
    assertThat(
        ccae, hasDefinedStructureWithDefinitionLines(filename, ADMIN_GROUP, "group4", contains(8)));
  }

  @Test
  public void testAdminGroupReferences() throws IOException {
    String hostname = "junos-mpls-admin-groups";
    String filename = "configs/" + hostname;
    Batfish batfish = getBatfishForConfigurationNames(hostname);
    batfish.loadConfigurations(batfish.getSnapshot());
    ConvertConfigurationAnswerElement ccae =
        batfish.loadConvertConfigurationAnswerElementOrReparse(batfish.getSnapshot());

    // Verify interface references
    assertThat(
        ccae, hasReferencedStructure(filename, ADMIN_GROUP, "group1", MPLS_INTERFACE_ADMIN_GROUP));
    assertThat(
        ccae, hasReferencedStructure(filename, ADMIN_GROUP, "group2", MPLS_INTERFACE_ADMIN_GROUP));

    // Verify LSP exclude references
    assertThat(
        ccae,
        hasReferencedStructure(filename, ADMIN_GROUP, "group1", MPLS_LSP_ADMIN_GROUP_EXCLUDE));

    // Verify LSP include-any references
    assertThat(
        ccae,
        hasReferencedStructure(filename, ADMIN_GROUP, "group1", MPLS_LSP_ADMIN_GROUP_INCLUDE_ANY));
    assertThat(
        ccae,
        hasReferencedStructure(filename, ADMIN_GROUP, "group2", MPLS_LSP_ADMIN_GROUP_INCLUDE_ANY));

    // Verify LSP include-all references
    assertThat(
        ccae,
        hasReferencedStructure(filename, ADMIN_GROUP, "group2", MPLS_LSP_ADMIN_GROUP_INCLUDE_ALL));
    assertThat(
        ccae,
        hasReferencedStructure(filename, ADMIN_GROUP, "group3", MPLS_LSP_ADMIN_GROUP_INCLUDE_ALL));

    // Verify secondary path exclude references
    assertThat(
        ccae,
        hasReferencedStructure(
            filename, ADMIN_GROUP, "group3", MPLS_LSP_SECONDARY_ADMIN_GROUP_EXCLUDE));

    // Verify secondary path include-any references
    assertThat(
        ccae,
        hasReferencedStructure(
            filename, ADMIN_GROUP, "group1", MPLS_LSP_SECONDARY_ADMIN_GROUP_INCLUDE_ANY));
    assertThat(
        ccae,
        hasReferencedStructure(
            filename, ADMIN_GROUP, "group2", MPLS_LSP_SECONDARY_ADMIN_GROUP_INCLUDE_ANY));

    // Verify secondary path include-all references
    assertThat(
        ccae,
        hasReferencedStructure(
            filename, ADMIN_GROUP, "group3", MPLS_LSP_SECONDARY_ADMIN_GROUP_INCLUDE_ALL));
    assertThat(
        ccae,
        hasReferencedStructure(
            filename, ADMIN_GROUP, "group4", MPLS_LSP_SECONDARY_ADMIN_GROUP_INCLUDE_ALL));
  }

  @Test
  public void testAdminGroupReferenceCount() throws IOException {
    String hostname = "junos-mpls-admin-groups";
    String filename = "configs/" + hostname;
    Batfish batfish = getBatfishForConfigurationNames(hostname);
    batfish.loadConfigurations(batfish.getSnapshot());
    ConvertConfigurationAnswerElement ccae =
        batfish.loadConvertConfigurationAnswerElementOrReparse(batfish.getSnapshot());

    // Verify reference counts
    assertThat(
        ccae,
        hasNumReferrers(
            filename,
            ADMIN_GROUP,
            "group1",
            4)); // 1 interface + 1 exclude + 1 include-any + 1 secondary include-any
    assertThat(
        ccae,
        hasNumReferrers(
            filename,
            ADMIN_GROUP,
            "group2",
            4)); // 1 interface + 1 include-any + 1 include-all + 1 secondary include-any
    assertThat(
        ccae,
        hasNumReferrers(
            filename, ADMIN_GROUP, "group3", 3)); // 1 include-all + 1 exclude + 1 include-all
    assertThat(ccae, hasNumReferrers(filename, ADMIN_GROUP, "group4", 1)); // 1 include-all
  }

  @Test
  public void testUndefinedReferences() throws IOException {
    String hostname = "junos-mpls-admin-groups";
    String filename = "configs/" + hostname;
    Batfish batfish = getBatfishForConfigurationNames(hostname);
    batfish.loadConfigurations(batfish.getSnapshot());
    ConvertConfigurationAnswerElement ccae =
        batfish.loadConvertConfigurationAnswerElementOrReparse(batfish.getSnapshot());

    // Verify undefined references are properly detected
    assertThat(
        ccae,
        hasUndefinedReference(
            filename, ADMIN_GROUP, "undefined-group", MPLS_INTERFACE_ADMIN_GROUP));
    assertThat(
        ccae,
        hasUndefinedReference(
            filename, ADMIN_GROUP, "undefined-group", MPLS_LSP_ADMIN_GROUP_EXCLUDE));
  }
}
