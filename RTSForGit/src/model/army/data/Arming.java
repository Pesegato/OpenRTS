/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package model.army.data;

import java.util.ArrayList;

/**
 *
 * @author Benoît
 */
public class Arming {
    Unit holder;
    ArrayList<Weapon> weapons = new ArrayList<>();
    ArrayList<Turret> turrets = new ArrayList<>();
    
    boolean aiming;
    boolean onScan;
    boolean atRange;
    
    
    public Arming(Unit holder){
        this.holder = holder;
    }
    
    protected void updateWeapons(){
        for(Weapon w : weapons){
            w.update(holder.faction.enemies.get(0).units);
            if(w.acquiring())
                atRange = true;
            if(w.scanning())
                onScan = true;
        }
    }
    
    protected void updateTurrets(double elapsedTime){
        for(Turret t : turrets)
            t.update(elapsedTime, holder.isMoving());
    }
    
    public boolean isAiming(){
        return aiming;
    }
    
    protected boolean hasTurret(){
        return !turrets.isEmpty();
    }
    
    public boolean scanning(){
        return onScan;
    }
    
    public boolean acquiring(){
        return atRange;
    }
    
    public boolean acquiring(Unit unit){
        for(Weapon w : weapons)
            if(w.hasTargetAtRange(unit))
                return true;
        return false;
    }
    
    public void attack(){
        if(!atRange)
            throw new RuntimeException("Asking to attack but no target at range.");
        for(Weapon w : weapons)
            if(w.acquiring())
                w.attack();
    }
    
    public void attack(Unit unit){
        boolean found = false;
        for(Weapon w : weapons)
            if(w.hasTargetAtRange(unit)){
                w.attack(unit);
                found = true;
            }
        if(!found)
            throw new IllegalArgumentException("Specific unit to attack is not at range.");
    }
    
    public Unit getNearestScanned(){
        Unit res = null;
        for(Weapon w : weapons)
            if(w.getTarget() != null)
                res = res == null? w.getTarget():holder.getNearest(res, w.getTarget());
        if(res == null)
            throw new RuntimeException("Asking the nearest scanned but nothing on scan.");
        return res;
    }
    
    
}