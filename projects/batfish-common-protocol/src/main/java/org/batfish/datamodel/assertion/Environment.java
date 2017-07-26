package org.batfish.datamodel.assertion;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import java.util.concurrent.ConcurrentMap;
import org.batfish.common.BatfishException;
import org.batfish.common.plugin.IBatfish;

public class Environment {

  private IBatfish _batfish;

  private Configuration _configuration;

  private final Object _jsonObject;

  private ConcurrentMap<String, ArrayNode> _pathCache;

  public Environment(
      IBatfish batfish,
      Object jsonObject,
      ConcurrentMap<String, ArrayNode> pathCache,
      Configuration c) {
    _batfish = batfish;
    _pathCache = pathCache;
    _jsonObject = jsonObject;
    _configuration = c;
  }

  public ArrayNode computePath(String path) {
    ArrayNode pathResult = _pathCache.get(path);
    if (pathResult == null) {
      JsonPath jsonPath = JsonPath.compile(path);

      try {
        pathResult = jsonPath.read(_jsonObject, _configuration);
      } catch (PathNotFoundException e) {
        pathResult = JsonNodeFactory.instance.arrayNode();
      } catch (Exception e) {
        throw new BatfishException("Error reading JSON path: " + path, e);
      }
      _pathCache.put(path, pathResult);
    }
    return pathResult;
  }

  public IBatfish getBatfish() {
    return _batfish;
  }

  public Object getJsonObject() {
    return _jsonObject;
  }
}
