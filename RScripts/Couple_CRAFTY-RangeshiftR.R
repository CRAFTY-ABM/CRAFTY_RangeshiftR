
# date: 30/11/20
# author: VB
# description: script to loosely couple RangeshiftR and CRAFTY. RangeshiftR will 
# feed in two capitals (OPM presence & OPM inverted). CRAFTY will take these and
# run for a single timestep. The agent locations from CRAFTY will be used to alter
# OPM populations in RangeshiftR. These edited pops will start the next iteration of
# RangeshiftR, etc...

### libraries ------------------------------------------------------------------

library(rgdal)
library(raster)
library(RangeshiftR)
library(sf)
library(viridis)
library(ggplot2)
library(sp)
library(rJava)
library(jdx)
library(xml2)
library(foreach)

# directories/ file paths ------------------------------------------------------

dirWorking<- "~/eclipse-workspace/CRAFTY_RangeshiftR"

dirCRAFTYInput <- path.expand(paste0(dirWorking, "/data_LondonOPM/"))
dirCRAFTYOutput <- path.expand(paste0(dirWorking, "/output"))

# store RangeshiftR files within CRAFTY output folder as it is the directory CRAFTY will need to run in
dirRsftr <- file.path(dirCRAFTYOutput, 'RangeshiftR')
# specific file structure needed for RangeshiftR to run
dirRsftrInput <- file.path(dirRsftr,"Inputs")
dirRsftrOutput <- file.path(dirRsftr,"Outputs")
dirRsftrOutputMaps <- file.path(dirRsftr,"Output_Maps")
#dir.create(dirRsftrInput)
#dir.create(dirRsftrOutput)
#dir.create(dirRsftrOutputMaps)
dirRsftr <- file.path("C:/Users/vanessa.burton.sb/Documents/eclipse-workspace/CRAFTY_RangeshiftR/output/RangeshiftR/") # need to add the / for this path to work in RunRS

setwd(dirWorking)

source("RScripts/Functions_CRAFTY_rJava.R")


### RangeshiftR set-up ---------------------------------------------------------

rangeshiftrYears <- 2
rstHabitat <- raster(file.path(dirRsftrInput, 'Habitat-2m.tif'))
habitatRes <- 100

init <- Initialise(InitType=2, InitIndsFile='initial_inds_2014_n10.txt')

land <- ImportedLandscape(LandscapeFile=sprintf('Habitat-%sm.asc', habitatRes),
                          Resolution=habitatRes,
                          HabitatQuality=TRUE,
                          K=50) # carrying capacity (individuals per hectare) when habitat at 100% quality

demo <- Demography(Rmax = 25,
                   ReproductionType = 0) # 0 = asexual / only female; 1 = simple sexual; 2 = sexual model with explicit mating system

disp <-  Dispersal(Emigration = Emigration(EmigProb = 0.2),
                   Transfer   = DispersalKernel(Distances = 1500), # test getting to top of landscape while keeping other params low
                   Settlement = Settlement() )

# for storing rangeshiftR output data
dfRangeShiftrData <- data.frame()
outRasterStack <- stack()


### CRAFTY set-up --------------------------------------------------------------

# points for each cell to extract from OPM population results
hexPoints <- st_read(paste0(dirWorking,"/data-processed/hexGrids/hexPoints40m.shp"))
hexPointsSP <- as_Spatial(hexPoints)

# agent names
aft_names_fromzero = c("mgmt_highInt", "mgmt_lowInt", "mgmt_medInt", "no_mgmt")
aft_cols = viridis::viridis(4)

