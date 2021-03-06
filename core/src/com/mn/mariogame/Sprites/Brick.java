package com.mn.mariogame.Sprites;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.physics.box2d.World;
import com.mn.mariogame.MarioGame;
import com.mn.mariogame.Screens.Hud;
import com.mn.mariogame.Screens.PlayScreen;

/**
 * Created by Admin on 3.3.2016.
 */
public class Brick extends InteractiveTileObject {

    public Brick(PlayScreen screen, MapObject object) {
        super(screen, object);
        fixture.setUserData(this);
        setCategoryFilter(MarioGame.BRICK_BIT);
    }

    @Override
    public void onHeadHit(Mario mario) {
        if(mario.isBig()) {
            setCategoryFilter(MarioGame.DESTROYED_BIT);
            getCell().setTile(null);
            Hud.addScore(100);
            MarioGame.manager.get("audio/sounds/breakblock.wav", Sound.class).play();
        }
        else {
            MarioGame.manager.get("audio/sounds/bump.wav", Sound.class).play();
        }
    }
}
