package org.batfish.common.plugin;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import org.batfish.datamodel.questions.Question;

public abstract class AbstractClient extends PluginConsumer implements IClient {

  protected final Map<String, Supplier<Question>> _questions;

  public AbstractClient() {
    _questions = new HashMap<>();
  }

  @Override
  public final PluginClientType getType() {
    return PluginClientType.CLIENT;
  }

  @Override
  public final void registerQuestion(String questionName, Supplier<Question> questionCreator) {
    _questions.put(questionName.toLowerCase(), questionCreator);
  }
}
