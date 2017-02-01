package server.game;

/**
 * Created by peran on 27/01/17.
 */
public class MovableEntity extends Entity {
    protected float speed;
    protected Vector2 dir;

    /**
     * A class intended for inheritence, should not be created
     */
    public MovableEntity() {}

    protected void move() {
        pos.add(dir.mult(speed));
    }

    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public Vector2 getDir() {
        return  dir;
    }

    public void setDir(float x, float y) {
        setDir(new Vector2(x, y));
    }

    public void setDir(Vector2 dir) {
        this.dir = dir;
    }
}