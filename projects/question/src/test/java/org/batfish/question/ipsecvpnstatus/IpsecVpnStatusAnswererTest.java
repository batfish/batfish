package org.batfish.question.ipsecvpnstatus;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.collect.Sets;
import java.util.Arrays;
import java.util.Collections;
import java.util.SortedMap;
import java.util.TreeMap;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationFormat;
import org.batfish.datamodel.IkeGateway;
import org.batfish.datamodel.IkePolicy;
import org.batfish.datamodel.IkeProposal;
import org.batfish.datamodel.IpsecPolicy;
import org.batfish.datamodel.IpsecProposal;
import org.batfish.datamodel.IpsecVpn;
import org.batfish.question.ipsecvpnstatus.IpsecVpnInfo.Problem;
import org.junit.Test;

public class IpsecVpnStatusAnswererTest {

  private static IpsecVpn createIpsecVpn(
      String name, IkeProposal ikeProposal, IpsecProposal ipsecProposal, String pskHash) {
    IpsecVpn ipsecVpn = new IpsecVpn(name);
    ipsecVpn.setOwner(new Configuration(name, ConfigurationFormat.UNKNOWN));

    IkeGateway ikeGw = new IkeGateway(name + "-ikeGw");
    ipsecVpn.setIkeGateway(ikeGw);

    IkePolicy ikePolicy = new IkePolicy(name + "-ikePolicy");
    ikeGw.setIkePolicy(ikePolicy);

    SortedMap<String, IkeProposal> ikeProposalMap = new TreeMap<>();
    ikeProposalMap.put(name + "-ikeproposal", ikeProposal);
    ikePolicy.setProposals(ikeProposalMap);
    ikePolicy.setPreSharedKeyHash(pskHash);

    IpsecPolicy ipsecPolicy = new IpsecPolicy(name + "-ipsecpolicy");
    ipsecVpn.setIpsecPolicy(ipsecPolicy);

    ipsecPolicy.getProposals().add(ipsecProposal);

    return ipsecVpn;
  }

  @Test
  public void analyzeVpnTestIncompatibleIkeProposal() {
    IpsecVpn ipsecVpn =
        createIpsecVpn(
            "local", IkeProposal.PSK_3DES_DH2_MD5, IpsecProposal.NOPFS_ESP_DES_MD5, "key");
    IpsecVpn remote1 =
        createIpsecVpn(
            "remote1", IkeProposal.PSK_3DES_DH2_SHA1, IpsecProposal.NOPFS_ESP_DES_MD5, "key");

    ipsecVpn.initCandidateRemoteVpns();
    ipsecVpn.setRemoteIpsecVpn(remote1);
    ipsecVpn.getCandidateRemoteIpsecVpns().add(remote1);

    IpsecVpnInfo vpnInfo = IpsecVpnStatusAnswerer.analyzeIpsecVpn(ipsecVpn);

    assertThat(
        vpnInfo.getProblems(), equalTo(Collections.singleton(Problem.INCOMPATIBLE_IKE_PROPOSALS)));
    assertThat(vpnInfo.getRemoteEndpoint(), equalTo(new IpsecVpnEndpoint(remote1)));
  }

  @Test
  public void analyzeVpnTestIncompatibleIpsecProposal() {
    IpsecVpn ipsecVpn =
        createIpsecVpn(
            "local", IkeProposal.PSK_3DES_DH2_MD5, IpsecProposal.NOPFS_ESP_DES_MD5, "key");
    IpsecVpn remote1 =
        createIpsecVpn(
            "remote1", IkeProposal.PSK_3DES_DH2_MD5, IpsecProposal.NOPFS_ESP_DES_SHA, "key");

    ipsecVpn.initCandidateRemoteVpns();
    ipsecVpn.setRemoteIpsecVpn(remote1);
    ipsecVpn.getCandidateRemoteIpsecVpns().add(remote1);

    IpsecVpnInfo vpnInfo = IpsecVpnStatusAnswerer.analyzeIpsecVpn(ipsecVpn);

    assertThat(
        vpnInfo.getProblems(),
        equalTo(Collections.singleton(Problem.INCOMPATIBLE_IPSEC_PROPOSALS)));
    assertThat(vpnInfo.getRemoteEndpoint(), equalTo(new IpsecVpnEndpoint(remote1)));
  }

