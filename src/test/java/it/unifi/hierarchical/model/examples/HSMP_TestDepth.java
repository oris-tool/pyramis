/* This program is part of the PYRAMIS library for compositional analysis of hierarchical UML statecharts.
 * Copyright (C) 2019-2021 The PYRAMIS Authors.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package it.unifi.hierarchical.model.example.hsmp;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.oristool.math.OmegaBigDecimal;
import org.oristool.math.function.GEN;

import it.unifi.hierarchical.model.CompositeState;
import it.unifi.hierarchical.model.ExitState;
import it.unifi.hierarchical.model.HierarchicalSMP;
import it.unifi.hierarchical.model.Region;
import it.unifi.hierarchical.model.SimpleState;
import it.unifi.hierarchical.model.State;
import it.unifi.hierarchical.model.Region.RegionType;

public class HSMP_TestDepth {

    public static HierarchicalSMP build() {
    
        //Level depth 2
        int depth = 2;
        
        State EA1 = new ExitState("EA1",2);
        State EA2 = new ExitState("EA2",2);
        
        State A1 = new SimpleState(
                "A1", 
                GEN.newUniform(new OmegaBigDecimal("1"), new OmegaBigDecimal("4")), 
                Arrays.asList(EA1), 
                Arrays.asList(1.0), 
                depth);
        
        State A2 = new SimpleState(
                "A2", 
                GEN.newUniform(new OmegaBigDecimal("1"), new OmegaBigDecimal("5")), 
                Arrays.asList(EA2), 
                Arrays.asList(1.0), 
                depth);
        
        
        Region RA1 = new Region(A1, RegionType.EXIT);
        Region RA2 = new Region(A2, RegionType.EXIT);
        
        depth=1;
        
        State E1 = new ExitState("E1",1);
        State E2 = new ExitState("E2",1);
        
        State B = new SimpleState(
                "B", 
                GEN.newUniform(new OmegaBigDecimal("3"), new OmegaBigDecimal("4")), 
                Arrays.asList(E1), 
                Arrays.asList(1.0), 
                depth);
        
        State C = new SimpleState(
                "C", 
                GEN.newUniform(new OmegaBigDecimal("1"), new OmegaBigDecimal("10")), 
                Arrays.asList(E2), 
                Arrays.asList(1.0), 
                depth);
                
        
        State A = new CompositeState(
                "A",  
                Arrays.asList(RA1, RA2), 
                Map.of(
                        EA1,
                        Arrays.asList(B),
                        EA2,
                        Arrays.asList(E1)),  
                Map.of(
                        EA1,
                        Arrays.asList(1.0),
                        EA2,
                        Arrays.asList(1.0)), 
                depth);
        
       
        State init = new SimpleState(
                "INIT", 
                GEN.newUniform(new OmegaBigDecimal("1"), new OmegaBigDecimal("2")), 
                Arrays.asList(A,B), 
                Arrays.asList(0.5,0.5), 
                depth);
        
        Region R1 = new Region(init, RegionType.EXIT);
        Region R2 = new Region(C, RegionType.EXIT);
        
        //Level depth 0
        depth = 0;
        
        List<State> nextStates = null;//Required to avoid ambiguity
        
        
        State S0 = new CompositeState(
                "S0",  
                Arrays.asList(R1, R2), 
                nextStates, 
                null, 
                depth);
        
        State S1 = new SimpleState(
                "S1",  
                GEN.newDeterministic(new BigDecimal("10")), 
                Arrays.asList(S0), 
                Arrays.asList(1.0), 
                depth);
        
        
        S0.setNextStates(Arrays.asList(S1), Arrays.asList(1.0));
         
        
        return new HierarchicalSMP(S0);
    }
}
