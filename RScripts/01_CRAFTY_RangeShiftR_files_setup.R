
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
dirData <- file.path(wd, 'data-store')
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

dfAgents <- read.csv(paste0(dirOut,"/csv/AgentMaster.csv"))
head(dfAgents)

lstAgents <- unique(dfAgents$Agent)
lstScenarios <- c("baseline","de-regulation","govt-intervention","un-coupled")
lstParamsets <- c("with-social","no-social")

for (AFT in lstAgents){
  
  #AFT <- lstAgents[5]
  
  dfAFT <- filter(dfAgents, Agent == AFT)
  dfAFT$Agent <- NULL
  
  print(AFT)
  
  for (scenario in lstScenarios){
    
    #scenario <- lstScenarios[4]
    
    dfAFT2 <- filter(dfAFT, Scenario == scenario)
    #dfAFT$Paramset <- NULL
    
    # no NAs accepted by CRAFTY, make 0
    dfAFT2[is.na(dfAFT2)] <- 0
    
    print(dfAFT2)
    
    for (paramset in lstParamsets){
      
      #paramset <- lstParamsets[1]
      
      dfAFT3 <- filter(dfAFT2, Paramset == paramset)
      
      print(dfAFT3)
  
      dfAFT3$Scenario <- NULL
      dfAFT3$Paramset <- NULL
      
      # check dir exists and create if not
      filePath <- paste0(dirOut,"/production/",scenario,"-",paramset,"/")
      
      if (dir.exists(path = filePath)){
        print("Directory exists")
      }else{
        print("Directory created")
        dir.create(path = filePath)
      }
      
      write.csv(dfAFT3, paste0(dirOut,"/production/",scenario,"-",paramset,"/",AFT,".csv"), row.names = F)
      
    }
    
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

# NOTE. potential for a behaviour set which makes agents more likely to give up every year?

# check AgentColors.csv

AgentCols <- read.csv(paste0(dirOut,"/csv/AgentColors.csv"))
head(AgentCols)
colnames(AgentCols) <- c("Name","Color")
write.csv(AgentCols, paste0(dirOut,"/csv/AgentColors.csv"), row.names = FALSE)


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

# points version for extracting RangeShiftR results in coupled set-up
sfPoints <- st_as_sf(dfWorld[,1:3], coords = c("Long","Lat"), crs = 27700)
st_write(sfPoints, dsn = paste0(dirData,"/01_Grid_points.shp"), delete_layer=T)

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

# store raw data for changes per scenario
#dfWorld_raw <- dfWorld

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
  
  for (paramset in lstParamsets){
    
    filePath <- paste0(dirOut,"/worlds/GreaterLondon/",scenario,"-",paramset,"/")
    
    if (dir.exists(path = filePath)){
      print("Directory exists")
    }else{
      print("Directory created")
      dir.create(path = filePath)
    }
    
    write.csv(dfWorld, paste0(dirOut,"/worlds/GreaterLondon/",scenario,"-",paramset,"/GreaterLondon.csv"), row.names = FALSE)
    
  }
  
}

# updater files

n_years = 20

updaterFiles <- dfWorld[1:10]
head(updaterFiles)

#updaterFiles$OPMinverted <- 1
#updaterFiles$knowledge <- 0
head(updaterFiles)
summary(updaterFiles)

ticks <- c(1:n_years)  

for (scenario in lstScenarios){
  
  scenario <- "un-coupled"
  
  for (paramset in lstParamsets){
    
    for (i in ticks){
      
      #tick <- ticks[1]
      write.csv(updaterFiles, paste0(dirOut,"/worlds/GreaterLondon/",scenario,"-",paramset,"/GreaterLondon_tstep_",i,".csv") ,row.names = FALSE)
      
    }
    
  }
  
}



### make changes per scenario --------------------------------------------------


### de-regulation ###

# budget stays the same as baseline

# risk perception increases through time
# implement this in loop where OPM occurs?
# so essentially de-regulation scenario uses same baseline updaters, but will get edited as model runs
# for (i in ticks){
#   
#   #tick <- ticks[1]
#   write.csv(updaterFiles, paste0(dirOut,"/worlds/GreaterLondon/de-regulation-with-social/GreaterLondon_tstep_",i,".csv") ,row.names = FALSE)
#   write.csv(updaterFiles, paste0(dirOut,"/worlds/GreaterLondon/de-regulation-no-social/GreaterLondon_tstep_",i,".csv") ,row.names = FALSE)
#   
# }

# no updating of knowledge in loop either

### govt-intervention ###

# pull in type (for private residents) + increase their budget in the control zone
# check zone
ggplot(dfWorld)+
  geom_tile(aes(x,y,fill=Zone))+
  theme_bw()
# control zone = 2

sfCapitals_RAW <- st_read(paste0(dirData,"/01_Grid_capitals_raw.shp"))
head(sfCapitals_RAW)

# make sure all non greenspace has no values for any capital (except zone which has no effect anyway)
sfCapitals_RAW$Nature[which(sfCapitals_RAW$type == "Non.greenspace")] <- 0
sfCapitals_RAW$Access[which(sfCapitals_RAW$type == "Non.greenspace")] <- 0
sfCapitals_RAW$riskPerc[which(sfCapitals_RAW$type == "Non.greenspace")] <- 0
sfCapitals_RAW$WTP[which(sfCapitals_RAW$type == "Non.greenspace")] <- 0
sfCapitals_RAW$knowledge[which(sfCapitals_RAW$type == "Non.greenspace")] <- 0
sfCapitals_RAW$riskAreas[which(sfCapitals_RAW$type == "Non.greenspace")] <- 0

unique(sfCapitals_RAW$type)
sfCapitals_RAW$WTP[which(sfCapitals_RAW$type == "Private.garden" & sfCapitals_RAW$Zone == 2)] <- 1 

# process and write capital file & updaters
dfWorldInt <- sfCapitals_RAW %>%
  mutate(Long = st_coordinates(st_centroid(.))[,1], # get long & lat 
         Lat = st_coordinates(st_centroid(.))[,2],
         OPM_presence = 0) %>% # empty OPM presence capital column
  dplyr::select(GridID, Long, Lat, OPM_presence, Risk_perception = riskPerc, Willingness_to_pay = WTP, Knowledge = knowledge, Risk_map = riskAreas,
                Nature, Access, Zone) %>%  # select and rename columns
  st_drop_geometry() # drop geometry so no longer sf

# make coords regular - thanks to this https://stackoverflow.com/questions/60345163/how-to-generate-high-resolution-temperature-map-using-unevenly-spaced-coordinate
dfWorldInt <- dfWorldInt %>%  mutate(X = plyr::round_any(Long, 0.1),  
                               Y = plyr::round_any(Lat, 0.1))

# and smaller so CRAFTY can cope with them
xmin <- min(dfWorldInt$X)
ymin <- min(dfWorldInt$Y)
dfWorldInt$X <- dfWorldInt$X - xmin
dfWorldInt$Y <- dfWorldInt$Y - ymin
dfWorldInt$X <- dfWorldInt$X / 1000
dfWorldInt$Y <- dfWorldInt$Y / 1000

# unique coordinates 
x_unq <- sort(unique(dfWorldInt$X), decreasing = F)
y_unq <- sort(unique(dfWorldInt$Y), decreasing = F)

# ranks (later row/col id in CRAFTY)
x_rnk <- 1:length(x_unq)
y_rnk <- 1:length(y_unq)

# order of the grid coords
x_ord <- match(dfWorldInt$X, x_unq)
y_ord <- match(dfWorldInt$Y, y_unq)

dfWorldInt$X <- x_ord
dfWorldInt$Y <- y_ord

# order the capitals for CRAFTY 
dfWorldInt <- dfWorldInt %>% dplyr::select(x = X, y = Y, OPM_presence:Zone)

# normalise 0-1 (social capitals already 0-1 so don't need normalising)
summary(dfWorldInt)

dfWorldInt <- dfWorldInt %>% mutate(Risk_map = normalize(Risk_map, method = "range", range = c(0,1)),
                              Nature = normalize(Nature, method = "range", range = c(0,1)),
                              Access = normalize(Access, method = "range", range = c(0,1)))

dfWorldInt[is.na(dfWorldInt)] <- 0

dfWorldInt_long <- pivot_longer(dfWorldInt,
                             cols = Risk_perception:Access,
                             names_to = "capital",
                             values_to = "value")

ggplot(dfWorldInt_long)+
  geom_tile(aes(x,y,fill=value))+
  facet_wrap(~capital)+
  scale_fill_viridis()+
  theme_bw()

# agent locations (all no_mgmt to start)
dfWorldInt$FR <- "no_mgmt"
dfWorldInt$BT <- 0

is.integer(dfWorldInt$x)

write.csv(dfWorldInt, paste0(dirOut,"/worlds/GreaterLondon/govt-intervention-with-social/GreaterLondon.csv"), row.names = FALSE)
write.csv(dfWorldInt, paste0(dirOut,"/worlds/GreaterLondon/govt-intervention-no-social/GreaterLondon.csv"), row.names = FALSE)

# updater files

updaterFiles <- dfWorldInt[1:10]
head(updaterFiles)

#updaterFiles$OPMinverted <- 1
#updaterFiles$knowledge <- 0
head(updaterFiles)
summary(updaterFiles)

ticks <- c(1:n_years)  

for (i in ticks){
  
  write.csv(updaterFiles, paste0(dirOut,"/worlds/GreaterLondon/govt-intervention-with-social/GreaterLondon_tstep_",i,".csv") ,row.names = FALSE)
  write.csv(updaterFiles, paste0(dirOut,"/worlds/GreaterLondon/govt-intervention-no-social/GreaterLondon_tstep_",i,".csv") ,row.names = FALSE)
  
  }

# risk perception stays the same as the baseline
# knowledge updates in loop based on OPM presence (and private resident social network to be implemented)


### demand ---------------------------------------------------------------------

# societal demand for ecosystem services
# First step. Set/guess a value for constant demand to start with, 
# then once CRAFTY is running use supply after 1 yr to set (see below)

# Year <- seq(1,10, by=1)
# biodiversity <- rep(1000, length(Year))
# recreation <- rep(1000, length(Year))
# 
# dfDemand <- tibble(Year,biodiversity,recreation)
# dfDemand
# 
# for (scenario in lstScenarios){
#   
#   #scenario <- lstScenarios[1]
#   
#   write.csv(dfDemand, paste0(dirOut,"/worlds/GreaterLondon/",scenario,"/Demand.csv"), row.names = FALSE)
#   
# }
# 
# for (scenario in lstScenarios2){
#   
#   #scenario <- lstScenarios[1]
#   
#   write.csv(dfDemand, paste0(dirOut,"/worlds/GreaterLondon/",scenario,"/Demand.csv"), row.names = FALSE)
#   
# }

# once the models have been run, can use initial supply of services after 1 yr to set appropriate demand level

# Step 2. Read in a results file to get supply after 1 yr
dfSupply <- read.csv(paste0(wd,"/output/behaviour_baseline/baseline-with-social/baseline-with-social-0-99-GreaterLondon-AggregateServiceDemand.csv"))
head(dfSupply)

# get the values after 1 year
bio <- dfSupply$ServiceSupply.biodiversity[2]
rec <- dfSupply$ServiceSupply.recreation[2]

# increase by 20%
bio <- bio*1.2
rec <- rec*1.2

Year <- seq(1,n_years, by=1)
biodiversity <- rep(bio, length(Year))
recreation <- rep(rec, length(Year))

dfDemand <- tibble(Year,biodiversity,recreation)
dfDemand

for (scenario in lstScenarios){
  
  #scenario <- lstScenarios[1]
  for (paramset in lstParamsets){
    
    write.csv(dfDemand, paste0(dirOut,"/worlds/GreaterLondon/",scenario,"-",paramset,"/Demand.csv"), row.names = FALSE)
    
  }
  
}

# 0 demands for uncoupled scenario - should mean no agent response
# so service output will be only affected by OPM presence... in theory
bio <- 0
rec <- 0

Year <- seq(1,n_years, by=1)
biodiversity <- rep(bio, length(Year))
recreation <- rep(rec, length(Year))

dfDemand <- tibble(Year,biodiversity,recreation)
dfDemand

for (paramset in lstParamsets){
  
  write.csv(dfDemand, paste0(dirOut,"/worlds/GreaterLondon/un-coupled-",paramset,"/Demand.csv"), row.names = FALSE)
  
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



