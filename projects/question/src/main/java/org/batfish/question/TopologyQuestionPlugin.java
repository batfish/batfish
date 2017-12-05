package org.batfish.question;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.service.AutoService;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import org.batfish.common.Answerer;
import org.batfish.common.BatfishException;
import org.batfish.common.plugin.IBatfish;
import org.batfish.common.plugin.Plugin;
import org.batfish.common.util.CommonUtil;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.ConfigurationDiff;
import org.batfish.datamodel.NodeType;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.datamodel.questions.Question;

@AutoService(Plugin.class)
public class TopologyQuestionPlugin extends QuestionPlugin {

  public static class TopologyAnswerElement implements AnswerElement {

    public TopologyAnswerElement(Map<String, Configuration> nodes) {}

    @JsonCreator
    public TopologyAnswerElement() {}
  }

  public static class TopologyAnswerer extends Answerer {

    public TopologyAnswerer(Question question, IBatfish batfish) {
      super(question, batfish);
    }

    @Override
    public TopologyAnswerElement answer() {
      TopologyQuestion question = (TopologyQuestion) _question;

      Map<String, Configuration> configurations = _batfish.loadConfigurations();

      return new TopologyAnswerElement(configurations);
    }
  }

  // <question_page_comment>

  /**
   * Outputs the topology of the network.
   *
   * <p>This question may be used to extract the topology of the network
   *
   */
  public static class TopologyQuestion extends Question {

    public TopologyQuestion() {
    }

    @Override
    public boolean getDataPlane() {
      return false;
    }

    @Override
    public String getName() {
      return "topology";
    }

    @Override
    public boolean getTraffic() {
      return false;
    }
  }

  @Override
  protected Answerer createAnswerer(Question question, IBatfish batfish) {
    return new TopologyAnswerer(question, batfish);
  }

  @Override
  protected Question createQuestion() {
    return new TopologyQuestion();
  }
}
