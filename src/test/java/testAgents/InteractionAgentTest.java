/*
This program has been developed by students from the bachelor Computer Science
at Utrecht University within the Software and Game project course.

©Copyright Utrecht University (Department of Information and Computing Sciences)
*/

package testAgents;

import agents.tactics.GoalStructureFactory;
import environments.EnvironmentConfig;
import environments.GymEnvironment;
import nl.uu.cs.aplib.mainConcepts.BasicAgent;
import world.BeliefState;

// Basic agent that combines movement with interactions to solve IntegrationTest.csv.
public class InteractionAgentTest {

    //@Test
    public void interactionAgent() throws InterruptedException {

        var g = GoalStructureFactory.chainButtonsToGoal("Goal", "button1", "button2", "button3");

        var agent = new BasicAgent().attachState(new BeliefState().setEnvironment(new GymEnvironment(new EnvironmentConfig("minimal"))));
        agent.setGoal(g);

        while (g.getStatus().inProgress()) {
            agent.update();
            Thread.sleep(30);
        }

        g.printGoalStructureStatus();
    }
}