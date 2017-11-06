/*
  Copyright 2017 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
*/
package ec.co.ant;

import ec.Setup;
import ec.co.ConstructiveProblemForm;
import ec.vector.IntegerVectorIndividual;

/**
 *
 * @author Eric O. Scott
 */
public interface ConstructionRule extends Setup
{
    public abstract IntegerVectorIndividual constructSolution(PheremoneMatrix pheremones, ConstructiveProblemForm problem);
}