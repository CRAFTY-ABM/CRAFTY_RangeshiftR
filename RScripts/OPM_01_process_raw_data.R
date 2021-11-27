
# date: 10/08/21
# author: VB
# purpose: process all raw data for OPM model (21/22) to same regular grid for CRAFTY and RangeShiftR

### libs -----------------------------------------------------------------------

library(tidyverse)
library(sf)
library(raster)
library(terra)
library(ggspatial) # for basemaps
library(viridis)
library(tmap)
library(rgeos)
library(units)


### data paths -----------------------------------------------------------------

wd <- "~/OPM-model-prep-21-22/"

# dir.create(wd)


# dataDisk <- "D:/CRAFTY-OPM/"
dataDisk <- "~/Dropbox/CRAFTY_RangeShiftR_data_2021/data-prep/CRAFTY-OPM/"

dirDataRaw <- paste0(dataDisk,"data-raw/")
dirDataOut <- paste0(wd,"data-processed/")
dirFigs <- paste0(wd,"figs/")




# increase memory limit to deal with large feature classes
#memory.limit() # 8165 (due to 8.28 GB RAM)
#memory.limit(size = 20000) # 20 GB assuming I have dynamic RAM on Sandbox which can go up to 32 GB
# ended up doing most feature class processing in ArcGIS so didn't implement this in the end


### 01. 100m grid --------------------------------------------------------------

# Tested a few options (code commented out below, ended up going with grid generated in ArcGIS from GiGL BHP data extent)

# OPTION 1 create grid in R (ended up using grid generated in ArcGIS instead)
# Create a 100 metre grid to extent of GiGL BHP layer
# # https://rpubs.com/dieghernan/beautifulmaps_I
# initial <- sfBHP
# initial$index_target <- 1:nrow(initial)
# target <- st_geometry(initial)
# 
# grid <- st_make_grid(target,
#                      100,
#                      crs = st_crs(initial),
#                      what = "polygons",
#                      square = TRUE)
# 
# # to sf
# grid <- st_sf(index = 1:length(lengths(grid)), grid) # Add index
# head(grid)
# 
# # identify the grids that belongs to a entity by assessing the centroid
# cent_grid <- st_centroid(grid)
# cent_merge <- st_join(cent_grid, initial["index_target"], left = F)
# grid_new <- inner_join(grid, st_drop_geometry(cent_merge))
# head(grid_new)
# 
# # fishnet
# Fishgeom <- aggregate(grid_new,
#                       by = list(grid_new$index_target),
#                       FUN = min,
#                       do_union = FALSE)
# Fishnet <- left_join(Fishgeom %>% dplyr::select(index_target),
#                      st_drop_geometry(initial)) %>%
#   dplyr::select(-index_target)
# 
# ggplot(Fishnet)+
#   geom_sf()
# 
# st_write(Fishnet, dsn = paste0(dirDataOut,"01_GiGL_BHP_100m_grid.shp"))

# OPTION 2
# generated in ArcGIS also using GiGL BHP boundary extent
# info in Data_processing_steps_2021.docx
sfGridGLondon <- st_read(paste0(dataDisk,"data-out.gdb"), layer = "GreaterLondon_100m_grid")
head(sfGridGLondon)
sfGridGLondon <- sfGridGLondon %>% dplyr::select(GridID,Shape)
# plot
ggplot(sfGridGLondon)+
    geom_sf(fill=NA,colour="lightgrey")+
    theme_bw()

# to extract values later, need to use sp points
sfPointsGLondon <- st_centroid(sfGridGLondon)
head(sfPointsGLondon)
sfPointsGLondon <- sfPointsGLondon[,c("GridID","Shape")]
spPointsGLondon <- as_Spatial(sfPointsGLondon)


### CRAFTY data ### ------------------------------------------------------------

### 02. Core & control zones ---------------------------------------------------

# 2021 data from Julia at Southampton University
# want to have coverage of both core & control areas in the model to test different policy options

# access from ArcGIS gdb in data-raw folder
sfCore <- st_read(paste0(dirDataRaw,"data-raw.gdb"), layer = "OPM_2021Core")
sfControl <- st_read(paste0(dirDataRaw,"data-raw.gdb"), layer = "OPM_2021Control")

# list possible types for annotation map tile
rosm::osm.types()

(p01 <- ggplot()+
        #annotation_map_tile(zoom = 9, type = "osm")+
        geom_sf(data = sfControl, fill = NA, size = 1)+
        geom_sf(data = sfCore, fill = NA, size = 1)+
        theme_bw())

#ggsave(paste0(dirFigs,"01_core_&_control_2021.jpg"), p01,  dpi = 300)

# merge
sfCoreControl <- rbind(sfCore,sfControl)
head(sfCoreControl)

sfCoreControl <- sfCoreControl %>% 
    mutate(Zone = ifelse(grepl("core",Type),"Core","Control"),
           Code = ifelse(grepl("Core",Zone),1,2))

# rasterise
vectZones <- vect(sfCoreControl)
rstExt <- rast(vectZones, resolution=100)
rstZones <- rasterize(vectZones, rstExt, "Code")
res(rstZones)

tm_shape(raster::raster(rstZones))+
    tm_raster()

# extract to grid
valsZones <- extract(raster::raster(rstZones), spPointsGLondon)
# join to points
spPointsZones <- cbind(spPointsGLondon,valsZones)
spPointsZones@data
colnames(spPointsZones@data)[2] <- "Code"

# merge with grid by GridID
sfGridGLondon <- merge(sfGridGLondon, spPointsZones@data[,c("GridID","Code")], by="GridID")
head(sfGridGLondon)

sfGridGLondon <- sfGridGLondon %>% mutate(Zone = ifelse(Code == 1, "Core", "Control"))

# plot
(p01 <- ggplot(sfGridGLondon)+
        annotation_map_tile(zoom = 10, type = "osm")+
        geom_sf(aes(fill=Zone), colour=NA, alpha=0.4)+
        scale_fill_brewer(palette = "Dark2")+
        theme_bw())

ggsave(paste0(dirFigs,"01_GreaterLondon_core_&_control.jpg"), p01,  dpi = 300)

### 03. GiGL BHP ---------------------------------------------------------------

# given that agents rely on this for biodiversity service production, this is a key limit for model coverage at the moment

# Biodiversity Hotspots for Planning https://data.london.gov.uk/dataset/biodiversity-hotspots-for-planning
# citation info:
# In-text citation: GiGL, [dataset creation date]
# Reference: "Biodiversity Hotspots for Planning" Greenspace Information for Greater London CIC, [dataset creation date]
# Where data is used in maps: Map displays GiGL data [dataset creation date]
# Where data is summarised but not mapped: Data provided by Greenspace Information for Greater London CIC [dataset creation date]
# last updated 01/09/2019

