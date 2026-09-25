package org.batfish.representation.juniper;

import java.io.Serializable;
import java.util.Set;
import java.util.TreeSet;
import javax.annotation.ParametersAreNonnullByDefault;

/** A named Junos set of forwarding classes. */
@ParametersAreNonnullByDefault
public final class ForwardingClassSet implements Serializable {

  private final Set<String> _forwardingClasses;
  private final String _name;

  public ForwardingClassSet(String name) {
    _forwardingClasses = new TreeSet<>();
    _name = name;
  }

  public Set<String> getForwardingClasses() {
    return _forwardingClasses;
  }

  public String getName() {
    return _name;
  }
}
