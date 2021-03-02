
library(tidyverse)
library(raster)
library(sf)
library(ggplot2)

#wd <- "~/CRAFTY-opm"# sandbox VM
wd <- "~/eclipse-workspace/CRAFTY_RangeshiftR"# sandbox VM
dirOut <- file.path(wd, 'data-processed')
baseline <- "baseline"


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

write.csv(capitalsRAW, paste0(dirOut, "/capitals/baseline_capitals_raw.csv"),row.names = F)

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
capitalsNORM <- read.csv(paste0(dirOut, "/capitals/baseline_capitals_norm.csv"),)

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

# change budget to be by type same as scenarios
budgetBySite <- read.csv("~/eclipse-workspace/CRAFTY_RangeshiftR/data_LondonOPM/worlds/LondonBoroughs/de-regulation/LondonBoroughs_XY.csv")
summary(budgetBySite$budget)
summary(london$budget)
london$budget <- budgetBySite$budget

write.csv(london, sprintf("~/eclipse-workspace/CRAFTY_RangeshiftR/data_LondonOPM/worlds/LondonBoroughs/%s/LondonBoroughs_XY.csv", baseline), row.names = F)

# updater files
updater <- london[1:8]
tsteps <- seq(1,10,by=1)
for (i in tsteps){
  
  write.csv(updater, paste0("~/eclipse-workspace/CRAFTY_RangeshiftR/data_LondonOPM/worlds/LondonBoroughs/baseline/LondonBoroughs_XY_tstep_",i,".csv"), row.names = F)
  
}

### de-regulation changes ------------------------------------------------------

londonXY <- read.csv("~/eclipse-workspace/CRAFTY_RangeshiftR/data_LondonOPM/worlds/LondonBoroughs/LondonBoroughs_original.csv")
hx <- read.csv("~/eclipse-workspace/CRAFTY_RangeshiftR/data-processed/Cell_ID_XY_Borough.csv")
capitalsRAW <- read.csv(paste0(dirOut, "/capitals/baseline_capitals_raw.csv"))
colnames(londonXY)[3:4] = c("Lon", "Lat")
londonXY$x <- hx$X[match(londonXY$joinID, hx$Cell_ID)]
londonXY$y <- hx$Y[match(londonXY$joinID, hx$Cell_ID)]
londonXY$OPMinverted <- capitalsRAW$OPMinv[match(londonXY$joinID, capitalsRAW$joinID)]
londonXY$riskPerc <- capitalsRAW$riskPerc[match(londonXY$joinID, capitalsRAW$joinID)]
londonXY$budget <- capitalsRAW$budget[match(londonXY$joinID, capitalsRAW$joinID)]
londonXY$OPMpresence <- capitalsRAW$OPMpresence[match(londonXY$joinID, capitalsRAW$joinID)]
londonXY$knowledge <- capitalsRAW$knowledge[match(londonXY$joinID, capitalsRAW$joinID)]
londonXY$nature <- capitalsRAW$nature[match(londonXY$joinID, capitalsRAW$joinID)]
londonXY$access <- capitalsRAW$access[match(londonXY$joinID, capitalsRAW$joinID)]

head(londonXY)
londonXY$Agent <- NULL

# 1. define budget by each land owner type (replace simple borough levels atm)
# need to come up with simple rules
# e.g. private residents lowest budgets 0.2
# random scale for parks, 0.2, 0.6, 0.9
# all other types medium 0.5?

ggplot(londonXY)+
  geom_tile(mapping = aes(x,y,fill=budget))

londonXY$budget <- NA

head(social)
londonXY$type <- social$type[match(londonXY$joinID, social$joinID)]
londonXY$ownerID <- social$ownerID[match(londonXY$joinID, social$joinID)]

ggplot(londonXY)+
  geom_tile(mapping = aes(x,y,fill=type))

head(londonXY)
unique(londonXY$type)
#unique(londonXY$ownerID[which(londonXY$type=="Public.park")])

# parks 50% low budget = 0.2, 50% high budget = 0.9
parks <- filter(londonXY, grepl("park", ownerID))
parkIDs <- unique(parks$ownerID)
perc50 <- length(sample(parkIDs,(0.5*length(parkIDs)))) # 50% of parkIDs
parkIDhalf <- sample(parkIDs,perc50,replace=F) # randomly sample
index <- parkIDs %nin% parkIDhalf
parkIDhalf2 <- parkIDs[index==T] # extract the other half
# check
summary(parkIDhalf %in% parkIDhalf2) # all false so all good

londonXY$budget[which(londonXY$ownerID %in% parkIDhalf == T)] <- 0.2
londonXY$budget[which(londonXY$ownerID %in% parkIDhalf2 == T)] <- 0.9