sfBHP <- st_read(paste0(dirDataRaw,"GiGL_BHP_region.shp"))

head(sfBHP)
st_crs(sfBHP) # OSGB British National Grid, EPSG:27700

# useful crs info: https://inbo.github.io/tutorials/tutorials/spatial_crs_coding/

(p02 <- ggplot()+
        geom_sf(data = sfBHP, aes(fill = BHP_Score), color = NA, alpha = 0.8)+
        scale_fill_viridis()+
        #geom_sf(data = sfControl, fill = NA, size = 1)+
        geom_sf(data = sfCore, fill = NA, size = 1)+
        labs(caption = "Map displays GiGL data 01/09/2019")+
        theme_bw())

# ggsave(paste0(dirFigs,"01_GiGL_BHP_2019.jpg"), p02,  dpi = 300)

# rasterise
vectBHP <- vect(sfBHP)
rstExt <- rast(vectBHP, resolution=100)
rstBHP <- rasterize(vectBHP, rstExt, "BHP_Score")
res(rstBHP)

tm_shape(raster::raster(rstBHP))+
    tm_raster()

# extract to grid
valsBHP <- extract(raster::raster(rstBHP), spPointsGLondon)
# join to points
spPointsBHP <- cbind(spPointsGLondon,valsBHP)
spPointsBHP@data
colnames(spPointsBHP@data)[2] <- "Nature"

# merge with grid by GridID
sfGridGLondon <- merge(sfGridGLondon, spPointsBHP@data[,c("GridID","Nature")], by="GridID")
head(sfGridGLondon)

# plot
ggplot(sfGridGLondon)+
    geom_sf(aes(fill=Nature),colour=NA)+
    scale_fill_viridis()+
    theme_bw()


### 04. Access -----------------------------------------------------------------

folder_name <- "OS_Open_Greenspace/OS Open Greenspace (ESRI Shape File) "

lstTiles <- c("SP","SU","TL","TQ")

for (i in lstTiles){
    
    #i <- lstTiles[1]
    
    sfOSOG <- st_read(paste0(dataDisk,"data-raw/",folder_name,i,"/data/",i,"_GreenspaceSite.shp"))
    head(sfOSOG)
    sfOSOG <- sfOSOG %>% dplyr::select(id,geometry)
    sfOSOG_points <- st_read(paste0(dataDisk,"data-raw/",folder_name,i,"/data/",i,"_AccessPoint.shp"))
    head(sfOSOG_points)
    
    dfAccessSummary <- sfOSOG_points %>% 
        st_drop_geometry() %>%  
        group_by(refToGSite) %>% 
        summarise(n.access = n())
    
    head(dfAccessSummary)
    summary(dfAccessSummary)
    
    colnames(dfAccessSummary)[1] <- "id"
    
    sfOSOG <- sfOSOG %>% left_join(dfAccessSummary, by="id")
    
    # drop Z
    sfOSOG <- st_zm(sfOSOG, drop=T, what='ZM')
    
    #st_write(sfOSOG, dsn = paste0(dataDisk,"data-raw/OS_Open_Greenspace/",i,"_GreenspaceAccess.shp"),append = FALSE)
    
}

SP <- st_read(paste0(dataDisk,"data-raw/OS_Open_Greenspace/SP_GreenspaceAccess.shp"))
SU <- st_read(paste0(dataDisk,"data-raw/OS_Open_Greenspace/SU_GreenspaceAccess.shp"))
TL <- st_read(paste0(dataDisk,"data-raw/OS_Open_Greenspace/TL_GreenspaceAccess.shp"))
TQ <- st_read(paste0(dataDisk,"data-raw/OS_Open_Greenspace/TQ_GreenspaceAccess.shp"))

sfOSOG <- rbind(SP,SU,TL,TQ)

ggplot(sfOSOG)+
    geom_sf(aes(fill=n_access),colour=NA)

summary(sfOSOG$n_access)
sfOSOG$n_access[which(sfOSOG$n_access>20)]<-20 # cap at 20 access points

# rasterise
vectAccess <- vect(sfOSOG)
summary(vectAccess$n_access)
rstExt <- rast(vectAccess, resolution=10)
rstAccess <- rasterize(vectAccess, rstExt, "n_access")
res(rstAccess)

tm_shape(raster::raster(rstAccess))+
    tm_raster()

# extract to grid
valsAccess <- extract(raster::raster(rstAccess), spPointsGLondon)
# join to points
spPointsAccess <- cbind(spPointsGLondon,valsAccess)
spPointsAccess@data
colnames(spPointsAccess@data)[2] <- "Access"

# merge with grid by GridID
sfGridGLondon <- merge(sfGridGLondon, spPointsAccess@data[,c("GridID","Access")], by="GridID")
head(sfGridGLondon)

sfGridGLondon$Access[which(is.nan(sfGridGLondon$Access))] <- 0

# plot
ggplot(sfGridGLondon)+
    geom_sf(aes(fill=Access),colour=NA)+
    scale_fill_viridis()+
    theme_bw()

st_write(sfGridGLondon, paste0(dirDataOut,"01_Grid_zone_nature_access.shp"),append = FALSE)


### 05. OSMM greenspace --------------------------------------------------------

# sfGreenspace <- st_read(paste0(dirDataRaw,"data-raw.gdb"), layer = "OSMM_greenspace_GreaterLondon")
# head(sfGreenspace)
# 
# # select only data needed to reduce size in memory
# sfGreenspace <- sfGreenspace %>% dplyr::select(priFunc, priForm, Shape) 
# 
# # too large to plot (takes ages so don't bother!!)
# # ggplot()+
# #    geom_sf(data = sfGreenspace, aes(fill = priFunc))
# 
# # double check units
# st_crs(sfGreenspace) # metres
# 
# # create and record codes for priFunc
# lstPriFunc <- unique(sfGreenspace$priFunc)
# codePriFunc <- 1:length(lstPriFunc)
# dfPriFunc <- tibble(codePriFunc = codePriFunc, priFunc = lstPriFunc)
# 
# # write to csv as look-up table
# write.csv(dfPriFunc, paste0(dirDataOut,"01_Greenspace_priFunc_code_lookup.csv"), row.names = FALSE)
# 
# # to get code
# sfGreenspace <- left_join(sfGreenspace, dfPriFunc, by = "priFunc")


