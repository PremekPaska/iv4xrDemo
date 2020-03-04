/*
This program has been developed by students from the bachelor Computer Science
at Utrecht University within the Software and Game project course.

©Copyright Utrecht University (Department of Information and Computing Sciences)
*/

package agents;

import agents.tactics.GoalLib;
import agents.tactics.TacticLib;
import agents.tactics.TestGoalFactory;
import environments.EnvironmentConfig;
import environments.LabRecruitsEnvironment;
import eu.iv4xr.framework.mainConcepts.TestAgent;
import eu.iv4xr.framework.mainConcepts.TestDataCollector;
import game.LabRecruitsTestServer;
import game.Platform;
import helperclasses.datastructures.Vec3;
import logger.JsonLoggerInstrument;
import nl.uu.cs.aplib.Logging;
import nl.uu.cs.aplib.mainConcepts.*;
import world.BeliefState;

import static agents.TestSettings.USE_GRAPHICS;
import static agents.TestSettings.USE_INSTRUMENT;
import static agents.TestSettings.USE_SERVER_FOR_TEST;
import static eu.iv4xr.framework.Iv4xrEDSL.assertTrue_;
import static nl.uu.cs.aplib.AplibEDSL.*;
import static org.junit.jupiter.api.Assertions.* ;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

// Basic agent that combines movement with interactions to solve Default.csv.
public class DefaultTest {
	
	private static LabRecruitsTestServer labRecruitsTestServer;

    @BeforeAll
    static void start() {
    	// Uncomment this to make the game's graphic visible:
    	// TestSettings.USE_GRAPHICS = true ;
    	String labRecruitesExeRootDir = System.getProperty("user.dir") ;
        if(USE_SERVER_FOR_TEST){
            labRecruitsTestServer =new LabRecruitsTestServer(
                    USE_GRAPHICS,
                    Platform.PathToLabRecruitsExecutable(labRecruitesExeRootDir));
            labRecruitsTestServer.waitForGameToLoad();
        }
    }

    @AfterAll
    static void close() { if(USE_SERVER_FOR_TEST) labRecruitsTestServer.close(); }


    @Test
    public void defaultAgent() throws InterruptedException {

    	var environment = new LabRecruitsEnvironment(new EnvironmentConfig("smallmaze"));
    	//var environment = new LabRecruitsEnvironment(new EnvironmentConfig("button1_opens_door1_v2"));
    	//var environment = new LabRecruitsEnvironment(new EnvironmentConfig("square"));
        // set this to true if we want to see the commands send through the Environment
        // USE_INSTRUMENT = true ;
        if(USE_INSTRUMENT)
            environment.registerInstrumenter(new JsonLoggerInstrument()).turnOnDebugInstrumentation();

        BeliefState state = new BeliefState().setEnvironment(environment);
        state.id = "agent1";
        LabRecruitsTestAgent agent = new LabRecruitsTestAgent(state);
        
        //var g = GoalLib.buttonsVisited_thenGoalVisited("door1", "button1");
        var g = SEQ(justObserve(),GoalLib.entityReached("button1").lift()) ;
        //var g = GoalLib.entityReachedAndInteracted("button1") ;
        
        agent.setGoal(g) ;

     // press play in Unity
        if (! environment.startSimulation())
            throw new InterruptedException("Unity refuses to start the Simulation!");

        int i = 0 ;
        while (g.getStatus().inProgress()) {
            agent.update();
            System.out.println("*** " + i + ", " + agent.getState().id + " @" + agent.getState().position) ;
            Thread.sleep(30);
            i++ ;
            if (i>120) {
            	break ;
            }
        }

        g.printGoalStructureStatus();

        if (!environment.close())
            throw new InterruptedException("Unity refuses to start the Simulation!");

    }
    
    static GoalStructure justObserve(){
        return goal("observe").toSolve((BeliefState b) -> b.position != null).withTactic(TacticLib.observe()).lift();
    }

    //@Test
    public void defaultTest()  {
        var game_env = new LabRecruitsEnvironment(new EnvironmentConfig("minimal"));
        var state = new BeliefState().setEnvironment(game_env);
        var agent = new TestAgent().attachState(state);
        state.id = "0";

        var goalPosition = new Vec3(7,0,7);

        var info = "Testing Default.csv";

        // Assert button was not pressed when walking to a position.
        GoalStructure g = null ;
        /* ******
        var g = TestGoalFactory.reachPosition(goalPosition)
                .oracle(agent, (BeliefState b) -> assertTrue_("", info,
                        b.getInteractiveEntity("Button 1") != null && !b.getInteractiveEntity("Button 1").isActive))
                .lift();
        */
        var dataCollector = new TestDataCollector();
        agent.setTestDataCollector(dataCollector);

        agent.setGoal(g);

        while (g.getStatus().inProgress()) {
            agent.update();
        }

        assertEquals(0, dataCollector.getNumberOfFailVerdictsSeen());
        assertEquals(1, dataCollector.getNumberOfPassVerdictsSeen());
        Logging.getAPLIBlogger().info("TEST END.");

        // *** g.printGoalStructureStatus();
    }
}