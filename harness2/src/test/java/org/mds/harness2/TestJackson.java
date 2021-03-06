package org.mds.harness2;

import org.codehaus.jackson.annotate.JsonAutoDetect.Visibility;
import org.codehaus.jackson.annotate.JsonMethod;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;

public class TestJackson {
    public static void main(String[] args) throws Exception {
        // { "aaa":"111", "bbb":"222", "ccc":"333" }
        String jsonInput = "{\"aaa\":\"111\", \"bbb\":\"222\"}";

        ObjectMapper mapper = new ObjectMapper().setVisibility(JsonMethod.FIELD, Visibility.ANY);
        mapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        Test test = mapper.readValue(jsonInput, Test.class);
        System.out.println(test);
    }
}
