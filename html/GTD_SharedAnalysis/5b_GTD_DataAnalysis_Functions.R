#--------------------------------------------------------------------------------------------------#

#------CODE developed and written by
# - Daniel R Schlaepfer (dschlaep@uwyo.edu, drs): 2009-2013
#for contact and further information see also: sites.google.com/site/drschlaepfer

#------DISCLAIMER: This program is distributed in the hope that it will be useful,
#but WITHOUT ANY WARRANTY; without even the implied warranty of
#MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
#--------------------------------------------------------------------------------------------------#

#-------------------------------
#---R packages
libraries <- c("RSQLite", "seqinr", "raster", "sp", "maps", "gstat", "rgdal")
l <- suppressMessages( lapply(libraries, FUN=function(lib) stopifnot(require(lib, character.only=TRUE, quietly=TRUE))) )

#---Directories
stopifnot(file.exists(dir.dat))
stopifnot(file.exists(dir.sana))
stopifnot(file.exists(dir.gis))

#---File names
name.dbScen <- "dbTables_current.sqlite3"
names.dbEns <- list.files(dir.dat, pattern="dbEnsemble")
rastername.mask <- "StudyAreaMask_CurrentFuture.asc"


#---Study area definition:
# Define study area with three criteria
#	1. Aridity: PPT/PET < 0.5
#	2. Temperateness: Trewartha's D
#	3. Contiguousness: Contiguous with areas matching criteria 1 & 2 under current conditions
# Cells included in simulations: "Mask_Current" == 1 || "Mask_Future" == 1
#	- "Mask_Current": based on preliminary simulation for 1979-2010
#	- "Mask_Future": most severely dry & hot future
# --> for each climate scenario, check which cells are included in study area of the climate scenario
var.Study <- "TemperateDryland12_Normals_TF_mean"


#---Get database functionality
source(file.path(dir.sana, "5_Database_Functions.R"))

addTrans <- function(color,trans)
{
	# This function adds transparancy to a color.
	# Define transparancy with an integer between 0 and 255
	# 0 being fully transparant and 255 being fully visable
	# Works with either color and trans a vector of equal length,
	# or one of the two of length 1.
	
	if (length(color)!=length(trans)&!any(c(length(color),length(trans))==1)) stop("Vector lengths not correct")
	if (length(color)==1 & length(trans)>1) color <- rep(color,length(trans))
	if (length(trans)==1 & length(color)>1) trans <- rep(trans,length(color))
	
	num2hex <- function(x)
	{
		hex <- unlist(strsplit("0123456789ABCDEF",split=""))
		return(paste(hex[(x-x%%16)/16+1],hex[x%%16+1],sep=""))
	}
	rgb <- rbind(col2rgb(color),trans)
	res <- paste("#",apply(apply(rgb,2,num2hex),2,paste,collapse=""),sep="")
	return(res)
}

#Database convenience functions
get.SeveralOverallVariables_ofStudy <- function(studyName=var.Study, responseName, MeanOrSD="Mean", i_climCat=1, whereClause=NULL){
	temp <- get.SeveralOverallVariables(responseName=c(studyName, responseName), MeanOrSD=MeanOrSD, i_climCat=i_climCat, whereClause=whereClause)
	temp <- temp[temp[, var.Study] >= 0.5, !(colnames(temp) %in% var.Study)] #Include only cells that are inside study area for climate condition for at least half of the GCMs
	return(temp)
}

get.Table_ofStudy <- function(studyName=var.Study, responseName, MeanOrSD="Mean", i_climCat=1, whereClause=NULL){
	if(length(whereClause) > 0 && grepl("Soil_Layer", whereClause)){ #adjust whereClause for extraction from table without soil layer
		#Remove "Soil_Layer='x'" and associated " AND "
		temp1 <- strsplit(whereClause, split=" ", fixed=TRUE)[[1]]
		i_layer <- grep("Soil_Layer", temp1)
		i_and <- grep("AND", temp1)
		i_andl <- ifelse((i_layer-1) %in% i_and, i_layer-1, i_layer+1)
		studyWhere <- paste(temp1[-c(i_layer, i_andl)], collapse=" ", sep="")
	} else {
		studyWhere <- whereClause
	}
	study <- get.SeveralOverallVariables(responseName=c("P_id", studyName), MeanOrSD=MeanOrSD, i_climCat=i_climCat, whereClause=studyWhere)
	temp <- get.Table(responseName=responseName, MeanOrSD=MeanOrSD, i_climCat=i_climCat, whereClause=whereClause, addPid=TRUE)
	#Include only cells that are inside study area for climate condition for at least half of the GCMs
	Pid_inStudyArea <- study$P_id[study[, var.Study] >= 0.5]
	temp <- temp[temp$P_id %in% Pid_inStudyArea, !(colnames(temp) %in% "P_id")]
	if(length(whereClause) > 0 && grepl("Soil_Layer", whereClause)) temp <- temp[, -grep("Soil_Layer", colnames(temp))]
	return(temp)
}


#---Climate categories
#mini project
#climCat <- maker.climateScenarios(currentScenario="Current", ensembleScenarios=c("SRESA2"), ensembleLevels=1:3)
#full project
climCat <- maker.climateScenarios(currentScenario="Current", ensembleScenarios=c("RCP45","RCP85"), ensembleLevels=c(2,8,15))


