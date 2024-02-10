package edu.jsu.mcis.cs310;

import com.github.cliftonlabs.json_simple.*;
import com.opencsv.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Arrays;
import java.io.IOException;

public class ClassSchedule {
    
    private final String CSV_FILENAME = "jsu_sp24_v1.csv";
    private final String JSON_FILENAME = "jsu_sp24_v1.json";
    
    private final String CRN_COL_HEADER = "crn";
    private final String SUBJECT_COL_HEADER = "subject";
    private final String NUM_COL_HEADER = "num";
    private final String DESCRIPTION_COL_HEADER = "description";
    private final String SECTION_COL_HEADER = "section";
    private final String TYPE_COL_HEADER = "type";
    private final String CREDITS_COL_HEADER = "credits";
    private final String START_COL_HEADER = "start";
    private final String END_COL_HEADER = "end";
    private final String DAYS_COL_HEADER = "days";
    private final String WHERE_COL_HEADER = "where";
    private final String SCHEDULE_COL_HEADER = "schedule";
    private final String INSTRUCTOR_COL_HEADER = "instructor";
    private final String SUBJECTID_COL_HEADER = "subjectid";
    
    public String convertCsvToJsonString(List<String[]> csv) {
        // Create "Outer" JSON Container
        
        JsonObject json = new JsonObject();
        
        //Create "inner" JSON Containers
        
        JsonObject scheduletype = new JsonObject();
        JsonObject subject = new JsonObject();
        JsonObject course = new JsonObject();
        ArrayList<JsonObject> section = new ArrayList<>();
        
        //Set up CSV iterator and then get reader row
        
        Iterator<String[]> iterator =csv.iterator();
        String[] headerRow =iterator.next();
        
        HashMap<String, Integer> headers = new HashMap<>();
        for (int i =0; i<headerRow.length; ++i){
            headers.put(headerRow[i], i);
        }
        
        //Declare the json string
        String jsonstring = "";
        
        //Process CSV records
        
        while (iterator.hasNext()){
            String[] record  =iterator.next();
            
            //Get the values from the record.
           
            Integer crn = Integer.valueOf(record[headers.get(CRN_COL_HEADER)]);
            Integer credits = Integer.valueOf(record[headers.get(CREDITS_COL_HEADER)]);
            
            //TAKE num as string to split the course and its number.
            String num = record[headers.get(NUM_COL_HEADER)];
            
            //Lets split them.
            String[] parts = num.split(" ");
            String COURSE = parts[0];
            String COURSENUM = parts[1];
            
            //Also lets separate the instructors.
            List<String> instructors = Arrays.asList(record[headers.get(INSTRUCTOR_COL_HEADER)].split(", "));
            
            
            //Now we can fill up the inner hashmap of the section array.
            JsonObject InSection = new JsonObject();
            InSection.put(CRN_COL_HEADER, crn);
            InSection.put(SUBJECTID_COL_HEADER,COURSE);
            InSection.put(NUM_COL_HEADER,COURSENUM);
            InSection.put(SECTION_COL_HEADER,record[headers.get(SECTION_COL_HEADER)]);
            InSection.put(TYPE_COL_HEADER,record[headers.get(TYPE_COL_HEADER)]);
            InSection.put(START_COL_HEADER,record[headers.get(START_COL_HEADER)]);
            InSection.put(END_COL_HEADER,record[headers.get(END_COL_HEADER)]);
            InSection.put(DAYS_COL_HEADER,record[headers.get(DAYS_COL_HEADER)]);
            InSection.put(END_COL_HEADER,record[headers.get(END_COL_HEADER)]);
            InSection.put(WHERE_COL_HEADER,record[headers.get(WHERE_COL_HEADER)]);
            InSection.put(INSTRUCTOR_COL_HEADER,instructors);
            
            //Lets add these inner section hashmaps to the main outer section array.
            section.add(InSection);
            
            //Now we move on to the inner course hashmap and we will fill it up.
            JsonObject InCourse = new JsonObject();
            InCourse.put(SUBJECTID_COL_HEADER, COURSE);
            InCourse.put(NUM_COL_HEADER,COURSENUM);
            InCourse.put(DESCRIPTION_COL_HEADER,record[headers.get(DESCRIPTION_COL_HEADER)]);
            InCourse.put(CREDITS_COL_HEADER, credits);
            
            
            //Now we can actually insert this inner course hashmap info to outer course hashmap
            course.put(record[headers.get(NUM_COL_HEADER)], InCourse);
            
            // Lets fill up the subject hashmap
            subject.put(COURSE,record[headers.get(SUBJECT_COL_HEADER)]);
            
            //scheduletype hashmap fillup
            scheduletype.put(record[headers.get(TYPE_COL_HEADER)],record[headers.get(SCHEDULE_COL_HEADER)]);
            
            //Lets fill the outermost hashmaps with all the hashmaps
            json.put("scheduletype", scheduletype);
            json.put("subject", subject);
            json.put("course", course);
            json.put("section", section);
            
            // Serialize the json object to json string
            jsonstring = Jsoner.serialize(json);
            
         }
        
        return jsonstring;
        
    }
    
