package client.graphics;

import client.ClientSettings;
import client.audio.Audio;
import objects.*;
import networking.Connection;
import org.lwjgl.Sys;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import server.game.*;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11.glDepthMask;

/**
 * Provides the visuals for the game itself.
 */
public class GameRenderer {

    private enum Mode {GAME,MENU,SCOREBOARD,GAMEOVER}
    private Mode mode = Mode.GAME;

    private static boolean gameRunning = true;

    private long lastFrame;
    private int fps;
    private long lastFPS;
    private int playerID;

    private GameData gameData;
    private MapRenderer map;
    private Connection conn;
    private CollisionManager collisions;

    private boolean fDown;
    private boolean clickDown;
    private boolean eDown;
    private boolean oneDown;
    private boolean twoDown;
    private boolean tabPressed;
    private boolean healthbar;
    private boolean gameMusic;
    private boolean muted;

    private Draw draw;
    private Pulse pulse;

    private boolean displayCollisions;
    private float rotation;

    public GameRenderer(GameData gd, Connection conn, int playerID) {
        super();
        this.conn = conn;
        this.gameData = gd;
        this.playerID = playerID;

        rotation = 0;
        fDown = false;
        clickDown = false;
        eDown = false;
        oneDown = false;
        twoDown = false;
        tabPressed = false;
        healthbar = true;
        gameMusic = false;
        muted = true;


        draw = new Draw();
        collisions = new CollisionManager(gd);
        displayCollisions = false;


        map = new MapRenderer(gd.getMapID());
        Player me = gameData.getPlayer(playerID);
        pulse = new Pulse(me.getPos(), me.getRadius(), me.getPhase(), 0, 1 - me.getPhase(), 20, 20, me.getPhase(), true);

        conn.addFunctionEvent("GameOver", this::gameOver);
    }

    private void gameOver(Sendable sendable) {
        gameData.updateScoreboard(((GameOver) sendable).getScoreboard());
        mode = Mode.GAMEOVER;
    }

    public void run(){
        switch (mode){
            case GAME:
                update();
                render();
                break;
            case MENU:
                //TODO Render in-game menu
                break;
            case SCOREBOARD:
                //TODO Show the scoreboard
                break;
            case GAMEOVER:
                TextRenderer textRenderer = new TextRenderer();
                textRenderer.drawText("Game over. To be changed.",0,0);
        }
    }

