package com.mn.mariogame.Items;

import com.badlogic.gdx.math.Vector2;

/**
 * Created by Admin on 27.3.2016.
 */
public class ItemDef {

    public Vector2 position;
    public Class <?> type;

    public ItemDef(Vector2 position, Class<?> type) {
        this.position = position;
        this.type = type;
    }
}