  @Test
  public void analyzeVpnTestIncompatiblePreSharedKey() {
    IpsecVpn ipsecVpn =
        createIpsecVpn(
            "local", IkeProposal.PSK_3DES_DH2_MD5, IpsecProposal.NOPFS_ESP_DES_MD5, "key");
    IpsecVpn remote1 =
        createIpsecVpn(
            "remote1", IkeProposal.PSK_3DES_DH2_MD5, IpsecProposal.NOPFS_ESP_DES_MD5, "key-bad");

    ipsecVpn.initCandidateRemoteVpns();
    ipsecVpn.setRemoteIpsecVpn(remote1);
    ipsecVpn.getCandidateRemoteIpsecVpns().add(remote1);

    IpsecVpnInfo vpnInfo = IpsecVpnStatusAnswerer.analyzeIpsecVpn(ipsecVpn);

    assertThat(
        vpnInfo.getProblems(), equalTo(Collections.singleton(Problem.INCOMPATIBLE_PRE_SHARED_KEY)));
    assertThat(vpnInfo.getRemoteEndpoint(), equalTo(new IpsecVpnEndpoint(remote1)));
  }

  @Test
  public void analyzeVpnTestMissingRemote() {
    IpsecVpn ipsecVpn =
        createIpsecVpn(
            "local", IkeProposal.PSK_3DES_DH2_MD5, IpsecProposal.NOPFS_ESP_DES_MD5, "key");
    IpsecVpnInfo vpnInfo = IpsecVpnStatusAnswerer.analyzeIpsecVpn(ipsecVpn);

    assertThat(
        vpnInfo.getProblems(), equalTo(Collections.singleton(Problem.MISSING_REMOTE_ENDPOINT)));
  }

  @Test
  public void analyzeVpnTestMultipleRemoteEndpoints() {
    IpsecVpn ipsecVpn =
        createIpsecVpn(
            "local", IkeProposal.PSK_3DES_DH2_MD5, IpsecProposal.NOPFS_ESP_DES_MD5, "key");
    IpsecVpn remote1 =
        createIpsecVpn(
            "remote1", IkeProposal.PSK_3DES_DH2_MD5, IpsecProposal.NOPFS_ESP_DES_MD5, "key");
    IpsecVpn remote2 =
        createIpsecVpn(
            "remote2", IkeProposal.PSK_3DES_DH2_MD5, IpsecProposal.NOPFS_ESP_DES_MD5, "key");

    ipsecVpn.initCandidateRemoteVpns();
    ipsecVpn.setRemoteIpsecVpn(remote1);
    ipsecVpn.getCandidateRemoteIpsecVpns().add(remote1);
    ipsecVpn.getCandidateRemoteIpsecVpns().add(remote2);

    IpsecVpnInfo vpnInfo = IpsecVpnStatusAnswerer.analyzeIpsecVpn(ipsecVpn);

    assertThat(
        vpnInfo.getProblems(), equalTo(Collections.singleton(Problem.MULTIPLE_REMOTE_ENDPOINTS)));
  }

  @Test
  public void analyzeVpnTestMultipleProblems() {
    IpsecVpn ipsecVpn =
        createIpsecVpn(
            "local", IkeProposal.PSK_3DES_DH2_SHA1, IpsecProposal.NOPFS_ESP_DES_SHA, "key");
    IpsecVpn remote1 =
        createIpsecVpn(
            "remote1", IkeProposal.PSK_3DES_DH2_MD5, IpsecProposal.NOPFS_ESP_DES_MD5, "key-bad");

    ipsecVpn.initCandidateRemoteVpns();
    ipsecVpn.setRemoteIpsecVpn(remote1);
    ipsecVpn.getCandidateRemoteIpsecVpns().add(remote1);

    IpsecVpnInfo vpnInfo = IpsecVpnStatusAnswerer.analyzeIpsecVpn(ipsecVpn);

    assertThat(
        vpnInfo.getProblems(),
        equalTo(
            Sets.newTreeSet(
                Arrays.asList(
                    Problem.INCOMPATIBLE_IKE_PROPOSALS,
                    Problem.INCOMPATIBLE_IPSEC_PROPOSALS,
                    Problem.INCOMPATIBLE_PRE_SHARED_KEY))));
    assertThat(vpnInfo.getRemoteEndpoint(), equalTo(new IpsecVpnEndpoint(remote1)));
  }

  @Test
  public void analyzeVpnTestNone() {
    IpsecVpn ipsecVpn =
        createIpsecVpn(
            "local", IkeProposal.PSK_3DES_DH2_MD5, IpsecProposal.NOPFS_ESP_DES_MD5, "key");
    IpsecVpn remote1 =
        createIpsecVpn(
            "remote1", IkeProposal.PSK_3DES_DH2_MD5, IpsecProposal.NOPFS_ESP_DES_MD5, "key");

    ipsecVpn.initCandidateRemoteVpns();
    ipsecVpn.setRemoteIpsecVpn(remote1);
    ipsecVpn.getCandidateRemoteIpsecVpns().add(remote1);

    IpsecVpnInfo vpnInfo = IpsecVpnStatusAnswerer.analyzeIpsecVpn(ipsecVpn);

    assertThat(vpnInfo.getProblems(), equalTo(Collections.singleton(Problem.NONE)));
    assertThat(vpnInfo.getRemoteEndpoint(), equalTo(new IpsecVpnEndpoint(remote1)));
  }
}
