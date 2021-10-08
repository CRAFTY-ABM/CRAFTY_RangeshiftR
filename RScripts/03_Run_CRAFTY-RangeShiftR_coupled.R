
# date updated: 07/10/21
# authors: Vanessa Burton, Bumsuk Seo
# description: script which runs coupled CRAFTY & RangeShiftR models


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
# increase java heap space before loading package, from here: https://stackoverflow.com/questions/21937640/handling-java-lang-outofmemoryerror-when-writing-to-excel-from-r
options(java.parameters = "-Xmx8000m")
library(rJava)
library(jdx)
library(xml2)
library(foreach)
library(doSNOW)
library(tictoc)



### directories/ file paths ----------------------------------------------------

if (Sys.info()["user"] %in% c("alan", "seo-b")) { 
  dirWorking<- "~/git/CRAFTY_RangeshiftR2"
  
} else { 
  dirWorking<- "~/eclipse-workspace/CRAFTY_RangeshiftR"
}

dirFigs <- "~/OPM-model-prep-21-22/figs"
dirData <- file.path(dirWorking, 'data-store')

dataDisk <- "D:/CRAFTY_RangeshiftR/output"

dirCRAFTYInput <- path.expand(paste0(dirWorking, "/data_LondonOPM/"))
dirCRAFTYOutput <- path.expand(paste0(dirWorking, "/output"))

# MOVED TO WITHIN LOOP BELOW as this will change per scenario (store a RangeshiftR folder within each scenario output folder)
# store RangeshiftR files within CRAFTY output folder as it is the directory CRAFTY will need to run in
# dirRsftr <- file.path(dirCRAFTYOutput, 'RangeshiftR_coupled')#; dir.create(dirRsftr)
# # specific file structure needed for RangeshiftR to run
# dirRsftrInput <- file.path(dirRsftr,"Inputs")
# dirRsftrOutput <- file.path(dirRsftr,"Outputs")
# dirRsftrOutputMaps <- file.path(dirRsftr,"Output_Maps")
#dir.create(dirRsftrInput)
#dir.create(dirRsftrOutput)
#dir.create(dirRsftrOutputMaps)

# important
# need to add the / for this path to work in RunRS()
#dirRsftr <- paste0(dirRsftr,"/") 

setwd(dirWorking)

source("RScripts/02_Functions_CRAFTY_rJava.R")



### RangeshiftR set-up ---------------------------------------------------------

rangeshiftrYears <- 2

# PoC 2020 data
# #rstHabitat <- raster(file.path(dirRsftrInput, 'Habitat-100m.tif'))
# ascHabitat <- raster(file.path(dirRsftrInput, 'Habitat-100m.asc'))
# # make sure BNG
# hexPoints <- st_read(paste0(dirWorking,"/data-processed/hexgrids/hexPoints40m.shp"))
# #rstHabitat <- projectRaster(rstHabitat, crs = crs(hexPoints))
# #st_crs(rstHabitat)
# crs(ascHabitat) <- crs(hexPoints)
# st_crs(ascHabitat)
# spplot(ascHabitat)
# habitatRes <- 100

# new 2021 Greater London data, in ascii format required by RangeShiftR
ascHabitat <- raster(file.path(dirData, 'Habitat-100m.asc'))
crs(ascHabitat)
# make sure BNG
sfGrid <- st_read(paste0(dirData,"/01_Grid_capitals_raw.shp")) %>% dplyr::select(GridID, geometry)
#st_crs(sfGrid)
# extract centroid points, make spatial points dataframe
spGrid <- as_Spatial(st_centroid(sfGrid))

crs(ascHabitat) <- crs(spGrid)
st_crs(ascHabitat)
spplot(ascHabitat)

# write to RangeShiftR input folder
# writeRaster(ascHabitat, file.path(dirRsftrInput, 'Habitat-100m.asc'), format="ascii", overwrite=TRUE, NAflag=-9999)

# read in initial individuals file (must be tab delimited)
dfInitialIndividuals <- read.table(paste0(dirData,"/01_initial_inds_2012.txt"), sep = '\t', header = TRUE)
# write to RangeShiftR input folder
#write.table(dfInitialIndividuals, file.path(dirRsftrInput, '01_initial_inds_2012.txt'), row.names = F, quote = F, sep = '\t')



