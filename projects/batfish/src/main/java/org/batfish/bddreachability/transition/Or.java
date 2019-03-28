package org.batfish.bddreachability.transition;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import java.util.List;
import net.sf.javabdd.BDD;

/**
 * A {@link Transition} with multiple subtransitions -- a packet can transit this transition if it
 * can transit any of its subtransitions.
 */
public class Or implements Transition {
  private final List<Transition> _transitions;

  Or(List<Transition> transitions) {
    checkArgument(!transitions.isEmpty(), "Cannot construct Or of 0 Transitions. Use Zero instead");
    checkArgument(
        transitions.size() != 1,
        "Cannot construct Or of a single Transition. Use that transition directly instead.");
    _transitions = ImmutableList.copyOf(transitions);
  }

  Or(Transition... transitions) {
    this(ImmutableList.copyOf(transitions));
  }

  public List<Transition> getTransitions() {
    return _transitions;
  }

  @Override
  public BDD transitForward(BDD bdd) {
    return _transitions.stream()
        .map(transition -> transition.transitForward(bdd))
        .reduce(bdd.getFactory().zero(), BDD::or);
  }

  @Override
  public BDD transitBackward(BDD bdd) {
    return _transitions.stream()
        .map(transition -> transition.transitBackward(bdd))
        .reduce(bdd.getFactory().zero(), BDD::or);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    } else if (!(o instanceof Or)) {
      return false;
    }
    return _transitions.equals(((Or) o)._transitions);
  }

  @Override
  public int hashCode() {
    return _transitions.hashCode();
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(Or.class).add("transitions", _transitions).toString();
  }
}
