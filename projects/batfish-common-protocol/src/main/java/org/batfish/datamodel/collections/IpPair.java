package org.batfish.datamodel.collections;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import org.batfish.common.Pair;
import org.batfish.datamodel.Ip;

public class IpPair extends Pair<Ip, Ip> {

  /** */
  private static final long serialVersionUID = 1L;

  private static Ip part1(String s) {
    String trimmed = s.substring(1, s.length() - 1);
    String[] parts = trimmed.split(":");
    String part1Str = parts[0];
    Ip part1 = new Ip(part1Str);
    return part1;
  }

  private static Ip part2(String s) {
    String trimmed = s.substring(1, s.length() - 1);
    String[] parts = trimmed.split(":");
    String part2Str = parts[1];
    Ip part2 = new Ip(part2Str);
    return part2;
  }

  public IpPair(Ip ip1, Ip ip2) {
    super(ip1, ip2);
  }

  @JsonCreator
  public IpPair(String s) {
    super(part1(s), part2(s));
  }

  @Override
  @JsonValue
  public String toString() {
    return super.toString();
  }
}
