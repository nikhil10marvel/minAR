package test;

import io.minAR.MinAR;
import io.minAR.util.DataUnits;

public class Test {
    private static final String NL = System.lineSeparator();
    public static void main(String[] args){
        if(args.length > 0){
            String archive_name = args[1];
            if(args[0].equals("-a")){
                String diriectory_to_be_archive = args[2];
                if(args.length > 4 && args[3].equals("-fspan")){
                    MinAR.enable_file_spanning(Integer.valueOf(args[4]) * DataUnits.kilobyte, "${FILE_NAME}.part${PART_NUM}");
                }
                MinAR.toggleFlags(MinAR._default_enc_file);
                MinAR.outputArchive(diriectory_to_be_archive, archive_name);
            } else if(args[0].equals("-e")){
                String extract_directory = args[2];
                String key = args[3];
                if(args.length > 4 && args[4].equals("-fspan")){
                    MinAR.enable_file_spanning(0, "${FILE_NAME}.part${PART_NUM}");
                }
                MinAR.toggleFlags(MinAR._default_enc_file);
                MinAR.extractArchive(archive_name, extract_directory, key);
            } else {
                System.out.println("Invalid arguments");
            }
        } else {
            System.out.println("minAR usage: " + NL +
                    "-a [archive name] [directory to archive] [-fspan [span size(KB)]]" + NL +
                    "-e [archive name] [directory to extract to] [key] -fspan" + NL);
        }
    }
    
}