#---Settings
output_aggregate_daily <- c("AET", "Transpiration", "EvaporationSoil", "EvaporationSurface", "EvaporationTotal", "VWC", "SWC", "SWP", "Snowpack", "SWA", "Rain", "Snowfall", "Snowmelt", "SnowLoss", "Runoff", "Infiltration", "DeepDrainage", "PET", "TotalPrecipitation", "TemperatureMin", "TemperatureMax")
seasons4 <- c("Dec-Jan", "Mar-May", "Jun-Aug", "Sep-Nov")
regionLabel <- c("South America", "Southern Africa", "Eastern Asia", "Western & Central Asia", "Western Mediterranean", "North America")
vegtypes <- c("Shrub", "Grass", "Tree")
mo <- 1:12
doy <- 1:365


#---Get GIS mapping functionality
mask <- raster(x=file.path(dir.gis, rastername.mask))
projection(mask) <- CRS("+proj=longlat +datum=WGS84 +no_defs +ellps=WGS84 +towgs84=0,0,0")


calc.RasterFromData <- function(Xcoord, Ycoord, dataVector, mask){
	stopifnot(identical(length(Xcoord), length(Ycoord), length(dataVector)))
#	stopifnot(sum(is.na(dataVector)) == 0) #final project
#	mini project:
		inna <- !is.na(dataVector)
		Xcoord <- Xcoord[inna]
		Ycoord <- Ycoord[inna]
		dataVector <- dataVector[inna]
#	end mini project
	datSP <- SpatialPointsDataFrame(coords=coordinates(data.frame(Xcoord, Ycoord)), data=data.frame(dat=dataVector), proj4string=CRS(projection(mask)))
	return(rasterize(datSP, y=mask, field="dat"))
}

calc.IDWinterpolateFromData <- function(Xcoord, Ycoord, dataVector, mask){
	# inverse distance weighted (IDW) interpolation with inverse distance power set to .5
	stopifnot(identical(length(Xcoord), length(Ycoord), length(dataVector)))
#	stopifnot(sum(is.na(dataVector)) == 0) #final project
#	mini project:
		inna <- !is.na(dataVector)
		Xcoord <- Xcoord[inna]
		Ycoord <- Ycoord[inna]
		dataVector <- dataVector[inna]
#	end mini project
	df <- data.frame(x=Xcoord, y=Ycoord, dat=dataVector)
	mg <- gstat(id="dat", formula=dat ~ 1,
				locations= ~x+y, data=df,
				nmax=min(7, round(0.05 * length(dataVector))), set=list(idp = .5))
	temp <- interpolate(mask, mg, debug.level=0)
	names(temp) <- "dat"
	return(mask(temp, mask))
}

panel.map <- function(fgrid, mask, colors=NULL, colmask="black", zlim=NULL, label_TF=TRUE){
	rgb_TF <- (("list" %in% class(fgrid)) && length(fgrid) == 3) || (("RasterStack" %in% class(fgrid)) && nlayers(fgrid) == 3)
	if(rgb_TF && ("list" %in% class(fgrid))) fgrid <- stack(fgrid)
	
	plot(mask, maxpixels=ncell(mask), col=colmask, axis.args=list(col="white", at=1, labels=""), legend.width=0.1, legend.shrink=0.01, axes=TRUE)

	if(is.null(zlim)){
		zlim <- c(min(minValue(fgrid)), max(maxValue(fgrid)))
	}
	if(!rgb_TF){
		if(is.null(colors)) colors <- rainbow(n=length(pretty(zlim))-1)
		if(label_TF){
			axis.args <- NULL #default
			legend.width <- 2 #default = 1.2
			legend.shrink <- 0.9 #default
		} else {
			axis.args <- list(col="white", at=1, labels="")
			legend.width <- 0.1
			legend.shrink <- 0.01
		}
	}

	if(rgb_TF){
		plotRGB(fgrid, maxpixels=ncell(fgrid), scale=1, zlim=zlim, bgalpha=0, add=TRUE)
		if(label_TF && length(lname <- names(fgrid)) == 3) title(sub=paste0("Red = ", lname[1], ", green = ", lname[2], ", blue = ", lname[3], ", ", colmask ," = no data"), line=-2)
	} else {
		plot(fgrid, maxpixels=ncell(fgrid), col=colors, zlim=zlim, add=TRUE,
			legend.width=legend.width, 
			axis.args=axis.args,
			legend.shrink=legend.shrink)
	}

	map("world", xlim=c((temp <- extent(mask))@xmin, temp@xmax), ylim=c(temp@ymin, temp@ymax), lwd=0.5, add=TRUE)
	#map("state", add=TRUE)	
}

draw.map <- function(fgrid, mask, colors=NULL, colmask="black", zlim=NULL, label_TF=TRUE, dir, filename, title=filename){
	pdf(width=10, height=7, file=file.path(dir, paste0(filename, ".pdf")))
	panel.map(fgrid=fgrid, mask=mask, colors=colors, colmask=colmask, zlim=zlim, label_TF=label_TF)
	title(main=title)
	dev.off()
}

draw.TernaryPanel <- function(dimnames, colors="RGB"){
	## based on vcd::ternaryplot
	#vcd::ternaryplot(x=tpx, col=rgb(red=tpx[, 1], green=tpx[, 2], blue=tpx[, 3]), dimnames=ctype, grid=FALSE)
	top <- sqrt(3) / 2
	
	## coordinates of point P(a,b,c): xp = b + c/2, yp = c * sqrt(3)/2
	tpx <- expand.grid(tpx <- seq(0, 1, by=0.05), tpx, tpx)[-1, ]
	tpx <- sweep(tpx, MARGIN=1, STATS=apply(tpx, 1, sum), FUN="/")
	tpx <- unique(tpx)
	
	xp <- tpx[,2] + tpx[,3] / 2
	yp <- tpx[,3] * top
	
	if(identical(colors, "RGB")){
		col <- rgb(red=tpx[, 1], green=tpx[, 2], blue=tpx[, 3])
	} else {
		col <- colors
	}
	
	plot(x=xp, y=yp, asp=1, xlim=c(-0.05, 1.05), ylim=c(-0.05, top + 0.05), col=col, pch=16, type="p", bty="n", xlab="", ylab="", axes=FALSE)
	segments(x0=x <- c(0, 0.5, 1), y0=y <- c(0, top, 0), x1=c(x[-1], x[1]), y1=c(y[-1], y[1]))
	text(x=c(-0.05, 1.05, 0.5), y=c(-0.02, -0.02, top + 0.02), labels=dimnames)
}


