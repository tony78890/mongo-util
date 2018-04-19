package com.mongodb.shardsync;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;

public class ShardConfigSyncApp {
    
    private static Options options;

    private final static String DROP_DEST = "dropDestinationCollectionsIfExisting";
    private final static String COLL_COUNTS = "compareCounts";
    private final static String CHUNK_COUNTS = "chunkCounts";
    private final static String FLUSH_ROUTER = "flushRouter";
    private final static String MIGRATE = "migrate";
    private final static String COMPARE_CHUNKS = "compareChunks";

    @SuppressWarnings("static-access")
    private static CommandLine initializeAndParseCommandLineOptions(String[] args) {
        options = new Options();
        options.addOption(new Option("help", "print this message"));
        options.addOption(OptionBuilder.withArgName("Source cluster connection uri").hasArgs().withLongOpt("source")
                .isRequired(true).create("s"));
        options.addOption(OptionBuilder.withArgName("Destination cluster connection uri").hasArgs().withLongOpt("dest")
                .isRequired(true).create("d"));
        options.addOption(OptionBuilder.withArgName("Drop destination collections if existing")
                .withLongOpt(DROP_DEST).create(DROP_DEST));
        options.addOption(OptionBuilder.withArgName("Compare counts only (do not sync/migrate)")
                .withLongOpt(COLL_COUNTS).create(COLL_COUNTS));
        options.addOption(OptionBuilder.withArgName("Show chunk counts when collection counts differ")
                .withLongOpt(CHUNK_COUNTS).create(CHUNK_COUNTS));
        options.addOption(OptionBuilder.withArgName("Flush router config on all mongos (do not sync/migrate)")
                .withLongOpt(FLUSH_ROUTER).create(FLUSH_ROUTER));
        options.addOption(OptionBuilder.withArgName("Compare all shard chunks (do not sync/migrate)")
                .withLongOpt(COMPARE_CHUNKS).create(COMPARE_CHUNKS));
        options.addOption(OptionBuilder.withArgName("Migrate/sync config data")
                .withLongOpt(MIGRATE).create(MIGRATE));

        CommandLineParser parser = new GnuParser();
        CommandLine line = null;
        try {
            line = parser.parse(options, args);
            if (line.hasOption("help")) {
                printHelpAndExit(options);
            }
        } catch (org.apache.commons.cli.ParseException e) {
            System.out.println(e.getMessage());
            printHelpAndExit(options);
        } catch (Exception e) {
            e.printStackTrace();
            printHelpAndExit(options);
        }

        return line;
    }

    private static void printHelpAndExit(Options options) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("logParser", options);
        System.exit(-1);
    }

    public static void main(String[] args) throws Exception {
        CommandLine line = initializeAndParseCommandLineOptions(args);
        ShardConfigSync sync = new ShardConfigSync();
        sync.setSourceClusterUri(line.getOptionValue("s"));
        sync.setDestClusterUri(line.getOptionValue("d"));
        sync.init();
        if (line.hasOption(COLL_COUNTS)) {
            sync.setDoChunkCounts(line.hasOption(CHUNK_COUNTS));
            sync.compareShardCounts();
        } else if (line.hasOption(FLUSH_ROUTER)) {
            sync.flushRouterConfig();
        } else if (line.hasOption(COMPARE_CHUNKS)) {
            sync.compareChunks();
        } else if (line.hasOption(MIGRATE)) {
            sync.setDropDestinationCollectionsIfExisting(line.hasOption(DROP_DEST));
            sync.run();
        } else {
            System.out.println("Missing action");
            printHelpAndExit(options);
        }
        
        // String[] fileNames = line.getOptionValues("f");
        // client.setEndpointUrl(line.getOptionValue("u"));

    }

}
