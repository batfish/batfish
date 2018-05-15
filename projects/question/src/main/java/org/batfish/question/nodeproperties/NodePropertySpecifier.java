package org.batfish.question.nodeproperties;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.function.Function;
import org.batfish.datamodel.Configuration;

/**
 * Enables specification a set of node properties.
 *
 * <p>Currently supported example specifier:
 *
 * <ul>
 *   <li>ntp-servers —> gets NTP servers using a configured Java function
 * </ul>
 *
 * <p>In the future, we might add other specifier types, e.g., those based on Json Path
 */
public class NodePropertySpecifier {

  static Map<String, Function<Configuration, Object>> JAVA_MAP =
      new ImmutableMap.Builder<String, Function<Configuration, Object>>()
          .put("interfaces", Configuration::getInterfaces)
          .put("ntpServers", Configuration::getNtpServers)
          .put("ntpSourceInterface", Configuration::getNtpSourceInterface)
          .build();

  private final String _expression;

  @JsonCreator
  public NodePropertySpecifier(String expression) {
    _expression = expression.trim();

    if (!JAVA_MAP.containsKey(expression)) {
      throw new IllegalArgumentException(
          "Invalid node property specification: '" + expression + "'");
    }
  }

  @Override
  @JsonValue
  public String toString() {
    return _expression;
  }
}
