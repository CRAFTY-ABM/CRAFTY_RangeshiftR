
### author: VB
### date: 12/10/2021
### description: start code to plot results, hopefully useful for RShiny dev.

### packages required ----------------------------------------------------------

library(tidyverse)
#library(foreach)
library(sf)
library(viridis)
#library(RangeShiftR)

### file paths -----------------------------------------------------------------

# currently Bumsuk is running the models on the cluster at KIT and sending outputs back  
# extracted usinf 7Zip to folder "from_KIT"

dirCRAFTY <- "C:/Users/vanessa.burton.sb/Documents/eclipse-workspace/CRAFTY_RangeshiftR/"
dataDrive <- "D:/CRAFTY_RangeShiftR_21-22_outputs/from_KIT/"

#dirOut <- paste0(dataDrive, "from_KIT/output")
dirOut <- paste0(dataDrive, "output")

# figure directory
dirFigs <- paste0(dirCRAFTY,"figures")

lstScenarios <- c("baseline-with-social","baseline-no-social",
                  "de-regulation-with-social","de-regulation-no-social",
                  "govt-intervention-with-social","govt-intervention-no-social",
                  "un-coupled-with-social","un-coupled-no-social")

lstRsftrYrs <- sprintf("Sim%s",seq(1:10))


### RangeShiftR results --------------------------------------------------------

dfPopsMaster <- data.frame()

# read in RangeshiftR population results
for (idx in 1:length(lstScenarios)){
  
  #scenario <- lstScenarios[1]
  scenario <- lstScenarios[idx]
  
  dirRsftr <- paste0(dirOut, "/behaviour_baseline/", scenario,"/Outputs/")
  
  #?readRange # this requires the simulation object as well as the file path (s in coupled script). Use output txt files instead
  
  # read in Range txt files
  for (idx2 in 1:length(lstRsftrYrs)){
    
    #year <- lstRsftrYrs[1]
    year <- lstRsftrYrs[idx2]
    
    path <- paste0(dirRsftr,"Batch1_",year,"_Land1_Range.txt")
      
    if(!file.exists(path)) {
        next
    }
    
    dfRange <- read.delim2(paste0(dirRsftr,"Batch1_",year,"_Land1_Range.txt"))
      
    dfRange <- dfRange %>% 
        filter(Year == 2) %>% # select just 2nd output "year" (RangeshiftR is run for 2 yrs per CRAFTY year - we take second yr as the result)
        group_by(Year) %>% 
        summarise(NInds = mean(NInds)) %>% # average across reps
        #select(., Rep, NInds, Occup.Suit) %>% 
        mutate(Year = year,
               Scenario = scenario)
      
      dfPopsMaster <- rbind(dfPopsMaster, dfRange[,])
      
  }
  }

dfPopsMaster$Year <- factor(dfPopsMaster$Year, levels = lstRsftrYrs)

ggplot(data = dfPopsMaster, aes(x=Year,y=NInds, colour = Scenario, group = Scenario))+
  geom_point()+
  geom_line()+
  facet_wrap(~Scenario)+
  theme_bw()


# rasters
library(raster)

rstBaseline <- stack(paste0(dataDrive,"/rstRangeshiftR_output_coupled_baseline-with-social.tif"))
rstDereg <- stack(paste0(dataDrive,"/rstRangeshiftR_output_coupled_de-regulation-with-social.tif"))
rstGovt <- stack(paste0(dataDrive,"/rstRangeshiftR_output_coupled_govt-intervention-with-social.tif"))

names(rstBaseline) <- c("Yr1","Yr2","Yr3","Yr4","Yr5","Yr6","Yr7","Yr8","Yr9","Yr10")
clrs.viridis <- colorRampPalette(viridis::viridis(10))
#png(paste0(dirFigs,"/rsftr_pops_CRAFTY-coupled_baseline.png"), width = 800, height = 600)
spplot(rstBaseline, layout = c(5,2))#, col.regions=clrs.viridis(14), at = seq(0,70,10))
#dev.off()

names(rstDereg) <- c("Yr1","Yr2","Yr3","Yr4","Yr5","Yr6","Yr7","Yr8","Yr9","Yr10")
clrs.viridis <- colorRampPalette(viridis::viridis(10))
#png(paste0(dirFigs,"/rsftr_pops_CRAFTY-coupled_de-regulation.png"), width = 800, height = 600)
spplot(rstDereg, layout = c(5,2))#, col.regions=clrs.viridis(14), at = seq(0,70,10))
#dev.off()

names(rstGovt) <- c("Yr1","Yr2","Yr3","Yr4","Yr5","Yr6","Yr7","Yr8","Yr9","Yr10")
clrs.viridis <- colorRampPalette(viridis::viridis(10))
#png(paste0(dirFigs,"/rsftr_pops_CRAFTY-coupled_govt-intervention.png"), width = 800, height = 600)
spplot(rstGovt, layout = c(5,2))#, col.regions=clrs.viridis(14), at = seq(0,70,10))
#dev.off()