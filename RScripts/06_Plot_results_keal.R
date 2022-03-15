
### author: VB
### date: 12/10/2021
### description: start code to plot results, hopefully useful for RShiny dev.

### packages required ----------------------------------------------------------

library(ggplot2)
library(tidyverse)
library(doSNOW)
library(sf)
library(viridis)
library(parallel)
#library(RangeShiftR)

### file paths -----------------------------------------------------------------

# currently Bumsuk is running the models on the cluster at KIT and sending outputs back  
# extracted using 7Zip to folder "from_KIT"

# dirCRAFTY <- "C:/Users/vanessa.burton.sb/Documents/eclipse-workspace/CRAFTY_RangeshiftR/"
# dataDrive <- "D:/CRAFTY_RangeShiftR_21-22_outputs/from_KIT/"

if (str_detect(Sys.info()["nodename"], "keal" )) { 
  
  dirCRAFTY <- "/pd/data/crafty/CRAFTY_RangeshiftR/"
  dataDrive <- "/pd/data/crafty/CRAFTY_RangeshiftR_21-22_outputs/"
  dirFig_root = dirCRAFTY
  
} else { 
   
  dirCRAFTY <- "/home/alan/git/CRAFTY_RangeshiftR/"
  dataDrive <- "/DATA4TB/CRAFTY_Rangeshifter_output_v16_30years/"
  dirFig_root = "~/Dropbox/"
}


dirOut <- dataDrive


agent.pal <- c("no_mgmt" = "#839192",
               "mgmt_remove" = "#7D3C98",
               "mgmt_pesticide" = "#E0D30F",
               "mgmt_fell" = "#A93226",
               "mgmt_nat" = "#138D75")


dirData <- file.path(dirCRAFTY, 'data-store')


prefs = c(
  "behaviour_scen1_00_09", "behaviour_scen2_00_08", "behaviour_scen3_01_10", "behaviour_scen4_01_09", "behaviour_scen5_01_08", "behaviour_scen6_02_10",
  "behaviour_scen7_02_09", "behaviour_scen8_02_08",  "behaviour_scen9_00_10"
)
prefs_todo = c(2,6,8,9)[3:4] #4 # Four behavioral experiments

lstScenarios <- c("baseline-with-social","baseline-no-social",
                  "de-regulation-with-social","de-regulation-no-social",
                  "govt-intervention-with-social","govt-intervention-no-social",
                  "un-coupled-with-social","un-coupled-no-social")[c(1,3,5,7)][c(1:3)] # ignore no-social and un-coupled scenarios



# Version (behavioural params, Rangeshifter params) 

batches_todo = c(2,3,6:9, 10, 12, 13) #3,6,8,10,13) #3, 6, 8, 10) # c(1:14))
# n_thread = length(prefs_todo)
n_thread = min(6, length(batches_todo))

cl = makeCluster(n_thread)


# Simulation period
n_years = 30



plotCRAFTY = TRUE


# read in greenspace types to plot % agents only by suitable habitat, not entire landscape area
sfGrid_geom <- st_read(paste0(dirCRAFTY,"/data-store/01_Grid_capitals_raw.shp"))
sfGrid <- sfGrid_geom %>% dplyr::select(GridID, type) %>% st_drop_geometry()

# read in suitable habitat
sfHabitat <- st_read(paste0(dirCRAFTY, "/data-store/01_Grid_RshiftR_habitat.shp")) %>% st_drop_geometry()

# read in coordinates

dfCoords = read.csv(paste0(dirData,"/Cell_ID_XY_GreaterLondon.csv"))


# cl = makeCluster(length(prefs_todo))
# registerDoSNOW(cl)


