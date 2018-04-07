package org.batfish.common.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.Configuration.ConfigurationBuilder;
import com.jayway.jsonpath.Configuration.Defaults;
import com.jayway.jsonpath.InvalidPathException;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.PathNotFoundException;
import com.jayway.jsonpath.spi.json.JacksonJsonNodeJsonProvider;
import com.jayway.jsonpath.spi.json.JsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;
import com.jayway.jsonpath.spi.mapper.MappingProvider;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import org.batfish.common.BatfishException;

/** Contains a few helper functions for applying JsonPath expressions to JSON strings and objects */
public final class JsonPathUtils {

  public static class BatfishJsonPathDefaults implements Defaults {

    public static final BatfishJsonPathDefaults INSTANCE = new BatfishJsonPathDefaults();

    private BatfishJsonPathDefaults() {}

    public static Configuration getDefaulConfiguration() {
      Configuration.setDefaults(BatfishJsonPathDefaults.INSTANCE);
      ConfigurationBuilder b = new ConfigurationBuilder();
      final Configuration c = b.build();
      return c;
    }

    @Override
    public JsonProvider jsonProvider() {
      return new JacksonJsonNodeJsonProvider();
    }

    @Override
    public MappingProvider mappingProvider() {
      return new JacksonMappingProvider();
    }

    @Override
    public Set<Option> options() {
      return EnumSet.noneOf(Option.class);
    }
  }

  private JsonPathUtils() {}

  /**
   * Produces the results of applying a function-based (e.g., length()) JsonPath query to Json
   * content.
   *
   * @param queryPath The JsonPath query
   * @param content The Json content
   * @return An @{link Object} that represents the answer
   */
  public static Object computePathFunction(String queryPath, String content) {
    return computePathFunctionPvt(
        queryPath,
        JsonPath.parse(content, BatfishJsonPathDefaults.getDefaulConfiguration()).json());
  }

  /**
   * Produces the results of applying a function-based (e.g., length()) JsonPath query to Json
   * content.
   *
   * @param queryPath The JsonPath query
   * @param content The Json content
   * @return An @{link Object} that represents the answer
   */
  public static Object computePathFunction(String queryPath, JsonNode content) {
    return computePathFunctionPvt(
        queryPath,
        JsonPath.parse(content, BatfishJsonPathDefaults.getDefaulConfiguration()).json());
  }

  // the 'Pvt' suffix prevents ambiguous calls
  private static Object computePathFunctionPvt(String queryPath, Object jsonObject) {
    ConfigurationBuilder cb = new ConfigurationBuilder();
    Configuration configuration = cb.build();

    JsonPath jsonPath;
    try {
      jsonPath = JsonPath.compile(queryPath);
    } catch (InvalidPathException e) {
      throw new BatfishException("Invalid JsonPath: " + queryPath, e);
    }

    try {
      return jsonPath.read(jsonObject, configuration);
    } catch (PathNotFoundException e) {
      return null;
    } catch (Exception e) {
      throw new BatfishException("Error reading JSON path: " + jsonPath, e);
    }
  }

  /**
   * Produces the results of applying JsonPath query to Json content. This function does not support
   * arithmetic functions (e.g., length() in queries.
   *
   * @param queryPath The JsonPath query
   * @param content The Json content
   * @return A list of @{link JsonPathResult} entities
   */
  public static List<JsonPathResult> getJsonPathResults(String queryPath, String content) {
    return getJsonPathResultsPvt(
        queryPath,
        JsonPath.parse(content, BatfishJsonPathDefaults.getDefaulConfiguration()).json());
  }

  /**
   * Produces the results of applying JsonPath query to Json content. This function does not support
   * arithmetic functions (e.g., length() in queries.
   *
   * @param queryPath The JsonPath query
   * @param content The Json content
   * @return A list of @{link JsonPathResult} entities
   */
  public static List<JsonPathResult> getJsonPathResults(String queryPath, JsonNode content) {
    return getJsonPathResultsPvt(
        queryPath,
        JsonPath.parse(content, BatfishJsonPathDefaults.getDefaulConfiguration()).json());
  }

  // the 'Pvt' suffix prevents ambiguous calls
  private static List<JsonPathResult> getJsonPathResultsPvt(String queryPath, Object jsonObject) {
    ConfigurationBuilder prefixCb = new ConfigurationBuilder();
    prefixCb.options(Option.ALWAYS_RETURN_LIST);
    prefixCb.options(Option.AS_PATH_LIST);
    Configuration prefixC = prefixCb.build();

    ConfigurationBuilder suffixCb = new ConfigurationBuilder();
    suffixCb.options(Option.ALWAYS_RETURN_LIST);
    Configuration suffixC = suffixCb.build();

    ArrayNode prefixes = null;
    ArrayNode suffixes = null;
    JsonPath jsonPath;
    try {
      jsonPath = JsonPath.compile(queryPath);
    } catch (InvalidPathException e) {
      throw new BatfishException("Invalid JsonPath: " + queryPath, e);
    }

    try {
      prefixes = jsonPath.read(jsonObject, prefixC);
      suffixes = jsonPath.read(jsonObject, suffixC);
    } catch (PathNotFoundException e) {
      suffixes = JsonNodeFactory.instance.arrayNode();
      prefixes = JsonNodeFactory.instance.arrayNode();
    } catch (Exception e) {
      throw new BatfishException("Error reading JSON path: " + queryPath, e);
    }

    List<JsonPathResult> results = new LinkedList<>();
    Iterator<JsonNode> p = prefixes.iterator();
    Iterator<JsonNode> s = suffixes.iterator();
    while (p.hasNext()) {
      JsonNode prefix = p.next();
      JsonNode suffix = s.next();
      results.add(new JsonPathResult(prefix, suffix));
    }

    return results;
  }
}
