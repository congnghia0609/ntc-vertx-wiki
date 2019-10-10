/*
 * Copyright 2019 nghiatc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.vertx.starter;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.JksOptions;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.ext.web.codec.BodyCodec;
import io.vertx.starter.database.WikiDatabaseVerticle;
import io.vertx.starter.http.AuthInitializerVerticle;
import io.vertx.starter.http.HttpServerVerticle;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 * @author nghiatc
 * @since Oct 9, 2019
 * 
 * cd ~/lab/labVertx/ntc-vertx-wiki
 * mvn test
 */
@RunWith(VertxUnitRunner.class)
public class ApiTest {
    private Vertx vertx;
    private WebClient webClient;
    private String jwtTokenHeaderValue;

    @Before
    public void prepare(TestContext context) {
        vertx = Vertx.vertx();

        JsonObject dbConf = new JsonObject()
                .put(WikiDatabaseVerticle.CONFIG_WIKIDB_JDBC_URL, "jdbc:hsqldb:mem:testdb;shutdown=true")
                .put(WikiDatabaseVerticle.CONFIG_WIKIDB_JDBC_MAX_POOL_SIZE, 4);

        vertx.deployVerticle(new AuthInitializerVerticle(),
                new DeploymentOptions().setConfig(dbConf), context.asyncAssertSuccess());
        
        vertx.deployVerticle(new WikiDatabaseVerticle(),
                new DeploymentOptions().setConfig(dbConf), context.asyncAssertSuccess());

        vertx.deployVerticle(new HttpServerVerticle(), context.asyncAssertSuccess());

        webClient = WebClient.create(vertx, new WebClientOptions()
                .setDefaultHost("localhost")
                .setDefaultPort(8080));
//        webClient = WebClient.create(vertx, new WebClientOptions()
//                .setDefaultHost("localhost")
//                .setDefaultPort(8080)
//                .setSsl(true)
//                .setTrustOptions(new JksOptions().setPath("server-keystore.jks").setPassword("secret4321"))); 
    }

    @After
    public void finish(TestContext context) {
        vertx.close(context.asyncAssertSuccess());
    }
    
    @Test
    public void play_with_api(TestContext context) {
        Async async = context.async();
        
        Promise<HttpResponse<String>> tokenPromise = Promise.promise();
        webClient.get("/api/token")
                .putHeader("login", "foo")
                .putHeader("password", "bar")
                .as(BodyCodec.string())
                .send(tokenPromise);
        Future<HttpResponse<String>> tokenFuture = tokenPromise.future();

        JsonObject page = new JsonObject()
                .put("name", "Sample")
                .put("markdown", "# A page");

//        Promise<HttpResponse<JsonObject>> postPagePromise = Promise.promise();
//        webClient.post("/api/pages")
//                .as(BodyCodec.jsonObject())
//                .sendJsonObject(page, postPagePromise);
//
//        Future<HttpResponse<JsonObject>> getPageFuture = postPagePromise.future().compose(resp -> {
//            Promise<HttpResponse<JsonObject>> promise = Promise.promise();
//            webClient.get("/api/pages")
//                    .as(BodyCodec.jsonObject())
//                    .send(promise);
//            return promise.future();
//        });
        
        Future<HttpResponse<JsonObject>> postPageFuture = tokenFuture.compose(tokenResponse -> {
            Promise<HttpResponse<JsonObject>> promise = Promise.promise();
            jwtTokenHeaderValue = "Bearer " + tokenResponse.body();
            webClient.post("/api/pages")
                    .putHeader("Authorization", jwtTokenHeaderValue)
                    .as(BodyCodec.jsonObject())
                    .sendJsonObject(page, promise);
            return promise.future();
        });
        
        Future<HttpResponse<JsonObject>> getPageFuture = postPageFuture.compose(resp -> {
            Promise<HttpResponse<JsonObject>> promise = Promise.promise();
            webClient.get("/api/pages")
                    .putHeader("Authorization", jwtTokenHeaderValue)
                    .as(BodyCodec.jsonObject())
                    .send(promise);
            return promise.future();
        });

        Future<HttpResponse<JsonObject>> updatePageFuture = getPageFuture.compose(resp -> {
            JsonArray array = resp.body().getJsonArray("pages");
            context.assertEquals(1, array.size());
            context.assertEquals(0, array.getJsonObject(0).getInteger("id"));
            Promise<HttpResponse<JsonObject>> promise = Promise.promise();
            JsonObject data = new JsonObject()
                    .put("id", 0)
                    .put("markdown", "Oh Yeah!");
            webClient.put("/api/pages/0")
                    .putHeader("Authorization", jwtTokenHeaderValue)
                    .as(BodyCodec.jsonObject())
                    .sendJsonObject(data, promise);
            return promise.future();
        });

        Future<HttpResponse<JsonObject>> deletePageFuture = updatePageFuture.compose(resp -> {
            context.assertTrue(resp.body().getBoolean("success"));
            Promise<HttpResponse<JsonObject>> promise = Promise.promise();
            webClient.delete("/api/pages/0")
                    .putHeader("Authorization", jwtTokenHeaderValue)
                    .as(BodyCodec.jsonObject())
                    .send(promise);
            return promise.future();
        });

        deletePageFuture.setHandler(ar -> {
            if (ar.succeeded()) {
                context.assertTrue(ar.result().body().getBoolean("success"));
                async.complete();
            } else {
                context.fail(ar.cause());
            }
        });

        async.awaitSuccess(5000);
    }
}
