package org.batfish.datamodel.questions;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.HashSet;
import java.util.Iterator;
import org.batfish.common.BatfishException;

public class Exclusions extends HashSet<ObjectNode> {

  private static final long serialVersionUID = 1L;

  /**
   * Checks if @{link row} is covered by one of the exlusions
   *
   * @param row The object whose coverage is to be checked
   * @return The exclusion that covers @{link row}. null if no such exclusion exists
   */
  public ObjectNode covered(ObjectNode row) {
    for (ObjectNode exclusion : this) {
      if (firstCoversSecond(exclusion, row)) {
        return exclusion;
      }
    }
    return null;
  }

  public static boolean firstCoversSecond(JsonNode first, JsonNode second) {
    if (first.isValueNode()) {
      return second.isValueNode() && first.equals(second);
    } else if (first.isArray()) {
      if (!second.isArray()) {
        return false;
      }
      for (JsonNode firstElement : first) {
        boolean covered = false;
        for (JsonNode secondElement : second) {
          if (firstCoversSecond(firstElement, secondElement)) {
            covered = true;
            break;
          }
        }
        if (!covered) {
          return false;
        }
      }
      // if we are here, all first elements must be covered
      return true;
    } else if (first.isObject()) {
      if (!second.isObject()) {
        return false;
      }
      Iterator<String> firstKeys = first.fieldNames();
      while (firstKeys.hasNext()) {
        String key = firstKeys.next();
        if (second.get(key) == null) {
          return false;
        }
        if (!firstCoversSecond(first.get(key), second.get(key))) {
          return false;
        }
      }
      // if we are here, all first keys must exist in second and first values cover second values
      return true;
    } else {
      throw new BatfishException("Missed some JsonNode type: " + first.getNodeType());
    }
  }
}
