library(parallel)
library(doMC)
library(raster)
library(sp)



# Get java home using rJava

getJdkPath = function(x) {
  require(rJava)
  .jinit()
  jdk.base.path <- .jcall( 'java/lang/System', 'S', 'getProperty', 'java.home' )
  # if it does not work, 
  # jdk.base.path <- "/Library/Java/JavaVirtualMachines/jdk1.8.0_171.jdk/Contents/Home/" # OSX
  # jdk.base.path <- "/usr/lib/jvm/java-8-openjdk-amd64/" # Linux
  
  jdk.bin.path <- paste0(jdk.base.path, "bin/java")
  return(jdk.bin.path)
}

java.mx <- "12g" # 25 GB max in LRZ
java.ms <- "1g -d64"  

# .jinit( parameters="-Dfile.encoding=UTF-8 -Dlog4j.configuration=log4j_cluster.properties")
# .jinit(parameters = "-Dfile.encoding=UTF-8", silent = FALSE, force.init = FALSE)
# .jinit( parameters=paste0("-Xms", java.ms, " -Xmx", java.mx)) # The .jinit returns 0 if the JVM got initialized and a negative integer if it did not. A positive integer is returned if the JVM got initialized partially. Before initializing the JVM, the rJava library must be loaded.
# .jclassPath() # print out the current class path settings. 


aft.names.fromzero <- c( "Ext_AF", "IA", "Int_AF", "Int_Fa", "IP", "MF", "Min_man", "Mix_Fa", "Mix_For", "Mix_P", "Multifun", "P-Ur", "UL", "UMF", "Ur", "VEP", "EP")



CRAFTY_main_name = "org.volante.abm.serialization.ModelRunner" # Better using the reflection based API in rJava.  

crafty_sp =NA 



# does not work on linux 
# INTERACTIVE = FALSE # do interactive run
# if (INTERACTIVE) { 
#     CRAFTY_sargs[length(CRAFTY_sargs) + 1 ] = "-i"
# }


# public void doOutput(Regions r) {
#     for (Outputter o : outputs) {
#         if (this.runInfo.getSchedule().getCurrentTick() >= o.getStartYear()
#             && (this.runInfo.getSchedule().getCurrentTick() <= o.getEndYear())
#             && (this.runInfo.getSchedule().getCurrentTick() - o.getStartYear())
#             % o.getEveryNYears() == 0) {
#             // <- LOGGING
#             log.info("Handle outputter " + o);
#             // LOGGING ->
#                 
#                 o.doOutput(r);
#         }
#     }
# }

# public void regionsToRaster(String filename, Regions r, CellToDouble converter,
# boolean writeInts, DecimalFormat format, String nDataString) throws Exception {

# r = allregions_iter$'next'()
# .jmethods(r)
# allcells_iter = r$getAllCells()$iterator()
# # r2 = .jcast(allregions$iterator(), new.class = "org/volante/abm/data/Region", check = T, convert.array = F)
# 
# 
# allcells_iter$
#     
#     
#     c = allcells_iter$'next'()
# c$getX()
# c$getY()
# fl = c$getOwnersFrLabel()
# 
# # val = allcells_iter$'next'()$getOwnersFrLabel()
# 
# val =  lapply(allcells_iter, function(c) c$getOwnersFrLabel() )
# fr_iter = r$getFunctionalRoles()$iterator()
# 
# while(fr_iter$hasNext()) { 
#     
# }
# f = fr_iter$'next'()
# f$getLabel()



# regionsToRaster(fn, r, this, isInt(), doubleFmt, nDataString);
# region r = .. 
# Extent e = r.getExtent();
# Raster raster = new Raster(e.getMinX(), e.getMinY(), e.getMaxX(), e.getMaxY());
# raster.setNDATA(nDataString);
# for (Cell c : r.getAllCells()) {
#     raster.setXYValue(c.getX(), c.getY(), converter.apply(c));
# }

# RasterWriter writer = new RasterWriter();
# if (format != null) {
#     writer.setCellFormat(format);
# } else if (writeInts) {
#     writer.setCellFormat(RasterWriter.INT_FORMAT);
# }
# writer.writeRaster(filename, raster);


