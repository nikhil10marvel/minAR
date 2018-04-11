package io.minAR.util;

import com.esotericsoftware.minlog.Log;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.minAR.core.Analyzer.writeToFile;

public class DataUnits {

    static final int _byte = 1;
    public static final int kibibyte = 1024 * _byte;
    public static final int mebibyte = 1024 * kibibyte;
    public static final int gibibyte = 1024 * mebibyte;

    public static final int kilobyte = 1000 * _byte;
    public static final int megabyte = 1000 * kilobyte;
    public static final int gigabyte = 1000 * megabyte;
    private static final String regex= "\\$\\{(PART_NUM|OFFSET|FILE_NAME)\\}";
    private static final Pattern pattern = Pattern.compile(regex);

    public static void writeParts(int partsize, byte[] data, String format, String file) {
        int extra_off = data.length % partsize;
        int off = 0; int non_divisible =  data.length - extra_off;
        while((off + partsize) <= non_divisible){
            byte[] part = Arrays.copyOfRange(data, off, off + partsize);
            writeToFile(analyze_format(format, file, off, off/partsize), part);
            off += partsize;
        }
        int part_num = (off/partsize);
        if(extra_off != 0){
            byte[] part = Arrays.copyOfRange(data, non_divisible, data.length);
            writeToFile(analyze_format(format, file, off, part_num), part);
        }
    }

    private static String humanReadableByteCount(long bytes, boolean si) {
        int unit = si ? 1000 : 1024;
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp-1) + (si ? "" : "i");
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }

    private static String analyze_format(String format, String file, int curr_off, int num){
        String ret = format;
        Matcher matcher = pattern.matcher(format);
        while(matcher.find()){
            Log.trace(DataUnits.class.getCanonicalName(), "Full match " + matcher.group(0));
            String grp = matcher.group(1);
            switch (grp){
                case "PART_NUM":
                    ret = ret.replaceAll("\\$\\{" + grp + "}", String.valueOf(num));
                    break;
                case "OFFSET":
                    ret = format.replaceAll("\\$\\{" + grp + "}", humanReadableByteCount(curr_off, true));
                    break;

                case "FILE_NAME":
                    ret = ret.replaceAll("\\$\\{" + grp + "}", file);
                    break;

                default:
                    ret = ret.replace("\\$\\{" + grp + "\\}", "[unknown value]");
                    break;
                }
        }
        return ret;
    }

}
