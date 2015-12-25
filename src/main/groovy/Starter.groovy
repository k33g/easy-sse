import io.vertx.groovy.core.Vertx
import io.vertx.groovy.core.http.HttpServer
import io.vertx.groovy.core.http.HttpServerResponse
import io.vertx.groovy.ext.web.Router
import io.vertx.groovy.ext.web.handler.StaticHandler

import java.time.LocalDateTime

/**
 * completely inspired from http://www.smartjava.org/content/html5-server-sent-events-angularjs-nodejs-and-expressjs
 * this version is interesting:
 * https://github.com/cgrotz/vertx-sse/blob/master/src/main/java/de/cgrotz/vertx/sse/SseTestVerticleSimple.java
 */

List<HttpServerResponse> openConnections = []
Vertx vertx = Vertx.vertx()

HttpServer server = vertx.createHttpServer()
Router router = Router.router(vertx)

// do something each 1000 ms
vertx.setPeriodic(1000,  {
  // generate data
  Random random = new Random()

  openConnections.each { res ->

    StringBuilder sb = new StringBuilder('event: message\ndata: ')
    sb.append('{')
    sb.append('"when"').append(':"').append(LocalDateTime.now().toLocalTime().toString()).append('",')
    sb.append('"temperature"').append(':').append(random.nextInt(20)).append(',')
    sb.append('"humidity"').append(':').append(random.nextInt(100))
    sb.append('}').append('\n\n')

    res.write(sb.toString())

  }
});


router.get("/sse").handler({ context ->

  HttpServerResponse res = context.response()
  res.setChunked(true)

  // send headers for event-stream connection
  res.headers().add("Content-Type", "text/event-stream;charset=UTF-8")
  res.headers().add("Cache-Control", "no-cache")
  res.headers().add("Connection", "keep-alive")

  res.setStatusCode(200)
  res.write("\n")

  // push res object to our openConnections
  openConnections.add(res)

  // When the request is closed, e.g. the browser window
  // is closed. We search through the open connections
  // list and remove this connection.

  res.closeHandler({
    println("Bye!")
    openConnections.remove(res);
  })

})

// serve static assets
router.route("/*").handler(StaticHandler.create())

server.requestHandler(router.&accept).listen(8080)