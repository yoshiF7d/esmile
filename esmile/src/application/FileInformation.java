package application;
import java.io.File;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.ImageInputStream;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class FileInformation {
	String dirName;
	String fileName;
	String dateTime;
	String unitString;
	double unit;
	double resolution_per_unit;
	double resolution;
	int fadingTime;
	double latitude;
	double voltage;
	
	FileInformation(File file) {
		try {
			ImageInputStream iis = ImageIO.createImageInputStream(file);
			Iterator<ImageReader> readers = ImageIO.getImageReaders(iis);
			if (readers.hasNext()) {
				// pick the first available ImageReader
				ImageReader reader = readers.next();
				// attach source to the reader
				reader.setInput(iis, true);
				// read metadata of first image
				IIOMetadata metadata = reader.getImageMetadata(0);
				String[] names = metadata.getMetadataFormatNames();
				extract(metadata.getAsTree(names[0]));
				resolution = resolution_per_unit*unit;
				fileName = file.getName();
				dirName = file.getParent();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	void extract(Node root) {
		extract(root, 0);
	}

	void parseUnit(String value) {
		switch(Integer.parseInt(value)) {
		case 1:
			unit = 1; unitString = "a.u"; break;
		case 2:
			unit = 0.0254; unitString = "inch"; break;
		case 3:
			unit = 0.01; unitString = "cm"; break;
		}
	}	
	
	void parseResolution(String value) {
		String[] tokens = value.split("/");
		resolution_per_unit = ((double)Integer.parseInt(tokens[1]))/Integer.parseInt(tokens[0]);
	}
	
	void parseDescription(String value) {
		Pattern pt = Pattern.compile("(?<time>\\d+)(min)?");
		Pattern pv = Pattern.compile("PMT=(?<voltage>\\d+)V");
		Pattern pl = Pattern.compile("Latitude=(?<latitude>\\d+)");
		Matcher mt,mv,ml;
		
		mt = pt.matcher(value);
		mv = pv.matcher(value);
		ml = pl.matcher(value);
		if(mt.find()) {
			fadingTime = Integer.parseInt(mt.group("time"));
		}
		if(mv.find()) {
			voltage = Integer.parseInt(mv.group("voltage"));
		}
		if(ml.find()) {
			latitude = Integer.parseInt(ml.group("latitude"));
		}
		
	}
	void extract(Node node, int level) {
		// print open tag of element
		if(node.getNodeName().equals("TIFFField")) {
			NamedNodeMap map = node.getAttributes();
			if (map != null) {
				// print attribute values
				Node attr = map.item(1);
				String value = node.getFirstChild().getFirstChild()
						.getAttributes().item(0).getNodeValue();
				switch(attr.getNodeValue()) {
				case "ResolutionUnit":
					parseUnit(value); break;
				case "XResolution":
					parseResolution(value); break;
				case "DateTime":
					dateTime = value; break;
				case "ImageDescription":
					parseDescription(value); break;
				}	
			}
			return;
		}

		Node child = node.getFirstChild();
		if (child == null) {
			// no children, so close element and return
			return;
		}
		// children, so close current tag
		while (child != null) {
			// print children recursively
			extract(child, level + 1);
			child = child.getNextSibling();
		}
	}
	public void print(){
		System.out.println("|----------------------------------------------------|");
		System.out.printf("| File Name          : %-29s |\n",fileName);
		System.out.printf("| Fading Time        : %-29s |\n",String.format("%d min",fadingTime));
		System.out.printf("| Latitude           : %-29g |\n",latitude);
		System.out.printf("| Voltage            : %-29s |\n",String.format("%g V", voltage));
		System.out.printf("| Resolution         : %-29s |\n",String.format("%e %s per pixel", resolution_per_unit,unitString));
		System.out.printf("| Date Time          : %-29s |\n",dateTime);
		System.out.println("|----------------------------------------------------|");
	}
	public String header(){
		return 
		"# File Information ----------------------------------#\n" +
		String.format("# File Name          : %-29s #\n",fileName) +
		String.format("# Fading Time        : %-29s #\n",String.format("%d min",fadingTime)) + 
		String.format("# Latitude           : %-29g #\n",latitude) + 
		String.format("# Voltage            : %-29s #\n",String.format("%s V", voltage)) +
		String.format("# Resolution         : %-29s #\n",String.format("%s um per pixel", resolution*1e+6)) +
		String.format("# Date Time          : %-29s #\n",dateTime) + 
		"#----------------------------------------------------#\n\n";
	}
}