# private residents low budget 0.2
londonXY$budget[which(londonXY$type=="Private.garden")] <- 0.2

# all other types (by cluster ID) medium 0.5
others <- filter(londonXY, grepl("schl|rlgs|inst|plysp|plyfd|othsp|ten|bwl|cmtry|altmt|amnrb|amnt", ownerID)) 
otherIDs <- unique(others$ownerID)

londonXY$budget[which(londonXY$ownerID %in% otherIDs == T)] <- 0.5

londonXY$budget[which(is.na(londonXY$budget))] <- 0
summary(londonXY$budget)

ggplot(londonXY)+
  geom_tile(mapping = aes(x,y,fill=budget))

summary(londonXY)

write.csv(londonXY, paste0(dirOut,"/capitals/de-reg_capitals_raw.csv"),row.names = F)

londonDEREG <- londonXY[,c(11,12,13,6:10)]
londonDEREG$FR <- "no_mgmt"
londonDEREG$BT <- 0
summary(londonDEREG)

# normalise
londonDEREGnrm <- data.frame(londonDEREG[1:2], lapply(londonDEREG[3:8], normalise), londonDEREG[9:10])
summary(londonDEREGnrm)

write.csv(londonDEREGnrm, "~/eclipse-workspace/CRAFTY_RangeshiftR/data_LondonOPM/worlds/LondonBoroughs/de-regulation/LondonBoroughs_XY.csv", row.names = F)

# 2. increase risk perception through time (if it's low) - apply to updater files

# tsteps 1 and 2
# keep same risk perc, assumining it will take a bit of time for risk perception to increase as OPM spread gets worse?
# normalise
updaterDRGnrm <- londonDEREGnrm[1:8]
write.csv(updaterDRGnrm,"~/eclipse-workspace/CRAFTY_RangeshiftR/data_LondonOPM/worlds/LondonBoroughs/de-regulation/LondonBoroughs_XY_tstep_1.csv", row.names = F)
write.csv(updaterDRGnrm,"~/eclipse-workspace/CRAFTY_RangeshiftR/data_LondonOPM/worlds/LondonBoroughs/de-regulation/LondonBoroughs_XY_tstep_2.csv", row.names = F)

# increase risk perception gradually at each subsequent timestep
# make changes to raw data, then re-normalise
updaterDRG <- londonDEREG[1:8]

ggplot(updaterDRG)+
  geom_tile(mapping = aes(x,y,fill=riskPerc))

summary(updaterDRG)

updaterDRG$riskPerc[which(updaterDRG$riskPerc>0 & updaterDRG$riskPerc<=0.5)] <- updaterDRG$riskPerc[which(updaterDRG$riskPerc>0 & updaterDRG$riskPerc<=0.5)]+0.05
updaterDRGnrm$riskPerc <- normalise(updaterDRG$riskPerc)
summary(updaterDRGnrm$riskPerc)
write.csv(updaterDRGnrm,"~/eclipse-workspace/CRAFTY_RangeshiftR/data_LondonOPM/worlds/LondonBoroughs/de-regulation/LondonBoroughs_XY_tstep_3.csv", row.names = F)

updaterDRG$riskPerc[which(updaterDRG$riskPerc<=0.5)] <- updaterDRG$riskPerc[which(updaterDRG$riskPerc<=0.5)]+0.05
updaterDRGnrm$riskPerc <- normalise(updaterDRG$riskPerc)
summary(updaterDRGnrm$riskPerc)
write.csv(updaterDRGnrm,"~/eclipse-workspace/CRAFTY_RangeshiftR/data_LondonOPM/worlds/LondonBoroughs/de-regulation/LondonBoroughs_XY_tstep_4.csv", row.names = F)

updaterDRG$riskPerc[which(updaterDRG$riskPerc<=0.5)] <- updaterDRG$riskPerc[which(updaterDRG$riskPerc<=0.5)]+0.05
updaterDRGnrm$riskPerc <- normalise(updaterDRG$riskPerc)
summary(updaterDRGnrm$riskPerc)
write.csv(updaterDRGnrm,"~/eclipse-workspace/CRAFTY_RangeshiftR/data_LondonOPM/worlds/LondonBoroughs/de-regulation/LondonBoroughs_XY_tstep_5.csv", row.names = F)

updaterDRG$riskPerc[which(updaterDRG$riskPerc<=0.5)] <- updaterDRG$riskPerc[which(updaterDRG$riskPerc<=0.5)]+0.05
updaterDRGnrm$riskPerc <- normalise(updaterDRG$riskPerc)
summary(updaterDRGnrm$riskPerc)
write.csv(updaterDRGnrm,"~/eclipse-workspace/CRAFTY_RangeshiftR/data_LondonOPM/worlds/LondonBoroughs/de-regulation/LondonBoroughs_XY_tstep_6.csv", row.names = F)