# for coordinate matching
london_xy_df <- read.csv(paste0(dirWorking, "/data-processed/Cell_ID_XY_Borough.csv"))
x_coords_v <- sort(unique(london_xy_df$X))
x_coords_bng_v <- london_xy_df[match(x_coords_v, london_xy_df$X), "x_coord"]
y_coords_v <- sort(unique(london_xy_df$Y))
y_coords_bng_v <- london_xy_df[match(y_coords_v, london_xy_df$Y), "y_coord"]
hx <- readOGR("data-processed/hexgrids", layer = "hexGrid40m")
proj4.BNG <- "+proj=tmerc +lat_0=49 +lon_0=-2 +k=0.9996012717 +x_0=400000 +y_0=-100000 +ellps=airy +towgs84=446.448,-125.157,542.06,0.15,0.247,0.842,-20.489 +units=m +no_defs"

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

parallelize <- FALSE # not loads of data so don't need to run in parallel

# change wd to the output folder to store output files
setwd(dirCRAFTYOutput) 

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



### CRAFTY first model loop ----------------------------------------------------

# clear objects when testing
rm(tick)
rm(CRAFTY_jobj)

print(paste0("============CRAFTY JAVA-R API: Create the instance"))

CRAFTY_jobj <- new(J(CRAFTY_main_name)) # Create a new instance (to call non-static methods)

# prepares a run and returns run information 
CRAFTY_RunInfo_jobj <- CRAFTY_jobj$EXTprepareRrun(CRAFTY_sargs)
print(paste0("============CRAFTY JAVA-R API: Run preparation done"))

# set the schedule
CRAFTY_loader_jobj <- CRAFTY_jobj$EXTsetSchedule(as.integer(start_year_idx), as.integer(end_year_idx))

# option to visualise as model runs
doProcessFR = FALSE

nticks <- length(start_year_idx:end_year_idx)
plot_return_list <- vector("list", nticks)
timesteps <- start_year_idx:end_year_idx



