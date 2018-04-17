package org.batfish.question.tracefilters;

import com.google.common.annotations.VisibleForTesting;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import org.batfish.common.Answerer;
import org.batfish.common.plugin.IBatfish;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.FilterResult;
import org.batfish.datamodel.IpAccessList;
import org.batfish.datamodel.IpsecVpn;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.datamodel.questions.Question;
import org.batfish.question.ipsecvpnstatus.IpsecVpnInfo;
import org.batfish.question.ipsecvpnstatus.IpsecVpnInfo.Problem;

public class TraceFiltersAnswerer extends Answerer {

  public TraceFiltersAnswerer(Question question, IBatfish batfish) {
    super(question, batfish);
  }

  @VisibleForTesting
  public static IpsecVpnInfo analyzeIpsecVpn(IpsecVpn ipsecVpn) {
    SortedSet<Problem> problems = new TreeSet<>();
    IpsecVpn remoteIpsecVpn = null;
    if (ipsecVpn.getRemoteIpsecVpn() == null) {
      problems.add(Problem.MISSING_REMOTE_ENDPOINT);
    } else {
      if (ipsecVpn.getCandidateRemoteIpsecVpns().size() != 1) {
        problems.add(Problem.MULTIPLE_REMOTE_ENDPOINTS);
      }
      remoteIpsecVpn = ipsecVpn.getRemoteIpsecVpn();
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
    return new IpsecVpnInfo(ipsecVpn, problems, remoteIpsecVpn);
  }

  @Override
  public AnswerElement answer() {
    TraceFiltersQuestion question = (TraceFiltersQuestion) _question;

    Map<String, Configuration> configurations = _batfish.loadConfigurations();
    Set<String> includeNodes = question.getNodeRegex().getMatchingNodes(configurations);

    TraceFiltersAnswerElement answerElement = new TraceFiltersAnswerElement();
    for (Configuration c : configurations.values()) {
      if (!includeNodes.contains(c.getHostname())) {
        continue;
      }
      for (IpAccessList filter : c.getIpAccessLists().values()) {
        if (!question.getFilterRegex().matches(filter)) {
          continue;
        }
        FilterResult result = filter.filter(null, null, c.getIpAccessLists());
      }
    }
    return answerElement;
  }
}
