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

import io.vertx.core.Vertx;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 * @author nghiatc
 * @since Oct 8, 2019
 *
 * cd ~/lab/labVertx/ntc-vertx-wiki mvn test
 */
//@RunWith(VertxUnitRunner.class)
//public class MainVerticleTest {
//
//    private Vertx vertx;
//
//    @Before
//    public void setUp(TestContext tc) {
//        vertx = Vertx.vertx();
//        vertx.deployVerticle(MainVerticle.class.getName(), tc.asyncAssertSuccess());
//    }
//
//    @After
//    public void tearDown(TestContext tc) {
//        vertx.close(tc.asyncAssertSuccess());
//    }
//
//    @Test
//    public void testThatTheServerIsStarted(TestContext tc) {
//        Async async = tc.async();
//        vertx.createHttpClient().getNow(8080, "localhost", "/", response -> {
//            tc.assertEquals(response.statusCode(), 200);
//            response.bodyHandler(body -> {
//                tc.assertTrue(body.length() > 0);
//                async.complete();
//            });
//        });
//    }
//
//}
