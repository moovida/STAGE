// define the necessary imports
import org.jgrasstools.modules.*

// the input file that needs to be styled
map = "DATAFOLDER/file.tif"
// the output sld file to be created
sld = "DATAFOLDER/file.sld"

// create the raster styler on the map file
styler = new RasterStyle(RasterReader.readRaster(map))

// set a transparency value
styler.setAlpha(0.8)

// choose the elevation raster style
newStyle = styler.style("elev")

// write the style into the sld file
sldFile = new File(sld)
println "Writing style to file: " + sld
sldFile << newStyle
println "Finished."
