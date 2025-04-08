package org.batfish.grammar.flatjuniper;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import java.util.HashMap;

import org.antlr.v4.runtime.Token;
import org.batfish.common.Warnings;

import org.batfish.grammar.silent_syntax.SilentSyntaxCollection;
import org.batfish.representation.juniper.LiteralCommunityMember;
import org.junit.Test;

public class CommunityMemberTest {
    // Create a simple subclass of ConfigurationBuilder for testing
    private static class TestConfigurationBuilder extends ConfigurationBuilder {
        public TestConfigurationBuilder() {

            super(
                    null,
                    "",
                    new Warnings(),
                    new HashMap<Token, String>(),
                    new SilentSyntaxCollection());
        }

        @Override
        public void warn(org.antlr.v4.runtime.ParserRuleContext ctx, String message) {
            // Do nothing in tests
        }
    }

    private static final CommunityMemberTest.TestConfigurationBuilder TEST_BUILDER = new CommunityMemberTest.TestConfigurationBuilder();

    @Test
    public void testCreateCommunityMemberWithJustColon() {
        assertThat(ConfigurationBuilder.createCommunityMember(":", null, TEST_BUILDER), instanceOf(LiteralCommunityMember.class));
    }
}
