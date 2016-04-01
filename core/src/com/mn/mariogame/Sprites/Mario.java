package com.mn.mariogame.Sprites;

import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.EdgeShape;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.mn.mariogame.MarioGame;
import com.mn.mariogame.Screens.PlayScreen;
import com.mn.mariogame.Sprites.Enemies.Enemy;
import com.mn.mariogame.Sprites.Enemies.Turtle;

/**
 * Created by Admin on 3.3.2016.
 */
public class Mario extends Sprite {

    public enum State {
        FALLING,
        JUMPING,
        STANDING,
        GROWING,
        RUNNING,
        DEAD
    };

    public State currentState;
    public State previousState;

    public World world;
    public Body b2body;

    private TextureRegion marioStand;
    private TextureRegion marioJump;
    private TextureRegion bigMarioStand;
    private TextureRegion bigMarioJump;
    private TextureRegion marioDead;
    private Animation marioRun;
    private Animation bigMarioRun;
    private Animation growMario;

    private float stateTimer;

    private boolean runningRight;
    private boolean marioIsBig;
    private boolean runGrowAnimation;

    private boolean timeToDefineBigMario;
    private boolean timeToRedefineMario;

    private boolean marioIsDead;
    private PlayScreen screen;

    private Array<Fireball> fireballs;

    public Mario(PlayScreen screen) {
        this.screen = screen;
        this.world = screen.getWorld();
        currentState = State.STANDING;
        previousState = State.STANDING;
        stateTimer = 0;
        runningRight = true;
        marioIsBig = false;
        Array<TextureRegion> frames = new Array<TextureRegion>();

        //running
        for(int i = 1; i < 4; i++ ) {
            frames.add(new TextureRegion(screen.getAtlas().findRegion("little_mario"), i * 16, 0, 16, 16));
        }
        marioRun = new Animation(0.1f, frames);
        frames.clear();

        for(int i = 1; i < 4; i++) {
            frames.add(new TextureRegion(screen.getAtlas().findRegion("big_mario"), i * 16, 0, 16, 32));
        }
        bigMarioRun = new Animation(0.1f, frames);
        frames.clear();

        //growing
        frames.add(new TextureRegion(screen.getAtlas().findRegion("big_mario"), 240, 0, 16, 32));
        frames.add(new TextureRegion(screen.getAtlas().findRegion("big_mario"), 0, 0, 16, 32));
        frames.add(new TextureRegion(screen.getAtlas().findRegion("big_mario"), 240, 0, 16, 32));
        frames.add(new TextureRegion(screen.getAtlas().findRegion("big_mario"), 0, 0, 16, 32));
        growMario = new Animation(0.1f, frames);

        //jumping
        marioJump = new TextureRegion(screen.getAtlas().findRegion("little_mario"), 80, 0, 16, 16);
        bigMarioJump = new TextureRegion(screen.getAtlas().findRegion("big_mario"), 80, 0, 16, 32);

        //dead
        marioDead = new TextureRegion(screen.getAtlas().findRegion("little_mario"), 96, 0, 16, 16);

        //standing
        marioStand = new TextureRegion(screen.getAtlas().findRegion("little_mario"), 0, 0, 16, 16);
        bigMarioStand = new TextureRegion(screen.getAtlas().findRegion("big_mario"), 0, 0, 16, 32);
        defineMario();
        setBounds(0, 0, 16 / MarioGame.PPM, 16 / MarioGame.PPM);
        setRegion(marioStand);

        fireballs = new Array<Fireball>();
    }

    public void update(float dt) {
        if(marioIsBig) {
            setPosition(b2body.getPosition().x - getWidth() / 2, b2body.getPosition().y - getHeight() / 2  - 6 / MarioGame.PPM  );
        }
        else {
            setPosition(b2body.getPosition().x - getWidth() / 2, b2body.getPosition().y - getHeight() / 2 );
        }

        setRegion(getFrame(dt));
        if(timeToDefineBigMario) {
            defineBigMario();
        }
        if(timeToRedefineMario) {
            redefineMario();
        }

        for(Fireball f : fireballs) {
            f.update(dt);
            if(f.isDestroyed()) {
                fireballs.removeValue(f, true);
            }
        }
    }