    private void update() {
        rotation += 1.5f;
        rotation %= 360;
        Player me = gameData.getPlayer(playerID);
        checkMusic(me);

        if (me.isAlive()) {

            Vector2 pos = me.getPos();

            float xPos = pos.getX();
            float yPos = pos.getY();

            int delta = getDelta();

            if (Keyboard.isKeyDown(Keyboard.KEY_A)) xPos -= 0.35f * delta;
            if (Keyboard.isKeyDown(Keyboard.KEY_D)) xPos += 0.35f * delta;
            if (Keyboard.isKeyDown(Keyboard.KEY_W)) yPos -= 0.35f * delta;
            if (Keyboard.isKeyDown(Keyboard.KEY_S)) yPos += 0.35f * delta;

            if (Keyboard.isKeyDown(Keyboard.KEY_M)) {
                if (!muted) {
                    muted = true;
                } else {
                    muted = false;
                    muteEverything();
                }
            }

            if (Keyboard.isKeyDown(Keyboard.KEY_F)) {
                fDown = true;
                if (muted) Audio.PHASE.play();
            } else if (fDown) {
                fDown = false;
                int newPhase = 0;
                if (me.getPhase() == 0) {
                    newPhase = 1;
                }
                Player p = new Player(me);
                p.setPhase(newPhase);
                if (collisions.validPosition(p)) {
                    conn.send(new PhaseObject(me.getID()));
                    pulse = new Pulse(me.getPos(), me.getRadius(), newPhase, 0, 1 - newPhase, 20, 20, newPhase, true);
                } else {
                    //invalid phase
                    pulse = new Pulse(me.getPos(), me.getRadius(), 0.3f, 0.3f, 0.3f, 20, 20, me.getPhase(), 250, false);
                }

            }
            if (Keyboard.isKeyDown(Keyboard.KEY_E)) {
                eDown = true;
            } else if (eDown) {
                eDown = false;
                if (me.isWeaponOneOut()) {
                    conn.send(new SwitchObject(me.getID(), false));
                } else {
                    conn.send(new SwitchObject(me.getID(), true));
                }
            }

            if (Keyboard.isKeyDown(Keyboard.KEY_1)) {
                oneDown = true;
            } else if (oneDown) {
                oneDown = false;
                conn.send(new SwitchObject(me.getID(), true));
            }

            if (Keyboard.isKeyDown(Keyboard.KEY_2)) {
                twoDown = true;
            } else if (twoDown) {
                twoDown = false;
                conn.send(new SwitchObject(me.getID(), false));
                displayCollisions = !displayCollisions;
            }

            if (Mouse.isButtonDown(0)) {
                if (!clickDown) {
                    conn.send(new FireObject(me.getID(), true));
                    clickDown = true;
                }
            } else if (clickDown) {
                if (me.activeWeapon() == 1 && muted)
                    Audio.SHOOT2.play();
                else if (muted) {
                    Audio.SHOOT.play();
                }
                conn.send(new FireObject(me.getID(), false));
                clickDown = false;
            }


            // keep quad on the screen
            if (xPos < 0) xPos = 0;
            if (xPos > 800) xPos = 800;
            if (yPos < 0) yPos = 0;
            if (yPos > 600) yPos = 600;

            if (pos.getX() != xPos || pos.getY() != yPos) {
                me.setPos(new Vector2(xPos, yPos));
                if (collisions.validPosition(me)) {
                    gameData.updatePlayer(me);
                } else {
                    me.setPos(pos);
                }
            }

            Vector2 tempPos = new Vector2(pos.getX(), ClientSettings.SCREEN_HEIGHT - pos.getY());
            Vector2 dir = getDirFromMouse(tempPos);
            if (!me.getDir().equals(dir)) {
                me.setDir(dir);
                conn.send(new MoveObject(me.getPos(), me.getDir(), playerID, me.getMoveCount()));
                gameData.updatePlayer(me);
            } else if (me.getPos().equals(pos)) {
                conn.send(new MoveObject(me.getPos(), me.getDir(), playerID, me.getMoveCount()));
            }
        }

        tabPressed = Keyboard.isKeyDown(Keyboard.KEY_TAB);

        updateFPS(); // update FPS Counter
    }

    public void render() {
        Player p = gameData.getPlayer(playerID);
        int phase = p.getPhase();
        draw.colourBackground(phase);

        if (pulse.isAlive() && pulse.isShowOtherPhase()) {
            drawStencil();
        } else {
            drawProjectiles(phase);
            map.renderMap(phase);
            drawOrbs(phase);
            drawPlayers(phase);
            drawPowerUps(phase);
            if (pulse.isAlive()) {
                pulse.draw();
            }
        }
        draw.drawHealthBar(p.getHealth(), p.getMaxHealth());
        draw.drawHeatBar(p.getWeaponOutHeat(), p.getActiveWeapon().getMaxHeat());

        if (tabPressed) {
            draw.shadeScreen();
        }

        if (displayCollisions) drawCollisions();
    }

    private Vector2 getDirFromMouse(Vector2 pos) {
        Vector2 mousePos = new Vector2(Mouse.getX(), Mouse.getY());
        Vector2 dir = pos.vectorTowards(mousePos);
        dir = dir.normalise();
        return new Vector2(dir.getX(), 0 - dir.getY());
    }

    private void positionBullet(Vector2 pos, Vector2 dir) {
        Vector2 cursor = pos.add((new Vector2(dir.getX(), 0 - dir.getY())).mult(21));
        float lastX = cursor.getX();
        float lastY = cursor.getY();

        if (lastX > 0 && lastY > 0)
            draw.drawCircle(lastX, lastY, 10, 50);
    }

