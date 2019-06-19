package org.batfish.datamodel.answers;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;

public class Problem implements Serializable {
  private static final String PROP_DESCRIPTION = "description";
  private static final String PROP_FILES = "files";

  private static final long serialVersionUID = 1L;

  private String _description;

  private SortedMap<String, SortedSet<Integer>> _files;

  public Problem() {
    _files = new TreeMap<>();
  }

  @JsonProperty(PROP_DESCRIPTION)
  public String getDescription() {
    return _description;
  }

  @JsonProperty(PROP_FILES)
  public SortedMap<String, SortedSet<Integer>> getFiles() {
    return _files;
  }

  @JsonProperty(PROP_DESCRIPTION)
  public void setDescription(String description) {
    _description = description;
  }

  @JsonProperty(PROP_FILES)
  public void setFiles(SortedMap<String, SortedSet<Integer>> files) {
    _files = files;
  }
}
