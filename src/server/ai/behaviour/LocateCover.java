package server.ai.behaviour;

import server.ai.AIBrain;
import server.ai.Intel;
import server.ai.OrbBrain;
import server.game.Vector2;

import java.util.Random;

/**
 * Created by rhys on 2/16/17.
 */
public class LocateCover extends Task {

    public LocateCover(Intel intel, AIBrain brain) {
        super(intel, brain);
    }

    @Override
    public boolean checkConditions() {
        return intel.ent().isAlive();
    }

    @Override
    public void doAction(){
        // SKELETON.
        // PERQUISITE: CollisionManager, Room detection.
        Random gen = new Random();
        float ranX = (float) gen.nextInt(intel.getMap().getMapWidth());
        float ranY = (float) gen.nextInt(intel.getMap().getMapLength());
        intel.setTargetLocation(new Vector2(ranX, ranY));
    }
}
