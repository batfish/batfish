package projects.minesweeper.src.main.java.org.batfish.minesweeper.question.semdiff;

import static org.batfish.datamodel.BgpRoute.PROP_COMMUNITIES;
import static org.batfish.datamodel.questions.BgpRoute.PROP_AS_PATH;
import static org.batfish.datamodel.questions.BgpRoute.PROP_LOCAL_PREFERENCE;
import static org.batfish.datamodel.questions.BgpRoute.PROP_METRIC;
import static org.batfish.datamodel.questions.BgpRoute.PROP_NEXT_HOP_IP;
import static org.batfish.datamodel.questions.BgpRoute.PROP_ORIGIN_TYPE;
import static org.batfish.datamodel.questions.BgpRoute.PROP_TAG;
import static org.batfish.datamodel.questions.BgpRoute.PROP_WEIGHT;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.SortedSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import org.batfish.datamodel.bgp.community.Community;
import org.batfish.datamodel.questions.BgpRoute;
import org.batfish.datamodel.questions.BgpRouteCommunityDiff;
import org.batfish.datamodel.questions.BgpRouteDiff;
import org.batfish.datamodel.questions.StructuredBgpRouteDiffs;
import org.batfish.datamodel.trace.TraceTree;

/** This class describes a concrete example of a difference in the output of two route-maps. */
public class DifferenceAnswer {

  /** An input route that triggers the difference */
  private final BgpRoute _inputRoute;

  /** The final route-map action on the reference snapshot - Permit/Deny */
  private final String _referenceAction;
  /** The final route-map action on the current snapshot - Permit/Deny */
  private final String _snapshotAction;
  /** The evaluation trace for the reference snapshot */
  private final List<TraceTree> _referenceTrace;
  /** The evaluation trace for the current snapshot */
  private final List<TraceTree> _snapshotTrace;
  /** The set of differences between the reference and current outputs. */
  private final StructuredBgpRouteDiffs _diff;

  public DifferenceAnswer(
      BgpRoute inputRoute,
      String referenceAction,
      String snapshotAction,
      List<TraceTree> referenceTrace,
      List<TraceTree> snapshotTrace,
      StructuredBgpRouteDiffs diff) {
    this._inputRoute = inputRoute;
    this._referenceAction = referenceAction;
    this._snapshotAction = snapshotAction;
    this._referenceTrace = referenceTrace;
    this._snapshotTrace = snapshotTrace;
    this._diff = diff;
  }

  @Override
  public String toString() {
    StringBuilder result = new StringBuilder();
    boolean same_action = _referenceAction.equals(_snapshotAction);

    result
        .append("| ")
        .append(prettyPrintInputRoute(_inputRoute))
        .append(" | ")
        .append(same_action ? _snapshotAction : "**" + _snapshotAction + "**")
        .append(" | ")
        .append(same_action ? _referenceAction : "**" + _referenceAction + "**")
        .append(" | ")
        .append(prettyPrintOutputRoute(false, _diff))
        .append(" | ")
        .append(prettyPrintOutputRoute(true, _diff))
        .append(" | ")
        .append(prettyPrintTrace(_snapshotTrace))
        .append(" | ")
        .append(prettyPrintTrace(_referenceTrace))
        .append(" |");
    return result.toString();
  }

  /**
   * @param route the input route to print
   * @return The input route in string form ignoring some fields that cannot be matched on such as
   *     the local-preference, the next-hop, and origin type.
   */
  private static String prettyPrintInputRoute(BgpRoute route) {
    StringBuilder result = new StringBuilder();
    result
        .append("_network_ = ")
        .append(route.getNetwork())
        .append(", _metric_ = ")
        .append(route.getMetric())
        .append(", _asPath_ = ")
        .append(prettyPrintAsPath(route.getAsPath().getAsPathString()))
        .append(", _communities_ = ")
        .append(route.getCommunities())
        .append(", _tag_ = ")
        .append(route.getTag())
        .append(", _weight_ = ")
        .append(route.getWeight());
    return result.toString();
  }

  private static String prettyPrintAsPath(String asPath) {
    return asPath.isEmpty() ? "\"\"" : asPath;
  }

