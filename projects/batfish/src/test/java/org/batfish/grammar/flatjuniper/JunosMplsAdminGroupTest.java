package org.batfish.grammar.flatjuniper;

import static org.batfish.datamodel.matchers.ConvertConfigurationAnswerElementMatchers.hasDefinedStructure;
import static org.batfish.datamodel.matchers.ConvertConfigurationAnswerElementMatchers.hasDefinedStructureWithDefinitionLines;
import static org.batfish.datamodel.matchers.ConvertConfigurationAnswerElementMatchers.hasNumReferrers;
import static org.batfish.datamodel.matchers.ConvertConfigurationAnswerElementMatchers.hasReferencedStructure;
import static org.batfish.datamodel.matchers.ConvertConfigurationAnswerElementMatchers.hasUndefinedReference;
import static org.batfish.grammar.JunosGrammarTestUtils.getBatfish;
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

import java.io.File;
import java.io.IOException;
import org.batfish.datamodel.answers.ConvertConfigurationAnswerElement;
import org.batfish.main.Batfish;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Tests for Juniper MPLS admin-group parsing. */
final class JunosMplsAdminGroupTest {

  @TempDir public File _folder;

  @Test
  void testAdminGroupDefinitions() throws IOException {
    String hostname = "junos-mpls-admin-groups";
    String filename = "configs/" + hostname;
    Batfish batfish = getBatfish(_folder, hostname);
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
  void testAdminGroupReferences() throws IOException {
    String hostname = "junos-mpls-admin-groups";
    String filename = "configs/" + hostname;
    Batfish batfish = getBatfish(_folder, hostname);
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
  void testAdminGroupReferenceCount() throws IOException {
    String hostname = "junos-mpls-admin-groups";
    String filename = "configs/" + hostname;
    Batfish batfish = getBatfish(_folder, hostname);
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
  void testUndefinedReferences() throws IOException {
    String hostname = "junos-mpls-admin-groups";
    String filename = "configs/" + hostname;
    Batfish batfish = getBatfish(_folder, hostname);
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
