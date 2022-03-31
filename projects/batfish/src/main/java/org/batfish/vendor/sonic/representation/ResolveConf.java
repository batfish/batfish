package org.batfish.vendor.sonic.representation;

import com.google.common.collect.ImmutableList;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nonnull;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.Ip6;

/** Represents information in resolve.conf file that is provided as part of the SONiC file bundle */
public class ResolveConf implements Serializable {

  private static final String PROP_NAMESERVER = "nameserver";

  private @Nonnull final List<Ip> _nameservers;
  private @Nonnull final List<Ip6> _nameservers6;

  public ResolveConf(List<Ip> nameservers, List<Ip6> nameservers6) {
    _nameservers = ImmutableList.copyOf(nameservers);
    _nameservers6 = ImmutableList.copyOf(nameservers6);
  }

  public static ResolveConf deserialize(String resolveConfText, Warnings warnings) {
    List<Ip> nameservers = new LinkedList<>();
    List<Ip6> nameservers6 = new LinkedList<>();
    boolean foundNameserverLine = false;
    String[] lines = resolveConfText.split("\n");
    for (String line : lines) {
      String[] parts = line.split("\\s");
      if (parts.length == 2 && parts[0].equals(PROP_NAMESERVER)) {
        foundNameserverLine = true;
        String address = parts[1];
        Optional<Ip> ip = Ip.tryParse(address);
        if (ip.isPresent()) {
          nameservers.add(ip.get());
          continue;
        }
        Optional<Ip6> ip6 = Ip6.tryParse(address);
        if (ip6.isPresent()) {
          nameservers6.add(ip6.get());
          continue;
        }
        warnings.redFlag(String.format("'%s' is neither IPv4 nor IPv6 address", address));
      }
    }
    if (!foundNameserverLine) {
      warnings.redFlag("No nameserver found");
    }
    return new ResolveConf(nameservers, nameservers6);
  }

  @Nonnull
  public List<Ip> getNameservers() {
    return _nameservers;
  }

  @Nonnull
  public List<Ip6> getNameservers6() {
    return _nameservers6;
  }
}