### CRAFTY set-up --------------------------------------------------------------

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
#path_crafty_jar <- path.expand(paste0(dirWorking, "/lib/CRAFTY_KIT_engineOct2020.jar"))
path_crafty_jar <- path.expand(paste0(dirWorking, "/lib/CRAFTY_KIT_engineAug2021.jar"))

# location of the CRAFTY lib files
path_crafty_libs <- path.expand(paste0(dirWorking, "/lib/"))
crafty_libs <- list.files(paste0(path_crafty_libs), pattern = "jar")

# make sure that in the classpath setting , gt-opengis-9.0.jar must be included before geoapi-20050403.jar. Otherwise it throws an uncatchable error during the giving up process: loading libraries without ordering them particularly, the opengis library is loaded after the geoapi library following alphabetical order.
# related commit - https://github.com/CRAFTY-ABM/CRAFTY_CoBRA/commit/4ce1041cae349572032fc7e25be49652781f5866
crafty_libs <- crafty_libs[crafty_libs != "geoapi-20050403.jar"  ] 
crafty_libs <- c(crafty_libs,  "geoapi-20050403.jar")

# java configuration
crafty_jclasspath <- c(path_crafty_jar, paste0(path_crafty_libs, crafty_libs))

# random seed used in CRAFTY
random_seed_crafty <- 99 

# CRAFTY timesteps
start_year_idx <- 1 # first year of the input data
end_year_idx <- 10 # 10th year of the input data 

parallelize <- FALSE # not loads of data so don't need to run in parallel