    private void drawCollisions() {
        Player p = new Player(gameData.getPlayer(playerID));
        glColor4f(1, 0, 0, 0.5f);
        for (int i = 0; i < ClientSettings.SCREEN_WIDTH; i += 10) {
            for (int j = 0; j < ClientSettings.SCREEN_HEIGHT; j += 10) {
                p.setPos(new Vector2(i, j));
                if (!collisions.validPosition(p)) {
                    draw.drawCircle(i, ClientSettings.SCREEN_HEIGHT - j, 5, 5);
                }
            }
        }
    }

    private void drawStencil() {
        int newPhase = pulse.getNewPhase();
        int oldPhase = 1;
        if (newPhase == 1) oldPhase = 0;

        draw.colourBackground(oldPhase);
        drawProjectiles(oldPhase);
        map.renderMap(oldPhase);
        drawOrbs(oldPhase);
        drawPlayers(oldPhase);
        drawPowerUps(oldPhase);

        GL11.glEnable(GL11.GL_STENCIL_TEST);

        glColorMask(false, false, false, false);
        glStencilFunc(GL_ALWAYS, 1, 0xFF); // Set any stencil to 1
        glStencilOp(GL_KEEP, GL_KEEP, GL_REPLACE);
        glStencilMask(0xFF); // Write to stencil buffer
        glDepthMask(false); // Don't write to depth buffer
        glClear(GL_STENCIL_BUFFER_BIT); // Clear stencil buffer (0 by default)

        draw.drawCircle(pulse.getStart().getX(), ClientSettings.SCREEN_HEIGHT - pulse.getStart().getY(), pulse.getRadius(), 500);

        glStencilFunc(GL_EQUAL, 1, 0xFF); // Pass test if stencil value is 1
        glStencilMask(0x00); // Don't write anything to stencil buffer
        glDepthMask(true); // Write to depth buffer
        glColorMask(true, true, true, true);

        //GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

        GL11.glColor3f(0, 0, 0);
        draw.drawCircle(pulse.getStart().getX(), ClientSettings.SCREEN_HEIGHT - pulse.getStart().getY(), pulse.getRadius(), 500);
        draw.colourBackground(newPhase);
        drawProjectiles(newPhase);
        map.renderMap(newPhase);
        drawOrbs(newPhase);
        drawPlayers(newPhase);
        drawPowerUps(newPhase);

        GL11.glDisable(GL11.GL_STENCIL_TEST);

        pulse.draw();

    }

    private void drawPlayers(int phase) {
        ConcurrentHashMap<Integer, Player> players = gameData.getPlayers();
        int radius = players.get(0).getRadius();
        float red;
        float green;
        float blue;
        for (Player p : players.values()) {
            if (p.getPhase() == phase) {
                if (p.isAlive()) {
                    if (p.getTeam() == 0) {
                        red = 1;
                        green = 0.33f;
                        blue = 0.26f;
                    } else {
                        red = 0.2f;
                        green = 0.9f;
                        blue = 0.5f;
                    }
                } else {
                    red = 0.6f;
                    green = 0.6f;
                    blue = 0.7f;
                }
                draw.drawAura(p.getPos(), p.getRadius() + 10, 10, red - 0.2f, green - 0.2f, blue - 0.2f);
                GL11.glColor3f(red, green, blue);

                draw.drawCircle(p.getPos().getX(), ClientSettings.SCREEN_HEIGHT - p.getPos().getY(), radius, 100);

                positionBullet(new Vector2(p.getPos().getX(), ClientSettings.SCREEN_HEIGHT - p.getPos().getY()), p.getDir());
            }
        }
    }

