package org.batfish.question;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.service.AutoService;
import java.util.List;
import org.batfish.common.Answerer;
import org.batfish.common.plugin.IBatfish;
import org.batfish.common.plugin.Plugin;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.datamodel.questions.Question;
import org.batfish.datamodel.questions.smt.HeaderLocationQuestion;

@AutoService(Plugin.class)
public class SmtWaypointingQuestion extends QuestionPlugin {

  public static class WaypointingAnswerer extends Answerer {

    public WaypointingAnswerer(Question question, IBatfish batfish) {
      super(question, batfish);
    }

    @Override
    public AnswerElement answer() {
      WaypointingQuestion q = (WaypointingQuestion) _question;

      return _batfish.smtWaypoint(q, q.getWaypoints());
    }
  }

  public static class WaypointingQuestion extends HeaderLocationQuestion {

    private static final String WAYPOINTS_VAR = "waypoints";

    private List<String> _waypoints;

    public WaypointingQuestion() {
      _waypoints = null;
    }

    @JsonProperty(WAYPOINTS_VAR)
    public List<String> getWaypoints() {
      return _waypoints;
    }

    @JsonProperty(WAYPOINTS_VAR)
    public void setWaypoints(List<String> w) {
      this._waypoints = w;
    }

    @Override
    public boolean getDataPlane() {
      return false;
    }

    @Override
    public String getName() {
      return "smt-waypointing";
    }
  }

  @Override
  protected Answerer createAnswerer(Question question, IBatfish batfish) {
    return new WaypointingAnswerer(question, batfish);
  }

  @Override
  protected Question createQuestion() {
    return new WaypointingQuestion();
  }
}
