/*
 * Copyright (c) 2016. Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 * http://aws.amazon.com/apache2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package com.ibm.cloud.objectstorage.http;

import static com.ibm.cloud.objectstorage.http.HttpResponseHandler.X_AMZN_REQUEST_ID_HEADER;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.junit.Assert.assertThat;

import com.ibm.cloud.objectstorage.AmazonServiceException;
import com.ibm.cloud.objectstorage.ClientConfiguration;
import com.ibm.cloud.objectstorage.transform.Unmarshaller;
import com.ibm.cloud.objectstorage.util.LogCaptor;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang.RandomStringUtils;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Ignore;  // IBM
import org.junit.Test;
import org.w3c.dom.Node;
import utils.http.WireMockTestBase;

@Ignore("IBM - Backend logger used in this test is no longer supported")
public class DefaultErrorResponseHandlerIntegrationTest extends WireMockTestBase {

    private static final String RESOURCE = "/some-path";
    // IBM private LogCaptor logCaptor = new LogCaptor.DefaultLogCaptor(Level.DEBUG);
    private final AmazonHttpClient client = new AmazonHttpClient(new ClientConfiguration());
    private final DefaultErrorResponseHandler sut = new DefaultErrorResponseHandler(new ArrayList<Unmarshaller<AmazonServiceException, Node>>());

    @Before
    public void setUp() {
        // IBM logCaptor.clear();
    }

    @Test
    public void invocationIdIsCapturedInTheLog() throws Exception {
        stubFor(get(urlPathEqualTo(RESOURCE)).willReturn(aResponse().withStatus(418)));

        executeRequest();

        // IBM Matcher<Iterable<? super LoggingEvent>> matcher = hasItem(hasEventWithContent("Invocation Id"));
        // IBM assertThat(debugEvents(), matcher);
    }

    @Test
    public void invalidXmlLogsXmlContentToDebug() throws Exception {
        String content = RandomStringUtils.randomAlphanumeric(10);
        stubFor(get(urlPathEqualTo(RESOURCE)).willReturn(aResponse().withStatus(418).withBody(content)));

        executeRequest();

        // IBM Matcher<Iterable<? super LoggingEvent>> matcher = hasItem(hasEventWithContent(content));
        // IBM assertThat(debugEvents(), matcher);
    }

    @Test
    public void requestIdIsLoggedWithDebugIfInTheHeader() throws Exception {
        String requestId = RandomStringUtils.randomAlphanumeric(10);

        stubFor(get(urlPathEqualTo(RESOURCE)).willReturn(aResponse().withStatus(418).withHeader(X_AMZN_REQUEST_ID_HEADER, requestId)));

        executeRequest();

        // IBM Matcher<Iterable<? super LoggingEvent>> matcher = hasItem(hasEventWithContent(requestId));
        // IBM assertThat(debugEvents(), matcher);
    }

    private void executeRequest() {
        expectException(new Runnable() {
            @Override
            public void run() {
                client.requestExecutionBuilder().errorResponseHandler(sut).request(newGetRequest(RESOURCE)).execute();
            }
        });
    }

    @SuppressWarnings("EmptyCatchBlock")
    private void expectException(Runnable r) {
        try {
            r.run();
            throw new RuntimeException("Expected exception, got none");
        } catch (Exception e) {
        }
    }

    // IBM private List<LoggingEvent> debugEvents() {
    // IBM     List<LoggingEvent> events = new ArrayList<LoggingEvent>();
    // IBM     List<LoggingEvent> loggingEvents = logCaptor.loggedEvents();
    // IBM     for (LoggingEvent le : loggingEvents) {
    // IBM         if (le.getLevel().equals(Level.DEBUG)) {
    // IBM             events.add(le);
    // IBM         }
    // IBM     }
    // IBM     return events;
    // IBM }

    // IBM private org.hamcrest.Matcher<? super LoggingEvent> hasEventWithContent(String content) {
    // IBM     return hasProperty("message", containsString(content));
    // IBM }
}

