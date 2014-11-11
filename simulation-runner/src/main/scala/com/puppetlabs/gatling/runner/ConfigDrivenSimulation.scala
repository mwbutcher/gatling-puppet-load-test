package com.puppetlabs.gatling.runner

import io.gatling.core.Predef._
import io.gatling.core.scenario.Simulation
import io.gatling.core.session
import io.gatling.http.Predef._
import com.puppetlabs.gatling.config.PuppetGatlingConfig
import io.gatling.core.scenario.Simulation
import io.gatling.core.structure.{ScenarioBuilder, PopulatedScenarioBuilder, ChainBuilder}
import scala.util.{Try, Success, Failure}


import scala.util.Try


//import com.excilys.ebi.gatling.core.structure.{ChainBuilder}
import io.gatling.core.structure.{PopulatedScenarioBuilder, ChainBuilder}

/**
 * This class is the "main" Simulation class that we'll always point
 * Gatling at.  Its job is to wrap one or more puppet node simulations
 * based on the configuration specified in the config file
 */
class ConfigDrivenSimulation extends Simulation {

  val REPETITION_COUNTER: String = "repetitionCounter"

  def makeLongRunning(chain:ChainBuilder, totalNumReps:Int): ChainBuilder = {
    // This is kind of a dirty hack.  Here's the deal.
    // In order to simulate real world agent runs, we need to sleep 30 minutes
    // in between each series of agent requests.  That can be achieved
    // easily by adding a "pause" to the end of the run.
    // However, if we do that, then after the final series of requests, we'll sleep
    // for 30 minutes before the simulation can end, even though that is entirely
    // unnecessary.  Since most of our jenkins jobs are going to run 2-6 sims,
    // that would mean we're sleeping for 1-3 extra hours and uselessly tying up the
    // hardware.  Thus, we need to make the sleep conditional based on whether
    // or not we're on the final repetition.

    // Here we've replaced our "pause" with a Gatling "session function",
    // which basically just sets a session variable to check to see if
    // we are on the final repetition, and if not, sleep for 30 mins.
    val sleepDuration = 1800    // 30 minutes
    chain.exec((session: Session) => {
      val repetitionCount = session(REPETITION_COUNTER).asOption[Int].getOrElse(0) + 1
            println("Agent " + session.userId + " completed " + repetitionCount + " of " + totalNumReps + " repetitions.")
      session.set(REPETITION_COUNTER, repetitionCount)
    }).doIf((session) => session(REPETITION_COUNTER).as[Int] < totalNumReps) {
      exec((session) => {
                  println("This is not the last repetition; sleeping " + sleepDuration + ".")
        session
      }).pause(sleepDuration)
    }.doIf((session) => session(REPETITION_COUNTER).as[Int] >= totalNumReps) {
      exec((session) => {
                 println("That was the last repetition. Not sleeping.")
        session
      })
    }
  }


  val config = PuppetGatlingConfig.configuration

  val httpConf = http
    .baseURL(config.baseUrl)
    .acceptHeader("pson, b64_zlib_yaml, yaml, raw")
    .connection("close")

  val scns: List[PopulatedScenarioBuilder] = config.nodes.map(node => {

    import node._

    val sim: SimulationWithScenario = simulationClass.newInstance

    val chainWithFailFast:ChainBuilder =
      // this wrapper causes the agent sims to exit the series of
      // of requests upon the first failure, rather than continuing
      // to send up the remaining requests for the agent run.
      exitBlockOnFail {
        exec(sim.scn)
      }

    val chainWithLongRunning:ChainBuilder =
      // this adds in the 30 minute sleeps at the end of each agent
      // run, if we're configured as a "long running" run.
      if (config.isLongRunning) {
        makeLongRunning(chainWithFailFast, numRepetitions)
      } else {
        chainWithFailFast
      }

    scenario(simulationClass.getSimpleName)
      .repeat(numRepetitions) {
        group((session) => simulationClass.getSimpleName) {
          chainWithLongRunning
        }
      }.inject(rampUsers(numInstances) over (rampUpDuration))
    .protocols(httpConf)
  })

  scns.foreach(scn => setUp(scn))
}
