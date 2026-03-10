package org.batfish.datamodel.questions;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.Iterator;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.common.BatfishException;
import org.batfish.datamodel.table.Row;

/**
 * Describes rows to be excluded in a {@link org.batfish.datamodel.table.TableAnswerElement}. An
 * {@link Exclusion} may "cover" multiple rows and cause them to be excluded.
 */
public class Exclusion {
  private static final String PROP_NAME = "name";
  private static final String PROP_SPECIFICATION = "specification";

  final @Nonnull String _name;

  final @Nonnull ObjectNode _specification;

  @JsonCreator
  public Exclusion(
      @JsonProperty(PROP_NAME) @Nullable String name,
      @JsonProperty(PROP_SPECIFICATION) @Nonnull ObjectNode exclusion) {
    _name = name == null ? exclusion.toString() : name;
    _specification = exclusion;
  }

  /**
   * Checks if {@code row} is covered by one of the exlusions
   *
   * @param row The object whose coverage is to be checked
   * @return The exclusion that covers {@code row}. null if no such exclusion exists
   */
  public static Exclusion covered(Row row, List<Exclusion> exclusions) {
    if (exclusions == null) {
      return null;
    }
    for (Exclusion exclusion : exclusions) {
      if (row.isCovered(exclusion.getExclusion())) {
        return exclusion;
      }
    }
    return null;
  }

  /**
   * Evaluates if the first object covers the second object. For it to be true, the two objects
   * should be of the same type and
   *
   * <p>i) If the type is atomic (e.g., string), should have equal values
   *
   * <p>ii) If the type is list, each element in the first object should cover some element in the
   * second. ['a'] covers ['a', 'b'] but not the other way around.
   *
   * <p>iii) If the type is map, each key in the first object should be present in the second object
   * and the corresponding first value should cover the second value. {'k1' : 'v1' } covers {'k1' :
   * 'v1', 'k2': 'v2'} but not the other way around.
   *
   * @param first The first object
   * @param second The second object
   * @return Results of the evaluation
   */
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

  @JsonProperty(PROP_SPECIFICATION)
  public ObjectNode getExclusion() {
    return _specification;
  }

  @JsonProperty(PROP_NAME)
  public String getName() {
    return _name;
  }
}