# OPTION 1 (not good enough representation of landscape)
# rasterise greenspace data to 100m resolution based on Primary Function
# vectGspace <- vect(sfGreenspace)
# rstExt <- rast(vectGspace, resolution=100) 
# rstGspace <- rasterize(vectGspace, rstExt, "codePriFunc")
# res(rstGspace)
# 
# (t01 <- tm_shape(raster::raster(rstGspace))+
#   tm_raster(palette = "viridis", n=length(lstPriFunc), style = "pretty"))
# 
# tmap_save(t01, paste0(dirFigs,"01_Gspace_priFunc_100m.jpg"), dpi = 300)
# 
# writeRaster(rstGspace, paste0(dirDataOut,"01_OSMMgspace_priFunc_100.tif"))
# too much missing data?

# OPTION 2 (takes way too long in R)
# rasterise greenspace data to finer resolution, and extract to 100m grid
# vectGspace <- vect(sfGreenspace)
# rstExt <- rast(vectGspace, resolution=2) 
# rstGspace <- rasterize(vectGspace, rstExt, "codePriFunc")
# res(rstGspace)
# 
# (t02 <- tm_shape(raster::raster(rstGspace))+
#   tm_raster(palette = "viridis", n=length(lstPriFunc), style = "pretty"))
# 
# tmap_save(t02, paste0(dirFigs,"01_Gspace_priFunc_2m.jpg"), dpi = 300)
# 
# writeRaster(rstGspace, paste0(dirDataOut,"01_OSMMgspace_priFunc_002.tif"))
# 
# # extract raster values for each 100m grid square
# spFishnet <- as_Spatial(Fishnet)
# rstGspace <- raster::raster(rstGspace)
# extVals <- extract(rstGspace, spFishnet) # this takes way too long in R. Use ArcGIS option.
# 
# # summarize the raster data for each grid square, returning proportions
# priFunc.counts <- lapply(extVals, table)
# priFunc.prop <- lapply(extVals, FUN = function(x) {prop.table(table(x))})


# OPTION 3
# use grid and values generated in ArcGIS (fishnet grid and tabulate area)
# csv created in ArcGIS using GreaterLondon_100m_grid.shp
df_priFunc <- read.csv(paste0(dataDisk,"data-out/OSMM_gspace_priFunc_tabulate.csv"))
head(df_priFunc)

# convert to percentage
df_priFunc[,3:21] <- df_priFunc[,3:21]/10000 *100

colnames(df_priFunc)[2:21] <- c("GridID",
                                "PrivateGarden","AmenityTransport","InstitutionalGrounds",
                                "AmenityResBus","SchoolGrounds","Natural",
                                "PublicPark","PlaySpace","PlayingField",
                                "ReligiousGrounds","SportsFacility","Changing",
                                "Allotments","GolfCourse","Cemetery",
                                "BowlingGreen","TennisCourt","CampingPark",
                                "NonGreenspace")

# assign types
nrows <- length(df_priFunc[,1])
type <- rep("NA",nrows)

for (i in c(1:nrows)) {
    
    # mask out natural (either inland water or shore front)
    if (df_priFunc$Natural[i]>20){
        type[i] <- "Natural" 
    }
    
    # prioritise bits of amenity
    if (df_priFunc$AmenityResBus[i]>=20){
        type[i] <- "Amenity.residential.business"
    }
    if (df_priFunc$AmenityTransport[i]>=20){
        type[i] <- "Amenity.transport"
    }
    
    # now private gardens
    if (df_priFunc$PrivateGarden[i]>=35){ # 40 underestimated overall area of private gardens, 30-35 gives more accurate proportions compared to gspace figs
        type[i]<- "Private.garden" 
    }                           
    
    # parks
    if (df_priFunc$PublicPark[i]>=45){
        type[i] <- "Public.park"
    }
    
    if (df_priFunc$SchoolGrounds[i]>=20){
        type[i] <- "School.grounds"
    }
    
    if (df_priFunc$ReligiousGrounds[i]>=20){
        type[i] <- "Religious.grounds"
    }
    
    if (df_priFunc$InstitutionalGrounds[i]>=20){
        type[i] <- "Institutional.grounds"
    }
    
    if (df_priFunc$Changing[i]>=50){
        type[i] <- "Non.greenspace"
    }
    
    if (df_priFunc$PlaySpace[i]>=40){
        type[i] <- "Play.space"
    }
    
    if (df_priFunc$SportsFacility[i] >= 40){
        type[i] <- "Other.sports"
    }
    
    if (df_priFunc$PlayingField[i] >= 40){
        type[i] <- "Playing.field"
    }
    
    if (df_priFunc$TennisCourt[i] >= 40){
        type[i] <- "Tennis.court"
    }
    
    if (df_priFunc$Cemetery[i] >= 40){
        type[i] <- "Cemetery"
    }
    
    if (df_priFunc$Allotments[i] >= 40) {
        type[i] <- "Allotments"
    }
    
    if(df_priFunc$BowlingGreen[i] >= 40) {
        type[i] <- "Bowling.green"
    }
    
    if(type[i] == "NA") {
        
        # catch smaller garden areas
        #if (df_priFunc$Garden[i] >= 0.3){
        #type[i] <- "Private.garden"
        #}
        
        # now assign urban with high threshold first
        if (df_priFunc$NonGreenspace[i]>=50){
            type[i] <- "Non.greenspace"
        }
        
        # if still na, check tiny areas of amenity
        if (df_priFunc$AmenityResBus[i]>0){
            type[i] <- "Amenity.residential.business"
        }
        if (df_priFunc$AmenityTransport[i]>0){
            type[i] <- "Amenity.transport"
        }
        # but otherwise make urban
        if (df_priFunc$NonGreenspace[i]>10){
            type[i] <- "Non.greenspace"
        }
        
    }
    
    # final NA catch
    if (type[i]=="NA"){
        type[i] <- "Non.greenspace"
    }
    
}

# check
df_priFunc$type <- type
head(df_priFunc)

# joint to grid
sfGridGLondon <- left_join(sfGridGLondon,df_priFunc[,c("GridID","type")],by="GridID")

type.pal <- c("Amenity.residential.business" = "grey",
              "Amenity.transport" = "darkgrey",
              "Private.garden" = "#483D8B",
              "Public.park" = "#008000",
              "School.grounds" = "#2F4F4F",
              "Religious.grounds" = "#2F4F4F",
              "Institutional.grounds" = "#2F4F4F",
              "Non.greenspace" = "white",
              "Play.space" = "#008080",
              "Playing.field" = "#008080",
              "Other.sports" = "#00FA9A",
              "Tennis.court" = "#00FA9A",
              "Bowling.green" = "#00FA9A",
              "Allotments" = "#B8860B",
              "Cemetery" = "#696969",
              "Natural" = "gray21",
              "NA" = "red")

#library(pals)

