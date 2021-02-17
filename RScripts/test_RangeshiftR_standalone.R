
# 21/01/21
# having issues with RangeShiftR results a few ticks into coupled models
# so test separately here with same parameters

library(RangeShiftR)
library(raster)
library(sf)
library(viridis)
library(ggplot2)

### directories ----------------------------------------------------------------

dirWorking<- "~/eclipse-workspace/CRAFTY_RangeshiftR"

dirFigs <- "~/CRAFTY-opm/figures"

dirCRAFTYInput <- path.expand(paste0(dirWorking, "/data_LondonOPM/"))
dirCRAFTYOutput <- path.expand(paste0(dirWorking, "/output"))
# store RangeshiftR files within CRAFTY output folder as it is the directory CRAFTY will need to run in
dirRsftr2 <- file.path(dirCRAFTYOutput, 'RangeshiftR_standalone')
# specific file structure needed for RangeshiftR to run
dirRsftrInput2 <- file.path(dirRsftr2,"Inputs")
dirRsftrOutput2 <- file.path(dirRsftr2,"Outputs")
dirRsftrOutputMaps2 <- file.path(dirRsftr2,"Output_Maps")
# important
# need to add the / for this path to work in RunRS()
dirRsftr2 <- paste0(dirRsftr2,"/") 

setwd(dirWorking)

### parameter set-up -----------------------------------------------------------

rangeshiftrYears2 <- 2
rangeshiftrYears10 <- 10
#rstHabitat <- raster(file.path(dirRsftrInput2, 'Habitat-100m.tif'))
ascHabitat <- raster(file.path(dirRsftrInput2, 'Habitat-100m.asc'))
# make sure BNG
hexPoints <- st_read(paste0(dirWorking,"/data-processed/hexgrids/hexPoints40m.shp"))
#rstHabitat <- projectRaster(rstHabitat, crs = crs(hexPoints))
crs(ascHabitat) <- crs(hexPoints)
st_crs(ascHabitat)
spplot(ascHabitat)
habitatRes <- 100

land <- ImportedLandscape(LandscapeFile=sprintf('Habitat-%sm.asc', habitatRes),
                          Resolution=habitatRes,
                          HabPercent=TRUE,
                          #K_or_DensDep=70) 
                          K_or_DensDep=50) # carrying capacity (individuals per hectare) when habitat at 100% quality

demo <- Demography(Rmax = 25,
#demo <- Demography(Rmax = 40,
                   ReproductionType = 0) # 0 = asexual / only female; 1 = simple sexual; 2 = sexual model with explicit mating system

disp <-  Dispersal(Emigration = Emigration(EmigProb = 0.2),
                   Transfer   = DispersalKernel(Distances = 800), # test getting to top of landscape while keeping other params low
                   #Transfer   = DispersalKernel(Distances = 1500), # test getting to top of landscape while keeping other params low
                   Settlement = Settlement() )




### run RangeShiftR 10 yrs------------------------------------------------------
init <- Initialise(InitType=2, InitIndsFile='initial_inds_2014_n10.txt')
sim <- Simulation(Simulation = 999, # 999 to make sure test simulation is obvious in results folder
                  Years = rangeshiftrYears10,
                  Replicates = 1,
                  OutIntPop = 1,
                  OutIntInd = 1,
                  ReturnPopRaster=TRUE)
s <- RSsim(simul = sim, land = land, demog = demo, dispersal = disp, init = init, seed = 261090)
validateRSparams(s)
result10yr <- RunRS(s, sprintf('%s', dirpath = dirRsftr2))
crs(result10yr) <- crs(ascHabitat)
extent(result10yr) <- extent(ascHabitat)

# plot
#spplot(result10yr)
names(result10yr) <- c("Yr1","Yr2","Yr3","Yr4","Yr5","Yr6","Yr7","Yr8","Yr9","Yr10")
clrs.viridis <- colorRampPalette(viridis::viridis(10))

png(paste0(dirFigs,"/rsftr_pops_10yr_standalone.png"), width = 800, height = 600)
spplot(result10yr, layout = c(5,2), col.regions=clrs.viridis(14), at = seq(0,70,10))
dev.off()

# plot abundance and occupancy
dfRange10yr <- readRange(s, dirRsftr2)
# ...with replicates:
par(mfrow=c(1,2))
plotAbundance(dfRange10yr)
plotOccupancy(dfRange10yr)
dev.off()

# completely fine running on it's own, so it may be a mistake in the coupling?
# or maybe due to how new individual files are being edited/written?


### test in loop, 2 yr chunks with new init files each time --------------------

timesteps <- 1:10
dfRangeShiftrData2 <- data.frame()
outRasterStack2 <- stack()