#Color scheme examples
colN <- 255
colors1 <- gray(0:colN/colN)
colors2 <- terrain.colors(n=colN)
colors3 <- heat.colors(n=colN)
colors4 <- rainbow(n=colN)
colors5 <- topo.colors(n=colN)
colors6 <- c("black", "pink", "purple", "pink3", "cyan", "blue", "cyan4")


#---CODE OF EXAMPLES----------------------------

#---Design of simulation experiment
con <- dbConnect(drv, file.path(dir.dat, name.dbScen))

temp <- dbListFields(con,name="treatments")[-(1:3)]#remove id experimental_id simulation_years_id
trNames_Experiment <- temp[!(temp %in% c("LookupWeatherFolder_id"))]

TreatmentDefinitions <- dbGetQuery(con, paste("SELECT DISTINCT Experimental_Label, ", paste0(paste0("\"", trNames_Experiment, "\"",sep=""), collapse=", "), " FROM header ORDER BY Experimental_Label;",sep=""))

trLevels_Site <- dbGetQuery(con, "SELECT DISTINCT site_id FROM sites ORDER BY site_id;")$site_id
trLevels_Region <- dbGetQuery(con, "SELECT DISTINCT Region FROM sites ORDER BY Region;")$Region
trLevels_Experiment <- dbGetQuery(con, "SELECT DISTINCT label FROM experimental_labels ORDER BY label;")$label

dbDisconnect(con)

get.CoordinatesOfRegion <- function(regions=NULL){
	temp <- get.SeveralOverallVariables(responseName=c("X_WGS84", "Y_WGS84", "Region"), i_climCat=1, whereClause=paste0("Experimental_Label=", shQuote(trLevels_Experiment[1])))
	if(!is.null(regions)) temp <- temp[temp$Region %in% regions,]
	return(temp[, c("Region", "X_WGS84", "Y_WGS84")])
}

#-------------------------------

#---Example: Soils
#Draw global maps of soils for each treatment if soil change with treatment
map.Soils <- function(dir.map, figname, byTreatment=FALSE){
	varSoil <- c("SWinput_Soil_maxDepth_cm", "SWinput_Soil_topLayers_", "SWinput_Soil_bottomLayers_")
	ctype <- c("Sand", "Silt", "Clay")
	en <- ifelse(byTreatment, length(trLevels_Experiment), 1)
				
	pdf(height=3*(1+length(varSoil)), width=5*en, file=file.path(dir.map, figname))
	op <- par(mfcol=c(1+length(varSoil), en), mar=c(2, 5, 2, 0.1), las=1)
	for(ie in 1:en){
		temp <- get.SeveralOverallVariables(responseName=c("X_WGS84", "Y_WGS84", varSoil), i_climCat=1, whereClause=paste0("Experimental_Label=", shQuote(trLevels_Experiment[ie])))
		colnames(temp) <- gsub(".", "_", colnames(temp), fixed=TRUE)
		if(any(!is.na(temp))){
			depth <- calc.IDWinterpolateFromData(Xcoord=temp$X_WGS84, Ycoord=temp$Y_WGS84, dataVector=temp$SWinput_Soil_maxDepth_cm, mask=mask)
			par(mfg=c(1, ie))
			panel.map(fgrid=depth, mask=mask, colors=rev(colors5), label_TF=(ie == 1))
			if(byTreatment) mtext(text=trLevels_Experiment[ie], side=3)
			if(ie == 1) mtext(text="Soil depth (cm)", side=ifelse(byTreatment, 2, 3), line=ifelse(byTreatment, 2, 0), las=0)
			
			temp$topLayers_Silt <- 1 - (temp$SWinput_Soil_topLayers_Sand_fraction + temp$SWinput_Soil_topLayers_Clay_fraction)
			icols <- sapply(1:3, FUN=function(i) c(grep(paste0("topLayers_", ctype[i]), colnames(temp))))
			gtop <- lapply(icols, FUN=function(iv) calc.IDWinterpolateFromData(Xcoord=temp$X_WGS84, Ycoord=temp$Y_WGS84, dataVector=temp[, iv], mask=mask))
			names(gtop) <- ctype				
			par(mfg=c(2, ie))
			panel.map(fgrid=gtop, mask=mask, zlim=c(0, 1), label_TF=FALSE)
			if(ie == 1) mtext(text=paste("Soil texture 0 -", depthTopBottomLayers_cm, "cm"), side=ifelse(byTreatment, 2, 3), line=ifelse(byTreatment, 2, 0), las=0)
			
			temp$bottomLayers_Silt <- 1 - (temp$SWinput_Soil_bottomLayers_Sand_fraction + temp$SWinput_Soil_bottomLayers_Clay_fraction)
			icols <- sapply(1:3, FUN=function(i) c(grep(paste0("bottomLayers_", ctype[i]), colnames(temp))))
			gbottom <- lapply(icols, FUN=function(iv) calc.IDWinterpolateFromData(Xcoord=temp$X_WGS84, Ycoord=temp$Y_WGS84, dataVector=temp[, iv], mask=mask))
			names(gbottom) <- ctype				
			par(mfg=c(3, ie))
			panel.map(fgrid=gbottom, mask=mask, zlim=c(0, 1), label_TF=FALSE)
			if(ie == 1) mtext(text=paste("Soil texture >", depthTopBottomLayers_cm, "cm"), side=ifelse(byTreatment, 2, 3), line=ifelse(byTreatment, 2, 0), las=0)
			
			if(ie == 1){		
				par(mfg=c(4, ie))
				draw.TernaryPanel(dimnames=ctype)
			} else {
				plot.new()
			}
			
		} else {
			for(i in 1:(1+length(varSoil))) plot.new()
		}
	}
	par(op)
	dev.off()
}


