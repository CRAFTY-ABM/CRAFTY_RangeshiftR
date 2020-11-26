
wd <- "~/CRAFTY-opm"# sandbox VM
dirOut <- file.path(wd, 'data-processed')

london <- read_csv("~/eclipse-workspace/CRAFTY_RangeshiftR/data_LondonOPM/worlds/LondonBoroughs/LondonBoroughs_original.csv")
hx <- read_csv("~/eclipse-workspace/CRAFTY_RangeshiftR/data-processed/Cell_ID_XY_Borough.csv")
hexGrid <- read.csv(paste0(dirOut,"/hexGrids/hexG_rangeshiftR_test.csv"))

colnames(london)[3:4] = c("Lon", "Lat")
london$x  = hx$X[match(london$joinID, hx$Cell_ID)]
london$y  = hx$Y[match(london$joinID, hx$Cell_ID)]

london$OPMpresence <- hexGrid$rep0_year9[match(london$joinID, hexGrid$joinID)]

london$id = NULL
london$joinID = NULL
london$Lon = NULL
london$Lat = NULL
london$FR = london$Agent 
london$Agent = NULL
london$BT = 0

# check
ggplot(london)+
  geom_tile(mapping = aes(x,y,fill=OPMpresence))

summary(london)
# normalise OPM presence 0-1
data <- london$OPMpresence
data[which(data==0)]<-NA
normalised <- (data-min(data,na.rm = T))/(max(data, na.rm = T)-min(data,na.rm=T))
hist(data)
hist(normalised)
normalised[which(is.na(normalised))]<-0
london$OPMpresence <- normalised

london$knowledge[which(london$OPMpresence>0)] <- 1

# normalise nature
data <- london$nature
summary(data)
data[which(data==0)]<-NA
normalised <- (data-min(data,na.rm = T))/(max(data, na.rm = T)-min(data,na.rm=T))
hist(data)
hist(normalised)
normalised[which(is.na(normalised))]<-0
london$nature <- normalised

summary(london)

write.csv(london, "~/eclipse-workspace/CRAFTY_RangeshiftR/data_LondonOPM/worlds/LondonBoroughs/LondonBoroughs_XY.csv")
