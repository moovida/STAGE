import org.geotools.referencing.CRS;

epsg  = "EPSG:4326";
path = "output_path.prj";

wkt = CRS.decode(epsg).toWKT();
new File(path) << wkt;