#---Example: Vegetation composition
#Draw global maps of vegetation composition for each treatment and for each climate scenario
map.VegComp <- function(dir.map, figname){
	varVegComp <- c("SWinput_Composition")
	i_ShGrTr <- c(2, 1, 3)
	ctype <- c("C3ofGrasses", "C4ofGrasses")

	pdf(height=3*nrow(climCat), width=5*length(trLevels_Experiment), file=file.path(dir.map, figname))
	op <- par(mfrow=c(nrow(climCat), length(trLevels_Experiment)), mar=c(2, 5, 2, 0.1), las=1)
	for(ic in 1:nrow(climCat)){
		for(ie in seq_along(trLevels_Experiment)){
			temp <- get.SeveralOverallVariables_ofStudy(responseName=c("X_WGS84", "Y_WGS84", varVegComp), i_climCat=ic, whereClause=paste0("Experimental_Label=", shQuote(trLevels_Experiment[ie])))
			if(all(!is.na(temp))){
				if(sum(itemp <- sapply(ctype, FUN=function(x) grepl(x, colnames(temp)))) == 2L){ #C3 and C4 data available
					temp2 <- cbind(temp[, c(1:2, 2+i_ShGrTr[1])], temp[, 2+i_ShGrTr[2]] * temp[, which(itemp[, 1])], temp[, 2+i_ShGrTr[2]] * temp[, which(itemp[, 2])])
					gtemp <- lapply(1:3, FUN=function(iv) calc.IDWinterpolateFromData(Xcoord=temp2$X_WGS84, Ycoord=temp2$Y_WGS84, dataVector=temp2[, 2+iv], mask=mask))
					names(gtemp) <- c("shrub", "C3-grass", "C4-grass")				
				} else {
					gtemp <- lapply(i_ShGrTr, FUN=function(iv) calc.IDWinterpolateFromData(Xcoord=temp$X_WGS84, Ycoord=temp$Y_WGS84, dataVector=temp[, 2+iv], mask=mask))
					names(gtemp) <- tolower(vegtypes)
				}
				panel.map(fgrid=gtemp, mask=mask, zlim=c(0, 1), label_TF=(ic==1 && ie==1))
			} else {
				plot.new()
			}
			if(ie==1) mtext(text=rownames(climCat)[ic], side=2, line=2, las=0)
			if(ic==1) mtext(text=trLevels_Experiment[ie], side=3)
		}
	}
	par(op)
	dev.off()
}

#---Example: Monthly biomass values
#Draw lineplots for each region for different climate conditions
fig.MonthlyBiomass <- function(dir.fig, figname1, figname2, iexperimental=1){
	varVegComp <- c("SWinput_Composition")
	varBiomass <- c("Grass_Litter", "Shrub_Litter", "Grass_LiveBiomass", "Shrub_LiveBiomass", "Grass_TotalBiomass", "Shrub_TotalBiomass")
	vs <- c("Litter", "LiveBiomass", "TotalBiomass")
	
	draw.MonthlyBiomass <- function(ic, ymax, iexperimental=1, figname){
		pdf(height=3*length(vs), width=5*length(trLevels_Region), file=file.path(dir.fig, figname))
		op <- par(mfcol=c(length(vs), length(trLevels_Region)), mar=c(4, 5, 2, 0.1), las=1)
		for(ir in seq_along(trLevels_Region)){
			temp <- get.SeveralOverallVariables_ofStudy(responseName=c(varVegComp, varBiomass), MeanOrSD="Mean", i_climCat=ic, whereClause=paste0("Experimental_Label=", shQuote(trLevels_Experiment[iexperimental]), " AND Region=", shQuote(trLevels_Region[ir])))
			if(sum(is.na(temp)) > 0){
				message("NAs in data for region: ", ir)
				temp <- temp[complete.cases(temp), ]
			}
			if(nrow(temp) > 0){
				vegcomp <- temp[, grep("Composition", colnames(temp))][, 1:3]
				for(iv in seq_along(vs)){
					tempV <- temp[, grep(vs[iv], colnames(temp))]
		
					tempV1 <- tempV[, grep(vegtypes[1], colnames(tempV))]
					tempV2 <- tempV[, grep(vegtypes[2], colnames(tempV))]
					tempV0 <- tempV1 * vegcomp[, grep(vegtypes[1], colnames(vegcomp))] + tempV2 * vegcomp[, grep(vegtypes[2], colnames(vegcomp))]
		
					v0 <- apply(tempV0, 2, FUN=function(x) c(min(x), mean(x), max(x)))
					v1 <- apply(tempV1, 2, FUN=function(x) c(min(x), mean(x), max(x)))
					v2 <- apply(tempV2, 2, FUN=function(x) c(min(x), mean(x), max(x)))
		
					matplot(mo, cbind(v0[2, ], v1[2, ], v2[2, ]), type="l", bty="l", col=c("blue", "red", "darkgreen"), lwd=2, lty=1, ylim=c(0, ymax[iv]), xlab=ifelse(iv==length(vs), "Month", ""), ylab=ifelse(ir==1, paste(vs[iv], "(g/m2)"), ""))
					polygon(x=c(mo, rev(mo)), y=c(v0[1, ], rev(v0[3, ])), col=col2alpha("blue", 0.3), border=NA)
					polygon(x=c(mo, rev(mo)), y=c(v1[1, ], rev(v1[3, ])), col=col2alpha("red", 0.3), border=NA)
					polygon(x=c(mo, rev(mo)), y=c(v2[1, ], rev(v2[3, ])), col=col2alpha("darkgreen", 0.3), border=NA)
		
					if(iv == 1) title(main=paste0(regionLabel[trLevels_Region[ir]], " (n = ", nrow(temp), ")"))
					if(ir == 1 && iv == 1) legend(x="topright", bty="n", legend=c("Combined vegetation", paste("100% of", vegtypes[1:2])), col=c("blue", "red", "darkgreen"), lwd=1, lty=1)
				}
			} else {
				for(iv in seq_along(vs)) plot.new()
			}
		}
		par(op)
		dev.off()
	}

	ymax <- c(400, 400, 700)
	if(sum(temp <- colnames(TreatmentDefinitions) %in% c("Vegetation_TotalBiomass_ScalingFactor", "Vegetation_Litter_ScalingFactor")) > 0){
		ymax <- ymax * max(TreatmentDefinitions[iexperimental, temp], na.rm=TRUE)
	}
	
	draw.MonthlyBiomass(ic=1, ymax=ymax, iexperimental=iexperimental, figname=figname1)
	draw.MonthlyBiomass(ic=3, ymax=ymax, iexperimental=iexperimental, figname=figname2)
}