    public String convertJsonToCsvString(JsonObject json) {
        
        // Lets get the objects from JSON first
        
        JsonObject scheduletype = (JsonObject)json.get("scheduletype");
        JsonObject subjects = (JsonObject)json.get("subject");
        JsonObject course = (JsonObject)json.get("course");
        JsonArray section = (JsonArray)json.get("section");
        
        // Lets declare the csv string
        
        String csvString ="";
        
        //We need to write in CSV format using CSv string
        
        try (StringWriter writer = new StringWriter();
            CSVWriter csvwriter = new CSVWriter(writer,'\t', '"', '\\', "\n" )){
            
            //Lets put in the headers for the csv file
            
            csvwriter.writeNext(new String[]{"crn", "subject", "num", "description", "section", "type", "credits", "start", "end", "days", "where", "schedule", "instructor"});
            
            //Now we need a iterator to construct the csv rows
            
            for(Object Obj:section ){
                JsonObject sections =(JsonObject) Obj;
                
                //Extract the data from Json objects
                String crn = String.valueOf(sections.get(CRN_COL_HEADER));
                String subjID = (String)sections.get(SUBJECTID_COL_HEADER);
                String subject= (String)subjects.get(subjID);
                String get_Num = (String)sections.get(NUM_COL_HEADER);
                String num = subjID + " " + get_Num;
                
                JsonObject incourse = (JsonObject)course.get(num);
                
                // Now get the incourse data
                
                String description = (String)incourse.get(DESCRIPTION_COL_HEADER);
                String sectionId = (String)sections.get(SECTION_COL_HEADER);
                String type = (String)sections.get(TYPE_COL_HEADER);
                String credits = String.valueOf(incourse.get(CREDITS_COL_HEADER));
                String start = (String)sections.get(START_COL_HEADER);
                String end = (String)sections.get(END_COL_HEADER);
                String days = (String)sections.get(DAYS_COL_HEADER);
                String where = (String)sections.get(WHERE_COL_HEADER);
                String schedule = (String)scheduletype.get(type);
                
                //Get the instructpr details now
                JsonArray instructorss = (JsonArray)sections.get(INSTRUCTOR_COL_HEADER);
                List<String> instructorssList = new ArrayList<>();
                for (Object instructors: instructorss){
                    instructorssList.add(instructors.toString());
                }
                
                String instructor = String.join(", ", instructorssList);
                
                
                //Lets write the CSV row now
                
                csvwriter.writeNext(new String[]{crn, subject, num, description, sectionId, type, credits, start, end, days, where, schedule, instructor});
                
                
            }
            
            //COnverting CSV writer to string
            csvString = writer.toString();
            
        } catch (IOException e){
            e.printStackTrace();
        }
        
                
            
                    
        
        
        
        return csvString; 
        
    }
    
    public JsonObject getJson() {
        
        JsonObject json = getJson(getInputFileData(JSON_FILENAME));
        return json;
        
    }
    
    public JsonObject getJson(String input) {
        
        JsonObject json = null;
        
        try {
            json = (JsonObject)Jsoner.deserialize(input);
        }
        catch (Exception e) { e.printStackTrace(); }
        
        return json;
        
    }
    
    public List<String[]> getCsv() {
        
        List<String[]> csv = getCsv(getInputFileData(CSV_FILENAME));
        return csv;
        
    }
    
    public List<String[]> getCsv(String input) {
        
        List<String[]> csv = null;
        
        try {
            
            CSVReader reader = new CSVReaderBuilder(new StringReader(input)).withCSVParser(new CSVParserBuilder().withSeparator('\t').build()).build();
            csv = reader.readAll();
            
        }
        catch (Exception e) { e.printStackTrace(); }
        
        return csv;
        
    }
    
    public String getCsvString(List<String[]> csv) {
        
        StringWriter writer = new StringWriter();
        CSVWriter csvWriter = new CSVWriter(writer, '\t', '"', '\\', "\n");
        
        csvWriter.writeAll(csv);
        
        return writer.toString();
        
    }
    
    private String getInputFileData(String filename) {
        
        StringBuilder buffer = new StringBuilder();
        String line;
        
        ClassLoader loader = ClassLoader.getSystemClassLoader();
        
        try {
        
            BufferedReader reader = new BufferedReader(new InputStreamReader(loader.getResourceAsStream("resources" + File.separator + filename)));

            while((line = reader.readLine()) != null) {
                buffer.append(line).append('\n');
            }
            
        }
        catch (Exception e) { e.printStackTrace(); }
        
        return buffer.toString();
        
    }
    
}