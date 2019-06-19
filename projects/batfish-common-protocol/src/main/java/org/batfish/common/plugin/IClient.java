package org.batfish.common.plugin;

import java.util.function.Supplier;
import org.batfish.datamodel.questions.Question;

public interface IClient extends IPluginConsumer {

  void registerQuestion(String questionName, Supplier<Question> questionSupplier);
}
