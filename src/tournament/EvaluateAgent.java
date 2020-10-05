package tournament;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;

import agents.IAgent;
import engine.MarioLog;
import tools.EvaluationInfo;
import tournament.run.MarioRun;
import tournament.run.MarioRunResult;
import tournament.run.MarioRunResults;
import tournament.run.MarioRunsGenerator;
import tournament.utils.Sanitize;

public class EvaluateAgent {
	
	private int seed = 0;

	private String levelOptions;
	
	private int runCount;
	
	private File resultDirFile;
	
	public EvaluateAgent(int seed, String levelOptions, int runCount, File resultDirFile) {
		this.seed = seed;
		this.levelOptions = levelOptions;
		this.runCount = runCount;
		this.resultDirFile = resultDirFile;
	}
	
	private void logFine(String agentId, String msg) {
		MarioLog.fine("[" + agentId + "] " + msg);
	}

	public MarioRunResults evaluateAgent(String agentId, IAgent agent) {
		agentId = Sanitize.idify(agentId);
		
        System.out.println(
            "Evaluating agent in " + runCount + " levels...");
		
		MarioRun[] runs = MarioRunsGenerator.generateRunList(seed, levelOptions, runCount);
		
		MarioRunResults results = new MarioRunResults();
		
		for (int i = 0; i < runs.length; ++i) {
			logFine(agentId, "LEVEL " + (i+1) + " / " + runs.length);
			
			MarioRunResult result = runs[i].run(agent);
			
			logFine(agentId, "LEVEL " + (i+1) + " / " + runs.length + " SIMULATIONS FINISHED: " + result.toString());
			
			results.addRunResults(result);			
		}
		
		System.out.println(results.toString());
		
		if (resultDirFile != null)
			outputResults(agentId, results);	
		
		return results;
	}

	private void outputResults(String agentId, MarioRunResults results) {		
		resultDirFile.mkdirs();
		
		outputAgentResults(agentId, results);
		outputAgentAvgs(agentId, results);
		outputAgentGlobalAvgs(agentId, results);
	}

	
	private void outputAgentResults(String agentId, MarioRunResults results) {
		File file = new File(resultDirFile, agentId + ".runs.csv");
		MarioLog.info("[" + agentId + "] Outputing runs into: " + file.getAbsolutePath());
		
		PrintWriter writer = null;
		try {
			writer = new PrintWriter(new FileOutputStream(file));
			
			writer.println("agentId;simulationNumber;levelSeed;" + results.getResults().get(0).getCSVHeader());
			int simulationNumber = 0;
			for (MarioConfig config : results.getConfigs()) {
                ++simulationNumber;
                writer.print(agentId);
                writer.print(";" + simulationNumber);
                writer.print(";" + config.getSeed());
                EvaluationInfo info = results.getResults().get(simulationNumber-1);
                writer.println(";" + info.getCSV());
			}
			
		} catch (Exception e) {
			throw new RuntimeException("Failed to write results into: " + file.getAbsolutePath());
		} finally {
			if (writer != null) writer.close();
		}
		
	}
	
	private void outputAgentAvgs(String agentId, MarioRunResults results) {
		File file = new File(resultDirFile, agentId + ".runs.avgs.csv");
		MarioLog.info("[" + agentId + "] Outputing runs avgs into: " + file.getAbsolutePath());
		
		PrintWriter writer = null;
		try {
			writer = new PrintWriter(new FileOutputStream(file));
			
			writer.println("agentId;configNumber;" + results.getRunResults().get(0).getCSVHeader());
			int configNumber = 0;
			for (MarioRunResult run : results.getRunResults()) {
				++configNumber;
				writer.print(agentId);
				writer.print(";" + configNumber);
				writer.println(";" + run.getCSV());				
			}
		} catch (Exception e) {
			throw new RuntimeException("Failed to write results into: " + file.getAbsolutePath());
		} finally {
			if (writer != null) writer.close();
		}
	}
	
	private void outputAgentGlobalAvgs(String agentId, MarioRunResults results) {
		File file = new File(resultDirFile, "results.csv");		
		MarioLog.info("[" + agentId + "] Outputing total avgs into: " + file.getAbsolutePath());
		
		PrintWriter writer = null;
		try {
			boolean outputHeaders = !file.exists();
			writer = new PrintWriter(new FileOutputStream(file, true));
			if (outputHeaders) {
				writer.println("agentId;configSeed;" + results.getCSVHeader());
			}
			writer.print(agentId + ";");
			writer.print(seed + ";");
			writer.println(results.getCSV());
		} catch (Exception e) {
			throw new RuntimeException("Failed to write results into: " + file.getAbsolutePath());
		} finally {
			if (writer != null) writer.close();
		}
	}



}
