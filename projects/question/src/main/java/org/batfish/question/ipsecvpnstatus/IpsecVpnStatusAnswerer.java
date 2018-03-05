package org.batfish.question.ipsecvpnstatus;

import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import org.batfish.common.Answerer;
import org.batfish.common.plugin.IBatfish;
import org.batfish.common.util.CommonUtil;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.IpsecVpn;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.datamodel.questions.Question;
import org.batfish.question.ipsecvpnstatus.IpsecVpnInfo.Problem;

public class IpsecVpnStatusAnswerer extends Answerer {

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
        if (problems.stream().anyMatch(v -> question.matchesProblem(v))) {
          answerElement.getIpsecVpns().add(new IpsecVpnInfo(ipsecVpn, problems, remoteIpsecVpn));
        }
      }
    }
    return answerElement;
  }
}