    public TextureRegion getFrame(float dt) {
        currentState = getState();
        TextureRegion region;
        switch(currentState) {
            case DEAD:
                region = marioDead;
                break;
            case GROWING:
                region = growMario.getKeyFrame(stateTimer);
                if(growMario.isAnimationFinished(stateTimer)) {
                    runGrowAnimation = false;
                }
                break;
            case JUMPING:
                region = marioIsBig ? bigMarioJump : marioJump;
                break;
            case RUNNING:
                region = marioIsBig ? bigMarioRun.getKeyFrame(stateTimer, true) : marioRun.getKeyFrame(stateTimer, true);
                break;
            case FALLING:
            case STANDING:
            default:
                region = marioIsBig ? bigMarioStand : marioStand;
                break;
        }
        if((b2body.getLinearVelocity().x < 0 || !runningRight) && !region.isFlipX()) {
            region.flip(true, false);
            runningRight = false;
        } else if((b2body.getLinearVelocity().x > 0 || runningRight) && region.isFlipX()) {
            region.flip(true, false);
            runningRight = true;
        }

        stateTimer = currentState == previousState ? stateTimer + dt : 0;
        previousState = currentState;
        return region;
    }

    public State getState() {
        if(marioIsDead) {
            return State.DEAD;
        } else if(runGrowAnimation) {
            return State.GROWING;
        } else if(b2body.getLinearVelocity().y > 0 || (b2body.getLinearVelocity().y < 0 && previousState == State.JUMPING)) {
            return State.JUMPING;
        } else if(b2body.getLinearVelocity().y < 0) {
            return State.FALLING;
        } else if(b2body.getLinearVelocity().x != 0) {
            return State.RUNNING;
        }
        else return State.STANDING;
    }

    public void grow() {
        runGrowAnimation = true;
        marioIsBig = true;
        timeToDefineBigMario = true;
        setBounds(getX(), getY(), getWidth(), getHeight() * 2);
        MarioGame.manager.get("audio/sounds/powerup.wav", Sound.class).play();
    }

    public boolean isBig() {
        return marioIsBig;
    }

    public boolean isDead() {
        return marioIsDead;
    }

    public float getStateTimer() {
        return stateTimer;
    }

    public void hit(Enemy enemy) {
        if(enemy instanceof Turtle && ((Turtle) enemy).getCurrentState() == Turtle.State.STANDING_SHELL) {
            ((Turtle) enemy).kick(this.getX() <= enemy.getX() ? Turtle.KICK_RIGHT_SPEED : Turtle.KICK_LEFT_SPEED);
        }
        else {
            if (marioIsBig) {
                marioIsBig = false;
                timeToRedefineMario = true;
                setBounds(getX(), getY(), getWidth(), getHeight() / 2);
                MarioGame.manager.get("audio/sounds/powerdown.wav", Sound.class).play();
            } else {
                MarioGame.manager.get("audio/music/mario_music.ogg", Music.class).stop();
                MarioGame.manager.get("audio/sounds/mariodie.wav", Sound.class).play();
                marioIsDead = true;
                Filter filter = new Filter();
                filter.maskBits = MarioGame.NOTHING_BIT;
                for (Fixture fixture : b2body.getFixtureList()) {
                    fixture.setFilterData(filter);
                }
                b2body.setLinearVelocity(new Vector2(0, 0));
                b2body.applyLinearImpulse(new Vector2(0, 4f), b2body.getWorldCenter(), true);
            }
        }
    }

