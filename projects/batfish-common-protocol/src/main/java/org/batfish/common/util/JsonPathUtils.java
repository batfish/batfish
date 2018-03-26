package org.batfish.common.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.Configuration.ConfigurationBuilder;
import com.jayway.jsonpath.InvalidPathException;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.PathNotFoundException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.batfish.common.BatfishException;

public class JsonPathUtils {

  public static Object computePathFunction(String queryPath, Object jsonObject) {
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

  public static List<JsonPathResult> getJsonPathResults(String queryPath, Object jsonObject) {
    ConfigurationBuilder prefixCb = new ConfigurationBuilder();
    prefixCb.options(Option.ALWAYS_RETURN_LIST);
    prefixCb.options(Option.AS_PATH_LIST);
    com.jayway.jsonpath.Configuration prefixC = prefixCb.build();

    ConfigurationBuilder suffixCb = new ConfigurationBuilder();
    suffixCb.options(Option.ALWAYS_RETURN_LIST);
    com.jayway.jsonpath.Configuration suffixC = suffixCb.build();

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