#tick <- 2 # for testing

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
  s <- RSsim(simul = sim, land = land, demog = demo, dispersal = disp, init = init, seed = 261090)
  stopifnot(validateRSparams(s)==TRUE) 
  
  # run RangeShiftR - use result2 to store output population raster.
  result2 <- RunRS(s, sprintf('%s', dirRsftr2))
  crs(result2) <- crs(ascHabitat)
  extent(result2) <- extent(ascHabitat)
  names(result2)
  # calculate average of 10 reps for current timestep
  #idx <- grep(paste0("year",tick), names(result2))
  idx <- grep("year1", names(result2))
  resultMean2 <- mean(result2[[idx]])
  spplot(resultMean2)
  
  # store population raster in output stack.
  outRasterStack2 <- addLayer(outRasterStack2, resultMean2)
  #outRasterStack2 <- addLayer(outRasterStack2, modal(result2))
  # store population data in output data frame.
  dfRange2 <- readRange(s, sprintf('%s',dirRsftr2))
  dfRange2$timestep <- tick
  dfRangeShiftrData2 <- rbind(dfRangeShiftrData2, dfRange2[,])
  
  # extract the population raster to a shapefile of the individuals
  #shpIndividuals2 <- rasterToPoints(result2[[rangeshiftrYears]], fun=function(x){x > 0}, spatial=TRUE) %>% st_as_sf()
  shpIndividuals2 <- rasterToPoints(resultMean2, fun=function(x){x > 0}, spatial=TRUE) %>% st_as_sf()
  shpIndividuals2 <- shpIndividuals2 %>% st_set_crs(st_crs(ascHabitat))
  shpIndividuals2$id <- 1:nrow(shpIndividuals2)
  shpIndividuals2$layer <- ceiling(shpIndividuals2$layer)
  
  # write new individuals file to be used by RangeShiftR on the next loop
  shpIndividuals2 <- shpIndividuals2 %>% as_Spatial()
  #dfNewIndsTable <- raster::extract(rasterize(shpIndividuals2, ascHabitat, field=sprintf('rep0_year%s', RsftR_tick-1)), shpIndividuals2, cellnumbers=T, df=TRUE)
  dfNewIndsTable <- raster::extract(rasterize(shpIndividuals2, ascHabitat, field='layer'), shpIndividuals2, cellnumbers=T, df=TRUE)
  dfNewIndsTable$Year <- 0
  dfNewIndsTable$Species <- 0
  dfNewIndsTable$X <- dfNewIndsTable$cells %% ncol(ascHabitat) 
  dfNewIndsTable$Y <- nrow(ascHabitat) - (floor(dfNewIndsTable$cells / ncol(ascHabitat))) 
  dfNewIndsTable$Ninds <- dfNewIndsTable$layer
  dfNewIndsTable <- dfNewIndsTable[ , !(names(dfNewIndsTable) %in% c('ID', 'cells', 'layer'))] 
  dfNewIndsTable <- dfNewIndsTable[!is.na(dfNewIndsTable$Ninds),]
  # quick fix coords
  dfNewIndsTable$X <- dfNewIndsTable$X -1
  dfNewIndsTable$Y<- dfNewIndsTable$Y -1
  # make sure individuals aren't being counted more than once in the same location
  dfNewIndsTable <- unique(dfNewIndsTable)
  # add another catch for Ninds == 0
  if (nrow(dfNewIndsTable[which(dfNewIndsTable$Ninds==0),])>0){
    dfNewIndsTable <- dfNewIndsTable[-which(dfNewIndsTable$Ninds==0),]
  }
  
  
  write.table(dfNewIndsTable, file.path(dirRsftrInput2, sprintf('inds_tick_%s.txt', tick)),row.names = F, quote = F, sep = '\t')
  
}


spplot(outRasterStack2)
names(outRasterStack2) <- c("Yr1","Yr2","Yr3","Yr4","Yr5","Yr6","Yr7","Yr8","Yr9","Yr10")
clrs.viridis <- colorRampPalette(viridis::viridis(10))

png(paste0(dirFigs,"/rsftr_pops_10yr_2yr-interrupted.png"), width = 800, height = 600)
spplot(outRasterStack2, layout = c(5,2), col.regions=clrs.viridis(14), at = seq(0,70,10))
dev.off()

write.csv(dfRangeShiftrData2, paste0(dirCRAFTYOutput,"/dfRangeshiftR_output_RsftR_standalone.csv"), row.names = F)

# compare abundance timeseries of 10 yr version with interrupted version with 3 simulated yrs at each tick
# want to see that the middle point (year==1) matches up with the lower point (year==0) of the following tick
par(mfrow=c(1,2))
plotAbundance(dfRange10yr)
plot(NInds~timestep, data = dfRangeShiftrData2)
dev.off()

