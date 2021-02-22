
library(tidyverse)
library(raster)
library(sf)
library(ggplot2)

#wd <- "~/CRAFTY-opm"# sandbox VM
wd <- "~/eclipse-workspace/CRAFTY_RangeshiftR"# sandbox VM
dirOut <- file.path(wd, 'data-processed')
baseline <- "baseline"
scenario <- "de-regulation"

# note. can skip to l.121 to read in baseline capitals and change from there.

### read in 'raw' data -----------------------------------------------------------

# data produced in scripts:
# OPM_nature_access.R
# OPM_social_capitals.R
# rangeshiftR-OPM_sensitivity.R

OPMpresence <- read.csv(paste0(dirOut,"/capitals/hexG_rangeshiftR_test.csv"))
social <- st_read(paste0(dirOut,"/capitals/hexG_social.shp"))
natural <- st_read(paste0(dirOut,"/capitals/hexG_bio_access.shp"))

social <- social %>% st_drop_geometry()
capitals <- merge(natural,social,by="joinID")
head(capitals)
capitals <- capitals[,c(1,6,8,2,3)] %>% st_drop_geometry()

capitals$OPMpresence <- OPMpresence$rep0_year9[match(capitals$joinID, OPMpresence$joinID)]
# test making OPM presence completely binary for CRAFTY
capitals$OPMpresence[which(capitals$OPMpresence>0)]<-1
unique(capitals$OPMpresence)

capitals$riskPrc[which(is.na(capitals$riskPrc))] <- 0

capitals$knowledge <- NA
capitals$knowledge[which(capitals$OPMpresence>0)] <- 1
capitals$knowledge[which(is.na(capitals$knowledge))] <- 0

head(capitals)

# normalise --------------------------------------------------------------------
summary(capitals)
# only need to normalise nature, rest are already 0-1

# commented out old code from when using OPM population values directly - don't need to do this now testing binary 0/1 OPM
# add step here to normalised/standardise using max carrying capacity
# otherwise normalising rangeshiftR populations in every run of CRAFTY_RangeshiftR will mess things up
# OPM presence
#data <- capitals$OPMpresence
#data[which(data==0)]<-NA
#normalised <- (data-min(data,na.rm = T))/(max(data, na.rm = T)-min(data,na.rm=T))
#hist(data)
#hist(normalised)
#normalised[which(is.na(normalised))]<-0
#capitals$OPMpresence <- normalised

# inverted OPM presence capital 
invert <- capitals$OPMpresence - 1
z <- abs(invert)
capitals$OPMinv <- z

# nature
data <- capitals$nature
normalised <- (data-min(data,na.rm = T))/(max(data, na.rm = T)-min(data,na.rm=T))
hist(data)
hist(normalised)
summary(normalised)
normalised[which(is.na(normalised))]<-0
capitals$nature <- normalised


# join to CRAFTY coords ---------------------------------------------------------
# original LondonBoroughs file and cellIDs for matching to CRAFTY coordinates
london <- read.csv("~/eclipse-workspace/CRAFTY_RangeshiftR/data_LondonOPM/worlds/LondonBoroughs/LondonBoroughs_original.csv")
hx <- read.csv("~/eclipse-workspace/CRAFTY_RangeshiftR/data-processed/Cell_ID_XY_Borough.csv")

head(london)
colnames(london)[3:4] = c("Lon", "Lat")
london$x <- hx$X[match(london$joinID, hx$Cell_ID)]
london$y <- hx$Y[match(london$joinID, hx$Cell_ID)]

# create look-up file
head(london)
lookUp <- london[,c(2,12,13)]
#write.csv(lookUp,"~/eclipse-workspace/CRAFTY_RangeshiftR/data-processed/joinID_lookup.csv", row.names = F)

head(london)
london$OPMpresence <- capitals$OPMpresence[match(london$joinID, capitals$joinID)]
london$OPMinverted <- capitals$OPMinv[match(london$joinID, capitals$joinID)]
london$riskPerc <- capitals$riskPrc[match(london$joinID, capitals$joinID)]
london$budget <- capitals$budget[match(london$joinID, capitals$joinID)]
london$OPMpresence <- capitals$OPMpresence[match(london$joinID, capitals$joinID)]
london$knowledge <- capitals$knowledge[match(london$joinID, capitals$joinID)]
london$nature <- capitals$nature[match(london$joinID, capitals$joinID)]
london$access <- capitals$access[match(london$joinID, capitals$joinID)]

# check
ggplot(london)+
  geom_tile(mapping = aes(x,y,fill=OPMinverted))

### baseline -------------------------------------------------------------------

# tidy up
london$id <- NULL
london$joinID <- NULL
london$Lon <- NULL
london$Lat <- NULL
london$FR <- "no_mgmt"
london$Agent <- NULL
london$BT <- 0

head(london)
london <- london[,c(7,8,1,9,2,3,4,5,6,10,11)]
summary(london)
head(london)

# remove OPMpresence
london$OPMpresence <- NULL

write.csv(london, sprintf("~/eclipse-workspace/CRAFTY_RangeshiftR/data_LondonOPM/worlds/LondonBoroughs/%s/LondonBoroughs_XY.csv", baseline), row.names = F)

### de-regulation changes ------------------------------------------------------

london <- read.csv("~/eclipse-workspace/CRAFTY_RangeshiftR/data_LondonOPM/worlds/LondonBoroughs/LondonBoroughs_original.csv")
hx <- read.csv("~/eclipse-workspace/CRAFTY_RangeshiftR/data-processed/Cell_ID_XY_Borough.csv")
colnames(london)[3:4] = c("Lon", "Lat")
london$x <- hx$X[match(london$joinID, hx$Cell_ID)]
london$y <- hx$Y[match(london$joinID, hx$Cell_ID)]
london$OPMpresence <- capitals$OPMpresence[match(london$joinID, capitals$joinID)]
london$OPMinverted <- capitals$OPMinv[match(london$joinID, capitals$joinID)]
london$riskPerc <- capitals$riskPrc[match(london$joinID, capitals$joinID)]
london$budget <- capitals$budget[match(london$joinID, capitals$joinID)]
london$OPMpresence <- capitals$OPMpresence[match(london$joinID, capitals$joinID)]
london$knowledge <- capitals$knowledge[match(london$joinID, capitals$joinID)]
london$nature <- capitals$nature[match(london$joinID, capitals$joinID)]
london$access <- capitals$access[match(london$joinID, capitals$joinID)]

# 1. define budget by each land owner type (replace simple borough levels atm)
# need to come up with simple rules
# e.g. private residents lowest budgets 0.2
# random scale for parks, 0.2, 0.6, 0.9
# all other types medium 0.5?

ggplot(london)+
  geom_tile(mapping = aes(x,y,fill=budget))

head(social)
london$type <- social$type[match(london$joinID, social$joinID)]
london$ownerID <- social$ownerID[match(london$joinID, social$joinID)]

ggplot(london)+
  geom_tile(mapping = aes(x,y,fill=type))

head(london)
unique(london$type)
unique(london$ownerID[which(london$type=="Public.park")])

# 2. increase risk perception through time (if it's low)
# 3. stop updating knowledge based on OPM presence

### govt-intervention changes --------------------------------------------------

# 1. create an artificial frontier zone (e.g. a single borough) and substantially increase budget in this area
# 2. keep risk perception at baseline
# 3. knowledge updates and spreads based on OPM presence



