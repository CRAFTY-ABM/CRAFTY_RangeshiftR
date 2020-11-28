
library(rgdal)
library(viridis)
library(raster)
library(sp)
library(jdx)
library(xml2)
# library(doSNOW) # doMC unavailable for Windows


###################################### 
# location of the cloned repository 
path_base = "~/git/CRAFTY_RangeshiftR2/"
# Input data
path_crafty_inputdata = path.expand(paste0(path_base, "data_LondonOPM/"))

# Output folder 
path_crafty_batch_run = path.expand(paste0("~/tmp"))



setwd(path_base) 

source("RScripts/Functions_CRAFTY_rJava.R")
# source("RScripts/Functions_CRAFTY_common.R")

###### OPM meta data
aft_names_fromzero = c("mgmt_highInt", "mgmt_lowInt", "mgmt_medInt", "no_mgmt_NOPM", "no_mgmt_unable")
aft_cols = viridis::viridis(5)
london_xy_df = read.csv(paste0(path_base, "data-processed/Cell_ID_XY_Borough.csv"))

x_coords_v = sort(unique(london_xy_df$X))
x_coords_bng_v =london_xy_df[match(x_coords_v, london_xy_df$X), "x_coord"]

y_coords_v = sort(unique(london_xy_df$Y))
y_coords_bng_v =london_xy_df[match(y_coords_v, london_xy_df$Y), "y_coord"]


hx = readOGR("data-processed/hexgrids", layer = "hexGrid40m")


proj4.BNG =  "+proj=tmerc +lat_0=49 +lon_0=-2 +k=0.9996012717 +x_0=400000 +y_0=-100000 +ellps=airy +towgs84=446.448,-125.157,542.06,0.15,0.247,0.842,-20.489 +units=m +no_defs"




#############################################################
# Location of the CRAFTY Jar file
path_crafty_jar = path.expand(paste0(path_base, "lib/CRAFTY_KIT_engineOct2020.jar"))
# Location of the CRAFTY lib files
path_crafty_libs = path.expand(paste0(path_base, "lib/"))
crafty_libs = list.files(paste0(path_crafty_libs), pattern = "jar")

# Make sure that in the classpath setting , gt-opengis-9.0.jar must be included before geoapi-20050403.jar. Otherwise it throws an uncatchable error during the giving up process: loading libraries without ordering them particularly, the opengis library is loaded after the geoapi library following alphabetical order.
# Related commit - https://github.com/CRAFTY-ABM/CRAFTY_CoBRA/commit/4ce1041cae349572032fc7e25be49652781f5866

crafty_libs = crafty_libs[crafty_libs != "geoapi-20050403.jar"  ] 
crafty_libs = c(crafty_libs,  "geoapi-20050403.jar")






### Scenario  

# Name of the scenario file

scenario.filename = "Scenario_Baseline_noGUI.xml" # no display

#### JAVA configuration 
crafty_jclasspath = c(
  path_crafty_jar
  , paste0(path_crafty_libs, crafty_libs)
  
)




# change wd to the output folder to store output files
setwd(path_crafty_batch_run) 

# (When parallelising) initialise jvm in forked processes / not before parallelism is initiated
# https://stackoverflow.com/questions/24337383/unloading-rjava-and-or-restarting-jvm
# "There is a way to run expressions using rJava in parallel based on running the parallel processes to get and assemble all results BEFORE you load the rJava library in the main process. As the main R process has not initiated jvm then java is started in each single subprocess and this particular instance will die together with subprocess as well."

library(rJava)

if (!rJava::.jniInitialized) { # initialize only once 
  
  .jinit(parameters="-Dlog4j.configuration=log4j2020_normal.properties")
  .jinit(parameters = "-Dfile.encoding=UTF-8", silent = FALSE, force.init = FALSE)
  .jinit( parameters=paste0("-Xms", java.ms, " -Xmx", java.mx)) # The .jinit returns 0 if the JVM got initialized and a negative integer if it did not. A positive integer is returned if the JVM got initialized partially. Before initializing the JVM, the rJava library must be loaded.
  
  # .jinit(parameters = paste0("user.dir=", path_crafty_batch_run )) # does not work.. 
}


# add java classpath
.jclassPath() # print out the current class path settings.
for (i in 1:length(crafty_jclasspath)) { 
  .jaddClassPath(crafty_jclasspath[i])
}

# set the batch run folder (path_crafty_batch_run)
.jcall( 'java/lang/System', 'S', 'setProperty', 'user.dir',  path_crafty_batch_run )

