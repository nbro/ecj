package ec.app.command;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import ec.EvolutionState;
import ec.Individual;
import ec.Population;
import ec.Problem;
import ec.coevolve.GroupedProblemForm;
import ec.simple.SimpleFitness;
import ec.simple.SimpleProblemForm;
import ec.util.Parameter;
import ec.vector.DoubleVectorIndividual;

public class CommandProblem extends Problem implements SimpleProblemForm, GroupedProblemForm
    {
    private static final long serialVersionUID = 1;

    public final static String P_COMMAND_PATH = "command-path";
    public final static String DELIMITER = ",";

    private String commandPath;

    public void setup(final EvolutionState state, final Parameter base)
        {
        this.commandPath = state.parameters.getString(base.push(P_COMMAND_PATH), null);
        }

	@Override
	public void preprocessPopulation(EvolutionState state, Population pop, boolean[] prepareForFitnessAssessment,
            boolean countVictoriesOnly)
        {
		// Do nothing
	    }

	@Override
	public int postprocessPopulation(EvolutionState state, Population pop, boolean[] assessFitness,
            boolean countVictoriesOnly)
        {
		// Do nothing
		return 0;
	    }

	@Override
	public void evaluate(EvolutionState state, Individual[] individuals, boolean[] updateFitness, boolean countVictoriesOnly,
            int[] subpops, int threadnum)
        {
        assert(state != null);
        assert(individuals != null);
        assert(individuals.length > 0);
        
        try
            {
            final String simulationResult = runCommand(individuals);
            final List<Double> fitnesses = parseFitnesses(simulationResult);

            if (fitnesses.size() != individuals.length)
                    throw new IllegalStateException(String.format("%s: Sent %d individuals to external command %s, but the returned simulation results had %d lines.", CommandProblem.class.getSimpleName(), individuals.length, commandPath, fitnesses.size()));
                
            for (int i = 0; i < individuals.length; i++)
                {
                final Individual ind = individuals[i];
                ind.fitness = new SimpleFitness();
                ((SimpleFitness)ind.fitness).setFitness(state, fitnesses.get(i), false);
                ind.evaluated = true;
                }
            }
        catch (final Exception e)
            {
            state.output.fatal(e.toString());
            }
	    }

	@Override
    public void evaluate(EvolutionState state, Individual ind, int subpopulation, int threadnum)
        {
		evaluate(state, new Individual[] { ind }, null, false, null, threadnum);
        }

    private String runCommand(final Individual[] individuals) throws IOException, InterruptedException
        {
        final Process p = Runtime.getRuntime().exec(commandPath);

        // Write genomes to the command's stdin
        final Writer carlsimInput = new BufferedWriter(new OutputStreamWriter(p.getOutputStream()));
        writeIndividuals(individuals, carlsimInput);
        carlsimInput.close(); // Sends EOF
        p.waitFor();

        // Read the output from the command's stdout
        final BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
        final StringBuilder sb = new StringBuilder();
        String line = "";			
        while ((line = reader.readLine())!= null) {
            sb.append(line).append("\n");
        }
        return sb.toString();
        }
    
    /** Take a list of DoubleVectorIndividuals and output them to a tab-delimited file.
     * 
     * @param individuals A non-empty population of Individuals.  If any element is null an IAE is thrown.
     * @param outWriter A non-null Writer to output the CSV to.  When this method returns it does *not* close the outWriter.
     * @return Nothing.  Side effects: Writes a tab-delimited CSV to outWriter, one row per individual, one column per gene.
     * @throws IOException 
     */
    public static void writeIndividuals(final Individual[] individuals, final Writer outWriter) throws IOException
        {
        assert(outWriter != null);
        assert(individuals != null);
        assert(individuals.length > 0);
        
        for (final Individual ind : individuals)
            {
            final double[] genome = ((DoubleVectorIndividual) ind).genome;
            assert(genome.length > 0);
            outWriter.write(String.valueOf(genome[0]));
            for (int i = 1; i < genome.length; i++)
                outWriter.write(String.format("%s%f", DELIMITER, genome[i]));
            outWriter.write(String.format("%n"));
            }
        }

    public static List<Double> parseFitnesses(final String simResult)
        {
            final String[] lines = simResult.split("\n");
            final List<Double> fitnesses = new ArrayList<>();
            for (final String f : lines)
                {
                final double realFitness = Double.valueOf(f);
                fitnesses.add(realFitness);
                }
            return fitnesses;
        }
    }