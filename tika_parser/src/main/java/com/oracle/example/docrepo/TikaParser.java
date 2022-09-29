package com.oracle.example.docrepo;

import com.fnproject.fn.api.InputBinding;
import com.oracle.bmc.Region;
import com.oracle.bmc.auth.AuthenticationDetailsProvider;
import com.oracle.bmc.auth.ResourcePrincipalAuthenticationDetailsProvider;
import com.oracle.bmc.objectstorage.requests.GetObjectRequest;
import com.oracle.bmc.objectstorage.requests.PutObjectRequest;
import com.oracle.bmc.objectstorage.responses.GetObjectResponse;
import com.oracle.bmc.objectstorage.responses.PutObjectResponse;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;

import com.oracle.bmc.responses.AsyncHandler;
import com.oracle.bmc.objectstorage.ObjectStorageAsyncClient;

import com.oracle.bmc.streaming.StreamClient;
import com.oracle.bmc.streaming.model.PutMessagesDetails;
import com.oracle.bmc.streaming.model.PutMessagesDetailsEntry;
import com.oracle.bmc.streaming.model.PutMessagesResultEntry;
import com.oracle.bmc.streaming.model.Stream;
import com.oracle.bmc.streaming.StreamAdminClient;

import com.oracle.bmc.streaming.requests.GetStreamRequest;
import com.oracle.bmc.streaming.requests.ListStreamsRequest;
import com.oracle.bmc.streaming.requests.PutMessagesRequest;
import com.oracle.bmc.streaming.responses.GetStreamResponse;
import com.oracle.bmc.streaming.responses.ListStreamsResponse;
import com.oracle.bmc.streaming.responses.PutMessagesResponse;
import com.oracle.bmc.util.internal.StringUtils;
import com.oracle.example.docrepo.cloudevents.OCIEventBinding;
import com.oracle.example.docrepo.cloudevents.ObjectStorageObjectEvent;
import com.oracle.bmc.objectstorage.ObjectStorage;
import com.oracle.bmc.objectstorage.ObjectStorageClient;
import com.fnproject.fn.api.RuntimeContext;
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.xml.sax.SAXException;

import javax.json.*;
import java.util.ArrayList;
import java.util.List;
import java.nio.charset.StandardCharsets;

import org.apache.http.conn.ssl.*;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.*;

import static java.nio.charset.StandardCharsets.UTF_8;

import javax.net.ssl.*;
import java.io.*;
import java.net.URL;
import java.security.*;
import java.security.cert.*;

public class TikaParser {
    private ObjectStorage objectStorageClient;
    private ResourcePrincipalAuthenticationDetailsProvider provider;

    public TikaParser(RuntimeContext ctx) {
        initOciClients();
    }

    private String reqEnv(RuntimeContext ctx, String key) {
        return ctx.getConfigurationByKey(key).orElseThrow(() -> new RuntimeException("Missing required config " + key));

    }

    private void initOciClients() {
        System.out.println("Inside initOciClients");
        try {
            provider = ResourcePrincipalAuthenticationDetailsProvider.builder().build();
            System.err.println("ResourcePrincipalAuthenticationDetailsProvider setup");
            objectStorageClient = ObjectStorageClient.builder().build(provider);
            // objectStorageClient.setRegion(Region.EU_FRANKFURT_1);
            System.out.println("ObjectStorage client setup");

        } catch (Exception ex) {
            System.err.println("Exception in FDK " + ex.getMessage());
            ex.printStackTrace();
            throw new RuntimeException("failed to init oci clients", ex);
        }
    }

