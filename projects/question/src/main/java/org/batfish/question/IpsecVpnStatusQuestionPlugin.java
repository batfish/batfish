package org.batfish.question;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.service.AutoService;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.common.Answerer;
import org.batfish.common.plugin.IBatfish;
import org.batfish.common.plugin.Plugin;
import org.batfish.common.util.CommonUtil;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.IpsecVpn;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.datamodel.questions.NodesSpecifier;
import org.batfish.datamodel.questions.Question;

@AutoService(Plugin.class)
public class IpsecVpnStatusQuestionPlugin extends QuestionPlugin {

  public enum Problem {
    INCOMPATIBLE_IKE_PROPOSALS,
    INCOMPATIBLE_IPSEC_PROPOSALS,
    INCOMPATIBLE_PRE_SHARED_KEY,
    MISSING_REMOTE_ENDPOINT,
    MULTIPLE_REMOTE_ENDPOINTS,
    NONE
  }

  public static class IpsecVpnEndpoint implements Comparable<IpsecVpnEndpoint> {

    private static final String PROP_HOSTNAME = "hostname";

    private static final String PROP_IPSEC_VPN = "ipsecVpn";

    private String _hostname;

    private String _ipsecVpn;

    @JsonCreator
    public IpsecVpnEndpoint(
        @JsonProperty(PROP_HOSTNAME) String hostname,
        @JsonProperty(PROP_IPSEC_VPN) String ipsecVpn) {
      _hostname = hostname;
      _ipsecVpn = ipsecVpn;
    }

    public IpsecVpnEndpoint(IpsecVpn vpn) {
      this(vpn.getOwner().getHostname(), vpn.getName());
    }

    @Override
    public int compareTo(IpsecVpnEndpoint o) {
      return Comparator.comparing(IpsecVpnEndpoint::getHostname)
          .thenComparing(IpsecVpnEndpoint::getIpsecVpn)
          .compare(this, o);
    }

    @JsonProperty(PROP_HOSTNAME)
    public String getHostname() {
      return _hostname;
    }

    @JsonProperty(PROP_IPSEC_VPN)
    public String getIpsecVpn() {
      return _ipsecVpn;
    }

    @Override
    public String toString() {
      return String.format("hostname: %s vpnname: %s", _hostname, _ipsecVpn);
    }
  }

  public static class IpsecVpnInfo implements Comparable<IpsecVpnInfo> {

    private static final String PROP_IPSEC_VPN_ENDPOINT = "ipsecVpnEndpoint";
    private static final String PROP_PROBLEMS = "problems";
    private static final String PROP_REMOTE_ENDPOINT = "remoteEndpoint";

    private IpsecVpnEndpoint _ipsecVpnEndpoint;
    private SortedSet<Problem> _problems;
    private IpsecVpnEndpoint _remoteEndpoint;

    @JsonCreator
    private IpsecVpnInfo(
        @JsonProperty(PROP_IPSEC_VPN_ENDPOINT) IpsecVpnEndpoint vpn,
        @JsonProperty(PROP_PROBLEMS) SortedSet<Problem> problems,
        @JsonProperty(PROP_REMOTE_ENDPOINT) IpsecVpnEndpoint remoteEndpoint) {
      _ipsecVpnEndpoint = vpn;
      _problems = problems;
      _remoteEndpoint = remoteEndpoint;
    }

    public IpsecVpnInfo(
        @Nonnull IpsecVpn ipsecVpn,
        @Nonnull SortedSet<Problem> problems,
        @Nullable IpsecVpn remoteEnd) {
      this(
          new IpsecVpnEndpoint(ipsecVpn),
          problems,
          remoteEnd == null ? null : new IpsecVpnEndpoint(remoteEnd));
    }

    @Override
    public int compareTo(IpsecVpnInfo o) {
      return Comparator.comparing(IpsecVpnInfo::getIpsecVpnEndpoint).compare(this, o);
    }

    @JsonProperty(PROP_IPSEC_VPN_ENDPOINT)
    public IpsecVpnEndpoint getIpsecVpnEndpoint() {
      return _ipsecVpnEndpoint;
    }

    @JsonProperty(PROP_PROBLEMS)
    public SortedSet<Problem> getProblems() {
      return _problems;
    }

    @JsonProperty(PROP_REMOTE_ENDPOINT)
    public IpsecVpnEndpoint getRemoteEndpoint() {
      return _remoteEndpoint;
    }

    @Override
    public String toString() {
      return String.format(
          "%s problems: %s remote: %s", _ipsecVpnEndpoint, _problems, _remoteEndpoint);
    }
  }

  public static class IpsecVpnStatusAnswerElement implements AnswerElement {

    private static final String PROP_IPSEC_VPNS = "ipsecVpns";

    private SortedSet<IpsecVpnInfo> _ipsecVpns;

    public IpsecVpnStatusAnswerElement() {
      this(null);
    }

