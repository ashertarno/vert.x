/*
 * Copyright 2011-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package vertx.tests.core.http;

import org.vertx.java.core.Handler;
import org.vertx.java.core.SimpleHandler;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.HttpServer;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.shareddata.SharedData;
import org.vertx.java.deploy.Verticle;
import org.vertx.java.framework.TestUtils;

/**
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
public class TLSServer extends Verticle {

  protected TestUtils tu = new TestUtils();

  private HttpServer server;

  public void start() {
    TLSTestParams params = TLSTestParams.deserialize(SharedData.instance.<String, byte[]>getMap("TLSTest").get("params"));

    server = new HttpServer();

    server.setSSL(true);

    if (params.serverTrust) {
      server.setTrustStorePath("./src/tests/keystores/server-truststore.jks").setTrustStorePassword
          ("wibble");
    }
    if (params.serverCert) {
      server.setKeyStorePath("./src/tests/keystores/server-keystore.jks").setKeyStorePassword("wibble");
    }
    if (params.requireClientAuth) {
      server.setClientAuthRequired(true);
    }

    server.requestHandler(new Handler<HttpServerRequest>() {
      public void handle(final HttpServerRequest req) {

        tu.checkContext();

        req.bodyHandler(new Handler<Buffer>() {
          public void handle(Buffer buffer) {
            tu.checkContext();
            tu.azzert("foo".equals(buffer.toString()));
            req.response.end("bar");
          }
        });
      }
    }).listen(4043);

    tu.appReady();
  }

  public void stop() {
    server.close(new SimpleHandler() {
      public void handle() {
        tu.checkContext();
        tu.appStopped();
      }
    });
  }

}