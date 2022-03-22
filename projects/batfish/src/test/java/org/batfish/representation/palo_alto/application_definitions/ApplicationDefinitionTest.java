package org.batfish.representation.palo_alto.application_definitions;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.batfish.common.util.BatfishObjectMapper;
import org.junit.Test;

/** Test of {@link ApplicationDefinition}. */
public class ApplicationDefinitionTest {
  @Test
  public void testJacksonDeserialization() throws JsonProcessingException {
    // Application definitions loosely based on dropbox-personal-uploading

    // App definition with lists of values
    {
      String appDefJsonWithLists =
          "{"
              + "  \"@name\": \"something-personal-uploading\","
              + "  \"parent-app\": \"something-uploading\","
              + "  \"default\": {"
              + "    \"port\": {"
              + "      \"member\": [\"tcp/443,80\", \"udp/dynamic\"]"
              + "    }"
              + "  },"
              + "  \"use-applications\": {"
              + "    \"member\": ["
              + "      \"something-base\","
              + "      {"
              + "        \"@minver\": \"3.0.0\","
              + "        \"#text\": \"something-uploading\""
              + "      }"
              + "    ]"
              + "  },"
              + "  \"implicit-use-applications\": {"
              + "    \"member\": [\"web-browsing\", \"ssl\"]"
              + "  },"
              + "  \"application-container\": \"something\","
              + "  \"unknown-key\": \"unknown-value\""
              + "}";
      ApplicationDefinition appDef =
          BatfishObjectMapper.ignoreUnknownMapper()
              .readValue(appDefJsonWithLists, ApplicationDefinition.class);
      assertNotNull(appDef);
      assertThat(appDef.getApplicationContainer(), equalTo("something"));
      assertNotNull(appDef.getDefault());
      assertNotNull(appDef.getDefault().getPort());
      assertThat(appDef.getDefault().getPort().getMember(), contains("tcp/443,80", "udp/dynamic"));
      assertThat(appDef.getName(), equalTo("something-personal-uploading"));
      assertNotNull(appDef.getImplicitUseApplications());
      assertThat(appDef.getImplicitUseApplications().getMember(), contains("web-browsing", "ssl"));
      assertNotNull(appDef.getUseApplications());
      assertThat(
          appDef.getUseApplications().getMember(),
          contains("something-base", "something-uploading"));
    }

    // App definition with single-values, no lists
    {
      String appDefJson =
          "{"
              + "  \"@name\": \"something-personal-uploading\","
              + "  \"parent-app\": \"something-uploading\","
              + "  \"default\": {"
              + "    \"port\": {"
              + "      \"member\": \"tcp/443,80\""
              + "    }"
              + "  },"
              + "  \"use-applications\": {"
              + "    \"member\": \"something-base\""
              + "  },"
              + "  \"implicit-use-applications\": {"
              + "    \"member\": \"web-browsing\""
              + "  },"
              + "  \"application-container\": \"something\","
              + "  \"unknown-key\": \"unknown-value\""
              + "}";
      ApplicationDefinition appDef =
          BatfishObjectMapper.ignoreUnknownMapper()
              .readValue(appDefJson, ApplicationDefinition.class);
      assertNotNull(appDef);
      assertThat(appDef.getApplicationContainer(), equalTo("something"));
      assertNotNull(appDef.getDefault());
      assertNotNull(appDef.getDefault().getPort());
      assertThat(appDef.getDefault().getPort().getMember(), contains("tcp/443,80"));
      assertThat(appDef.getName(), equalTo("something-personal-uploading"));
      assertNotNull(appDef.getImplicitUseApplications());
      assertThat(appDef.getImplicitUseApplications().getMember(), contains("web-browsing"));
      assertNotNull(appDef.getUseApplications());
      assertThat(appDef.getUseApplications().getMember(), contains("something-base"));
    }
  }
}
