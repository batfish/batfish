package org.batfish.common.plugin;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import org.batfish.datamodel.questions.Question;

public abstract class AbstractClient extends PluginConsumer implements IClient {

  protected final Map<String, Supplier<Question>> _questions;

  public AbstractClient(boolean serializeToText, List<Path> pluginDirs) {
    super(serializeToText, pluginDirs);
    _questions = new HashMap<>();
  }

  @Override
  public final PluginClientType getType() {
    return PluginClientType.CLIENT;
  }

  @Override
  public final void registerQuestion(String questionName, Supplier<Question> questionCreator) {
    _questions.put(questionName, questionCreator);
  }
}
