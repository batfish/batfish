package org.batfish.question.bgpsessionstatus;

import static com.google.common.base.MoreObjects.firstNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSortedSet;
import java.util.SortedSet;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.BgpSessionProperties.SessionType;
import org.batfish.datamodel.questions.NodesSpecifier;
import org.batfish.datamodel.questions.Question;
import org.batfish.question.bgpsessionstatus.BgpSessionInfo.SessionStatus;

/** Based on node configurations, determines the status of IBGP and EBGP sessions. */
public class BgpSessionStatusQuestion extends Question {

  private static final String PROP_FOREIGN_BGP_GROUPS = "foreignBgpGroups";

  private static final String PROP_INCLUDE_ESTABLISHED_COUNT = "includeEstablishedCount";

  private static final String PROP_NODE1_REGEX = "node1Regex";

  private static final String PROP_NODE2_REGEX = "node2Regex";

  private static final String PROP_STATUS = "status";

  private static final String PROP_TYPE_REGEX = "type";

  @Nonnull private SortedSet<String> _foreignBgpGroups;

  private boolean _includeEstablishedCount;

  @Nonnull private NodesSpecifier _node1Regex;

  @Nonnull private NodesSpecifier _node2Regex;

  @Nonnull private Pattern _statusRegex;

  @Nonnull private Pattern _typeRegex;

  /** Create a new BGP Session question with default parameters. */
  public BgpSessionStatusQuestion() {
    this(null, false, null, null, null, null);
  }

  /**
   * Create a new BGP Session question.
   *
   * @param foreignBgpGroups only look at peers that belong to a given named BGP group.
   * @param includeEstablishedCount run post-dataplane analysis to see how many seesions get
   *     actually established.
   * @param regex1 Regular expression to match the nodes names for one end of the sessions. Default
   *     is '.*' (all nodes).
   * @param regex2 Regular expression to match the nodes names for the other end of the sessions.
   *     Default is '.*' (all nodes).
   * @param statusRegex Regular expression to match status type (see {@link
   *     BgpSessionInfo.SessionStatus})
   * @param type Regular expression to match session type (see {@link SessionType})
   */
  @JsonCreator
  public BgpSessionStatusQuestion(
      @Nullable @JsonProperty(PROP_FOREIGN_BGP_GROUPS) SortedSet<String> foreignBgpGroups,
      @Nullable @JsonProperty(PROP_INCLUDE_ESTABLISHED_COUNT) Boolean includeEstablishedCount,
      @Nullable @JsonProperty(PROP_NODE1_REGEX) NodesSpecifier regex1,
      @Nullable @JsonProperty(PROP_NODE2_REGEX) NodesSpecifier regex2,
      @Nullable @JsonProperty(PROP_STATUS) String statusRegex,
      @Nullable @JsonProperty(PROP_TYPE_REGEX) String type) {
    _foreignBgpGroups = firstNonNull(foreignBgpGroups, ImmutableSortedSet.of());
    _includeEstablishedCount = firstNonNull(includeEstablishedCount, Boolean.FALSE);
    _node1Regex = firstNonNull(regex1, NodesSpecifier.ALL);
    _node2Regex = firstNonNull(regex2, NodesSpecifier.ALL);
    _statusRegex =
        Strings.isNullOrEmpty(statusRegex)
            ? Pattern.compile(".*")
            : Pattern.compile(statusRegex.toUpperCase());
    _typeRegex =
        Strings.isNullOrEmpty(type) ? Pattern.compile(".*") : Pattern.compile(type.toUpperCase());
  }

  @Override
  public boolean getDataPlane() {
    return _includeEstablishedCount;
  }

  @JsonProperty(PROP_FOREIGN_BGP_GROUPS)
  private SortedSet<String> getForeignBgpGroups() {
    return _foreignBgpGroups;
  }

  @JsonProperty(PROP_INCLUDE_ESTABLISHED_COUNT)
  public boolean getIncludeEstablishedCount() {
    return _includeEstablishedCount;
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

  boolean matchesStatus(@Nullable SessionStatus status) {
    return status != null && _statusRegex.matcher(status.toString()).matches();
  }

  boolean matchesType(SessionType type) {
    return _typeRegex.matcher(type.toString()).matches();
  }
}
