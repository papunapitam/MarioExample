package com.mn.mariogame.Screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.FPSLogger;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.mn.mariogame.Items.Item;
import com.mn.mariogame.Items.ItemDef;
import com.mn.mariogame.Items.Mushroom;
import com.mn.mariogame.MarioGame;
import com.mn.mariogame.Sprites.Enemies.Enemy;
import com.mn.mariogame.Sprites.Enemies.Goomba;
import com.mn.mariogame.Sprites.Mario;
import com.mn.mariogame.Tools.B2WorldCreator;
import com.mn.mariogame.Tools.WorldContactListener;

import java.util.concurrent.LinkedBlockingDeque;


/**
 * Created by Admin on 3.3.2016.
 */
public class PlayScreen implements Screen {

    private MarioGame game;
    private OrthographicCamera gameCam;
    private Hud hud;
    private Viewport gamePort;
    private TmxMapLoader mapLoader;
    private TiledMap map;
    private OrthogonalTiledMapRenderer renderer;
    private World world;
    private Box2DDebugRenderer b2dr;
    private Mario player;
    private TextureAtlas atlas;
    private FPSLogger fpsLogger;
    private Music music;
    private Array<Item> items;
    private LinkedBlockingDeque<ItemDef> itemsToSpawn;
    private Goomba goomba;
    private B2WorldCreator creator;

    public PlayScreen(MarioGame game) {
        atlas = new TextureAtlas("Mario_and_Enemies.pack");

        this.game = game;
        gameCam = new OrthographicCamera();
        gamePort = new FitViewport(MarioGame.V_WIDTH / MarioGame.PPM, MarioGame.V_HEIGHT / MarioGame.PPM, gameCam);
        hud = new Hud(game.batch);

        fpsLogger = new FPSLogger();

        mapLoader = new TmxMapLoader();
        map = mapLoader.load("level1.tmx");
        renderer = new OrthogonalTiledMapRenderer(map, 1 / MarioGame.PPM);
        gameCam.position.set(gamePort.getWorldWidth() / 2, gamePort.getWorldHeight() / 2, 0);

        world = new World(new Vector2(0, -10), true);
        b2dr = new Box2DDebugRenderer();

        creator = new B2WorldCreator(this);

        player = new Mario(this);

        world.setContactListener(new WorldContactListener());

        music = MarioGame.manager.get("audio/music/mario_music.ogg", Music.class);
        music.setLooping(true);
        music.play();

        items = new Array<Item>();
        itemsToSpawn = new LinkedBlockingDeque<ItemDef>();
    }

    public void spawnItem(ItemDef idef) {
        itemsToSpawn.add(idef);
    }

    public void handleSpawningItems() {
        if(!itemsToSpawn.isEmpty()) {
            ItemDef idef = itemsToSpawn.poll();
            if(idef.type == Mushroom.class) {
                items.add(new Mushroom(this, idef.position.x, idef.position.y));
            }
        }
    }

    public void update(float dt) {
        handleInput(dt);
        handleSpawningItems();

        world.step(1 / 60f, 6, 2);
        if(!player.isDead()) {
            gameCam.position.x = player.b2body.getPosition().x;
        }
        player.update(dt);
        for(Enemy enemy : creator.getEnemies()) {
            enemy.update(dt);
            if(enemy.getX() < player.getX() + 224 / MarioGame.PPM) {
                enemy.b2body.setActive(true);
            }
        }

        for(Item item : items) {
            item.update(dt);
        }

        hud.update(dt);

        gameCam.update();
        renderer.setView(gameCam);
    }

    public void handleInput(float dt) {
        if(player.currentState != Mario.State.DEAD) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
                if ((player.currentState == Mario.State.JUMPING) || (player.currentState == Mario.State.FALLING)) {
                    return;
                } else {
                    player.b2body.applyLinearImpulse(new Vector2(0, 4f), player.b2body.getWorldCenter(), true);
                }
            }
            if (Gdx.input.isKeyPressed(Input.Keys.RIGHT) && (player.b2body.getLinearVelocity().x <= 2)) {
                player.b2body.applyLinearImpulse(new Vector2(0.1f, 0), player.b2body.getWorldCenter(), true);
            }
            if (Gdx.input.isKeyPressed(Input.Keys.LEFT) && (player.b2body.getLinearVelocity().x >= -2)) {
                player.b2body.applyLinearImpulse(new Vector2(-0.1f, 0), player.b2body.getWorldCenter(), true);
            }
            if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
                player.fire();
            }
        }
    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        update(delta);

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        //render game map
        renderer.render();

        //render debug lines
      //  b2dr.render(world, gameCam.combined);

        game.batch.setProjectionMatrix(gameCam.combined);
        game.batch.begin();
        player.draw(game.batch);
        for(Enemy enemy : creator.getEnemies()) {
            enemy.draw(game.batch);
        }
        for(Item item : items) {
            item.draw(game.batch);
        }
        game.batch.end();

        //draw next layer of HUD
        game.batch.setProjectionMatrix(hud.stage.getCamera().combined);
        hud.stage.draw();

        if(gameOver()) {
            game.setScreen(new GameOverScreen(game));
            dispose();
        }

        fpsLogger.log();
    }

    @Override
    public void resize(int width, int height) {
        gamePort.update(width, height);
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {
        map.dispose();
        renderer.dispose();
        world.dispose();
        b2dr.dispose();
        hud.dispose();
    }

    public TextureAtlas getAtlas(){
        return atlas;
    }

    public TiledMap getMap() {
        return map;
    }

    public World getWorld() {
        return world;
    }

    public Mario getMario() {
        return player;
    }

    public B2WorldCreator getCreator() {
        return creator;
    }

    public boolean gameOver() {
        if(player.currentState == Mario.State.DEAD && player.getStateTimer() > 3) {
            return true;
        }
        else {
            return false;
        }
    }
}
