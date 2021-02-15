
# 21/01/21
# having issues with RangeShiftR results a few ticks into coupled models
# so test separately here with same parameters

library(RangeShiftR)
library(raster)
library(sf)

### directories ----------------------------------------------------------------

dirWorking<- "~/eclipse-workspace/CRAFTY_RangeshiftR"

dirCRAFTYInput <- path.expand(paste0(dirWorking, "/data_LondonOPM/"))
dirCRAFTYOutput <- path.expand(paste0(dirWorking, "/output"))
# store RangeshiftR files within CRAFTY output folder as it is the directory CRAFTY will need to run in
dirRsftr <- file.path(dirCRAFTYOutput, 'RangeshiftR')
# specific file structure needed for RangeshiftR to run
dirRsftrInput <- file.path(dirRsftr,"Inputs")
dirRsftrOutput <- file.path(dirRsftr,"Outputs")
dirRsftrOutputMaps <- file.path(dirRsftr,"Output_Maps")
# important
# need to add the / for this path to work in RunRS()
dirRsftr <- paste0(dirRsftr,"/") 

setwd(dirWorking)

### parameter set-up -----------------------------------------------------------

rangeshiftrYears2 <- 2
rangeshiftrYears10 <- 10
rstHabitat <- raster(file.path(dirRsftrInput, 'Habitat-100m.tif'))
# make sure BNG
hexPoints <- st_read(paste0(dirWorking,"/data-processed/hexgrids/hexPoints40m.shp"))
rstHabitat <- projectRaster(rstHabitat, crs = crs(hexPoints))
st_crs(rstHabitat)
habitatRes <- 100

land <- ImportedLandscape(LandscapeFile=sprintf('Habitat-%sm.asc', habitatRes),
                          Resolution=habitatRes,
                          HabPercent=TRUE,
                          K_or_DensDep=50) # carrying capacity (individuals per hectare) when habitat at 100% quality

demo <- Demography(Rmax = 25,
                   ReproductionType = 0) # 0 = asexual / only female; 1 = simple sexual; 2 = sexual model with explicit mating system

disp <-  Dispersal(Emigration = Emigration(EmigProb = 0.2),
                   Transfer   = DispersalKernel(Distances = 1500), # test getting to top of landscape while keeping other params low
                   Settlement = Settlement() )




### run RangeShiftR 10 yrs------------------------------------------------------
init <- Initialise(InitType=2, InitIndsFile='initial_inds_2014_n10.txt')
sim <- Simulation(Simulation = 999, # 999 to make sure test simulation is obvious in results folder
                  Years = rangeshiftrYears10,
                  Replicates = 1,
                  OutIntPop = 1,
                  OutIntInd = 1,
                  ReturnPopRaster=TRUE)
s <- RSsim(simul = sim, land = land, demog = demo, dispersal = disp, init = init)
validateRSparams(s)
result <- RunRS(s, sprintf('%s', dirpath = dirRsftr))
crs(result) <- crs(rstHabitat)
extent(result) <- extent(rstHabitat)
#result[[1]]
spplot(result)
#spplot(result[[-1]])

# plot abundance and occupancy
range_df <- readRange(s, dirRsftr)
# ...with replicates:
par(mfrow=c(1,2))
plotAbundance(range_df)
plotOccupancy(range_df)
dev.off()

# completely fine running on it's own, so it may be a mistake in the coupling?
# or maybe due to how new individual files are being edited/written?


### test in loop, 2 yr chunks with new init files each time --------------------

timesteps <- 1:10
dfRangeShiftrData <- data.frame()
outRasterStack <- stack()

#tick <- 1 # for testing

