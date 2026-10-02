package org.batfish.grammar.flatjuniper;

import static org.batfish.common.matchers.ParseWarningMatchers.isTodo;
import static org.batfish.datamodel.matchers.ConvertConfigurationAnswerElementMatchers.hasDefinedStructure;
import static org.batfish.datamodel.matchers.ConvertConfigurationAnswerElementMatchers.hasReferencedStructure;
import static org.batfish.grammar.JunosGrammarTestUtils.getBatfish;
import static org.batfish.grammar.JunosGrammarTestUtils.getParseWarnings;
import static org.batfish.grammar.JunosGrammarTestUtils.getVendorConfiguration;
import static org.batfish.representation.juniper.JuniperStructureType.AUTHENTICATION_KEY_CHAIN;
import static org.batfish.representation.juniper.JuniperStructureType.INTERFACE;
import static org.batfish.representation.juniper.JuniperStructureType.MACSEC_CONNECTIVITY_ASSOCIATION;
import static org.batfish.representation.juniper.JuniperStructureUsage.INTERFACE_MACSEC_CONNECTIVITY_ASSOCIATION;
import static org.batfish.representation.juniper.JuniperStructureUsage.MACSEC_INTERFACE;
import static org.batfish.representation.juniper.JuniperStructureUsage.MACSEC_INTERFACE_CONNECTIVITY_ASSOCIATION;
import static org.batfish.representation.juniper.JuniperStructureUsage.MACSEC_PRE_SHARED_KEY_CHAIN;
import static org.batfish.representation.juniper.MacsecConnectivityAssociation.MkaSecurityMode.MUST_SECURE;
import static org.batfish.representation.juniper.MacsecConnectivityAssociation.MkaSecurityMode.SHOULD_SECURE;
import static org.batfish.representation.juniper.MacsecConnectivityAssociation.SecurityMode.DYNAMIC;
import static org.batfish.representation.juniper.MacsecConnectivityAssociation.SecurityMode.STATIC_CONNECTIVITY_ASSOCIATION_KEY;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.matchesPattern;
import static org.hamcrest.Matchers.nullValue;

