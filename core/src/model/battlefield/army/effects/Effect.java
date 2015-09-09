package model.battlefield.army.effects;

import java.util.ArrayList;

import model.builders.EffectBuilder;

/**
 * Abstract class that defines the basic structure of effets.
 * 
 * The role of effects is to create interaction between battlefield components.
 * 
 * source and target are, for example, units, buildings, players... and implements the EffectSource
 * and EffectTarget interfaces.
 * 
 * On launch, effects may have different behaviors, and may also launch children effects 
 * 
 */
public abstract class Effect {
    protected final ArrayList<String> childEffectBuildersID;
    
    public final EffectSource source;
    public final EffectTarget target;

    public Effect(ArrayList<String> childEffectBuildersID, EffectSource source, EffectTarget target) {
        this.childEffectBuildersID = childEffectBuildersID;
        this.source = source;
        this.target = target;
    }
    
    public abstract void launch();
}
