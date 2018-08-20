package org.batfish.representation.palo_alto;

import java.io.Serializable;
import java.util.SortedSet;
import java.util.TreeSet;
import org.batfish.datamodel.IpSpace;
import org.batfish.datamodel.LineAction;

/** PAN datamodel component containing security rule configuration */
public final class Rule implements Serializable {
  /** */
  private static final long serialVersionUID = 1L;

  private LineAction _action;

  private String _description;

  private SortedSet<IpSpace> _destination;

  private boolean _disabled;

  private SortedSet<String> _from;

  private SortedSet<ServiceOrServiceGroupReference> _service;

  private SortedSet<IpSpace> _source;

  private SortedSet<String> _to;

  private final String _name;

  private final Vsys _vsys;

  public Rule(String name, Vsys vsys) {
    _destination = new TreeSet<>();
    _disabled = false;
    _from = new TreeSet<>();
    _service = new TreeSet<>();
    _source = new TreeSet<>();
    _to = new TreeSet<>();
    _name = name;
    _vsys = vsys;
  }

  public String getName() {
    return _name;
  }

  public LineAction getAction() {
    return _action;
  }

  public String getDescription() {
    return _description;
  }

  public SortedSet<IpSpace> getDestination() {
    return _destination;
  }

  public boolean getDisabled() {
    return _disabled;
  }

  public SortedSet<String> getFrom() {
    return _from;
  }

  public SortedSet<ServiceOrServiceGroupReference> getService() {
    return _service;
  }

  public SortedSet<IpSpace> getSource() {
    return _source;
  }

  public SortedSet<String> getTo() {
    return _to;
  }

  public Vsys getVsys() {
    return _vsys;
  }

  public void setAction(LineAction action) {
    _action = action;
  }

  public void setDescription(String description) {
    _description = description;
  }

  public void setDisabled(boolean disabled) {
    _disabled = disabled;
  }
}
