package ir.rkr.temp

import com.typesafe.config.ConfigFactory
import ir.rkr.temp.rest.JettyRestServer
import ir.rkr.temp.utils.TempMetrics
import mu.KotlinLogging

const val version = 0.1


/**
 * GoldPan main entry point.
 */

fun main(args: Array<String>) {

    val logger = KotlinLogging.logger {}
    val config = ConfigFactory.defaultApplication()
    val tempMetrics = TempMetrics()

    JettyRestServer( config, tempMetrics)

}