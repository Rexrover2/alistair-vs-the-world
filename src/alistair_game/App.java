// Main App handler for Alistair-themed Tower Defence Game

package alistair_game;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.newdawn.slick.*;

/**
 * Main handler for the game as a program.
 *
 * Creates a World to handle the gameplay itself.
 */
public class App extends BasicGame {

    private static final int
        WINDOW_W = 960, WINDOW_H = 672, TILE_SIZE = 48,
        GRID_W = WINDOW_W / TILE_SIZE, GRID_H = WINDOW_H / TILE_SIZE,
        MAXWAVES = 50, MAXSPAWNS = 1000;

    private World world;

    public static void main(String[] args) {
        try {
            System.out.println("Yeet, starting main");

            App game = new App("Alistair vs The World");
            AppGameContainer appgc = new AppGameContainer(game);
            appgc.setDisplayMode(WINDOW_W, WINDOW_H, false);
            appgc.start();

            System.err.println("GAME STATE: Game forced exit");
        } catch (SlickException e) {
            Logger.getLogger(App.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    public App(String title) {
        super(title);
    }

    /** Calls world to initializes all game object before the game begins.
     *
     * Sets game parametrs and loads up files.
     * */
    @Override
    public void init(GameContainer gc) throws SlickException {
        System.out.println("GAME STATE: Initialising game...");
        gc.setShowFPS(false);

        // Game update speed. 1 tick every 20 ms (50/sec)
        gc.setMaximumLogicUpdateInterval(20);
        gc.setMinimumLogicUpdateInterval(20);

        // Initialise level from file and create world object
        try {
            // 2D grid array
            int[][] level = new int[GRID_W][GRID_H];

            // Load map info file
            Scanner scanner = new Scanner(new File("assets\\levels\\level1.txt"));
            for (int y = 0; y < GRID_H; y++) {
                assert (scanner.hasNext());
                char[] line = scanner.next().toCharArray();
                int x = 0;
                for (char c : line) {
                    if (x >= GRID_W)
                        break;
                    assert (Character.isDigit(c));
                    level[x++][y] = Character.getNumericValue(c);
                }
            }

            // Enemy spawn location
            float startx = (float) scanner.nextInt() * TILE_SIZE + TILE_SIZE / 2;
            float starty = (float) scanner.nextInt() * TILE_SIZE + TILE_SIZE / 2;
            scanner.close();


            // Load in wave info
            scanner = new Scanner(new File("assets\\waves\\game1.txt"));
            // Read line-by-line
            scanner.useDelimiter("[\\r\\n;]+");

            ArrayList<Wave> waves = new ArrayList<>();
            int wavenum = 1, k = 0;

            // Wave-by-wave
            while (scanner.hasNext()) {
                String wave = scanner.next();
                Wave currWave = new Wave(wavenum);
                waves.add(currWave);

                // Split into spawn sequences - enemytype/enemynum/spawnrate/starttime
                String[] spawnSequences = wave.split(" ");
                int seqs = spawnSequences.length;

                for (int i = seqs-1; i >= 0; i--) {
                    String seq = spawnSequences[i];

                    // Extract info
                    String[] seqInfo = seq.split("/");
                    String enemy = seqInfo[0];
                    int enemyNum = Integer.parseInt(seqInfo[1]);
                    float spawnRate = Float.parseFloat(seqInfo[2]), spawnTime = Float.parseFloat(seqInfo[3]);

                    // Generate and add spawn individual instructions
                    for (int j = enemyNum; j >= 1; j--) {
                        currWave.addInstruction(new SpawnInstruction(enemy, spawnTime*1000));
                        spawnTime += spawnRate;
                    }
                }
                wavenum++;
            }
            scanner.close();

            world = new World(WINDOW_W, WINDOW_H, TILE_SIZE, startx, starty, level);
            world.setWaves(waves);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }

    /**
     * Should be called every 20ms. Executes a 'tick' operations.
     * @throws SlickException
     */
    @Override
    public void update(GameContainer gc, int delta) throws SlickException {
        // something about speed -- increment it given the delta I guess
        world.tick(delta);
        world.moveEnemies();
        world.moveProjectiles();

        Input input = gc.getInput();
        world.processTowers(input);
    }

    /**
     * Responsible for drawing sprites. Called regularly automatically.
     * @throws SlickException
     */
    @Override
    public void render(GameContainer gc, Graphics g) throws SlickException {
        // Draw all the sprites
        world.renderTiles();
        world.renderEnemies();
        world.renderTowers(g);
        world.renderProjectiles();

        world.drawGUI(g);
    }

    /**
     * Closes the game
     */
    @Override
    public boolean closeRequested() {
        System.out.println("GAME STATE: Exiting game");
        System.exit(0);
        return false; // only here to placate the compiler
    }
}