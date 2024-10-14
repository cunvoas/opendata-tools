package oneshot;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.commons.lang3.StringUtils;

public class OneShotInspireFiles {

	public static void main(String[] args) {
		
		//String file = "/work/PERSO/ASSO/data/loc_inspire_demo.csv";
		String file = "/work/PERSO/ASSO/data/loc_inspire_full.csv";
		
		String baseFold = "/work/PERSO/github/geoservice-data/localization/inspire/";

		int i=0;

		try (BufferedReader reader = new BufferedReader(new FileReader(file))){
			String line = null;
			// skip 1st
			line = reader.readLine();
			line = reader.readLine();
			while (line != null) {
		
				String key = StringUtils.left(line, 30);
				String loc = StringUtils.mid(line, 32, line.length()-33).replaceAll("\"\"", "\"");
//				System.out.println(key+" ### "+loc);
				
				String fold= StringUtils.left(key, 22);
				
				File d = new File(baseFold+fold);
				if (!d.isDirectory()) {
					d.mkdir();
				}
				FileWriter fw = new FileWriter(baseFold+fold+"/"+key+".json");
				fw.write(loc);
				fw.close();
				
				if (i % 10000==0) {
					System.out.println(i);
				}
				// read next line
				line = reader.readLine();
				i++;
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