    public GetObjectResponse readObject(String namespace, String bucketname, String filename) {
        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .namespaceName(namespace)
                    .bucketName(bucketname)
                    .objectName(filename)
                    .build();
            GetObjectResponse getObjectResponse = objectStorageClient.getObject(getObjectRequest);
            return getObjectResponse;
        } catch (Exception e) {
            throw new RuntimeException("Could not read from os!" + e.getMessage());
        }
    }

    public JsonObject parseObject(GetObjectResponse objectResponse)
            throws IOException, TikaException, SAXException {
        // Create a Tika instance with the default configuration
        Tika tika = new Tika();
        Metadata metadata = new Metadata();
        Reader reader = tika.parse(objectResponse.getInputStream(), metadata);
        objectResponse.getInputStream().close();
        JsonObjectBuilder jsonObjectBuilder = Json.createObjectBuilder();
        // getting metadata of the document
        String[] metadataNames = metadata.names();
        for (String name : metadataNames) {
            jsonObjectBuilder.add(name, metadata.get(name));
        }
        // getting the content of the document
        BufferedReader bufferedReader = new BufferedReader(reader);
        StringBuilder stringBuilder = new StringBuilder();
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            stringBuilder.append(line);
        }
        String content = stringBuilder.toString();
        bufferedReader.close();
        jsonObjectBuilder.add("content", content);
        JsonObject jsonObject = jsonObjectBuilder.build();

        return jsonObject;
    }


    public static class ObjectInfo {

        private String bucketName;
        private String resourceName;
        private String namespace;

        public ObjectInfo() {
        }

        public String getBucketName() {
            return bucketName;
        }

        public void setBucketName(String bucketName) {
            this.bucketName = bucketName;
        }

        public String getResourceName() {
            return resourceName;
        }

        public void setResourceName(String resourceName) {
            this.resourceName = resourceName;
        }

        public String getNamespace() {
            return namespace;
        }

        public void setNamespace(String namespace) {
            this.namespace = namespace;
        }
    }

    public String handleRequest(ObjectInfo objectInfo) {
        System.err.println("request: objectInfo=" + objectInfo.getResourceName());
        try {
            GetObjectResponse getObjectResponse = readObject(objectInfo.getNamespace(), objectInfo.getBucketName(), objectInfo.getResourceName());
            JsonObject jsondoc = parseObject(getObjectResponse);

            return jsondoc.toString(); // "ok";
        } catch (Exception ex) {
            System.err.println("Exception in FDK " + ex.getMessage());
            ex.printStackTrace();
            return "Exception in TikaObjectStorage: " + ex.getMessage();
        }
    }
/*    
    public String handleRequest(@InputBinding(coercion = OCIEventBinding.class) ObjectStorageObjectEvent event) {
        System.err.println("Got a new event: " + event.toString());
        try {
            String namespace = event.additionalDetails.namespace;
            String bucketName = event.additionalDetails.bucketName;
            String resourceName = event.resourceName;
            String compartmentId = event.compartmentId;

            GetObjectResponse getObjectResponse = readObject(namespace, bucketName, resourceName);
            String path = "https://objectstorage.eu-frankfurt-1.oraclecloud.com/n/" + namespace + "/b/" + bucketName
                    + "/o/" + resourceName;

            JsonObject jsondoc = parseObject(getObjectResponse, path);

            streamObject(jsondoc, resourceName, compartmentId);
            writeObject(jsondoc, namespace, outputBucketName, resourceName + ".json");
            indexObject(jsondoc, resourceName);

            return jsondoc.toString(); // "ok";
        } catch (Exception ex) {
            System.err.println("Exception in FDK " + ex.getMessage());
            ex.printStackTrace();
            return "oops";
        }
    }
*/
    static class ResponseHandler<IN, OUT> implements AsyncHandler<IN, OUT> {
        private Throwable failed = null;
        private CountDownLatch latch = new CountDownLatch(1);

        private void waitForCompletion() throws Exception {
            latch.await();
            if (failed != null) {
                if (failed instanceof Exception) {
                    throw (Exception) failed;
                }
                throw (Error) failed;
            }
        }

        @Override
        public void onSuccess(IN request, OUT response) {
            if (response instanceof PutObjectResponse) {
                System.out.println(
                        "New object md5: " + ((PutObjectResponse) response).getOpcContentMd5());
            } else if (response instanceof GetObjectResponse) {
                System.out.println("Object md5: " + ((GetObjectResponse) response).getContentMd5());
            }
            latch.countDown();
        }

        @Override
        public void onError(IN request, Throwable error) {
            failed = error;
            latch.countDown();
        }
    }

}
