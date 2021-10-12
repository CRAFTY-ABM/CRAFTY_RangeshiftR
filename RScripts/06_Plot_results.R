
### author: VB
### date: 12/10/2021
### description: start code to plot results, hopefully useful for RShiny dev.

### packages required ----------------------------------------------------------

library(tidyverse)
library(foreach)
library(sf)
library(viridis)

### file paths -----------------------------------------------------------------

dirCRAFTY <- "C:/Users/vanessa.burton.sb/Documents/eclipse-workspace/CRAFTY_RangeshiftR/"
dataDrive <- "D:/CRAFTY_RangeShiftR_21-22_outputs/"

dirOut <- paste0(dirCRAFTY, "output")

# figure directory
dirFigs <- paste0(dirCRAFTY,"figures")



### RangeShiftR results --------------------------------------------------------