foreach(batch_prefix = batches_todo) %dopar% { 
  
  version = paste0("Batch_", batch_prefix)
  print(version)
  
  # for (pref_idx in pref_todo) { 
  foreach (pref_idx = prefs_todo, .packages = c("dplyr", "ggplot2", "tidyverse", "viridis", "sf", "doSNOW")) %do% {
    
    pref = prefs[pref_idx]
    
    out_pref = paste0("/", pref, "/")
    # scen_names = c("baseline-with-social", "baseline-no-social", "de-regulation-with-social",
    #                "de-regulation-no-social", "govt-intervention-with-social", "govt-intervention-no-social",
    #                "un-coupled-with-social", "un-coupled-no-social")
    #  
    
    
    # figure directory
    dirFigs <- paste0(dirFig_root,"figures", "/", version, "/", pref)
    
    if (!dir.exists(dirFigs)) {dir.create(dirFigs, recursive = T)}
    
    
    lstRsftrYrs <- sprintf("Sim%s",seq(1:n_years))
    
    
    
    sfGrid_rep <- as.data.frame(sapply(sfGrid, rep.int, times=n_years))
    
    sfHabitat_rep <- as.data.frame(sapply(sfHabitat, rep.int, times=n_years))
    
    if (plotCRAFTY) {
      
      ### CRAFTY results 
      
      dfMaster <- data.frame()
      
      foreach (idx = 1:length(lstScenarios)) %do% {
        
        scenario <- lstScenarios[idx]
        
        csv_path_tmp =  paste0(dirOut, "/", version, "/output/", out_pref,scenario,"/")
        
        csv_names_tmp = list.files(path = csv_path_tmp,
                                   pattern = "*.csv", 
                                   full.names = T) %>% 
          grep("-Cell-", value=TRUE, .) 
        
        print(csv_path_tmp)
        
        # to extract first n-year data only
        year_tmp = str_extract(csv_names_tmp, pattern = "(?<=Cell\\-)[0-9]+") # extract years
        csv_names_tmp = csv_names_tmp[year_tmp %in% seq(1:n_years)] # exclude years of non-interst
        
        
        
        dfResults <- csv_names_tmp %>% 
          #map_df(~read_csv(., col_types = cols(.default = "c")))
          map_df(~read.csv(.))
        
        
        
        
        # head(dfResults)
        # summary(dfResults)
        dfResults$Tick <- factor(dfResults$Tick)
        dfResults$Agent <- factor(dfResults$Agent, levels = names(agent.pal), labels = names(agent.pal))
        
        # inverted OPM presence capital 
        invert <- dfResults$Capital.OPM_presence - 1
        z <- abs(invert)
        dfResults$OPMpresence <- z
        
        # use habitat suitability to plot % agents only by suitable habitat, not entire landscape area
        # add greenspace type too
        dfResults$type <- sfGrid_rep$type
        dfResults$habSuit <- sfHabitat_rep$habSuit 
        
        # AFT map
        # nrow(sfGrid_geom)
        
        foreach (idx2 = c(1, seq(5,n_years,5)),  .packages = c("dplyr", "ggplot2", "tidyverse", "viridis", "sf")) %do% { 
          
          year <- lstRsftrYrs[idx2]
          
          dfResults_y_tmp = merge(dfResults[dfResults$Tick == idx2,], dfCoords, by.x = c("X", "Y"), by.y = c("X", "Y"))
          
          sfAFT_tmp = sfGrid_geom
          sfAFT_tmp$AFT = 0
          sfAFT_tmp[match( dfResults_y_tmp$GridID, sfAFT_tmp$GridID),]$AFT =  dfResults_y_tmp$Agent
          
          plotid = paste0( pref, "_", scenario, "_", year)
          
          
          # https://stackoverflow.com/questions/60802808/why-does-geom-sf-is-not-allowing-fill-with-discrete-column-from-a-dataframe
          # ggplot()+
          #   geom_sf(data = subset(DF, !is.na(ClusterGroup)), aes(fill = factor(ClusterGroup)))+
          #   theme_bw()+
          #   scale_fill_manual(values = c("red", "grey", "seagreen3","gold", "green","orange"), name= "Cluster Group")+ 
          #   theme(legend.position = "right")
          
          sfAFT_tmp$AFT = factor(sfAFT_tmp$AFT, levels = 1:length(agent.pal), labels = names(agent.pal))
          
          
          cat("plotting map: ",plotid)
          gAFT = ggplot(sfAFT_tmp[,]) + 
            labs(title= paste0("Year", idx2), caption = plotid) + # for the main title
            geom_sf(aes(fill= AFT), colour=NA)+
            theme_bw()+ 
            scale_fill_manual(values =agent.pal,   labels = paste0(names(agent.pal), " (", table(sfAFT_tmp$AFT), ")"
            ), name= "AFT") #+
          # scale_fill_viridis(discrete = F)+
          
          ggsave(gAFT, file=paste0(dirFigs, "/OPM_AFT_", pref, "_", scenario, "_", year, ".jpg"), width=14, height=10, dpi=300)
        }
        
        
        
        
        # bar plot agents 
        
        OPMSummary <- dfResults %>% 
          filter(habSuit > 0 & type != "Non.greenspace") %>% 
          group_by(Tick) %>% 
          summarise(n.tot = n(), # total number of greenspace squares suitable for OPM
                    n.opm = sum(OPMpresence)) %>%  # sum number of 100m suitable squares with OPM in
          mutate(perc.opm = n.opm/n.tot*100)
        
        agentSummary <- dfResults %>%
          filter(habSuit > 0 & type != "Non.greenspace") %>%  
          group_by(Tick,Agent) %>% 
          summarise(n.agents = n()) %>%  # number of agent types per yr
          mutate(perc.mgmt = n.agents/11808*100)
        
        p1a <-  OPMSummary %>% 
          ggplot()+
          geom_col(aes(x=Tick,y=perc.opm), fill = "gray20", position = "stack")+
          ggtitle("OPM coverage")+
          ylab("Percentage of suitable habitat (%)")+xlab("Year")+
          theme_bw()
        
        # plot(p1a)
        
        png(paste0(dirFigs,"/OPMcoverage_",scenario,".png"), units="cm", width = 12, height = 8, res=1000)
        print(p1a)
        dev.off()
        
        p1b <- agentSummary %>% 
          ggplot()+
          geom_col(aes(x=Tick,y=perc.mgmt, fill=Agent), position = "stack")+
          scale_fill_manual(values=agent.pal)+
          ggtitle("Management coverage")+
          ylab("Percentage of suitable habitat (%)")+xlab("Year")+
          theme_bw()
        
        # plot(p1b)
        
        png(paste0(dirFigs,"/agentBarPlot_",scenario,".png"), units="cm", width = 12, height = 8, res=1000)
        print(p1b)
        dev.off()
        
        # plot service provision through time 
        
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
          scale_x_continuous("Year",n.breaks = n_years)+
          theme_bw()
        
        png(paste0(dirFigs,"/servicesLinePlot_",scenario,".png"), units="cm", width = 12, height = 6, res=1000)
        print(p2)
        dev.off()
        
        
        # competitiveness 
        
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
          scale_x_continuous("Year",n.breaks = n_years)+
          theme_bw()
        
        # print(p3)
        
        png(paste0(dirFigs,"/compPlot_",scenario,".png"), units="cm", width = 12, height = 6, res=1000)
        print(p3)
        dev.off()
        
        # store in master df 
        
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
        scale_x_continuous("Year",n.breaks = n_years)+
        theme_bw()
      dev.off()
    }
    
    
    ### RangeShiftR results 
    
    dfPopsMaster <- data.frame()
    
    # read in RangeshiftR population results
    for (idx in 1:length(lstScenarios)){
      
      #scenario <- lstScenarios[2]
      scenario <- lstScenarios[idx]
      
      dirRsftr <- paste0(dirOut, version, "/output/", out_pref, scenario,"/Outputs/")
      
      #?readRange # this requires the simulation object as well as the file path (s in coupled script). Use output txt files instead
      
      # read in Range txt files
      for (idx2 in c(1, seq(5,n_years,5))){
        
        #year <- lstRsftrYrs[2]
        year <- lstRsftrYrs[idx2]
        
        fpath <- paste0(dirRsftr,"Batch1_",year,"_Land1_Range.txt")
        
        if(!file.exists(fpath)) {
          print(paste0(fpath, " does not exist."))
          next
        }
        
        dfRange <- read.delim2(fpath)
        
        dfRange <- dfRange %>% 
          filter(Year == 2) %>% # select just 2nd output "year" (RangeshiftR is run for 2 yrs per CRAFTY year - we take second yr as the result)
          # probably don't need to group and summarise now we only have one rep per timestep
          # so could comment out the next two lines?
          group_by(Year) %>% 
          summarise(NInds = mean(NInds)) %>% # average across reps
          #select(., Rep, NInds, Occup.Suit) %>% 
          mutate(Year = year,
                 Scenario = scenario)
        
        dfPopsMaster <- rbind(dfPopsMaster, dfRange[,])
        
        
        # fpath_IndIDs <- paste0(dirRsftr,"Batch1_",year,"_Land1_Rep0_Inds.txt")
        fpath_nInd <- paste0(dirRsftr,"Batch1_",year,"_Land1_Pop.txt")
        
        dfInd <- read.delim2(fpath_nInd)
        # table(dfInd$Year)
        # table(dfInd$RepSeason)
        # table(dfInd$Status)
        # sum(dfInd[dfInd$Year==2,]$NInd)
        
        # use the second output year
        dfInd_result = merge( dfInd[dfInd$Year==2,], dfCoords, by.x = c("x", "y"), by.y = c("X", "Y"))
        
        # nrow(sfGrid_geo)
        sfInd_tmp = sfGrid_geom
        sfInd_tmp$NInd = 0
        sfInd_tmp[match( dfInd_result$GridID, sfInd_tmp$GridID),]$NInd =  dfInd_result$NInd
        
        plotid = paste0( pref, "_", scenario, "_", year)
        
        
         cat("plotting map: ",plotid)
        gInd = ggplot(sfInd_tmp)+ 
          labs(title= paste0("Year", idx2), subtitle = paste0("Total number of OPM individuals =", dfRange$NInds), caption = plotid) + # for the main title
          
          geom_sf(aes(fill=NInd),colour=NA)+
          scale_fill_viridis()+
          theme_bw()    
        
        ggsave(gInd, file=paste0(dirFigs, "/OPM_NInds_", pref, "_", scenario, "_", year, ".jpg"), width=14, height=10, dpi=300)
        
        
        
        
        
      }
    }
    
    dfPopsMaster$Year <- factor(dfPopsMaster$Year, levels = lstRsftrYrs)
    
    g1 <- ggplot(data = dfPopsMaster, aes(x=Year,y=NInds, colour = Scenario, group = Scenario))+
      geom_point()+
      geom_line(position=position_dodge(width=0.2))+
      scale_color_brewer(palette = "Paired")+
      #facet_wrap(~Scenario)+
      theme_bw()
    
    # print(g1)
    
    ggsave(g1, file=paste0(dirFigs, "/OPM_pops_per_scenario.jpg"), width=16, height=8, dpi=300)
    
    
    
    
    
    
    
  }
}

stopCluster(cl)
