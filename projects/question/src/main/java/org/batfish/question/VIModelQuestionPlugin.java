package org.batfish.question;

import static com.google.common.base.MoreObjects.firstNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auto.service.AutoService;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableSortedMap;
import java.util.Comparator;
import java.util.Map.Entry;
import java.util.Set;
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
import org.batfish.specifier.AllNodesNodeSpecifier;
import org.batfish.specifier.NodeSpecifier;
import org.batfish.specifier.SpecifierContext;
import org.batfish.specifier.SpecifierFactories;

@AutoService(Plugin.class)
public final class VIModelQuestionPlugin extends QuestionPlugin {

  private static final String PROP_NODES = "nodes";

  public static final class VIModelAnswerElement extends AnswerElement {

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
      return new VIModelAnswerElement(
          getConfigs((VIModelQuestion) _question, _batfish.specifierContext(snapshot)));
    }

    @JsonIgnore
    @VisibleForTesting
    static SortedMap<String, Configuration> getConfigs(
        VIModelQuestion question, SpecifierContext specifierContext) {
      NodeSpecifier nodeSpecifier = question.getNodeSpecifier();
      Set<String> nodes = nodeSpecifier.resolve(specifierContext);
      return specifierContext.getConfigs().entrySet().stream()
          .filter(entry -> nodes.contains(entry.getKey()))
          .collect(
              ImmutableSortedMap.toImmutableSortedMap(
                  Comparator.naturalOrder(), Entry::getKey, Entry::getValue));
    }
  }

  public static final class VIModelQuestion extends Question {

    private @Nullable final String _nodes;

    public VIModelQuestion(@Nullable String nodes) {
      _nodes = nodes;
    }

    @JsonCreator
    private static VIModelQuestion create(@Nullable @JsonProperty(PROP_NODES) String nodes) {
      return new VIModelQuestion(nodes);
    }

    @Override
    public boolean getDataPlane() {
      return false;
    }

    @Override
    public String getName() {
      return "viModel";
    }

    @JsonProperty(PROP_NODES)
    public @Nullable String getNodes() {
      return _nodes;
    }

    private NodeSpecifier getNodeSpecifier() {
      return SpecifierFactories.getNodeSpecifierOrDefault(_nodes, AllNodesNodeSpecifier.INSTANCE);
    }
  }

  @Override
  protected Answerer createAnswerer(Question question, IBatfish batfish) {
    return new VIModelAnswerer(question, batfish);
  }

  @Override
  protected Question createQuestion() {
    return new VIModelQuestion(null);
  }
}
