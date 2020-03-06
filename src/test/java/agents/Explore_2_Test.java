package agents;

import agents.tactics.*;
import environments.EnvironmentConfig;
import environments.LabRecruitsEnvironment;
import eu.iv4xr.framework.mainConcepts.TestAgent;
import game.LabRecruitsTestServer;
import game.Platform;
import helperclasses.datastructures.Vec3;
import logger.JsonLoggerInstrument;
import nl.uu.cs.aplib.Logging;
import nl.uu.cs.aplib.mainConcepts.*;
import world.BeliefState;

import static agents.TestSettings.*;
import static nl.uu.cs.aplib.AplibEDSL.*;
import static org.junit.jupiter.api.Assertions.* ;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

/**
 * Test that the agent can explore its way to find an entity (a button). 
 * The setup is a simple maze-like room. The room contains another room
 * which is initially unreachable due to a close door guarding it. In
 * the original implementation this subroom causes the explore-tactic to
 * get stuck (because it thinks the subroom is reachable, so it insists on
 * steering the agent to go there; or, when that problem is fixed, the
 * pathfinding algorithm ends up blocking too many navigation triangles, hence
 * making the agent unable to path-plan anywhere; the problem is "fixed",
 * and this test is to test this solution).
 */
public class Explore_2_Test {
	
	private static LabRecruitsTestServer labRecruitsTestServer;

    @BeforeAll
    static void start() {
    	// Uncomment this to make the game's graphic visible:
    	//TestSettings.USE_GRAPHICS = true ;
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

    /**
     * Test that the agent can continue to explore despite a nearby closed-room.
     */
    @Test
    public void test_explore_on_maze_with_closedroom() throws InterruptedException {

    	var environment = new LabRecruitsEnvironment(new EnvironmentConfig("button1_opens_door1_v2"));
    	
    	// set this to true if we want to see the commands send through the Environment
        // USE_INSTRUMENT = true ;
        if(USE_INSTRUMENT)
            environment.registerInstrumenter(new JsonLoggerInstrument()).turnOnDebugInstrumentation();

        BeliefState state = new BeliefState().setEnvironment(environment);
        state.id = "agent1";
        LabRecruitsTestAgent agent = new LabRecruitsTestAgent(state);
        
        var g = GoalLib.entityReachedAndInteracted("button1") ;
        
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
            if (i>90) {
            	break ;
            }
        }
        
        assertTrue(g.getStatus().success()) ;
        var agent_p = agent.getState().position ;
        var button_p = agent.getState().getEntity("button1").position ;
        assertTrue(agent_p.distance(button_p) < 0.5) ;

        g.printGoalStructureStatus();

        if (!environment.close())
            throw new InterruptedException("Unity refuses to start the Simulation!");

    }
    
}
