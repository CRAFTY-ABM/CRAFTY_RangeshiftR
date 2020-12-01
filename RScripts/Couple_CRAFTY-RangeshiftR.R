
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
library(jdx)
library(xml2)
library(foreach)

# directories/ file paths ------------------------------------------------------

dirWorking<- "~/eclipse-workspace/CRAFTY_RangeshiftR"

dirRsftr <- file.path(dirWorking, 'RangeshiftR')
dirRsftrInput <- file.path(dirRsftr,"Inputs")
dirRsftrOutput <- file.path(dirRsftr,"Outputs")
dirRsftrOutputMaps <- file.path(dirRsftr,"Output_Maps")
dirRsftr <- file.path('C:/Users/vanessa.burton.sb/Documents/eclipse-workspace/CRAFTY_RangeshiftR/RangeshiftR/') # need to add the / for this path to work in RunRS

#dir.create(dirRsftrInput)
#dir.create(dirRsftrOutput)
#dir.create(dirRsftrOutputMaps)

dirCRAFTYInput <- path.expand(paste0(dirWorking, "data_LondonOPM/"))
dirCRAFTYOutput <- path.expand(paste0(dirWorking, "output/"))

setwd(dirWorking)

source("RScripts/Functions_CRAFTY_rJava.R")


### Global paramaters ----------------------------------------------------------

# british national grid crs
proj4.BNG <- "+proj=tmerc +lat_0=49 +lon_0=-2 +k=0.9996012717 +x_0=400000 +y_0=-100000 +ellps=airy +towgs84=446.448,-125.157,542.06,0.15,0.247,0.842,-20.489 +units=m +no_defs"


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


### CRAFTY set-up --------------------------------------------------------------

# points for each cell to extract from OPM population results
hexPoints <- st_read(paste0(dirWorking,"/data-processed/hexGrids/hexPoints40m.shp"))
hexPointsSP <- as_Spatial(hexPoints)



### Model loop -----------------------------------------------------------------

# for storing output data
dfRangeShiftrData <- data.frame()
outRasterStack <- stack()

for (iteration in 1:10) {
  
  iteration <- 1 # for testing
  
  # set up RangeShiftR for current iteration
  sim <- Simulation(Simulation = iteration,
                    Years = rangeshiftrYears,
                    Replicates = 1,
                    OutIntPop = 1,
                    OutIntInd = 1,
                    ReturnPopRaster=TRUE)
  s <- RSsim(simul = sim, land = land, demog = demo, dispersal = disp, init = init)
  validateRSparams(s)
  
  # run RangeShiftR - use result to store our output population raster.
  result <- RunRS(s, sprintf('%s/', dirRsftr))
  crs(result) <- crs(rstHabitat)
  extent(result) <- extent(rstHabitat)
  #plot(result[[rangeshiftrYears]])
  # store RangeShiftR's population raster in output stack.
  outRasterStack <- addLayer(outRasterStack, result[[rangeshiftrYears]])
  # store RangeShiftR's population data in output data frame.
  dfRange <- readRange(s, sprintf('%s/',dirRsftr))
  dfRange$iteration <- iteration
  dfRangeShiftrData <- rbind(dfRangeShiftrData, dfRange[1,])
  
  # extract the population raster to a shapefile of the individuals
  shpIndividuals <- rasterToPoints(result[[rangeshiftrYears]], fun=function(x){x > 0}, spatial=TRUE) %>% st_as_sf()
  shpIndividuals <- st_transform(shpIndividuals, crs(rstHabitat))
  shpIndividuals$id <- 1:nrow(shpIndividuals)
  
  ### get CRAFTY working here
  
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
  
  #dfOPM %>% dplyr::filter(population>0)
  
  # add to/edit capitals file
  capitals <- read.csv(paste0(dirCRAFTYInput,"worlds/LondonBoroughs/LondonBoroughs_original.csv"))
  hx <- read.csv(paste0(dirWorking,"/data-processed/Cell_ID_XY_Borough.csv"))
  
    head(capitals)
  colnames(capitals)[3:4] = c("Lon", "Lat")
  capitals$x  = hx$X[match(capitals$joinID, hx$Cell_ID)]
  capitals$y  = hx$Y[match(capitals$joinID, hx$Cell_ID)]
  
  head(capitals)
  capitals$OPMpresence <- dfOPM$OPMpresence[match(capitals$joinID, dfOPM$joinID)]
  capitals$OPMinverted <- dfOPM$OPMinv[match(capitals$joinID, dfOPM$joinID)]
  
  # check
  #ggplot(capitals)+
    #geom_tile(mapping = aes(x,y,fill=OPMpresence))
  
  }
  

