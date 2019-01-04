package org.batfish.bddreachability.transition;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.collect.ImmutableList;
import java.util.List;
import net.sf.javabdd.BDD;

/**
 * A {@link Transition} with multiple subtransitions -- a packet can transit this transition if it
 * can transit any of its subtransitions.
 */
public class Or implements Transition {
  private final List<Transition> _transitions;

  public Or(List<Transition> transitions) {
    checkArgument(transitions.size() > 1);
    _transitions = ImmutableList.copyOf(transitions);
  }

  public Or(Transition... transitions) {
    checkArgument(transitions.length > 1);
    _transitions = ImmutableList.copyOf(transitions);
  }

  @Override
  public BDD transitForward(BDD bdd) {
    return _transitions
        .stream()
        .map(transition -> transition.transitForward(bdd))
        .reduce(bdd.getFactory().zero(), BDD::or);
  }

  @Override
  public BDD transitBackward(BDD bdd) {
    return _transitions
        .stream()
        .map(transition -> transition.transitBackward(bdd))
        .reduce(bdd.getFactory().zero(), BDD::or);
  }
}
