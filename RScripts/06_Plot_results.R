
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
# extracted using 7Zip to folder "from_KIT"

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

agent.pal <- c("no_mgmt" = "#839192",
               "mgmt_remove" = "#7D3C98",
               "mgmt_pesticide" = "#E0D30F",
               "mgmt_fell" = "#A93226",
               "mgmt_nat" = "#138D75")


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

g1 <- ggplot(data = dfPopsMaster, aes(x=Year,y=NInds, colour = Scenario, group = Scenario))+
  geom_point()+
  geom_line(position=position_dodge(width=0.2))+
  scale_color_brewer(palette = "Paired")+
  #facet_wrap(~Scenario)+
  theme_bw()

print(g1)

ggsave(g1, file=paste0(dirFigs, "/OPM_pops_per_scenario.jpg"), width=8, height=6, dpi=300)


### CRAFTY results -------------------------------------------------------------

dfMaster <- data.frame()

for (idx in 1:length(lstScenarios)){
  
  #scenario <- lstScenarios[1]
  scenario <- lstScenarios[idx]
  
  dfResults <-
    list.files(path = paste0(dirOut,"/behaviour_baseline/",scenario,"/"),
               pattern = "*.csv", 
               full.names = T) %>% 
    grep("-Cell-", value=TRUE, .) %>% 
    #map_df(~read_csv(., col_types = cols(.default = "c")))
    map_df(~read.csv(.))
  
  head(dfResults)
  summary(dfResults)
  dfResults$Tick <- factor(dfResults$Tick)
  dfResults$Agent <- factor(dfResults$Agent)
  
  # inverted OPM presence capital 
  invert <- dfResults$Capital.OPM_presence - 1
  z <- abs(invert)
  dfResults$OPMpresence <- z
  
  # bar plot agents --------------------------------------------------------------
  
  agentSummary <- dfResults %>% 
    group_by(Tick,Agent) %>% 
    summarise(agentCount = length(Agent)) %>% 
    ungroup() %>% 
    group_by(Tick) %>% 
    mutate(tot=sum(agentCount),
           perc=agentCount/tot*100)
  
  p1 <- agentSummary %>% 
    #filter(Agent != "no_mgmt") %>% 
    ggplot()+
    geom_col(aes(x=Tick,y=perc, fill=Agent), position = "stack")+
    scale_fill_manual(values=agent.pal)+
    ylab("Percentage of area (%)")+xlab("Year")+
    theme_bw()
  
  plot(p1)
  
  png(paste0(dirFigs,"/agentBarPlot_",scenario,".png"), units="cm", width = 12, height = 8, res=1000)
  print(p1)
  dev.off()
  
  # plot service provision through time ------------------------------------------
  
  serviceSummary <- dfResults %>% 
    group_by(Tick,Agent) %>% 
    #group_by(Tick) %>% 
    summarise(biodiversity = mean(Service.biodiversity),
              recreation = mean(Service.recreation)) %>% 
    pivot_longer(., cols=3:4, names_to="service",values_to="provision")
  
  serviceSummary$Tick <- as.numeric(as.character(serviceSummary$Tick))
  
  p2 <- serviceSummary %>% 
    ggplot()+
    geom_line(aes(x=Tick,y=provision,col=Agent))+
    scale_color_manual(values=agent.pal)+
    facet_wrap(~service)+
    ylim(c(0,1))+ylab("Service provision")+
    scale_x_continuous("Year",n.breaks = 10)+
    theme_bw()
  
  png(paste0(dirFigs,"/servicesLinePlot_",scenario,".png"), units="cm", width = 12, height = 6, res=1000)
  print(p2)
  dev.off()
  
  
  # competitiveness ------------------------------------------------------------
  
  compSummary <- dfResults %>% 
    group_by(Tick,Agent) %>% 
    #group_by(Tick) %>% 
    summarise(Competitiveness = mean(Competitiveness)) 
  
  compSummary$Tick <- as.numeric(as.character(compSummary$Tick))
  
  p3 <- compSummary %>% 
    ggplot()+
    geom_line(aes(x=Tick,y=Competitiveness,col=Agent))+
    scale_color_manual(values=agent.pal)+
    ylim(c(0,1))+ylab("Competitiveness")+
    scale_x_continuous("Year",n.breaks = 10)+
    theme_bw()
  
  print(p3)
  
  png(paste0(dirFigs,"/compPlot_",scenario,".png"), units="cm", width = 12, height = 6, res=1000)
  print(p3)
  dev.off()
  
  # store in master df ---------------------------------------------------------
  
  dfResults$Tick <- factor(dfResults$Tick)
  dfResults$Agent <- factor(dfResults$Agent)
  dfResults$scenario <- scenario
  
  dfMaster <- rbind(dfMaster,dfResults)
  
}

head(dfMaster)
dfMaster$scenario <- factor(dfMaster$scenario, ordered = T, levels=lstScenarios)
summary(dfMaster)

dfMaster$Tick <- as.numeric(dfMaster$Tick)

png(paste0(dirFigs,"/services_per_scenario.png"), units="cm", width = 16, height = 8, res=1000)
dfMaster %>% pivot_longer(cols = Service.biodiversity:Service.recreation,
                          names_to = "Benefit", values_to = "Value") %>% 
  group_by(scenario, Benefit, Tick) %>% 
  summarise(Value = mean(Value)) %>% 
  ggplot(aes(Tick,Value,col=scenario))+
  geom_line(lwd=0.8,position=position_dodge(width=0.2))+
  scale_color_brewer(palette = "Paired")+
  facet_wrap(~Benefit)+
  ylim(c(0,1))+ylab("Service level")+
  scale_x_continuous("Year",n.breaks = 10)+
  theme_bw()
dev.off()
