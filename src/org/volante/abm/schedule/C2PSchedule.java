/**
 * This file is part of
 * 
 * CRAFTY - Competition for Resources between Agent Functional TYpes
 *
 * Copyright (C) 2014 School of GeoScience, University of Edinburgh, Edinburgh, UK
 * 
 * CRAFTY is free software: You can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software 
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *  
 * CRAFTY is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * School of Geoscience, University of Edinburgh, Edinburgh, UK
 */
package org.volante.abm.schedule;


//import java.util.ArrayList;
//import java.util.LinkedHashSet;
//import java.util.List;
//import java.util.Set;
//
//import org.apache.log4j.Logger;
//import org.volante.abm.agent.Agent;
//import org.volante.abm.agent.DefaultSocialLandUseAgent;
//import org.volante.abm.agent.LandUseAgent;
//import org.volante.abm.agent.bt.InnovativeBC;
//import org.volante.abm.data.Cell;
//import org.volante.abm.data.ModelData;
//import org.volante.abm.data.Region;
//import org.volante.abm.data.RegionSet;
//import org.volante.abm.example.RegionalDemandModel;
//import org.volante.abm.institutions.global.GlobalInstitution;
//import org.volante.abm.institutions.global.GlobalInstitutionsRegistry;
//import org.volante.abm.models.WorldSynchronisationModel;
//import org.volante.abm.output.Outputs;
//import org.volante.abm.schedule.ScheduleStatusEvent.ScheduleStage;


