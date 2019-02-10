package ir.rkr.temp.rest


import com.google.gson.GsonBuilder
import com.typesafe.config.Config

import ir.rkr.temp.utils.TempMetrics
import ir.rkr.temp.version
import org.eclipse.jetty.http.HttpStatus
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.server.ServerConnector
import org.eclipse.jetty.servlet.ServletContextHandler
import org.eclipse.jetty.servlet.ServletHolder
import org.eclipse.jetty.util.thread.QueuedThreadPool
import java.util.function.Supplier
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse


/**
 * [JettyRestServer] is a rest-based service to handle requests of redis cluster with an additional
 * in-memory cache layer based on ignite to increase performance and decrease number of requests of
 * redis cluster.
 */


class JettyRestServer(config: Config, tempMetrics: TempMetrics) : HttpServlet() {

    private val gson = GsonBuilder().disableHtmlEscaping().create()

    /**
     * Start a jetty server.
     */
    init {

        val threadPool = QueuedThreadPool(100, 20)
        val server = Server(threadPool)
        val http = ServerConnector(server).apply {
            host = config.getString("metrics.ip")
            port = config.getInt("metrics.port")
        }

        server.addConnector(http)

        tempMetrics.addGauge("Version", Supplier { version })

        val handler = ServletContextHandler(server, "/")

        handler.addServlet(ServletHolder(object : HttpServlet() {
            override fun doGet(req: HttpServletRequest, resp: HttpServletResponse) {

                tempMetrics.MarkMetricsCall(1)
                resp.apply {
                    status = HttpStatus.OK_200
                    addHeader("Content-Type", "application/json; charset=utf-8")
                    //addHeader("Connection", "close")
                    writer.write(gson.toJson(tempMetrics.getInfo()))
                }
            }
        }), "/metrics")

        handler.addServlet(ServletHolder(object : HttpServlet() {
            override fun doGet(req: HttpServletRequest, resp: HttpServletResponse) {
                resp.apply {
                    status = HttpStatus.OK_200
                    addHeader("Content-Type", "text/plain; charset=utf-8")
                    addHeader("Connection", "close")
                    writer.write("server  is running :D")
                }
            }
        }), "/version")

        server.start()

    }
}