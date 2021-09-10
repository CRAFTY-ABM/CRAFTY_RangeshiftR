
# Date: 01/09/21
# Author: VB
# Description: Run RangeShiftR standalone to test parameter set-up

library(RangeShiftR)
library(raster)
library(sf)
library(viridis)
library(ggplot2)

### directories ----------------------------------------------------------------

dirWorking<- "~/eclipse-workspace/CRAFTY_RangeshiftR/"

dirFigs <- paste0(dirWorking, "figures/")

dirData <- file.path(dirWorking, 'data-processed')

dirCRAFTYInput <- path.expand(paste0(dirWorking, "data_LondonOPM/"))
dirCRAFTYOutput <- path.expand(paste0(dirWorking, "output"))

# store RangeshiftR files within CRAFTY output folder as it is the directory CRAFTY will need to run in
dirRsftr <- file.path(dirCRAFTYOutput, 'RangeShiftR_standalone')
#dir.create(dirRsftr)

# specific file structure needed for RangeshiftR to run
dirRsftrInput <- file.path(dirRsftr,"Inputs")#; dir.create(dirRsftrInput)
dirRsftrOutput <- file.path(dirRsftr,"Outputs")#; dir.create(dirRsftrOutput)
dirRsftrOutputMaps <- file.path(dirRsftr,"Output_Maps")#; dir.create(dirRsftrOutputMaps)
# important
# need to add the / for this path to work in RunRS()
dirRsftr <- paste0(dirRsftr,"/") 

setwd(dirWorking)

### parameter set-up -----------------------------------------------------------

# number of years to run
rangeshiftrYears <- 10

# habitat data
ascHabitat <- raster(file.path(dirData, 'Habitat-100m.asc'))
crs(ascHabitat)
# make sure BNG
sfGrid <- st_read(paste0(dirData,"/01_Grid_capitals_raw.shp")) %>% dplyr::select(GridID, geometry)
st_crs(sfGrid)
# extract centroid points, make spatial points dataframe
spGrid <- as_Spatial(st_centroid(sfGrid))

crs(ascHabitat) <- crs(spGrid)
st_crs(ascHabitat)
spplot(ascHabitat)

# write to RangeShiftR input folder
writeRaster(ascHabitat, file.path(dirRsftrInput, 'Habitat-100m.asc'), format="ascii", overwrite=TRUE, NAflag=-9999)

# read in initial individuals file (must be tab delimited)
dfInitialIndividuals <- read.table(paste0(dirData,"/01_initial_inds_2012.txt"), sep = '\t', header = TRUE)
# write to RangeShiftR input folder
write.table(dfInitialIndividuals, file.path(dirRsftrInput, '01_initial_inds_2012.txt'), row.names = F, quote = F, sep = '\t')


# set up objects required for RangeShiftR

land <- ImportedLandscape(LandscapeFile='Habitat-100m.asc',
                          Resolution=100,
                          HabPercent=TRUE,
                          K_or_DensDep=1000) # carrying capacity (individuals per hectare) when habitat at 100% quality

demo <- Demography(Rmax = 25,
                   ReproductionType = 0) # 0 = asexual / only female; 1 = simple sexual; 2 = sexual model with explicit mating system

disp <-  Dispersal(Emigration = Emigration(EmigProb = 0.2),
                   Transfer = DispersalKernel(Distances = 500), 
                   Settlement = Settlement())

# need to work out how to set second, less likely dispersal kernel of up to
?Dispersal
?DispersalKernel
# do we want mixed kernel with inter-individual variation?
# this would need vals/cols for: mean(kernel1), sd(kernel1), mean(kernel2), sd(kernel2), mean(probability), sd(probability)
# just double kernel - mean(kernel1),mean(kernel2), probablity(kernel1)
dists <- matrix(c(500,7300,0.95),ncol = 3)

disp <-  Dispersal(Emigration = Emigration(EmigProb = 0.2),
                   Transfer = DispersalKernel(Distances = dists, DoubleKernel = TRUE), 
                   Settlement = Settlement())

### run RangeShiftR 10 yrs------------------------------------------------------

init <- Initialise(InitType=2, InitIndsFile='01_initial_inds_2012.txt')

sim <- Simulation(Simulation = 999, # 999 to make sure test simulation is obvious in results folder
                  Years = rangeshiftrYears,
                  Replicates = 1,
                  OutIntPop = 1,
                  OutIntInd = 1,
                  ReturnPopRaster=TRUE)

s <- RSsim(simul = sim, land = land, demog = demo, dispersal = disp, init = init, seed = 261090)

validateRSparams(s)

result10yr <- RunRS(s, sprintf('%s', dirpath = dirRsftr))

crs(result10yr) <- crs(ascHabitat)

extent(result10yr) <- extent(ascHabitat)

# plot
#spplot(result10yr)
names(result10yr) <- c("Yr1","Yr2","Yr3","Yr4","Yr5","Yr6","Yr7","Yr8","Yr9","Yr10")
clrs.viridis <- colorRampPalette(viridis::viridis(10))

#png(paste0(dirFigs,"/rsftr_pops_10yr_standalone.png"), width = 800, height = 600)
spplot(result10yr, layout = c(5,2), col.regions=clrs.viridis(14), at = seq(0,70,10))
#dev.off()

# plot abundance and occupancy
dfRange10yr <- readRange(s, dirRsftr)
# ...with replicates:
par(mfrow=c(1,2))
plotAbundance(dfRange10yr)
plotOccupancy(dfRange10yr)
dev.off()

