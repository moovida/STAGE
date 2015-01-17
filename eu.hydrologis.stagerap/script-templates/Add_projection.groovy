// define the necessary imports
import org.jgrasstools.modules.*

// the path with the files that need a projection file to be created
folder = "/path/to/folder/"

// the epsg code of the projection file to create. Ex: UTM32N
epsg = 32632 

// run the creation of files
FileIterator.addPrj(folder,"EPSG:" + epsg)