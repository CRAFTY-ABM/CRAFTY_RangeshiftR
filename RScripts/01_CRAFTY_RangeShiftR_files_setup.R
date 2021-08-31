
# date: 31/08/2021
# author: VB
# purpose: edit or create all files required for CRAFTY

### libs -----------------------------------------------------------------------

library(tidyverse)
library(sf)
library(raster)

### paths ----------------------------------------------------------------------

wd <- "~/eclipse-workspace/CRAFTY_RangeshiftR" # sandbox VM
dirData <- file.path(wd, 'data-processed')
dirOut <- file.path(wd, 'data_LondonOPM')

### CRAFTY set-up --------------------------------------------------------------

### csv ------------------------------------------------------------------------

# this folder holds basic index files which tell CRAFTY which capitals and services it should expect

dfCapitals <- read.csv(paste0(dirOut,"/csv/Capitals.csv"))
head(dfCapitals)
write.csv(dfCapitals, paste0(dirOut,"/csv/Capitals_old.csv"), row.names = FALSE) # save PoC version

# list all capitals here
Name <- c("OPM_presence",
          "Risk_perception",
          "Willingness_to_pay",
          "Knowledge",
          "Risk_map",
          "Nature",
          "Access")
# index must run from 0
Index <- seq(0,length(Name)-1,by=1)

dfCapitals <- tibble(Name,Index)
dfCapitals

write.csv(dfCapitals, paste0(dirOut,"/csv/Capitals.csv"), row.names = FALSE)

dfServices <- read.csv(paste0(dirOut,"/csv/Services.csv"))
dfServices
# this can stay the same - no change this year


### production -----------------------------------------------------------------

# this folder holds agent production files
# these describe which capitals each agent relies on (values 0-1, no reliance - high reliance)
# and which services the agent produces (values 0-1, no production - highest possible production)

# scenario sub-folders within production mean these parameters can change per scenario


