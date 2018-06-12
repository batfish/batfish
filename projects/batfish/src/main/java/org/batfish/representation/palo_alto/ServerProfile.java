package org.batfish.representation.palo_alto;

import java.util.SortedMap;
import java.util.TreeMap;
import org.batfish.common.util.ComparableStructure;

public class ServerProfile extends ComparableStructure<String> {
  private static final long serialVersionUID = 1L;

  private final SortedMap<String, Server> _servers;

  public ServerProfile(String name) {
    super(name);
    _servers = new TreeMap<>();
  }

  public SortedMap<String, Server> getServers() {
    return _servers;
  }
}
