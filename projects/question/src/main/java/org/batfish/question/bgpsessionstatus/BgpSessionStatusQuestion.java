package org.batfish.question.bgpsessionstatus;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Strings;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.batfish.datamodel.questions.NodesSpecifier;
import org.batfish.datamodel.questions.Question;
import org.batfish.question.bgpsessionstatus.BgpSessionInfo.SessionStatus;
import org.batfish.question.bgpsessionstatus.BgpSessionInfo.SessionType;

/**
 * Returns the status of BGP sessions.
 *
 * <p>Based on config data, determines the status of IBGP and EBGP sessions
 *
 * @type BgpSessionCheck multifile
 * @param foreignBgpGroups Details coming.
 * @param node1Regex Regular expression to match the nodes names for one end of the sessions.
 *     Default is '.*' (all nodes).
 * @param node2Regex Regular expression to match the nodes names for the other end of the sessions.
 *     Default is '.*' (all nodes).
 * @example bf_answer("BgpSessionCheck", node1Regex="as1.*", node2Regex="as2.*") Checks all BGP
 *     sessions between nodes that start with as1 and those that start with as2.
 */
public class BgpSessionStatusQuestion extends Question {

  private static final String PROP_FOREIGN_BGP_GROUPS = "foreignBgpGroups";

  private static final String PROP_INCLUDE_DYNAMIC_COUNT = "includeDynamicCount";

  private static final String PROP_NODE1_REGEX = "node1Regex";

  private static final String PROP_NODE2_REGEX = "node2Regex";

  private static final String PROP_STATUS = "status";

  private static final String PROP_TYPE_REGEX = "type";

  @Nonnull private SortedSet<String> _foreignBgpGroups;

  @Nonnull private boolean _includeDynamicCount;

  @Nonnull private NodesSpecifier _node1Regex;

  @Nonnull private NodesSpecifier _node2Regex;

  @Nonnull private Pattern _statusRegex;

  @Nonnull private Pattern _typeRegex;

  public BgpSessionStatusQuestion(
      @JsonProperty(PROP_FOREIGN_BGP_GROUPS) SortedSet<String> foreignBgpGroups,
      @JsonProperty(PROP_INCLUDE_DYNAMIC_COUNT) Boolean includeDynamicCount,
      @JsonProperty(PROP_NODE1_REGEX) NodesSpecifier regex1,
      @JsonProperty(PROP_NODE2_REGEX) NodesSpecifier regex2,
      @JsonProperty(PROP_STATUS) String statusRegex,
      @JsonProperty(PROP_TYPE_REGEX) String type) {
    _foreignBgpGroups = foreignBgpGroups == null ? new TreeSet<>() : foreignBgpGroups;
    _includeDynamicCount = includeDynamicCount != null && includeDynamicCount;
    _node1Regex = regex1 == null ? NodesSpecifier.ALL : regex1;
    _node2Regex = regex2 == null ? NodesSpecifier.ALL : regex2;
    _statusRegex =
        Strings.isNullOrEmpty(statusRegex)
            ? Pattern.compile(".*")
            : Pattern.compile(statusRegex.toUpperCase());
    _typeRegex =
        Strings.isNullOrEmpty(type) ? Pattern.compile(".*") : Pattern.compile(type.toUpperCase());
  }

  @Override
  public boolean getDataPlane() {
    return _includeDynamicCount;
  }

  @JsonProperty(PROP_FOREIGN_BGP_GROUPS)
  private SortedSet<String> getForeignBgpGroups() {
    return _foreignBgpGroups;
  }

  @JsonProperty(PROP_INCLUDE_DYNAMIC_COUNT)
  public boolean getIncludeDynamicCount() {
    return _includeDynamicCount;
  }

  @Override
  public String getName() {
    return "bgpsessionstatusnew";
  }

  @JsonProperty(PROP_NODE1_REGEX)
  public NodesSpecifier getNode1Regex() {
    return _node1Regex;
  }

  @JsonProperty(PROP_NODE2_REGEX)
  public NodesSpecifier getNode2Regex() {
    return _node2Regex;
  }

  @JsonProperty(PROP_STATUS)
  private String getStatusRegex() {
    return _statusRegex.toString();
  }

  @JsonProperty(PROP_TYPE_REGEX)
  private String getTypeRegex() {
    return _typeRegex.toString();
  }

  boolean matchesForeignGroup(String group) {
    return _foreignBgpGroups.contains(group);
  }

  boolean matchesStatus(SessionStatus status) {
    return _statusRegex.matcher(status.toString()).matches();
  }

  boolean matchesType(SessionType type) {
    return _typeRegex.matcher(type.toString()).matches();
  }
}
