package projects.minesweeper.src.main.java.org.batfish.minesweeper.question.semdiff;

import static projects.minesweeper.src.main.java.org.batfish.minesweeper.question.semdiff.OutputRouteDifference.compute_action_difference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import javax.annotation.Nullable;
import org.batfish.common.Answerer;
import org.batfish.common.NetworkSnapshot;
import org.batfish.common.plugin.IBatfish;
import org.batfish.datamodel.BgpPeerConfig;
import org.batfish.datamodel.BgpProcess;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.Vrf;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.datamodel.answers.StringAnswerElement;
import org.batfish.datamodel.questions.BgpRoute;
import org.batfish.datamodel.questions.BgpRouteDiff;
import org.batfish.datamodel.questions.Question;
import org.batfish.datamodel.questions.StructuredBgpRouteDiffs;
import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.RoutingPolicy;
import org.batfish.datamodel.table.ColumnMetadata;
import org.batfish.datamodel.table.Row;
import org.batfish.datamodel.table.TableAnswerElement;
import org.batfish.datamodel.table.TableMetadata;
import org.batfish.datamodel.trace.TraceTree;
import org.batfish.minesweeper.question.compareroutepolicies.CompareRoutePoliciesAnswerer;
import org.batfish.minesweeper.question.compareroutepolicies.CompareRoutePoliciesQuestion;

public class SemDiffAnswerer extends Answerer {
  private final Integer _maxSimilarDifferences = 3;

  public SemDiffAnswerer(Question question, IBatfish batfish) {
    super(question, batfish);
  }

  /** This question is only working in Differential mode. */
  @Override
  public AnswerElement answer(NetworkSnapshot snapshot) {

    throw new UnsupportedOperationException(
        "SemDiff is only meant to be used in differential mode.");
  }

  @Override
  public AnswerElement answerDiff(NetworkSnapshot snapshot, NetworkSnapshot reference) {

    Map<SyntacticDifference, SortedSet<String>> candidates =
        findDifferenceCandidates(snapshot, reference);
    // Add each difference in a SortedMap (to ensure consistent order in the generated differences)
    // that
    // maps the difference as a string, to a representative device it appears on.
    // To minimize the output, the key should not hold device-specific information, so that we can
    // benefit from deduplication. Duplication can happen in cases, where the route-maps might
    // differ syntactically
    // or their routing context might be different, but these differences do not affect the
    // current-reference difference
    // so they do not appear in the output and hence we can deduplicate.
    SortedMap<String, SortedSet<String>> answers = new TreeMap<>();

    for (Map.Entry<SyntacticDifference, SortedSet<String>> entry : candidates.entrySet()) {
      SyntacticDifference candidate = entry.getKey();
      String representativeDevice = entry.getValue().first();
      CompareRoutePoliciesQuestion crpQ =
          new CompareRoutePoliciesQuestion(
              Environment.Direction.IN,
              candidate.getCurrentPolicy().getName(),
              candidate.getReferencePolicy().getName(),
              representativeDevice);

      CompareRoutePoliciesAnswerer crpAnswerer =
          new CompareRoutePoliciesAnswerer(crpQ, this._batfish);
      TableAnswerElement differences =
          (TableAnswerElement) crpAnswerer.answerDiff(snapshot, reference);
      if (!differences.getRowsList().isEmpty()) {
        // Add a difference if it is non-empty, i.e., there is a semantic difference.
        String answer = tableAnswerToStringAnswer(differences);
        SortedSet<String> devices = answers.get(answer);
        if (devices == null) {
          devices = new TreeSet<>();
          answers.put(answer, devices);
        }
        devices.add(representativeDevice);
      }
    }

    StringBuilder answer = new StringBuilder();
    for (Map.Entry<String, SortedSet<String>> diff : answers.entrySet()) {
      answer
          .append("#### Found difference on device _")
          .append(diff.getValue().first())
          .append("_:\n\n");
      answer.append(diff.getKey()).append("\n");
    }
    return new StringAnswerElement(answer.toString());
  }

  /**
   * @param currentPolicy The routing policy in the current snapshot.
   * @param referencePolicy The routing policy in the reference snapshot.
   * @return True if the two routing policies are syntactically the same or if either one is null.
   */
  private boolean routingPoliciesSyntacticEq(
      @Nullable RoutingPolicy currentPolicy, @Nullable RoutingPolicy referencePolicy) {
    if (currentPolicy == null || referencePolicy == null) {
      return true;
    }
    return currentPolicy.getStatements().equals(referencePolicy.getStatements());
  }

