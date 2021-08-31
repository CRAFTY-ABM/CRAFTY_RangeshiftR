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

### directories/ file paths ----------------------------------------------------

if (Sys.info()["user"] %in% c("alan", "seo-b")) { 
  dirWorking<- "~/git/CRAFTY_RangeshiftR"
  
} else { 
  dirWorking<- "~/eclipse-workspace/CRAFTY_RangeshiftR"
}


dirCRAFTYInput <- path.expand(paste0(dirWorking, "/data_LondonOPM/"))
dirCRAFTYOutput <- path.expand(paste0(dirWorking, "/output"))
setwd(dirWorking)

source("RScripts/Functions_CRAFTY_rJava.R")

### CRAFTY set-up --------------------------------------------------------------

# points for each cell to extract from OPM population results
hexPointsSP <- as_Spatial(hexPoints)

# agent names
aft_names_fromzero = c("mgmt_highInt", "mgmt_lowInt", "no_mgmt")
aft_cols = viridis::viridis(3)

# read in look-up for joinID
lookUp <- read.csv(paste0(dirWorking,"/data-processed/joinID_lookup.csv"))
# hexGrid for plotting/and cellIDs
hexGrid <- st_read(paste0(dirWorking,"/data-processed/hexgrids/hexGrid40m.shp"))
london_xy_df <- read.csv(paste0(dirWorking,"/data-processed/Cell_ID_XY_Borough.csv"))

# location of the CRAFTY Jar file
path_crafty_jar <- path.expand(paste0(dirWorking, "/lib/CRAFTY_KIT_engineOct2020.jar"))
# location of the CRAFTY lib files
path_crafty_libs <- path.expand(paste0(dirWorking, "/lib/"))
crafty_libs <- list.files(paste0(path_crafty_libs), pattern = "jar")
# make sure that in the classpath setting , gt-opengis-9.0.jar must be included before geoapi-20050403.jar. Otherwise it throws an uncatchable error during the giving up process: loading libraries without ordering them particularly, the opengis library is loaded after the geoapi library following alphabetical order.
# related commit - https://github.com/CRAFTY-ABM/CRAFTY_CoBRA/commit/4ce1041cae349572032fc7e25be49652781f5866
crafty_libs <- crafty_libs[crafty_libs != "geoapi-20050403.jar"  ] 
crafty_libs <- c(crafty_libs,  "geoapi-20050403.jar")

# name of the scenario file
scenario.filename <- "Scenario_Baseline_noGUI.xml" # no display

# java configuration
crafty_jclasspath <- c(path_crafty_jar, paste0(path_crafty_libs, crafty_libs))

# scenario file
CRAFTY_sargs <- c("-d", dirCRAFTYInput, "-f", scenario.filename, "-o", "99", "-r", "1",  "-n", "1", "-sr", "0") 

# CRAFTY timesteps
start_year_idx <- 1 # first year of the input data
end_year_idx <- 10 # 10th year of the input data 

# change wd to the output folder to store output files
setwd(dirCRAFTYOutput) 

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


### test set of capitals -------------------------------------------------------

# put OPM and knowledge everywhere - test for agent response
capitals <- read.csv(paste0(dirCRAFTYInput,"worlds/LondonBoroughs/LondonBoroughs_XY.csv"), header = T, sep = ",")
head(capitals)
#write.csv(capitals, paste0(dirCRAFTYInput,"worlds/LondonBoroughs/LondonBoroughs_XY.csv"),row.names = F)

capitals$OPMinverted <- 0 # OPM everywhere
capitals$knowledge <- 1 # knowledge everywhere
write.csv(capitals, paste0(dirCRAFTYInput,"worlds/LondonBoroughs/LondonBoroughs_XY.csv"),row.names = F)

updaterFiles <- capitals[1:8]
head(updaterFiles)

updaterFiles$OPMinverted <- 1
updaterFiles$knowledge <- 0
head(updaterFiles)
summary(updaterFiles)

ticks <- c(1,2,3,4,5,6,7,8,9,10)

for (i in ticks){
  
  #tick <- ticks[1]
  write.csv(updaterFiles, paste0(dirCRAFTYInput,"worlds/LondonBoroughs/LondonBoroughs_XY_tstep_",i,".csv") ,row.names = FALSE)
  
}


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
region = CRAFTY_loader_jobj$getRegions()$getAllRegions()$iterator()$'next'()

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

# check agents

dirResults <- paste0(dirCRAFTYOutput,"/output/")

# read in all results
dfResults <-
  list.files(path = dirResults,
             pattern = "*.csv", 
             full.names = T) %>% 
  grep("-Cell-", value=TRUE, .) %>% 
  #map_df(~read_csv(., col_types = cols(.default = "c")))
  map_df(~read.csv(.))

head(dfResults)
summary(dfResults)
dfResults$Tick <- factor(dfResults$Tick)
dfResults$Agent <- factor(dfResults$Agent)

# match back to hex grid -------------------------------------------------------

hexGrid <- st_read(paste0(dirWorking,"/data-processed/hexgrids/hexGrid40m.shp"))
london_xy_df <- read.csv(paste0(dirWorking,"/data-processed/Cell_ID_XY_Borough.csv"))
tick10 <- filter(dfResults, Tick==10)
val_xy <- data.frame(tick10$X,tick10$Y)
colnames(val_xy) <- c("X", "Y")
x_coord <- london_xy_df[match(val_xy$X, london_xy_df$X), "x_coord"]
y_coord <- london_xy_df[match(val_xy$Y, london_xy_df$Y), "y_coord"]

cellid <- foreach(rowid = 1:nrow(val_xy), .combine = "c") %do% { 
  which((as.numeric(val_xy[rowid, 1]) == london_xy_df$X) & (as.numeric(val_xy[rowid, 2]) == london_xy_df$Y))
}

tick10$joinID <- cellid
sfResult <- left_join(hexGrid, tick10, by="joinID")

# plot -------------------------------------------------------------------------

ggplot() +
  geom_sf(sfResult, mapping = aes(fill = Agent), col = NA)+
  scale_fill_brewer(palette="Dark2")
