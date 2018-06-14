package org.batfish.datamodel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.NavigableMap;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import org.batfish.common.util.CommonUtil;
import org.batfish.datamodel.answers.AnswerElement;

public class ConfigDiffElement extends AnswerElement {

  private static final String PROP_DIFF_INFO = "diffInfo";

  private static final String PROP_DIFF = "diff";

  // private static final String PROP_IDENTICAL = "identical";

  private static final String PROP_IN_AFTER_ONLY = "inAfterOnly";

  private static final String PROP_IN_BEFORE_ONLY = "inBeforeOnly";

  private static final int MAX_IDENTICAL = 5;

  private Set<String> _after;

  private Set<String> _before;

  protected SortedSet<String> _diff;

  protected SortedMap<String, AnswerElement> _diffInfo;

  protected SortedSet<String> _identical;

  protected SortedSet<String> _inAfterOnly;

  protected SortedSet<String> _inBeforeOnly;

  @JsonCreator
  public ConfigDiffElement() {}

  /**
   * Basic diff only comparing names and with optional summary
   *
   * @param before The base element
   * @param after The delta element
   */
  protected <T> ConfigDiffElement(
      NavigableMap<String, T> before, NavigableMap<String, T> after, boolean summarizeIdentical) {
    this(before.keySet(), after.keySet());
    for (String name : common()) {
      if (!skip(name)) {
        T beforeCurrent = before.get(name);
        T afterCurrent = after.get(name);
        if (beforeCurrent.equals(afterCurrent)) {
          _identical.add(name);
        } else {
          _diff.add(name);
        }
      }
    }
    if (summarizeIdentical) {
      summarizeIdentical();
    }
  }

  public ConfigDiffElement(Set<String> before, Set<String> after) {
    _before = before;
    _after = after;
    _diff = new TreeSet<>();
    _diffInfo = new TreeMap<>();
    _identical = new TreeSet<>();
    _inBeforeOnly = CommonUtil.difference(before, after, TreeSet::new);
    _inAfterOnly = CommonUtil.difference(after, before, TreeSet::new);
  }

  public final Set<String> common() {
    return CommonUtil.intersection(_before, _after, TreeSet::new);
  }

  @JsonProperty(PROP_DIFF)
  public SortedSet<String> getDiff() {
    return _diff;
  }

  @JsonProperty(PROP_DIFF_INFO)
  public SortedMap<String, AnswerElement> getDiffInfo() {
    return _diffInfo;
  }

  // @JsonProperty(PROP_IDENTICAL)
  @JsonIgnore
  public SortedSet<String> getIdentical() {
    return _identical;
  }

  @JsonProperty(PROP_IN_AFTER_ONLY)
  public SortedSet<String> getInAfterOnly() {
    return _inAfterOnly;
  }

  @JsonProperty(PROP_IN_BEFORE_ONLY)
  public SortedSet<String> getInBeforeOnly() {
    return _inBeforeOnly;
  }

  @JsonIgnore
  public boolean isEmpty() {
    return _diff.isEmpty()
        && _diffInfo.isEmpty()
        && _inBeforeOnly.isEmpty()
        && _inAfterOnly.isEmpty();
  }

  @JsonProperty(PROP_DIFF)
  public void setDiff(SortedSet<String> diff) {
    _diff = diff;
  }

  @JsonProperty(PROP_DIFF_INFO)
  public void setDiffInfo(SortedMap<String, AnswerElement> diffInfo) {
    _diffInfo = diffInfo;
  }

  // @JsonProperty(PROP_IDENTICAL)
  @JsonIgnore
  public void setIdentical(SortedSet<String> identical) {
    _identical = identical;
  }

  @JsonProperty(PROP_IN_AFTER_ONLY)
  public void setInAfterOnly(SortedSet<String> inAfterOnly) {
    _inAfterOnly = inAfterOnly;
  }

  @JsonProperty(PROP_IN_BEFORE_ONLY)
  public void setInBeforeOnly(SortedSet<String> inBeforeOnly) {
    _inBeforeOnly = inBeforeOnly;
  }

  protected boolean skip(String name) {
    return false;
  }

  protected final void summarizeIdentical() {
    int numIdentical = _identical.size();
    if (numIdentical > MAX_IDENTICAL) {
      _identical = new TreeSet<>();
      _identical.add(numIdentical + " identical elements not shown for readability.");
    }
  }
}