  /**
   * @param router The router for which we are comparing routing policies
   * @param currentPolicy the name policy of the new snapshot
   * @param referencePolicy the name policy of the reference snapshot
   * @param currentConfig new config
   * @param referenceConfig reference config
   * @param differences A map tracking the potential (syntactic) differences. The same differences
   *     across different devices are grouped together.
   */
  private void trackDifference(
      String router,
      String currentPolicy,
      String referencePolicy,
      Configuration currentConfig,
      Configuration referenceConfig,
      Map<SyntacticDifference, SortedSet<String>> differences) {
    RoutingPolicy curPolicy = currentConfig.getRoutingPolicies().get(currentPolicy);
    RoutingPolicy refPolicy = referenceConfig.getRoutingPolicies().get(referencePolicy);

    // TODO: For syntactic equality we should be doing a context-aware equality such that routing
    // policy calls are dereferenced.
    // That said, this works correctly for most purposes.
    boolean policiesSyntaxEq = routingPoliciesSyntacticEq(curPolicy, refPolicy);

    // Build routing context (prefix/community/as-path list definitions) of each snapshot.
    RoutingPolicyContextDiff context = new RoutingPolicyContextDiff(currentConfig, referenceConfig);

    // If the two policies are not syntactically equal or their routing context differs then track
    // them as a potential difference.
    if (!policiesSyntaxEq || context.differ(curPolicy)) {
      _logger.debug(
          "Adding potential difference for "
              + " policy "
              + currentPolicy
              + " and "
              + referencePolicy);
      if (policiesSyntaxEq) {
        _logger.debug("Difference due to routing context difference.");
      }

      SyntacticDifference d = new SyntacticDifference(curPolicy, refPolicy, context);
      SortedSet<String> routers = differences.computeIfAbsent(d, k -> new TreeSet<>());
      routers.add(router);
    }
  }

