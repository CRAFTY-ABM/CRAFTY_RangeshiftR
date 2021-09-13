
# date: 31/08/2021
# author: VB
# purpose: edit or create all files required for CRAFTY

### libs -----------------------------------------------------------------------

library(tidyverse)
library(sf)
library(raster)
library(terra)
library(BBmisc) # for normalisation
library(viridis)
library(tmap)

### paths ----------------------------------------------------------------------

wd <- "~/eclipse-workspace/CRAFTY_RangeshiftR" # sandbox VM
dirData <- file.path(wd, 'data-processed')
dirOut <- file.path(wd, 'data_LondonOPM')

### CRAFTY set-up --------------------------------------------------------------

### csv ------------------------------------------------------------------------

# this folder holds basic index files which tell CRAFTY which capitals and services it should expect

dfCapitals <- read.csv(paste0(dirOut,"/csv/Capitals.csv"))
head(dfCapitals)
write.csv(dfCapitals, paste0(dirOut,"/csv/Capitals_old.csv"), row.names = FALSE) # save PoC version

# list all capitals here
Name <- c("OPM_presence",
          "Risk_perception",
          "Willingness_to_pay",
          "Knowledge",
          "Risk_map",
          "Nature",
          "Access",
          "Zone")
# index must run from 0
Index <- seq(0,length(Name)-1,by=1)

dfCapitals <- tibble(Name,Index)
dfCapitals

write.csv(dfCapitals, paste0(dirOut,"/csv/Capitals.csv"), row.names = FALSE)

dfServices <- read.csv(paste0(dirOut,"/csv/Services.csv"))
dfServices
# this can stay the same - no change this year


### production -----------------------------------------------------------------

# this folder holds agent production files
# these describe which capitals each agent relies on (values 0-1, no reliance - high reliance)
# and which services the agent produces (values 0-1, no production - highest possible production)

# scenario sub-folders within production mean these parameters can change per scenario

dfAgents <- read.csv(paste0(dirData,"/AgentMaster.csv"))
head(dfAgents)

lstAgents <- unique(dfAgents$Agent)
lstScenarios <- c("with_social","no_social") # sensitivity to social capitals vs. not

for (AFT in lstAgents){
  
  #AFT <- lstAgents[1]
  
  dfAFT <- filter(dfAgents, Agent == AFT)
  dfAFT$Agent <- NULL
  
  print(AFT)
  
  for (scenario in lstScenarios){
    
    #scenario <- lstScenarios[1]
    
    dfAFT <- filter(dfAFT, Paramset == scenario)
    #dfAFT$Paramset <- NULL
    
    print(dfAFT)
    
    dfAFT[is.na(dfAFT)] <- 0
    
    dfAFT2 <- dfAFT
    dfAFT2$Paramset <- NULL
    
    write.csv(dfAFT2, paste0(dirOut,"/production/",scenario,"/",AFT,".csv"), row.names = F)
    
  }
  
}


### agents ---------------------------------------------------------------------

# this folder contains behavioural files for each agent

dfBehaviour <- read.csv(paste0(dirData,"/BehaviourMaster.csv"))

dfBehaviour <- dfBehaviour %>% mutate(productionCsvFile = paste0(".//production/%s/",Agent,".csv"))
dfBehaviour$aftParamId <- 0
dfBehaviour <- dfBehaviour[,c(1:2,11,3:10)]

# Behavioural baseline
dfBaseline <- dfBehaviour %>% filter(Version == "behaviour_baseline")

for (i in 1:nrow(dfBaseline)){
  
  #i <- 1
  r1 <- dfBaseline[i,]
  r1$Version <- NULL
  Agent <- r1$Agent
  r1$Agent <- NULL
  write.csv(r1, paste0(dirOut,"/agents/behaviour_baseline/AftParams_",Agent,".csv"),row.names = FALSE)
  
}

# NOTE. do we need a behaviour set which makes agents more likely to give up every year?


### worlds ---------------------------------------------------------------------

# this is where capital files are held
# these describe the resources available to the agents and vary through time via updater csvs and by scenario (sub-folder structure)

# read in raw capital data (prepared in https://github.com/FR-LUES/OPM-model-prep-21-22.git)

sfCapitals_RAW <- st_read(paste0(dirData,"/01_Grid_capitals_raw.shp"))
head(sfCapitals_RAW)

# make sure all non greenspace has no values for any capital (except zone which has no effect anyway)
sfCapitals_RAW$Nature[which(sfCapitals_RAW$type == "Non.greenspace")] <- 0
sfCapitals_RAW$Access[which(sfCapitals_RAW$type == "Non.greenspace")] <- 0
sfCapitals_RAW$riskPerc[which(sfCapitals_RAW$type == "Non.greenspace")] <- 0
sfCapitals_RAW$WTP[which(sfCapitals_RAW$type == "Non.greenspace")] <- 0
sfCapitals_RAW$knowledge[which(sfCapitals_RAW$type == "Non.greenspace")] <- 0
sfCapitals_RAW$riskAreas[which(sfCapitals_RAW$type == "Non.greenspace")] <- 0

dfWorld <- sfCapitals_RAW %>%
  mutate(Long = st_coordinates(st_centroid(.))[,1], # get long & lat 
         Lat = st_coordinates(st_centroid(.))[,2],
         OPM_presence = 0) %>% # empty OPM presence capital column
  dplyr::select(GridID, Long, Lat, OPM_presence, Risk_perception = riskPerc, Willingness_to_pay = WTP, Knowledge = knowledge, Risk_map = riskAreas,
                Nature, Access, Zone) %>%  # select and rename columns
  st_drop_geometry() # drop geometry so no longer sf

