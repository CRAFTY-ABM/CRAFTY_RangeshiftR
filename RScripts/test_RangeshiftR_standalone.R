
# 21/01/21
# having issues with RangeShiftR results a few ticks into coupled models
# so test separately here with same parameters

library(RangeShiftR)
library(raster)
library(sf)

### directories ----------------------------------------------------------------

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
# important
# need to add the / for this path to work in RunRS()
dirRsftr <- paste0(dirRsftr,"/") 

setwd(dirWorking)

### parameter set-up -----------------------------------------------------------

rangeshiftrYears <- 2
rangeshiftrYears2 <- 10
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




### run RangeShiftR ------------------------------------------------------------
init <- Initialise(InitType=2, InitIndsFile='initial_inds_2014_n10.txt')
sim <- Simulation(Simulation = 999, # 999 to make sure test simulation is obvious in results folder
                  Years = rangeshiftrYears2,
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

# completely fine running on it's own, so it must be a mistake in the coupling...
# maybe due to how new individual files are being edited/written?


### test in loop with new init files each time ---------------------------------

timesteps <- 1:10
dfRangeShiftrData <- data.frame()
outRasterStack <- stack()

#tick <- 1

for (tick in timesteps) {
  
  if (tick==1){
    init <- Initialise(InitType=2, InitIndsFile='initial_inds_2014_n10.txt')
  }else{
    init <- Initialise(InitType=2, InitIndsFile=sprintf('inds_tick_%s.txt', tick-1))
  }
  
  RsftR_tick <- tick+1
  
  sim <- Simulation(Simulation = tick,
                    Years = RsftR_tick,
                    Replicates = 100,
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
  # calculate average of 100 reps for current timestep
  idx <- grep(paste0("year",tick), names(result))
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
  # where Ninds = 1, set to 10. Otherwise populations die out
  # they don't die out in RangeshiftR standalone run (which uses the same init file with Ninds set to 10 for entire simulation)
  #dfNewIndsTable$Ninds[which(dfNewIndsTable$Ninds==1)] <- 10
  # add another catch for Ninds == 0
  if (nrow(dfNewIndsTable[which(dfNewIndsTable$Ninds==0),])>0){
    dfNewIndsTable <- dfNewIndsTable[-which(dfNewIndsTable$Ninds==0),]
  }
  
  
  write.table(dfNewIndsTable, file.path(dirRsftrInput, sprintf('inds_tick_%s.txt', tick)),row.names = F, quote = F, sep = '\t')
  
}

#plot(modal(result))
spplot(outRasterStack)

# populations are dying off by tick 3/4 if run in 2-year steps per tick.
# why is it different extracting the result at every timestep compared to running for 10 years from the same init file??
# 25/01/21
# try using new Rsftr_tick - run RangeshiftR from start year to timestep year each timestep

# next thing to try - more reps each year and take mean/modal of all reps?
