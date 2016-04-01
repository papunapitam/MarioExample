package com.mn.mariogame.Sprites;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.mn.mariogame.MarioGame;
import com.mn.mariogame.Screens.GameOverScreen;
import com.mn.mariogame.Screens.PlayScreen;
import com.mn.mariogame.Sprites.Enemies.Enemy;
import com.mn.mariogame.Sprites.Enemies.Goomba;
import com.mn.mariogame.Sprites.Enemies.Turtle;

/**
 * Created by Admin on 1.4.2016.
 */
public class Fireball extends Sprite {

    private Animation rotateAnimation;
    private float stateTime;
    private boolean setToDestroy;
    private boolean destroyBody;
    private Array<TextureRegion> frames;
    private boolean destroyed;
    private Body b2body;
    private World world;
    private PlayScreen screen;
    private boolean fireRight;

    public Fireball(PlayScreen screen, float x, float y, boolean fireRight) {
        this.screen = screen;
        this.fireRight = fireRight;
        frames = new Array<TextureRegion>();
        for(int i = 0; i < 4; i++) {
            frames.add(new TextureRegion(screen.getAtlas().findRegion("fireball"), i * 8, 0, 8, 8));
        }
        rotateAnimation = new Animation(0.2f, frames);
        stateTime = 0;
        setPosition(x, y);
        setBounds(getX(), getY(), 6 / MarioGame.PPM, 6 / MarioGame.PPM);
        setToDestroy = false;
        destroyed = false;
        setRegion(rotateAnimation.getKeyFrame(0));
        defineFireball();
    }


    public void update(float dt) {
        stateTime += dt;
        setRegion(rotateAnimation.getKeyFrame(stateTime, true));
        if(setToDestroy && !destroyed) {
            world.destroyBody(b2body);
            destroyed = true;
            stateTime = 0;

        } else if (!destroyed) {
            b2body.setLinearVelocity(new Vector2(fireRight ? 2f : -2f, 0));
            setPosition(b2body.getPosition().x - getWidth() / 2, b2body.getPosition().y - getHeight() / 2);
            setRegion(rotateAnimation.getKeyFrame(stateTime, true));
        }
    }

    public void defineFireball() {
        BodyDef bdef = new BodyDef();
        world = screen.getWorld();
        bdef.position.set(getX(), getY());
        bdef.type = BodyDef.BodyType.DynamicBody;
        b2body = world.createBody(bdef);

        FixtureDef fdef = new FixtureDef();
        CircleShape shape = new CircleShape();
        shape.setRadius(3 / MarioGame.PPM);
        fdef.filter.categoryBits = MarioGame.FIREBALL_BIT;
        fdef.filter.maskBits = MarioGame.GROUND_BIT |
                MarioGame.COIN_BIT |
                MarioGame.BRICK_BIT |
                MarioGame.ENEMY_BIT |
                MarioGame.OBJECT_BIT;

        fdef.shape = shape;
        b2body.createFixture(fdef).setUserData(this);
        b2body.setLinearVelocity(new Vector2(2, 2.5f));
    }

    public void draw(Batch batch) {
        if(!destroyed) {
            super.draw(batch);
        }
    }

    public void hitEnemy(Enemy enemy) {
        setToDestroy = true;
        if(enemy instanceof Turtle) {
            ((Turtle)enemy).onBallHit();
        } else if(enemy instanceof Goomba) {
            ((Goomba)enemy).destroy();
        }
    }

    public void destroy() {
        setToDestroy = true;
    }

    public boolean isDestroyed() {
        return destroyed;
    }
}
