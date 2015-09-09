/*
 * To change this template, choose Tools | Templates and open the template in the editor.
 */
package model.builders;

import geometry.math.MyRandom;

import java.util.ArrayList;
import java.util.List;

import model.battlefield.map.cliff.Cliff;
import model.builders.definitions.BuilderManager;
import model.builders.definitions.DefElement;
import model.builders.definitions.Definition;

/**
 * @author Benoît
 */
public class CliffShapeBuilder extends Builder {
	private static final String NATURAL_FACE_LINK = "NaturalFaceLink";
	private static final String MANMADE_FACE_LINK = "ManmadeFaceLink";
	private static final String TRINKET_LIST = "TrinketList";
	private static final String RAMP_TRINKET_LIST = "RampTrinketList";
	private static final String EDITOR_ICON_PATH = "EditorIconPath";
	private static final String LINK = "link";
	private static final String PROB = "prob";

	private NaturalFaceBuilder naturalFaceBuilder = null;
	private String naturalFaceBuilderID = "";
	private ManmadeFaceBuilder manmadeFaceBuilder = null;
	private String manmadeFaceBuilderID = "";
	private List<TrinketBuilder> trinketBuilders = new ArrayList<>();
	private List<String> trinketBuildersID = new ArrayList<>();
	private List<Double> trinketProbs = new ArrayList<>();
	private List<TrinketBuilder> rampTrinketBuilders = new ArrayList<>();
	private List<String> rampTrinketBuildersID = new ArrayList<>();
	private List<Double> rampTrinketProbs = new ArrayList<>();
	private String editorIconPath = "textures/editor/defaultcliffshapeicon.png";

	public CliffShapeBuilder(Definition def) {
		super(def);
		for (DefElement de : def.getElements()) {
			switch (de.name) {
				case NATURAL_FACE_LINK:
					naturalFaceBuilderID = de.getVal();
					break;
				case MANMADE_FACE_LINK:
					manmadeFaceBuilderID = de.getVal();
					break;
				case TRINKET_LIST:
					trinketBuildersID.add(de.getVal(LINK));
					trinketProbs.add(de.getDoubleVal(PROB));
					break;
				case RAMP_TRINKET_LIST:
					rampTrinketBuildersID.add(de.getVal(LINK));
					rampTrinketProbs.add(de.getDoubleVal(PROB));
					break;
				case EDITOR_ICON_PATH:
					editorIconPath = de.getVal();
			}
		}
	}

	public void build(Cliff cliff) {
		cliff.getTile().setCliffShapeID(def.getId());
		if (naturalFaceBuilder != null) {
			cliff.face = ((NaturalFaceBuilder)BuilderManager.getBuilder("model.builders.CliffShapeBuilder",naturalFaceBuilderID,NaturalFaceBuilder.class)).build(cliff);
		} else {
			cliff.face = ((ManmadeFaceBuilder)BuilderManager.getBuilder("model.builders.ManmadeFaceBuilder",manmadeFaceBuilderID,ManmadeFaceBuilder.class)).build(cliff);
		}
		int i = 0;
		cliff.trinkets.clear();
	}

	public String getID() {
		return def.getId();
	}

	public String getIconPath() {
		return editorIconPath;
	}

	@Override
	public void readFinalizedLibrary() {
		if (!naturalFaceBuilderID.isEmpty()) {
			//naturalFaceBuilder = (NaturalFaceBuilder)BuilderManager.getBuilder("model.builders.CliffShapeBuilder",naturalFaceBuilderID,NaturalFaceBuilder.class).build(naturalFaceBuilderID);
		}
		if (!manmadeFaceBuilderID.isEmpty()) {
			//manmadeFaceBuilder = BuilderManager.getManmadeFaceBuilder(manmadeFaceBuilderID);
		}
		for (String s : trinketBuildersID) {
			//trinketBuilders.add(BuilderManager.getTrinketBuilder(s));
		}
		for (String s : rampTrinketBuildersID) {
			//rampTrinketBuilders.add(BuilderManager.getTrinketBuilder(s));
		}
	}
}
