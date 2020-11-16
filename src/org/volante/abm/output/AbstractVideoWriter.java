/**
 * This file is part of
 * 
 * CRAFTY - Competition for Resources between Agent Functional TYpes
 *
 * Copyright (C) 2014 School of GeoScience, University of Edinburgh, Edinburgh, UK
 * 
 * CRAFTY is free software: You can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software 
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *  
 * CRAFTY is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * School of Geoscience, University of Edinburgh, Edinburgh, UK
 * 
 */
package org.volante.abm.output;


import static org.monte.media.FormatKeys.EncodingKey;
import static org.monte.media.FormatKeys.FrameRateKey;
import static org.monte.media.FormatKeys.MediaTypeKey;
import static org.monte.media.VideoFormatKeys.DepthKey;
import static org.monte.media.VideoFormatKeys.ENCODING_AVI_PNG;
import static org.monte.media.VideoFormatKeys.HeightKey;
import static org.monte.media.VideoFormatKeys.QualityKey;
import static org.monte.media.VideoFormatKeys.WidthKey;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import org.apache.log4j.Logger;
import org.monte.media.Format;
import org.monte.media.FormatKeys.MediaType;
import org.monte.media.avi.AVIWriter;
import org.monte.media.math.Rational;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.volante.abm.data.ModelData;
import org.volante.abm.data.Regions;
import org.volante.abm.output.Outputs.CloseableOutput;
import org.volante.abm.schedule.RunInfo;
import org.volante.abm.serialization.GloballyInitialisable;


public abstract class AbstractVideoWriter implements CloseableOutput, Outputter,
		GloballyInitialisable {

	/**
	 * Output name Subclasses should provide sensible defaults for this in initialise if it is blank
	 */
	@Attribute(required = false)
	String				output			= "";
	/**
	 * Number of frames per second in the video file
	 */
	@Attribute(required = false)
	long				frameRate		= 1;
	/**
	 * Number of times to write an image to the file each tick. Can be used to make videos with
	 * slower updates than 1 per second.
	 */
	@Attribute(required = false)
	long				imagesPerFrame	= 1;
	/**
	 * Width of the video file. Defaults to 500px
	 */
	@Attribute(required = false)
	int					width			= 500;
	/**
	 * Height of the video file. Defaults to 500px
	 */
	@Attribute(required = false)
	int					height			= 500;

	@Attribute(required = false)
	float quality = 1.0f;

	/**
	 * Should the current tick be added to the images?
	 */
	@Attribute(required = false)
	boolean				addTick			= true;

	@Attribute(required = false)
	float tickSize = 36.0f;

	@Attribute(required = false)
	String tickPrefix = "t=";

	/**
	 * If < 0 it's subtracted from width
	 */
	@Attribute(required = false)
	protected int tickLocationX = 2;

	/**
	 * If < 0 it's subtracted from height
	 */
	@Attribute(required = false)
	protected int tickLocationY = -2;

	@Attribute(required = false)
	protected int		everyNYears		= 1;
	@Attribute(required = false)
	protected int		startYear		= 1;
	@Attribute(required = false)
	protected int		endYear			= Integer.MAX_VALUE;

	@Element(required = false)
	Color tickColor = new Color(0.0f, 0.6f, 0.3f, 0.5f);

	NumberFormat		tickFormat		= new DecimalFormat("000");

	protected AVIWriter	out;
	protected String	fn;
	protected Logger	log				= Logger.getLogger(getClass());
	protected Outputs	outputs;
	protected RunInfo	info;
	protected ModelData	data;

	@Override
	public void open() {
		try {
			fn = outputs.getOutputFilename(output, ".avi", this.data.getRootRegionSet()); // Construct proper output
																						  // filename
			File file = new File(fn);
			Format format = new Format(MediaTypeKey, MediaType.VIDEO, //
					EncodingKey, ENCODING_AVI_PNG,
					FrameRateKey, new Rational(frameRate, 1),//
					WidthKey, width, //
					HeightKey, height,//
					DepthKey, 24, QualityKey, quality);
			out = new AVIWriter(file);
			log.info("Starting video file: " + fn + " using " + out + " on file: " + file + ", w:"
					+ width + ",p_rest:" + height);
			BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
			out.addTrack(format);
			out.setPalette(0, image.getColorModel());
		} catch (IOException e) {
			log.error("Couldn't start video file: " + fn);
			e.printStackTrace();
		}
	}

	@Override
	public void doOutput(Regions r) {
		if (out == null) {
			return;
		} else if (info.getSchedule().getCurrentTick() >= this.getStartYear() &&
				info.getSchedule().getCurrentTick() <= this.getEndYear() &&
				(info.getSchedule().getCurrentTick() - this.getStartYear())
						% this.getEveryNYears() == 0) {
			try {
				for (int i = 0; i < imagesPerFrame; i++) {
					BufferedImage image = getImage(r);
					if (addTick) {
						Graphics2D g = image.createGraphics();
						g.setColor(tickColor);
						g.setFont(g.getFont().deriveFont(tickSize).deriveFont(Font.BOLD));
						g.drawString(tickPrefix + tickFormat.format(info.getSchedule().getCurrentTick()),
								tickLocationX < 0 ? width + tickLocationX : tickLocationX, tickLocationY < 0 ? height
										+ tickLocationY : tickLocationY);
						g.dispose();
					}
					out.write(0, image, 1);
				}
			} catch (IOException e) {
				log.error("Couldn't write file to " + fn);
				e.printStackTrace();
			}
		}
	}

	abstract BufferedImage getImage(Regions r);

	@Override
	public void close() {
		if (out == null) {
			return;
		}
		try {
			out.close();
			log.info("Closed video file: " + fn);
		} catch (IOException e) {
			log.error("Couldn't close video file: " + fn);
			e.printStackTrace();
		}
	}

	@Override
	public void initialise(ModelData data, RunInfo info) throws Exception {
		outputs = info.getOutputs();
		outputs.registerClosableOutput(this);
		this.info = info;
		this.data = data;
	}

	@Override
	public void initialise() throws Exception {
	} // Do it all in the real initialise

	@Override
	public void setOutputManager(Outputs outputs) {
		this.outputs = outputs;
	}

	/**
	 * @see org.volante.abm.output.Outputter#getStartYear()
	 */
	@Override
	public int getStartYear() {
		return this.startYear;
	}

	/**
	 * @see org.volante.abm.output.Outputter#getEndYear()
	 */
	@Override
	public int getEndYear() {
		return this.endYear;
	}

	/**
	 * @see org.volante.abm.output.Outputter#getEveryNYears()
	 */
	@Override
	public int getEveryNYears() {
		return this.everyNYears;
	}
}