#---Example: Rooting distribution
#Draw lineplots for each region for different climate conditions
fig.RootingDistribution <- function(dir.fig, figname, iexperimental=1, maxRootDepth_cm=150){
	varVegComp <- c("SWinput_Composition")
	varRoots <- c("TranspirationCoefficients")

	pdf(height=3*nrow(climCat), width=5*length(trLevels_Region), file=file.path(dir.fig, figname))
	op <- par(mfrow=c(nrow(climCat), length(trLevels_Region)), mar=c(4, 5, 2, 0.1), las=1)
	for(ic in 1:nrow(climCat)){
		for(ir in seq_along(trLevels_Region)){
			temp <- get.SeveralOverallVariables_ofStudy(responseName=c(varVegComp, varRoots), MeanOrSD="Mean", i_climCat=ic, whereClause=paste0("Experimental_Label=", shQuote(trLevels_Experiment[iexperimental]), " AND Region=", shQuote(trLevels_Region[ir])))
			if(nrow(temp) > 0){
				vegcomp <- temp[, itemp <- grep("Composition", colnames(temp))][, 1:3]
				compShrub <- vegcomp[, grep(vegtypes[1], colnames(vegcomp))]
				compGrass <- vegcomp[, grep(vegtypes[2], colnames(vegcomp))]
				temp <- temp[, -itemp]
	
				tempV1 <- temp[, grepl(vegtypes[1], colnames(temp)) & !grepl("Layer", colnames(temp))]
				if(length(iNot <- which(!(compShrub >= 0))) > 0) tempV1[iNot, ] <- NA
				tempV2 <- temp[, grepl(vegtypes[2], colnames(temp)) & !grepl("Layer", colnames(temp))]
				if(length(iNot <- which(!(compGrass >= 0))) > 0) tempV2[iNot, ] <- NA
				tempV0 <- tempV1 * compShrub + tempV2 * compGrass
				
				Lmax1 <- 1:sum(apply(tempV0, 2, FUN=function(x) any(!is.na(x) & x >= 0)))
				v0 <- t(apply(apply(tempV0[, Lmax1], 1, cumsum), 1, FUN=function(x) c(min(x, na.rm=TRUE), mean(x, na.rm=TRUE), max(x, na.rm=TRUE))))
				v1 <- t(apply(apply(tempV1[, Lmax1], 1, cumsum), 1, FUN=function(x) c(min(x, na.rm=TRUE), mean(x, na.rm=TRUE), max(x, na.rm=TRUE))))
				v2 <- t(apply(apply(tempV2[, Lmax1], 1, cumsum), 1, FUN=function(x) c(min(x, na.rm=TRUE), mean(x, na.rm=TRUE), max(x, na.rm=TRUE))))
				
				#Lmax2 <- 1:(1 + max(sapply(1:ncol(v0), FUN=function(i) which(v0[, i] >= 1 - sqrt(.Machine$double.neg.eps))[1])))
				Lmax2 <- 1:(1 + max(Lmax1))
				v0 <- rbind(rep(0, 3), v0)[Lmax2, ]
				v1 <- rbind(rep(0, 3), v1)[Lmax2, ]
				v2 <- rbind(rep(0, 3), v2)[Lmax2, ]
				xt <- layerDepths_cm[Lmax2]
				
				matplot(cbind(v0[, 2], v1[, 2], v2[, 2]), xt, type="l", bty="l", col=c("blue", "red", "darkgreen"), lwd=2, lty=1, xlim=c(0, 1.1), ylim=c(maxRootDepth_cm, 0), xlab=ifelse(ic==nrow(climCat), "Cummulative root fraction (-)", ""), ylab=ifelse(ir==1, paste0("Soil depth (cm)\n", rownames(climCat)[ic]), ""))
				polygon(x=c(v0[, 1], rev(v0[, 3])), y=c(xt, rev(xt)), col=col2alpha("blue", 0.3), border=NA)
				polygon(x=c(v1[, 1], rev(v1[, 3])), y=c(xt, rev(xt)), col=col2alpha("red", 0.3), border=NA)
				polygon(x=c(v2[, 1], rev(v2[, 3])), y=c(xt, rev(xt)), col=col2alpha("darkgreen", 0.3), border=NA)
	
				if(ic == 1) title(main=paste0(regionLabel[trLevels_Region[ir]], " (n = ", nrow(temp), ")"))
				if(ir == 1 && ic == 1) legend(x="bottomleft", bty="n", legend=c("Combined vegetation", paste("100% of", vegtypes[1:2])), col=c("blue", "red", "darkgreen"), lwd=1, lty=1)
			} else {
				plot.new()
			}
		}
	}
	par(op)
	dev.off()
}


