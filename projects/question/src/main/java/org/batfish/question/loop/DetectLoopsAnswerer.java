package org.batfish.question.loop;

import com.google.common.base.Functions;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.stream.Collectors;
import org.batfish.common.Answerer;
import org.batfish.common.plugin.IBatfish;
import org.batfish.datamodel.Flow;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.answers.AnswerElement;
import org.batfish.datamodel.flow.Trace;
import org.batfish.datamodel.questions.Question;
import org.batfish.datamodel.table.TableAnswerElement;
import org.batfish.question.traceroute.TracerouteAnswerer;

/** {@link Answerer} for {@link DetectLoopsQuestion}. */
public final class DetectLoopsAnswerer extends Answerer {
  public DetectLoopsAnswerer(Question question, IBatfish batfish) {
    super(question, batfish);
  }

  @Override
  public AnswerElement answer() {
    DetectLoopsQuestion question = (DetectLoopsQuestion) _question;
    Set<Flow> flows = _batfish.bddLoopDetection();

    /*
     * There can be many flows exercising the same loop, so let's pick one per dstIp.
     */
    Multimap<Ip, Flow> flowsPerDst =
        flows.stream()
            .collect(
                Multimaps.toMultimap(Flow::getDstIp, Functions.identity(), HashMultimap::create));

    flows =
        flowsPerDst.asMap().values().stream()
            .flatMap(flowsWithSameDst -> flowsWithSameDst.stream().limit(1))
            .collect(Collectors.toSet());

    SortedMap<Flow, List<Trace>> flowTraces = _batfish.buildFlows(flows, false);
    TableAnswerElement tableAnswer = new TableAnswerElement(TracerouteAnswerer.metadata(false));
    TracerouteAnswerer.flowTracesToRows(flowTraces, question.getMaxTraces())
        .forEach(tableAnswer::addRow);
    return tableAnswer;
  }
}