import java.io.IOException;
import java.util.Map;
import org.batfish.common.Warnings;
import org.batfish.datamodel.answers.ConvertConfigurationAnswerElement;
import org.batfish.main.Batfish;
import org.batfish.representation.juniper.Interface;
import org.batfish.representation.juniper.LogicalSystem;
import org.batfish.representation.juniper.MacsecConnectivityAssociation;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public final class JunosMacsecConnectivityAssociationTest {

  private static final String CONFIG_NAME = "macsec-connectivity-associations";

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  @Test
  public void testExtractionAndWarnings() throws IOException {
    Batfish batfish = getBatfish(_folder, CONFIG_NAME);
    LogicalSystem logicalSystem =
        getVendorConfiguration(batfish, CONFIG_NAME).getMasterLogicalSystem();
    Map<String, MacsecConnectivityAssociation> associations =
        logicalSystem.getMacsecConnectivityAssociations();

    assertThat(
        associations.keySet(), containsInAnyOrder("MODERN", "LEGACY", "SECURITY-LEGACY", "XPN"));
    MacsecConnectivityAssociation modern = associations.get("MODERN");
    assertThat(modern.getName(), equalTo("MODERN"));
    assertThat(modern.getCakHash(), matchesPattern("[0-9a-f]{64}"));
    assertThat(modern.getCipherSuite(), equalTo("gcm-aes-256"));
    assertThat(modern.getCkn(), equalTo("00112233445566778899"));
    assertThat(modern.getIncludeSci(), equalTo(true));
    assertThat(modern.getKeyServerPriority(), equalTo(16));
    assertThat(modern.getMkaSecurityMode(), equalTo(MUST_SECURE));
    assertThat(modern.getPreSharedKeyCakHash(), matchesPattern("[0-9a-f]{64}"));
    assertThat(modern.getPreSharedKeyCkn(), equalTo("99887766554433221100"));
    assertThat(modern.getPreSharedKeyChain(), equalTo("MACSEC-KEYS"));
    assertThat(modern.getSecurityMode(), equalTo(STATIC_CONNECTIVITY_ASSOCIATION_KEY));
    assertThat(associations.get("LEGACY").getCipherSuite(), equalTo("gcm-aes-128"));
    assertThat(associations.get("LEGACY").getMkaSecurityMode(), equalTo(SHOULD_SECURE));
    assertThat(associations.get("LEGACY").getSecurityMode(), equalTo(DYNAMIC));
    assertThat(associations.get("SECURITY-LEGACY").getCakHash(), matchesPattern("[0-9a-f]{64}"));
    assertThat(associations.get("XPN").getCipherSuite(), equalTo("gcm-aes-xpn-256"));

    Map<String, Interface> interfaces = logicalSystem.getInterfaces();
    assertThat(
        logicalSystem.getMacsecInterfaceConnectivityAssociations(), hasEntry("et-0/0/0", "MODERN"));
    assertThat(interfaces.get("et-0/0/0").getMacsecConnectivityAssociation(), nullValue());
    assertThat(interfaces.get("et-0/0/1").getMacsecConnectivityAssociation(), equalTo("LEGACY"));
    assertThat(
        interfaces.get("et-0/0/2").getMacsecConnectivityAssociation(), equalTo("SECURITY-LEGACY"));

    assertThat(
        getParseWarnings(batfish, CONFIG_NAME),
        containsInAnyOrder(
            isTodo("cak <SCRUBBED>"),
            isTodo("cipher-suite gcm-aes-256"),
            isTodo("ckn 00112233445566778899"),
            isTodo("include-sci"),
            isTodo("mka key-server-priority 16"),
            isTodo("mka must-secure"),
            isTodo("pre-shared-key cak <SCRUBBED>"),
            isTodo("pre-shared-key ckn 99887766554433221100"),
            isTodo("pre-shared-key-chain MACSEC-KEYS"),
            isTodo("security-mode static-cak"),
            isTodo("interfaces et-0/0/0 connectivity-association MODERN"),
            isTodo("cipher-suite gcm-aes-128"),
            isTodo("mka should-secure"),
            isTodo("security-mode dynamic"),
            isTodo("connectivity-association LEGACY"),
            isTodo("cak <SCRUBBED>"),
            isTodo("connectivity-association SECURITY-LEGACY"),
            isTodo("cipher-suite gcm-aes-xpn-128"),
            isTodo("cipher-suite gcm-aes-xpn-256")));

    batfish.loadConfigurations(batfish.getSnapshot());
    ConvertConfigurationAnswerElement ccae =
        batfish.loadConvertConfigurationAnswerElementOrReparse(batfish.getSnapshot());
    String filename = "configs/" + CONFIG_NAME;
    assertThat(ccae, hasDefinedStructure(filename, MACSEC_CONNECTIVITY_ASSOCIATION, "MODERN"));
    assertThat(ccae, hasDefinedStructure(filename, MACSEC_CONNECTIVITY_ASSOCIATION, "LEGACY"));
    assertThat(
        ccae, hasDefinedStructure(filename, MACSEC_CONNECTIVITY_ASSOCIATION, "SECURITY-LEGACY"));
    assertThat(
        ccae,
        hasReferencedStructure(
            filename, AUTHENTICATION_KEY_CHAIN, "MACSEC-KEYS", MACSEC_PRE_SHARED_KEY_CHAIN));
    assertThat(ccae, hasReferencedStructure(filename, INTERFACE, "et-0/0/0", MACSEC_INTERFACE));
    assertThat(
        ccae,
        hasReferencedStructure(
            filename,
            MACSEC_CONNECTIVITY_ASSOCIATION,
            "MODERN",
            MACSEC_INTERFACE_CONNECTIVITY_ASSOCIATION));
    assertThat(
        ccae,
        hasReferencedStructure(
            filename,
            MACSEC_CONNECTIVITY_ASSOCIATION,
            "LEGACY",
            INTERFACE_MACSEC_CONNECTIVITY_ASSOCIATION));
    assertThat(
        ccae,
        hasReferencedStructure(
            filename,
            MACSEC_CONNECTIVITY_ASSOCIATION,
            "SECURITY-LEGACY",
            INTERFACE_MACSEC_CONNECTIVITY_ASSOCIATION));
    Warnings warnings = ccae.getWarnings().getOrDefault(CONFIG_NAME, new Warnings());
    assertThat(warnings.getRedFlagWarnings(), empty());
    assertThat(warnings.getUnimplementedWarnings(), empty());
  }
}