#---Example: Climate change scenarios
#Draw lineplots for each region for different climate conditions
fig.MonthlyClimateScenarioDelta <- function(dir.fig, figname){
	varClimChange <- c("SWinput_ClimatePerturbations_PrcpMultiplier", "SWinput_ClimatePerturbations_TmaxAddand", "SWinput_ClimatePerturbations_TminAddand")
	ylabs <- c("Scenario PPT/Current PPT (-)", "Scenario Tmax - Current Tmax (C)", "Scenario Tmin - Current Tmin (C)")
	ymax <- c(3, 9, 9)
	pdf(height=3*length(varClimChange), width=5*length(trLevels_Region), file=file.path(dir.fig, figname))
	op <- par(mfcol=c(length(varClimChange), length(trLevels_Region)), mar=c(4, 5, 2, 0.1), las=1)
	for(ir in seq_along(trLevels_Region)){
		temp <- lapply(2:nrow(climCat), FUN=function(ic) get.SeveralOverallVariables_ofStudy(responseName=varClimChange, MeanOrSD="Mean", i_climCat=ic, whereClause=paste0("Experimental_Label=", shQuote(trLevels_Experiment[1]), " AND Region=", shQuote(trLevels_Region[ir]))))
		tMean <- sapply(temp, FUN=function(dat) apply(dat, 2, mean, na.rm=TRUE))
		tSD <- sapply(temp, FUN=function(dat) apply(dat, 2, sd, na.rm=TRUE))
		for(iv in seq_along(varClimChange)){
			matplot(mo, tMean[(iv-1)*12+mo, ], col=colors6[-1], lty=1, ylim=c(0, ymax[iv]), type="l", bty="l", xlab=ifelse(iv==length(varClimChange), "Time (Month)", ""), ylab=ifelse(ir==1, ylabs[iv], ""))
			if(iv == 1) abline(h=1, lwd=0.5)
			for(ic in 2:nrow(climCat)) polygon(x=c(mo, rev(mo)), y=c(tMean[(iv-1)*12+mo, ic-1]-tSD[(iv-1)*12+mo, ic-1], rev(tMean[(iv-1)*12+mo, ic-1]+tSD[(iv-1)*12+mo, ic-1])), col=col2alpha(colors6[ic], 0.3), border=NA)
			if(iv == 1 && ir == 1) legend(x="topright", legend=rownames(climCat)[-1], lty=1, col=colors6[2:nrow(climCat)], bty="o", box.col="white", bg="white")
			if(iv == 1) title(main=paste0(regionLabel[trLevels_Region[ir]], " (n = ", paste(sapply(temp, nrow), collapse=", "), ")"))
		}
	}
	par(op)
	dev.off()
}


#---Example: Variables from the 'overall' table

#Draw boxplots for each variable showing differences among climate scenarios and treatments
fig.OverallClimate <- function(dir.fig, figname, vars=c("MAP_mm_mean", "MAT_C_mean", "AET_mm_mean", "PET_mm_mean"), iexperimentals=seq_along(trLevels_Experiment)){
	pdf(height=3*length(vars), width=5*length(iexperimentals), file=file.path(dir.fig, figname))
	op <- par(mfrow=c(length(vars), length(iexperimentals)), mar=c(3, 5, 2, 0.1), las=1)
	for(iv in seq_along(vars)){
		for(ie in iexperimentals){
			temp <- lapply(1:nrow(climCat), FUN=function(ic) get.SeveralOverallVariables_ofStudy(responseName=vars[iv], i_climCat=ic, whereClause=paste0("Experimental_Label=", shQuote(trLevels_Experiment[ie]))))
			boxplot(temp, notch=TRUE, names=if(iv==length(vars)) paste0(rownames(climCat), "\nn = ", sapply(temp, length)) else rep("", length(temp)), xlab="", ylab=ifelse(ie == 1, vars[iv], ""))
			if(iv == 1) title(main=trLevels_Experiment[ie])
		}
	}
	par(op)
	dev.off()
}

