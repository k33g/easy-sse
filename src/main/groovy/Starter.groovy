import io.vertx.core.json.Json
import io.vertx.groovy.core.Vertx
import io.vertx.groovy.core.http.HttpServer
import io.vertx.groovy.core.http.HttpServerResponse
import io.vertx.groovy.ext.web.Router
import io.vertx.groovy.ext.web.handler.StaticHandler

import java.time.LocalDateTime
/**
 * completely inspired from http://www.smartjava.org/content/html5-server-sent-events-angularjs-nodejs-and-expressjs
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

    LocalDateTime date = LocalDateTime.now()
    String uniqueId = 'id: ' + date.toLocalTime().toString() + '\n'
    String data = 'data:' + Json.encodePrettily([
        "temperature": random.nextInt(20),
        "humidity": random.nextInt(100)
    ]) +   '\n\n'

    res.write(uniqueId)
    res.write(data)
  }
});


router.get("/sse").handler({ context ->

  HttpServerResponse res = context.response()

  // send headers for event-stream connection
  res.headers().add("Content-Type", "text/event-stream")
  res.headers().add("Cache-Control", "no-cache")
  res.headers().add("Connection", "keep-alive")

  res.setChunked(true)
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