# compare all three (10 yr standalone, 2yr chunks standalone, 2 yr chunks coupled)
#par(mfrow=c(1,3))
#plotAbundance(dfRange10yr)
#plot(NInds~timestep, data=dfRangeShiftrData2)
#plot(NInds~timestep, data=dfRangeShiftrData)
#dev.off()

png(paste0(dirFigs,"/rsftr_comparePops_uncoupled_vs_coupled.png"), width = 800, height = 600)
par(mfrow=c(1,2))
dfRangeShiftrData2 %>% filter(Year==2) %>% 
  group_by(timestep) %>% summarise(avgNinds=mean(NInds)) %>% 
  plot(avgNinds~timestep, ., main = "Uncoupled", type="l")
  #ggplot(aes(timestep,NInds))+geom_point()+theme_bw()
dfRangeShiftrData %>% filter(Year==2) %>%
  group_by(timestep) %>% summarise(avgNinds=mean(NInds)) %>% 
  plot(avgNinds~timestep, ., main = "Coupled", type="l")
  #ggplot(aes(timestep,NInds))+geom_point()+theme_bw()
dev.off()

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
dfRangeShiftrData2 <- data.frame()
outRasterStack2 <- stack()
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
    result2 <- RunRS(s, sprintf('%s/',dirRsftr2))
  
    crs(result2) <- crs(ascHabitat)
    extent(result2) <- extent(ascHabitat)
    names(result2)
    
    # add catch for if pops have died off
    
    if (length(names(result2))<20){
      
      # skip to next i in dfSensitivity
      print(paste0("Pops died for parameter test ", i, " @ tick", tick))
      statusList <- append(statusList,paste0("Pops died for parameter test ", i, " @ tick", tick) )
      break

    }
    
    # calculate average of 10 reps for current timestep
    #idx <- grep(paste0("year",tick), names(result2))
    idx <- grep("year1", names(result2))
    resultMean2 <- mean(result2[[idx]])
    plot(resultMean2)
      
    # store population raster in output stack.
    outRasterStack2 <- addLayer(outRasterStack2, resultMean2)
    #outRasterStack2 <- addLayer(outRasterStack2, modal(result2))
    # store population data in output data frame.
    dfRange2 <- readRange(s, sprintf('%s',dirRsftr2))
    dfRange2$timestep <- tick
    dfRange2$sensitivityID <- ID
    dfRangeShiftrData2 <- rbind(dfRangeShiftrData2, dfRange2[1,])
      
    # extract the population raster to a shapefile of the individuals
    #shpIndividuals2 <- rasterToPoints(result2[[rangeshiftrYears]], fun=function(x){x > 0}, spatial=TRUE) %>% st_as_sf()
    shpIndividuals2 <- rasterToPoints(resultMean2, fun=function(x){x > 0}, spatial=TRUE) %>% st_as_sf()
    shpIndividuals2 <- shpIndividuals2 %>% st_set_crs(st_crs(ascHabitat))
    shpIndividuals2$id <- 1:nrow(shpIndividuals2)
    shpIndividuals2$layer <- ceiling(shpIndividuals2$layer)
      
    # write new individuals file to be used by RangeShiftR on the next loop
    shpIndividuals2 <- shpIndividuals2 %>% as_Spatial()
    #dfNewIndsTable <- raster::extract(rasterize(shpIndividuals2, ascHabitat, field=sprintf('rep0_year%s', RsftR_tick-1)), shpIndividuals2, cellnumbers=T, df=TRUE)
    dfNewIndsTable <- raster::extract(rasterize(shpIndividuals2, ascHabitat, field='layer'), shpIndividuals2, cellnumbers=T, df=TRUE)
    dfNewIndsTable$Year <- 0
    dfNewIndsTable$Species <- 0
    dfNewIndsTable$X <- dfNewIndsTable$cells %% ncol(ascHabitat)
    dfNewIndsTable$Y <- nrow(ascHabitat) - (floor(dfNewIndsTable$cells / ncol(ascHabitat)))
    dfNewIndsTable$Ninds <- dfNewIndsTable$layer
    dfNewIndsTable <- dfNewIndsTable[ , !(names(dfNewIndsTable) %in% c('ID', 'cells', 'layer'))]
    dfNewIndsTable <- dfNewIndsTable[!is.na(dfNewIndsTable$Ninds),]
    # make sure individuals aren't being counted more than once in the same location
    dfNewIndsTable <- unique(dfNewIndsTable)
    # add another catch for Ninds == 0
    if (nrow(dfNewIndsTable[which(dfNewIndsTable$Ninds==0),])>0){
      dfNewIndsTable <- dfNewIndsTable[-which(dfNewIndsTable$Ninds==0),]
    }
      
    write.table(dfNewIndsTable, file.path(dirRsftrInput2, sprintf('inds_tick_%s.txt', tick)),row.names = F, quote = F, sep = '\t')
    
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

