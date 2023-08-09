package org.batfish.representation.juniper;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Range;
import org.batfish.datamodel.routing_policy.as_path.AsPathMatchExpr;
import org.batfish.datamodel.routing_policy.as_path.AsPathMatchRegex;
import org.batfish.datamodel.routing_policy.as_path.AsSetsMatchingRanges;
import org.batfish.representation.juniper.parboiled.AsPathRegex;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * A class that converts a Juniper AS Path regex to an instance of {@link AsPathMatchExpr}.
 *
 * @see <a
 *     href="https://www.juniper.net/documentation/en_US/junos/topics/usage-guidelines/policy-configuring-as-path-regular-expressions-to-use-as-routing-policy-match-conditions.html">Juniper
 *     docs</a>
 */
public final class AsPathMatchExprParser {

    // ".* N .*" : "AS Path contains N"
    private static final Pattern AS_PATH_CONTAINS_ASN = Pattern.compile("\\.\\* (\\d+) \\.\\*");

    // ".* N" or ".* N$" : "AS Path ends with N"
    private static final Pattern AS_PATH_ENDS_WITH_ASN = Pattern.compile("\\.\\* (\\d+)\\$?");

    // "N .*" or "^N .*" : "AS Path starts with N".
    private static final Pattern AS_PATH_STARTS_WITH_ASN = Pattern.compile("\\^?(\\d+) \\.\\*");

    // ".* [start-end] .*" : "AS Path contains an ASN in range between start and end included
    private static final Pattern AS_PATH_CONTAINS_ASN_RANGE = Pattern.compile("\\.\\* \\[(\\d+)-(\\d+)\\] \\.\\*");


    /**
     * Converts the given Juniper AS Path regular expression to an instance of {@link AsPathMatchExpr}.
     * First match the AS path input regex against predefined AS path regexes and return VI construct {@link AsSetsMatchingRanges}
     * Else convert to java regex and return {@link  AsPathMatchRegex}
     */
    public static AsPathMatchExpr convertToAsPathMatchExpr(String regex) {
        Matcher m1 = AS_PATH_CONTAINS_ASN.matcher(regex);
        if (m1.matches()) {
            String asn = m1.group(1);
            long asnLong = Long.parseLong(asn);
            return AsSetsMatchingRanges.of(false, false, ImmutableList.of(Range.singleton(asnLong)));
        }

        Matcher m2 = AS_PATH_STARTS_WITH_ASN.matcher(regex);
        if (m2.matches()) {
            String asn = m2.group(1);
            long asnLong = Long.parseLong(asn);
            return AsSetsMatchingRanges.of(false, true, ImmutableList.of(Range.singleton(asnLong)));
        }

        Matcher m3 = AS_PATH_ENDS_WITH_ASN.matcher(regex);
        if (m3.matches()) {
            String asn = m3.group(1);
            long asnLong = Long.parseLong(asn);
            return AsSetsMatchingRanges.of(true, false, ImmutableList.of(Range.singleton(asnLong)));
        }

        Matcher m4 = AS_PATH_CONTAINS_ASN_RANGE.matcher(regex);
        if (m4.matches()) {
            String asnLowerRange = m4.group(1);
            String asnUpperRange = m4.group(2);
            long start = Long.parseLong(asnLowerRange);
            long end = Long.parseLong(asnUpperRange);
            checkArgument(start <= end, "Invalid range %s-%s", start, end);
            return AsSetsMatchingRanges.of(false, false, ImmutableList.of(
                    Range.closed(start, end)
            ));
        }

        String javaRegex = AsPathRegex.convertToJavaRegex(regex);
        return AsPathMatchRegex.of(javaRegex);
    }
    private AsPathMatchExprParser() {} // prevent instantiation of utility class.
}