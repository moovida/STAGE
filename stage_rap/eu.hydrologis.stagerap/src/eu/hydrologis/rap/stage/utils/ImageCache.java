/*
 * JGrass - Free Open Source Java GIS http://www.jgrass.org 
 * (C) HydroloGIS - www.hydrologis.com 
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * (http://www.eclipse.org/legal/epl-v10.html), and the HydroloGIS BSD
 * License v1.0 (http://udig.refractions.net/files/hsd3-v10.html).
 */
package eu.hydrologis.rap.stage.utils;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

import eu.hydrologis.rap.stage.utilsrap.ImageUtil;

/**
 * A singleton cache for images.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
@SuppressWarnings("nls")
public class ImageCache {

    public static final String CATEGORY = "category.gif";
    public static final String MODULE = "module.gif";
    public static final String MODULEEXP = "module_exp.gif";
    public static final String RUN = "run_module.gif";
    public static final String STOP = "stop_module.gif";
    public static final String GRID = "grid_obj.gif";
	
    public static final String OPEN = "prj_obj.gif";
    public static final String SAVE = "save_edit.gif";
	
	private static ImageCache imageCache;

	private HashMap<String, Image> imageMap = new HashMap<String, Image>();

	private ImageCache() {
	}

	public static ImageCache getInstance() {
		if (imageCache == null) {
			imageCache = new ImageCache();
		}
		return imageCache;
	}

	/**
	 * Get an image for a certain key.
	 * 
	 * <p>
	 * <b>The only keys to be used are the static strings in this class!!</b>
	 * </p>
	 * 
	 * @param key
	 *            a file key, as for example {@link ImageCache#DATABASE_VIEW}.
	 * @return the image.
	 */
	public Image getImage(Display display, String key) {
		Image image = imageMap.get(key);
		if (image == null) {
			image = createImage(display, key);
			imageMap.put(key, image);
		}
		return image;
	}

	private Image createImage(Display display, String key) {
		Image image = ImageUtil.getImage(display, key);
		return image;
	}

	/**
	 * Disposes the images and clears the internal map.
	 */
	public void dispose() {
		Set<Entry<String, Image>> entrySet = imageMap.entrySet();
		for (Entry<String, Image> entry : entrySet) {
			entry.getValue().dispose();
		}
		imageMap.clear();
	}

}
