
# 21/01/21
# having issues with RangeShiftR results a few ticks into coupled models
# so test separately here with same parameters

### directories ----------------------------------------------------------------

if (Sys.info()["user"] %in% c("alan", "seo-b")) { 
  dirWorking<- "~/git/CRAFTY_RangeshiftR"
  
} else { 
  dirWorking<- "~/eclipse-workspace/CRAFTY_RangeshiftR"
}

# store RangeshiftR files within CRAFTY output folder as it is the directory CRAFTY will need to run in
dirRsftr <- file.path(dirCRAFTYOutput, 'RangeshiftR')
# specific file structure needed for RangeshiftR to run
dirRsftrInput <- file.path(dirRsftr,"Inputs")
dirRsftrOutput <- file.path(dirRsftr,"Outputs")
dirRsftrOutputMaps <- file.path(dirRsftr,"Output_Maps")
# important
# need to add the / for this path to work in RunRS()
dirRsftr <- paste0(dirRsftr,"/") 

setwd(dirWorking)

### parameter set-up -----------------------------------------------------------

rangeshiftrYears <- 10
rstHabitat <- raster(file.path(dirRsftrInput, 'Habitat-100m.tif'))
# make sure BNG
hexPoints <- st_read(paste0(dirWorking,"/data-processed/hexgrids/hexPoints40m.shp"))
rstHabitat <- projectRaster(rstHabitat, crs = crs(hexPoints))
st_crs(rstHabitat)
habitatRes <- 100

init <- Initialise(InitType=2, InitIndsFile='initial_inds_2014_n10.txt')

land <- ImportedLandscape(LandscapeFile=sprintf('Habitat-%sm.asc', habitatRes),
                          Resolution=habitatRes,
                          HabPercent=TRUE,
                          K_or_DensDep=50) # carrying capacity (individuals per hectare) when habitat at 100% quality

demo <- Demography(Rmax = 25,
                   ReproductionType = 0) # 0 = asexual / only female; 1 = simple sexual; 2 = sexual model with explicit mating system

disp <-  Dispersal(Emigration = Emigration(EmigProb = 0.2),
                   Transfer   = DispersalKernel(Distances = 1500), # test getting to top of landscape while keeping other params low
                   Settlement = Settlement() )

sim <- Simulation(Simulation = 999, # 999 to make sure test simulation is obvious in results folder
                  Years = rangeshiftrYears,
                  Replicates = 1,
                  OutIntPop = 1,
                  OutIntInd = 1,
                  ReturnPopRaster=TRUE)


### run RangeShiftR ------------------------------------------------------------

s <- RSsim(simul = sim, land = land, demog = demo, dispersal = disp, init = init)
validateRSparams(s)
result <- RunRS(s, sprintf('%s/', dirpath = dirRsftr))
crs(result) <- crs(rstHabitat)
extent(result) <- extent(rstHabitat)
#result[[1]]
spplot(result)
#spplot(result[[-1]])

# plot abundance and occupancy
range_df <- readRange(s, dirRsftr)
# ...with replicates:
par(mfrow=c(1,2))
plotAbundance(range_df)
plotOccupancy(range_df)
dev.off()

# completely fine running on it's own, so it must be a mistake in the coupling...
# maybe due to how new individual files are being edited/written?

