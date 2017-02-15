package server.game;

import server.ai.Behaviour;

/**
 * Created by peran on 01/02/17.
 */
public class Orb extends MovableEntity {

    private Behaviour behaviour;
    private Game gameState;

    /**
     * The basic AI controlled enemy
     * @param pos starting pos
     * @param dir starting dir
     * @param team the team the is on
     * @param phase starting phase
     */
    public Orb(Vector2 pos, Vector2 dir, int team, int phase, int id, Game gameState) {
        this.pos = pos;
        this.dir = dir;
        this.team = team;
        this.phase = phase;
        this.damageable = true;
        this.visible = true;
        maxHealth = 50;
        this.health = maxHealth;
        this.speed = 2;
        this.team = team;
        radius = 10;
        ID = id;
        this.gameState = gameState;
        // this.behaviour = new Wander();
    }

    /**
     * Makes this Orb behave in an appropriate manner when triggered by the Game Loop.
     */
    public void live() {
        if (behaviour.getState() == null) {
            behaviour.start();
        }
        behaviour.act(this, gameState);
    }
}