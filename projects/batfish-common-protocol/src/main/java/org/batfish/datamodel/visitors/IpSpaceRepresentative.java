package org.batfish.datamodel.visitors;

import static com.google.common.base.Preconditions.checkState;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;
import org.batfish.datamodel.Ip;
import org.batfish.datamodel.IpSpace;

public interface IpSpaceRepresentative {
  static IpSpaceRepresentative load() {
    List<IpSpaceRepresentative> impls =
        ImmutableList.copyOf(ServiceLoader.load(IpSpaceRepresentative.class));
    checkState(!impls.isEmpty(), "No IpSpaceRepresentative implementation found");
    checkState(
        impls.size() == 1,
        String.format("Only 1 IpSpaceRepresentative allowed. Found %d", impls.size()));
    return impls.get(0);
  }

  Optional<Ip> getRepresentative(IpSpace ipSpace);
}
