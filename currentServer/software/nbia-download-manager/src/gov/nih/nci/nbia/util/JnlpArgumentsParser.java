package gov.nih.nci.nbia.util;

import gov.nih.nci.nbia.download.SeriesData;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

/**
 * @author lethai
 *
 */
public class JnlpArgumentsParser {

    public static List<SeriesData> parse(String [] args){
        int length = args.length;
        List<SeriesData> seriesDataList = new ArrayList<SeriesData>();
        for(int i=0; i< length; i++ ) {
            SeriesData series = new SeriesData();
            System.out.println("---------------------Split string----------------");
            System.out.println(args[i]);
            String[] result = StringUtils.split(args[i],"\\|");
            if(result != null && result.length > 0) {
                series.setCollection(result[0]);
                series.setPatientId(result[1]);
                series.setStudyInstanceUid(result[2]);
                series.setModality(result[3]);
                series.setSeriesInstanceUid(result[4]);
                if(result[5].equalsIgnoreCase("yes") ){
                    series.setHasAnnotation(true);
                }else if(Boolean.valueOf(result[5])){
                    series.setHasAnnotation(true);
                }
                //series.setHasAnnotation(Boolean.valueOf(result[4]));
                series.setNumberImages(result[6]);
                series.setImageUid(result[7]);
                series.setImagesSize(Integer.valueOf(result[8]));
                series.setAnnoSize(Integer.valueOf(result[9]));

                series.setUrl(result[10]);
                series.setDisplayName(result[11]);
                series.setLocal(Boolean.valueOf(result[12]));

                seriesDataList.add(series);
            }
        }
        return seriesDataList;
    }
}