fig.OverallClimate2 <- function(dir.fig, figname, names=NULL, notch=TRUE, vars=c("MAP_mm_mean", "MAT_C_mean", "AET_mm_mean", "PET_mm_mean"), iexperimentals=seq_along(trLevels_Experiment)){
	h.panel <- 2.5; w.panel <- 5/12*max(12, length(iexperimentals)); w.edge <- 0.75; h.edge <- 3
	pdf(height=h.edge+h.panel*length(vars), width=w.edge+w.panel*nrow(climCat), file=file.path(dir.fig, figname))
	layout(matrix(1:((1+length(vars))*(1+nrow(climCat))), ncol=1+nrow(climCat), byrow=TRUE), heights=c(rep(h.panel, times=length(vars)), h.edge), widths=c(w.edge, rep(w.panel, times=nrow(climCat))))
	op <- par(mar=c(0.5, 0, 1, 0), mgp=c(1.5, 0.75, 0), las=3, xpd=FALSE, cex=1)
	
	for(iv in seq_along(vars)){
		plot.new()
		for(ic in 1:nrow(climCat)){
			temp <- lapply(iexperimentals, FUN=function(ie) get.SeveralOverallVariables_ofStudy(responseName=vars[iv], i_climCat=ic, whereClause=paste0("Experimental_Label=", shQuote(trLevels_Experiment[ie]))))
			if(iv == length(vars)) if(is.null(names)){
				names <- trLevels_Experiment[iexperimentals]
			} else {
				names <- names[seq_along(iexperimentals)]
			}
			boxplot(temp, notch=notch, names=rep("", length(temp)), xlab="", ylab="", axes=FALSE)
			axis(side=1, at=seq_along(iexperimentals), labels=if(iv == length(vars)) names else FALSE)
			axis(side=2, labels=(ic == 1))
			if(ic == 1) mtext(side=2, line=1.5, text=vars[iv], las=3) 
			if(iv == 1) title(main=rownames(climCat)[ic])
		}
	}
	par(op)
	dev.off()
}


#Draw boxplots for one variable and for each region showing differences among climate scenarios
fig.AET1 <- function(dir.fig, figname, iexperimentals=seq_along(trLevels_Experiment)){
	varAET <- "AET_mm_mean"
	pdf(height=3*length(trLevels_Region), width=5*length(iexperimentals), file=file.path(dir.fig, figname))
	op <- par(mfrow=c(length(trLevels_Region), length(iexperimentals)), mar=c(3, 5, 2, 0.1), las=1)
	for(ir in seq_along(trLevels_Region)){
		for(ie in iexperimentals){
			temp <- lapply(1:nrow(climCat), FUN=function(ic) get.SeveralOverallVariables_ofStudy(responseName=varAET, i_climCat=ic, whereClause=paste0("Experimental_Label=", shQuote(trLevels_Experiment[iexperimentals]), " AND Region=", shQuote(trLevels_Region[ir]))))
			boxplot(temp, notch=TRUE, names=paste0(rownames(climCat), "\nn = ", sapply(temp, length)), xlab="", ylab="AET (mm)", main=regionLabel[trLevels_Region[ir]])
			if(ir == 1) title(main=trLevels_Experiment[ie])
		}
	}
	par(op)
	dev.off()
}

#Draw boxplots for one variable and for each region and for each treatment showing differences among climate scenarios
fig.AET2 <- function(dir.fig, figname){
	varAET <- "AET_mm_mean"
	pdf(height=3*length(trLevels_Region), width=5*length(trLevels_Experiment), file=file.path(dir.fig, figname))
	op <- par(mfrow=c(length(trLevels_Region), length(trLevels_Experiment)), mar=c(3, 6, 2, 0.1), las=1)
	for(ir in seq_along(trLevels_Region)){
		for(ie in seq_along(trLevels_Experiment)){
			temp <- lapply(1:nrow(climCat), FUN=function(ic) get.SeveralOverallVariables_ofStudy(responseName=varAET, i_climCat=ic, whereClause=paste0("Experimental_Label=", shQuote(trLevels_Experiment[ie]), " AND Region=", shQuote(trLevels_Region[ir]))))
			if(all(sapply(temp, FUN=function(x) length(x) > 0))){
				boxplot(temp, notch=TRUE, names=paste0(rownames(climCat), "\nn = ", sapply(temp, length)), xlab="", ylab=ifelse(ie==1, paste0(regionLabel[trLevels_Region[ir]], "\nAET (mm)"), ""), main=ifelse(ir==1, trLevels_Experiment[ie], ""))
			} else {
				plot.new()
				if(ie == 1) mtext(side=2, line=par("mgp")[1], text=paste0(regionLabel[trLevels_Region[ir]], "\nAET (mm)"), las=0, cex=par("cex"))
			}
		}
	}
	par(op)
	dev.off()
}

#Draw global maps for one variable and for each treatment and for each climate scenario
map.AET3 <- function(dir.map, figname){
	varAET <- "AET_mm_mean"
	ylim <- c(0, 1500)
	pdf(height=3*nrow(climCat), width=5*length(trLevels_Experiment), file=file.path(dir.map, figname))
	op <- par(mfrow=c(nrow(climCat), length(trLevels_Experiment)), mar=c(2, 5, 2, 0.1), las=1)
	for(ic in 1:nrow(climCat)){
		for(ie in seq_along(trLevels_Experiment)){
			temp <- get.SeveralOverallVariables(responseName=c("X_WGS84", "Y_WGS84", varAET), MeanOrSD="Mean", i_climCat=ic, whereClause=paste0("Experimental_Label=", shQuote(trLevels_Experiment[ie])))
			if(all(!is.na(temp[, 3]))){
				gtemp <- calc.IDWinterpolateFromData(Xcoord=temp$X_WGS84, Ycoord=temp$Y_WGS84, dataVector=temp[, 3], mask=mask)
				panel.map(fgrid=gtemp, mask=mask, colors=rev(colors5), colmask="white", zlim=ylim, label_TF=(ic==1 && ie==1))
			} else {
				plot.new()
			}
			if(ie==1) mtext(text=rownames(climCat)[ic], side=2, line=2, las=0)
			if(ic==1) mtext(text=trLevels_Experiment[ie], side=3)
		}
	}
	par(op)
	dev.off()
}