# assertion
stopifnot(path_crafty_batch_run== .jcall( 'java/lang/System', 'S', 'getProperty', 'user.dir' ))




# # If needs to change the content of the scenario xml file on the fly
# modifyScenario = FALSE
# if (modifyScenario) {
#   
#   scenario.filename.template = "Scenario_Baseline_noGUI.xml"  
#   # Read the scenario file
#   scenario.xml <- xml2::read_xml(paste0(path_crafty_inputdata, scenario.filename.template))
#   # str(scenario.xml)
#   scenario.l <- xml2::as_list(scenario.xml)
#   
#   
#   # e.g. Replace scenario name
#   attr(scenario.l$scenario, "scenario") <- "USER_SCENARIO_NAME"
#   # Replace version info
#   attr(scenario.l$scenario, "version") <- "USER_VERSION_NAME"
#   
#   ## Write the modified competition file
#   scenario.xml.modified <- xml2::as_xml_document(scenario.l)
#   
#   xml2::write_xml(scenario.xml.modified, paste0(path_crafty_inputdata, scenario.filename), options = "no_empty_tags")
#   
#   # Can edit other xml files in the same manner
#   # e.g. competiton file
#   # for (u.idx in 1:length(utility.b)) {
#   #     print(  attr(competition.l$competition[[u.idx]]$curve, "b") <- as.character(utility.b[u.idx]))
#   # }
#   
# }


############# CRAFTY configuration
# Model run 

# Scenario  
CRAFTY_sargs =   c("-d", path_crafty_inputdata, "-f", scenario.filename, "-o", "99", "-r", "1",  "-n", "1", "-sr", "0") # change the argument as you wish. 
# -ed: end of the simulation (overriding the parameter in the scenario file)

start_year_idx = 1 # first year of the input data
end_year_idx = 10 # 10th year of the input data 


parallelize = FALSE

########### Model running 
print(paste0("============CRAFTY JAVA-R API: Create the instance"))

CRAFTY_jobj = new(J(CRAFTY_main_name)) # Create a new instance (to call non-static methods)

# prepares a run and returns run information 
CRAFTY_RunInfo_jobj = CRAFTY_jobj$EXTprepareRrun(CRAFTY_sargs)
print(paste0("============CRAFTY JAVA-R API: Run preparation done"))

# set the schedule
CRAFTY_loader_jobj = CRAFTY_jobj$EXTsetSchedule(as.integer(start_year_idx), as.integer(end_year_idx))


# To visualise output
doProcessFR = T # visualisation


# rJava occasionally hangs.. it has something to do with the JVM initialisation (don't initialise more than once)
if (doProcessFR) { # visiaulisation
  
  # slower..  
  # system.time({ 
  #     val_fr = sapply(allcells_l, function(c) c$getOwnersFrLabel() )
  # })
  # print("sapply")
  # system.time({
  #     val_fr = sapply(allcells_l2, function(c) c$getOwnersFrLabel() )
  # })
  
  region = CRAFTY_loader_jobj$getRegions()$getAllRegions()$iterator()$'next'()
  
  # # alloc_m = region$getAllocationModel()
  # # .jmethods(alloc_m)
  # # btmap = region$getBehaviouralTypeMapByLabel() 
  
  
  allcells_uset = region$getAllCells() 
  allcells_l =   as.list(allcells_uset)
  
  
  #### Get XY coordinates
  print("Get XY coords")
  
  system.time({
    val_xy =foreach(c = allcells_l, .combine = "rbind") %do% { 
      c(X=c$getX(), Y=c$getY())
    }
  })
  
  val_xy = data.frame(val_xy)
  colnames(val_xy) = c("X", "Y")
  x_coord = london_xy_df[match(val_xy$X, london_xy_df$X), "x_coord"]
  y_coord = london_xy_df[match(val_xy$Y, london_xy_df$Y), "y_coord"]
   
  cellid = foreach(rowid = 1:nrow(val_xy), .combine = "c") %do% { 
    which((as.numeric(val_xy[rowid, 1]) == london_xy_df$X) & (as.numeric(val_xy[rowid, 2]) == london_xy_df$Y))
  }
  
  
  
  
  help(match)
  
  crafty_coords = cbind(x_coord, y_coord)
  na_idx = is.na(crafty_coords[,1]) | is.na(crafty_coords[,2])
  crafty_coords = crafty_coords[!na_idx,]
  
  crafty_sp =SpatialPoints(crafty_coords)
  proj4string(crafty_sp) = proj4.BNG
  # plot(crafty_sp)  
}

