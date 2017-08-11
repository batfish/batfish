package org.batfish.client.params;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import org.batfish.datamodel.Edge;

public class InitEnvironmentParams {

  private static final String PROP_DO_DELTA = "doDelta";

  private static final String PROP_EDGE_BLACKLIST = "edgeBlacklist";

  private static final String PROP_INTERFACE_BLACKLIST = "interfaceBlacklist";

  private static final String PROP_NEW_ENVIRONMENT_NAME = "newEnvironmentName";

  private static final String PROP_NEW_ENVIRONMENT_PREFIX = "newEnvironmentPrefix";

  private static final String PROP_NODE_BLACKLIST = "nodeBlacklist";

  private static final String PROP_SOURCE_ENVIRONMENT_NAME = "sourceEnvironmentName";

  private static final String PROP_SOURCE_PATH = "sourcePath";

  private boolean _doDelta;

  private SortedSet<Edge> _edgeBlacklist;

  private SortedMap<String, SortedSet<String>> _interfaceBlacklist;

  private String _newEnvironmentName;

  private String _newEnvironmentPrefix;

  private SortedSet<String> _nodeBlacklist;

  private String _sourceEnvironmentName;

  private String _sourcePath;

  @JsonCreator
  public InitEnvironmentParams() {
    _edgeBlacklist = new TreeSet<>();
    _interfaceBlacklist = new TreeMap<>();
    _nodeBlacklist = new TreeSet<>();
  }

  @JsonProperty(PROP_DO_DELTA)
  public boolean getDoDelta() {
    return _doDelta;
  }

  @JsonProperty(PROP_EDGE_BLACKLIST)
  public SortedSet<Edge> getEdgeBlacklist() {
    return _edgeBlacklist;
  }

  @JsonProperty(PROP_INTERFACE_BLACKLIST)
  public SortedMap<String, SortedSet<String>> getInterfaceBlacklist() {
    return _interfaceBlacklist;
  }

  @JsonProperty(PROP_NEW_ENVIRONMENT_NAME)
  public String getNewEnvironmentName() {
    return _newEnvironmentName;
  }

  @JsonProperty(PROP_NEW_ENVIRONMENT_PREFIX)
  public String getNewEnvironmentPrefix() {
    return _newEnvironmentPrefix;
  }

  @JsonProperty(PROP_NODE_BLACKLIST)
  public SortedSet<String> getNodeBlacklist() {
    return _nodeBlacklist;
  }

  @JsonProperty(PROP_SOURCE_ENVIRONMENT_NAME)
  public String getSourceEnvironmentName() {
    return _sourceEnvironmentName;
  }

  @JsonProperty(PROP_SOURCE_PATH)
  public String getSourcePath() {
    return _sourcePath;
  }

  @JsonProperty(PROP_DO_DELTA)
  public void setDoDelta(boolean doDelta) {
    _doDelta = doDelta;
  }

  @JsonProperty(PROP_EDGE_BLACKLIST)
  public void setEdgeBlacklist(SortedSet<Edge> edgeBlacklist) {
    _edgeBlacklist = edgeBlacklist;
  }

  @JsonProperty(PROP_INTERFACE_BLACKLIST)
  public void setInterfaceBlacklist(SortedMap<String, SortedSet<String>> interfaceBlacklist) {
    _interfaceBlacklist = interfaceBlacklist;
  }

  @JsonProperty(PROP_NEW_ENVIRONMENT_NAME)
  public void setNewEnvironmentName(String newEnvironmentName) {
    _newEnvironmentName = newEnvironmentName;
  }

  @JsonProperty(PROP_NEW_ENVIRONMENT_PREFIX)
  public void setNewEnvironmentPrefix(String newEnvironmentPrefix) {
    _newEnvironmentPrefix = newEnvironmentPrefix;
  }

  @JsonProperty(PROP_NODE_BLACKLIST)
  public void setNodeBlacklist(SortedSet<String> nodeBlacklist) {
    _nodeBlacklist = nodeBlacklist;
  }

  @JsonProperty(PROP_SOURCE_ENVIRONMENT_NAME)
  public void setSourceEnvironmentName(String sourceEnvironmentName) {
    _sourceEnvironmentName = sourceEnvironmentName;
  }

  @JsonProperty(PROP_SOURCE_PATH)
  public void setSourcePath(String sourcePath) {
    _sourcePath = sourcePath;
  }
}
