package com.github.cunvoas.dbf2cvs;

import java.io.*;
import com.linuxense.javadbf.*;

public class ParseDbf {
    static String dbf="/work/PERSO/ASSO/data/grille200m_metropole.dbf";
    static String csv="/work/PERSO/ASSO/data/grille200m_metropole.csv";


/**
 * @see https://www.insee.fr/fr/statistiques/4176290?sommaire=4176305#documentation
 * */
    public static void main(String args[]) {

        DBFReader reader = null;
        FileWriter fileWriter = null;
        FileWriter metaWriter = null;
        try {
            fileWriter = new FileWriter(csv);
            metaWriter = new FileWriter(csv+".meta");
            // create a DBFReader object
            reader = new DBFReader(new FileInputStream(dbf));

            // get the field count if you want for some reasons like the following

            int numberOfFields = reader.getFieldCount();

            // use this count to fetch all field information
            // if required

            for (int i = 0; i < numberOfFields; i++) {

                DBFField field = reader.getField(i);

                metaWriter.write(i+" "+field.getName()+"\n");
                metaWriter.write("\tgetLength "+field.getLength()+"\n");
                metaWriter.write("\tgetType "+field.getType()+"\n");
                metaWriter.write("\tgetDecimalCount "+field.getDecimalCount()+"\n");

                // do something with it if you want
                // refer the JavaDoc API reference for more details
                //
                fileWriter.write(field.getName());
                fileWriter.write("\t");
            }
            fileWriter.write("\n");
            metaWriter.close();


            // Now, lets us start reading the rows

            Object[] rowObjects;

            while ((rowObjects = reader.nextRecord()) != null) {

                for (int i = 0; i < rowObjects.length; i++) {
                    if (i>0) {
                        fileWriter.write("\t");
                    }
                    fileWriter.write(rowObjects[i].toString());
                }
                fileWriter.write("\n");
                fileWriter.flush();
            }

            // By now, we have iterated through all of the rows

        } catch (DBFException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            DBFUtils.close(reader);
            try {
                fileWriter.close();

            } catch (Exception e) {

            }
        }
    }
}
