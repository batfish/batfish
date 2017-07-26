package org.batfish.question;

import static org.junit.Assert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import java.util.ArrayList;
import java.util.List;

import org.batfish.common.util.CommonUtil;
import org.batfish.question.jsonpath.JsonPathQuery;
import org.junit.Test;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.PathNotFoundException;
import com.jayway.jsonpath.Configuration.ConfigurationBuilder;
import com.jayway.jsonpath.spi.json.JacksonJsonNodeJsonProvider;

/**
 * Test JsonPath functionality
 */
public class JsonPathTest {

   @Test
   public void testUnnamed1() {
      ConfigurationBuilder b = new ConfigurationBuilder();
      b.jsonProvider(new JacksonJsonNodeJsonProvider());
      final Configuration c = b.build();

      List<JsonPathQuery> paths = new ArrayList<>();
      JsonPathQuery jsonPathQuery = new JsonPathQuery();
      paths.add(jsonPathQuery);
      jsonPathQuery.setPath(
            "$.nodes[*][?(!([\"1.2.3.4\"] subsetof @.ntpServers))].ntpServers");
      jsonPathQuery.setSuffix(true);

      String nodesAnswerStr = CommonUtil
            .readResource("org/batfish/question/unnamed1.json");
      Object jsonObject = JsonPath.parse(nodesAnswerStr, c).json();
      List<Integer> indices = new ArrayList<>();
      for (int i = 0; i < paths.size(); i++) {
         indices.add(i);
      }
      for (int i : indices) {
         JsonPathQuery nodesPath = paths.get(i);
         String path = nodesPath.getPath();

         ConfigurationBuilder prefixCb = new ConfigurationBuilder();
         prefixCb.mappingProvider(c.mappingProvider());
         prefixCb.jsonProvider(c.jsonProvider());
         prefixCb.evaluationListener(c.getEvaluationListeners());
         prefixCb.options(c.getOptions());
         prefixCb.options(Option.ALWAYS_RETURN_LIST);
         prefixCb.options(Option.AS_PATH_LIST);
         Configuration prefixC = prefixCb.build();

         ArrayNode prefixes = null;
         JsonPath jsonPath = JsonPath.compile(path);

         try {
            prefixes = jsonPath.read(jsonObject, prefixC);
         }
         catch (PathNotFoundException e) {
            prefixes = JsonNodeFactory.instance.arrayNode();
         }
         assertThat(prefixes, not(equalTo(nullValue())));
      }

   }

}
