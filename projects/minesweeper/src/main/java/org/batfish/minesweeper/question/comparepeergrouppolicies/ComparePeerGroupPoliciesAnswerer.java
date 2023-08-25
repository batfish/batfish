package projects.minesweeper.src.main.java.org.batfish.minesweeper.question.comparepeergrouppolicies;

import com.google.common.collect.ImmutableList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Stream;
import org.batfish.common.Answerer;
import org.batfish.common.NetworkSnapshot;
import org.batfish.common.plugin.IBatfish;
import org.batfish.datamodel.BgpPeerConfig;
import org.batfish.datamodel.BgpProcess;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.datamodel.questions.BgpRoute;
import org.batfish.datamodel.questions.Question;
import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.table.Row;
import org.batfish.datamodel.table.TableAnswerElement;
import org.batfish.minesweeper.question.compareroutepolicies.CompareRoutePoliciesAnswerer;
import org.batfish.minesweeper.question.compareroutepolicies.CompareRoutePoliciesQuestion;
import org.batfish.minesweeper.utils.Tuple;
import org.batfish.question.testroutepolicies.Result;
import org.batfish.question.testroutepolicies.TestRoutePoliciesAnswerer;

public class ComparePeerGroupPoliciesAnswerer extends Answerer {

  public ComparePeerGroupPoliciesAnswerer(Question question, IBatfish batfish) {
    super(question, batfish);
  }

  /** This question is only working in Differential mode. */
  @Override
  public AnswerElement answer(NetworkSnapshot snapshot) {

    throw new UnsupportedOperationException(
        "SemDiff is only meant to be used in differential mode.");
  }

  public Stream<Tuple<Result<BgpRoute>, Result<BgpRoute>>> answerDiffHelper(
      NetworkSnapshot snapshot, NetworkSnapshot reference) {
    // Find the candidate differences based on a syntactic check.
    Map<SyntacticDifference, SortedSet<String>> candidates =
        findDifferenceCandidates(snapshot, reference);

    Stream<Tuple<Result<BgpRoute>, Result<BgpRoute>>> answers =
        Stream.<Tuple<Result<BgpRoute>, Result<BgpRoute>>>builder().build();

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

      CompareRoutePoliciesAnswerer crpAnswerer =
          new CompareRoutePoliciesAnswerer(crpQ, this._batfish);
      answers = Stream.concat(answers, crpAnswerer.answerDiffHelper(snapshot, reference));
    }

    return answers;
  }

  @Override
  public AnswerElement answerDiff(NetworkSnapshot snapshot, NetworkSnapshot reference) {
    List<Row> answers =
        answerDiffHelper(snapshot, reference)
            .map(t -> TestRoutePoliciesAnswerer.toCompareRow(t.getFirst(), t.getSecond()))
            .collect(ImmutableList.toImmutableList());

    return CompareRoutePoliciesAnswerer.toTableAnswer(_question, answers);
  }

  /**
   * Given a list of Tables with the same columns composes them together by appending their rows
   * in-order.
   *
   * @param tables a list of tables
   * @return one table containing the same columns and the rows of all input tables.
   */
  private TableAnswerElement stitchTables(List<TableAnswerElement> tables) {
    TableAnswerElement stitchedTable =
        new TableAnswerElement(TestRoutePoliciesAnswerer.compareMetadata());

    for (TableAnswerElement t : tables) {
      for (Row r : t.getRowsList()) {
        stitchedTable.addRow(r);
      }
    }
    return stitchedTable;
  }

  /**
   * @param router The router for which we are comparing routing policies
   * @param currentPolicy the name policy of the new snapshot
   * @param referencePolicy the name policy of the reference snapshot
   * @param syntacticCompare A syntactic comparator.
   * @param differences A map tracking the syntactic differences. The keys of the map represent
   *     syntactic differences and the values are the sets of devices they appear on.
   */
  private void trackDifference(
      String router,
      String currentPolicy,
      String referencePolicy,
      SyntacticCompare syntacticCompare,
      Map<SyntacticDifference, SortedSet<String>> differences) {
    if (!syntacticCompare.areEqual(currentPolicy, referencePolicy)) {
      SyntacticDifference d =
          new SyntacticDifference(
              syntacticCompare.getCurrentConfig().getRoutingPolicies().get(currentPolicy),
              syntacticCompare.getReferenceConfig().getRoutingPolicies().get(referencePolicy),
              syntacticCompare.getContextDiff());
      SortedSet<String> routers = differences.computeIfAbsent(d, k -> new TreeSet<>());
      routers.add(router);
    }
  }

  /**
   * @param snapshot The current snapshot
   * @param reference The reference snapshot
   * @return A map from potential differences to the devices they appear on.
   */
  private SortedMap<SyntacticDifference, SortedSet<String>> findDifferenceCandidates(
      NetworkSnapshot snapshot, NetworkSnapshot reference) {
    SortedMap<String, Configuration> currentConfigs =
        _batfish.getProcessedConfigurations(snapshot).orElse(_batfish.loadConfigurations(snapshot));
    SortedMap<String, Configuration> referenceConfigs =
        _batfish
            .getProcessedConfigurations(reference)
            .orElse(_batfish.loadConfigurations(reference));

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
