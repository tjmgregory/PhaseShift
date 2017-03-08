package server.ai;

import server.ai.behaviour.*;

/**
 * Represents the brain of an Orb, making decisions on the Orb's behalf while taking
 * the Orb's situation and surroundings into account.
 * Created by Rhys on 2/20/17.
 */
public class OrbBrain extends AIBrain {

    private Intel intel;        // Stores information that the Orb needs for making decisions.
    private Check check;        // Allows the Orb to carry out a variety of checks pertaining to its surroundings.
    private Feel feel;          // Allows the Orb to determine its current emotional state.
    private Sequence flee;      // Allows the Orb to escape when it is SCARED.
    private Sequence drift;     // Allows the Orb to drift aimlessly around the map when it is BORED.
    private FindPath pathfinder;// Given a target location, determines a path for how to get there.
    private Travel traveller;   // Allows the Orb to travel along a predetermined path.
    private Zap zapper;         // Allows the Orb to damage an enemy player when in touching range.

    /**
     * Constructs an Orb's Brain - the decision maker of an Orb.
     * @param intel - The Intel object the brain utilises to make decisions.
     */
    public OrbBrain(Intel intel) {
        super(intel);
    }

    /**
     * Initialises all tasks and task-sequences that the Orb can carry out.
     */
    protected void constructBehaviours(){

        this.drift = new Sequence(intel, this);
        this.drift.add(new Dawdle(intel, this));
        this.drift.add(new Wander(intel, this));
        this.drift.add(new FindPath(intel, this));
        this.drift.add(new Travel(intel, this));

        this.flee = new Sequence(intel, this);
        this.flee.add(new LocateCover(intel, this));
        this.flee.add(new FindPath(intel, this));
        this.flee.add(new Travel(intel, this));

        this.pathfinder = new FindPath(intel, this);
        this.traveller = new Travel(intel, this);
        this.zapper = new Zap(intel, this);
    }

    /**
     * Causes the Orb to carry out the most appropriate task, by evaluating
     * its current situation and surrounding environment.
     */
    public void doSomething(){

        // Perform checks.
        boolean inPain = check.doCheck(Check.CheckMode.HEALTH);
        boolean playerNear = check.doCheck(Check.CheckMode.PROXIMITY);

        // Find emotion.
        feel.setParameters(inPain, playerNear);
        feel.doFinal();

        // Decide what to do.
        if (curEmotion == EmotionalState.INTIMIDATED){
            flee.doAction();
        }
        else if (curEmotion == EmotionalState.BORED) {
            drift.doAction();
        }
        else if (curEmotion == EmotionalState.AGGRESSIVE) {
            // Compute/re-compute travel path if the target has moved since the last tick.
            if (check.doCheck(Check.CheckMode.TARGET_MOVED)) {
                intel.setTargetLocation(intel.getTargetPlayer().getPos());
                pathfinder.run();
                traveller.start();
            } // Or if it hasn't...
            else {
                // Travel towards the target player if they're out of attacking range.
                if (!check.doCheck(Check.CheckMode.RANGE)) {
                    traveller.doAction();
                }
                // Or, if the target is in range, zap them.
                else {
                    zapper.run();
                }
            }
        }
    }

    /**
     * Sets the Orb's emotional state for this tick and compares it with the emotional state
     * of the last tick, resetting progress in all task-sequences if the emotional state has changed.
     * @param newEmotion - the Orb's emotional state for this tick.
     */
    public void emotionTransition(EmotionalState newEmotion){
        if (newEmotion != curEmotion) {
            //System.out.println("Changing emotion to " + newEmotion);
            flee.reset();
            drift.reset();
            pathfinder.reset();
            zapper.reset();
            traveller.reset();
            if (newEmotion == EmotionalState.BORED){
                intel.ent().setSpeed(0.5F);
                drift.start();
            }
            else if (newEmotion == EmotionalState.AGGRESSIVE) {
                intel.ent().setSpeed(1F);
            }
            else if (newEmotion == EmotionalState.INTIMIDATED) {
                intel.ent().setSpeed(3F);
                flee.start();
            }

            curEmotion = newEmotion;
        }
    }
}