# format coords for CRAFTY and save look-up
# unique coordinates 
# x_unq <- sort(unique(dfWorld$Long), decreasing = F)
# y_unq <- sort(unique(dfWorld$Lat), decreasing = F)
# 
# # ranks (later row/col id in CRAFTY)
# x_rnk <- 1:length(x_unq)
# y_rnk <- 1:length(y_unq)
# 
# # order of the grid coords
# x_ord <- match(dfWorld$Long, x_unq)
# y_ord <- match(dfWorld$Lat, y_unq)
# 
# dfWorld$X <- plyr::round_any(x_ord, 0.1)
# dfWorld$Y <- plyr::round_any(y_ord, 0.1)

# make coords regular - thanks to this https://stackoverflow.com/questions/60345163/how-to-generate-high-resolution-temperature-map-using-unevenly-spaced-coordinate
dfWorld <- dfWorld %>%  mutate(X = plyr::round_any(Long, 0.1),  
                               Y = plyr::round_any(Lat, 0.1))
# and smaller so CRAFTY can cope with them
xmin <- min(dfWorld$X)
ymin <- min(dfWorld$Y)
dfWorld$X <- dfWorld$X - xmin
dfWorld$Y <- dfWorld$Y - ymin
dfWorld$X <- dfWorld$X / 1000
dfWorld$Y <- dfWorld$Y / 1000

# unique coordinates 
x_unq <- sort(unique(dfWorld$X), decreasing = F)
y_unq <- sort(unique(dfWorld$Y), decreasing = F)

# ranks (later row/col id in CRAFTY)
x_rnk <- 1:length(x_unq)
y_rnk <- 1:length(y_unq)

# order of the grid coords
x_ord <- match(dfWorld$X, x_unq)
y_ord <- match(dfWorld$Y, y_unq)

dfWorld$X <- x_ord
dfWorld$Y <- y_ord

# save look-up
dfCoords <- dfWorld %>% dplyr::select(GridID,Long,Lat,X,Y)
write.csv(dfCoords, paste0(dirData,"/Cell_ID_XY_GreaterLondon.csv"), quote = F, row.names = F)

# order the capitals for CRAFTY 
dfWorld <- dfWorld %>% dplyr::select(x = X, y = Y, OPM_presence:Zone)

# normalise 0-1 (social capitals already 0-1 so don't need normalising)
summary(dfWorld)

dfWorld <- dfWorld %>% mutate(Risk_map = normalize(Risk_map, method = "range", range = c(0,1)),
                              Nature = normalize(Nature, method = "range", range = c(0,1)),
                              Access = normalize(Access, method = "range", range = c(0,1)))

dfWorld[is.na(dfWorld)] <- 0

dfWorld_long <- pivot_longer(dfWorld,
                             cols = Risk_perception:Access,
                             names_to = "capital",
                             values_to = "value")

ggplot(dfWorld_long)+
  geom_tile(aes(x,y,fill=value))+
  facet_wrap(~capital)+
  scale_fill_viridis()+
  theme_bw()

# agent locations (all no_mgmt to start)
dfWorld$FR <- "no_mgmt"
dfWorld$BT <- 0

# round so CRAFTY can deal with numbers
#dfWorld[,1:10] <- round(dfWorld[,1:10], digits = 1)

is.integer(dfWorld$x)
# dfWorld$x <- as.integer(dfWorld$x)
# dfWorld$y <- as.integer(dfWorld$y)

for (scenario in lstScenarios){
  
  #scenario <- lstScenarios[1]
  
  write.csv(dfWorld, paste0(dirOut,"/worlds/GreaterLondon/",scenario,"/GreaterLondon.csv"), row.names = FALSE)
  
}


### demand ---------------------------------------------------------------------

# societal demand for ecosystem services
# set constant to start with, then once CRAFTY is running use supply after 1 yr to set

Year <- seq(1,10, by=1)
biodiversity <- rep(1000, length(Year))
recreation <- rep(1000, length(Year))

dfDemand <- tibble(Year,biodiversity,recreation)
dfDemand

for (scenario in lstScenarios){
  
  #scenario <- lstScenarios[1]
  
  write.csv(dfDemand, paste0(dirOut,"/worlds/GreaterLondon/",scenario,"/Demand.csv"), row.names = FALSE)
  
}


### RangeShiftR set-up ---------------------------------------------------------

### habitat raster -------------------------------------------------------------

# habitat quality/suitability (produced in https://github.com/FR-LUES/OPM-model-prep-21-22/blob/main/scripts/01_process_raw_data.R)
sfHabitat <- st_read(paste0(dirData, "/01_Grid_RshiftR_habitat.shp"))

# plot
ggplot(sfHabitat)+
  geom_sf(aes(fill=habSuit),colour=NA)+
  scale_fill_viridis()+
  theme_bw()

st_crs(sfHabitat) # check crs

# rasterise
vectHabitat <- vect(sfHabitat)
rstExt <- rast(vectHabitat, resolution=100)
rstHabitat <- rasterize(vectHabitat, rstExt, "habSuit")
res(rstHabitat)
crs(rstHabitat) <- "EPSG:27700"

# convert from terra::rast to raster::raster
rstHabitat <- raster::raster(rstHabitat)

tm_shape(rstHabitat)+
  tm_raster()

# important to write to ascii and make sure NA flag is -9999 for RangeshiftR to accept the raster
writeRaster(rstHabitat, file.path(dirData, 'Habitat-100m.asc'), format="ascii", overwrite=TRUE, NAflag=-9999)



