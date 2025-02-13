package projects.minesweeper.src.main.java.org.batfish.minesweeper.question.comparepeergrouppolicies;

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Stream;
import org.batfish.common.NetworkSnapshot;
import org.batfish.common.plugin.IBatfish;
import org.batfish.datamodel.BgpPeerConfig;
import org.batfish.datamodel.BgpProcess;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.questions.BgpRoute;
import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.minesweeper.question.compareroutepolicies.CompareRoutePoliciesAnswerer;
import org.batfish.minesweeper.question.compareroutepolicies.CompareRoutePoliciesQuestion;
import org.batfish.minesweeper.utils.Tuple;
import org.batfish.question.testroutepolicies.Result;

public final class ComparePeerGroupPoliciesUtils {

  /**
   * @param batfish the batfish object
   * @param snapshot the current snapshot
   * @param reference the reference snapshot
   * @return a stream of all the BGP policy semantic differences between the two snapshots. For
   *     every node in both snapshots, and for every peer group that appears in both snapshots we
   *     use CRP to compare their policies across the snapshots and add to the stream any pair of
   *     policies that are different. See {@link CompareRoutePoliciesAnswerer} for more details on
   *     the comparison of two policies.
   */
  public static Stream<Tuple<Result<BgpRoute, BgpRoute>, Result<BgpRoute, BgpRoute>>>
      getDifferencesStream(IBatfish batfish, NetworkSnapshot snapshot, NetworkSnapshot reference) {
    // Find the candidate differences based on a syntactic check.
    Map<SyntacticDifference, SortedSet<String>> candidates =
        findDifferenceCandidates(batfish, snapshot, reference);

    Stream<Tuple<Result<BgpRoute, BgpRoute>, Result<BgpRoute, BgpRoute>>> answers =
        Stream.<Tuple<Result<BgpRoute, BgpRoute>, Result<BgpRoute, BgpRoute>>>builder().build();

    for (Map.Entry<SyntacticDifference, SortedSet<String>> entry : candidates.entrySet()) {
      SyntacticDifference candidate = entry.getKey();
      String representativeDevice = entry.getValue().first();
      // For each potential difference running CompareRoutePolicies
      CompareRoutePoliciesQuestion crpQ =
          new CompareRoutePoliciesQuestion(
              Environment.Direction.IN,
              candidate.getCurrentPolicy().getName(),
              candidate.getReferencePolicy().getName(),
              representativeDevice);

      CompareRoutePoliciesAnswerer crpAnswerer = new CompareRoutePoliciesAnswerer(crpQ, batfish);
      answers =
          Stream.concat(answers, crpAnswerer.getUtils().getDifferencesStream(snapshot, reference));
    }

    return answers;
  }

  /**
   * @param router The router for which we are comparing routing policies
   * @param currentPolicy the name policy of the new snapshot
   * @param referencePolicy the name policy of the reference snapshot
   * @param syntacticCompare A syntactic comparator.
   * @param differences A map tracking the syntactic differences. The keys of the map represent
   *     syntactic differences and the values are the sets of devices they appear on.
   */
  private static void trackDifference(
      String router,
      String currentPolicy,
      String referencePolicy,
      SyntacticCompare syntacticCompare,
      Map<SyntacticDifference, SortedSet<String>> differences) {
    if (!syntacticCompare.areEqual(currentPolicy, referencePolicy)) {
      SyntacticDifference d =
          new SyntacticDifference(
              syntacticCompare.getCurrentConfig().getRoutingPolicies().get(currentPolicy),
              syntacticCompare.getReferenceConfig().getRoutingPolicies().get(referencePolicy));
      SortedSet<String> routers = differences.computeIfAbsent(d, k -> new TreeSet<>());
      routers.add(router);
    }
  }

  /**
   * @param batfish the batfish object
   * @param snapshot The current snapshot
   * @param reference The reference snapshot
   * @return A map from potential differences to the devices they appear on.
   */
  private static SortedMap<SyntacticDifference, SortedSet<String>> findDifferenceCandidates(
      IBatfish batfish, NetworkSnapshot snapshot, NetworkSnapshot reference) {
    SortedMap<String, Configuration> currentConfigs =
        batfish.getProcessedConfigurations(snapshot).orElse(batfish.loadConfigurations(snapshot));
    SortedMap<String, Configuration> referenceConfigs =
        batfish.getProcessedConfigurations(reference).orElse(batfish.loadConfigurations(reference));

    // Routing policies that are potentially different. Maps them to the devices they live on.
    SortedMap<SyntacticDifference, SortedSet<String>> candidates = new TreeMap<>();

    // Go through every device in the current snapshot
    for (Map.Entry<String, Configuration> current : currentConfigs.entrySet()) {
      String router = current.getKey();
      Configuration currentConfig = current.getValue();
      Configuration refConfig = referenceConfigs.get(current.getKey());
      // If the device is also present in the reference snapshot
      if (refConfig != null) {
        // Find out what changed in the route-maps of this device.

        SyntacticCompare syntacticCompare = new SyntacticCompare(currentConfig, refConfig);
        for (Vrf vrf : currentConfig.getVrfs().values()) {
          // Each peerGroup has multiple peers that have uniform policy. We keep this set of
          // peerGroup's seen to
          // ignore tracking a difference for the same peerGroup twice.
          Set<String> peerGroupsSeen = new HashSet<>();
          Vrf refVrf = refConfig.getVrfs().get(vrf.getName());
          if (refVrf != null) {
            // Compare the BGP peering policies on this VRF.
            BgpProcess bgp = vrf.getBgpProcess();
            if (bgp != null) {
              // If there is a BGP process on the device in the current snapshot.
              for (BgpPeerConfig peer : bgp.getAllPeerConfigs()) {
                // If we have already covered this peerGroup then we do not to compare policies
                // again.
                if (!peerGroupsSeen.contains(peer.getGroup())) {
                  // Find the config for the same peer in the reference snapshot.
                  Optional<BgpPeerConfig> refPeer =
                      refVrf
                          .getBgpProcess()
                          .allPeerConfigsStream()
                          .filter(
                              p -> {
                                assert p.getGroup() != null;
                                return p.getGroup().equals(peer.getGroup());
                              })
                          .findFirst();

                  if (refPeer.isPresent()) {
                    // The peer is in both snapshots. Check if the export policy used in both
                    // snapshots is syntactically
                    // the same.
                    if ((peer.getIpv4UnicastAddressFamily().getExportPolicy() != null)
                        && (refPeer.get().getIpv4UnicastAddressFamily().getExportPolicy()
                            != null)) {
                      trackDifference(
                          router,
                          peer.getIpv4UnicastAddressFamily().getExportPolicy(),
                          refPeer.get().getIpv4UnicastAddressFamily().getExportPolicy(),
                          syntacticCompare,
                          candidates);
                    }
                    // Likewise for the import policy.
                    if ((peer.getIpv4UnicastAddressFamily().getImportPolicy() != null)
                        && (refPeer.get().getIpv4UnicastAddressFamily().getImportPolicy()
                            != null)) {
                      trackDifference(
                          router,
                          peer.getIpv4UnicastAddressFamily().getImportPolicy(),
                          refPeer.get().getIpv4UnicastAddressFamily().getImportPolicy(),
                          syntacticCompare,
                          candidates);
                    }
                    peerGroupsSeen.add(peer.getGroup());
                  }
                }
              }
            }
          }
        }
      }
    }
    return candidates;
  }
}