    public void redefineMario() {
        Vector2 position = b2body.getPosition();
        world.destroyBody(b2body);

        BodyDef bdef = new BodyDef();
        bdef.position.set(position);
        bdef.type = BodyDef.BodyType.DynamicBody;
        b2body = world.createBody(bdef);

        FixtureDef fdef = new FixtureDef();
        CircleShape shape = new CircleShape();
        shape.setRadius(6 / MarioGame.PPM);
        fdef.filter.categoryBits = MarioGame.MARIO_BIT;
        fdef.filter.maskBits = MarioGame.GROUND_BIT |
                MarioGame.COIN_BIT |
                MarioGame.BRICK_BIT |
                MarioGame.OBJECT_BIT |
                MarioGame.ENEMY_BIT |
                MarioGame.ENEMY_HEAD_BIT |
                MarioGame.ITEM_BIT;

        fdef.shape = shape;
        b2body.createFixture(fdef).setUserData(this);

        EdgeShape head = new EdgeShape();
        head.set(new Vector2(-2 / MarioGame.PPM, 6 / MarioGame.PPM), (new Vector2(2 / MarioGame.PPM, 6 / MarioGame.PPM)));
        fdef.filter.categoryBits = MarioGame.MARIO_HEAD_BIT;
        fdef.shape = head;
        fdef.isSensor = true;
        b2body.createFixture(fdef).setUserData(this);

        timeToRedefineMario = false;
    }

    public void defineBigMario() {
        Vector2 currentPosition = b2body.getPosition();
        world.destroyBody(b2body);

        BodyDef bdef = new BodyDef();
        bdef.position.set(currentPosition.add(0, 10 / MarioGame.PPM));
        bdef.type = BodyDef.BodyType.DynamicBody;
        b2body = world.createBody(bdef);

        FixtureDef fdef = new FixtureDef();
        CircleShape shape = new CircleShape();
        shape.setRadius(6 / MarioGame.PPM);
        fdef.filter.categoryBits = MarioGame.MARIO_BIT;
        fdef.filter.maskBits = MarioGame.GROUND_BIT |
                MarioGame.COIN_BIT |
                MarioGame.BRICK_BIT |
                MarioGame.OBJECT_BIT |
                MarioGame.ENEMY_BIT |
                MarioGame.ENEMY_HEAD_BIT |
                MarioGame.ITEM_BIT;

        fdef.shape = shape;
        b2body.createFixture(fdef).setUserData(this);
        shape.setPosition(new Vector2(0, -14 / MarioGame.PPM));
        b2body.createFixture(fdef).setUserData(this);


        EdgeShape head = new EdgeShape();
        head.set(new Vector2(-2 / MarioGame.PPM, 6 / MarioGame.PPM), (new Vector2(2 / MarioGame.PPM, 6 / MarioGame.PPM)));
        fdef.filter.categoryBits = MarioGame.MARIO_HEAD_BIT;
        fdef.shape = head;
        fdef.isSensor = true;
        b2body.createFixture(fdef).setUserData(this);

        timeToDefineBigMario = false;
    }

    public void defineMario() {
        BodyDef bdef = new BodyDef();
        bdef.position.set(32 / MarioGame.PPM, 32 / MarioGame.PPM);
        bdef.type = BodyDef.BodyType.DynamicBody;
        b2body = world.createBody(bdef);

        FixtureDef fdef = new FixtureDef();
        CircleShape shape = new CircleShape();
        shape.setRadius(6 / MarioGame.PPM);
        fdef.filter.categoryBits = MarioGame.MARIO_BIT;
        fdef.filter.maskBits = MarioGame.GROUND_BIT |
                MarioGame.COIN_BIT |
                MarioGame.BRICK_BIT |
                MarioGame.OBJECT_BIT |
                MarioGame.ENEMY_BIT |
                MarioGame.ENEMY_HEAD_BIT |
                MarioGame.ITEM_BIT;

        fdef.shape = shape;
        b2body.createFixture(fdef).setUserData(this);

        EdgeShape head = new EdgeShape();
        head.set(new Vector2(-2 / MarioGame.PPM, 6 / MarioGame.PPM), (new Vector2(2 / MarioGame.PPM, 6 / MarioGame.PPM)));
        fdef.filter.categoryBits = MarioGame.MARIO_HEAD_BIT;
        fdef.shape = head;
        fdef.isSensor = true;
        b2body.createFixture(fdef).setUserData(this);
    }

    public void fire() {
        fireballs.add(new Fireball(screen, b2body.getPosition().x, b2body.getPosition().y));
    }

    public void draw(Batch batch) {
        super.draw(batch);
        for(Fireball f : fireballs) {
            f.draw(batch);
        }
    }
}