  /**
   * @param snapshot The current snapshot
   * @param reference The reference snapshot
   * @return A map from potential differences to the devices they appear on.
   */
  private Map<SyntacticDifference, SortedSet<String>> findDifferenceCandidates(
      NetworkSnapshot snapshot, NetworkSnapshot reference) {
    SortedMap<String, Configuration> currentConfigs =
        _batfish.getProcessedConfigurations(snapshot).orElse(_batfish.loadConfigurations(snapshot));
    SortedMap<String, Configuration> referenceConfigs =
        _batfish
            .getProcessedConfigurations(reference)
            .orElse(_batfish.loadConfigurations(reference));

    // Routing policies that are potentially different. Maps them to the devices they live on.
    Map<SyntacticDifference, SortedSet<String>> candidates = new HashMap<>();

    // Go through every device in the current snapshot
    for (Map.Entry<String, Configuration> current : currentConfigs.entrySet()) {
      String router = current.getKey();
      Configuration currentConfig = current.getValue();
      Configuration refConfig = referenceConfigs.get(current.getKey());
      // If it is also present in the reference config
      if (refConfig != null) {
        // Find out what changed in the route-maps of this device.
        // NOTE: In the future, we might want to check other elements too, such as community-lists,
        // etc.
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
                          currentConfig,
                          refConfig,
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
                          currentConfig,
                          refConfig,
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

  /**
   * @param answer the differences for one policy as returned by CRP.
   * @return The differences formatted as a Markdown string.
   */
  private String tableAnswerToStringAnswer(TableAnswerElement answer) {
    StringBuilder result = new StringBuilder();
    TableMetadata metadata = answer.getMetadata();

    // 1st row of Markdown table (header)
    StringBuilder header = new StringBuilder();
    // 2nd row of Markdown table (header separator)
    StringBuilder header_div = new StringBuilder();
    header.append("| ");
    header_div.append("| ");
    for (ColumnMetadata column : metadata.getColumnMetadata()) {
      String name = prettyPrintName(column.getName());
      if (name.equals("Node")
          || name.equals("Policy_Name")
          || name.equals("Reference_Policy_Name")
          || name.equals("Differences")) {
        continue;
      }
      header.append(name).append(" | ");
      header_div.append("-".repeat(name.length())).append(" |");
    }

    // Add one more column to track the number of similar differences.
    header.append("# Similar Differences |");
    header.append("\n");
    header_div.append("-".repeat("# Similar Differences".length())).append(" |");
    header_div.append("\n");

    Map<String, ColumnMetadata> column_metadata = metadata.toColumnMap();

    // Maps differences to the number of times they have been encountered.
    // The key is an OutputRouteDifference, representing any differences in the action taken
    // (permit/deny) and the
    // route fields. Using SortedMap to ensure consistent ordering in answers.
    SortedMap<OutputRouteDifference, Integer> difference_freq = new TreeMap<>();

    // Maps each difference to the concrete examples we generated for it.
    SortedMap<OutputRouteDifference, List<DifferenceAnswer>> difference_map = new TreeMap<>();

    // Add header
    result.append(header);
    result.append(header_div);

    // Add each difference found.
    for (Row row : answer.getRowsList()) {
      BgpRoute snapshot_route =
          (BgpRoute)
              row.get(
                  "Snapshot_Output_Route",
                  column_metadata.get("Snapshot_Output_Route").getSchema());
      BgpRoute reference_route =
          (BgpRoute)
              row.get(
                  "Reference_Output_Route",
                  column_metadata.get("Reference_Output_Route").getSchema());

      StructuredBgpRouteDiffs route_delta = new StructuredBgpRouteDiffs();
      // NOTE: Rebuilding the differences instead of using the ones computed by TRP because we want
      // the structured differences.
      if ((snapshot_route != null) && (reference_route != null)) {
        route_delta = BgpRouteDiff.structuredRouteDiffs(reference_route, snapshot_route);
      }

      String reference_action = row.getString("Reference_Action");
      String snapshot_action = row.getString("Snapshot_Action");

      // Compute the key for this difference based on the route-map's action and the route
      // field differences.
      OutputRouteDifference diff_key =
          new OutputRouteDifference(
              route_delta, compute_action_difference(snapshot_action, reference_action));

      Integer count = difference_freq.getOrDefault(diff_key, 0);
      // Add 1 to the count of times we have seen this difference.
      difference_freq.put(diff_key, count + 1);

      if (count >= _maxSimilarDifferences) {
        _logger.info("Ignoring difference because we have already displayed a similar difference.");
        // If we have encountered this difference before then skip to the next one.
        continue;
      }

      BgpRoute input_route =
          (BgpRoute) row.get("Input_Route", column_metadata.get("Input_Route").getSchema());

      List<TraceTree> reference_trace =
          (List<TraceTree>)
              row.get("Reference_Trace", column_metadata.get("Reference_Trace").getSchema());
      List<TraceTree> snapshot_trace =
          (List<TraceTree>)
              row.get("Snapshot_Trace", column_metadata.get("Snapshot_Trace").getSchema());

      // Build the concrete example, including the input route that triggered it and the evaluation
      // traces.
      DifferenceAnswer d =
          new DifferenceAnswer(
              input_route,
              reference_action,
              snapshot_action,
              reference_trace,
              snapshot_trace,
              route_delta);

      List<DifferenceAnswer> differenceAnswers = difference_map.get(diff_key);
      if (differenceAnswers == null) {
        differenceAnswers = new ArrayList<>();
        difference_map.put(diff_key, differenceAnswers);
      }
      differenceAnswers.add(d);
    }

    // Print each concrete example along with the number of similar differences.
    for (Map.Entry<OutputRouteDifference, Integer> diff_entry : difference_freq.entrySet()) {
      OutputRouteDifference diff_key = diff_entry.getKey();
      Integer count = diff_entry.getValue();
      List<DifferenceAnswer> differenceAnswers = difference_map.get(diff_key);
      for (int i = 0; i < differenceAnswers.size(); i++) {
        result.append(differenceAnswers.get(i));
        result.append(" ").append(i == 0 ? count : "See above").append(" |\n");
      }
    }
    return result.toString();
  }

  private String prettyPrintName(String name) {
    switch (name) {
      case "Input_Route":
        return "Example Input Route";
      case "Snapshot_Action":
        return "Proposed Policy Action";
      case "Reference_Action":
        return "Current Policy Action";
      case "Snapshot_Output_Route":
        return "Proposed Policy Output";
      case "Reference_Output_Route":
        return "Current Policy Output";
      case "Snapshot_Trace":
        return "Proposed Policy Trace";
      case "Reference_Trace":
        return "Current Policy Trace";
      case "Difference":
        return "Differences";
      default:
        return name;
    }
  }
}
