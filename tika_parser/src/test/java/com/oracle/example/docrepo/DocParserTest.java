package com.oracle.example.docrepo;

import com.fnproject.fn.testing.*;
import org.junit.*;

import static org.junit.Assert.*;

public class DocParserTest {

    @Rule
    public final FnTestingRule testing = FnTestingRule.createDefault();
    String sampleEvent = "{\n" +
            "  \"eventType\": \"com.oraclecloud.objectstorage.object.create\",\n" +
            "  \"eventTypeVersion\": \"1.0\",\n" +
            "  \"cloudEventsVersion\": \"0.1\",\n" +
            "  \"source\": \"/service/objectstorage/resourceType/object\",\n" +
            "  \"eventID\": \"dead-beef-abcd-1234\",\n" +
            "  \"eventTime\": \"2022-04-12T23:20:50.52Z\",\n" +
            "  \"extensions\": {\n" +
            "    \"compartmentId\": \"ocid1.compartment.oc1..aaaaaaaaociwxf74o7snuti7isddz3ieixricjbkkgbi6osgbq7eafcaaxva\"\n" +
            "  },\n" +
            "  \"data\": {\n" +
            "    \"resourceName\": \"example.pdf\",\n" +
            "    \"additionalDetails\": {\n" +
            "      \"bucketId\": \"ocid1.bucket.oc1.eu-frankfurt-1.aaaaaaaasibbkwciy6xp6rxul3u6subp3j2jhrxiaacozbg3rfiannkdsjla\",\n" +
            "      \"bucketName\": \"OSUploadBucket\",\n" +
            "      \"namespace\": \"fr9qm01oq44x\"\n" +
            "    }\n" +
            "  }\n" +
            "}\n";
    @Ignore
    @Test
    public void shouldReturnOk() {
        testing.givenEvent().withHeader("Content-Type","application/cloudevents+json")
                .withBody(sampleEvent)
                .enqueue();
        testing.thenRun(TikaParser.class, "handleRequest");

        FnResult result = testing.getOnlyResult();
        assertEquals("ok", result.getBodyAsString());
    }

}