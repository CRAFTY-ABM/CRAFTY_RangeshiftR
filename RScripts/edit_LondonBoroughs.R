
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

### read in raw data -----------------------------------------------------------

# data produced in scripts:
# OPM_nature_access.R
# OPM_social_capitals.R
# rangeshiftR-OPM_sensitivity.R

OPMpresence <- read.csv(paste0(dirOut,"/capitals/hexG_rangeshiftR_test.csv"))
social <- st_read(paste0(dirOut,"/capitals/hexG_social_RAW.shp"))
natural <- st_read(paste0(dirOut,"/capitals/hexG_bio_access_RAW.shp"))

social <- social %>% st_drop_geometry()
capitalsRAW <- merge(natural,social,by="joinID")
head(capitalsRAW)
capitalsRAW <- capitalsRAW[,c(1,3,4,7,9,10)] %>% st_drop_geometry()

capitalsRAW$OPMpresence <- OPMpresence$rep0_year9[match(capitalsRAW$joinID, OPMpresence$joinID)]
# test making OPM presence completely binary for CRAFTY
capitalsRAW$OPMpresence[which(capitalsRAW$OPMpresence>0)]<-1
unique(capitalsRAW$OPMpresence)

capitalsRAW$riskPerc[which(is.na(capitalsRAW$riskPerc))] <- 0

capitalsRAW$knowledge <- NA
capitalsRAW$knowledge[which(capitalsRAW$OPMpresence>0)] <- 1
capitalsRAW$knowledge[which(is.na(capitalsRAW$knowledge))] <- 0

# inverted OPM presence capital 
invert <- capitalsRAW$OPMpresence - 1
z <- abs(invert)
capitalsRAW$OPMinv <- z

head(capitalsRAW)
summary(capitalsRAW)

# normalise --------------------------------------------------------------------

normalise <- function(x) {
  return ((x - min(x)) / (max(x) - min(x)))
}

capitalsNORM <- data.frame(capitalsRAW[1], lapply(capitalsRAW[2:8], normalise))
summary(capitalsNORM)

capitalsNORM %>% 
  pivot_longer(cols = nature:OPMinv, names_to = "capital", values_to = "value") %>% 
  ggplot()+
  geom_boxplot(aes(capital,value))

write.csv(capitalsNORM, paste0(dirOut, "/capitals/baseline_capitals_norm.csv"),row.names = F)


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
london$OPMpresence <- capitalsNORM$OPMpresence[match(london$joinID, capitalsNORM$joinID)]
london$OPMinverted <- capitalsNORM$OPMinv[match(london$joinID, capitalsNORM$joinID)]
london$riskPerc <- capitalsNORM$riskPerc[match(london$joinID, capitalsNORM$joinID)]
london$budget <- capitalsNORM$budget[match(london$joinID, capitalsNORM$joinID)]
london$OPMpresence <- capitalsNORM$OPMpresence[match(london$joinID, capitalsNORM$joinID)]
london$knowledge <- capitalsNORM$knowledge[match(london$joinID, capitalsNORM$joinID)]
london$nature <- capitalsNORM$nature[match(london$joinID, capitalsNORM$joinID)]
london$access <- capitalsNORM$access[match(london$joinID, capitalsNORM$joinID)]

# check
london %>% 
  pivot_longer(cols = c("OPMinverted","nature","access","riskPerc","budget","knowledge"), names_to = "capitals", values_to = "values") %>% 
  ggplot()+
  geom_tile(mapping = aes(x,y,fill=values))+
  facet_wrap(~capitals)+
  theme_bw()

write.csv(london, paste0(dirOut,"/capitals/london_baseline_capitals_norm.csv"))

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
london <- london[,c(6,7,8,1,9,2,3,4,5,10,11)]
summary(london)
head(london)

# remove OPMpresence
london$OPMpresence <- NULL

write.csv(london, sprintf("~/eclipse-workspace/CRAFTY_RangeshiftR/data_LondonOPM/worlds/LondonBoroughs/%s/LondonBoroughs_XY.csv", baseline), row.names = F)

### de-regulation changes ------------------------------------------------------

londonXY <- read.csv("~/eclipse-workspace/CRAFTY_RangeshiftR/data_LondonOPM/worlds/LondonBoroughs/LondonBoroughs_original.csv")
hx <- read.csv("~/eclipse-workspace/CRAFTY_RangeshiftR/data-processed/Cell_ID_XY_Borough.csv")
capitalsNORM <- read.csv(paste0(dirOut, "/capitals/baseline_capitals_norm.csv"))
colnames(londonXY)[3:4] = c("Lon", "Lat")
londonXY$x <- hx$X[match(londonXY$joinID, hx$Cell_ID)]
londonXY$y <- hx$Y[match(londonXY$joinID, hx$Cell_ID)]
londonXY$OPMinverted <- capitalsNORM$OPMinv[match(londonXY$joinID, capitalsNORM$joinID)]
londonXY$riskPerc <- capitalsNORM$riskPrc[match(londonXY$joinID, capitalsNORM$joinID)]
londonXY$budget <- capitalsNORM$budget[match(londonXY$joinID, capitalsNORM$joinID)]
londonXY$OPMpresence <- capitalsNORM$OPMpresence[match(londonXY$joinID, capitalsNORM$joinID)]
londonXY$knowledge <- capitalsNORM$knowledge[match(londonXY$joinID, capitalsNORM$joinID)]
londonXY$nature <- capitalsNORM$nature[match(londonXY$joinID, capitalsNORM$joinID)]
londonXY$access <- capitalsNORM$access[match(londonXY$joinID, capitalsNORM$joinID)]

head(londonXY)
londonXY$Agent <- NULL

# 1. define budget by each land owner type (replace simple borough levels atm)
# need to come up with simple rules
# e.g. private residents lowest budgets 0.2
# random scale for parks, 0.2, 0.6, 0.9
# all other types medium 0.5?

ggplot(londonXY)+
  geom_tile(mapping = aes(x,y,fill=budget))

head(social)
londonXY$type <- social$type[match(londonXY$joinID, social$joinID)]
londonXY$ownerID <- social$ownerID[match(londonXY$joinID, social$joinID)]

ggplot(londonXY)+
  geom_tile(mapping = aes(x,y,fill=type))

head(londonXY)
unique(londonXY$type)
unique(londonXY$ownerID[which(londonXY$type=="Public.park")])

# 2. increase risk perception through time (if it's low)
# 3. stop updating knowledge based on OPM presence

### govt-intervention changes --------------------------------------------------

# 1. create an artificial frontier zone (e.g. a single borough) and substantially increase budget in this area
# 2. keep risk perception at baseline
# 3. knowledge updates and spreads based on OPM presence



