/*
 * To change this template, choose Tools | Templates and open the template in the editor.
 */
package model.editor.tools;

import geometry.geom2d.Point2D;
import geometry.tools.LogUtil;

import java.nio.ByteBuffer;
import java.util.ArrayList;

import model.ModelManager;
import model.battlefield.map.atlas.Atlas;
import model.battlefield.map.atlas.AtlasExplorer;
import model.battlefield.map.atlas.AtlasLayer;
import model.editor.AssetSet;
import model.editor.Pencil;
import model.editor.ToolManager;

/**
 * @author Benoît
 */
public class AtlasTool extends Tool {
	private static final String ADD_DELETE_OP = "add/delete";
	private static final String PROPAGATE_SMOOTH_OP = "propagate/smooth";

	Atlas atlas;
	AtlasExplorer explorer;

	int autoLayer;
	double increment = 40;

	public AtlasTool(ToolManager manager) {
		super(manager, ADD_DELETE_OP, PROPAGATE_SMOOTH_OP);
		this.atlas = ModelManager.battlefield.map.atlas;
		explorer = new AtlasExplorer(ModelManager.battlefield.map);
		set = new AssetSet(ModelManager.battlefield.map.style.textures, true);
	}

	@Override
	protected void createPencil() {
		pencil = new Pencil();
		pencil.size = 2;
		pencil.sizeIncrement = 0.25;
		pencil.strength = 0.5;
		pencil.strengthIncrement = 0.01;
	}

	@Override
	public void primaryAction() {
		switch (actualOp) {
			case ADD_DELETE_OP:
				increment(getInvolvedPixels());
				break;
			case PROPAGATE_SMOOTH_OP:
				propagate(getInvolvedPixels());
				break;
		}

		manager.updateGroundAtlas();
	}

	@Override
	public void secondaryAction() {
		switch (actualOp) {
			case ADD_DELETE_OP:
				decrement(getInvolvedPixels());
				break;
			case PROPAGATE_SMOOTH_OP:
				smooth(getInvolvedPixels());
				break;
		}
		manager.updateGroundAtlas();
	}

	public ArrayList<Point2D> getInvolvedPixels() {
		switch (pencil.shape) {
			case Circle:
				return explorer.getPixelsInMapSpaceCircle(pencil.getCoord(), pencil.size / 2);
			case Diamond:
				return explorer.getPixelsInMapSpaceDiamond(pencil.getCoord(), pencil.size / 2);
			case Square:
				return explorer.getPixelsInMapSpaceSquare(pencil.getCoord(), pencil.size / 2);
			default:
				throw new RuntimeException();
		}
	}

	private void increment(ArrayList<Point2D> pixels) {
		for (Point2D p : pixels) {
			increment(p, set.actual);
		}
	}

	private void increment(Point2D p, int layer) {
		int x = (int) Math.round(p.x);
		int y = (int) Math.round(p.y);
		double attenuatedInc = increment * pencil.strength * pencil.getApplicationRatio(explorer.getInMapSpace(p));

		double valueToDitribute = attenuatedInc;
		ArrayList<AtlasLayer> availableLayers = new ArrayList<>();
		for (AtlasLayer l : atlas.layers) {
			if (atlas.layers.indexOf(l) == layer) {
				valueToDitribute -= l.addAndReturnExcess(x, y, attenuatedInc);
			} else {
				availableLayers.add(l);
			}
		}
		int secur = -1;
		while (valueToDitribute > 0 && !availableLayers.isEmpty() && secur++ < 50) {
			ArrayList<AtlasLayer> unavailableLayers = new ArrayList<>();
			double shared = valueToDitribute / availableLayers.size();
			valueToDitribute = 0;
			for (AtlasLayer l : availableLayers) {
				valueToDitribute += l.withdrawAndReturnExcess(x, y, shared);
				if (l.get(x, y) == 0) {
					unavailableLayers.add(l);
				}
			}
			availableLayers.removeAll(unavailableLayers);
		}
		if (secur > 40) {
			LogUtil.logger.warning("Impossible to distribute value");
		}
		atlas.updatePixel(x, y);
	}

	private void decrement(ArrayList<Point2D> pixels) {
		for (Point2D p : pixels) {
			decrement(p, set.actual);
		}
	}

	private void decrement(Point2D p, int layer) {
		int x = (int) Math.round(p.x);
		int y = (int) Math.round(p.y);
		double attenuatedInc = increment * pencil.strength * pencil.getApplicationRatio(explorer.getInMapSpace(p));

		double valueToDitribute = attenuatedInc;
		ArrayList<AtlasLayer> availableLayers = new ArrayList<>();
		for (AtlasLayer l : atlas.layers) {
			if (atlas.layers.indexOf(l) == layer) {
				valueToDitribute -= l.withdrawAndReturnExcess(x, y, attenuatedInc);
			} else if (l.get(x, y) > 0) {
				availableLayers.add(l);
			}
		}
		if (availableLayers.isEmpty()) {
			availableLayers.add(atlas.layers.get(0));
		}

		int secur = -1;
		while (valueToDitribute > 0 && !availableLayers.isEmpty() && secur++ < 50) {
			ArrayList<AtlasLayer> unavailableLayers = new ArrayList<>();
			double shared = valueToDitribute / availableLayers.size();
			valueToDitribute = 0;
			for (AtlasLayer l : availableLayers) {
				valueToDitribute += l.addAndReturnExcess(x, y, shared);
				if (l.get(x, y) == 255) {
					unavailableLayers.add(l);
				}
			}
			availableLayers.removeAll(unavailableLayers);
		}
		if (secur > 40) {
			LogUtil.logger.warning("Impossible to distribute value");
		}
		atlas.updatePixel(x, y);
	}

	private void propagate(ArrayList<Point2D> pixels) {
		if (!pencil.maintained) {
			pencil.maintain();
			autoLayer = 0;
			Point2D center = pencil.getCoord().getMult(atlas.width, atlas.height).getDivision(ModelManager.battlefield.map.width, ModelManager.battlefield.map.height);
			int centerX = (int) Math.round(center.x);
			int centerY = (int) Math.round(center.y);
			for (AtlasLayer l : atlas.layers) {
				if (l.get(centerX, centerY) > atlas.layers.get(autoLayer).get(centerX, centerY)) {
					autoLayer = atlas.layers.indexOf(l);
				}
			}
		}
		for (Point2D p : pixels) {
			increment(p, autoLayer);
		}
	}

	private void smooth(ArrayList<Point2D> pixels) {
		for (Point2D p : pixels) {
			int x = (int) Math.round(p.x);
			int y = (int) Math.round(p.y);
			double attenuatedInc = increment
					* pencil.strength
					* pencil.getApplicationRatio(new Point2D(x, y).getMult(ModelManager.battlefield.map.width, ModelManager.battlefield.map.height).getDivision(
							atlas.width, atlas.height));

			int activeLayerCount = 0;
			for (AtlasLayer l : atlas.layers) {
				if (l.get(x, y) != 0) {
					activeLayerCount++;
				}
			}
			double targetVal = 255 / activeLayerCount;
			for (AtlasLayer l : atlas.layers) {
				if (l.get(x, y) != 0) {
					double diff = targetVal - l.get(x, y);
					if (diff < 0) {
						l.set(x, y, l.get(x, y) + Math.max(diff, -attenuatedInc));
					} else if (diff > 0) {
						l.set(x, y, l.get(x, y) + Math.min(diff, attenuatedInc));
					}
				}
			}
			atlas.updatePixel(x, y);
		}

	}
}
