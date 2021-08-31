
# date: 31/08/2021
# author: VB
# purpose: edit or create all files required for CRAFTY

### libs -----------------------------------------------------------------------

library(tidyverse)
library(sf)
library(raster)

### paths ----------------------------------------------------------------------

wd <- "~/eclipse-workspace/CRAFTY_RangeshiftR"# sandbox VM
dirData <- file.path(wd, 'data-processed')
dirOut <- file.path(wd, 'data_LondonOPM')
