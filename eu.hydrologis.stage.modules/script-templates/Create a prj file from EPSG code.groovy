import org.geotools.referencing.CRS;

epsg  = "EPSG:4326";
path = "/media/hydrologis/FATBOTTOMED/dati_unibz/2015_06_rilievo_drone/20150609_it-valaur_aoi683_merged_gcp_dsm.prj";

wkt = CRS.decode(epsg).toWKT();
new File(path) << wkt;
