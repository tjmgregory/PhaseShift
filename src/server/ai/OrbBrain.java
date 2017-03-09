package server.ai;

import server.ai.behaviour.*;

/**
 * Represents the brain of an Orb, making decisions on the Orb's behalf while taking
 * the Orb's situation and surroundings into account.
 * Created by Rhys on 2/20/17.
 */
public class OrbBrain extends AIBrain {

    private Sequence drift;     // Allows the Orb to drift aimlessly around the map when it is BORED.

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
    protected void configureBehaviours(){

        this.drift = new Sequence(intel, this);
        this.drift.add(behaviours.getBehaviour("Dawdle"));
        this.drift.add(behaviours.getBehaviour("Wander"));
        this.drift.add(behaviours.getBehaviour("FindPath"));
        this.drift.add(behaviours.getBehaviour("Travel"));
    }

    /**
     * Causes the Orb to carry out the most appropriate task, by evaluating
     * its current situation and surrounding environment.
     */
    public void doSomething(){

        // Perform checks.
        boolean playerNear = check.doCheck(Check.CheckMode.PROXIMITY);

        // Decide emotion.
        if (playerNear) {
            emotionTransition(EmotionalState.AGGRESSIVE);
        } else {
            emotionTransition(EmotionalState.BORED);
        }

        // Decide what to do.
        if (curEmotion == EmotionalState.BORED) {
            drift.doAction();
        }
        else if (curEmotion == EmotionalState.AGGRESSIVE) {
            // Compute/re-compute travel path if the target has moved since the last tick.
            if (check.doCheck(Check.CheckMode.TARGET_MOVED)) {
                intel.setTargetLocation(intel.getTargetPlayer().getPos());
                behaviours.getBehaviour("FindPath").run();
                behaviours.getBehaviour("Travel").start();
            } // Or if it hasn't...
            else {
                // Travel towards the target player if they're out of attacking range.
                if (!check.doCheck(Check.CheckMode.RANGE)) {
                    behaviours.getBehaviour("Travel").doAction();
                }
                // Or, if the target is in range, zap them.
                else {
                    behaviours.getBehaviour("Zap").run();
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
            behaviours.resetAll();
            if (newEmotion == EmotionalState.BORED){
                intel.ent().setSpeed(0.5F);
                drift.start();
            }
            else if (newEmotion == EmotionalState.AGGRESSIVE) {
                intel.ent().setSpeed(1F);
            }

            curEmotion = newEmotion;
        }
    }
}