for (tick in timesteps) {
  
  if (tick==1){
    init <- Initialise(InitType=2, InitIndsFile='initial_inds_2014_n10.txt')
  }else{
    init <- Initialise(InitType=2, InitIndsFile=sprintf('inds_tick_%s.txt', tick-1))
  }
  
  #RsftR_tick <- tick+1
  
  sim <- Simulation(Simulation = tick,
                    Years = rangeshiftrYears2,
                    Replicates = 10,
                    OutIntPop = 1,
                    OutIntInd = 1,
                    ReturnPopRaster=TRUE)
  s <- RSsim(simul = sim, land = land, demog = demo, dispersal = disp, init = init)
  stopifnot(validateRSparams(s)==TRUE) 
  
  # run RangeShiftR - use result to store output population raster.
  result <- RunRS(s, sprintf('%s', dirRsftr))
  crs(result) <- crs(rstHabitat)
  extent(result) <- extent(rstHabitat)
  names(result)
  # calculate average of 10 reps for current timestep
  #idx <- grep(paste0("year",tick), names(result))
  idx <- grep("year1", names(result))
  resultMean <- mean(result[[idx]])
  plot(resultMean)
  
  # store population raster in output stack.
  outRasterStack <- addLayer(outRasterStack, resultMean)
  #outRasterStack <- addLayer(outRasterStack, modal(result))
  # store population data in output data frame.
  dfRange <- readRange(s, sprintf('%s',dirRsftr))
  dfRange$timestep <- tick
  dfRangeShiftrData <- rbind(dfRangeShiftrData, dfRange[1,])
  
  # extract the population raster to a shapefile of the individuals
  #shpIndividuals <- rasterToPoints(result[[rangeshiftrYears]], fun=function(x){x > 0}, spatial=TRUE) %>% st_as_sf()
  shpIndividuals <- rasterToPoints(resultMean, fun=function(x){x > 0}, spatial=TRUE) %>% st_as_sf()
  shpIndividuals <- shpIndividuals %>% st_set_crs(st_crs(rstHabitat))
  shpIndividuals$id <- 1:nrow(shpIndividuals)
  shpIndividuals$layer <- ceiling(shpIndividuals$layer)
  
  # write new individuals file to be used by RangeShiftR on the next loop
  shpIndividuals <- shpIndividuals %>% as_Spatial()
  #dfNewIndsTable <- raster::extract(rasterize(shpIndividuals, rstHabitat, field=sprintf('rep0_year%s', RsftR_tick-1)), shpIndividuals, cellnumbers=T, df=TRUE)
  dfNewIndsTable <- raster::extract(rasterize(shpIndividuals, rstHabitat, field='layer'), shpIndividuals, cellnumbers=T, df=TRUE)
  dfNewIndsTable$Year <- 0
  dfNewIndsTable$Species <- 0
  dfNewIndsTable$X <- dfNewIndsTable$cells %% ncol(rstHabitat)
  dfNewIndsTable$Y <- nrow(rstHabitat) - (floor(dfNewIndsTable$cells / ncol(rstHabitat)))
  dfNewIndsTable$Ninds <- dfNewIndsTable$layer
  dfNewIndsTable <- dfNewIndsTable[ , !(names(dfNewIndsTable) %in% c('ID', 'cells', 'layer'))]
  dfNewIndsTable <- dfNewIndsTable[!is.na(dfNewIndsTable$Ninds),]
  # make sure individuals aren't being counted more than once in the same location
  dfNewIndsTable <- unique(dfNewIndsTable)
  # add another catch for Ninds == 0
  if (nrow(dfNewIndsTable[which(dfNewIndsTable$Ninds==0),])>0){
    dfNewIndsTable <- dfNewIndsTable[-which(dfNewIndsTable$Ninds==0),]
  }
  
  
  write.table(dfNewIndsTable, file.path(dirRsftrInput, sprintf('inds_tick_%s.txt', tick)),row.names = F, quote = F, sep = '\t')
  
}

#plot(modal(result))
spplot(outRasterStack)
# 15/02/21 this now seems to be working fine


### notes ----------------------------------------------------------------------
# populations are dying off by tick 3/4 if run in 2-year steps per tick.
# why is it different extracting the result at every timestep compared to running for 10 years from the same init file??
# 25/01/21
# tried using new Rsftr_tick - run RangeshiftR from start year to timestep year each timestep
# because stochastic, will mean individuals take different path each time...
# instead...
# next thing to try - more reps each year and take mean/modal of all reps?
# 01/02/21
# tried mean of 10 reps, running in 2 yr chunks. still dying off by around tick 5


### vary params ----------------------------------------------------------------

# set up tests varying parameters. added catch to jump to next test if populations have died off...

# current params
# K 50, Rmax 25, Dispersal 1500

library(AlgDesign)
dfSensitivity <- gen.factorial(levels = 2, nVars = 3, varNames=c("K","Rmax","Dispersal"))
dfSensitivity
dfSensitivity$K[which(dfSensitivity$K==-1)] <- 20 
dfSensitivity$K[which(dfSensitivity$K==1)] <- 70
dfSensitivity$Rmax[which(dfSensitivity$Rmax==-1)] <- 10 
dfSensitivity$Rmax[which(dfSensitivity$Rmax==1)] <- 40 
dfSensitivity$Dispersal[which(dfSensitivity$Dispersal==-1)] <- 800 # from Cowley et al. 2015
dfSensitivity$Dispersal[which(dfSensitivity$Dispersal==1)] <- 2000 # higher option

dfSensitivity <- tibble::rowid_to_column(dfSensitivity, "ID")

# Create empty data frame and raster stack to store the output data
dfRangeShiftrData <- data.frame()
outRasterStack <- stack()
timesteps <- 1:10
statusList <- c()