# initialise Java once only. If getting random Java errors, restart Rstudio
if (!rJava::.jniInitialized) { 
  
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

# MOVED TO WITHIN SCENARIO LOOP

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

# scenarios to loop through

#scenario.filenames <- c("Scenario_Baseline_noGUI.xml", "Scenario_de-regulation_noGUI.xml","Scenario_govt-intervention_noGUI.xml") 
scenario.filenames <- c("Scenario_baseline-with-social_GUI.xml", 
                        "Scenario_baseline-no-social_GUI.xml", 
                        "Scenario_de-regulation-with-social_GUI.xml", 
                        "Scenario_de-regulation-no-social_GUI.xml",
                        "Scenario_govt-intervention-with-social.xml",
                        "Scenario_govt-intervention-no-social.xml") 
n.scenario <- length(scenario.filenames)

# run in parallel for speed
# parallelize <- TRUE # VM has 8 cores and 32GB dynamic RAM
# if (parallelize) { 
#   # 6 cores - 1 per scenario
#   n_thread <- 6 # detectCores() 
#   cl <- makeCluster(n_thread)
#   registerDoSNOW(cl)
# }

#foreach(s.idx = 1:n.scenario, .errorhandling = "stop",.packages = c("doSNOW","rJava"), .verbose = T) %dopar% {
for (s.idx in 1:length(scenario.filenames)){
  
  #s.idx <- 1 # for testing
  scenario <- scenario.filenames[s.idx] 
  scenario.filename <- scenario
  scenario.split <- strsplit(scenario, "[_]")[[1]][2]
 
  # scenario file and arguments reguired for CRAFTY run
  CRAFTY_sargs <- c("-d", dirCRAFTYInput, "-f", scenario.filename, "-o", random_seed_crafty, "-r", "1",  "-n", "1", "-sr", "0") 
  
  # set up CRAFTY job
  # create a new instance (to call non-static methods)
  CRAFTY_jobj <- new(J(CRAFTY_main_name)) 
  # (can ignore the error this spits out)
  
  # prepares a run and returns run information 
  CRAFTY_RunInfo_jobj <- CRAFTY_jobj$EXTprepareRrun(CRAFTY_sargs)
  
  # set the schedule
  CRAFTY_loader_jobj <- CRAFTY_jobj$EXTsetSchedule(as.integer(start_year_idx), as.integer(end_year_idx))
  
  timesteps <- start_year_idx:end_year_idx
  
  ### pre-process CRAFTY Java object
  region <- CRAFTY_loader_jobj$getRegions()$getAllRegions()$iterator()$'next'()
  
  # change wd to a scenario folder to store output files
  dirCRAFTYscenario <- paste0(dirCRAFTYOutput,"/behaviour_baseline/",scenario.split)
  
  # check if exists and create if not
  if (file.exists(dirCRAFTYscenario)){
    # set as wd? / do nothing
    print("folder exists")
  }else{dir.create(file.path(dirCRAFTYscenario))}
  
  # set RangeshiftR paths based on scenario
  dirRsftr <- file.path(dirCRAFTYscenario)
 
  # specific file structure needed for RangeshiftR to run
  dirRsftrInput <- file.path(dirRsftr,"Inputs")
  dirRsftrOutput <- file.path(dirRsftr,"Outputs")
  dirRsftrOutputMaps <- file.path(dirRsftr,"Output_Maps")
  
  # check if folders exist and create if no
  if (file.exists(dirRsftrInput)){
    # set as wd? / do nothing
    print("folder exists")
  }else{dir.create(file.path(dirRsftrInput))}
  if (file.exists(dirRsftrOutput)){
    # set as wd? / do nothing
    print("folder exists")
  }else{dir.create(file.path(dirRsftrOutput))}
  if (file.exists(dirRsftrOutputMaps)){
    # set as wd? / do nothing
    print("folder exists")
  }else{dir.create(file.path(dirRsftrOutputMaps))}
  
  ## important - need to add the / for this path to work in RunRS()
  dirRsftr <- paste0(dirRsftr,"/") 
  
  # make sure RangeshiftR Inputs folder has required files
  writeRaster(ascHabitat, file.path(dirRsftrInput, 'Habitat-100m.asc'), format="ascii", overwrite=TRUE, NAflag=-9999)
  write.table(dfInitialIndividuals, file.path(dirRsftrInput, '01_initial_inds_2012.txt'), row.names = F, quote = F, sep = '\t')
  
  # set-up RangeshiftR parameters for this scenario
  land <- ImportedLandscape(LandscapeFile='Habitat-100m.asc',
                            Resolution=100,
                            HabPercent=TRUE,
                            K_or_DensDep=1000) # carrying capacity (individuals per hectare) when habitat at 100% quality
  
  demo <- Demography(Rmax = 25,
                     ReproductionType = 0) # 0 = asexual / only female; 1 = simple sexual; 2 = sexual model with explicit mating system
  
  ### ADD ANOTHER LOOP HERE TO RUN BOTH DISPERSAL KERNEL TYPES?
  
  # single kernel
  # disp <-  Dispersal(Emigration = Emigration(EmigProb = 0.2),
  #                    Transfer = DispersalKernel(Distances = 500), 
  #                    Settlement = Settlement())
  
  # double kernel
  # set up probability matrix - 95% prob dispersal will be 500m, some chance of long range dispersal
  dists <- matrix(c(500,7300,0.95),ncol = 3)
  
  disp <-  Dispersal(Emigration = Emigration(EmigProb = 0.2),
                     Transfer = DispersalKernel(Distances = dists, DoubleKernel = TRUE), 
                     Settlement = Settlement())
  
  # for storing RangeshiftR output data
  dfRangeShiftrData <- data.frame()
  outRasterStack <- stack()
  
  # set the batch run folder (dirCRAFTYOutput)
  .jcall( 'java/lang/System', 'S', 'setProperty', 'user.dir',  dirCRAFTYOutput)
  
  # assertion
  stopifnot(dirCRAFTYOutput == .jcall( 'java/lang/System', 'S', 'getProperty', 'user.dir' ))
  
  # loop through years for this scenario 
  
  for (yr.idx in 1:length(timesteps)) {
    
    #yr.idx <- 1 #for testing
    
    CRAFTY_tick <- timesteps[yr.idx]
    
    # before EXTtick() (line 438)
    # run RangeshiftR to get OPM capital
    
    tic(CRAFTY_tick) # time loop
    
    print(paste0("============CRAFTY JAVA-R API: Setting up RangeShiftR tick = ", CRAFTY_tick))
    
    # set up RangeShiftR for current iteration
    # init file updates based on CRAFTY if after tick 1
    if (CRAFTY_tick==1){
      init <- Initialise(InitType=2, InitIndsFile='01_initial_inds_2012.txt')
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
    #Sys.sleep(1)
    
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
    spPointsOPM <- raster::extract(resultMean, spPoints)
    dfOPM <- cbind(spPoints,spPointsOPM) %>% as.data.frame()
    colnames(dfOPM)[2] <- "population"
    dfOPM$population[which(is.na(dfOPM$population))] <- 0
    
    print(paste0("============CRAFTY JAVA-R API: Convert RangeShiftR population results to binary capital = ", CRAFTY_tick))
    
    # make binary version and invert for CRAFTY
    OPMbinary <- dfOPM$population
    OPMbinary[which(OPMbinary>0)] <- 1
    invert <- OPMbinary - 1
    OPMinv <- abs(invert)
    dfOPMinv <- tibble(dfOPM$GridID,OPMinv)
    colnames(dfOPMinv)[1] <- "GridID"
    
    # update OPM inverted capital in updater files
    capitals <- read.csv(paste0(dirCRAFTYInput,"worlds/GreaterLondon/",scenario.split,"/GreaterLondon_tstep_",CRAFTY_tick,".csv"))
    # update OPM capital using lookUp
    lookUp$OPMinverted <- dfOPMinv$OPMinv[match(lookUp$GridID, dfOPMinv$GridID)]
    capitals$GridID <- lookUp$GridID
    capitals$OPM_presence <- dfOPMinv$OPMinv[match(capitals$GridID, dfOPM$GridID)]
    # check
    p2 <- ggplot(capitals)+
      geom_tile(mapping = aes(x,y,fill=OPM_presence))
    print(p2)
    
    
    ### IMPLEMENT SCENARIO DEPDENDENT CHANGES HERE:
    
    # 1. conditional update knowledge to be dependent on OPM presence
    # for de-regulation scenario, there is no monitoring, so minimal knowledge (0.2 instead of 1)
    if (scenario.split == "de-regulation"){
      if (CRAFTY_tick==1){
        
        # clear previous test capital
        capitals$Knowledge<-0 
        
        # and add small amount of knowledge based on contact with OPM
        capitals$Knowledge[which(capitals$OPM_presence==0)]<-0.2
        capitals$Knowledge[which(capitals$OPM_presence==1)]<-0
        
        # increase risk perception where there is OPM
        capitals$Risk_perception[which(capitals$OPM_presence == 0)] <- 0.8
        
      }else{
        # keep previous knowledge
        prevKnowledge <- read.csv(paste0(dirCRAFTYInput,"worlds/GreaterLondon/",scenario.split,"/GreaterLondon_tstep_",CRAFTY_tick-1,".csv"))
        capitals$Knowledge <- prevKnowledge$knowledge
        # add new
        capitals$Knowledge[which(capitals$OPM_presence==0)]<-0.2
      }
    }else{
      if (CRAFTY_tick==1){
        capitals$Knowledge<-0 # clear previous test capital
        # and add any new knowledge based on contact with OPM
        capitals$Knowledge[which(capitals$OPM_presence==0)]<-1
        capitals$Knowledge[which(capitals$OPM_presence==1)]<-0
      }else{
        # keep previous knowledge
        prevKnowledge <- read.csv(paste0(dirCRAFTYInput,"worlds/GreaterLondon/",scenario.split,"/GreaterLondon_tstep_",CRAFTY_tick-1,".csv"))
        capitals$Knowledge <- prevKnowledge$Knowledge
        # add new
        capitals$Knowledge[which(capitals$OPM_presence==0)]<-1
      }
    }
    
    # check
    p3 <- ggplot(capitals)+
      geom_tile(mapping = aes(x,y,fill=Knowledge))
    print(p3)
    
    capitals$GridID <- NULL
    capitals <- write.csv(capitals, paste0(dirCRAFTYInput,"worlds/GreaterLondon/",scenario.split,"/GreaterLondon_tstep_",CRAFTY_tick,".csv"),row.names = F)
    
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
    
    # extract agent locations, match to sf grid
    val_df <- read.csv(paste0(dirCRAFTYscenario,"/",scenario.split,"-0-", random_seed_crafty,"-GreaterLondon-Cell-",CRAFTY_tick,".csv"))
    val_fr <- val_df[,"Agent"]
    val_fr_fac <- factor(val_fr,  labels = aft_names_fromzero, levels = aft_names_fromzero)
    
    # match back to sf Grid using GridID
    # val_xy <- data.frame(val_df$X,val_df$Y)
    # colnames(val_xy) <- c("X", "Y")
    # x_coord <- lookUp[match(val_xy$X, lookUp$X), "x_coord"]
    # y_coord <- lookUp[match(val_xy$Y, lookUp$Y), "y_coord"]
    # 
    # cellid <- foreach(rowid = 1:nrow(val_xy), .combine = "c") %do% { 
    #   which((as.numeric(val_xy[rowid, 1]) == london_xy_df$X) & (as.numeric(val_xy[rowid, 2]) == london_xy_df$Y))
    # }
    
    # simpler this year because square grid, not hexagonal
    val_df$GridID <- lookUp$GridID # in same order so I can get away with this simple version
    sfResult <- left_join(sfGrid, val_df, by="GridID")
    sfResult$Agent <- factor(sfResult$Agent, levels=aft_names_fromzero)
    
    print(paste0("============CRAFTY JAVA-R API: Show agents & OPM individuals = ", CRAFTY_tick)) 
    p1 <- ggplot() +
      geom_sf(sfResult, mapping = aes(fill = Agent), col = NA)+
      #geom_sf(data=shpIndividuals, color="black", pch=4)+
      scale_fill_brewer(palette="Dark2")
    print(p1)
    
    # now use to edit RangeshiftR individuals
    print(paste0("============CRAFTY JAVA-R API: Edit RangeshiftR individuals tick = ", CRAFTY_tick))
    
    # find where OPM individuals intersect
    mgmt_remove <- sfResult %>% filter(Agent == "mgmt_remove")
    mgmt_pesticide <- sfResult %>% filter(Agent == "mgmt_pesticide")
    mgmt_fell <- sfResult %>% filter(Agent == "mgmt_fell")
    
    # find OPM individuals within each agent type
    # https://gis.stackexchange.com/questions/245136/how-to-subset-point-data-by-outside-of-polygon-data-in-r
    if (nrow(mgmt_remove)>0) { 
      mgmt_remove <- st_transform(mgmt_remove, crs = st_crs(shpIndividuals))
      
      remove <- sapply(st_intersects(shpIndividuals, mgmt_remove),function(x){length(x)>0})
      
      # reduce population by half if physical removal
      remPops <- shpIndividuals$layer[remove]
      if (length(remPops)>1){
        for (pop in c(1:length(remPops))){
          remPops[pop]<-round(remPops[pop]*0.5) # reduce by 50%
          if (remPops[pop]<1){
            remPops[-pop]}
        }
      }
      
      shpIndividuals$layer[remove] <- remPops
    }
    
    if (nrow(mgmt_pesticide)>0) { 
      mgmt_pesticide <- st_transform(mgmt_pesticide, crs = st_crs(shpIndividuals))
      
      pesticide <- sapply(st_intersects(shpIndividuals, mgmt_pesticide),function(x){length(x)>0})
      
      # reduce population by 80% if spraying pesticides
      pestPops <- shpIndividuals$layer[pesticide]
      if (length(pestPops)>1){
        for (pop in c(1:length(pestPops))){
          pestPops[pop]<-round(pestPops[pop]*0.2) # reduce by 80%
          if (pestPops[pop]<1){
            pestPops[-pop]}
        }
      }
      shpIndividuals$layer[remove] <- pestPops
      
    }
    
    
    if (nrow(mgmt_fell)>0) { 
      mgmt_fell <- st_transform(mgmt_fell, crs = st_crs(shpIndividuals))
      
      fell <- sapply(st_intersects(shpIndividuals, mgmt_fell),function(x){length(x)>0})
      
      # reduce pop by 80% if spraying pesticides
      shpIndividuals <- shpIndividuals[!fell,] 
    }
    
    
    print(paste0("============CRAFTY JAVA-R API: Write new individuals file for RangeShiftR = ", CRAFTY_tick))
    
    # write new individuals file to be used by RangeShiftR on the next loop
    #shpIndividuals <- shpIndividuals %>% as_Spatial()
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
#stopCluster(cl)



# look at timings
log.txt <- tic.log(format = TRUE)
log.lst <- tic.log(format = FALSE)
tic.clearlog()

