package com.mn.mariogame.Tools;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.mn.mariogame.Items.Item;
import com.mn.mariogame.MarioGame;
import com.mn.mariogame.Sprites.Enemy;
import com.mn.mariogame.Sprites.Goomba;
import com.mn.mariogame.Sprites.InteractiveTileObject;
import com.mn.mariogame.Sprites.Mario;

/**
 * Created by Admin on 10.3.2016.
 */
public class WorldContactListener implements ContactListener{
    @Override
    public void beginContact(Contact contact) {
        Fixture fixA = contact.getFixtureA();
        Fixture fixB = contact.getFixtureB();

        int cdef = fixA.getFilterData().categoryBits | fixB.getFilterData().categoryBits;

        if (fixA.getUserData() == "head" || fixB.getUserData() == "head") {
            Fixture head = fixA.getUserData().equals("head") ? fixA : fixB;
            Fixture object = head == fixA ? fixB : fixA;

            if(object.getUserData() instanceof InteractiveTileObject) {
                ((InteractiveTileObject) object.getUserData()).onHeadHit();
            }
        }

        switch(cdef) {
            case MarioGame.ENEMY_HEAD_BIT | MarioGame.MARIO_BIT:
                if(fixA.getFilterData().categoryBits == MarioGame.ENEMY_HEAD_BIT) {
                    ((Enemy)fixA.getUserData()).hitOnHead();
                } else {
                    ((Enemy) fixB.getUserData()).hitOnHead();
                } break;
            case MarioGame.ENEMY_BIT | MarioGame.OBJECT_BIT:
                if(fixA.getFilterData().categoryBits == MarioGame.ENEMY_BIT) {
                    ((Enemy)fixA.getUserData()).reverseVelocity(true, false);
                } else {
                    ((Enemy)fixB.getUserData()).reverseVelocity(true, false);
                } break;
            case MarioGame.MARIO_BIT | MarioGame.ENEMY_BIT:
                Gdx.app.log("MARIO", "DIED");
                break;
            case MarioGame.ENEMY_BIT | MarioGame.ENEMY_BIT:
                ((Enemy)fixA.getUserData()).reverseVelocity(true, false);
                ((Enemy)fixB.getUserData()).reverseVelocity(true, false);
                break;
            case MarioGame.ITEM_BIT | MarioGame.OBJECT_BIT:
                if(fixA.getFilterData().categoryBits == MarioGame.ITEM_BIT) {
                    ((Item)fixA.getUserData()).reverseVelocity(true, false);
                } else {
                    ((Item)fixB.getUserData()).reverseVelocity(true, false);
                } break;
            case MarioGame.ITEM_BIT | MarioGame.MARIO_BIT:
                if(fixA.getFilterData().categoryBits == MarioGame.ITEM_BIT) {
                    ((Item)fixA.getUserData()).use((Mario) fixB.getUserData());
                } else {
                    ((Item)fixB.getUserData()).use((Mario) fixA.getUserData());
                } break;
        }

    }


    @Override
    public void endContact(Contact contact) {
    }

    @Override
    public void preSolve(Contact contact, Manifold oldManifold) {

    }

    @Override
    public void postSolve(Contact contact, ContactImpulse impulse) {

    }
}
