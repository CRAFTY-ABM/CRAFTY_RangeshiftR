
# date: 20/01/21
# authors: VB/BS
# description: script to loosely couple RangeShiftR and CRAFTY. RangeShiftR will 
# feed in two capitals (OPM inverted & knowledge). CRAFTY will take these and
# run for a single timestep. The agent locations from CRAFTY will be used to alter
# OPM populations in RangeShiftR. These edited pops will start the next iteration of
# RangeshiftR, etc...



### libraries ------------------------------------------------------------------

library(rgdal)
library(raster)
library(tidyverse)

#if (!require(RangeShiftR)) { 
  # Install RangeShiftR from GitHub:
  #devtools::install_github("RangeShifter/RangeShiftR-package", ref="main")
  #library(RangeShiftR)
#} else {}

library(RangeShiftR)
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

# store RangeshiftR files within CRAFTY output folder as it is the directory CRAFTY will need to run in
dirRsftr <- file.path(dirCRAFTYOutput, 'RangeshiftR')
# specific file structure needed for RangeshiftR to run
dirRsftrInput <- file.path(dirRsftr,"Inputs")
dirRsftrOutput <- file.path(dirRsftr,"Outputs")
dirRsftrOutputMaps <- file.path(dirRsftr,"Output_Maps")
#dir.create(dirRsftrInput)
#dir.create(dirRsftrOutput)
#dir.create(dirRsftrOutputMaps)

# important
# need to add the / for this path to work in RunRS()
dirRsftr <- paste0(dirRsftr,"/") 

setwd(dirWorking)

source("RScripts/Functions_CRAFTY_rJava.R")



### RangeshiftR set-up ---------------------------------------------------------

rangeshiftrYears <- 2
rstHabitat <- raster(file.path(dirRsftrInput, 'Habitat-100m.tif'))
# make sure BNG
hexPoints <- st_read(paste0(dirWorking,"/data-processed/hexgrids/hexPoints40m.shp"))
rstHabitat <- projectRaster(rstHabitat, crs = crs(hexPoints))
st_crs(rstHabitat)
habitatRes <- 100

init <- Initialise(InitType=2, InitIndsFile='initial_inds_2014_n10.txt')

land <- ImportedLandscape(LandscapeFile=sprintf('Habitat-%sm.asc', habitatRes),
                          Resolution=habitatRes,
                          HabPercent=TRUE,
                          K_or_DensDep=50) # carrying capacity (individuals per hectare) when habitat at 100% quality

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
hexPointsSP <- as_Spatial(hexPoints)

# agent names
aft_names_fromzero = c("mgmt_highInt", "mgmt_lowInt", "no_mgmt")
aft_cols = viridis::viridis(3)

# read in look-up for joinID
lookUp <- read.csv(paste0(dirWorking,"/data-processed/joinID_lookup.csv"))
# hexGrid for plotting/and cellIDs
hexGrid <- st_read(paste0(dirWorking,"/data-processed/hexgrids/hexGrid40m.shp"))
london_xy_df <- read.csv(paste0(dirWorking,"/data-processed/Cell_ID_XY_Borough.csv"))

#proj4.BNG <- "+proj=tmerc +lat_0=49 +lon_0=-2 +k=0.9996012717 +x_0=400000 +y_0=-100000 +ellps=airy +towgs84=446.448,-125.157,542.06,0.15,0.247,0.842,-20.489 +units=m +no_defs"

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


# Random seed used in the crafty 
random_seed_crafty = 99 

# scenario file
CRAFTY_sargs <- c("-d", dirCRAFTYInput, "-f", scenario.filename, "-o", random_seed_crafty, "-r", "1",  "-n", "1", "-sr", "0") 

# CRAFTY timesteps
start_year_idx <- 1 # first year of the input data
end_year_idx <- 10 # 10th year of the input data 

parallelize <- FALSE # not loads of data so don't need to run in parallel

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



### Set up CRAFTY job ----------------------------------------------------------

# clear objects when testing
#rm(CRAFTY_tick)
#rm(CRAFTY_jobj)

# Create a new instance (to call non-static methods)
CRAFTY_jobj <- new(J(CRAFTY_main_name)) 

# prepares a run and returns run information 
CRAFTY_RunInfo_jobj <- CRAFTY_jobj$EXTprepareRrun(CRAFTY_sargs)

# set the schedule
CRAFTY_loader_jobj <- CRAFTY_jobj$EXTsetSchedule(as.integer(start_year_idx), as.integer(end_year_idx))

