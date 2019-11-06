package org.batfish.representation.cisco_xr;

import java.io.Serializable;

public class Edge implements Serializable {

  private String _host1;
  private String _host2;
  private String _int1;
  private String _int2;

  public Edge(String host1, String int1, String host2, String int2) {
    _host1 = host1;
    _host2 = host2;
    _int1 = int1;
    _int2 = int2;
  }

  public String getHost1() {
    return _host1;
  }

  public String getHost2() {
    return _host2;
  }

  public String getInt1() {
    return _int1;
  }

  public String getInt2() {
    return _int2;
  }
}
