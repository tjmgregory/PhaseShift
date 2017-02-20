package server.ai.behaviour;

import server.ai.Intel;
import server.ai.OrbBrain;
import server.game.Orb;
import server.game.Player;

/**
 * Allows the entity to perform a very powerful, touching-range attack.
 * Restricted for use by Orbs only.
 * Created by rhys on 2/16/17.
 */
public class Zap extends Attack {

    private final double FREQUENCY = 10;
    private double ctr;

    public Zap(Intel intel, OrbBrain brain) {
        super(intel, brain);
        this.ctr = 0;
    }

    @Override
    public boolean checkConditions() {
        return (intel.ent().getPos().getDistanceTo(intel.getTargetPlayer().getPos())
                < intel.ent().getRadius()) &&
                (intel.ent() instanceof Orb);
    }

    @Override
    public void doAction() {
        if (ctr == FREQUENCY) {
            Player target = intel.getTargetPlayer();
            target.damage(45);
            ctr = 0;
        } else {
            ctr++;
        }
    }

    @Override
    public void reset() {
        super.reset();

    }
}