#pal.bands(alphabet, alphabet2, cols25, glasbey, kelly, polychrome, 
#stepped, tol, watlington,
#show.names=FALSE)
# https://github.com/kwstat/pals/issues/3

(p03 <- ggplot(sfGridGLondon)+
        geom_sf(aes(fill=type),colour=NA)+
        scale_fill_manual(values=type.pal)+
        #scale_fill_manual(values = as.vector(watlington(16)))+
        labs(fill = "Greenspace type")+
        theme_bw())

ggsave(paste0(dirFigs,"01_Greenspace_type_allocation.jpg"), p03,  dpi = 300)


# now cluster adjacent values of the same type to get owner ids (to assign social capitals)
# a function that takes an sf polygons object and clusters all features within a threshold distance, then merges the features
# https://gis.stackexchange.com/questions/254519/group-and-union-polygons-that-share-a-border-in-r
clusterSF <- function(sfpolys, thresh){
    dmat = st_distance(sfpolys)
    hc = hclust(as.dist(dmat>thresh), method="single")
    groups = cutree(hc, h=0.5)
    d = st_sf(
        geom = do.call(c,
                       lapply(1:max(groups), function(g){
                           st_union(sfpolys[groups==g,])
                       })
        )
    )
    d$group = 1:nrow(d)
    d
}

#unique(sfGridGLondon$type)
# list of types that if polygons are within 5m of each other, are likely to be under same ownership
# essentially everything except private gardens (and non.greenspace/natural)
lstCluster <- c("School.grounds","Religious.grounds", "Institutional.grounds", 
                "Play.space", "Other.sports","Tennis.court","Bowling.green","Playing.field", "Public.park",
                "Cemetery", "Allotments", "Amenity.residential.business", "Amenity.transport")

# commented out as only need to do once
# quick fix as ran out of memory
#lstExtra <- c("Amenity.residential.business", "Amenity.transport")

#for (i in lstCluster){
# #for (i in lstExtra){
#   
#   #i <- lstExtra[1] # test
#   
#   x <- sfGridGLondon %>% 
#     filter(type == i) #%>% 
#   #st_union(by_feature = TRUE)
#   
#   print(paste0("Filtered to ",i))
#   
#   xClust <- clusterSF(x, set_units(5, "m"))
#   #plot(xClust, col=xClust$group)
#   #xClust$uniqueID <- paste0(i,"-",xClust$group)
#   
#   print(paste0("Clustered for ",i))
#   
#   xRast <- rasterize(x=xClust,
#                      y=raster(extent(xClust), res=2),
#                      field='group')
#   #plot(xRast)
#   
#   print(paste0("Rasterised for ", i))
#   
#   #st_write(xClust, paste0(dirOut,"/hexClusters/hexGspace_",i,"_Clust.shp"), append=F)
#   writeRaster(xRast, file.path(paste0(dataDisk, "data-scratch/clust_",i,".tif")), format="GTiff", overwrite=TRUE)
#   
# }

nrows <- nrow(sfGridGLondon)

# empty dataframe for values
df <- data.frame(School.grounds = rep(NA, nrows),
                 Religious.grounds = rep(NA, nrows), 
                 Institutional.grounds = rep(NA, nrows), 
                 Play.space = rep(NA, nrows), 
                 Other.sports = rep(NA, nrows),
                 Tennis.court = rep(NA, nrows),
                 Bowling.green = rep(NA, nrows),
                 Playing.field = rep(NA, nrows), 
                 Public.park = rep(NA, nrows),
                 Cemetery = rep(NA, nrows), 
                 Allotments = rep(NA, nrows),
                 Amenity.residential.business = rep(NA,nrows),
                 Amenity.transport = rep(NA,nrows))

# read in rasters which show group id and extract values
for (i in lstCluster){
    
    #i <- "Allotments"
    
    x <- raster(paste0(dataDisk, "data-scratch/clust_",i,".tif"))
    
    print(paste0("Raster read in for ", i))
    
    value <- extract(x, spPointsGLondon)
    
    print(paste0("Values extracted for ",i))
    
    df[,i] <- value
    
}

summary(df)
df$ownerID <- NA

df$ownerID[which(!is.na(df$School.grounds))] <- paste0("schl-",df$School.grounds[which(!is.na(df$School.grounds))])
df$ownerID[which(!is.na(df$Religious.grounds))] <- paste0("rlgs-",df$Religious.grounds[which(!is.na(df$Religious.grounds))])
df$ownerID[which(!is.na(df$Institutional.grounds))] <- paste0("inst-",df$Institutional.grounds[which(!is.na(df$Institutional.grounds))])
df$ownerID[which(!is.na(df$Play.space))] <- paste0("plysp-",df$Play.space[which(!is.na(df$Play.space))])
df$ownerID[which(!is.na(df$Other.sports))] <- paste0("othsp-",df$Other.sports[which(!is.na(df$Other.sports))])
df$ownerID[which(!is.na(df$Tennis.court))] <- paste0("ten-",df$Tennis.court[which(!is.na(df$Tennis.court))])
df$ownerID[which(!is.na(df$Bowling.green))] <- paste0("bwl-",df$Bowling.green[which(!is.na(df$Bowling.green))])
df$ownerID[which(!is.na(df$Playing.field))] <- paste0("plyfd-",df$Playing.field[which(!is.na(df$Playing.field))])
df$ownerID[which(!is.na(df$Public.park))] <- paste0("park-",df$Public.park[which(!is.na(df$Public.park))])
df$ownerID[which(!is.na(df$Cemetery))] <- paste0("cmtry-",df$Cemetery[which(!is.na(df$Cemetery))])
df$ownerID[which(!is.na(df$Allotments))] <- paste0("altmt-",df$Allotments[which(!is.na(df$Allotments))])
df$ownerID[which(!is.na(df$Amenity.residential.business))] <- paste0("amnrb-",df$Amenity.residential.business[which(!is.na(df$Amenity.residential.business))])
df$ownerID[which(!is.na(df$Amenity.transport))] <- paste0("amnt-",df$Amenity.transport[which(!is.na(df$Amenity.transport))])


# join owner ids to points
spPointsOwners <- cbind(spPointsGLondon,df$ownerID)
spPointsOwners@data
colnames(spPointsOwners@data)[2] <- "ownerID"

# merge with grid by gridID
#sfGridGLondon_drop <- st_drop_geometry(sfGridGLondon)
sfGridGLondon <- merge(sfGridGLondon, spPointsOwners[,c("GridID","ownerID")], by="GridID")

# where owner ID still NA, give unique hex id
sfGridGLondon$ownerID[which(is.na(sfGridGLondon$ownerID))] <- paste0("grid-",sfGridGLondon$GridID[which(is.na(sfGridGLondon$ownerID))])

