package org.batfish.representation.juniper;

import org.batfish.datamodel.AsPath;
import org.batfish.datamodel.routing_policy.as_path.AsPathContext;
import org.batfish.datamodel.routing_policy.as_path.AsPathMatchExpr;
import org.batfish.datamodel.routing_policy.as_path.AsPathMatchExprEvaluator;
import org.batfish.datamodel.routing_policy.as_path.AsPathMatchRegex;
import org.batfish.datamodel.routing_policy.as_path.AsSetsMatchingRanges;
import org.junit.Test;
import static org.batfish.representation.juniper.AsPathMatchExprParser.convertToAsPathMatchExpr;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertFalse;

 /**
 * Tests of {@link AsPathMatchExprParser}.
 *
 * @see <a
 *     href="https://www.juniper.net/documentation/en_US/junos/topics/usage-guidelines/policy-configuring-as-path-regular-expressions-to-use-as-routing-policy-match-conditions.html">Juniper
 *     docs</a>
 */
 public class AsPathMatchExprParserTest {

    private final AsPathMatchExprEvaluator evaluator = new AsPathMatchExprEvaluator(AsPathContext.builder().setInputAsPath(AsPath.empty()).build());

     private void assertMatches(AsPathMatchExpr res, Long... asPath) {
         assertTrue(res.accept(evaluator, AsPath.ofSingletonAsSets(asPath)));
     }

     private void assertDoesNotMatch(AsPathMatchExpr res, Long... asPath) {
         assertFalse(res.accept(evaluator, AsPath.ofSingletonAsSets(asPath)));
     }

    @Test
    public void testContainsAsn() {
        AsPathMatchExpr res = convertToAsPathMatchExpr(".* 1234 .*");
        assertThat(res, instanceOf(AsSetsMatchingRanges.class)); // did not fall back to regex
        assertMatches(res, 1234L);
        assertMatches(res, 56L, 1234L);
        assertMatches(res, 1L, 2L, 1234L);
        assertMatches(res, 1234L, 1L, 2L);
        assertMatches(res, 5L, 1234L, 8L);
        assertDoesNotMatch(res);
        assertDoesNotMatch(res, 1L, 2L, 3L, 124L);
    }

    @Test
    public void testStartsWithAsn() {
        for (String regex : new String[] {"1234 .*", "^1234 .*"}) {
            AsPathMatchExpr res = convertToAsPathMatchExpr(regex);
            assertThat(res, instanceOf(AsSetsMatchingRanges.class)); // did not fall back to regex
            assertMatches(res, 1234L);
            assertMatches(res, 1234L, 12L);
            assertMatches(res, 1234L, 56L, 78L);
            assertDoesNotMatch(res);
            assertDoesNotMatch(res, 12L, 1234L);
            assertDoesNotMatch(res, 1L, 2L, 1234L);
            assertDoesNotMatch(res, 5L, 1234L, 8L);
        }
    }

    @Test
    public void testEndsWithAsn() {
        for (String regex : new String[] {".* 1234", ".* 1234$"}) {
            AsPathMatchExpr res = convertToAsPathMatchExpr(regex);
            assertThat(res, instanceOf(AsSetsMatchingRanges.class)); // did not fall back to regex
            assertMatches(res, 1234L);
            assertMatches(res, 12L, 1234L);
            assertMatches(res, 56L, 78L, 1234L);
            assertDoesNotMatch(res);
            assertDoesNotMatch(res, 1234L, 12L);
            assertDoesNotMatch(res, 1234L, 1L, 8L);
            assertDoesNotMatch(res, 5L, 1234L, 8L);
        }
    }

    @Test
    public void testContainsAsnRange() {
        AsPathMatchExpr res = convertToAsPathMatchExpr(".* [123-187] .*");
        assertThat(res, instanceOf(AsSetsMatchingRanges.class)); // did not fall back to regex
        assertMatches(res, 123L);
        assertMatches(res, 135L);
        assertMatches(res, 187L);
        assertMatches(res, 110L, 123L, 135L, 2L);
        assertDoesNotMatch(res);
        assertDoesNotMatch(res, 0L);
        assertDoesNotMatch(res, 1102L);
        assertDoesNotMatch(res, 110L, 122L, 188L);
    }

    /**
     * Test for fallback to {@link AsPathMatchRegex} for some example regexes tested in {@link AsPathRegexTest}.
     */
    @Test
    public void testAsPathMatchRegexFallback() {
        for (String regex : new String[]{"1234", "1234?", "1234{0,1}", "12{1,4} 34", "[123-125]", "123 (56 | 78)?", "()"}) {
            AsPathMatchExpr res1 = convertToAsPathMatchExpr(regex);
            assertThat(res1, instanceOf(AsPathMatchRegex.class));
        }
        // Sanity testing that the fallback delegation AsPathMatchRegex works as expected
        AsPathMatchExpr res2 = convertToAsPathMatchExpr("1 | (2 3) | (4 (5|6))");
        assertThat(res2, instanceOf(AsPathMatchRegex.class));; // fall back to regex
        assertMatches(res2,1L);
        assertMatches(res2,2L, 3L);
        assertMatches(res2,4L, 5L);
        assertMatches(res2,4L, 6L);
        assertDoesNotMatch(res2,1L, 2L, 3L);
    }

}
