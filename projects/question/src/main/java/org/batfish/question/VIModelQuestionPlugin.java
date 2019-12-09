package org.batfish.question;

import static com.google.common.base.MoreObjects.firstNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableSortedMap;
import java.util.SortedMap;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.common.Answerer;
import org.batfish.common.NetworkSnapshot;
import org.batfish.common.plugin.IBatfish;
import org.batfish.common.plugin.Plugin;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.datamodel.questions.Question;

@AutoService(Plugin.class)
public final class VIModelQuestionPlugin extends QuestionPlugin {

  public static final class VIModelAnswerElement extends AnswerElement {

    private static final String PROP_NODES = "nodes";

    private final SortedMap<String, Configuration> _nodes;

    @JsonCreator
    private static VIModelAnswerElement jsonCreator(
        @Nullable @JsonProperty(PROP_NODES) SortedMap<String, Configuration> nodes) {
      return new VIModelAnswerElement(firstNonNull(nodes, ImmutableSortedMap.of()));
    }

    VIModelAnswerElement(@Nonnull SortedMap<String, Configuration> nodes) {
      _nodes = nodes;
    }

    @JsonProperty(PROP_NODES)
    public @Nonnull SortedMap<String, Configuration> getNodes() {
      return _nodes;
    }
  }

  public static final class VIModelAnswerer extends Answerer {

    VIModelAnswerer(Question question, IBatfish batfish) {
      super(question, batfish);
    }

    @Override
    public VIModelAnswerElement answer(NetworkSnapshot snapshot) {
      return new VIModelAnswerElement(_batfish.loadConfigurations(snapshot));
    }
  }

  public static final class VIModelQuestion extends Question {
    @Override
    public boolean getDataPlane() {
      return false;
    }

    @Override
    public String getName() {
      return "viModel";
    }
  }

  @Override
  protected Answerer createAnswerer(Question question, IBatfish batfish) {
    return new VIModelAnswerer(question, batfish);
  }

  @Override
  protected Question createQuestion() {
    return new VIModelQuestion();
  }
}
