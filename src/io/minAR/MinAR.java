package io.minAR;

import io.minAR.core.Analyzer;
import io.minAR.core.Extractor;
import io.minAR.util.Crypt;

import javax.crypto.SecretKey;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;

public class MinAR {

    /**
     * An enum of the various flags that can be set.
     * These flags affect the finally generated archive, and the exact setting is required in order to extract them.
     * If not flags are set, a normal archive will be generated which is not encrypted and whose contents are not compressed.
     */
    public enum FLAG{
        COMPRESSED,
        ENCRYPTED,
        KEY_IS_STRING,
        KEY_IS_FILE;
    }

    private static ArrayList<FLAG> activated_flags = new ArrayList<>();

    // Defaults

    /** The Default flag set, compression only */
    public static final FLAG[] _default_no_enc  = {FLAG.COMPRESSED};
    /** The Default flag set, compression and encryption, key from file */
    public static final FLAG[] _default_enc_file= {FLAG.COMPRESSED, FLAG.ENCRYPTED, FLAG.KEY_IS_FILE};
    /** The Default flag set, compression and encryption, key from string */
    public static final FLAG[] _default_enc_str = {FLAG.COMPRESSED, FLAG.ENCRYPTED, FLAG.KEY_IS_STRING};


    /**
     * Toggle the given flag i.e, deactivate the flag if activated, activate the flag if deactivated.
     * @param flag the flag to be toggled.
     */
    public static void toggleFlag(FLAG flag){
        if(activated_flags.contains(flag)) activated_flags.remove(flag);
        else activated_flags.add(flag);
    }

    /**
     * Create an archive of the directory specified output it to the file.
     * @param directory the directory whose contents are to be  archived
     * @param archive_file the path of the file.
     * No flags are mandatory for this operation. But, {@link FLAG}.COMPRESSED and {@link FLAG}.ENCRYPTED are recommended.
     */
    public static void outputArchive(String directory, String archive_file){
        File dir = new File(directory);
        if(!dir.exists()) throw new RuntimeException(new FileNotFoundException(directory + " does not exist"));
        boolean cmpr = isFlagged(FLAG.COMPRESSED);
        boolean encr = isFlagged(FLAG.ENCRYPTED);
        Analyzer analyzer = new Analyzer(dir, cmpr, encr);
        analyzer.convertToDataTree();
        analyzer.OUTPUT_minAR(archive_file);
    }

    /**
     * Checks whether the given flag is activated or not.
     * @param flag the flag to be checked
     * @return whether the flag is activated or not
     */
    public static boolean isFlagged(FLAG flag){
        return activated_flags.contains(flag);
    }

    /**
     * Toggle multiple flags
     * @param flags the flags to be toggled
     */
    public static void toggleFlags(FLAG... flags){
        for (FLAG flag: flags) {
            toggleFlag(flag);
        }
    }

    /**
     * Extract the archive into the specified directory.
     * No flags are mandatory for this operation, but if encrypted is selected, then, either {@link FLAG}{@code .KEY_IS_FILE} or {@link FLAG}{@code .KEY_IS_STRING} must be set(toggled on).
     * @param archive the archive to be extracted
     * @param directory the directory to contain the extracted contents
     * @param key this argument depends on the flag set, </br>
     *            <table border="1">
     *            <tr>
     *            <td>{@link FLAG} to be set</td> <td>Parameter description</td>
     *            </tr>
     *            <tr>
     *            <td>{@link FLAG}{@code .KEY_IS_FILE}</td> <td>The path to the .key file generated along with the archive</td>
     *            </tr>
     *            <tr>
     *            <td>{@link FLAG}{@code .KEY_IS_STRING}</td> <td>The string form the key</td>
     *            </tr>
     *            </table>
     */
    public static void extractArchive(String archive, String directory, String key){
        Extractor extractor = new Extractor(directory, archive);
        SecretKey real_key = null;
        if(isFlagged(FLAG.ENCRYPTED)){
            if(isFlagged(FLAG.KEY_IS_FILE)) real_key = Crypt.getKeyFromFile(key);
            else if(isFlagged(FLAG.KEY_IS_STRING)) real_key = Crypt.stringAsKey(key);
            else throw new IllegalStateException("Invalid Flags... 'KEY_' FLAG missing!");
        }
        extractor.analyze(isFlagged(FLAG.COMPRESSED), isFlagged(FLAG.ENCRYPTED), real_key);
        extractor.generate();
    }
}
