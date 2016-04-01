package com.mn.mariogame.Tools;

import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.mn.mariogame.MarioGame;
import com.mn.mariogame.Screens.PlayScreen;
import com.mn.mariogame.Sprites.Brick;
import com.mn.mariogame.Sprites.Coin;
import com.mn.mariogame.Sprites.Enemies.Enemy;
import com.mn.mariogame.Sprites.Enemies.Goomba;
import com.mn.mariogame.Sprites.Enemies.Turtle;

/**
 * Created by Admin on 3.3.2016.
 */
public class B2WorldCreator {

    private Array<Goomba> goombas;
    private Array<Turtle> turtles;

    public B2WorldCreator(PlayScreen screen) {

        World world = screen.getWorld();
        TiledMap map = screen.getMap();

        BodyDef bdef = new BodyDef();
        PolygonShape shape = new PolygonShape();
        FixtureDef fdef = new FixtureDef();
        Body body;

        //create ground bodies/fixtures
        for(MapObject object : map.getLayers().get(2).getObjects().getByType(RectangleMapObject.class)) {
            Rectangle rect = ((RectangleMapObject) object).getRectangle();

            bdef.type = BodyDef.BodyType.StaticBody;
            bdef.position.set((rect.getX() + rect.getWidth() / 2) / MarioGame.PPM, (rect.getY() + rect.getHeight() / 2) / MarioGame.PPM);

            body = world.createBody(bdef);

            shape.setAsBox(rect.getWidth() / 2 / MarioGame.PPM, rect.getHeight() / 2 / MarioGame.PPM);
            fdef.shape = shape;
            body.createFixture(fdef);
        }

        //create pipe bodies/fixtures
        for(MapObject object : map.getLayers().get(3).getObjects().getByType(RectangleMapObject.class)) {
            Rectangle rect = ((RectangleMapObject) object).getRectangle();

            bdef.type = BodyDef.BodyType.StaticBody;
            bdef.position.set((rect.getX() + rect.getWidth() / 2) / MarioGame.PPM, (rect.getY() + rect.getHeight() / 2) / MarioGame.PPM);

            body = world.createBody(bdef);

            shape.setAsBox(rect.getWidth() / 2 / MarioGame.PPM, rect.getHeight() / 2 / MarioGame.PPM);
            fdef.shape = shape;
            fdef.filter.categoryBits = MarioGame.OBJECT_BIT;
            body.createFixture(fdef);
        }

        //create bricks bodies/fixtures
        for(MapObject object : map.getLayers().get(5).getObjects().getByType(RectangleMapObject.class)) {

            new Brick(screen, object);
        }

        //create coins bodies/fixtures
        for(MapObject object : map.getLayers().get(4).getObjects().getByType(RectangleMapObject.class)) {

            new Coin(screen, object);
        }

        //create all goombas
        goombas = new Array<Goomba>();
        for(MapObject object : map.getLayers().get(6).getObjects().getByType(RectangleMapObject.class)) {
            Rectangle rect = ((RectangleMapObject) object).getRectangle();
            goombas.add(new Goomba(screen, rect.getX() / MarioGame.PPM, rect.getY() / MarioGame.PPM));
        }

        //create all turtles
        turtles = new Array<Turtle>();
        for(MapObject object : map.getLayers().get(7).getObjects().getByType(RectangleMapObject.class)) {
            Rectangle rect = ((RectangleMapObject) object).getRectangle();
            turtles.add(new Turtle(screen, rect.getX() / MarioGame.PPM, rect.getY() / MarioGame.PPM));
        }
    }

    public Array<Enemy> getEnemies() {
        Array<Enemy> enemies = new Array<Enemy>();
        enemies.addAll(goombas);
        enemies.addAll(turtles);
        return enemies;
    }

    public void removeTurtle(Turtle turtle) {
        turtles.removeValue(turtle, true);
    }

    public Array<Goomba> getGoombas() {
        return goombas;
    }
    public Array<Turtle> getTurtles() {
        return turtles;
    }

}