# crafty main loop
for (tick in timesteps) {
  
  tick <-timesteps[1]
  
  # before EXTtick()
  # run RangeshiftR to get first OPM capitals?
  
  # set up RangeShiftR for current iteration
  print(paste0("============CRAFTY JAVA-R API: Setting up RangeshiftR tick=", tick))
  # init file updates based on CRAFTY if after tick 1
  if (tick==1){
    init <- Initialise(InitType=2, InitIndsFile='initial_inds_2014_n10.txt')
  }else{
    init <- Initialise(InitType=2, InitIndsFile=sprintf('inds%s.txt', iteration))
  }
  sim <- Simulation(Simulation = tick,
                    Years = rangeshiftrYears,
                    Replicates = 1,
                    OutIntPop = 1,
                    OutIntInd = 1,
                    ReturnPopRaster=TRUE)
  s <- RSsim(simul = sim, land = land, demog = demo, dispersal = disp, init = init)
  validateRSparams(s)
  
  print(paste0("============CRAFTY JAVA-R API: Running RangeshiftR tick=", tick))
  # run RangeShiftR - use result to store output population raster.
  result <- RunRS(s, sprintf('%s/', dirRsftr))
  crs(result) <- crs(rstHabitat)
  extent(result) <- extent(rstHabitat)
  #plot(result[[rangeshiftrYears]])
  # store population raster in output stack.
  outRasterStack <- addLayer(outRasterStack, result[[rangeshiftrYears]])
  # store population data in output data frame.
  dfRange <- readRange(s, sprintf('%s/',dirRsftr))
  dfRange$iteration <- iteration
  dfRangeShiftrData <- rbind(dfRangeShiftrData, dfRange[1,])
  
  # extract the population raster to a shapefile of the individuals
  shpIndividuals <- rasterToPoints(result[[rangeshiftrYears]], fun=function(x){x > 0}, spatial=TRUE) %>% st_as_sf()
  shpIndividuals <- st_transform(shpIndividuals, crs(rstHabitat))
  shpIndividuals$id <- 1:nrow(shpIndividuals)
  
  # extract OPM population raster and use as OPM presence capital (+ OPM inverted capital)
  result2 <- result[[rangeshiftrYears]]
  hexPointsOPM <- raster::extract(result2, hexPointsSP)
  dfOPM <- cbind(hexPointsSP,hexPointsOPM) %>% as.data.frame()
  colnames(dfOPM)[2] <- "population"
  dfOPM$population[which(is.na(dfOPM$population))] <- 0
  
  # normalise and created inverted version
  # OPM presence
  data <- dfOPM$population
  data[which(data==0)]<-NA
  normalised <- (data-min(data,na.rm = T))/(max(data, na.rm = T)-min(data,na.rm=T))
  hist(data)
  hist(normalised)
  normalised[which(is.na(normalised))]<-0
  dfOPM$OPMpresence <- normalised
  
  # inverted OPM presence
  invert <- dfOPM$OPMpresence - 1
  z <- abs(invert)
  dfOPM$OPMinv <- z
  
  #check
  #dfOPM %>% dplyr::filter(population>0)
  
  # edit capitals file
  
  # read in capitals file
  capitals <- read.csv(paste0(dirCRAFTYInput,"worlds/LondonBoroughs/LondonBoroughs_XY.csv"))
  head(capitals)
  
  # read in look-up for joinID
  lookUp <- read.csv(paste0(dirWorking,"/data-processed/joinID_lookup.csv"))
  
  # update OPM capitals
  lookUp$OPMpresence <- dfOPM$population[match(lookUp$joinID, dfOPM$joinID)]
  capitals$joinID <- lookUp$joinID
  head(capitals)
  capitals$OPMpresence <- dfOPM$OPMpresence[match(capitals$joinID, dfOPM$joinID)]
  capitals$OPMinverted <- dfOPM$OPMinv[match(capitals$joinID, dfOPM$joinID)]
  
  # check
  #ggplot(capitals)+
  #geom_tile(mapping = aes(x,y,fill=OPMinverted))
  
  #head(capitals)
  capitals$joinID <- NULL
  # write to file. overwrite or write to new file? do both for now
  # if writing to new file, would need to change CRAFTY scenario file within loop to point to the correct version per tick
  write.csv(capitals, paste0(dirCRAFTYInput,"worlds/LondonBoroughs/LondonBoroughs_XY.csv"))
  write.csv(capitals, paste0(dirCRAFTYInput,"worlds/LondonBoroughs/LondonBoroughs_XY_tstep",tick,".csv"))
  
  
  #####
  #####
  nextTick = CRAFTY_jobj$EXTtick()
  #####
  #####
  
  stopifnot(nextTick == (tick + 1 )) # assertion
  
  
  # after EXTtick()
  # extract agent locations and use them to edit RangeshiftR individuals
  print(paste0("============CRAFTY JAVA-R API: Extract agent locations tick=", tick))
  # extract agent locations, match to hexagonal grid
  val_df <- t(sapply(allcells_l, FUN = function(c) c(c$getOwnersFrLabel()))) #, c$getEffectiveCapitals()$getAll(), c$getSupply()$getAll()
  val_fr <- val_df[1,]
  # find hexagons 
  hx_idx <-  match(cellid, hx$joinID )
  
  hx$fr <- NA
  # add FR to the hexagonal grid
  hx$fr[hx_idx] <- val_fr
  
  # now use to edit RangeshiftR individuals

  
  print(paste0("============CRAFTY JAVA-R API: Edit RangeshiftR individuals tick=", tick))
  # remove individuals based on management type
  # write new individuals file to be used by RangeshiftR on the next loop
  
  # could insert code for visualising here if wanted (lines 177-230 in CRAFTY_rJava_OPM)
  # (think it will be easier just to work with output csv files)
  

  if (nextTick <= end_year_idx) {
    print(paste0("============CRAFTY JAVA-R API: NextTick=", nextTick))
  } else {
    print(paste0("============CRAFTY JAVA-R API: Simulation done (tick=", tick, ")"))
    
  }
}