# option to visualise as model runs
#doProcessFR = FALSE

nticks <- length(start_year_idx:end_year_idx)
plot_return_list <- vector("list", nticks)
timesteps <- start_year_idx:end_year_idx

### pre-process CRAFTY Java object
region = CRAFTY_loader_jobj$getRegions()$getAllRegions()$iterator()$'next'()



### Run the models -------------------------------------------------------------

CRAFTY_tick <- 1
#RR_iteration <- 1

for (CRAFTY_tick in timesteps) {
  
  # before EXTtick()
  # run RangeshiftR to get OPM capital
  
  print(paste0("============CRAFTY JAVA-R API: Setting up RangeShiftR tick = ", CRAFTY_tick))
  # set up RangeShiftR for current iteration
  # init file updates based on CRAFTY if after tick 1
  if (CRAFTY_tick==1){
    init <- Initialise(InitType=2, InitIndsFile='initial_inds_2014_n10.txt')
  }else{
    init <- Initialise(InitType=2, InitIndsFile=sprintf('inds_tick_%s.txt', CRAFTY_tick-1))
  }
  sim <- Simulation(Simulation = CRAFTY_tick,
                    Years = rangeshiftrYears,
                    Replicates = 1,
                    OutIntPop = 1,
                    OutIntInd = 1,
                    ReturnPopRaster=TRUE)
  s <- RSsim(simul = sim, land = land, demog = demo, dispersal = disp, init = init)
  stopifnot(validateRSparams(s)==TRUE) 
  
  print(paste0("============CRAFTY JAVA-R API: Running RangeShiftR tick = ", CRAFTY_tick))
  # run RangeShiftR - use result to store output population raster.
  result <- RunRS(s, sprintf('%s', dirRsftr))
  crs(result) <- crs(rstHabitat)
  extent(result) <- extent(rstHabitat)
  print(paste0("============CRAFTY JAVA-R API: Show RangeShiftR result tick = ", CRAFTY_tick))
  print(plot(result[[rangeshiftrYears]]))
  # store population raster in output stack.
  outRasterStack <- addLayer(outRasterStack, result[[rangeshiftrYears]])
  # store population data in output data frame.
  dfRange <- readRange(s, sprintf('%s/',dirRsftr))
  dfRange$timestep <- CRAFTY_tick
  dfRangeShiftrData <- rbind(dfRangeShiftrData, dfRange[1,])
  
  print(paste0("============CRAFTY JAVA-R API: Extract RangeShiftR population results = ", CRAFTY_tick))
  # extract the population raster to a shapefile of the individuals
  shpIndividuals <- rasterToPoints(result[[rangeshiftrYears]], fun=function(x){x > 0}, spatial=TRUE) %>% st_as_sf()
  shpIndividuals <- shpIndividuals %>% st_set_crs(st_crs(rstHabitat))
  shpIndividuals$id <- 1:nrow(shpIndividuals)
  # extract OPM population raster and use as OPM capital
  result2 <- result[[rangeshiftrYears]]
  hexPointsOPM <- raster::extract(result2, hexPointsSP)
  dfOPM <- cbind(hexPointsSP,hexPointsOPM) %>% as.data.frame()
  colnames(dfOPM)[2] <- "population"
  dfOPM$population[which(is.na(dfOPM$population))] <- 0
  
  print(paste0("============CRAFTY JAVA-R API: Convert RangeShiftR population results to binary capital = ", CRAFTY_tick))
  # make binary version and invert 
  OPMbinary <- dfOPM$population
  OPMbinary[which(OPMbinary>0)] <- 1
  invert <- OPMbinary - 1
  OPMinv <- abs(invert)
  dfOPMinv <- tibble(dfOPM$joinID,OPMinv)
  colnames(dfOPMinv)[1] <- "joinID"
  
  # update OPM inverted capital in updater files
  capitals <- read.csv(paste0(dirCRAFTYInput,"worlds/LondonBoroughs/LondonBoroughs_XY_tstep_",CRAFTY_tick,".csv"))
  # update OPM capital using lookUp
  lookUp$OPMinverted <- dfOPMinv$OPMinv[match(lookUp$joinID, dfOPMinv$joinID)]
  capitals$joinID <- lookUp$joinID
  capitals$OPMinverted <- dfOPMinv$OPMinv[match(capitals$joinID, dfOPM$joinID)]
  # check
  p2 <- ggplot(capitals)+
    geom_tile(mapping = aes(x,y,fill=OPMinverted))
  print(p2)
  
  # update knowledge to be dependent on OPM presence
  if (CRAFTY_tick==1){
    capitals$knowledge<-NA # clear previous test capital
    # and add any new knowledge based on contact with OPM
    capitals$knowledge[which(capitals$OPMinverted==0)]<-1
    capitals$knowledge[which(capitals$OPMinverted==1)]<-0
    }else{
      # keep previous knowledge
      prevKnowledge <- read.csv(paste0(dirCRAFTYInput,"worlds/LondonBoroughs/LondonBoroughs_XY_tstep_",CRAFTY_tick-1,".csv"))
      capitals$knowledge <- prevKnowledge$knowledge
      # add new
      capitals$knowledge[which(capitals$OPMinverted==0)]<-1
    }
  
  p3 <- ggplot(capitals)+
    geom_tile(mapping = aes(x,y,fill=knowledge))
  print(p3)
  capitals$joinID <- NULL
  
  capitals <- write.csv(capitals, paste0(dirCRAFTYInput,"worlds/LondonBoroughs/LondonBoroughs_XY_tstep_",CRAFTY_tick,".csv"),row.names = F)
  
  #####
  #####
  print(paste0("============CRAFTY JAVA-R API: Running CRAFTY tick = ", CRAFTY_tick))
  CRAFTY_nextTick = CRAFTY_jobj$EXTtick()
  #####
  #####
  
  stopifnot(CRAFTY_nextTick == (CRAFTY_tick + 1 )) # assertion
  print(paste0("============CRAFTY JAVA-R API: CRAFTY run complete = ", CRAFTY_tick))
  
  # after EXTtick()
  # extract agent locations and use them to edit RangeshiftR individuals
  print(paste0("============CRAFTY JAVA-R API: Extract agent locations tick = ", CRAFTY_tick))
  
  # extract agent locations, match to hexagonal grid
  val_df <- read.csv(paste0(dirCRAFTYOutput,"/output/Baseline-0-", random_seed_crafty+1,"-LondonBoroughs-Cell-",CRAFTY_tick,".csv"))
  val_fr <- val_df[,"Agent"]
  val_fr_fac <- factor(val_fr,  labels = aft_names_fromzero, levels = aft_names_fromzero)
  
  # match back to hexGrid using joinID/cellid
  val_xy <- data.frame(val_df$X,val_df$Y)
  colnames(val_xy) <- c("X", "Y")
  x_coord <- london_xy_df[match(val_xy$X, london_xy_df$X), "x_coord"]
  y_coord <- london_xy_df[match(val_xy$Y, london_xy_df$Y), "y_coord"]
  
  cellid <- foreach(rowid = 1:nrow(val_xy), .combine = "c") %do% { 
    which((as.numeric(val_xy[rowid, 1]) == london_xy_df$X) & (as.numeric(val_xy[rowid, 2]) == london_xy_df$Y))
  }
  
  val_df$joinID <- cellid
  sfResult <- left_join(hexGrid, val_df, by="joinID")
  sfResult$Agent <- factor(sfResult$Agent, levels=aft_names_fromzero)
  
  print(paste0("============CRAFTY JAVA-R API: Show agents & OPM individuals = ", CRAFTY_tick)) 
  p1 <- ggplot() +
    geom_sf(sfResult, mapping = aes(fill = Agent), col = NA)+
    geom_sf(data=shpIndividuals, color="black", pch=4)+
    scale_fill_brewer(palette="Dark2")
  print(p1)
  
  # now use to edit RangeshiftR individuals
  print(paste0("============CRAFTY JAVA-R API: Edit RangeshiftR individuals tick = ", CRAFTY_tick))

  # find where OPM individuals intersect
  lowInt <- sfResult %>% filter(Agent == "mgmt_lowInt")
  #lowInt <- sfResult %>% filter(borough == "hammersmith") # use to test as no mgmt agents atm
  highInt <- sfResult %>% filter(Agent == "mgmt_highInt")

  # find OPM individuals within each agent type
  # https://gis.stackexchange.com/questions/245136/how-to-subset-point-data-by-outside-of-polygon-data-in-r
  low <- sapply(st_intersects(shpIndividuals, lowInt),function(x){length(x)>0})
  high <- sapply(st_intersects(shpIndividuals, highInt),function(x){length(x)>0})
  
  # check
  #ggplot() +
    #geom_sf(sfResult, mapping = aes(fill = Agent), col = NA)+
    #geom_sf(data=shpIndividuals[!low,])+
    #scale_fill_brewer(palette="Dark2")

  # edit OPM populations based on management type
  
  # reduce population by half if low intensity
  lowPops <- shpIndividuals$rep0_year1[low]
  if (length(lowPops)>1){
    for (pop in c(1:length(lowPops))){
    lowPops[pop]<-lowPops[pop]/2
    if (lowPops[pop]<1){
      lowPops[pop] <- 0}
    }
  }
  shpIndividuals$rep0_year1[low] <- lowPops
  
  # remove if high intensity
  shpIndividuals <- shpIndividuals[!high,] 
  
  
  print(paste0("============CRAFTY JAVA-R API: Write new individuals file for RangeShiftR = ", CRAFTY_tick))

  # write new individuals file to be used by RangeShiftR on the next loop
  shpIndividuals <- shpIndividuals %>% as_Spatial()
  dfNewIndsTable <- raster::extract(rasterize(shpIndividuals, rstHabitat, field=sprintf('rep0_year%s', rangeshiftrYears-1)), shpIndividuals, cellnumbers=T, df=TRUE)
  dfNewIndsTable$Year <- 0 # CRAFTY_tick - 1
  dfNewIndsTable$Species <- 0
  dfNewIndsTable$X <- dfNewIndsTable$cells %% ncol(rstHabitat)
  dfNewIndsTable$Y <- nrow(rstHabitat) - (floor(dfNewIndsTable$cells / ncol(rstHabitat)))
  dfNewIndsTable$Ninds <- dfNewIndsTable$layer
  dfNewIndsTable <- dfNewIndsTable[ , !(names(dfNewIndsTable) %in% c('ID', 'cells', 'layer'))]
  dfNewIndsTable <- dfNewIndsTable[!is.na(dfNewIndsTable$Ninds),]
  # join to previous individuals file?
  # trying this to stop populations dying out... but don't think this is correct as it will undo any management changes made based on CRAFTY...
  if (CRAFTY_tick==1){
    initIndsTable <- read.table(file.path(dirRsftrInput, "initial_inds_2014_n10.txt"), header = T)
  }else{
    initIndsTable <- read.table(file.path(dirRsftrInput, sprintf('inds_tick_%s.txt', CRAFTY_tick-1)), header = T)
  }
  dfNewIndsTable <- rbind(initIndsTable,dfNewIndsTable)
  # make sure individuals aren't being counted more than once in the same location
  dfNewIndsTable <- unique(dfNewIndsTable)
  # where Ninds = 1, set to 10. Otherwise populations die out
  # they don't die out in RangeshiftR standalone run (which uses the same init file with Ninds set to 10 for entire simulation)
  dfNewIndsTable$Ninds[which(dfNewIndsTable$Ninds==1)] <- 10
  
  write.table(dfNewIndsTable, file.path(dirRsftrInput, sprintf('inds_tick_%s.txt', CRAFTY_tick)),row.names = F, quote = F, sep = '\t')
  
  # set init file for next tick
  #init <- Initialise(InitType=2, InitIndsFile=sprintf('inds_tick_%s.txt', CRAFTY_tick))
  
  
  if (CRAFTY_nextTick <= end_year_idx) {
    print(paste0("============CRAFTY JAVA-R API: NextTick=", CRAFTY_nextTick))
  } else {
    print(paste0("============CRAFTY JAVA-R API: Simulation done (tick=", CRAFTY_tick, ")"))
    
  }
  
}

warnings()
spplot(outRasterStack)

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

# match back to hex grid 
tick <- filter(dfResults, Tick==10)
val_xy <- data.frame(tick$X,tick$Y)
colnames(val_xy) <- c("X", "Y")
x_coord <- london_xy_df[match(val_xy$X, london_xy_df$X), "x_coord"]
y_coord <- london_xy_df[match(val_xy$Y, london_xy_df$Y), "y_coord"]

cellid <- foreach(rowid = 1:nrow(val_xy), .combine = "c") %do% { 
  which((as.numeric(val_xy[rowid, 1]) == london_xy_df$X) & (as.numeric(val_xy[rowid, 2]) == london_xy_df$Y))
}

tick$joinID <- cellid
sfResult <- left_join(hexGrid, tick, by="joinID")

# plot 
ggplot() +
  geom_sf(sfResult, mapping = aes(fill = Agent), col = NA)+
  scale_fill_brewer(palette="Dark2")
