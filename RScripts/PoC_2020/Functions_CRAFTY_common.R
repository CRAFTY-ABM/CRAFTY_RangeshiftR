
# Cell ID and cooridnates 
ctry.ids <- read.csv("~/Dropbox/KIT/CLIMSAVE/IAP/Cell_ID_LatLong.csv")
x.lat.v = sort(unique(ctry.ids$Longitude))
y.lon.v = sort(unique(ctry.ids$Latitude))


# Lon-Lat projection 
proj4.LL <- CRS("+proj=longlat +datum=WGS84")
