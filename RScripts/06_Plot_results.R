
### author: VB
### date: 12/10/2021
### description: start code to plot results, hopefully useful for RShiny dev.

### packages required ----------------------------------------------------------

library(tidyverse)
#library(foreach)
library(sf)
library(viridis)
library(RangeShiftR)

### file paths -----------------------------------------------------------------

# currently Bumsuk is running the models on the cluster at KIT and sending outputs back as tar.gz
# command line code for extracting tar.gz files
# tar -zxvf CRAFTY_RangeshiftR_21-22_outputs.tar.gz -C "D:\CRAFTY_RangeShiftR_21-22_outputs # this was very buggy & didn't extract properly
# extracted usinf 7Zip to folder "from_KIT"

dirCRAFTY <- "C:/Users/vanessa.burton.sb/Documents/eclipse-workspace/CRAFTY_RangeshiftR/"
dataDrive <- "D:/CRAFTY_RangeShiftR_21-22_outputs/"

#dirOut <- paste0(dataDrive, "from_KIT/output")
dirOut <- paste0(dataDrive, "output")

# figure directory
dirFigs <- paste0(dirCRAFTY,"figures")

lstScenarios <- c("baseline-with-social","baseline-no-social",
                  "de-regulation-with-social","de-regulation-no-social",
                  "govt-intervention-with-social","govt-intervention-no-social")#,
                  #"un-coupled-with-social","un-coupled-no-social")

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

ggplot(dfPopsMaster)+
  geom_point(aes(x=Year,y=NInds, colour = Scenario))+
  theme_bw()