    @JsonCreator
    public IpsecVpnStatusAnswerElement(
        @JsonProperty(PROP_IPSEC_VPNS) SortedSet<IpsecVpnInfo> ipsecVpns) {
      _ipsecVpns = ipsecVpns == null ? new TreeSet<>() : ipsecVpns;
    }

    @JsonProperty(PROP_IPSEC_VPNS)
    public SortedSet<IpsecVpnInfo> getIpsecVpns() {
      return _ipsecVpns;
    }
  }

  public static class IpsecVpnStatusAnswerer extends Answerer {

    public IpsecVpnStatusAnswerer(Question question, IBatfish batfish) {
      super(question, batfish);
    }

    @Override
    public AnswerElement answer() {
      IpsecVpnStatusQuestion question = (IpsecVpnStatusQuestion) _question;

      Map<String, Configuration> configurations = _batfish.loadConfigurations();
      Set<String> includeNodes1 = question.getNode1Regex().getMatchingNodes(configurations);
      Set<String> includeNodes2 = question.getNode2Regex().getMatchingNodes(configurations);

      CommonUtil.initRemoteIpsecVpns(configurations);

      IpsecVpnStatusAnswerElement answerElement = new IpsecVpnStatusAnswerElement();
      for (Configuration c : configurations.values()) {
        if (!includeNodes1.contains(c.getHostname())) {
          continue;
        }
        for (IpsecVpn ipsecVpn : c.getIpsecVpns().values()) {
          SortedSet<Problem> problems = new TreeSet<>();
          IpsecVpn remoteIpsecVpn = null;
          if (ipsecVpn.getRemoteIpsecVpn() == null) {
            problems.add(Problem.MISSING_REMOTE_ENDPOINT);
          } else {
            if (ipsecVpn.getCandidateRemoteIpsecVpns().size() != 1) {
              problems.add(Problem.MULTIPLE_REMOTE_ENDPOINTS);
            }
            remoteIpsecVpn = ipsecVpn.getRemoteIpsecVpn();
            String remoteHost = remoteIpsecVpn.getOwner().getHostname();
            if (!includeNodes2.contains(remoteHost)) {
              continue;
            }
            if (!ipsecVpn.compatibleIkeProposals(remoteIpsecVpn)) {
              problems.add(Problem.INCOMPATIBLE_IKE_PROPOSALS);
            }
            if (!ipsecVpn.compatibleIpsecProposals(remoteIpsecVpn)) {
              problems.add(Problem.INCOMPATIBLE_IPSEC_PROPOSALS);
            }
            if (!ipsecVpn.compatiblePreSharedKey(remoteIpsecVpn)) {
              problems.add(Problem.INCOMPATIBLE_PRE_SHARED_KEY);
            }
          }
          if (problems.size() == 0) {
            problems.add(Problem.NONE);
          }
          answerElement.getIpsecVpns().add(new IpsecVpnInfo(ipsecVpn, problems, remoteIpsecVpn));
        }
      }
      return answerElement;
    }
  }

  // <question_page_comment>

  /**
   * Checks if IPSec VPNs are correctly configured.
   *
   * <p>Details coming on what it means to be correctly configured.
   *
   * @type IpsecVpnStatus multifile
   * @param node1Regex Regular expression to match the nodes names for one end of the sessions.
   *     Default is '.*' (all nodes).
   * @param node2Regex Regular expression to match the nodes names for the other end of the
   *     sessions. Default is '.*' (all nodes).
   * @example bf_answer("IpsecVpnStatus", node1Regex="as1.*", node2Regex="as2.*") Returns status for
   *     all IPSec VPN sessions between nodes that start with as1 and those that start with as2.
   */
  public static class IpsecVpnStatusQuestion extends Question {

    private static final String PROP_NODE1_REGEX = "node1Regex";

    private static final String PROP_NODE2_REGEX = "node2Regex";

    private NodesSpecifier _node1Regex;

    private NodesSpecifier _node2Regex;

    @JsonCreator
    public IpsecVpnStatusQuestion(
        @JsonProperty(PROP_NODE1_REGEX) NodesSpecifier regex1,
        @JsonProperty(PROP_NODE2_REGEX) NodesSpecifier regex2) {
      _node1Regex = regex1 == null ? NodesSpecifier.ALL : regex1;
      _node2Regex = regex2 == null ? NodesSpecifier.ALL : regex2;
    }

    @Override
    public boolean getDataPlane() {
      return false;
    }

    @Override
    public String getName() {
      return "ipsecvpnstatus";
    }

    @JsonProperty(PROP_NODE1_REGEX)
    public NodesSpecifier getNode1Regex() {
      return _node1Regex;
    }

    @JsonProperty(PROP_NODE2_REGEX)
    public NodesSpecifier getNode2Regex() {
      return _node2Regex;
    }
  }

  @Override
  protected Answerer createAnswerer(Question question, IBatfish batfish) {
    return new IpsecVpnStatusAnswerer(question, batfish);
  }

  @Override
  protected Question createQuestion() {
    return new IpsecVpnStatusQuestion(null, null);
  }
}
