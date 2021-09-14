
# date: 13/09/21
# authors: VB
# description: Test CRAFTY set-up standalone to make sure all ok running via R


### libraries ------------------------------------------------------------------

library(rgdal)
library(raster)
library(tidyverse)
library(sf)
library(viridis)
library(ggplot2)
library(sp)
library(rJava)
library(jdx)
library(xml2)
library(foreach)


### file paths -----------------------------------------------------------------

if (Sys.info()["user"] %in% c("alan", "seo-b")) { 
  dirWorking<- "~/git/CRAFTY_RangeshiftR2"
  
} else { 
  dirWorking<- "~/eclipse-workspace/CRAFTY_RangeshiftR"
}

dirFigs <- "~/OPM-model-prep-21-22/figs"

dirCRAFTYInput <- path.expand(paste0(dirWorking, "/data_LondonOPM/"))
dirCRAFTYOutput <- path.expand(paste0(dirWorking, "/output"))

setwd(dirWorking)

source("RScripts/02_Functions_CRAFTY_rJava.R")


### test set of capitals -------------------------------------------------------

# put OPM and knowledge everywhere - test for agent response
dfCapitals <- read.csv(paste0(dirCRAFTYInput,"worlds/GreaterLondon/with_social/GreaterLondon.csv"), header = T, sep = ",")
head(dfCapitals)
#write.csv(capitals, paste0(dirCRAFTYInput,"worlds/LondonBoroughs/LondonBoroughs_XY.csv"),row.names = F)

dfCapitals$OPM_presence <- 0 # OPM everywhere
dfCapitals$Knowledge <- 1 # knowledge everywhere
write.csv(dfCapitals, paste0(dirCRAFTYInput,"worlds/GreaterLondon/with_social/GreaterLondon.csv"),row.names = F)

updaterFiles <- dfCapitals[1:10]
head(updaterFiles)

#updaterFiles$OPMinverted <- 1
#updaterFiles$knowledge <- 0
head(updaterFiles)
summary(updaterFiles)

ticks <- c(1,2,3,4,5,6,7,8,9,10)

for (i in ticks){
  
  #tick <- ticks[1]
  write.csv(updaterFiles, paste0(dirCRAFTYInput,"worlds/GreaterLondon/with_social/GreaterLondon_tstep_",i,".csv") ,row.names = FALSE)
  
}

### set up CRAFTY --------------------------------------------------------------

# points file for Greater London 100m grid - to extract from OPM population results
sfPoints <- st_read(paste0(dirWorking,"/data-store/01_Grid_points.shp"))

# convert to spatial
spPoints <- as_Spatial(sfPoints)

# agent names
aft_names_fromzero <- c("no_mgmt", "mgmt_remove", "mgmt_pesticide", "mgmt_fell")

# read in look-up for GridID & coords
lookUp <- read.csv(paste0(dirWorking,"/data-store/Cell_ID_XY_GreaterLondon.csv"))
# sf polygon grid for plotting if required
sfGrid <- st_read(paste0(dirWorking,"/data-store/01_Grid_capitals_raw.shp"))
sfGrid <- sfGrid %>% dplyr::select(GridID, geometry)


# location of the CRAFTY Jar file
path_crafty_jar <- path.expand(paste0(dirWorking, "/lib/CRAFTY_KIT_engineOct2020.jar"))
#path_crafty_jar <- path.expand(paste0(dirWorking, "/lib/CRAFTY_KIT_engineAug2021.jar"))

# location of the CRAFTY lib files
path_crafty_libs <- path.expand(paste0(dirWorking, "/lib/"))
crafty_libs <- list.files(paste0(path_crafty_libs), pattern = "jar")

# make sure that in the classpath setting , gt-opengis-9.0.jar must be included before geoapi-20050403.jar. Otherwise it throws an uncatchable error during the giving up process: loading libraries without ordering them particularly, the opengis library is loaded after the geoapi library following alphabetical order.
# related commit - https://github.com/CRAFTY-ABM/CRAFTY_CoBRA/commit/4ce1041cae349572032fc7e25be49652781f5866
crafty_libs <- crafty_libs[crafty_libs != "geoapi-20050403.jar"  ] 
crafty_libs <- c(crafty_libs,  "geoapi-20050403.jar")

# java configuration
crafty_jclasspath <- c(path_crafty_jar, paste0(path_crafty_libs, crafty_libs))

# Random seed used in CRAFTY
random_seed_crafty <- 99 

# name of the scenario file
scenario.filename <- "Scenario_with_social_GUI.xml"

# scenario file
CRAFTY_sargs <- c("-d", dirCRAFTYInput, "-f", scenario.filename, "-o", "99", "-r", "1",  "-n", "1", "-sr", "0") 

# CRAFTY timesteps
start_year_idx <- 1 # first year of the input data
end_year_idx <- 10 # 10th year of the input data 

parallelize <- FALSE # not loads of data so don't need to run in parallel

# if getting random Java errors, restart Rstudio
# initialise Java
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

# set the batch run folder (dirCRAFTYOutput)
.jcall( 'java/lang/System', 'S', 'setProperty', 'user.dir',  dirCRAFTYOutput )

# assertion
stopifnot(dirCRAFTYOutput == .jcall( 'java/lang/System', 'S', 'getProperty', 'user.dir' ))



### Set up CRAFTY job ----------------------------------------------------------

# Create a new instance (to call non-static methods)
CRAFTY_jobj <- new(J(CRAFTY_main_name)) 

# prepares a run and returns run information 
CRAFTY_RunInfo_jobj <- CRAFTY_jobj$EXTprepareRrun(CRAFTY_sargs)

# set the schedule
CRAFTY_loader_jobj <- CRAFTY_jobj$EXTsetSchedule(as.integer(start_year_idx), as.integer(end_year_idx))

# option to visualise as model runs
#doProcessFR = FALSE

nticks <- length(start_year_idx:end_year_idx)
timesteps <- start_year_idx:end_year_idx

### pre-process CRAFTY Java object
region <- CRAFTY_loader_jobj$getRegions()$getAllRegions()$iterator()$'next'()

for (CRAFTY_tick in timesteps) {
  
  CRAFTY_nextTick = CRAFTY_jobj$EXTtick()
  
  stopifnot(CRAFTY_nextTick == (CRAFTY_tick + 1 )) # assertion
  print(paste0("============CRAFTY JAVA-R API: CRAFTY run complete = ", CRAFTY_tick))
  
  if (CRAFTY_nextTick <= end_year_idx) {
    print(paste0("============CRAFTY JAVA-R API: NextTick=", CRAFTY_nextTick))
  } else {
    print(paste0("============CRAFTY JAVA-R API: Simulation done (tick=", CRAFTY_tick, ")"))
  }
}