    private void drawOrbs(int phase) {
        HashMap<Integer, Orb> orbs = gameData.getOrbs();
        Player me = gameData.getPlayer(playerID);
        float red;
        float green;
        float blue;
        for (Orb o : orbs.values()) {
            if (o.isAlive()) {
                red = 0.2f;
                green = 0.2f;
                blue = 1f;
            } else {
                red = 0.5f;
                green = 0.5f;
                blue = 0.7f;
            }
            if (phase == o.getPhase()) {
                draw.drawAura(o.getPos(), o.getRadius() + 5, 5, red - 0.1f, green - 0.1f, blue - 0.1f);
                glColor4f(red, green, blue, 1);
                draw.drawCircle(o.getPos().getX(), ClientSettings.SCREEN_HEIGHT - o.getPos().getY(), o.getRadius(), 100);
            } else {
                float dist = me.getPos().getDistanceTo(o.getPos());
                if (dist < 150) {
                    float fade = 0.7f - (dist / 150f);
                    draw.drawAura(o.getPos(), o.getRadius() + 5, 5, red - 0.1f, green - 0.1f, blue - 0.1f, fade);
                    glColor4f(red, green, blue, fade);
                    draw.drawCircle(o.getPos().getX(), ClientSettings.SCREEN_HEIGHT - o.getPos().getY(), o.getRadius(), 100);
                }
            }
        }
    }

    private void drawProjectiles(int phase) {
        ConcurrentHashMap<Integer, Projectile> projectiles = gameData.getProjectiles();
        float red;
        float green;
        float blue;
        for (Projectile p : projectiles.values()) {
            if (phase == p.getPhase()) {
                if (p.getTeam() == 0) {
                    red = 0.7f;
                    green = 0.1f;
                    blue = 0.1f;
                } else {
                    red = 0.1f;
                    green = 1f;
                    blue = 0.1f;
                }
                glColor3f(red, green, blue);
                float radius = p.getRadius();
                draw.drawCircle(p.getPos().getX(), ClientSettings.SCREEN_HEIGHT - p.getPos().getY(), radius, 100);
                draw.drawAura(p.getPos(), radius + radius / 2, radius / 2, red, green, blue);
            }
        }
    }

    private void drawPowerUps(int phase) {
        HashMap<Integer, PowerUp> powerUps = gameData.getPowerUps();
        float red;
        float green;
        float blue;
        for (PowerUp p : powerUps.values()) {
            if (phase == p.getPhase() && p.isAlive()) {
                if (p.getType() == PowerUp.Type.health) {
                    red = 0.7f;
                    green = 0.1f;
                    blue = 0.1f;
                } else {
                    red = 0.1f;
                    green = 1f;
                    blue = 0.1f;
                }
                float radius = p.getRadius();
                draw.drawQuad(p.getPos(), rotation, radius, red, green, blue);
            }
        }
    }

    private long getTime() {
        return (Sys.getTime() * 1000) / Sys.getTimerResolution();
    }

    private int getDelta() {
        long time = getTime();
        int delta = (int) (time - lastFrame);
        lastFrame = time;
        return delta;
    }

    private void updateFPS() {
        if (getTime() - lastFPS > 1000) {
            Display.setTitle("FPS: " + fps);
            fps = 0;
            lastFPS += 1000;
        }
        fps++;
    }

    private void checkMusic(Player me) {
        if (me.getHealth() < 25 && healthbar && muted) {
            healthbar = false;
            gameMusic = true;
            Audio.GAMEMUSIC.stopClip();
            Audio.WARNING.playallTime();
        } else if (me.getHealth() > 25 && gameMusic && muted) {
            gameMusic = false;
            healthbar = true;
            Audio.WARNING.stopClip();
            Audio.GAMEMUSIC.playallTime();
        } else if (muted) {
            Audio.GAMEMUSIC.playallTime();
        }
    }

    private void muteEverything() {
        Audio.SHOOT.stopClip();
        Audio.SHOOT.stopClip();
        Audio.GAMEMUSIC.stopClip();
        Audio.WARNING.stopClip();
    }
}
