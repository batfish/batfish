package org.batfish.question.loop;

import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Ordering;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.SortedMap;
import java.util.stream.Collectors;
import org.batfish.common.Answerer;
import org.batfish.common.NetworkSnapshot;
import org.batfish.common.plugin.IBatfish;
import org.batfish.datamodel.Flow;
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
  public AnswerElement answer(NetworkSnapshot snapshot) {
    DetectLoopsQuestion question = (DetectLoopsQuestion) _question;
    Set<Flow> flows = _batfish.bddLoopDetection(snapshot);

    /*
     * There can be many flows exercising the same loop, so let's pick one per dstIp.
     */
    flows =
        flows.stream()
            .collect(Collectors.groupingBy(Flow::getDstIp, Collectors.minBy(Ordering.natural())))
            .values()
            .stream()
            .map(Optional::get) // safe: the min here cannot be empty by construction.
            .collect(ImmutableSortedSet.toImmutableSortedSet(Ordering.natural()));

    SortedMap<Flow, List<Trace>> flowTraces = _batfish.buildFlows(snapshot, flows, false);
    TableAnswerElement tableAnswer = new TableAnswerElement(TracerouteAnswerer.metadata(false));
    TracerouteAnswerer.flowTracesToRows(flowTraces, question.getMaxTraces())
        .forEach(tableAnswer::addRow);
    return tableAnswer;
  }
}