  /**
   * Pretty-prints the output routes.
   *
   * @param isReference true if this is the output route of the reference route - used for
   *     pretty-printing communities.
   * @param differences - the differences between the reference/current route.
   * @return A string that includes the fields that are different between the snapshot
   */
  private static String prettyPrintOutputRoute(
      boolean isReference, StructuredBgpRouteDiffs differences) {
    // Note: with the current printing (where we only print fields that changed) we do not really
    // need the route as an argument.
    if (differences.get_diffs().isEmpty() && !differences.get_communityDiff().isPresent()) {
      // The difference is due to the action not one the route fields.
      return "--";
    } else {
      StringBuilder result = new StringBuilder();

      // Collect each different field as strings (empty string if the fields do not differ).
      String metric = includeField(PROP_METRIC, isReference, differences);
      String as_path = includeField(PROP_AS_PATH, isReference, differences);
      String comms = includeCommunities(isReference, differences);
      String lp = includeField(PROP_LOCAL_PREFERENCE, isReference, differences);
      String nexthop = includeField(PROP_NEXT_HOP_IP, isReference, differences);
      String origin_type = includeField(PROP_ORIGIN_TYPE, isReference, differences);
      String tag = includeField(PROP_TAG, isReference, differences);
      String weight = includeField(PROP_WEIGHT, isReference, differences);

      // Remove any field that is empty.
      List<String> elements =
          Stream.of(metric, as_path, comms, lp, nexthop, origin_type, tag, weight)
              .filter(s -> !s.isEmpty())
              .collect(Collectors.toList());

      // Print fields, comma-separated.
      for (int i = 0; i < elements.size(); i++) {
        result.append(elements.get(i));
        if (i < elements.size() - 1) {
          result.append(", ");
        }
      }
      return result.toString();
    }
  }

  /** Pretty prints a route-map trace. Assumes there are no route-map calls. */
  private static String prettyPrintTrace(List<TraceTree> trace) {
    StringBuilder result = new StringBuilder();
    for (int i = 0; i < trace.size(); i++) {
      TraceTree t = trace.get(i);
      String fragment = t.getTraceElement().getFragments().get(1).getText();
      String[] fragments = fragment.split(" ");
      String rm_name = fragments[1];
      String entry = fragments[3];
      if (result.length() == 0) {
        result.append(rm_name).append(":[");
      }
      result.append(entry);
      if (i < trace.size() - 1) {
        result.append(", ");
      }
    }

    // If the trace was empty
    if (result.length() == 0) {
      result.append("[");
    }

    result.append("]");
    return result.toString();
  }

  /**
   * @param field The field to print
   * @param isReference Whether we are printing the reference route or the current route.
   * @param differences The differences between the two output routes
   * @return The empty string if there is no difference on the given field, or the field and its
   *     value if there is a difference.
   */
  private static String includeField(
      String field, boolean isReference, @Nullable StructuredBgpRouteDiffs differences) {
    if (differences != null
        && differences.get_diffs().stream().anyMatch(d -> d.getFieldName().equals(field))) {
      Optional<BgpRouteDiff> diff =
          differences.get_diffs().stream().filter(d -> d.getFieldName().equals(field)).findFirst();
      if (diff.isPresent()) {
        // Print the old or new value depending on whether we are outputting the reference or
        // current snapshot.
        String value = isReference ? diff.get().getOldValue() : diff.get().getNewValue();
        if (field.equals(PROP_AS_PATH)) {
          value = prettyPrintAsPath(value);
        }
        return "_" + field + "_ = " + value;
      }
    }
    return "";
  }

  /**
   * @param isReference true if this field is on the reference route
   * @param differences the route community differences
   * @return The communities field of a BGP route, where communities are included if they are part
   *     of the difference.
   */
  private static String includeCommunities(
      boolean isReference, @Nullable StructuredBgpRouteDiffs differences) {
    if (differences == null) {
      return "";
    }

    Optional<BgpRouteCommunityDiff> diff = differences.get_communityDiff();
    if (diff.isPresent()) {
      BgpRouteCommunityDiff cdiff = diff.get();
      SortedSet<Community> value = isReference ? cdiff.getOldValue() : cdiff.getNewValue();
      // If there is a difference on the communities then highlight the difference.
      // If this is the reference route then we want to highlight the communities removed, otherwise
      // for the proposed route we highlight the communities added.
      SortedSet<Community> diffCommunities = isReference ? cdiff.getRemoved() : cdiff.getAdded();
      StringBuilder result = new StringBuilder();
      result.append("_" + PROP_COMMUNITIES + "_ = ");
      result.append("[");
      Iterator<Community> iter = value.iterator();
      while (iter.hasNext()) {
        Community comm = iter.next();

        // Highlight community if it's part of the difference in the field.
        if (diffCommunities.contains(comm)) {
          result.append("**").append(comm.toString()).append("**");
        } else {
          result.append(comm.toString());
        }
        // Add separator if there are more elements.
        if (iter.hasNext()) {
          result.append(", ");
        }
      }
      result.append("]");
      return result.toString();
    } else {
      // The communities were the same.
      return "";
    }
  }
}
