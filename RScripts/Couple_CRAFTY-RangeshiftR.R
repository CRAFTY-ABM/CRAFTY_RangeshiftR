
# date: 26/01/21
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
library(tictoc)


### directories/ file paths ----------------------------------------------------

if (Sys.info()["user"] %in% c("alan", "seo-b")) { 
  dirWorking<- "~/git/CRAFTY_RangeshiftR2"
  
} else { 
  dirWorking<- "~/eclipse-workspace/CRAFTY_RangeshiftR"
}

dirFigs <- "~/CRAFTY-opm/figures"

dirCRAFTYInput <- path.expand(paste0(dirWorking, "/data_LondonOPM/"))
dirCRAFTYOutput <- path.expand(paste0(dirWorking, "/output"))

# this will change per scenario (store a RangeshiftR folder within each scenario output folder)
# store RangeshiftR files within CRAFTY output folder as it is the directory CRAFTY will need to run in
dirRsftr <- file.path(dirCRAFTYOutput, 'RangeshiftR_coupled')
# specific file structure needed for RangeshiftR to run
dirRsftrInput <- file.path(dirRsftr,"Inputs")
dirRsftrOutput <- file.path(dirRsftr,"Outputs")
dirRsftrOutputMaps <- file.path(dirRsftr,"Output_Maps")
#dir.create(dirRsftrInput)
#dir.create(dirRsftrOutput)
#dir.create(dirRsftrOutputMaps)

# important
# need to add the / for this path to work in RunRS()
#dirRsftr <- paste0(dirRsftr,"/") 

setwd(dirWorking)

source("RScripts/Functions_CRAFTY_rJava.R")


### RangeshiftR set-up ---------------------------------------------------------

rangeshiftrYears <- 2
#rstHabitat <- raster(file.path(dirRsftrInput, 'Habitat-100m.tif'))
ascHabitat <- raster(file.path(dirRsftrInput, 'Habitat-100m.asc'))
# make sure BNG
hexPoints <- st_read(paste0(dirWorking,"/data-processed/hexgrids/hexPoints40m.shp"))
#rstHabitat <- projectRaster(rstHabitat, crs = crs(hexPoints))
#st_crs(rstHabitat)
crs(ascHabitat) <- crs(hexPoints)
st_crs(ascHabitat)
spplot(ascHabitat)
habitatRes <- 100


### CRAFTY set-up --------------------------------------------------------------

# points for each cell to extract from OPM population results
hexPointsSP <- as_Spatial(hexPoints)

# agent names
aft_names_fromzero = c("mgmt_highInt", "mgmt_lowInt", "no_mgmt")
##aft_cols = viridis::viridis(3)

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
#scenario.filename <- "Scenario_Baseline_noGUI.xml" # no display

# java configuration
crafty_jclasspath <- c(path_crafty_jar, paste0(path_crafty_libs, crafty_libs))

# Random seed used in CRAFTY
random_seed_crafty <- 99 

# scenario file
#CRAFTY_sargs <- c("-d", dirCRAFTYInput, "-f", scenario.filename, "-o", random_seed_crafty, "-r", "1",  "-n", "1", "-sr", "0") 

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
#.jcall( 'java/lang/System', 'S', 'setProperty', 'user.dir',  dirCRAFTYOutput )

# assertion
#stopifnot(dirCRAFTYOutput == .jcall( 'java/lang/System', 'S', 'getProperty', 'user.dir' ))



### Set up CRAFTY job ----------------------------------------------------------

# moved to within scenario loop

# Create a new instance (to call non-static methods)
#CRAFTY_jobj <- new(J(CRAFTY_main_name)) 

# prepares a run and returns run information 
#CRAFTY_RunInfo_jobj <- CRAFTY_jobj$EXTprepareRrun(CRAFTY_sargs)

# set the schedule
#CRAFTY_loader_jobj <- CRAFTY_jobj$EXTsetSchedule(as.integer(start_year_idx), as.integer(end_year_idx))

# option to visualise as model runs
#doProcessFR = FALSE

#nticks <- length(start_year_idx:end_year_idx)
#plot_return_list <- vector("list", nticks)
#timesteps <- start_year_idx:end_year_idx

### pre-process CRAFTY Java object
#region = CRAFTY_loader_jobj$getRegions()$getAllRegions()$iterator()$'next'()



### Run the models -------------------------------------------------------------