# loop through
for (i in c(1:nrow(dfSensitivity))) {
  
  #params <- dfSensitivity[1,] # test
  params <- dfSensitivity[i,] 
  ID <- params[[1]]
  
  sim <- Simulation(Simulation = ID,
                    Years = rangeshiftrYears2,
                    Replicates = 10,
                    OutIntPop = 1, 
                    OutIntInd = 1, 
                    OutIntOcc = 1,
                    ReturnPopRaster = TRUE)
  
  land <- ImportedLandscape(LandscapeFile=sprintf('Habitat-%sm.asc', habitatRes),
                            Resolution=habitatRes,
                            HabPercent=TRUE,
                            K_or_DensDep=params[[2]])
  
  demo <- Demography(Rmax = params[[3]],
                     ReproductionType = 0)
  
  disp <-  Dispersal(Emigration = Emigration(EmigProb = 0.2),
                     Transfer   = DispersalKernel(Distances = params[[4]]),
                     Settlement = Settlement())
  
  #tick <- 1 # test
  
  for (tick in timesteps) {
    
    if (tick==1){
    init <- Initialise(InitType=2, InitIndsFile='initial_inds_2014_n10.txt')
    }else{
    init <- Initialise(InitType=2, InitIndsFile=sprintf('inds_tick_%s.txt', tick-1))
    }
    
    s <- RSsim(simul = sim, land = land, demog = demo, dispersal = disp, init = init)
    
    # run and store raster result
    result <- RunRS(s, sprintf('%s/',dirRsftr))
  
    crs(result) <- crs(rstHabitat)
    extent(result) <- extent(rstHabitat)
    names(result)
    
    # add catch for if pops have died off
    
    if (length(names(result))<20){
      
      # skip to next i in dfSensitivity
      print(paste0("Pops died for parameter test ", i, " @ tick", tick))
      statusList <- append(statusList,paste0("Pops died for parameter test ", i, " @ tick", tick) )
      break

    }
    
    # calculate average of 10 reps for current timestep
    #idx <- grep(paste0("year",tick), names(result))
    idx <- grep("year1", names(result))
    resultMean <- mean(result[[idx]])
    plot(resultMean)
      
    # store population raster in output stack.
    outRasterStack <- addLayer(outRasterStack, resultMean)
    #outRasterStack <- addLayer(outRasterStack, modal(result))
    # store population data in output data frame.
    dfRange <- readRange(s, sprintf('%s',dirRsftr))
    dfRange$timestep <- tick
    dfRange$sensitivityID <- ID
    dfRangeShiftrData <- rbind(dfRangeShiftrData, dfRange[1,])
      
    # extract the population raster to a shapefile of the individuals
    #shpIndividuals <- rasterToPoints(result[[rangeshiftrYears]], fun=function(x){x > 0}, spatial=TRUE) %>% st_as_sf()
    shpIndividuals <- rasterToPoints(resultMean, fun=function(x){x > 0}, spatial=TRUE) %>% st_as_sf()
    shpIndividuals <- shpIndividuals %>% st_set_crs(st_crs(rstHabitat))
    shpIndividuals$id <- 1:nrow(shpIndividuals)
    shpIndividuals$layer <- ceiling(shpIndividuals$layer)
      
    # write new individuals file to be used by RangeShiftR on the next loop
    shpIndividuals <- shpIndividuals %>% as_Spatial()
    #dfNewIndsTable <- raster::extract(rasterize(shpIndividuals, rstHabitat, field=sprintf('rep0_year%s', RsftR_tick-1)), shpIndividuals, cellnumbers=T, df=TRUE)
    dfNewIndsTable <- raster::extract(rasterize(shpIndividuals, rstHabitat, field='layer'), shpIndividuals, cellnumbers=T, df=TRUE)
    dfNewIndsTable$Year <- 0
    dfNewIndsTable$Species <- 0
    dfNewIndsTable$X <- dfNewIndsTable$cells %% ncol(rstHabitat)
    dfNewIndsTable$Y <- nrow(rstHabitat) - (floor(dfNewIndsTable$cells / ncol(rstHabitat)))
    dfNewIndsTable$Ninds <- dfNewIndsTable$layer
    dfNewIndsTable <- dfNewIndsTable[ , !(names(dfNewIndsTable) %in% c('ID', 'cells', 'layer'))]
    dfNewIndsTable <- dfNewIndsTable[!is.na(dfNewIndsTable$Ninds),]
    # make sure individuals aren't being counted more than once in the same location
    dfNewIndsTable <- unique(dfNewIndsTable)
    # add another catch for Ninds == 0
    if (nrow(dfNewIndsTable[which(dfNewIndsTable$Ninds==0),])>0){
      dfNewIndsTable <- dfNewIndsTable[-which(dfNewIndsTable$Ninds==0),]
    }
      
    write.table(dfNewIndsTable, file.path(dirRsftrInput, sprintf('inds_tick_%s.txt', tick)),row.names = F, quote = F, sep = '\t')
    
    if(tick==10){
      print(paste0("Timesteps completed for parameter test ", i))
      statusList <- append(statusList,paste0("Timesteps completed for parameter test ", i))
    }
    
  }
  
  }

statusList
dfSensitivity
# tests 4, 6 and 8 doing best
# higher K carrying capacity the main factor?
# and either increased Rmax or dispersal

