package model;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import model.battlefield.map.Map;
import model.battlefield.Battlefield;
import ressources.definitions.BuilderLibrary;
import model.editor.ToolManager;
import model.battlefield.BattlefieldFactory;
import ressources.definitions.DefParser;
import tools.LogUtil;

public class Model {
    public static final String BATTLEFIELD_UPDATED_EVENT = "mapupdatedevent";
    static final String CONFIG_PATH = "assets/data";
    public static final String DEFAULT_MAP_PATH = "assets/maps/";
    static final double UPDATE_DELAY = 1000;
    
    public BattlefieldFactory factory;
    public Battlefield battlefield;
    
    public Commander commander;
    public Reporter reporter;
    public ToolManager toolManager;
    

    public BuilderLibrary lib;
    DefParser parser;
    File confFile;
    double nextUpdate = 0;
    
    private ArrayList<ActionListener> listeners = new ArrayList<>();
    
    public Model() {
        lib = new BuilderLibrary();
        parser = new DefParser(lib, CONFIG_PATH);
        
        factory = new BattlefieldFactory(lib);
        setNewBattlefield();

//        armyManager.createTestArmy(lib);
    }
    
    public void updateConfigs() {
        if(System.currentTimeMillis()>nextUpdate){
            nextUpdate = System.currentTimeMillis()+UPDATE_DELAY;
            parser.readFile();
        }
    }
    
    public void loadBattlefield(){
        Battlefield loadedBattlefield = factory.load();
        if(loadedBattlefield != null)
            setBattlefield(loadedBattlefield);
    }
    
    public void saveBattlefield(){
        factory.save(battlefield);
    }
    
    public void setNewBattlefield(){
        setBattlefield(factory.getNew(128, 128));
    }
    
    private void setBattlefield(Battlefield battlefield){
        this.battlefield = battlefield;
        lib.battlefield = battlefield;
        commander = new Commander(battlefield.armyManager, battlefield.map);
        toolManager = new ToolManager(battlefield, lib);
        LogUtil.logger.info("Reseting view...");
        notifyListeners(BATTLEFIELD_UPDATED_EVENT);
        LogUtil.logger.info("Done.");
        battlefield.engagement.resetEngagement();
    }
    
    public void addListener(ActionListener listener){
        listeners.add(listener);
    }
    
    public void notifyListeners(String eventCommand){
        for(ActionListener l : listeners)
            l.actionPerformed(new ActionEvent(this, 0, eventCommand));
        
    }
}