# check no NAs
summary(is.na(sfGridGLondon$ownerID))

head(sfGridGLondon)
sfGridGLondon$coords.x1 <- NULL
sfGridGLondon$coords.x2 <- NULL

# check
parks <- filter(sfGridGLondon, grepl("park", ownerID))
cmtry <- filter(sfGridGLondon, grepl("cmtry", ownerID))
plyf <- filter(sfGridGLondon, grepl("plyfd", ownerID))
amenity <- filter(sfGridGLondon, grepl("amnt", ownerID))

ggplot(parks)+geom_sf(aes(fill=ownerID), colour=NA)+
    theme(legend.position = "none") # too many ids!

st_write(sfGridGLondon, paste0(paste0(dirDataOut, "01_Grid_gspace_types_ownerIDs.shp")), append=F)



### 06. Assign social capitals -------------------------------------------------

library(Hmisc) # for %nin% (not in)

# read in ratings from SERG
dfSERGratings <- read.csv(paste0(dataDisk,"data-raw/OPM_model_ratings_SERG.csv"))
head(dfSERGratings)

dfSERGsummary <- dfSERGratings %>% dplyr::select(ID,Type,Risk,WTP,Knowledge) %>% 
    pivot_longer(cols = Risk:Knowledge, names_to = "social", values_to = "value") %>% 
    mutate(value = value/10) %>% # scale 0-1, low-high
    group_by(Type,social) %>% 
    summarise(average = round(mean(value, na.rm=TRUE),1)) %>% 
    pivot_wider(names_from = social, values_from = average) 

dfSERGsummary$Knowledge[which(is.nan(dfSERGsummary$Knowledge))] <- NA
dfSERGsummary$Risk[which(is.nan(dfSERGsummary$Risk))] <- NA
dfSERGsummary$WTP[which(is.nan(dfSERGsummary$WTP))] <- NA

dfSERGsummary$Knowledge[which(dfSERGsummary$Type=="Natural")] <- NA
dfSERGsummary$Risk[which(dfSERGsummary$Type=="Natural")] <- NA
dfSERGsummary$WTP[which(dfSERGsummary$Type=="Natural")] <- NA

write.csv(dfSERGsummary, paste0(dirDataOut,"SERG_social_capital_ratings_averaged.csv"), row.names = F)

# copy of sfGrid for social capitals
sfGridSocial <- sfGridGLondon

# new variables
sfGridSocial$riskPerc <- NA 
sfGridSocial$WTP <- NA
sfGridSocial$knowledge <- NA
head(sfGridSocial)

# start with private gardens (these values are from Public Survey 17/18)
# 16% strongly agree OPM is a risk - value = 1
# 54% agree - value = 0.8
# 9% neutral - value = 0.5
# 21%  disagree, strongly disagree or don't know - value = 0
gardensAll <- as.numeric(row.names(sfGridSocial[which(sfGridSocial$type == "Private.garden"),]))
# random sample 16%                       
perc16 <- length(sample(gardensAll,(0.16*length(gardensAll))))
gardens16 <- sample(gardensAll,perc16,replace = F)
sfGridSocial$riskPerc[gardens16] <- 1
# random sample 54% from gardens not already sampled
gardens84 <- as.numeric(row.names(sfGridSocial[which(sfGridSocial$type=="Private.garden" & is.na(sfGridSocial$riskPerc)),]))
perc54 <- length(sample(gardens84,(0.54*length(gardensAll))))
gardens54 <- sample(gardens84,perc54,replace = F)
sfGridSocial$riskPerc[gardens54] <- 0.8
# random sample 9% from gardens not already sampled
gardens30 <- as.numeric(row.names(sfGridSocial[which(sfGridSocial$type=="Private.garden" & is.na(sfGridSocial$riskPerc)),]))
perc9 <- length(sample(gardens30,(0.09*length(gardensAll))))
gardens9 <- sample(gardens30,perc9,replace = F)
sfGridSocial$riskPerc[gardens9] <- 0.5
# final 21%
gardens21 <- as.numeric(row.names(sfGridSocial[which(sfGridSocial$type=="Private.garden" & is.na(sfGridSocial$riskPerc)),]))
perc21 <- length(sample(gardens21,(0.21*length(gardensAll))))
sfGridSocial$riskPerc[gardens21] <- 0

summary(sfGridSocial$riskPerc)

#check
length(sfGridSocial$riskPerc[which(sfGridSocial$riskPerc==0.8)])/length(gardensAll)*100 # 54%
length(sfGridSocial$riskPerc[which(sfGridSocial$riskPerc==0.5)])/length(gardensAll)*100 # 9%
length(sfGridSocial$riskPerc[which(sfGridSocial$riskPerc==0)])/length(gardensAll)*100 #21%
# all good

# apply SERG ratings to other types
unique(sfGridSocial$type)
sfGridSocial$riskPerc[which(sfGridSocial$type == "Allotments")] <- dfSERGsummary$Risk[which(dfSERGsummary$Type == "Allotment or community growing space")]
sfGridSocial$riskPerc[which(sfGridSocial$type == "Amenity.residential.business")] <- dfSERGsummary$Risk[which(dfSERGsummary$Type == "Amenity - residential or business")]
sfGridSocial$riskPerc[which(sfGridSocial$type == "Amenity.transport")] <- dfSERGsummary$Risk[which(dfSERGsummary$Type == "Amenity - transport")]
sfGridSocial$riskPerc[which(sfGridSocial$type == "Bowling.green")] <- dfSERGsummary$Risk[which(dfSERGsummary$Type == "Bowling green")]
sfGridSocial$riskPerc[which(sfGridSocial$type == "Cemetery")] <- dfSERGsummary$Risk[which(dfSERGsummary$Type == "Cemetery")]
sfGridSocial$riskPerc[which(sfGridSocial$type == "Institutional.grounds")] <- dfSERGsummary$Risk[which(dfSERGsummary$Type == "Institutional grounds")]
sfGridSocial$riskPerc[which(sfGridSocial$type == "Other.sports")] <- dfSERGsummary$Risk[which(dfSERGsummary$Type == "Other sports facility")]
sfGridSocial$riskPerc[which(sfGridSocial$type == "Play.space")] <- dfSERGsummary$Risk[which(dfSERGsummary$Type == "Play space")]
sfGridSocial$riskPerc[which(sfGridSocial$type == "Playing.field")] <- dfSERGsummary$Risk[which(dfSERGsummary$Type == "Playing field")]
sfGridSocial$riskPerc[which(sfGridSocial$type == "Public.park")] <- dfSERGsummary$Risk[which(dfSERGsummary$Type == "Public park or garden")]
sfGridSocial$riskPerc[which(sfGridSocial$type == "Religious.grounds")] <- dfSERGsummary$Risk[which(dfSERGsummary$Type == "Religious grounds")]
sfGridSocial$riskPerc[which(sfGridSocial$type == "School.grounds")] <- dfSERGsummary$Risk[which(dfSERGsummary$Type == "School grounds")]
sfGridSocial$riskPerc[which(sfGridSocial$type == "Tennis.court")] <- dfSERGsummary$Risk[which(dfSERGsummary$Type == "Tennis court")]

