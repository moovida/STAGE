// define the necessary imports
import org.jgrasstools.modules.*

// the input file that needs to be styled
map = "@df/file.tif"
// the output sld file to be created
sld = "@df/file.sld"

// create the raster styler on the map file
styler = new RasterStyle(RasterReader.readRaster(map))

// set a transparency value
styler.setAlpha(0.8)

/*
	choose the elevation raster style
    (possible values are:
    rainbow, extrainbow, aspect, flow, bathymetric,
    elev, logarithmic, radiation, net, shalstab,
    greyscale, greyscaleinverse, geomorphon, sea, slope)
*/      
newStyle = styler.style("elev")

// write the style into the sld file
sldFile = new File(sld)
sldFile.delete()
println "Writing style to file: " + sld
sldFile << newStyle
println "Finished."
