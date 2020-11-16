CRAFTY_CoBRA
=============

CRAFTY Component-Based Role Agents (CoBRA) in continuing development of the CRAFTY CoBRA Impressions model in 2020. 

See [documentation](http://crafty-abm.sourceforge.net/) for more information.



# CRAFTY CoBRA IMPRESSIONS EU Model

## Configuration

The base configuration is:

* startTick: 2010
* endTick:	?
* world:	EU28
* scenario:	base01
* FRs:		?
* BTs:		Pseudo
* Preferences: ?
* Capitals:	Cprod, Fprod, Infra, Lprod, Nat, Econ (?)
* Services:	Meat, Cereal, Recreation, Timber

## Parameterisation

There are some R scripts that ease the creation of configuration file from templates.
These can be run all at once by executing './config/R/base01/createbatch/createWorld.R
The scripts in './config/R/base01/createbatch/' can be copied to another subfolder, e.g. './config/R/base02' and adjusted to create another set of scenarios.

NOTE: the simp configuration must be correct before the scripts may be applied (e.g., simp$mdata$aftNames)

Agenda for defining agent types:

1. Adapt '/config/R/simp-machine_cluster.R' (consider to rename it) and execute
1. Configure 'simp$mdata$aftNames' ('./config/R/simpBasic.R')
1. Configure './data/agents/FunctionalRoles.xml'
2. Define properties in './data/agents/template/AFT.csv'
3. Run './config/R/base01/createbatch/createWorld.R' (calls 'createAftParamCSV.R' and 'create1by1RunCSV.R')
4. Define production in './data/production/<AFT>.csv'


## Some Notes

* CRAFTY-CoBRA currently issues a number of warnings from LEventbus. They basically mean that decision making
processes are triggered, but no actual decision for that trigger configured. In most cases the warnings can be 
ignored.

## Post-Processing
The folder ./config/R contains templates to aggregate and visualise simulation output data with R.
See [crafty wiki](https://www.wiki.ed.ac.uk/display/CRAFTY/Post-Processing) for details.
