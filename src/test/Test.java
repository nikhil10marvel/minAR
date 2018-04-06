package test;

import io.minAR.MinAR;

public class Test {
    
    public static void main(String[] args){
        if(args.length > 0){
            String archive_name = args[1];
            if(args[0].equals("-a")){
                String diriectory_to_be_archive = args[2];
                MinAR.toggleFlags(MinAR._default_no_enc);
                MinAR.outputArchive(diriectory_to_be_archive, archive_name);
            } else if(args[0].equals("-e")){
                String extract_directory = args[2];
                String key = args[3];
                MinAR.toggleFlags(MinAR._default_no_enc);
                MinAR.extractArchive(archive_name, extract_directory, key);
            } else {
                System.out.println("Invalid arguments");
            }
        }
    }
    
}
