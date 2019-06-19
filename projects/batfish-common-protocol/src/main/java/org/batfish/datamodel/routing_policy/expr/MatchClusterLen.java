package org.batfish.datamodel.routing_policy.expr;

import static com.google.common.base.Preconditions.checkArgument;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.batfish.common.BatfishException;
import org.batfish.datamodel.routing_policy.Environment;
import org.batfish.datamodel.routing_policy.Result;

@ParametersAreNonnullByDefault
public final class MatchClusterLen extends BooleanExpr {
    private static final String PROP_COMPARATOR = "comparator";
    private static final String PROP_CLUSTERLEN = "clusterLen";

    private static final long serialVersionUID = 1L;

    @Nonnull private final IntComparator _comparator;
    private final int _clusterLen;

    @JsonCreator
    private static MatchClusterLen jsonCreator(
            @Nullable @JsonProperty(PROP_COMPARATOR) IntComparator comparator,
            @Nullable @JsonProperty(PROP_CLUSTERLEN) int clusterLen) {
        checkArgument(comparator != null, "%s must be provided", PROP_COMPARATOR);
        return new MatchClusterLen(comparator, clusterLen);
    }

    public MatchClusterLen(IntComparator comparator, int clusterLen) {
        _comparator = comparator;
        _clusterLen = clusterLen;
    }

    @Override
    public Result evaluate(Environment environment) {
        return _comparator.apply(environment.getBgpProcess().getClusterIds().size(),_clusterLen);
    }

    @JsonProperty(PROP_COMPARATOR)
    @Nonnull
    public IntComparator getComparator() {
        return _comparator;
    }

    @JsonProperty(PROP_CLUSTERLEN)
    public int getClusterLen() {
        return _clusterLen;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        } else if (!(obj instanceof MatchClusterLen)) {
            return false;
        }
        MatchClusterLen other = (MatchClusterLen) obj;
        return _comparator == other._comparator && Objects.equals(_clusterLen, other._clusterLen);
    }

    @Override
    public int hashCode() {
        return Objects.hash(_comparator.ordinal(), _clusterLen);
    }
}