updaterDRG$riskPerc[which(updaterDRG$riskPerc<=0.7)] <- updaterDRG$riskPerc[which(updaterDRG$riskPerc<=0.7)]+0.05
updaterDRGnrm$riskPerc <- normalise(updaterDRG$riskPerc)
summary(updaterDRGnrm$riskPerc)
write.csv(updaterDRGnrm,"~/eclipse-workspace/CRAFTY_RangeshiftR/data_LondonOPM/worlds/LondonBoroughs/de-regulation/LondonBoroughs_XY_tstep_7.csv", row.names = F)

updaterDRG$riskPerc[which(updaterDRG$riskPerc<=0.7)] <- updaterDRG$riskPerc[which(updaterDRG$riskPerc<=0.7)]+0.05
updaterDRGnrm$riskPerc <- normalise(updaterDRG$riskPerc)
summary(updaterDRGnrm$riskPerc)
write.csv(updaterDRGnrm,"~/eclipse-workspace/CRAFTY_RangeshiftR/data_LondonOPM/worlds/LondonBoroughs/de-regulation/LondonBoroughs_XY_tstep_8.csv", row.names = F)

updaterDRG$riskPerc[which(updaterDRG$riskPerc<=0.7)] <- updaterDRG$riskPerc[which(updaterDRG$riskPerc<=0.7)]+0.1
updaterDRGnrm$riskPerc <- normalise(updaterDRG$riskPerc)
summary(updaterDRGnrm$riskPerc)
write.csv(updaterDRGnrm,"~/eclipse-workspace/CRAFTY_RangeshiftR/data_LondonOPM/worlds/LondonBoroughs/de-regulation/LondonBoroughs_XY_tstep_9.csv", row.names = F)

updaterDRG$riskPerc[which(updaterDRG$riskPerc<=0.7)] <- updaterDRG$riskPerc[which(updaterDRG$riskPerc<=0.7)]+0.2
updaterDRGnrm$riskPerc <- normalise(updaterDRG$riskPerc)
summary(updaterDRGnrm$riskPerc)
write.csv(updaterDRGnrm,"~/eclipse-workspace/CRAFTY_RangeshiftR/data_LondonOPM/worlds/LondonBoroughs/de-regulation/LondonBoroughs_XY_tstep_10.csv", row.names = F)

summary(updaterDRGnrm)
ggplot(updaterDRGnrm)+
  geom_tile(mapping = aes(x,y,fill=riskPerc))+theme_bw()

# 3. stop updating knowledge based on OPM presence
#(will need to implement this in CRAFTY-RangeshiftR loop)


### govt-intervention changes --------------------------------------------------

# same initial landowner dependent budget as de-regulation
londonGOVINTnrm <- read.csv("~/eclipse-workspace/CRAFTY_RangeshiftR/data_LondonOPM/worlds/LondonBoroughs/de-regulation/LondonBoroughs_XY.csv")
head(londonGOVINTnrm)
write.csv(londonGOVINTnrm, "~/eclipse-workspace/CRAFTY_RangeshiftR/data_LondonOPM/worlds/LondonBoroughs/govt-intervention/LondonBoroughs_XY.csv", row.names = F)

# updater format
londonGOVINTnrm <- londonGOVINTnrm[1:8]
# then edit raw data and re-normalise for updaters
londonGOVINT <- read.csv(paste0(dirOut,"/capitals/de-reg_capitals_raw.csv"))

# 1. create an artificial frontier zone (e.g. a single borough) and substantially increase budget in this area
londonGOVINT$borough <- social$borough[match(londonGOVINT$joinID, social$joinID)]
ggplot(londonGOVINT)+
  geom_tile(aes(x,y,fill=borough))

# create artificial frontier zone in camden & westminster
londonGOVINT$budget[which(londonGOVINT$borough=="camden"|londonGOVINT$borough=="westminster")] <- 1
londonGOVINTnrm$budget <- normalise(londonGOVINT$budget)
ggplot(londonGOVINTnrm)+
  geom_tile(aes(x,y,fill=budget))

tsteps <- seq(1,10,by=1)
for (i in tsteps){
  
  write.csv(londonGOVINTnrm, paste0("~/eclipse-workspace/CRAFTY_RangeshiftR/data_LondonOPM/worlds/LondonBoroughs/govt-intervention/LondonBoroughs_XY_tstep_",i,".csv"), row.names = F)
  
}

# 2. keep risk perception at baseline
# so no changes needed

# 3. knowledge updates and spreads based on OPM presence
# make sure this happens in CRAFTY-RangeshiftR loop