ggplot(sfGridSocial)+
    geom_sf(aes(fill=riskPerc),colour=NA)+
    scale_fill_viridis()+
    theme_bw()

# check no missed types
test <- sfGridSocial[which(is.na(sfGridSocial$riskPerc)),]
unique(test$type)

# Willingness to pay
sfGridSocial$WTP[which(sfGridSocial$type == "Allotments")] <- dfSERGsummary$WTP[which(dfSERGsummary$Type == "Allotment or community growing space")]
sfGridSocial$WTP[which(sfGridSocial$type == "Amenity.residential.business")] <- dfSERGsummary$WTP[which(dfSERGsummary$Type == "Amenity - residential or business")]
sfGridSocial$WTP[which(sfGridSocial$type == "Amenity.transport")] <- dfSERGsummary$WTP[which(dfSERGsummary$Type == "Amenity - transport")]
sfGridSocial$WTP[which(sfGridSocial$type == "Bowling.green")] <- dfSERGsummary$WTP[which(dfSERGsummary$Type == "Bowling green")]
sfGridSocial$WTP[which(sfGridSocial$type == "Cemetery")] <- dfSERGsummary$WTP[which(dfSERGsummary$Type == "Cemetery")]
sfGridSocial$WTP[which(sfGridSocial$type == "Institutional.grounds")] <- dfSERGsummary$WTP[which(dfSERGsummary$Type == "Institutional grounds")]
sfGridSocial$WTP[which(sfGridSocial$type == "Other.sports")] <- dfSERGsummary$WTP[which(dfSERGsummary$Type == "Other sports facility")]
sfGridSocial$WTP[which(sfGridSocial$type == "Play.space")] <- dfSERGsummary$WTP[which(dfSERGsummary$Type == "Play space")]
sfGridSocial$WTP[which(sfGridSocial$type == "Playing.field")] <- dfSERGsummary$WTP[which(dfSERGsummary$Type == "Playing field")]
sfGridSocial$WTP[which(sfGridSocial$type == "Public.park")] <- dfSERGsummary$WTP[which(dfSERGsummary$Type == "Public park or garden")]
sfGridSocial$WTP[which(sfGridSocial$type == "Religious.grounds")] <- dfSERGsummary$WTP[which(dfSERGsummary$Type == "Religious grounds")]
sfGridSocial$WTP[which(sfGridSocial$type == "School.grounds")] <- dfSERGsummary$WTP[which(dfSERGsummary$Type == "School grounds")]
sfGridSocial$WTP[which(sfGridSocial$type == "Tennis.court")] <- dfSERGsummary$WTP[which(dfSERGsummary$Type == "Tennis court")]
sfGridSocial$WTP[which(sfGridSocial$type == "Private.garden")] <- dfSERGsummary$WTP[which(dfSERGsummary$Type == "Private garden")]

ggplot(sfGridSocial)+
    geom_sf(aes(fill=WTP),colour=NA)+
    scale_fill_viridis()+
    theme_bw()

# Knowledge
sfGridSocial$knowledge[which(sfGridSocial$type == "Allotments")] <- dfSERGsummary$Knowledge[which(dfSERGsummary$Type == "Allotment or community growing space")]
sfGridSocial$knowledge[which(sfGridSocial$type == "Amenity.residential.business")] <- dfSERGsummary$Knowledge[which(dfSERGsummary$Type == "Amenity - residential or business")]
sfGridSocial$knowledge[which(sfGridSocial$type == "Amenity.transport")] <- dfSERGsummary$Knowledge[which(dfSERGsummary$Type == "Amenity - transport")]
sfGridSocial$knowledge[which(sfGridSocial$type == "Bowling.green")] <- dfSERGsummary$Knowledge[which(dfSERGsummary$Type == "Bowling green")]
sfGridSocial$knowledge[which(sfGridSocial$type == "Cemetery")] <- dfSERGsummary$Knowledge[which(dfSERGsummary$Type == "Cemetery")]
sfGridSocial$knowledge[which(sfGridSocial$type == "Institutional.grounds")] <- dfSERGsummary$Knowledge[which(dfSERGsummary$Type == "Institutional grounds")]
sfGridSocial$knowledge[which(sfGridSocial$type == "Other.sports")] <- dfSERGsummary$Knowledge[which(dfSERGsummary$Type == "Other sports facility")]
sfGridSocial$knowledge[which(sfGridSocial$type == "Play.space")] <- dfSERGsummary$Knowledge[which(dfSERGsummary$Type == "Play space")]
sfGridSocial$knowledge[which(sfGridSocial$type == "Playing.field")] <- dfSERGsummary$Knowledge[which(dfSERGsummary$Type == "Playing field")]
sfGridSocial$knowledge[which(sfGridSocial$type == "Public.park")] <- dfSERGsummary$Knowledge[which(dfSERGsummary$Type == "Public park or garden")]
sfGridSocial$knowledge[which(sfGridSocial$type == "Religious.grounds")] <- dfSERGsummary$Knowledge[which(dfSERGsummary$Type == "Religious grounds")]
sfGridSocial$knowledge[which(sfGridSocial$type == "School.grounds")] <- dfSERGsummary$Knowledge[which(dfSERGsummary$Type == "School grounds")]
sfGridSocial$knowledge[which(sfGridSocial$type == "Tennis.court")] <- dfSERGsummary$Knowledge[which(dfSERGsummary$Type == "Tennis court")]
sfGridSocial$knowledge[which(sfGridSocial$type == "Private.garden")] <- dfSERGsummary$Knowledge[which(dfSERGsummary$Type == "Private garden")]

ggplot(sfGridSocial)+
    geom_sf(aes(fill=knowledge),colour=NA)+
    scale_fill_viridis()+
    theme_bw()

st_write(sfGridSocial, paste0(paste0(dirDataOut, "01_Grid_social.shp")), append=F)



### 07. Risk layer -------------------------------------------------------------

# Read in nonconiferous tree layer extracted from MasterMap in Arc

sfOSMMBroadleaf <- st_read(paste0(dataDisk,"data-scratch.gdb"), layer = "OSMM_topo_broadleaved")
head(sfOSMMBroadleaf)

ggplot(sfOSMMBroadleaf)+geom_sf(aes(fill = style_description),colour=NA)

