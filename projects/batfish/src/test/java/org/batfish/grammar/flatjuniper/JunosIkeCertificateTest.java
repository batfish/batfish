package org.batfish.grammar.flatjuniper;

import static org.batfish.common.matchers.ParseWarningMatchers.isTodo;
import static org.batfish.datamodel.IkeKeyType.RSA_PUB_KEY;
import static org.batfish.datamodel.matchers.ConvertConfigurationAnswerElementMatchers.hasDefinedStructure;
import static org.batfish.datamodel.matchers.ConvertConfigurationAnswerElementMatchers.hasReferencedStructure;
import static org.batfish.grammar.JunosGrammarTestUtils.getBatfish;
import static org.batfish.grammar.JunosGrammarTestUtils.getParseWarnings;
import static org.batfish.grammar.JunosGrammarTestUtils.getVendorConfiguration;
import static org.batfish.representation.juniper.IkePolicy.PeerCertificateType.PKCS7;
import static org.batfish.representation.juniper.IkePolicy.PeerCertificateType.X509_SIGNATURE;
import static org.batfish.representation.juniper.JuniperStructureType.PKI_LOCAL_CERTIFICATE;
import static org.batfish.representation.juniper.JuniperStructureUsage.IKE_POLICY_LOCAL_CERTIFICATE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;

import java.io.IOException;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.answers.ConvertConfigurationAnswerElement;
import org.batfish.main.Batfish;
import org.batfish.representation.juniper.IkePolicy;
import org.batfish.representation.juniper.JuniperConfiguration;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public final class JunosIkeCertificateTest {

  private static final String HOSTNAME = "ike-certificate-policy";

  @Rule public TemporaryFolder _folder = new TemporaryFolder();

  @Test
  public void testExtractionReferencesAndConversion() throws IOException {
    Batfish batfish = getBatfish(_folder, HOSTNAME);
    JuniperConfiguration juniperConfiguration = getVendorConfiguration(batfish, HOSTNAME);
    IkePolicy x509 =
        juniperConfiguration.getMasterLogicalSystem().getIkePolicies().get("X509-POLICY");
    IkePolicy pkcs7 =
        juniperConfiguration.getMasterLogicalSystem().getIkePolicies().get("PKCS7-POLICY");

    assertThat(x509.getLocalCertificates(), contains("CERT-A", "CERT-B"));
    assertThat(x509.getPeerCertificateType(), equalTo(X509_SIGNATURE));
    assertThat(pkcs7.getLocalCertificates(), contains("CERT-C"));
    assertThat(pkcs7.getPeerCertificateType(), equalTo(PKCS7));
    assertThat(
        getParseWarnings(batfish, HOSTNAME),
        containsInAnyOrder(
            isTodo("local-certificate CERT-A"),
            isTodo("local-certificate CERT-B"),
            isTodo("peer-certificate-type x509-signature"),
            isTodo("local-certificate CERT-C"),
            isTodo("peer-certificate-type pkcs7")));

    Configuration configuration = batfish.loadConfigurations(batfish.getSnapshot()).get(HOSTNAME);
    assertThat(
        configuration.getIkePhase1Policies().get("X509-POLICY").getIkePhase1Key().getKeyType(),
        equalTo(RSA_PUB_KEY));
    assertThat(
        configuration.getIkePhase1Policies().get("PKCS7-POLICY").getIkePhase1Key().getKeyType(),
        equalTo(RSA_PUB_KEY));

    ConvertConfigurationAnswerElement ccae =
        batfish.loadConvertConfigurationAnswerElementOrReparse(batfish.getSnapshot());
    String filename = "configs/" + HOSTNAME;
    assertThat(ccae, hasDefinedStructure(filename, PKI_LOCAL_CERTIFICATE, "CERT-A"));
    assertThat(ccae, hasDefinedStructure(filename, PKI_LOCAL_CERTIFICATE, "CERT-B"));
    assertThat(ccae, hasDefinedStructure(filename, PKI_LOCAL_CERTIFICATE, "CERT-C"));
    assertThat(
        ccae,
        hasReferencedStructure(
            filename, PKI_LOCAL_CERTIFICATE, "CERT-A", IKE_POLICY_LOCAL_CERTIFICATE));
    assertThat(
        ccae,
        hasReferencedStructure(
            filename, PKI_LOCAL_CERTIFICATE, "CERT-B", IKE_POLICY_LOCAL_CERTIFICATE));
    assertThat(
        ccae,
        hasReferencedStructure(
            filename, PKI_LOCAL_CERTIFICATE, "CERT-C", IKE_POLICY_LOCAL_CERTIFICATE));
    Warnings warnings = ccae.getWarnings().getOrDefault(HOSTNAME, new Warnings());
    assertThat(warnings.getRedFlagWarnings(), empty());
    assertThat(warnings.getUnimplementedWarnings(), empty());
  }
}