map.TbToT <- function(dir.map, figname){
	varTbT <- "TranspirationBottomToTranspirationTotal_fraction_mean"
	ylim <- c(0, 1)
	pdf(height=3*nrow(climCat), width=5*length(trLevels_Experiment), file=file.path(dir.map, figname))
	op <- par(mfrow=c(nrow(climCat), length(trLevels_Experiment)), mar=c(2, 5, 2, 0.1), las=1)
	for(ic in 1:nrow(climCat)){
		for(ie in seq_along(trLevels_Experiment)){
			temp <- get.SeveralOverallVariables(responseName=c("X_WGS84", "Y_WGS84", varTbT), MeanOrSD="Mean", i_climCat=ic, whereClause=paste0("Experimental_Label=", shQuote(trLevels_Experiment[ie])))
			if(any(!is.na(temp[, 3]))){
				gtemp <- calc.IDWinterpolateFromData(Xcoord=temp$X_WGS84, Ycoord=temp$Y_WGS84, dataVector=temp[, 3], mask=mask)
				panel.map(fgrid=gtemp, mask=mask, colors=rev(colors3), colmask="white", zlim=ylim, label_TF=(ic==1 && ie==1))
			} else {
				plot.new()
			}
			if(ie==1) mtext(text=rownames(climCat)[ic], side=2, line=2, las=0)
			if(ic==1) mtext(text=trLevels_Experiment[ie], side=3)
		}
	}
	par(op)
	dev.off()
}




#-------------------------------


#---Example: Variable from a 'daily aggregated' table

#Draw plots for each soil layer showing differences among climate scenarios
fig.SWP1 <- function(dir.fig, figname, title=NULL, iexperimental=1, layerN=8){
	tblSWPdoy <- "aggregation_doy_SWP"
	ylim <- c(-10, 0)
	pdf(height=3*layerN, width=5, file=file.path(dir.fig, figname))
	op <- par(mfrow=c(layerN, 1), mar=c(4, 5, 2, 0.1), las=1)
	for(il in 1:layerN){
		temp <- lapply(1:nrow(climCat), FUN=function(ic) get.Table_ofStudy(responseName=tblSWPdoy, i_climCat=ic, whereClause=paste0("Experimental_Label=", shQuote(trLevels_Experiment[iexperimental]), " AND Soil_Layer=", shQuote(il))))
		tMean <- sapply(temp, FUN=function(dat) apply(dat[, doy], 2, mean, na.rm=TRUE))
		tSD <- sapply(temp, FUN=function(dat) apply(dat[, doy], 2, sd, na.rm=TRUE))
		matplot(doy, tMean, col=colors6, lwd=2, lty=1, ylim=ylim, type="l", bty="l", xlab=ifelse(il==layerN, "Time (doy)", ""), ylab="SWP (MPa)")
		for(ic in 1:nrow(climCat)) polygon(x=c(doy, rev(doy)), y=c(tMean[, ic]-tSD[, ic], rev(tMean[, ic]+tSD[, ic])), col=col2alpha(colors6[ic], 0.3), border=NA)
		text(x=axTicks(1)[1], y=axTicks(2)[1], adj=0, labels=paste0(paste(layerDepths_cm[il], layerDepths_cm[il+1], sep="-"), " cm"))
		if(il == 1){
			legend(x="topright", legend=rownames(climCat), lty=1, col=colors6[1:nrow(climCat)], bty="n")
			title(main=ifelse(is.null(title), trLevels_Experiment[iexperimental], title))
		}
	}
	par(op)
	dev.off()
}

#Draw plots for each soil layer and for each region showing differences among climate scenarios
fig.SWP2 <- function(dir.fig, figname, iexperimental=1, layerN=8){
	tblSWPdoy <- "aggregation_doy_SWP"
	ylim <- c(-10, 0)
	pdf(height=3*layerN, width=5*length(trLevels_Region), file=file.path(dir.fig, figname))
	op <- par(mfrow=c(layerN, length(trLevels_Region)), mar=c(4, 5, 2, 0.1), las=1)
	for(il in 1:layerN){
		for(ir in seq_along(trLevels_Region)){
			temp <- lapply(1:nrow(climCat), FUN=function(ic) get.Table_ofStudy(responseName=tblSWPdoy, MeanOrSD="Mean", i_climCat=ic, whereClause=paste0("Experimental_Label=", shQuote(trLevels_Experiment[iexperimental]), " AND Soil_Layer=", shQuote(il), " AND Region=", shQuote(trLevels_Region[ir]))))
			tMean <- sapply(temp, FUN=function(dat) apply(dat[, doy], 2, mean, na.rm=TRUE))
			tSD <- sapply(temp, FUN=function(dat) apply(dat[, doy], 2, sd, na.rm=TRUE))
			matplot(doy, tMean, col=colors6, lty=1, ylim=ylim, type="l", bty="l", xlab=ifelse(il==layerN, "Time (doy)", ""), ylab=ifelse(ir==1, "SWP (MPa)", ""))
			for(ic in 1:nrow(climCat)) polygon(x=c(doy, rev(doy)), y=c(tMean[, ic]-tSD[, ic], rev(tMean[, ic]+tSD[, ic])), col=col2alpha(colors6[ic], 0.3), border=NA)
			if(ir == 1) text(x=axTicks(1)[1], y=axTicks(2)[1], adj=0, labels=paste0(paste(layerDepths_cm[il], layerDepths_cm[il+1], sep="-"), " cm"))
			if(il == 1 && ir == 1) legend(x="topright", legend=rownames(climCat), lty=1, col=colors6[1:nrow(climCat)], bty="o", box.col="white", bg="white")
			if(il == 1) title(main=paste0(regionLabel[trLevels_Region[ir]], " (n = ", paste(sapply(temp, nrow), collapse=", "), ")"))
		}
	}
	par(op)
	dev.off()
}

#-------------------------------
