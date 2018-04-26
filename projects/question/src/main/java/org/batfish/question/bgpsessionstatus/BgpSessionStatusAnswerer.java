package org.batfish.question.bgpsessionstatus;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Sets;
import com.google.common.graph.Network;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.batfish.common.Answerer;
import org.batfish.common.BatfishException;
import org.batfish.common.plugin.IBatfish;
import org.batfish.common.util.CommonUtil;
import org.batfish.datamodel.BgpNeighbor;
import org.batfish.datamodel.BgpSession;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Interface;
import org.batfish.datamodel.InterfaceAddress;
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

    Set<Ip> allInterfaceIps = new HashSet<>();
    Set<Ip> loopbackIps = new HashSet<>();
    Map<Ip, Set<String>> ipOwners = new HashMap<>();
    // TODO: refactor this out into CommonUtil
    for (Configuration c : configurations.values()) {
      for (Interface i : c.getInterfaces().values()) {
        if (i.getActive() && i.getAddress() != null) {
          for (InterfaceAddress address : i.getAllAddresses()) {
            Ip ip = address.getIp();
            if (i.isLoopback(c.getConfigurationFormat())) {
              loopbackIps.add(ip);
            }
            allInterfaceIps.add(ip);
            Set<String> currentIpOwners = ipOwners.computeIfAbsent(ip, k -> new HashSet<>());
            currentIpOwners.add(c.getHostname());
          }
        }
      }
    }

    Network<BgpNeighbor, BgpSession> staticBgpTopology =
        CommonUtil.initBgpTopology(configurations, ipOwners, true);

    Network<BgpNeighbor, BgpSession> dynamicBgpTopology =
        question.getIncludeDynamicCount()
            ? CommonUtil.initBgpTopology(
                configurations,
                ipOwners,
                false,
                true,
                _batfish.getDataPlanePlugin().getFlowProcessor(),
                _batfish.loadDataPlane())
            : null;

    for (BgpNeighbor bgpNeighbor : staticBgpTopology.nodes()) {
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
        bgpSessionInfo._staticStatus = SessionStatus.PASSIVE;
      } else if (localIp == null) {
        bgpSessionInfo._staticStatus = SessionStatus.MISSING_LOCAL_IP;
      } else {
        bgpSessionInfo._localIp = localIp;
        bgpSessionInfo._onLoopback = loopbackIps.contains(localIp);
        Ip remoteIp = bgpNeighbor.getAddress();

        if (!allInterfaceIps.contains(localIp)) {
          bgpSessionInfo._staticStatus = SessionStatus.UNKNOWN_LOCAL_IP;
        } else if (remoteIp == null || !allInterfaceIps.contains(remoteIp)) {
          bgpSessionInfo._staticStatus = SessionStatus.UNKNOWN_REMOTE_IP;
        } else {
          if (!node2RegexMatchesIp(remoteIp, ipOwners, includeNodes2)) {
            continue;
          }
          if (staticBgpTopology.adjacentNodes(bgpNeighbor).isEmpty()) {
            bgpSessionInfo._staticStatus = SessionStatus.HALF_OPEN;
            // degree > 2 because of directed edges. 1 edge in, 1 edge out == single connection
          } else if (staticBgpTopology.degree(bgpNeighbor) > 2) {
            bgpSessionInfo._staticStatus = SessionStatus.MULTIPLE_REMOTES;
          } else {
            BgpNeighbor remoteNeighbor =
                staticBgpTopology.adjacentNodes(bgpNeighbor).iterator().next();
            bgpSessionInfo._remoteNode = remoteNeighbor.getOwner().getHostname();
            bgpSessionInfo._staticStatus = SessionStatus.UNIQUE_MATCH;
          }
        }
      }
      if (!question.matchesStatus(bgpSessionInfo._staticStatus)) {
        continue;
      }

      bgpSessionInfo._dynamicNeighbors =
          dynamicBgpTopology != null && dynamicBgpTopology.nodes().contains(bgpNeighbor)
              ? dynamicBgpTopology.inDegree(bgpNeighbor)
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
