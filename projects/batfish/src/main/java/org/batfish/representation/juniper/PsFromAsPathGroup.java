package org.batfish.representation.juniper;

import com.google.common.annotations.VisibleForTesting;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import org.batfish.common.Warnings;
import org.batfish.datamodel.Configuration;
import org.batfish.datamodel.routing_policy.as_path.AsPathMatchRegex;
import org.batfish.datamodel.routing_policy.as_path.InputAsPath;
import org.batfish.datamodel.routing_policy.as_path.MatchAsPath;
import org.batfish.datamodel.routing_policy.expr.BooleanExpr;
import org.batfish.datamodel.routing_policy.expr.BooleanExprs;

public final class PsFromAsPathGroup extends PsFrom {
    private final String _asPathGroupName;

    public PsFromAsPathGroup(String asPathGroupName) {
        _asPathGroupName = asPathGroupName;
    }

    @Override
    public BooleanExpr toBooleanExpr(JuniperConfiguration jc, Configuration c, Warnings warnings) {
        Map<String, AsPathGroup> asPathGroups = jc.getMasterLogicalSystem().getAsPathGroups();
        return toBooleanExpr(asPathGroups.get(_asPathGroupName), warnings);
    }

    @VisibleForTesting
    static BooleanExpr toBooleanExpr(@Nullable AsPathGroup asPathGroup, Warnings w) {
        if (asPathGroup == null) {
            // Undefined reference, return false.
            return BooleanExprs.FALSE;
        }
        try {
            List<String> javaRegex = new ArrayList<>();
            asPathGroup
                    .getAsPathRegexes()
                    .forEach(
                            regex ->
                                    javaRegex.add(
                                            org.batfish.representation.juniper.parboiled.AsPathRegex.convertToJavaRegex(
                                                    regex)));
            String ORedRegexes = String.join(" | ", javaRegex);

            return MatchAsPath.of(InputAsPath.instance(), AsPathMatchRegex.of(ORedRegexes));
        } catch (Exception e) {
            w.redFlag(
                    String.format(
                            "Error converting Juniper as-path-group regex %s, will assume no paths match instead:"
                                    + " %s.",
                            asPathGroup.getName(), e.getMessage()));
            /* Handle error, return false instead. */
            return BooleanExprs.FALSE;
        }
    }
}