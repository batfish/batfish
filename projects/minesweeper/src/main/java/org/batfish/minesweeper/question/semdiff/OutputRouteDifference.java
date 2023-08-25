package projects.minesweeper.src.main.java.org.batfish.minesweeper.question.semdiff;

import java.util.Iterator;
import org.batfish.common.BatfishException;
import org.batfish.datamodel.questions.BgpRouteDiff;
import org.batfish.datamodel.questions.StructuredBgpRouteDiffs;

/**
 * This class describes the differences between two output routes, including the action of the
 * routing policy that processed them and any field differences.
 */
public class OutputRouteDifference implements Comparable<OutputRouteDifference> {
  private final StructuredBgpRouteDiffs _fieldDifferences;
  private final ActionDifference _actionDifference;

  public OutputRouteDifference(
      StructuredBgpRouteDiffs fieldDifferences, ActionDifference actionDifference) {
    this._fieldDifferences = fieldDifferences;
    this._actionDifference = actionDifference;
  }

  public static ActionDifference compute_action_difference(String current, String reference) {
    if (current.equals("PERMIT") && reference.equals("PERMIT")) {
      return ActionDifference.PermitPermit;
    }
    if (current.equals("PERMIT") && reference.equals("DENY")) {
      return ActionDifference.ReferenceDenyCurrentPermit;
    }
    if (current.equals("DENY") && reference.equals("PERMIT")) {
      return ActionDifference.ReferencePermitCurrentDeny;
    }
    throw new BatfishException("Unexpected route actions.");
  }

  @Override
  public int compareTo(OutputRouteDifference o) {
    // First sort based on the action
    int a = this._actionDifference.compareTo(o._actionDifference);
    if (a != 0) {
      return a;
    }

    // Then based on the route differences
    Iterator<BgpRouteDiff> otherI = o._fieldDifferences.toBgpRouteDiffs().getDiffs().iterator();
    for (BgpRouteDiff thisDiff : _fieldDifferences.toBgpRouteDiffs().getDiffs()) {
      if (!otherI.hasNext()) {
        return 1;
      }
      int comparison = thisDiff.compareTo(otherI.next());
      if (comparison != 0) {
        return comparison;
      }
    }
    if (otherI.hasNext()) {
      return -1;
    } else {
      return 0;
    }
  }
}