sfOSMMBroadleaf <- sfOSMMBroadleaf %>% dplyr::select(Toid,DescTerm,proximitySchool,proximityPark,proximitySSSI,SHAPE)

# new numeric cols
sfOSMMBroadleaf <- sfOSMMBroadleaf %>% 
    mutate(school = ifelse(proximitySchool=="Within 100m",1,NA),
           park = ifelse(proximityPark=="Within 100m",1,NA),
           sssi = ifelse(proximitySSSI=="Within 100m",1,NA))

# risk column
dfRisk <- sfOSMMBroadleaf %>% 
    rowwise() %>% 
    mutate(risk = sum(c_across(school:sssi),na.rm = TRUE))

sfOSMMBroadleaf$risk <- dfRisk$risk

head(sfOSMMBroadleaf)

# plot
ggplot(sfOSMMBroadleaf)+
    geom_sf(aes(fill = risk),colour=NA)

# rasterise based on risk
vect <- vect(sfOSMMBroadleaf)
rstExt <- rast(vect, resolution=100)
rst <- rasterize(vect, rstExt, "risk")
res(rst)

(t03 <- tm_shape(raster::raster(rst))+
        tm_raster(palette = "YlOrRd", n=3, style = "pretty"))

tmap_save(t03, paste0(dirFigs,"01_Risk_layer.jpg"), dpi = 300)

# extract to grid
valsRisk <- extract(raster::raster(rst), spPointsGLondon)
# join owner ids to points
sp_pointsRisk <- cbind(spPointsGLondon,valsRisk)
sp_pointsRisk@data
colnames(sp_pointsRisk@data)[2] <- "riskAreas"

# merge with grid by gridID
sfGridGLondon <- sfGridSocial
sfGridGLondon <- merge(sfGridGLondon, sp_pointsRisk, by="GridID")
head(sfGridGLondon)
sfGridGLondon$coords.x1 <- NULL
sfGridGLondon$coords.x2 <- NULL

sfGridGLondon$riskAreas <- sfGridGLondon$riskAreas + 1
sfGridGLondon$riskAreas[which(is.nan(sfGridGLondon$riskAreas))] <- 0
# 0 = not broadleaf woodland
# 1 = woodland not in proximity to risk factors
# 2 = woodland in proximity to one risk factor
# 3 = woodland in proximity to two risk factors
# 4 = woodland in proximity to all three risk factors

ggplot(sfGridGLondon)+
    geom_sf(aes(fill=riskAreas),colour=NA)+
    theme_bw()

head(sfGridGLondon)
#sfGridRisk <- sfGridGLondon %>% dplyr::select(GridID,risk)

st_write(sfGridGLondon, paste0(paste0(dirDataOut, "01_Grid_capitals_raw.shp")), append=F)

sfCapitals <- st_read(paste0(paste0(dirDataOut, "01_Grid_capitals_raw.shp")))

### plot all -------------------------------------------------------------------

# dfGridGLondon <- sfGridGLondon %>% 
#   mutate(X = st_coordinates(st_centroid(.))[,2],
#          Y = st_coordinates(st_centroid(.))[,1]) %>% #%>% sf::st_set_geometry(NULL)
#   st_drop_geometry()
# 
# #dfGridGLondon %>% dplyr::select(GridID,X,Y,Zone,Nature,Access,riskAreas,riskPerc,WTP,knowledge) %>% 
# 
# sfGridGLondon %>% dplyr::select(Zone,Nature,Access,riskAreas,riskPerc,WTP,knowledge,geometry) %>% 
#   pivot_longer(cols = Zone:knowledge, names_to = "capital", values_to = "value") %>% 
#   st_as_sf(.) %>% 
#   ggplot()+
#   geom_sf(aes(fill=value),colour=NA)+
#   #geom_tile(aes(x=X,y=Y,fill = value))+
#   facet_wrap(~capital)+
#   scale_fill_viridis()+
#   theme_bw()

sfGrid_long <- sfGridGLondon %>% dplyr::select(Zone,Nature,Access,riskAreas,riskPerc,WTP,knowledge,geometry) %>% 
    pivot_longer(cols = Zone:knowledge, names_to = "capital", values_to = "value") %>% 
    st_as_sf()

# warning - takes ages!!
(tm_caps <- tm_shape(sfGrid_long)+
        tm_fill("value", palette = "viridis", border.col = NA)+
        tm_layout(legend.position = c("left","bottom"))+
        tm_facets("capital", free.scales = TRUE))

tmap_save(tm_caps, filename = paste0(dirFigs,"01_Capitals_raw.jpg"))



### RangeShiftR Data ### -------------------------------------------------------

### 08. MasterMap broadleaf woodland -------------------------------------------

# Read in nonconiferous tree layer extracted from MasterMap in Arc
sfOSMMBroadleaf <- st_read(paste0(dataDisk,"data-scratch.gdb"), layer = "OSMM_topo_broadleaved")
head(sfOSMMBroadleaf)

# separate out DescTerm into 3
sfOSMMBroadleaf <- sfOSMMBroadleaf %>% 
    separate(col = DescTerm, into = c("DescTerm1","DescTerm2","DescTerm3"), sep = "[,]")

head(sfOSMMBroadleaf)

sfOSMMBroadleaf <- sfOSMMBroadleaf %>% dplyr::select(DescTerm1, DescTerm2, DescTerm3, SHAPE)

sfOSMMBroadleaf <- sfOSMMBroadleaf %>% mutate(habSuit = ifelse(DescTerm1 == "Nonconiferous Trees", 75,
                                                               ifelse(DescTerm2 == "Nonconiferous Trees", 50,
                                                                      ifelse(DescTerm3 == "Nonconiferous Trees", 50, 0))))

ggplot(sfOSMMBroadleaf)+
    geom_sf(aes(fill = habSuit), colour = NA)+
    theme_bw()

# rasterise
vectBroad <- vect(sfOSMMBroadleaf)
rstExt <- rast(vectBroad, resolution=100)
rstBroad <- rasterize(vectBroad, rstExt, "habSuit")
res(rstBroad)

tm_shape(raster::raster(rstBroad))+
    tm_raster()

# extract to grid
valsBroad <- extract(raster::raster(rstBroad), spPointsGLondon)
# join to points
spPointsBroad <- cbind(spPointsGLondon,valsBroad)
spPointsBroad@data
colnames(spPointsBroad@data)[2] <- "habSuit"

# merge with grid by GridID
sfGridGLondon <- merge(sfGridGLondon, spPointsBroad@data[,c("GridID","habSuit")], by="GridID")
head(sfGridGLondon)

sfGridGLondon$habSuit[which(is.nan(sfGridGLondon$habSuit))] <- 0