public class C2PSchedule extends DefaultSchedule { 

	
//	 
//	
//
//
//
//### Note ###
//# Make sure no space in the name of dir path
//# Structure:
//# jdk file path + java + memory + encoding + classpath + Runner + datapath + data
//
//### Setting for cmd ###
//
//# jdk_base_path = "/usr/lib/jvm/java-8-openjdk-amd64/" # alanbuntu4
//# jdk_base_path = r"/Library/Java/JavaVirtualMachines/jdk1.8.0_171.jdk/Contents/Home/"
//jdk_base_path = r"/lrz/mnt/sys.x86_sles12/compilers/java/jdk1.8.0_112/" # LRZ
//
//jdk_bin_path = jdk_base_path + "bin/"
//
//memory_max = 'Xmx5g'  # 25 GB max in LRZ
//memory_min = 'Xms500m'
//
//
//# javaprj_path =  r'/Users/seo-b/workspace/EU1_Institution/'
//# javaprj_path = r'/Users/seo-b/workspace/Summerschool/'
//javaprj_path = r'/naslx/projects/pn69tu/di52xeg/workspace/Summerschool/'
//
//
//javaclass_path = javaprj_path + 'lib/'
//# Data path needs 'javadata_name' in it.
//# javadata_path = r'/Users/seo-b/workspace/Summerschool/data/'
//javadata_path = r'/naslx/projects/pn69tu/di52xeg/workspace/Summerschool/data/'
//
//javadata_name = 'Scenario.xml'
//
//
//
//### CRAFTY COMMAND
//CRAFTY_cmd = jdk_bin_path +'java '+ ' -' + memory_max +' -'+ memory_min + " -Dfile.encoding=UTF-8 -classpath '" + javaprj_path +'bin:' + javaprj_path +'config/log:' +  javaclass_path + "*'" + ' org.volante.abm.serialization.ModelRunner -d ' + javadata_path +' -f '+ javadata_name +' -o 0 -r 1 -n 1 -sr 0'
//
//### PLUM COMMAND
//# PLUM_base_path = '/Users/seo-b/eclipse-workspace/UNPLUM/'
//# PLUM_base_path = '/home/alan/eclipse-workspace/UNPLUM/'
//PLUM_base_path = '/naslx/projects/pn69tu/di52xeg/eclipse-workspace/UNPLUM/'
//
//# GAMS_path = '/Users/seo-b/Dropbox/KIT_Modelling/GlobalABM/PLUM/CRAFTY2PLUM/gams24.7_linux_x64_64_sfx/'
//# GAMS_path = '/home/alan/Dropbox/KIT_Modelling/GlobalABM/PLUM/CRAFTY2PLUM/gams24.7_linux_x64_64_sfx/'
//GAMS_path = '/naslx/projects/pn69tu/di52xeg/CRAFTY2PLUM/gams24.7_linux_x64_64_sfx/'
//
//PLUM_config = 'debug_config.properties' # or hind1970
//
//PLUM_cmd = jdk_bin_path +'java '+ ' -' + memory_max +' -'+ memory_min + " -Dfile.encoding=UTF-8 -DCONFIG_FILE=" + PLUM_config + " -classpath '" + PLUM_base_path + 'bin:' + GAMS_path + 'apifiles/Java/api/GAMSJavaAPI.jar' + "'" + ' ac.ed.lurg.ModelMain'
//
//####### Folders
//# simulation_wd = r"/Users/seo-b/Dropbox/KIT_Modelling/GlobalABM/PLUM/CRAFTY2PLUM/PythonInterface/"
//# simulation_wd = r"/home/alan/Dropbox/KIT_Modelling/GlobalABM/PLUM/CRAFTY2PLUM/PythonInterface/"
//simulation_wd = r"/naslx/projects/pn69tu/di52xeg/CRAFTY2PLUM/PythonInterface/"
//
//os.chdir(simulation_wd)
//
//os.environ['PATH'] = GAMS_path
//
//
//
//
//######## SIMULATION ID
//
//
//### Logging info
//# t = strftime("%Y-%m-%d %H%M%S", gmtime()) # '2018-11-05 155909'
//t = strftime('%d%b%y', gmtime())   #
//
//
//# simulation_ID = 'sim1234_16may2018' # will be
//
//simulation_ID = '1234' #  @todo perhaps more informative form..
//simulation_name = 'sim' + simulation_ID + '_' +  t
//
//log_filename = simulation_name + '.log'
//
//marker_filename_plum_started = 'plum_started'
//marker_filename_plum_done = 'plum_done'
//marker_filename_plum_error = 'plum_error'
//
//marker_filename_crafty_started = 'crafty_started'
//marker_filename_crafty_done = 'crafty_done'
//marker_filename_crafty_error = 'crafty_error'
//
//simulation_out =  simulation_wd + "Out_test/" + simulation_name + '/'
//
//
//
//
//### Target period
//st_year = 2016
//ed_year = 2016
//target_years = list(range(st_year, ed_year + 1 , 1))
//n_years = len(target_years)
//
//
//# 1) Creates a simulation folder
//
//if (not os.path.exists(simulation_out)):
//    os.makedirs(simulation_out, exist_ok=True)
//
//# 2) Simulation starts
//
//### N-year loop starts (continuing for the target period)
//
//for target_year in target_years:
//
//    print(target_year)
//
//    marker_filename_thisyear_started = str(target_year) + '_started'
//    marker_filename_thisyear_done = str(target_year) + '_done'
//
//
//    # 2-1) Creates a marker
//
//    with open(simulation_out + marker_filename_thisyear_started, 'a'):
//        os.utime(simulation_out + marker_filename_thisyear_started, None)
//
//    # 2-2) Execute PLUM
//    # PLUM simulates one year and exit.
//
//
//    with open(simulation_out + marker_filename_plum_started, 'a'):
//        os.utime(simulation_out + marker_filename_plum_started, None)
//
//    with io.open(log_filename, 'wb') as writer, io.open(log_filename, 'rb', 1) as reader:
//
//        os.chdir(PLUM_base_path)
//
//        process = subprocess.Popen(PLUM_cmd, shell=True, stdout=writer)
//
//        while process.poll() is None:
//            sys.stdout.write(reader.read().decode())
//            time.sleep(0.5)
//
//        # Read the remaining log message
//        sys.stdout.write(reader.read().decode())
//
//    os.chdir(simulation_wd)
//
//
//    # 2-3) PLUM wrap-up
//    # @todo Examine the execution result and checks the marker (stops if it says an error).
//
//    # #  If all good, changes the marker and start a CRAFTY run and wait.
//
//    # PLUM done
//    with open(simulation_out + marker_filename_plum_done, 'a'):
//        os.utime(simulation_out + marker_filename_plum_done, None)
//
//    os.remove(simulation_out + marker_filename_plum_started)
//
//
//
//
//    # 2-4) Run CRAFTY
//    # CRAFTY creates a marker and run for the year and exit
//
//    with io.open(log_filename, 'wb') as writer, io.open(log_filename, 'rb', 1) as reader:
//
//        process = subprocess.Popen(PLUM_cmd, shell=True, stdout=writer)
//        while process.poll() is None:
//
//            sys.stdout.write(reader.read().decode())
//            time.sleep(0.5)
//
//        # Read the remaining log message
//        sys.stdout.write(reader.read().decode())
//
//        # print("CRAFTY is running.........")
//        # CRAFTY started
//        with open(simulation_out + marker_filename_crafty_started, 'a'):
//            os.utime(simulation_out + marker_filename_crafty_started, None)
//
//
//        with io.open(log_filename, 'wb') as writer, io.open(log_filename
//                , 'rb', 1) as reader:
//            process = subprocess.Popen(CRAFTY_cmd, shell=True, stdout=writer)
//            while process.poll() is None:
//                sys.stdout.write(reader.read().decode())
//                time.sleep(0.5)
//
//            # Read the remaining log message
//            sys.stdout.write(reader.read().decode())
//
//        # CRAFTY done
//        # print("CRAFTY run is finished.")
//        with open(simulation_out + marker_filename_crafty_done, 'a'):
//            os.utime(simulation_out + marker_filename_crafty_done, None)
//
//        os.remove(simulation_out + marker_filename_crafty_started)
//
//
//    # 2-5) CRAFTY wrap-up
//    # @todo Examine the execution result and checks the marker (stops if it says an error).
//
//    # If all good, changes the marker and go to the next year.
//
//    # This year done
//    with open(simulation_out + marker_filename_thisyear_done, 'a'):
//        os.utime(simulation_out + marker_filename_thisyear_done, None)
//
//    os.remove(simulation_out + marker_filename_thisyear_started)
//
//
//### N-year loop ends
//
//
//
//
//
//# 6) @todo The script checks the outcome files (all yearly outcome produced?)
//#    @todo Print out the simulation summary.
//
}


