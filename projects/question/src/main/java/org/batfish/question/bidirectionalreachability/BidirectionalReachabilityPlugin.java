package org.batfish.question.bidirectionalreachability;

import static org.batfish.question.bidirectionalreachability.ReturnFlowType.SUCCESS;

import com.google.auto.service.AutoService;
import org.batfish.common.Answerer;
import org.batfish.common.plugin.IBatfish;
import org.batfish.common.plugin.Plugin;
import org.batfish.datamodel.PacketHeaderConstraints;
import org.batfish.datamodel.questions.Question;
import org.batfish.question.QuestionPlugin;
import org.batfish.question.specifiers.PathConstraintsInput;

/** */
@AutoService(Plugin.class)
public final class BidirectionalReachabilityPlugin extends QuestionPlugin {

  @Override
  protected Answerer createAnswerer(Question question, IBatfish batfish) {
    return new BidirectionalReachabilityAnswerer(
        (BidirectionalReachabilityQuestion) question, batfish);
  }

  @Override
  protected Question createQuestion() {
    return new BidirectionalReachabilityQuestion(
        PacketHeaderConstraints.unconstrained(), PathConstraintsInput.unconstrained(), SUCCESS);
  }
}
