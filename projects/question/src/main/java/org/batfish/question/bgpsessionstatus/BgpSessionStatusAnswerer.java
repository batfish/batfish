package org.batfish.question.bgpsessionstatus;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Sets;
import com.google.common.graph.Network;
import java.util.Map;
import java.util.Set;
import org.batfish.common.Answerer;
import org.batfish.common.BatfishException;
import org.batfish.common.plugin.IBatfish;
import org.batfish.common.util.CommonUtil;
import org.batfish.datamodel.BgpNeighbor;
import org.batfish.datamodel.BgpSession;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.datamodel.questions.Exclusion;
import org.batfish.datamodel.questions.Question;
import org.batfish.question.bgpsessionstatus.BgpSessionInfo.SessionStatus;
import org.batfish.question.bgpsessionstatus.BgpSessionInfo.SessionType;

public class BgpSessionStatusAnswerer extends Answerer {

  BgpSessionStatusAnswerer(Question question, IBatfish batfish) {
    super(question, batfish);
  }

  @Override
  public AnswerElement answer() {

    BgpSessionStatusQuestion question = (BgpSessionStatusQuestion) _question;

    Map<String, Configuration> configurations = _batfish.loadConfigurations();
    Set<String> includeNodes1 = question.getNode1Regex().getMatchingNodes(_batfish);
    Set<String> includeNodes2 = question.getNode2Regex().getMatchingNodes(_batfish);

    BgpSessionStatusAnswerElement answer = BgpSessionStatusAnswerElement.create(question);

    Map<Ip, Set<String>> ipOwners = CommonUtil.computeIpOwners(configurations, true);
    Set<Ip> allInterfaceIps = ipOwners.keySet();

    Network<BgpNeighbor, BgpSession> configuredBgpTopology =
        CommonUtil.initBgpTopology(configurations, ipOwners, true);

    Network<BgpNeighbor, BgpSession> establishedBgpTopology =
        question.getIncludeEstablishedCount()
            ? CommonUtil.initBgpTopology(
                configurations,
                ipOwners,
                false,
                true,
                _batfish.getDataPlanePlugin().getTracerouteEngine(),
                _batfish.loadDataPlane())
            : null;

    for (BgpNeighbor bgpNeighbor : configuredBgpTopology.nodes()) {
      String hostname = bgpNeighbor.getOwner().getHostname();
      String vrfName = bgpNeighbor.getVrf();
      // Only match nodes we care about
      if (!includeNodes1.contains(hostname)) {
        continue;
      }

      // Match foreign group
      boolean foreign =
          bgpNeighbor.getGroup() != null && question.matchesForeignGroup(bgpNeighbor.getGroup());
      if (foreign) {
        continue;
      }

      // Setup session info
      boolean ebgp = !bgpNeighbor.getRemoteAs().equals(bgpNeighbor.getLocalAs());
      boolean ebgpMultihop = bgpNeighbor.getEbgpMultihop();
      Prefix remotePrefix = bgpNeighbor.getPrefix();
      BgpSessionInfo bgpSessionInfo = new BgpSessionInfo(hostname, vrfName, remotePrefix);
      bgpSessionInfo._sessionType =
          ebgp
              ? ebgpMultihop ? SessionType.EBGP_MULTIHOP : SessionType.EBGP_SINGLEHOP
              : SessionType.IBGP;
      // Skip session types we don't care about
      if (!question.matchesType(bgpSessionInfo._sessionType)) {
        continue;
      }

      Ip localIp = bgpNeighbor.getLocalIp();
      if (bgpNeighbor.getDynamic()) {
        bgpSessionInfo._configuredStatus = SessionStatus.DYNAMIC_LISTEN;
      } else if (localIp == null) {
        bgpSessionInfo._configuredStatus = SessionStatus.MISSING_LOCAL_IP;
      } else {
        bgpSessionInfo._localIp = localIp;
        bgpSessionInfo._onLoopback =
            CommonUtil.isActiveLoopbackIp(localIp, configurations.get(hostname));

        Ip remoteIp = bgpNeighbor.getAddress();

        if (!allInterfaceIps.contains(localIp)) {
          bgpSessionInfo._configuredStatus = SessionStatus.UNKNOWN_LOCAL_IP;
        } else if (remoteIp == null || !allInterfaceIps.contains(remoteIp)) {
          bgpSessionInfo._configuredStatus = SessionStatus.UNKNOWN_REMOTE_IP;
        } else {
          if (!node2RegexMatchesIp(remoteIp, ipOwners, includeNodes2)) {
            continue;
          }
          if (configuredBgpTopology.adjacentNodes(bgpNeighbor).isEmpty()) {
            bgpSessionInfo._configuredStatus = SessionStatus.HALF_OPEN;
            // degree > 2 because of directed edges. 1 edge in, 1 edge out == single connection
          } else if (configuredBgpTopology.degree(bgpNeighbor) > 2) {
            bgpSessionInfo._configuredStatus = SessionStatus.MULTIPLE_REMOTES;
          } else {
            BgpNeighbor remoteNeighbor =
                configuredBgpTopology.adjacentNodes(bgpNeighbor).iterator().next();
            bgpSessionInfo._remoteNode = remoteNeighbor.getOwner().getHostname();
            bgpSessionInfo._configuredStatus = SessionStatus.UNIQUE_MATCH;
          }
        }
      }
      if (!question.matchesStatus(bgpSessionInfo._configuredStatus)) {
        continue;
      }

      bgpSessionInfo._establishedNeighbors =
          establishedBgpTopology != null && establishedBgpTopology.nodes().contains(bgpNeighbor)
              ? establishedBgpTopology.inDegree(bgpNeighbor)
              : -1;

      ObjectNode row = BgpSessionStatusAnswerElement.toRow(bgpSessionInfo);

      // exclude or not?
      Exclusion exclusion = Exclusion.covered(row, question.getExclusions());
      if (exclusion != null) {
        answer.addExcludedRow(row, exclusion.getName());
      } else {
        answer.addRow(row);
      }
    }

    answer.setSummary(answer.computeSummary(question.getAssertion()));
    return answer;
  }

  private static boolean node2RegexMatchesIp(
      Ip ip, Map<Ip, Set<String>> ipOwners, Set<String> includeNodes2) {
    Set<String> owners = ipOwners.get(ip);
    if (owners == null) {
      throw new BatfishException("Expected at least one owner of ip: " + ip);
    }
    return !Sets.intersection(includeNodes2, owners).isEmpty();
  }
}
