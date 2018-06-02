package org.batfish.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.function.BiFunction;
import org.batfish.common.plugin.IBatfish;
import org.batfish.common.util.BatfishObjectMapper;
import org.batfish.common.util.JsonDiff;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.datamodel.answers.JsonDiffAnswerElement;
import org.batfish.datamodel.questions.Question;
import org.batfish.datamodel.table.TableAnswerElement;
import org.batfish.datamodel.table.TableDiff;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

public abstract class Answerer {

  /** While computing diffs, whether to ignore Keys that appear in one table but not the other */
  /** TODO: Make this controllable via the Question */
  /** TODO: Make this setting work for Json-level diff as well if we continue to use it */
  private static final boolean IGNORE_MISSING_KEYS = true;

  public static Answerer create(Question question, IBatfish batfish) {
    String questionName = question.getName();
    BiFunction<Question, IBatfish, Answerer> answererCreator =
        batfish.getAnswererCreators().get(questionName);
    if (answererCreator == null) {
      throw new BatfishException(
          "Cannot create answerer for missing question with name: " + questionName);
    }
    return answererCreator.apply(question, batfish);
  }

  protected final IBatfish _batfish;

  protected final BatfishLogger _logger;

  protected final Question _question;

  public Answerer(Question question, IBatfish batfish) {
    _batfish = batfish;
    _logger = batfish.getLogger();
    _question = question;
  }

  public abstract AnswerElement answer();

  /**
   * The default implementation for generating differential answers.
   *
   * <p>It uses {@link TableDiff} if the answer element is a {@link TableAnswerElement}. Otherwise,
   * it uses a JSON-level diff.
   *
   * <p>Answerers that want a custom differential answer, should override this function.
   */
  public AnswerElement answerDiff() {
    _batfish.pushBaseEnvironment();
    _batfish.checkEnvironmentExists();
    _batfish.popEnvironment();
    _batfish.pushDeltaEnvironment();
    _batfish.checkEnvironmentExists();
    _batfish.popEnvironment();
    _batfish.pushBaseEnvironment();
    AnswerElement before = create(_question, _batfish).answer();
    _batfish.popEnvironment();
    _batfish.pushDeltaEnvironment();
    AnswerElement after = create(_question, _batfish).answer();
    _batfish.popEnvironment();
    if (before instanceof TableAnswerElement) {
      AnswerElement diff =
          TableDiff.diffTables(
              (TableAnswerElement) before, (TableAnswerElement) after, IGNORE_MISSING_KEYS);
      return diff;
    } else {
      try {
        String beforeJsonStr = BatfishObjectMapper.writePrettyString(before);
        String afterJsonStr = BatfishObjectMapper.writePrettyString(after);
        JSONObject beforeJson = new JSONObject(beforeJsonStr);
        JSONObject afterJson = new JSONObject(afterJsonStr);
        JsonDiff diff = new JsonDiff(beforeJson, afterJson);

        return new JsonDiffAnswerElement(diff);
      } catch (JsonProcessingException | JSONException e) {
        throw new BatfishException("Could not convert diff element to json string", e);
      }
    }
  }
}