# start setting up structure for scenarios

scenario.filenames <- c("Scenario_Baseline_noGUI.xml", "Scenario_de-regulation_noGUI.xml","Scenario_govt-intervention_noGUI.xml") 

for (scenario in scenario.filenames){
  
  #scenario <- scenario.filenames[3]
  scenario.filename <- scenario
  scenario.split <- strsplit(scenario, "[_]")[[1]][2]
 
  # scenario file
  CRAFTY_sargs <- c("-d", dirCRAFTYInput, "-f", scenario.filename, "-o", random_seed_crafty, "-r", "1",  "-n", "1", "-sr", "0") 
  
  # set up CRAFTY job
  # Create a new instance (to call non-static methods)
  CRAFTY_jobj <- new(J(CRAFTY_main_name)) 
  
  # prepares a run and returns run information 
  CRAFTY_RunInfo_jobj <- CRAFTY_jobj$EXTprepareRrun(CRAFTY_sargs)
  
  # set the schedule
  CRAFTY_loader_jobj <- CRAFTY_jobj$EXTsetSchedule(as.integer(start_year_idx), as.integer(end_year_idx))
  
  timesteps <- start_year_idx:end_year_idx
  
  ### pre-process CRAFTY Java object
  region <- CRAFTY_loader_jobj$getRegions()$getAllRegions()$iterator()$'next'()
  
  # change wd to a scenario folder to store output files
  dirCRAFTYscenario <- paste0(dirCRAFTYOutput,"/V4/",scenario.split)
  
  # set RangeshiftR paths based on scenario
  dirRsftr <- file.path(dirCRAFTYscenario)
 
  # specific file structure needed for RangeshiftR to run
  dirRsftrInput <- file.path(dirRsftr,"Inputs")
  dirRsftrOutput <- file.path(dirRsftr,"Outputs")
  dirRsftrOutputMaps <- file.path(dirRsftr,"Output_Maps")
  # important - need to add the / for this path to work in RunRS()
  dirRsftr <- paste0(dirRsftr,"/") 
  
  # set-up RangeshiftR parameters for this scenario
  land <- ImportedLandscape(LandscapeFile=sprintf('Habitat-%sm.asc', habitatRes),
                            Resolution=habitatRes,
                            HabPercent=TRUE,
                            K_or_DensDep=50) # carrying capacity (individuals per hectare) when habitat at 100% quality
  
  demo <- Demography(Rmax = 25,
                     ReproductionType = 0) # 0 = asexual / only female; 1 = simple sexual; 2 = sexual model with explicit mating system
  
  disp <-  Dispersal(Emigration = Emigration(EmigProb = 0.2),
                     Transfer   = DispersalKernel(Distances = 800), # test getting to top of landscape while keeping other params low
                     Settlement = Settlement() )
  
  # for storing RangeshiftR output data
  dfRangeShiftrData <- data.frame()
  outRasterStack <- stack()
  
  # set the batch run folder (dirCRAFTYOutput)
  .jcall( 'java/lang/System', 'S', 'setProperty', 'user.dir',  dirCRAFTYOutput)
  
  # assertion
  stopifnot(dirCRAFTYOutput == .jcall( 'java/lang/System', 'S', 'getProperty', 'user.dir' ))
  
  # loop through years for this scenario 
  
  for (CRAFTY_tick in timesteps) {
    
    #CRAFTY_tick <- 1
    
    # before EXTtick() (line 380)
    # run RangeshiftR to get OPM capital
    
    tic(CRAFTY_tick)
    
    print(paste0("============CRAFTY JAVA-R API: Setting up RangeShiftR tick = ", CRAFTY_tick))
    
    # set up RangeShiftR for current iteration
    # init file updates based on CRAFTY if after tick 1
    if (CRAFTY_tick==1){
      init <- Initialise(InitType=2, InitIndsFile='initial_inds_2014_n10.txt')
    }else{
      init <- Initialise(InitType=2, InitIndsFile=sprintf('inds_tick_%s.txt', CRAFTY_tick-1))
    }
    
    # run RangeShiftR for 2-years per CRAFTY_tick and extract mean of 10 reps as result
    sim <- Simulation(Simulation = CRAFTY_tick,
                      Years = rangeshiftrYears,
                      Replicates = 10,
                      OutIntPop = 1,
                      OutIntInd = 1,
                      ReturnPopRaster=TRUE)
    
    # set up simulation
    s <- RSsim(simul = sim, land = land, demog = demo, dispersal = disp, init = init, seed = 261090) # set seed to enable replication
    
    # stop if set up incorrectly
    stopifnot(validateRSparams(s)==TRUE) 
    
    print(paste0("============CRAFTY JAVA-R API: Running RangeShiftR tick = ", CRAFTY_tick))
    
    # run RangeShiftR - use result to store output population raster.
    result <- RunRS(s, sprintf('%s', dirRsftr))
    
    # wait few seconds before reading the output
    Sys.sleep(1)
    
    # set crs and extent
    crs(result) <- crs(ascHabitat)
    extent(result) <- extent(ascHabitat)
    
    #names(result)
    
    # calculate average of 10 reps for current timestep
    idx <- grep("year1", names(result)) # this selects the second years data
    resultMean <- mean(result[[idx]])
    print(spplot(resultMean))
    
    # store population raster in output stack.
    #outRasterStack <- addLayer(outRasterStack, result[[rangeshiftrYears]])
    outRasterStack <- addLayer(outRasterStack, resultMean)
    
    # store population data in output data frame.
    dfRange <- readRange(s, sprintf('%s',dirRsftr))
    dfRange$timestep <- CRAFTY_tick
    dfRangeShiftrData <- rbind(dfRangeShiftrData, dfRange[,])
    
    print(paste0("============CRAFTY JAVA-R API: Extract RangeShiftR population results = ", CRAFTY_tick))
    
    # extract the population raster to a shapefile of the individuals
    #shpIndividuals <- rasterToPoints(result[[rangeshiftrYears]], fun=function(x){x > 0}, spatial=TRUE) %>% st_as_sf()
    shpIndividuals <- rasterToPoints(resultMean, fun=function(x){x > 0}, spatial=TRUE) %>% st_as_sf()
    shpIndividuals <- shpIndividuals %>% st_set_crs(st_crs(ascHabitat))
    shpIndividuals$id <- 1:nrow(shpIndividuals)
    shpIndividuals$layer <- ceiling(shpIndividuals$layer)
    
    # extract OPM population raster and use as OPM capital
    #result2 <- result[[rangeshiftrYears]]
    #hexPointsOPM <- raster::extract(result2, hexPointsSP)
    hexPointsOPM <- raster::extract(resultMean, hexPointsSP)
    dfOPM <- cbind(hexPointsSP,hexPointsOPM) %>% as.data.frame()
    colnames(dfOPM)[2] <- "population"
    dfOPM$population[which(is.na(dfOPM$population))] <- 0
    
    print(paste0("============CRAFTY JAVA-R API: Convert RangeShiftR population results to binary capital = ", CRAFTY_tick))
    
    # make binary version and invert for CRAFTY
    OPMbinary <- dfOPM$population
    OPMbinary[which(OPMbinary>0)] <- 1
    invert <- OPMbinary - 1
    OPMinv <- abs(invert)
    dfOPMinv <- tibble(dfOPM$joinID,OPMinv)
    colnames(dfOPMinv)[1] <- "joinID"
    
    # update OPM inverted capital in updater files
    capitals <- read.csv(paste0(dirCRAFTYInput,"worlds/LondonBoroughs/",scenario.split,"/LondonBoroughs_XY_tstep_",CRAFTY_tick,".csv"))
    # update OPM capital using lookUp
    lookUp$OPMinverted <- dfOPMinv$OPMinv[match(lookUp$joinID, dfOPMinv$joinID)]
    capitals$joinID <- lookUp$joinID
    capitals$OPMinverted <- dfOPMinv$OPMinv[match(capitals$joinID, dfOPM$joinID)]
    # check
    p2 <- ggplot(capitals)+
      geom_tile(mapping = aes(x,y,fill=OPMinverted))
    print(p2)
    
    # update knowledge to be dependent on OPM presence
    # for de-regulation scenario, there is no monitoring, so way minimal knowledge (0.2 instead of 1)
    if (scenario.split == "de-regulation"){
      if (CRAFTY_tick==1){
        capitals$knowledge<-NA # clear previous test capital
        # and add any new knowledge based on contact with OPM
        capitals$knowledge[which(capitals$OPMinverted==0)]<-0.2
        capitals$knowledge[which(capitals$OPMinverted==1)]<-0
      }else{
        # keep previous knowledge
        prevKnowledge <- read.csv(paste0(dirCRAFTYInput,"worlds/LondonBoroughs/",scenario.split,"/LondonBoroughs_XY_tstep_",CRAFTY_tick-1,".csv"))
        capitals$knowledge <- prevKnowledge$knowledge
        # add new
        capitals$knowledge[which(capitals$OPMinverted==0)]<-0.2
      }
    }else{
      if (CRAFTY_tick==1){
        capitals$knowledge<-NA # clear previous test capital
        # and add any new knowledge based on contact with OPM
        capitals$knowledge[which(capitals$OPMinverted==0)]<-1
        capitals$knowledge[which(capitals$OPMinverted==1)]<-0
      }else{
        # keep previous knowledge
        prevKnowledge <- read.csv(paste0(dirCRAFTYInput,"worlds/LondonBoroughs/",scenario.split,"/LondonBoroughs_XY_tstep_",CRAFTY_tick-1,".csv"))
        capitals$knowledge <- prevKnowledge$knowledge
        # add new
        capitals$knowledge[which(capitals$OPMinverted==0)]<-1
      }
    }
    
    p3 <- ggplot(capitals)+
      geom_tile(mapping = aes(x,y,fill=knowledge))
    print(p3)
    capitals$joinID <- NULL
    
    capitals <- write.csv(capitals, paste0(dirCRAFTYInput,"worlds/LondonBoroughs/",scenario.split,"/LondonBoroughs_XY_tstep_",CRAFTY_tick,".csv"),row.names = F)
    
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
    val_df <- read.csv(paste0(dirCRAFTYscenario,"/",scenario.split,"-0-", random_seed_crafty,"-LondonBoroughs-Cell-",CRAFTY_tick,".csv"))
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
    highInt <- sfResult %>% filter(Agent == "mgmt_highInt")
    
    # find OPM individuals within each agent type
    # https://gis.stackexchange.com/questions/245136/how-to-subset-point-data-by-outside-of-polygon-data-in-r
    if (nrow(lowInt)>0) { 
      lowInt <- st_transform(lowInt, crs = st_crs(shpIndividuals))
      
      low <- sapply(st_intersects(shpIndividuals, lowInt),function(x){length(x)>0})
      
      # reduce population by half if low intensity
      lowPops <- shpIndividuals$layer[low]
      if (length(lowPops)>1){
        for (pop in c(1:length(lowPops))){
          lowPops[pop]<-round(lowPops[pop]/2)
          if (lowPops[pop]<1){
            lowPops[-pop]}
        }
        
        # remove as test1
        #shpIndividuals <- shpIndividuals[!low,] 
        
      }
      shpIndividuals$layer[low] <- lowPops
    }
    
    if (nrow(highInt)>0) { 
      highInt <- st_transform(highInt, crs = st_crs(shpIndividuals))
      
      high <- sapply(st_intersects(shpIndividuals, highInt),function(x){length(x)>0})
      
      # remove inidividuals if high intensity
      shpIndividuals <- shpIndividuals[!high,] 
    }
    
    print(paste0("============CRAFTY JAVA-R API: Write new individuals file for RangeShiftR = ", CRAFTY_tick))
    
    # write new individuals file to be used by RangeShiftR on the next loop
    shpIndividuals <- shpIndividuals %>% as_Spatial()
    #dfNewIndsTable <- raster::extract(rasterize(shpIndividuals, ascHabitat, field=sprintf('rep0_year%s', rangeshiftrYears-1)), shpIndividuals, cellnumbers=T, df=TRUE)
    dfNewIndsTable <- raster::extract(rasterize(shpIndividuals, ascHabitat, field='layer'), shpIndividuals, cellnumbers=T, df=TRUE)
    dfNewIndsTable$Year <- 0 
    dfNewIndsTable$Species <- 0
    dfNewIndsTable$X <- dfNewIndsTable$cells %% ncol(ascHabitat)
    dfNewIndsTable$Y <- nrow(ascHabitat) - (floor(dfNewIndsTable$cells / ncol(ascHabitat)))
    dfNewIndsTable$Ninds <- dfNewIndsTable$layer
    dfNewIndsTable <- dfNewIndsTable[ , !(names(dfNewIndsTable) %in% c('ID', 'cells', 'layer'))]
    dfNewIndsTable <- dfNewIndsTable[!is.na(dfNewIndsTable$Ninds),]
    # quick fix coords (for some reason when extracting from result raster they get offset by 1 compared to output txt pop file)
    dfNewIndsTable$X <- dfNewIndsTable$X -1
    dfNewIndsTable$Y<- dfNewIndsTable$Y -1
    # make sure individuals aren't being counted more than once in the same location
    dfNewIndsTable <- unique(dfNewIndsTable)
    # add another catch where Ninds == 0 (remove)
    if (nrow(dfNewIndsTable[which(dfNewIndsTable$Ninds==0),])>0){
      dfNewIndsTable <- dfNewIndsTable[-which(dfNewIndsTable$Ninds==0),]
    }
    
    write.table(dfNewIndsTable, file.path(dirRsftrInput, sprintf('inds_tick_%s.txt', CRAFTY_tick)),row.names = F, quote = F, sep = '\t')
    
    if (CRAFTY_nextTick <= end_year_idx) {
      
      (paste0("============CRAFTY JAVA-R API: NextTick=", CRAFTY_nextTick))
      
    } else {
      
      print(paste0("============CRAFTY JAVA-R API: Simulation done (tick=", CRAFTY_tick, ")"))
      write.csv(dfRangeShiftrData, paste0(dirCRAFTYOutput,"/dfRangeshiftR_output_coupled_",scenario.split,".csv"), row.names = F)
      writeRaster(outRasterStack, paste0(dirCRAFTYOutput,"/rstRangeshiftR_output_coupled_",scenario.split,".tif"), overwrite = T)
      
    }
    
    toc(log = TRUE, quiet = TRUE)
    
  }
  
}


