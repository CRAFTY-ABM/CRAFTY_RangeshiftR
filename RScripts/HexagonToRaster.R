library(raster)
library(rgeos)
library(rgdal)

setwd("~/git/CRAFTY_RangeshiftR2/")


hx = readOGR("data_LondonOPM/shp/hexGrid40m/hexGrid40m.shp")

# plot(hx)

hx_coords = coordinates(hx)


gc = gCentroid(hx, byid = T)
# plot(gc)

# extract centroid coordinates
x = gc@coords[,1]
y = gc@coords[,2]

# unique coordinates 
x_unq = sort(unique(x), decreasing = F)
y_unq = sort(unique(y), decreasing = F)

# ranks (later row/col id in CRAFTY)
x_rnk = 1:length(x_unq)
y_rnk = 1:length(y_unq)

# order of the centroid coordiantes
x_ord = match(x, x_unq)
y_ord = match(y, y_unq)

# the raster will not be sparse
table(1:max(x_ord) %in% x_ord)
table(1:max(y_ord) %in% y_ord)

# dummy raster 
library(raster)
sp = SpatialPixels(SpatialPoints(expand.grid(x_rnk, y_rnk))) 
r = setValues(raster(sp), NA)

# find the cells corresponding to the hexagons 
cells = raster::cellFromRowCol(r, rev(y_ord), x_ord) # must reverse the y_ord 
table(is.na(cells))

val = getValues(r)
val[cells] =  ( hx$borough)
r2 = setValues(r, val)

plot(r2, col =viridis::viridis(4))

writeRaster(r2, filename = "borough.tif", overwrite=T)





# todo 
# https://stackoverflow.com/questions/61414897/how-can-i-bin-data-into-hexagons-of-a-shapefile-and-plot-it

# require(sp)
# data(meuse.riv)
# meuse.sr = SpatialPolygons(list(Polygons(list(Polygon(meuse.riv)), "x")))
# plot(meuse.sr)
# 
# library(rgeos)
# meuse.large = gBuffer(meuse.sr, width = 2000)
# HexPts <-spsample(hx, type="hexagonal", cellsize=1000)
# HexPols <- HexPoints2SpatialPolygons(HexPts)
# plot(HexPols[ ,], add=TRUE)
