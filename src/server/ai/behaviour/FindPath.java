package server.ai.behaviour;

import server.ai.AIBrain;
import server.ai.Intel;
import server.ai.Pathfinding.AStar;
import server.ai.Pathfinding.Node;
import server.ai.Task;
import server.game.Vector2;

import java.util.ArrayList;
import java.util.List;

/**
 * Defines a behaviour where the entity maps a path to the target.
 * Created by rhys on 2/16/17.
 */
public class FindPath extends Task {

    private boolean lineOfSight;

    public FindPath(Intel intel, AIBrain brain) {
        super(intel, brain);
    }

    /**
     * Determines whether this path-finding object needs to
     * use A* search to find a path or not.
     * @param lineOfSight - set to true if the target is within line of sight
     *                    of the entity.
     */
    public void setSimplePath(boolean lineOfSight){
        this.lineOfSight = lineOfSight;
    }

    @Override
    public boolean checkConditions() {
        return (intel.ent().isAlive() &&
                intel.getTargetLocation() != null);
    }

    @Override
    public void doAction() {
        ArrayList<Vector2> path = new ArrayList<>();
        if (lineOfSight) {
            path.add(intel.getTargetLocation());
            intel.resetPath(path);
        } else {
            // SKELETON CODE
            AStar aStar=new AStar(intel);
            Node target=new Node(intel.getTargetLocation(),intel.ent().getRadius(),intel.ent().getPhase(),intel,intel.getTargetLocation());
            aStar.makeGraph(target,intel.ent().getPhase());
            Node start=new Node(intel.ent().getPos(),intel.ent().getRadius(),intel.ent().getPhase(),intel,intel.getTargetLocation());
            aStar.AstarSearch(start,target);
            List<Node> printPath=aStar.printPath(target);
            for (Node node:printPath ) {
                path.add(new Vector2(node.getX(),node.getY()));
            }


            // PERQUISITE: A*

            intel.resetPath(path);
        }
        end();
    }
}