log.txt <- tic.log(format = TRUE)
log.lst <- tic.log(format = FALSE)
tic.clearlog()

### look at outputs ------------------------------------------------------------

#warnings() # crs warnings can ignore

names(outRasterStack) <- c("Yr1","Yr2","Yr3","Yr4","Yr5","Yr6","Yr7","Yr8","Yr9","Yr10")
clrs.viridis <- colorRampPalette(viridis::viridis(10))

#png(paste0(dirFigs,"/rsftr_pops_CRAFTY-coupled_",test,".png"), width = 800, height = 600)
spplot(outRasterStack, layout = c(5,2), col.regions=clrs.viridis(14), at = seq(0,70,10))
#dev.off()


### initial tests --------------------------------------------------------------

dfRangeShiftrData <- read.csv(paste0(dirCRAFTYOutput,"/dfRangeshiftR_output_coupled_test1.csv"))
dfRangeShiftrData_standalone <- read.csv(paste0(dirCRAFTYOutput,"/dfRangeshiftR_output_RsftR_standalone.csv"))
dfRangeShiftrData_standalone$models <- "Uncoupled"
dfRangeShiftrData$models <- "Coupled"

dfRsftR_all <- rbind(dfRangeShiftrData_standalone,dfRangeShiftrData)
head(dfRsftR_all)
dfRsftR_all$models <- factor(dfRsftR_all$models, ordered = T, levels = c("Uncoupled","Coupled"))

#png(paste0(dirFigs,"/rsftr_comparePops_uncoupled_vs_coupled_test1.png"), width = 700, height = 450)
dfRsftR_all %>% filter(Year==2) %>% 
  ggplot(aes(timestep,NInds))+
  geom_smooth(color="purple3")+
  facet_wrap(~models)+
  scale_x_continuous(breaks=seq(1,10,1))+
  xlab("Year")+ylab("Total number of individuals in landscape")+
  theme_bw()+theme(text = element_text(size=20, family = "Roboto"),
                   axis.text=element_text(size=10, family = "Roboto"),
                   axis.title=element_text(size=14,face="bold", family = "Roboto"),
                   axis.title.y = element_text(margin = margin(t = 0, r = 10, b = 0, l = 0)),
                   axis.title.x = element_text(margin = margin(t = 10, r = 0, b = 0, l = 0)))
#dev.off()


# read in all CRAFTY results
dirResults <- paste0(dirCRAFTYOutput,"/CRAFTY_coupled_",test,"/")
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
head(sfResult)

# plot 
ggplot() +
  geom_sf(sfResult, mapping = aes(fill = Agent), col = NA)+
  scale_fill_brewer(palette="Dark2")+
  theme_bw()
