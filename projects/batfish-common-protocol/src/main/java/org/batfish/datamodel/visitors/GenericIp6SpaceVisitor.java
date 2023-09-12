package org.batfish.datamodel.visitors;

import org.batfish.datamodel.Ip6Space;
import org.batfish.datamodel.Ip6SpaceReference;

public interface GenericIp6SpaceVisitor<R> {
    default R visit(Ip6Space ip6Space) {
        return ip6Space.accept(this);
    }

    R visitIp6SpaceReference (Ip6SpaceReference ip6SpaceReference);
}
