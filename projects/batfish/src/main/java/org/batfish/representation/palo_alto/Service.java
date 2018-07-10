package org.batfish.representation.palo_alto;

import java.io.Serializable;
import java.util.SortedSet;
import java.util.TreeSet;
import org.batfish.datamodel.IpProtocol;
import org.batfish.datamodel.SubRange;

public final class Service implements Serializable {
  private static final long serialVersionUID = 1L;

  private String _description;

  private final String _name;

  private final SortedSet<SubRange> _ports;

  private IpProtocol _protocol;

  private final SortedSet<SubRange> _sourcePorts;

  public Service(String name) {
    _name = name;
    _ports = new TreeSet<>();
    _sourcePorts = new TreeSet<>();
  }

  public String getDescription() {
    return _description;
  }

  public String getName() {
    return _name;
  }

  public SortedSet<SubRange> getPorts() {
    return _ports;
  }

  public IpProtocol getProtocol() {
    return _protocol;
  }

  public SortedSet<SubRange> getSourcePorts() {
    return _sourcePorts;
  }

  public void setDescription(String description) {
    _description = description;
  }

  public void setProtocol(IpProtocol protocol) {
    _protocol = protocol;
  }
}