# plot
ggplot(sfGridGLondon)+
    geom_sf(aes(fill=habSuit),colour=NA)+
    scale_fill_viridis()+
    theme_bw()


### 09. Oak trees from Southampton database ------------------------------------

# csv exported from Trees tab of Southampton database (FRExport_20210708)
dfOaks <- read.csv(paste0(dirDataRaw,"OPMCP_Southampton/TreesSurveyed_2021.csv"))
head(dfOaks)
summary(dfOaks)


# use all for oak tree locations
# convert to shapefile
dfOaks <- dfOaks[!is.na(dfOaks$Easting), ]
dfOaks <- dfOaks[!is.na(dfOaks$Northing), ]
spOaks <- dfOaks
coordinates(spOaks) <- ~Easting+Northing
projection(spOaks) <- crs(sfGridGLondon)
st_crs(spOaks) # make sure BNG
# crop to species locations only within the study landscape
spOaks <- crop(spOaks, extent(sfGridGLondon))
spOaks$n <- 1

sfOaks <- st_as_sf(spOaks)

ggplot(sfGridGLondon)+
    geom_sf(aes(fill=habSuit),colour=NA)+
    scale_fill_viridis()+
    geom_sf(data = sfOaks, shape = 1, size = 1, colour = "lightgrey")+
    theme_bw()

# count oaks per 100m grid
sfGridGLondon$oakCount <- lengths(st_intersects(sfGridGLondon,sfOaks))

ggplot(sfGridGLondon)+
    geom_sf(aes(fill=oakCount),colour=NA)+
    scale_fill_viridis()+
    theme_bw() 

head(sfGridGLondon)
summary(sfGridGLondon$oakCount)

# make squares with 5 or more oaks 100% quality
sfGridGLondon$habSuit[which(sfGridGLondon$oakCount>=5)] <- 100

ggplot(sfGridGLondon)+
    geom_sf(aes(fill=habSuit),colour=NA)+
    scale_fill_viridis()+
    theme_bw()

st_write(sfGridGLondon, paste0(dirDataOut, "01_Grid_RshiftR_habitat.shp"))

# rasterise
vectSuit <- vect(sfGridGLondon)
rstExt <- rast(vectSuit, resolution=100)
rstSuit <- rasterize(vectSuit, rstExt, "habSuit")
res(rstSuit)

tm_shape(raster::raster(rstSuit))+
    tm_raster()

# write to raster
writeRaster(raster::raster(rstSuit), paste0(dirDataOut, "01_RshiftR_habitat.tif"))


# prep for RangeShiftR initial individuals
dfOaks_filter <- subset(dfOaks, SurveyYear == 2012)
dfOaks_filter <- subset(dfOaks_filter, Status == 'Infested' | Status == "Previously infested")

# convert to shapefile
dfOaks_filter <- dfOaks_filter[!is.na(dfOaks_filter$Easting), ]
dfOaks_filter <- dfOaks_filter[!is.na(dfOaks_filter$Northing), ]
shpInitialIndividuals <- dfOaks_filter
#rm(dfOaks)
coordinates(shpInitialIndividuals) <- ~Easting+Northing
projection(shpInitialIndividuals) <- crs(sfGridGLondon)
st_crs(shpInitialIndividuals) # make sure BNG
# crop to species locations only within the study landscape
shpInitialIndividuals <- crop(shpInitialIndividuals, extent(sfGridGLondon))
shpInitialIndividuals$n <- 1

# plot
sfOaks <- st_as_sf(shpInitialIndividuals)
head(sfOaks)

ggplot(sfGridGLondon)+
    #geom_sf(aes(fill=habSuit),colour=NA)+
    #scale_fill_manual(values = type.pal)+
    #labs(fill = "Greenspace type")+
    geom_sf(data=sfOaks)+
    theme_bw()

rstHabitat <- raster::raster(rstSuit)

# need to rasterise then extract the species locations to get the xy, row/col (not spatial) indices for rangeshifter.
rstInitIndividuals <- rasterize(shpInitialIndividuals, rstHabitat, field='n', background=0)
plot(rstInitIndividuals)
dfInitialIndividuals <- extract(rasterize(shpInitialIndividuals, rstHabitat, field='n', background=0), shpInitialIndividuals, cellnumbers=T, df=TRUE)

# RangeShiftR requires a specific format for the individuals file, so add the required columns here,
# and convert 'cells' value to x/y, row/col values.
# As an example we are just initialising each cell with 100 individuals - we may need to adjust this later.
dfInitialIndividuals$Year <- 0
dfInitialIndividuals$Species <- 0
dfInitialIndividuals$X <- dfInitialIndividuals$cells %% ncol(rstHabitat)
dfInitialIndividuals$Y <- nrow(rstHabitat) - (floor(dfInitialIndividuals$cells / ncol(rstHabitat)))
dfInitialIndividuals$Ninds <- 100
dfInitialIndividuals <- dfInitialIndividuals[ , !(names(dfInitialIndividuals) %in% c('ID', 'cells', 'layer'))]

write.table(dfInitialIndividuals, file.path(dirDataOut, '01_initial_inds_2012.txt'), row.names = F, quote = F, sep = '\t')


### 10. London tree canopy -----------------------------------------------------

# https://data.london.gov.uk/dataset/curio-canopy

# 350m hexagon option
# sfTreeCanopy <- st_read(paste0(dataDisk,"data-raw/Curio_London_Canopy_Cover/shp-hexagon-files/gla-canopy-hex.shp"))
# st_crs(sfTreeCanopy)
# head(sfTreeCanopy)
# 
# summary(sfTreeCanopy$canopy_per)
# 
# ggplot()+
#   geom_sf(data=sfTreeCanopy, aes(fill = canopy_per))+
#   scale_fill_viridis()+
#   theme_bw()
# 
# # could filter to hexagons above a certain percentage canopy cover and use this as a rough indication of street trees?
# 
# # or rasterise to same grid based on percentage cover
# vectCanopy <- vect(sfTreeCanopy)
# #rstExt <- rast(vectGspace, resolution=100) # use same extent from GiGL BHP above
# rstCanopy <- rasterize(vectCanopy, rstExt, "canopy_per")
# res(rstCanopy)
# 
# tm_shape(raster::raster(rstCanopy))+
#   tm_raster(palette = "viridis", n=10, style = "pretty")
# 
# writeRaster(rstCanopy, paste0(dirDataOut,"01_Curio_Canopy.tif"))


# there is a detailed vector layer in kml format. think about possibilities for using either/both
# looking into converting kml in Arc on laptop - https://www.gislounge.com/how-to-import-a-kml-file-into-arcgis/ 
