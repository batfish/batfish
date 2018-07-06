package org.batfish.representation.palo_alto;

import java.io.Serializable;
import java.util.SortedSet;
import java.util.TreeSet;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.LineAction;

public final class Rule implements Serializable {
  /** */
  private static final long serialVersionUID = 1L;

  private LineAction _action;

  private SortedSet<IpSpace> _destination;

  private SortedSet<String> _from;

  private SortedSet<String> _service;

  private SortedSet<IpSpace> _source;

  private SortedSet<String> _to;

  private final String _name;

  public Rule(String name) {
    _destination = new TreeSet<>();
    _from = new TreeSet<>();
    _service = new TreeSet<>();
    _source = new TreeSet<>();
    _to = new TreeSet<>();
    _name = name;
  }

  public String getName() {
    return _name;
  }

  public LineAction getAction() {
    return _action;
  }

  public SortedSet<IpSpace> getDestination() {
    return _destination;
  }

  public SortedSet<String> getFrom() {
    return _from;
  }

  public SortedSet<String> getService() {
    return _service;
  }

  public SortedSet<IpSpace> getSource() {
    return _source;
  }

  public SortedSet<String> getTo() {
    return _to;
  }

  public void setAction(LineAction action) {
    _action = action;
  }
}
