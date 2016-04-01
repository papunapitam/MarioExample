package com.mn.mariogame.Sprites.Enemies;


import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.utils.Array;
import com.mn.mariogame.MarioGame;
import com.mn.mariogame.Screens.PlayScreen;
import com.mn.mariogame.Sprites.Mario;


/**
 * Created by Admin on 31.3.2016.
 */
public class Turtle extends Enemy {

    private float stateTime;
    private Animation walkAnimation;
    private Array<TextureRegion> frames;
    private boolean setToDestroy;
    private boolean destroyed;

    private TextureRegion shell;

    public enum State {
        WALKING,
        STANDING_SHELL,
        MOVING_SHELL,
        DEAD
    };

    public State currentState;
    public State previousState;
    public static final int KICK_LEFT_SPEED = -2;
    public static final int KICK_RIGHT_SPEED = 2;
    private float deadRotationDegrees;

    public Turtle(PlayScreen screen, float x, float y) {
        super(screen, x, y);
        frames = new Array<TextureRegion>();
        frames.add(new TextureRegion(screen.getAtlas().findRegion("turtle"), 0, 0, 16, 24));
        frames.add(new TextureRegion(screen.getAtlas().findRegion("turtle"), 16, 0, 16, 24));
        shell = new TextureRegion(screen.getAtlas().findRegion("turtle"), 64, 0, 16, 24);
        walkAnimation = new Animation(0.2f, frames);
        currentState = previousState = State.WALKING;
        deadRotationDegrees = 0;

        setBounds(getX(), getY(), 16 / MarioGame.PPM, 24 / MarioGame.PPM);
    }

    @Override
    protected void defineEnemy() {
        BodyDef bdef = new BodyDef();
        bdef.position.set(getX(), getY());
        bdef.type = BodyDef.BodyType.DynamicBody;
        b2body = world.createBody(bdef);

        FixtureDef fdef = new FixtureDef();
        CircleShape shape = new CircleShape();
        shape.setRadius(6 / MarioGame.PPM);
        fdef.filter.categoryBits = MarioGame.ENEMY_BIT;
        fdef.filter.maskBits = MarioGame.GROUND_BIT |
                MarioGame.COIN_BIT |
                MarioGame.BRICK_BIT |
                MarioGame.ENEMY_BIT |
                MarioGame.OBJECT_BIT |
                MarioGame.MARIO_BIT |
                MarioGame.FIREBALL_BIT;

        fdef.shape = shape;
        b2body.createFixture(fdef).setUserData(this);

        PolygonShape head = new PolygonShape();
        Vector2[] vertice = new Vector2[4];
        vertice[0] = new Vector2(-5, 8).scl(1 / MarioGame.PPM);
        vertice[1] = new Vector2(5, 8).scl(1 / MarioGame.PPM);
        vertice[2] = new Vector2(-3, 3).scl(1 / MarioGame.PPM);
        vertice[3] = new Vector2(3, 3).scl(1 / MarioGame.PPM);
        head.set(vertice);

        fdef.shape = head;
        fdef.restitution = 0.5f;
        fdef.filter.categoryBits = MarioGame.ENEMY_HEAD_BIT;
        b2body.createFixture(fdef).setUserData(this);
    }

    @Override
    public void hitOnHead(Mario mario) {
        if(currentState != State.STANDING_SHELL) {
            currentState = State.STANDING_SHELL;
            velocity.x = 0;
        }
        else {
            kick(mario.getX() <= this.getX() ? KICK_RIGHT_SPEED : KICK_LEFT_SPEED);
        }
    }

    public void onBallHit() {
        if(currentState != State.STANDING_SHELL) {
            currentState = State.STANDING_SHELL;
            velocity.x = 0;
        }
        else {
            return;
        }
    }

    @Override
    public void onEnemyHit(Enemy enemy) {
        if(enemy instanceof Turtle) {
            if(((Turtle) enemy).currentState == State.MOVING_SHELL && currentState != State.MOVING_SHELL) {
                killed();
            } else if(currentState == State.MOVING_SHELL && ((Turtle) enemy).currentState == State.WALKING) {
                return;
            }
            else {
                reverseVelocity(true, false);
            }
        } else if(currentState != State.MOVING_SHELL) {
            reverseVelocity(true, false);
        }
    }

    public void kick(int speed) {
        velocity.x = speed;
        currentState = State.MOVING_SHELL;
    }

    public State getCurrentState() {
        return currentState;
    }

    public TextureRegion getFrame(float dt) {
        TextureRegion region;

        switch (currentState) {
            case STANDING_SHELL:
            case MOVING_SHELL:
                region = shell;
                break;
            case WALKING:
            default:
                region = walkAnimation.getKeyFrame(stateTime, true);
                break;
        }

        if(velocity.x > 0 && region.isFlipX() == false) {
            region.flip(true, false);
        }

        if(velocity.x < 0 && region.isFlipX() == true) {
            region.flip(true, false);
        }
        stateTime = currentState == previousState ? stateTime + dt : 0;
        previousState = currentState;
        return region;
    }

    @Override
    public void update(float dt) {
        setRegion(getFrame(dt));
        if(currentState == State.STANDING_SHELL && stateTime > 5) {
            currentState = State.WALKING;
            velocity.x = 1;
        }

        setPosition(b2body.getPosition().x - getWidth() / 2, b2body.getPosition().y - 8 / MarioGame.PPM);

        if(currentState == State.DEAD) {
            deadRotationDegrees += 3;
            rotate(deadRotationDegrees);
            if(stateTime > 5 && !destroyed) {
                world.destroyBody(b2body);
                destroyed = true;
            }
        }
        else {
            b2body.setLinearVelocity(velocity);
        }
    }

    public void killed() {
        currentState = State.DEAD;
        Filter filter = new Filter();
        filter.maskBits = MarioGame.NOTHING_BIT;

        for(Fixture fixture : b2body.getFixtureList()) {
            fixture.setUserData(filter);
        }
        b2body.applyLinearImpulse(new Vector2(0, 5f), b2body.getWorldCenter(), true);
        screen.getCreator().removeTurtle(this);
    }
}
