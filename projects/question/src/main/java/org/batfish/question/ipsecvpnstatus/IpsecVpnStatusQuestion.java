package org.batfish.question.ipsecvpnstatus;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Strings;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import org.batfish.datamodel.questions.NodesSpecifier;
import org.batfish.datamodel.questions.Question;
import org.batfish.question.ipsecvpnstatus.IpsecVpnInfo.Problem;

// <question_page_comment>
/*
 * Checks if IPSec VPNs are correctly configured.
 *
 * <p>Details coming on what it means to be correctly configured.
 *
 * @type IpsecVpnStatus multifile
 * @param node1Regex Regular expression to match the nodes names for one end of the sessions.
 *     Default is '.*' (all nodes).
 * @param node2Regex Regular expression to match the nodes names for the other end of the sessions.
 *     Default is '.*' (all nodes).
 * @example bf_answer("IpsecVpnStatus", node1Regex="as1.*", node2Regex="as2.*") Returns status for
 *     all IPSec VPN sessions between nodes that start with as1 and those that start with as2.
 */
public class IpsecVpnStatusQuestion extends Question {

  private static final String PROP_NODE1_REGEX = "node1Regex";
  private static final String PROP_NODE2_REGEX = "node2Regex";
  private static final String PROP_PROBLEM_REGEX = "problemRegex";

  @Nonnull private NodesSpecifier _node1Regex;
  @Nonnull private NodesSpecifier _node2Regex;
  @Nonnull Pattern _problemRegex;

  @JsonCreator
  public IpsecVpnStatusQuestion(
      @JsonProperty(PROP_NODE1_REGEX) NodesSpecifier regex1,
      @JsonProperty(PROP_NODE2_REGEX) NodesSpecifier regex2,
      @JsonProperty(PROP_PROBLEM_REGEX) String problemRegex) {
    _node1Regex = regex1 == null ? NodesSpecifier.ALL : regex1;
    _node2Regex = regex2 == null ? NodesSpecifier.ALL : regex2;
    _problemRegex =
        Strings.isNullOrEmpty(problemRegex)
            ? Pattern.compile(".*")
            : Pattern.compile(problemRegex.toUpperCase());
  }

  @Override
  public boolean getDataPlane() {
    return false;
  }

  @Override
  public String getName() {
    return "ipsecvpnstatus";
  }

  @JsonProperty(PROP_NODE1_REGEX)
  public NodesSpecifier getNode1Regex() {
    return _node1Regex;
  }

  @JsonProperty(PROP_NODE2_REGEX)
  public NodesSpecifier getNode2Regex() {
    return _node2Regex;
  }

  @JsonProperty(PROP_PROBLEM_REGEX)
  public String getProblemRegex() {
    return _problemRegex.toString();
  }

  public boolean matchesProblem(Problem problem) {
    return _problemRegex.matcher(problem.toString()).matches();
  }
}