nticks = length(start_year_idx:end_year_idx)
plot_return_list = vector("list", nticks)

# crafty main loop
for (tick in start_year_idx:end_year_idx) {
  
  nextTick = CRAFTY_jobj$EXTtick()
  
  stopifnot(nextTick == (tick + 1 )) # assertion
  
  
  ######
  ######
  # safe to alter capital files here
  ######
  ######
  
  
  
  # Yet experimental as rJava frequently hangs..
  if (doProcessFR) {
    
    region = CRAFTY_loader_jobj$getRegions()$getAllRegions()$iterator()$'next'()
    
    # allcells = region$getAllCells()
    allcells_uset = region$getAllCells() # slower
    # allcells_arr = allcells_uset$toArray() # slower # often throws jave execption/warning
    allcells_l =   as.list(allcells_uset)
    
    print("Process output")
    
    # visualise something
    
    print(Sys.time())
    
    if (parallelize) {
      system.time({
        val_df = foreach(c = allcells_l, .combine = rbind, .packages = c("rJava"), .verbose = F, .export = c("region",  "allcells_l") ) %dopar% {
          c(  c$getOwnersFrLabel(), c$getEffectiveCapitals()$getAll(), c$getSupply()$getAll())
        }
        val_fr = val_df[,1]
        
      })
    } else  {
      system.time({
        val_df = t(sapply(allcells_l, FUN = function(c) c(  c$getOwnersFrLabel()#, c$getEffectiveCapitals()$getAll(), c$getSupply()$getAll()
        )))
        val_fr = val_df[1,]
        
      })
    }
    print(Sys.time())
    
    
    
    val_fr_fac = factor(val_fr,  labels = aft_names_fromzero, levels = aft_names_fromzero)
    

 
     
    fr_spdf = SpatialPixelsDataFrame(crafty_sp, data =data.frame( as.numeric(val_fr_fac )), tolerance = 0.0011)
    fr_r = raster(fr_spdf)
    # plot(fr_r)
    # par(mfrow=c(3,3))
    val_cols = aft_cols[val_fr_fac]
    
    plot(fr_r, main = paste0("Tick=", tick), xlab = "X_BNG", ylab = "Y_BNG", col = val_cols, legend=F)
    legend("topright", legend = aft_names_fromzero, fill = aft_cols)
    # rm(allregions_iter)
    rm(allcells_l)
    
    
    plot_return_list[[tick]] = fr_r
    
    ####
    # find hexagons 
    hx_idx = match(cellid, hx$joinID )
    
    hx$fr = NA
    # add FR to the hexagonal grid
    hx$fr[hx_idx] = val_fr
    
  }
  
  
  if (nextTick <= end_year_idx) {
    print(paste0("============CRAFTY JAVA-R API: NextTick=", nextTick))
  } else {
    print(paste0("============CRAFTY JAVA-R API: Simulation done (tick=", tick, ")"))
    
  }
}


fr_returned = stack(plot_return_list)



fr_tb = apply( getValues(fr_returned), MARGIN = 2, FUN = function(x)  table(factor(x, levels = 1:5, labels = aft_names_fromzero)))
colnames(fr_tb) = 2020:2029

pdf("output/AFTtable.pdf", width = 12, height = 12, onefile = T)

barplot(fr_tb, col = aft_cols, beside=T, ylim=c(0, 3E4))
legend("topright", legend = aft_names_fromzero, fill = aft_cols)
dev.off()


pdf("output/AFTmap.pdf", width = 12, height = 12, onefile = T)


for (tick in 1:nticks) { 
  par(mfrow=c(3,2))
  
  for (aft in 1:5) {
    col_tmp = ifelse(getValues(fr_returned[[tick]]==aft), col2rgb("red"),col2rgb("grey"))
    plot(fr_returned[[tick]]==aft, main = paste0("Tick=", tick, "(", aft_names_fromzero[aft], ")"), xlab = "lon", ylab = "lat", legend=F, col = col_tmp)
  }
  plot.new()
  # legend("topright", legend = aft_names_fromzero, fill = aft_cols)
  # rm(allregions_iter)
}
dev.off()

print("Close run")


# close the run
CRAFTY_jobj$EXTcloseRrun() # ignore the MPI initialisation error

# delete the java objects
rm(CRAFTY_RunInfo_jobj)
rm(CRAFTY_loader_jobj)
rm(CRAFTY_jobj)




